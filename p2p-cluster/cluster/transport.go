package cluster

import (
	"net"
	"time"
)

type Packet struct {
	Buf []byte

	From      net.Addr
	Timestamp time.Time
}

type Transport interface {
	WriteTo(b []byte, addr string) (time.Time, error)
	DialTimeout(addr string, timeout time.Duration) (net.Conn, error)

	PacketCh() <-chan *Packet
	StreamCh() <-chan net.Conn

	Shutdown() error
}
