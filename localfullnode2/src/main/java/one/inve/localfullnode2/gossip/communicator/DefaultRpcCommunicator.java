package one.inve.localfullnode2.gossip.communicator;

import java.util.concurrent.CompletableFuture;

import com.zeroc.Ice.Communicator;

import one.inve.cluster.Member;
import one.inve.localfullnode2.gossip.vo.AppointEvent;
import one.inve.localfullnode2.gossip.vo.GossipObj;
import one.inve.localfullnode2.rpc.Local2localPrx;
import one.inve.localfullnode2.rpc.RpcConnectionService;

/**
 * 
 * 
 * Copyright © CHXX Co.,Ltd. All rights reserved.
 * 
 * @Description: built-in zeroc rpc communicator
 * @author: Francis.Deng
 * @date: May 12, 2019 7:39:33 PM
 * @version: V1.0
 */
public class DefaultRpcCommunicator implements GossipCommunicationConsumable {

	@Override
	public CompletableFuture<GossipObj> gossipMyMaxSeqList4ConsensusAsync(Communicator communicator, Member neighbor,
			String pubkey, String sig, String snapVersion, String snapHash, long[] seqs) {
		Local2localPrx nprx = RpcConnectionService.buildConnection2localFullNode(communicator, neighbor);

		return nprx.gossipMyMaxSeqList4ConsensusAsync(pubkey, sig, snapVersion, snapHash, seqs);
	}

	@Override
	public CompletableFuture<GossipObj> gossipMyMaxSeqList4SyncAsync(Communicator communicator, Member neighbor,
			String pubkey, String sig, int otherShardId, String snapVersion, String snapHash, long[] seqs) {
		Local2localPrx nprx = RpcConnectionService.buildConnection2localFullNode(communicator, neighbor);

		return nprx.gossipMyMaxSeqList4SyncAsync(pubkey, sig, otherShardId, snapVersion, snapHash, seqs);
	}

	@Override
	public AppointEvent gossip4AppointEvent(Communicator communicator, Member neighbor, String pubkey, String sig,
			int shardId, long creatorId, long creatorSeq) {
		Local2localPrx nprx = RpcConnectionService.buildConnection2localFullNode(communicator, neighbor);

		return nprx.gossip4AppointEvent(pubkey, sig, shardId, creatorId, creatorSeq);
	}

	@Override
	public CompletableFuture<Boolean> gossipReport4splitAsync(Communicator communicator, Member neighbor, String pubkey,
			String sig, String data, int shardId, String event) {
		Local2localPrx nprx = RpcConnectionService.buildConnection2localFullNode(communicator, neighbor);

		return nprx.gossipReport4splitAsync(pubkey, sig, data, shardId, event);
	}

	@Override
	public long[] getHeight(Communicator communicator, Member neighbor) {
		Local2localPrx nprx = RpcConnectionService.buildConnection2localFullNode(communicator, neighbor);

		return nprx.getHeight();
	}

}
