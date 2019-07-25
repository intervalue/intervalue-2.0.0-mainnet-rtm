package main

import (
	"flag"
	"fmt"
	"time"

	"github.com/intervalue/intervalue-2.0.0-mainnet-rtm/p2p-cluster/cluster"
)

var (
	host   string
	port   int
	guider string
)

func init() {
	flag.StringVar(&host, "h", "", "listening net address")
	flag.IntVar(&port, "p", -1, "listening port [0-65535]")
	flag.StringVar(&guider, "g", "", "work as guider waiting another peer to join [ip:port]")
}

//work as a guider peer:
//clus -h 192.168.207.129 -p 33010

//work as a follower of guider peer
//clus -h 192.168.207.129 -p 33011 -g 192.168.207.129:33010
func main() {
	flag.Parse()

	c0, err := NewCluster("", host, port)

	if err != nil {
		fmt.Printf("fail to create peer [%s:%d] %v\n", host, port, err)
		return
	}

	//join a guider peer
	if len(guider) > 0 {
		c0.Join([]string{guider})
	}

	for {
		for _, n := range c0.Members() {
			fmt.Printf("find peer node (%s) in a cluster\n", n.Addr)

		}

		time.Sleep(10 * time.Second)
	}
}

func NewCluster(name string, addr string, port int) (*cluster.Cluster, error) {
	conf := &cluster.Config{
		Name:     name,
		BindAddr: addr,
		BindPort: port,
	}

	return cluster.Create(conf)
}
