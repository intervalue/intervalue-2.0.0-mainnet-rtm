package cluster

import (
	"strings"
	"sync"
	"time"
)

//type nodesSet []*nodeState

const (
	stateAlive nodeStateType = iota
	stateSuspect
	stateDead
)

var (
	nss      []*nodeState          = []*nodeState{}
	nodeMap  map[string]*nodeState = make(map[string]*nodeState)
	nodeLock sync.RWMutex
)

type nodeStateType int

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

func joinedNodes() []nodeState {
	var nssCopy []nodeState

	nodeLock.RLock()
	defer nodeLock.RUnlock()

	for _, nsPtr := range nss {
		nssCopy = append(nssCopy, *nsPtr)
	}

	return nssCopy
}

func nodeSetAdd(ns0 *nodeState) {
	if nodeSetContains(ns0) {
		nodeSetRemove(ns0)
	}

	nodeMap[ns0.Addr] = ns0
	nss = append(nss, ns0)
}

func nodeSetContains(ns0 *nodeState) bool {
	nodeLock.RLock()
	defer nodeLock.RUnlock()

	for _, ns := range nss {
		if strings.Compare(ns.Addr, ns0.Addr) == 0 {
			return true
		}
	}

	return false
}

func nodeSetRemove(ns0 *nodeState) bool {
	nodeLock.Lock()
	defer nodeLock.Unlock()

	for index, ns := range nss {
		if strings.Compare(ns.Addr, ns0.Addr) == 0 {

			delete(nodeMap, ns0.Addr)
			nss = append(nss[:index], nss[index+1:]...)
			return true
		}
	}

	return false
}
