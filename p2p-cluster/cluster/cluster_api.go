package cluster

import "sync/atomic"

/**
 *
 * Copyright © INVE FOUNDATION. All rights reserved.
 *
 * @Description:define useful cluster api any invoker is able to use
 * @author: Francis.Deng
 * @version: V1.0
 */

func Create(config *Config) (*Cluster, error) {
	c, err := newCluster(config)
	if err != nil {
		return nil, err
	}

	c.schedule()

	return c, nil
}

//Join is just firing a ping message,which doesn't mean the node is accepted by cluster
func (c *Cluster) Join(existed []string) {
	for _, exist := range existed {
		c.pingNode(exist, true)
	}
}

var metaItems map[string]string = make(map[string]string)

func (c *Cluster) Set(k string,v string){
	metaItems[k] = v
}

func (c *Cluster) TransmitMeta(){
	c.transmitMeta(metaItems)
}

// Members returns a list of all known alive nodes.
func (c *Cluster) AliveMembers() []Node {
	//joinedNodes := nodesExclude("")
	//nodes := make([]Node, 0, len(joinedNodes))
	//for _, n := range joinedNodes {
	//	if n.state != stateAlive {
	//		nodes = append(nodes, n.Node)
	//	}
	//}
	//
	//return nodes
	return c.findTypedMembers(stateAlive)
}

// Members returns a list of all known suspected nodes.
func (c *Cluster) SuspectedMembers() []Node {
	return c.findTypedMembers(stateSuspect)
}

// Members returns a list of all known dead nodes.
func (c *Cluster) DeadMembers() []Node {
	return c.findTypedMembers(stateDead)
}

//all members
func (c *Cluster) Members() []Node {
	return c.findTypedMembers(stateless)
}

func (c *Cluster) Shutdown() error {
	c.shutdownLock.Lock()
	defer c.shutdownLock.Unlock()

	if c.hasShutdown() {
		return nil
	}

	if err := c.transport.Shutdown(); err != nil {
		c.logger.Printf("[ERR] Failed to shutdown transport: %v", err)
	}

	atomic.StoreInt32(&c.shutdown, 1)
	close(c.shutdownCh)
	c.deschedule()
	return nil
}

func NewCluster(name string, addr string, port int) (*Cluster, error) {
	conf := DefaultConfig()
	conf.Name = name
	conf.BindAddr = addr
	conf.BindPort = port

	return Create(conf)
}