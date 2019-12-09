package one.inve.localfullnode2.rpc.impl;

import com.zeroc.Ice.Current;

import one.inve.localfullnode2.rpc.AppointEvent;
import one.inve.localfullnode2.rpc.GossipObj;
import one.inve.localfullnode2.rpc.Local2local;
import one.inve.localfullnode2.rpc.SnapObj;

/**
 * 
 * Copyright © INVE FOUNDATION. All rights reserved.
 * 
 * @ClassName: DefLocal2localImpl
 * @Description: define a impl of Local2local that do nothing
 * @author Francis.Deng [francis_xiiiv@163.com]
 * @date Dec 4, 2019
 *
 */
public class DefLocal2localImpl implements Local2local {

	@Override
	public GossipObj gossipMyMaxSeqList4Consensus(String pubkey, String sig, String snapVersion, String snapHash,
			long[] seqs, Current current) {
		return null;
	}

	@Override
	public GossipObj gossipMyMaxSeqList4Sync(String pubkey, String sig, int otherShardId, String snapVersion,
			String snapHash, long[] seqs, Current current) {
		return null;
	}

	@Override
	public SnapObj gossipMySnapVersion4Snap(String pubkey, String sig, String hash, String transCount,
			Current current) {
		return null;
	}

	@Override
	public AppointEvent gossip4AppointEvent(String pubkey, String sig, int shardId, long creatorId, long creatorSeq,
			Current current) {
		return null;
	}

	@Override
	public boolean gossipReport4split(String pubkey, String sig, String data, int shardId, String event,
			Current current) {
		return false;
	}

	@Override
	public boolean gossip4SplitDel(String pubkey, String sig, String data, int shardId, long creatorId, long creatorSeq,
			String eventHash, boolean isNeedGossip2Center, Current current) {
		return false;
	}

	@Override
	public long[] getHeight(Current current) {
		return null;
	}

}
