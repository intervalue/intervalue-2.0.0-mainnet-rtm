package cluster

import (
	"fmt"
	"log"
	"math/rand"
	"net"
	"os"
	"sync"
	"sync/atomic"
	"time"
)

/**
 *
 * Copyright © INVE FOUNDATION. All rights reserved.
 *
 * @Description:define cluster lifecycle
 * @author: Francis.Deng
 * @version: V1.0
 */

type receiptHandler struct {
	ackFn func([]byte, time.Time)
	timer *time.Timer
}

type receipt struct {
	Complete  bool
	Payload   []byte
	Timestamp time.Time
}

type Cluster struct {
	sequenceNum uint32 // Local sequence number

	config    *Config
	transport Transport

	shutdown     int32
	shutdownCh   chan struct{}
	shutdownLock sync.Mutex // Serializes calls to Shutdown

	//seqNo uint32

	tickerLock sync.Mutex
	tickers    []*time.Ticker

	//nodes []*nodeState //known nodes

	receiptLock     sync.Mutex
	receiptHandlers map[uint32]*receiptHandler

	logger *log.Logger
}

func newCluster(config *Config) (*Cluster, error) {
	logger := config.Logger
	if logger == nil {
		logger = log.New(os.Stderr, "", log.LstdFlags)
	}

	ntc := &NetTransportConfig{
		BindAddrs: []string{config.BindAddr},
		BindPort:  config.BindPort,
		Logger:    logger,
	}

	makeNetTry := func(limit int) (*NetTransport, error) {
		var e error
		for try := 0; try < limit; try++ {
			if nt, e := NewNetTransport(ntc); e == nil {
				return nt, nil
			}
		}

		return nil, fmt.Errorf("failed to obtain an address: %v", e)
	}

	transport, err := makeNetTry(1)
	if err != nil {
		return nil, fmt.Errorf("Could not set up network transport: %v", err)
	}

	c := &Cluster{
		config:     config,
		shutdownCh: make(chan struct{}),
		transport:  transport,
		logger:     logger,

		receiptHandlers: make(map[uint32]*receiptHandler),
	}

	go c.packetListen()

	return c, nil
}

//schedule is used to ensure the Tick is performed periodically
func (c *Cluster) schedule() {

}

func (c *Cluster) packetListen() {
	for {
		select {
		case pack := <-c.transport.PacketCh():
			c.handleCommand(pack.Buf, pack.From, pack.Timestamp)
		case <-c.shutdownCh:
			return
		}
	}
}

func (c *Cluster) rawSendMsgPacket(addr string, msg []byte) error {
	_, err := c.transport.WriteTo(msg, addr)
	return err
}

func (c *Cluster) encodeAndSendMsg(addr string, mType msgType, msg interface{}) error {
	out, err := encode(mType, msg)
	if err != nil {
		return err
	}

	if err := c.rawSendMsgPacket(addr, out.Bytes()); err != nil {
		return err
	}

	return nil
}

func (c *Cluster) handleCommand(buf []byte, from net.Addr, timestamp time.Time) {
	mType := msgType(buf[0])
	buf = buf[1:]

	switch mType {
	case aliveMsg:
		fallthrough
	case suspectMsg:
		fallthrough
	case pingMsg:
		c.handlePing(buf, from)
	case pongMsg:
		c.handlePong(buf, from, timestamp)
	case bouncePingMsg:
		c.handleBouncePing(buf, from)
	}
}

//trigger a function at a interval util a stop tick arrives
func (c *Cluster) triggerFunc(stagger time.Duration, C <-chan time.Time, stop <-chan struct{}, f func()) {
	randStagger := time.Duration(uint64(rand.Int63()) % uint64(stagger))

	select {
	case <-time.After(randStagger):
	case <-stop:
		return
	}

	for {
		select {
		case <-C:
			f()
		case <-stop:
			return
		}
	}
}

func (c *Cluster) nextSeqno() uint32 {
	return atomic.AddUint32(&c.sequenceNum, 1)
}

func (c *Cluster) handleBouncePing(buf []byte, from net.Addr) {
	var bPing bouncePing

	if err := decode(buf, &bPing); err != nil {
		c.logger.Printf("[ERR] cluster: Failed to decode bouncePing request: %s %s", err, LogAddress(from))
		return
	}

	if _, err := c.pingNode(bPing.Addr, false); err != nil {
		c.logger.Printf("[ERR] cluster: Failed to ping[ when bouncePing ] : %s %s", err, bPing.Addr)
	}

	return
}

//merge it and answer a pong response
func (c *Cluster) handlePing(buf []byte, from net.Addr) {
	var pi ping
	var po pong

	if err := decode(buf, &pi); err != nil {
		c.logger.Printf("[ERR] cluster: Failed to decode ping request: %s %s", err, LogAddress(from))
		return
	}

	// ns := &nodeState{
	// 	state:       stateAlive,
	// 	stateChange: time.Now(),
	// }
	// ns.Addr = from.String()
	// nodeSetAdd(ns)
	c.mergeNode(from.String())

	po.SeqNo = pi.SeqNo
	c.encodeAndSendMsg(from.String(), pongMsg, &po)

	if pi.Join { //first join to notify other nodes to ping together
		bounce := bouncePing{from.String()}

		for _, node := range joinedNodes() {
			c.encodeAndSendMsg(node.Addr, bouncePingMsg, &bounce)
		}
	}

	return
}

//merge it
func (c *Cluster) handlePong(buf []byte, from net.Addr, timestamp time.Time) {
	c.mergeNode(from.String())
}

//after receiving ping and pong,merge it into local node table
func (c *Cluster) mergeNode(addr string) {
	oldNs := node(addr)
	ns := &nodeState{
		state:       stateAlive,
		stateChange: time.Now(),
	}
	ns.Addr = addr

	if oldNs != nil {
		ns.Name = oldNs.Name
		ns.Meta = oldNs.Meta
	}

	nodeSetAdd(ns)
}

//emitting a Ping event
func (c *Cluster) ping(node *nodeState, join bool) (time.Duration, error) {
	pi := ping{c.nextSeqno(), join, time.Now()}
	receiptCh := make(chan receipt)
	c.setPingPongChannel(pi.SeqNo, receiptCh, c.config.ProbeInterval)

	if err := c.encodeAndSendMsg(node.Addr, pingMsg, &pi); err != nil {
		return 0, err
	}

	sent := time.Now()
	select {
	case receipt := <-receiptCh:
		if receipt.Complete {
			return receipt.Timestamp.Sub(sent), nil
		}
	case <-time.After(c.config.ProbeTimeout):
	}

	return 0, NoPongError{node.Addr}
}

func (c *Cluster) pingNode(addr string, join bool) (time.Duration, error) {
	node := nodeState{}
	node.Addr = addr

	return c.ping(&node, join)
}

func (c *Cluster) setPingPongChannel(seqNo uint32, receiptCh chan receipt, timeout time.Duration) {
	receiptFn := func(payload []byte, timestamp time.Time) {
		select {
		case receiptCh <- receipt{true, payload, timestamp}:
		default:
		}
	}

	rh := &receiptHandler{receiptFn, nil}
	c.receiptLock.Lock()
	c.receiptHandlers[seqNo] = rh
	c.receiptLock.Unlock()

	rh.timer = time.AfterFunc(timeout, func() {
		c.receiptLock.Lock()
		delete(c.receiptHandlers, seqNo)
		c.receiptLock.Unlock()

		select {
		case receiptCh <- receipt{false, nil, time.Now()}:
		default:
		}
	})
}

func (c *Cluster) Shutdown() error {
	c.shutdownLock.Lock()
	defer c.shutdownLock.Unlock()

	if c.hasShutdown() {
		return nil
	}

	if err := c.transport.Shutdown(); err != nil {
		c.logger.Printf("[ERR] Failed to shutdown transport: %v", err)
	}

	atomic.StoreInt32(&c.shutdown, 1)
	close(c.shutdownCh)

	return nil
}

func (c *Cluster) hasShutdown() bool {
	return atomic.LoadInt32(&c.shutdown) == 1
}
