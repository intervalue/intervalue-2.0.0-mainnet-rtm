package one.inve.localfullnode2.postconsensus.readout;

import java.util.concurrent.LinkedBlockingQueue;

import one.inve.localfullnode2.hashnet.Event;
import one.inve.localfullnode2.store.EventBody;

public interface EventsReadoutDependent {
	int getShardCount();

	// the source
	Event[] getAllConsEvents(int shardId);

	// the destination to which impls push EventBodies
	LinkedBlockingQueue<EventBody> getShardSortQueue(int shardId);
}
