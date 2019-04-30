package one.inve.localfullnode2.gossip.communicator;

import java.util.concurrent.CompletableFuture;

import one.inve.cluster.Member;
import one.inve.localfullnode2.gossip.vo.AppointEvent;
import one.inve.localfullnode2.gossip.vo.GossipObj;

/**
 * 
 * 
 * Copyright © CHXX Co.,Ltd. All rights reserved.
 * 
 * @Description: consume gossipy communication results.
 * @author: Francis.Deng
 * @date: March 29, 2018 1:32:58 AM
 * @version: V1.0
 */
public interface GossipCommunicationConsumable {
	CompletableFuture<GossipObj> gossipMyMaxSeqList4ConsensusAsync(Member neighbor, String pubkey, String sig,
			String snapVersion, String snapHash, long[] seqs);

	CompletableFuture<GossipObj> gossipMyMaxSeqList4SyncAsync(Member neighbor, String pubkey, String sig,
			int otherShardId, String snapVersion, String snapHash, long[] seqs);

	AppointEvent gossip4AppointEvent(Member neighbor, String pubkey, String sig, int shardId, long creatorId,
			long creatorSeq);

	CompletableFuture<java.lang.Boolean> gossipReport4splitAsync(String pubkey, String sig, String data, int shardId,
			String event);
}
