package one.inve.localfullnode2.gossip;

import java.math.BigInteger;
import java.security.PrivateKey;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;

import one.inve.cluster.Member;
import one.inve.localfullnode2.gossip.communicator.GossipCommunicationConsumable;
import one.inve.localfullnode2.store.EventFlow;

/**
 * 
 * 
 * Copyright © CHXX Co.,Ltd. All rights reserved.
 * 
 * @Description: a description of gossip module
 * @author: Francis.Deng
 * @date: March 29, 2018 8:58:13 PM
 * @version: V1.0
 */
public interface GossipDependent {
	/**
	 * get participated members by in one sharding or across global sharding
	 * 
	 * @return
	 */
	// List<Member> getMembers();

	int getRandomOtherShardId();

	// getGossipType()
	List<Member> getMembers(int gossipType);

	Member getMember(int gossipType, int index);

	int getShardCount();

	int getShardId();

	int getCreatorId();

	// last seqs is equal to height
	long[] getLastSeqsByShardId(int shardId);

	long[][] getLastSeqs();

	String getPublicKey();

	BigInteger getCurrSnapshotVersion();

	// call a remote peer communicator
	GossipCommunicationConsumable getGossipCommunication();

	// Config.GOSSIP_IN_SHARD or Config.GOSSIP_GLOBAL_SHARD
	int getGossipType();

	// the destination to which the result is sent
	ConcurrentLinkedQueue<byte[]> getMessageQueue();

	EventFlow getEventFlow();

	List<String> getBlackList4PubKey();

	PrivateKey getPrivateKey();
}
