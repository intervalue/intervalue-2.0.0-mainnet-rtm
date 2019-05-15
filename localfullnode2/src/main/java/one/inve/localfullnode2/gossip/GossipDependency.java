package one.inve.localfullnode2.gossip;

import java.math.BigInteger;
import java.util.List;
import java.util.Queue;

import com.zeroc.Ice.Communicator;

import one.inve.cluster.Member;
import one.inve.localfullnode2.conf.Config;
import one.inve.localfullnode2.dep.DependentItem;
import one.inve.localfullnode2.dep.DependentItemConcerned;
import one.inve.localfullnode2.dep.items.AllQueues;
import one.inve.localfullnode2.dep.items.BlackList4PubKey;
import one.inve.localfullnode2.dep.items.CreatorId;
import one.inve.localfullnode2.dep.items.CurrSnapshotVersion;
import one.inve.localfullnode2.dep.items.DirectCommunicator;
import one.inve.localfullnode2.dep.items.EventFlow;
import one.inve.localfullnode2.dep.items.LastSeqs;
import one.inve.localfullnode2.dep.items.Members;
import one.inve.localfullnode2.dep.items.PrivateKey;
import one.inve.localfullnode2.dep.items.PublicKey;
import one.inve.localfullnode2.dep.items.ShardCount;
import one.inve.localfullnode2.dep.items.ShardId;
import one.inve.localfullnode2.gossip.communicator.DefaultRpcCommunicator;
import one.inve.localfullnode2.gossip.communicator.GossipCommunicationConsumable;
import one.inve.localfullnode2.staging.StagingArea;
import one.inve.localfullnode2.store.IEventFlow;

/**
 * 
 * Copyright © CHXX Co.,Ltd. All rights reserved.
 * 
 * @Description: delve into the source of {@code lastSeqs} and
 *               {@code currSnapshotVersion}
 * @author: Francis.Deng
 * @date: Oct 12, 2018 8:05:18 PM
 * @version: V1.0
 */
public class GossipDependency implements GossipDependent, DependentItemConcerned {

	private Members members;
	private ShardCount shardCount;
	private ShardId shardId;
	private CreatorId creatorId;
	private LastSeqs lastSeqs;
	private PublicKey publicKey;
	private CurrSnapshotVersion currSnapshotVersion;
	private EventFlow eventFlow;
	private BlackList4PubKey blackList4PubKey;
	private PrivateKey privateKey;
	private GossipCommunicationConsumable rpcCommunicator = new DefaultRpcCommunicator();
	private DirectCommunicator directCommunicator;
	private AllQueues allQueues;

	@Override
	public List<Member> getMembers(int gossipType) {
		return members.get();
	}

	@Override
	public Member getMember(int gossipType, int index) {
		return members.get().get(index);
	}

	@Override
	public int getShardCount() {
		return shardCount.get();
	}

	@Override
	public int getShardId() {
		return shardId.get();
	}

	@Override
	public int getCreatorId() {
		return creatorId.get();
	}

	@Override
	public long[] getLastSeqsByShardId(int shardId) {
		return lastSeqs.get()[shardId];
	}

	@Override
	public long[][] getLastSeqs() {
		return lastSeqs.get();
	}

	@Override
	public String getPublicKey() {
		return publicKey.get().toString();
	}

	@Override
	public BigInteger getCurrSnapshotVersion() {
		return currSnapshotVersion.get();
	}

	@Override
	public GossipCommunicationConsumable getGossipCommunication() {
		return rpcCommunicator;
	}

	// Config.GOSSIP_GLOBAL_SHARD nonsupport,hardcode the type.
	@Override
	public int getGossipType() {
		return Config.GOSSIP_IN_SHARD;
	}

	// ensuing that {@link StagingArea} created queue already.
	@Override
	public Queue<byte[]> getMessageQueue() {
		return allQueues.get().getQueue(byte[].class, StagingArea.MessageQueueName);
	}

	@Override
	public IEventFlow getEventFlow() {
		return eventFlow.get();
	}

	@Override
	public List<String> getBlackList4PubKey() {
		return blackList4PubKey.get();
	}

	@Override
	public java.security.PrivateKey getPrivateKey() {
		return privateKey.get();
	}

	@Override
	public void update(DependentItem item) {
		set(this, item);
	}

	@Override
	public Communicator getCommunicator() {
		return directCommunicator.get();
	}

}
