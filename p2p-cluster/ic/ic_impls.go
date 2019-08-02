package ic

import (
	"golang.org/x/net/context"
	"github.com/intervalue/intervalue-2.0.0-mainnet-rtm/p2p-cluster/cluster"
)

/**
 *
 * Copyright © INVE FOUNDATION. All rights reserved.
 *
 * @Description: cluster ic services which depending on cluster.Cluster
 * @author: Francis.Deng
 * @version: V1.0
 */

type clusterIcImpl struct{
	cluster *cluster.Cluster
}


func (icImpl *clusterIcImpl) UpdateMeta(ctx context.Context,requestUpdateMeta *RequestUpdateMeta) (*ResponseUpdateMeta, error) {
	var received bool = false

	for _,meta:=range requestUpdateMeta.Meta{
		icImpl.cluster.Set(meta.Key,meta.Value)
		received = true
	}

	if (received){
		icImpl.cluster.TransmitMeta()
	}


	return &ResponseUpdateMeta{}, nil
}

func (c *clusterIcImpl) FindAliveMembers(context.Context, *RequestFindMembers) (*ResponseFindMembers, error) {
	nodes := c.cluster.AliveMembers()
	copyNodeTo(nodes,&ResponseFindMembers{})

	return &ResponseFindMembers{}, nil
}

func (c *clusterIcImpl) FindSuspectedMembers(context.Context, *RequestFindMembers) (*ResponseFindMembers, error) {
	nodes := c.cluster.SuspectedMembers()
	copyNodeTo(nodes,&ResponseFindMembers{})

	return &ResponseFindMembers{}, nil
}


func copyNodeTo(nodes []cluster.Node,findMembers *ResponseFindMembers){
	for _,node := range nodes{
		findMember := new(ResponseFindMember)
		findMember.Name = node.Name
		findMember.Addr = node.Addr
		copyMetaTo(node.Meta,findMember.Meta)

		findMembers.FindMember = append(findMembers.FindMember,findMember)
	}
}

func copyMetaTo(nodeMeta map[string]string,protoMetaData []*MetaData){
	for k,v := range nodeMeta{
		protoMetaData = append(protoMetaData,&MetaData{
			Key:k,
			Value:v,
		})
	}
}





