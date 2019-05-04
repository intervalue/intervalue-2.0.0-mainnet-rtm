package one.inve.localfullnode2.postconsensus.sorting;

import java.util.concurrent.LinkedBlockingQueue;

import one.inve.localfullnode2.store.EventBody;

public interface EventsSortingDependent {
	int getShardCount();

	// the source
	LinkedBlockingQueue<EventBody> getShardSortQueue(int shardId);

	// the destination
	LinkedBlockingQueue<EventBody> getConsEventHandleQueue();
}
