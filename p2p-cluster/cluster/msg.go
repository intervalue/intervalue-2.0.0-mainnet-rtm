package cluster

import (
	"time"
)

type msgType uint8

const (

	// validate and retransmit
	aliveMsg msgType = iota
	suspectMsg

	//ping - pong - bounce
	pingMsg
	pongMsg
	bouncePingMsg
	userMsg
	metaMsg

)

//via udp protocol
type ping struct {
	SeqNo uint32
	Join  bool //determine whether it is first-join or routine ping
	now   time.Time
}

//via udp protocol
//the node asks other nodes to do ping in the case of first receiving node-join event
type bouncePing struct {
	Addr string
}

//via udp protocol
type pong struct {
	SeqNo uint32
}

type userMsgHeader struct {
	UserMsgLen int // Encodes the byte lengh of user state
}

type meta struct {
	SeqNo uint32
	Addr string
	Items map[string]string
}