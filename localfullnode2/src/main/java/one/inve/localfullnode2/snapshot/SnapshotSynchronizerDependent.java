package one.inve.localfullnode2.snapshot;

import java.math.BigInteger;
import java.security.PublicKey;
import java.util.HashMap;
import java.util.concurrent.BlockingQueue;
import one.inve.bean.message.SnapshotPoint;
import com.alibaba.fastjson.JSONObject;
import one.inve.bean.message.SnapshotMessage;

/**
 * 
 * Copyright © INVE FOUNDATION. All rights reserved.
 * 
 * @Description: snapshot sync dependent definition
 * @author: Francis.Deng
 * @date: May 6, 2019 1:45:36 AM
 * @version: V1.0
 */
public interface SnapshotSynchronizerDependent {

	BigInteger getCurrSnapshotVersion();

	int getShardCount();

	int getnValue();

	BigInteger getConsMessageMaxId();

	SnapshotSyncConsumable getSnapshotSync();

	PublicKey getPublicKey();

	BlockingQueue<JSONObject> getConsMessageVerifyQueue();

	HashMap<BigInteger, SnapshotPoint> getSnapshotPointMap();

	HashMap<BigInteger, String> getTreeRootMap();

	void setSnapshotMessage(SnapshotMessage snapshotMessage);

    void setMsgHashTreeRoot(String msgHashTreeRoot);

	String getMsgHashTreeRoot();

	void putTreeRootMap(BigInteger snapVersion, String msgHashTreeRoot);

	void putSnapshotPointMap(BigInteger snapVersion, SnapshotPoint snapshotPoint);

}
