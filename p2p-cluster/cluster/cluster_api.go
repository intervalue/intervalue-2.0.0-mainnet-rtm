package cluster

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

// Members returns a list of all known alive nodes.
func (c *Cluster) Members() []Node {
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
