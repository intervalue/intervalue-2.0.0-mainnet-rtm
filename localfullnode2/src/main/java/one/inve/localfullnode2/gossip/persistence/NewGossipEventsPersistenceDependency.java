package one.inve.localfullnode2.gossip.persistence;

import java.math.BigInteger;
import java.util.concurrent.BlockingQueue;

import one.inve.localfullnode2.dep.DependentItem;
import one.inve.localfullnode2.dep.DependentItemConcerned;
import one.inve.localfullnode2.dep.items.AllQueues;
import one.inve.localfullnode2.dep.items.DBId;
import one.inve.localfullnode2.dep.items.Stat;
import one.inve.localfullnode2.staging.StagingArea;
import one.inve.localfullnode2.store.EventBody;

public class NewGossipEventsPersistenceDependency
		implements NewGossipEventsPersistenceDependent, DependentItemConcerned {

	private DBId dbId;
	private AllQueues allQueues;
	private Stat stat;

	@Override
	public BlockingQueue<EventBody> getEventSaveQueue() {
		return allQueues.get().getQueue(EventBody.class, StagingArea.EventSaveQueueName);
	}

	@Override
	public String getDbId() {
		return dbId.get();
	}

	@Override
	public void addTotalEventCount(long delta) {
		stat.addTotalEventCount(delta);

	}

	@Override
	public BigInteger getTotalEventCount() {
		return stat.getTotalEventCount();
	}

	@Override
	public void update(DependentItem item) {
		set(this, item);

	}

}
