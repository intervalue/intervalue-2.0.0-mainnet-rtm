package cluster

import (
	"fmt"
	"log"
	"net"
	"sync"
	"sync/atomic"
	"time"
)

const (
	udpPacketBufSize = 65536
	udpRecvBufSize   = 5 * 1024 * 1024
)

type NetTransportConfig struct {
	BindAddrs []string
	BindPort  int

	Logger *log.Logger
}

type NetTransport struct {
	config       *NetTransportConfig
	packetCh     chan *Packet
	streamCh     chan net.Conn
	logger       *log.Logger
	wg           sync.WaitGroup
	tcpListeners []*net.TCPListener
	udpListeners []*net.UDPConn
	shutdown     int32
}

//return a NetTranport with the given configuration
func NewNetTransport(config *NetTransportConfig) (*NetTransport, error) {
	if len(config.BindAddrs) == 0 {
		return nil, fmt.Errorf("At least one bind address is required")
	}

	var allRight bool
	transport := NetTransport{
		config:   config,
		packetCh: make(chan *Packet),
		streamCh: make(chan net.Conn),
		logger:   config.Logger,
	}

	defer func() {
		if !allRight {
			transport.Shutdown()
		}
	}()

	port := config.BindPort
	for _, addr := range config.BindAddrs {
		ip := net.ParseIP(addr)

		tcpAddr := &net.TCPAddr{IP: ip, Port: port}
		tcpLn, err := net.ListenTCP("tcp", tcpAddr)
		if err != nil {
			return nil, fmt.Errorf("Failed to start TCP listener on %q port %d: %v", addr, port, err)
		}
		transport.tcpListeners = append(transport.tcpListeners, tcpLn)

		udpAddr := &net.UDPAddr{IP: ip, Port: port}
		udpLn, err := net.ListenUDP("udp", udpAddr)

		if err := setUDPRecvBuf(udpLn); err != nil {
			return nil, fmt.Errorf("Failed to resize UDP buffer: %v", err)
		}
		transport.udpListeners = append(transport.udpListeners, udpLn)
	}

	for i := 0; i < len(config.BindAddrs); i++ {
		transport.wg.Add(2)

		go transport.tcpListen(transport.tcpListeners[i])
		go transport.udpListen(transport.udpListeners[i])
	}

	allRight = true
	return &transport, nil
}

func (t *NetTransport) tcpListen(tcpLn *net.TCPListener) {
	defer t.wg.Done()

	for {
		conn, err := tcpLn.AcceptTCP()
		if err != nil {
			if s := atomic.LoadInt32(&t.shutdown); s == 1 {
				break
			}

			t.logger.Printf("[ERR] memberlist: Error accepting TCP connection: %v", err)
			continue
		}

		t.streamCh <- conn
	}
}

func (t *NetTransport) udpListen(udpLn *net.UDPConn) {
	defer t.wg.Done()
	for {
		buf := make([]byte, udpPacketBufSize)

		n, addr, err := udpLn.ReadFrom(buf)
		ts := time.Now()
		if err != nil {
			if s := atomic.LoadInt32(&t.shutdown); s == 1 {
				break
			}

			t.logger.Printf("[ERR] memberlist: Error reading UDP packet: %v", err)
			continue
		}

		t.packetCh <- &Packet{
			Buf:       buf[:n],
			From:      addr,
			Timestamp: ts,
		}
	}
}

//Transport interface
func (t *NetTransport) Shutdown() error {
	atomic.StoreInt32(&t.shutdown, 1)

	for _, conn := range t.tcpListeners {
		conn.Close()
	}
	for _, conn := range t.udpListeners {
		conn.Close()
	}

	t.wg.Wait()
	return nil
}

//Transport interface
func (t *NetTransport) PacketCh() <-chan *Packet {
	return t.packetCh
}

//Transport interface
func (t *NetTransport) StreamCh() <-chan net.Conn {
	return t.streamCh
}

//Transport interface
func (t *NetTransport) WriteTo(b []byte, addr string) (time.Time, error) {
	udpAddr, err := net.ResolveUDPAddr("udp", addr)
	if err != nil {
		return time.Time{}, err
	}

	_, err = t.udpListeners[0].WriteTo(b, udpAddr)
	return time.Now(), nil
}

//Transport interface
func (t *NetTransport) DialTimeout(addr string, timeout time.Duration) (net.Conn, error) {
	dialer := net.Dialer{Timeout: timeout}
	return dialer.Dial("tcp", addr)
}

func setUDPRecvBuf(c *net.UDPConn) error {
	size := udpRecvBufSize
	var err error
	for size > 0 {
		if err = c.SetReadBuffer(size); err == nil {
			return nil
		}

		size = size / 2
	}

	return err
}
