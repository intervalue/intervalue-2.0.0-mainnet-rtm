package one.inve.localfullnode2.store;

import java.util.concurrent.BlockingQueue;

import one.inve.localfullnode2.dep.DependentItem;
import one.inve.localfullnode2.dep.DependentItemConcerned;
import one.inve.localfullnode2.dep.items.AllQueues;
import one.inve.localfullnode2.dep.items.CreatorId;
import one.inve.localfullnode2.dep.items.DBId;
import one.inve.localfullnode2.dep.items.NValue;
import one.inve.localfullnode2.dep.items.ShardCount;
import one.inve.localfullnode2.staging.StagingArea;

public class EventStoreDependency implements EventStoreDependent, DependentItemConcerned {
	private DBId dbId;
	private NValue nValue;
	private ShardCount shardCount;
	private CreatorId creatorId;
	private AllQueues allQueues;

	@Override
	public String getDbId() {
		return dbId.get();
	}

	@Override
	public int getnValue() {
		return nValue.get();
	}

	@Override
	public int getShardCount() {
		return shardCount.get();
	}

	@Override
	public long getCreatorId() {
		return creatorId.get();
	}

	@Override
	public BlockingQueue<EventBody> getEventSaveQueue() {
		return allQueues.get().getQueue(EventBody.class, StagingArea.EventSaveQueueName);
	}

	@Override
	public void update(DependentItem item) {
		set(this, item);

	}

}
