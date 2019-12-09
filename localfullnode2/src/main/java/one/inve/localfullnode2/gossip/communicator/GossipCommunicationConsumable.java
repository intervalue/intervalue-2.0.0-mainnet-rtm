package one.inve.localfullnode2.gossip.communicator;

import java.util.concurrent.CompletableFuture;

import com.zeroc.Ice.Communicator;

import one.inve.cluster.Member;
import one.inve.localfullnode2.rpc.AppointEvent;
import one.inve.localfullnode2.rpc.GossipObj;

/**
 * 
 * 
 * Copyright © INVE FOUNDATION. All rights reserved.
 * 
 * @Description: consume gossipy communication results.
 * @author: Francis.Deng
 * @date: March 29, 2018 1:32:58 AM
 * @version: V1.0
 */
public interface GossipCommunicationConsumable {
	// gossip communication among nodes
	CompletableFuture<GossipObj> gossipMyMaxSeqList4ConsensusAsync(Communicator communicator, Member neighbor,
			String pubkey, String sig, String snapVersion, String snapHash, long[] seqs);

	CompletableFuture<GossipObj> gossipMyMaxSeqList4SyncAsync(Communicator communicator, Member neighbor, String pubkey,
			String sig, int otherShardId, String snapVersion, String snapHash, long[] seqs);

	AppointEvent gossip4AppointEvent(Communicator communicator, Member neighbor, String pubkey, String sig, int shardId,
			long creatorId, long creatorSeq);

	CompletableFuture<java.lang.Boolean> gossipReport4splitAsync(Communicator communicator, Member neighbor,
			String pubkey, String sig, String data, int shardId, String event);

	long[] getHeight(Communicator communicator, Member neighbor);
}
