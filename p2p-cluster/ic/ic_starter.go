package ic

import (
	"log"
	"net"
	"strconv"

	"google.golang.org/grpc"
	"github.com/intervalue/intervalue-2.0.0-mainnet-rtm/p2p-cluster/cluster"
)

/**
 *
 * Copyright © INVE FOUNDATION. All rights reserved.
 *
 * @Description: start a inter-communication service
 * @See: clusterIcImpl
 * @author: Francis.Deng
 * @version: V1.0
 */

func StartClusterIcListener(icport int,c *cluster.Cluster){
	address := "localhost:" + strconv.Itoa(icport)
	tcpAddr, err := net.ResolveTCPAddr("tcp", address)
	if err != nil {
		log.Fatalf("%s : %v", address, err)
		return
	}

	//listener, err := net.Listen("tcp", tcpAddr)
	listener, err := net.ListenTCP("tcp", tcpAddr)
	if err != nil {
		log.Fatalf("fail to listen %s : %v", address, err)
		return
	}

	s := grpc.NewServer()
	RegisterClusterServer(s, &clusterIcImpl{c})

	log.Printf("p2p cluster ic is listening on : %s", address)

	err = s.Serve(listener)
	if err != nil{
		log.Fatalf("fail to start p2p cluster: %v", err)
	}
}