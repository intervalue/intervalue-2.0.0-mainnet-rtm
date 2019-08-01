package cluster

import (
	"fmt"
	//"github.com/hashicorp/go-msgpack/codec"
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
	ackFn func(time.Time)
	timer *time.Timer
}

type receipt struct {
	Complete  bool
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
	stopTick   chan struct{}
	//probeIndex int

	//nodes []*nodeState //known nodes

	receiptLock     sync.Mutex
	receiptHandlers map[uint32]*receiptHandler

	rustedMetaIds map[string]uint32//which is used to avoid meta message flood - address:id

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
		rustedMetaIds:make(map[string]uint32),
	}

	go c.packetListen()

	return c, nil
}

//schedule is used to ensure the Tick is performed periodically
func (c *Cluster) schedule() {
	c.tickerLock.Lock()
	defer c.tickerLock.Unlock()

	//it is safe to call it many times
	if len(c.tickers) > 0{
		return
	}

	// when we should stop the tickers.
	stopCh := make(chan struct{})

	if (c.config.ProbeInterval>0){
		t:=time.NewTicker(c.config.ProbeInterval)
		go c.triggerFunc(c.config.ProbeInterval,t.C,stopCh,c.probe)
		c.tickers = append(c.tickers,t)
	}

	// If we made any tickers, then record the stopTick channel for
	// later.
	if len(c.tickers) > 0 {
		c.stopTick = stopCh
	}

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

func (c *Cluster) rawSendMsgStream(conn net.Conn, sendBuf []byte) error {
	if n, err := conn.Write(sendBuf); err != nil {
		return err
	} else if n != len(sendBuf) {
		return fmt.Errorf("only %d of %d bytes written", n, len(sendBuf))
	}

	return nil
}

//func (c *Cluster) sendUserMsg(addr string, sendBuf []byte) error {
//	conn, err := c.transport.DialTimeout(addr, c.config.TCPTimeout)
//	if err != nil {
//		return err
//	}
//	defer conn.Close()
//
//	bufConn := bytes.NewBuffer(nil)
//	if err := bufConn.WriteByte(byte(userMsg)); err != nil {
//		return err
//	}
//
//	header := userMsgHeader{UserMsgLen: len(sendBuf)}
//	hd := codec.MsgpackHandle{}
//	enc := codec.NewEncoder(bufConn, &hd)
//	if err := enc.Encode(&header); err != nil {
//		return err
//	}
//	if _, err := bufConn.Write(sendBuf); err != nil {
//		return err
//	}
//	return c.rawSendMsgStream(conn, bufConn.Bytes())
//}

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
	case metaMsg:
		c.handleMeta(buf,from)
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


func (c *Cluster) handleMeta(buf []byte, from net.Addr) {
	var metaMsg meta

	if err := decode(buf, &metaMsg); err != nil {
		c.logger.Printf("[ERR] cluster: Failed to decode meta request: %s %s", err, LogAddress(from))
		return
	}

	//supply original address because of public ip issue.
	if len(metaMsg.Addr) == 0{
		metaMsg.Addr = from.String()
	}

	//old,rusted meta-message id
	if id,ok := c.rustedMetaIds[metaMsg.Addr];ok && id >= metaMsg.SeqNo{
		return
	}
	c.rustedMetaIds[metaMsg.Addr] = metaMsg.SeqNo

	c.mergeNodeMeta(&metaMsg)

	//should not transmit msg to originator or last host who delivered to me
	c.retransmitMeta(&metaMsg,func(n Node) bool{
		if metaMsg.Addr == n.Addr || from.String() == n.Addr{
			return true
		}

		return false
	})

}

func (c *Cluster) mergeNodeMeta(m *meta) {
	oldNs := node(m.Addr)
	if oldNs != nil {
		ns := &nodeState{
			state:       stateAlive,
			stateChange: time.Now(),
		}
		ns.Meta = m.Items
		ns.Addr = m.Addr

		nodeSetAdd(ns)
	}
}

func (c *Cluster) handleBouncePing(buf []byte, from net.Addr) {
	var bPing bouncePing

	if err := decode(buf, &bPing); err != nil {
		c.logger.Printf("[ERR] cluster: Failed to decode bouncePing request: %s %s", err, LogAddress(from))
		return
	}

	//avoid the main routine to freeze
	//if _, err := c.pingNode(bPing.Addr, false); err != nil {
	//	c.logger.Printf("[ERR] cluster: Failed to ping (when bouncePing) : %v ", err)
	//}
	go c.pingNode(bPing.Addr, false)

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
	fromAddr := from.String()

	if pi.Join == true { //first join to call other nodes to ping together
		bounce := bouncePing{fromAddr}

		for _, node := range nodesExclude(fromAddr) {
			c.encodeAndSendMsg(node.Addr, bouncePingMsg, &bounce)
		}
	}

}

//merge it
func (c *Cluster) handlePong(buf []byte, from net.Addr, timestamp time.Time) {
	var po pong

	if err := decode(buf, &po); err != nil {
		c.logger.Printf("[ERR] cluster: Failed to decode pong request: %s %s", err, LogAddress(from))
		return
	}

	c.invokeReciptHandler(po, time.Now())
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

//better solution is to select random k node to retransmit,not all nodes
func (c *Cluster) retransmitMeta(me *meta, filterFn func(Node) bool) {
	aliveNodes := c.findTypedMembers(stateAlive)
	for _,aliveNode:=range aliveNodes{
		if !filterFn(aliveNode){
			err := c.encodeAndSendMsg(aliveNode.Addr, metaMsg, &me)
			c.logger.Printf("[ERR] Failed to send meta to %s during retransmission due to exception: %v", aliveNode.Addr,err)
		}

	}
}

func (c *Cluster) transmitMeta(metaItems map[string]string) error {
	aliveNodes := c.findTypedMembers(stateAlive)

	if len(aliveNodes) > 0 {
		rand := int(rand.Int31()) % len(aliveNodes)

		me:=meta{SeqNo:c.nextSeqno(),Items:metaItems}

		return c.encodeAndSendMsg(aliveNodes[rand].Addr, metaMsg, me)
	}

	return fmt.Errorf("error in finding no node")
}

//emitting a Ping event
func (c *Cluster) ping(node *nodeState, join bool) (time.Duration, error) {
	pi := ping{c.nextSeqno(), join, time.Now()}
	receiptCh := make(chan receipt,1)
	c.setPingPongChannel(pi.SeqNo, receiptCh, c.config.ProbeInterval)

	if err := c.encodeAndSendMsg(node.Addr, pingMsg, &pi); err != nil {
		return 0, err
	}

	sent := time.Now()
	select {
	case receipt := <-receiptCh:
		if receipt.Complete == true {
			return receipt.Timestamp.Sub(sent), nil
		}

	case <-time.After(c.config.ProbeTimeout):
		c.logger.Printf("[ERR] Failed to get Pong from %v due to timeout: %v", node.Addr,c.config.ProbeTimeout)
	}

	return 0, NoPongError{node.Addr}


}

// Invokes an pong handler if any is associated
func (c *Cluster) invokeReciptHandler(po pong, timestamp time.Time) {
	c.receiptLock.Lock()
	rh, ok := c.receiptHandlers[po.SeqNo]
	delete(c.receiptHandlers, po.SeqNo)
	c.receiptLock.Unlock()
	if !ok {
		return
	}
	rh.timer.Stop()
	rh.ackFn(timestamp)
}

func (c *Cluster) pingNode(addr string, join bool) (time.Duration, error) {
	node := nodeState{}
	node.Addr = addr

	return c.ping(&node, join)
}

func (c *Cluster) setPingPongChannel(seqNo uint32, receiptCh chan receipt, timeout time.Duration) {
	receiptFn := func(timestamp time.Time) {
		select {
		case receiptCh <- receipt{true, timestamp}:
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
		case receiptCh <- receipt{false, time.Now()}:
		default:
		}
	})
}

func (c *Cluster) hasShutdown() bool {
	return atomic.LoadInt32(&c.shutdown) == 1
}


//start a single round of failure detection
func (c *Cluster) probe(){
	//numCheck:=0
	nodes := nodesExclude("")
	var unPingedNodes []nodeState
	wg := sync.WaitGroup{}

	if len(nodes)>0 {
		wg.Add(len(nodes))

		//nodeLock.RLock()
		//if numCheck >= len(nodesExclude("")){
		//	nodeLock.RUnlock()
		//	return
		//}

		for _,node:=range nodes{
			//change node state from stateAlive to stateSuspect
			if node.state == stateAlive{
				go func (){
					_,err := c.ping(&node,false)
					if err != nil{
						node.state = stateSuspect
						node.stateChange = time.Now()
						unPingedNodes = append(unPingedNodes,node)
					}

					wg.Done()

				}()
			}
			//change node state from stateSuspect to stateAlive
			if node.state == stateSuspect{
				go func (){
					_,err := c.ping(&node,false)
					if err == nil{
						node.state = stateAlive
						node.stateChange = time.Now()
						unPingedNodes = append(unPingedNodes,node)
					}

					wg.Done()

				}()
			}
		}

		//merge unpinged nodes
		wg.Wait()
		nodeSetMerge(unPingedNodes)
	}


}

func (c *Cluster) deschedule() {
	c.tickerLock.Lock()
	defer c.tickerLock.Unlock()

	// If we have no tickers, then we aren't scheduled.
	if len(c.tickers) == 0 {
		return
	}

	// Close the stop channel so all the ticker listeners stop.
	close(c.stopTick)

	// Explicitly stop all the tickers themselves so they don't take
	// up any more resources, and get rid of the list.
	for _, t := range c.tickers {
		t.Stop()
	}
	c.tickers = nil
}


