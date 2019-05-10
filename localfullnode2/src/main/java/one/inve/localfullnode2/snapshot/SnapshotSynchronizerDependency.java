package one.inve.localfullnode2.snapshot;

import java.math.BigInteger;
import java.security.PublicKey;
import java.util.List;
import java.util.concurrent.BlockingQueue;

import com.alibaba.fastjson.JSONObject;
import com.zeroc.Ice.Communicator;

import one.inve.bean.message.SnapshotMessage;
import one.inve.bean.node.LocalFullNode;
import one.inve.localfullnode2.message.service.ITransactionDbService;
import one.inve.localfullnode2.message.service.TransactionDbService;
import one.inve.localfullnode2.store.SnapshotDbService;
import one.inve.localfullnode2.store.SnapshotDbServiceImpl2;

/**
 * 
 * 
 * Copyright © CHXX Co.,Ltd. All rights reserved.
 * 
 * @Description:cover all dependency coming to {@code SnapshotSynchronizer}
 *                    Note:pay more attention on TBD items.
 * @author: Francis.Deng
 * @date: May 6, 2018 11:49:14 PM
 * @version: V1.0
 */
public class SnapshotSynchronizerDependency implements SnapshotSynchronizerDependent {
	final private SnapshotSyncConsumable ssc;

	final private int shardId;
	final private int shardCount;
	final private int nValue;
	final private PublicKey publicKey;
	final private long creatorId;
	final private String dbId;
	final private BlockingQueue<JSONObject> consMessageVerifyQueue;

	public SnapshotSynchronizerDependency(Communicator communicator, int shardId, int shardCount, int nValue,
			PublicKey publicKey, long creatorId, String dbId, BlockingQueue<JSONObject> consMessageVerifyQueue) {
		this.ssc = new SnapshotSyncConsumer(communicator);
		this.shardId = shardId;
		this.shardCount = shardCount;
		this.nValue = nValue;
		this.publicKey = publicKey;
		this.creatorId = creatorId;
		this.dbId = dbId;
		this.consMessageVerifyQueue = consMessageVerifyQueue;
	}

	@Override
	public BigInteger getCurrSnapshotVersion() {
		// TBD

//	    public BigInteger getCurrSnapshotVersion() {
//	        return (null==snapshotMessage)
//	                ? BigInteger.ONE : BigInteger.ONE.add(snapshotMessage.getSnapVersion());
//	    }	

//	    public void setSnapshotMessage(SnapshotMessage snapshotMessage) {
//	        this.snapshotMessage = snapshotMessage;
//	    }		

		return null;
	}

	@Override
	public int getShardId() {
		return shardId;
	}

	@Override
	public int getShardCount() {
		return shardCount;
	}

	@Override
	public int getnValue() {
		return nValue;
	}

	@Override
	public BigInteger getConsMessageMaxId() {
		// TBD
//		node.getConsMessageMaxId();
		return null;
	}

	@Override
	public SnapshotSyncConsumable getSnapshotSync() {
		return ssc;
	}

	@Override
	public PublicKey getPublicKey() {
		return publicKey;
	}

	@Override
	public BlockingQueue<JSONObject> getConsMessageVerifyQueue() {
		return consMessageVerifyQueue;
	}

	@Override
	public void refresh(SnapshotMessage syncedSnapshotMessage) {
		// TBD
//		node.setSnapshotMessage(snapshotMessage);
//		node.getSnapshotPointMap().put(snapshotMessage.getSnapVersion(),
//				snapshotMessage.getSnapshotPoint());
//		node.getTreeRootMap().put(snapshotMessage.getSnapVersion(),
//				snapshotMessage.getSnapshotPoint().getMsgHashTreeRoot());		

	}

	@Override
	public long getCreatorId() {
		return creatorId;
	}

	@Override
	public String getDbId() {
		return dbId;
	}

	@Override
	public List<LocalFullNode> getLocalFullNodes() {
		// TBD
//		node.getLocalFullNodes();
		return null;
	}

	@Override
	public SnapshotDbService getSnapshotDBService() {
		return new SnapshotDbServiceImpl2();
	}

	@Override
	public ITransactionDbService getTransactionDbService() {
		return new TransactionDbService();
	}

}
