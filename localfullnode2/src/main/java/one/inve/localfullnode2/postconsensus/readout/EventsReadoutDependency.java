package one.inve.localfullnode2.postconsensus.readout;

import java.util.concurrent.BlockingQueue;

import one.inve.localfullnode2.dep.DependentItem;
import one.inve.localfullnode2.dep.DependentItemConcerned;
import one.inve.localfullnode2.dep.items.AllQueues;
import one.inve.localfullnode2.dep.items.ShardCount;
import one.inve.localfullnode2.hashnet.Event;
import one.inve.localfullnode2.hashnet.IHashneter;
import one.inve.localfullnode2.staging.StagingArea;
import one.inve.localfullnode2.store.EventBody;

public class EventsReadoutDependency implements EventsReadoutDependent, DependentItemConcerned {

	private ShardCount shardCount;
	private AllQueues allQueues;

	private IHashneter hashneter;

	@Override
	public void update(DependentItem item) {
		this.set(this, item);

	}

	@Override
	public int getShardCount() {
		return shardCount.get();
	}

	@Override
	public Event[] getAllConsEvents(int shardId) {
		return hashneter.getAllConsEvents(shardId);
	}

	@Override
	public BlockingQueue<EventBody> getShardSortQueue(int shardId) {
		return allQueues.get().getQueue(EventBody.class, StagingArea.ShardSortQueueName, shardId);
	}

	public void setHashneter(IHashneter hashneter) {
		this.hashneter = hashneter;
	}

}
