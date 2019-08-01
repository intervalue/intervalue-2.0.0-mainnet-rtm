package main

import "C"
import "github.com/intervalue/intervalue-2.0.0-mainnet-rtm/p2p-cluster/cluster"

/**
 *
 * Copyright © INVE FOUNDATION. All rights reserved.
 *
 * @Description: dll(go build -buildmode=c-shared -o libp2pcluster.dll p2pcluster.go) or
                 so(go build -buildmode=c-shared -o libp2pcluster.so p2pcluster.go) library
 * @author: Francis.Deng
 * @version: V1.0
 */

var (
	host   string
	port   int
	guider string
	c *cluster.Cluster
)

//export SetLocal
func SetLocal(h string,p int){
	host = h
	port = p
}

//export SetGuider
func SetGuider(g string){
	guider = g
}

//export Start
func Start() bool {
	c, err := cluster.NewCluster("", host, port)
	if err != nil{
		return false
	}

	if len(guider)>0{
		c.Join([]string{guider})
	}

	return true
}

//export GetAliveMembers
func GetAliveMembers() []cluster.Node{
	return c.Members()
}

//export GetSuspectedMembers
func GetSuspectedMembers() []cluster.Node{
	return c.SuspectedMembers()
}

//export Shutdown
func Shutdown(){

}

func main(){

}