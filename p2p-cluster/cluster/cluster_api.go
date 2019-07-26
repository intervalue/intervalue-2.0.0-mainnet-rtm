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

// Members returns a list of all known live nodes.
func (c *Cluster) Members() []*Node {

	joinedNodes := joinedNodes()
	nodes := make([]*Node, 0, len(joinedNodes))
	for _, n := range joinedNodes {
		if n.state != stateDead {
			nodes = append(nodes, &n.Node)
		}
	}

	return nodes
}
