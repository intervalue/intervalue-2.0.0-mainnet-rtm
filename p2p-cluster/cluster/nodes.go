package cluster

import (
	"strings"
	"sync"
	"time"
)


/**
 *
 * Copyright © INVE FOUNDATION. All rights reserved.
 *
 * @Description:define how to maintain another nodes status.
 * @author: Francis.Deng
 * @version: V1.0
 */

//type nodesSet []*nodeState
type nodeStateType int

const (
	stateAlive nodeStateType = iota
	stateSuspect
	stateDead
)

var (
	//nss      []*nodeState          = []*nodeState{}
	nodeMap  map[string]*nodeState = make(map[string]*nodeState)
	nodeLock sync.RWMutex
)

//cluster node
type Node struct {
	Name string
	Addr string            // ip:port
	Meta map[string]string // key:value is used to describe node
}

//a view of another node
type nodeState struct {
	Node
	state       nodeStateType
	stateChange time.Time


}

func node(addr string) *nodeState {
	n, ok := nodeMap[addr]
	if !ok {
		n = nil
	}

	return n
}

func nodesExclude(exclude string) []nodeState {
	var nssCopy []nodeState

	//nodeLock.RLock()
	//defer nodeLock.RUnlock()

	// for _, nsPtr := range nss {
	// 	if strings.Compare(nsPtr.Addr, exclude) != 0 {
	// 		nssCopy = append(nssCopy, *nsPtr)
	// 	}

	// }
	for _, v := range nodeMap {
		if strings.Compare(v.Addr, exclude) != 0 {
			nssCopy = append(nssCopy, *v)
		}
	}

	return nssCopy
}

func nodeSetAdd(ns0 *nodeState) {
	//nodeLock.Lock()
	//defer nodeLock.Unlock()

	//if nodeSetContains(ns0) {
	//	nodeSetRemove(ns0)
	//}
	//nodeSetRemove(ns0)
	nodeMap[ns0.Addr] = ns0
	//nss = append(nss, ns0)
}

func nodeSetContains(ns0 *nodeState) bool {
	nodeLock.RLock()
	defer nodeLock.RUnlock()

	// for _, ns := range nss {
	// 	if strings.Compare(ns.Addr, ns0.Addr) == 0 {
	// 		return true
	// 	}
	// }
	_, ok := nodeMap[ns0.Addr]

	return ok
}

func nodeSetRemove(ns0 *nodeState) {
	nodeLock.Lock()
	defer nodeLock.Unlock()

	// for index, ns := range nss {
	// 	if strings.Compare(ns.Addr, ns0.Addr) == 0 {

	// 		delete(nodeMap, ns0.Addr)
	// 		nss = append(nss[:index], nss[index+1:]...)
	// 		return true
	// 	}
	// }
	delete(nodeMap, ns0.Addr)

}

func nodeSetMerge(nnss []nodeState){
	if (len(nnss) >0) {
		nodeLock.Lock()
		defer nodeLock.Unlock()
		var nonExistedNodes []nodeState

		for _,ns := range nnss{
			if n:=node(ns.Addr);n != nil{
				if ns.stateChange.After(n.stateChange) {
					n.state = ns.state
					n.stateChange = ns.stateChange
				}
			}  else {
				nonExistedNodes = append(nonExistedNodes,ns)
			}
		}

		for _,nonExistedNode := range nonExistedNodes{
			nodeSetAdd(&nonExistedNode)
		}
	}

}

func (c *Cluster) findTypedMembers(typed nodeStateType) []Node {
	joinedNodes := nodesExclude("")
	var knodes []Node

	if (len(joinedNodes) > 0){
		//knodes := make([]Node, 0, len(joinedNodes))

		for _, n := range joinedNodes {
			if n.state == typed {
				knodes = append(knodes, n.Node)
			}
		}
	}


	return knodes
}
