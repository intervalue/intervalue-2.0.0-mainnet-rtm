package one.inve.localfullnode2.snapshot;

import java.math.BigInteger;
import java.security.PublicKey;
import java.util.concurrent.BlockingQueue;

import com.alibaba.fastjson.JSONObject;

import one.inve.bean.message.SnapshotMessage;

/**
 * 
 * Copyright © CHXX Co.,Ltd. All rights reserved.
 * 
 * @Description: snapshot sync dependent definition
 * @author: Francis.Deng
 * @date: May 6, 2019 1:45:36 AM
 * @version: V1.0
 */
public interface SnapshotSynchronizerDependent {

	BigInteger getCurrSnapshotVersion();

	int getShardId();

	int getShardCount();

	int getnValue();

	BigInteger getConsMessageMaxId();

	SnapshotSyncConsumable getSnapshotSync();

	PublicKey getPublicKey();

	BlockingQueue<JSONObject> getConsMessageVerifyQueue();

	// refresh the node info
	void refresh(SnapshotMessage syncedSnapshotMessage);

//	GossipObj getGossipObj();	

//	HashMap<BigInteger, SnapshotPoint> getSnapshotPointMap();
//
//	void setSnapshotMessage(SnapshotMessage snapshotMessage);
//
//	HashMap<BigInteger, String> getTreeRootMap();
//
//	Map<String, HashSet<String>> getSnapVersionMap();

//	Member getNeighbor();

}
