package main

import (
	"flag"
	"fmt"
	"github.com/intervalue/intervalue-2.0.0-mainnet-rtm/p2p-cluster/cluster"
	"github.com/intervalue/intervalue-2.0.0-mainnet-rtm/p2p-cluster/ic"
	"time"
)

/**
 *
 * Copyright © INVE FOUNDATION. All rights reserved.
 *
 * @Description: cluster daemon tool,it's a good example to demonstrate how to use Cluster
 * @author: Francis.Deng
 * @version: V1.0
 * @version: v1.1 add "icport" flag to specify ic port
 */

var (
	_host   string
	_port   int
	_guider string
	_icport int
)

func init() {
	flag.StringVar(&_host, "h", "", "listening net address [ipv4 format]")
	flag.IntVar(&_port, "p", -1, "listening port [0-65535]")
	flag.StringVar(&_guider, "g", "", "work as guider waiting another peer to join [ip:port]")
	flag.IntVar(&_icport, "icp", -1, "inter communication(ic) listening port [0-65535]")
}

//work as a guider peer:
//p2pclusterdm -h 192.168.207.129 -p 3308 -icp 4408

//work as a follower of guider peer
//p2pclusterdm -h 192.168.207.129 -p 3309 -g 192.168.207.129:3308 -icp 4409
func main() {
	flag.Parse()

	start(_host, _port, _guider, _icport)
}

func start(host string, port int, guider string, icport int) {
	//flag.Parse()

	c0, err := cluster.NewCluster("", host, port)

	if err != nil {
		fmt.Printf("fail to create peer [%s:%d] %v\n", host, port, err)
		return
	}

	//join a guider peer
	if len(guider) > 0 {
		c0.Join([]string{guider})
	}

	//time.AfterFunc(20 * time.Second,func(){
	//	c0.Set("name","dodge")
	//	c0.Set("rand",strconv.Itoa(rand.Int()))
	//
	//	c0.TransmitMeta()
	//})

	// start cluster inter-communication service
	go ic.StartClusterIcListener(icport, c0)

	for {
		for _, n := range c0.AliveMembers() {
			fmt.Printf("find alive node: %s %v\n", n.Addr, n.Meta)
		}
		for _, n := range c0.SuspectedMembers() {
			fmt.Printf("find suspected node: %s %v\n", n.Addr, n.Meta)
		}
		for _, n := range c0.DeadMembers() {
			fmt.Printf("find dead node: %s %v\n", n.Addr, n.Meta)
		}
		fmt.Println("")

		time.Sleep(10 * time.Second)
	}
}
