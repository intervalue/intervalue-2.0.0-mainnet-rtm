package one.inve.localfullnode2.message;

import java.math.BigInteger;
import java.util.concurrent.BlockingQueue;

import com.alibaba.fastjson.JSONObject;

import one.inve.contract.MVM.WorldStateService;
import one.inve.localfullnode2.dep.DependentItem;
import one.inve.localfullnode2.dep.DependentItemConcerned;
import one.inve.localfullnode2.dep.items.AllQueues;
import one.inve.localfullnode2.dep.items.DBId;
import one.inve.localfullnode2.dep.items.ShardCount;
import one.inve.localfullnode2.dep.items.Stat;
import one.inve.localfullnode2.staging.StagingArea;
import one.inve.localfullnode2.store.rocks.INosql;
import one.inve.localfullnode2.store.rocks.RocksJavaUtil;

/**
 * 
 * 
 * Copyright © CHXX Co.,Ltd. All rights reserved.
 * 
 * @Description: {@setTotalFeeBetween2Snapshots},{@getTotalFeeBetween2Snapshots} is
 *               unknown
 * @author: Francis.Deng
 * @date: May 21, 2019 1:51:10 AM
 * @version: V1.0
 */
public class MessagesExeDependency implements MessagesExeDependent, DependentItemConcerned {

	private AllQueues allQueues;
	private ShardCount shardCount;
	private DBId dbId;
	private Stat stat;

	@Override
	public void update(DependentItem item) {
		set(this, item);
	}

	@Override
	public BlockingQueue<JSONObject> getConsMessageHandleQueue() {
		return allQueues.get().getQueue(JSONObject.class, StagingArea.ConsMessageHandleQueueName);
	}

	@Override
	public BlockingQueue<JSONObject> getConsMessageSaveQueue() {
		return allQueues.get().getQueue(JSONObject.class, StagingArea.ConsMessageSaveQueueName);
	}

	@Override
	public BlockingQueue<JSONObject> getSystemAutoTxSaveQueue() {
		return allQueues.get().getQueue(JSONObject.class, StagingArea.SystemAutoTxSaveQueueName);
	}

	@Override
	public int getMultiple() {
		return 1;
	}

	@Override
	public int getShardCount() {
		return shardCount.get();
	}

	@Override
	public INosql getNosql() {
		return new RocksJavaUtil(dbId.get());
	}

	@Override
	public String getDbId() {
		return dbId.get();
	}

	@Override
	public void addSystemAutoTxMaxId(long delta) {
		stat.addSystemAutoTxMaxId(delta);

	}

	@Override
	public BigInteger getSystemAutoTxMaxId() {
		return stat.getSystemAutoTxMaxId();
	}

	@Override
	public void setTotalFeeBetween2Snapshots(BigInteger totalFeeBetween2Snapshots) {
		// TODO Auto-generated method stub

	}

	@Override
	public BigInteger getTotalFeeBetween2Snapshots() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public IWorldStateService getWorldStateService() {
		return new IWorldStateService() {

			@Override
			public boolean transfer(String dbId, String fromAddr, String toAddr, BigInteger value) {
				return WorldStateService.transfer(dbId, fromAddr, toAddr, value);
			}

			@Override
			public BigInteger getBalanceByAddr(String dbId, String address) throws NullPointerException {
				return WorldStateService.getBalanceByAddr(dbId, address);
			}

			@Override
			public byte[] getRoothash(String dbId) {
				return WorldStateService.getRoothash(dbId);
			}

			@Override
			public void setBalance(String dbId, String address, BigInteger value) {
				WorldStateService.setBalance(dbId, address, value);

			}

			@Override
			public byte[] executeViewTransaction(String dbId, String address, String callData) {
				return WorldStateService.executeViewTransaction(dbId, address, callData);
			}
		};
	}

}
