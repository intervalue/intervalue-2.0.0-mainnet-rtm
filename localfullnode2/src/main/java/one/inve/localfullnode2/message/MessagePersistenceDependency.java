package one.inve.localfullnode2.message;

import java.math.BigInteger;
import java.util.concurrent.BlockingQueue;

import com.alibaba.fastjson.JSONObject;

import one.inve.localfullnode2.dep.DependentItem;
import one.inve.localfullnode2.dep.DependentItemConcerned;
import one.inve.localfullnode2.dep.items.AllQueues;
import one.inve.localfullnode2.dep.items.DBId;
import one.inve.localfullnode2.dep.items.Stat;
import one.inve.localfullnode2.staging.StagingArea;
import one.inve.localfullnode2.store.rocks.INosql;
import one.inve.localfullnode2.store.rocks.RocksJavaUtil;

public class MessagePersistenceDependency implements MessagePersistenceDependent, DependentItemConcerned {

	private AllQueues allQueues;
	private Stat stat;
	private DBId dbId;

	@Override
	public void update(DependentItem item) {
		set(this, item);

	}

	@Override
	public BlockingQueue<JSONObject> getConsMessageSaveQueue() {
		return allQueues.get().getQueue(JSONObject.class, StagingArea.ConsMessageSaveQueueName);
	}

	@Override
	public void setConsMessageCount(BigInteger consMessageCount) {
		stat.setConsMessageCount(consMessageCount);

	}

	@Override
	public BigInteger getConsMessageCount() {
		return stat.getConsMessageCount();
	}

	@Override
	public String getDbId() {
		return dbId.get();
	}

	@Override
	public INosql getNosql() {
		return new RocksJavaUtil(dbId.get());
	}

	@Override
	public BlockingQueue<JSONObject> getSystemAutoTxSaveQueue() {
		return allQueues.get().getQueue(JSONObject.class, StagingArea.SystemAutoTxSaveQueueName);
	}

}
