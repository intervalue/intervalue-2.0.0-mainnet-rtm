package one.inve.localfullnode2.snapshot;

import java.util.concurrent.CompletableFuture;

import one.inve.cluster.Member;
import one.inve.localfullnode2.gossip.vo.GossipObj;

/**
 * 
 * Copyright © CHXX Co.,Ltd. All rights reserved.
 * 
 * @Description: provide snapshot sync communication utility
 * @author: Francis.Deng
 * @date: Dec 6, 2018 1:31:23 AM
 * @version: V1.0
 */
public interface SnapshotSyncConsumable {
	// snapshot synchronizing
	CompletableFuture<GossipObj> gossipMySnapVersion4SnapAsync(Member neighbor, String pubkey, String sig, String hash,
			String transCount);
}
