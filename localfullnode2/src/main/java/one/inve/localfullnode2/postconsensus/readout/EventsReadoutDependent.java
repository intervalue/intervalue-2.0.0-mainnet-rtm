package one.inve.localfullnode2.postconsensus.readout;

import java.util.concurrent.BlockingQueue;

import one.inve.localfullnode2.hashnet.Event;
import one.inve.core.EventBody;

public interface EventsReadoutDependent {
	int getShardCount();

	// the source
	Event[] getAllConsEvents(int shardId);

	// the destination to which impls push EventBodies
	BlockingQueue<EventBody> getShardSortQueue(int shardId);
}
