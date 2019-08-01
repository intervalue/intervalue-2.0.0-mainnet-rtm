package main

import (
	"flag"
	"fmt"
	"github.com/intervalue/intervalue-2.0.0-mainnet-rtm/p2p-cluster/cluster"
	"math/rand"
	"time"
	"strconv"
)

/**
 *
 * Copyright © INVE FOUNDATION. All rights reserved.
 *
 * @Description: cluster client tool,it's a good example to demonstrate how to use Cluster
 * @author: Francis.Deng
 * @version: V1.0
 */

var (
	host   string
	port   int
	guider string
)

func init() {
	flag.StringVar(&host, "h", "", "listening net address [ipv4 format]")
	flag.IntVar(&port, "p", -1, "listening port [0-65535]")
	flag.StringVar(&guider, "g", "", "work as guider waiting another peer to join [ip:port]")
}

//work as a guider peer:
//clus -h 192.168.207.129 -p 33010

//work as a follower of guider peer
//clus -h 192.168.207.129 -p 33011 -g 192.168.207.129:33010
func main() {
	flag.Parse()

	c0, err := cluster.NewCluster("", host, port)

	if err != nil {
		fmt.Printf("fail to create peer [%s:%d] %v\n", host, port, err)
		return
	}

	//join a guider peer
	if len(guider) > 0 {
		c0.Join([]string{guider})
	}

	time.AfterFunc(20 * time.Second,func(){
		c0.Set("name","dodge")
		c0.Set("rand",strconv.Itoa(rand.Int()))

		c0.TransmitMeta()
	})

	for {
		for _, n := range c0.Members() {
			fmt.Printf("find alive node: %s %v\n", n.Addr,n.Meta)
		}
		for _, n := range c0.SuspectedMembers() {
			fmt.Printf("find suspected node: %s %v\n", n.Addr,n.Meta)
		}
		for _, n := range c0.DeadMembers() {
			fmt.Printf("find dead node: %s %v\n", n.Addr,n.Meta)
		}
		fmt.Println("")

		time.Sleep(10 * time.Second)
	}
}


