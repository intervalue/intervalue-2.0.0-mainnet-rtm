package one.inve.localfullnode2.hashnet;

import java.math.BigInteger;
import java.util.concurrent.LinkedBlockingQueue;

import one.inve.localfullnode2.store.EventBody;
import one.inve.localfullnode2.store.EventStore;

public interface HashneterDependent {
	int getShardCount();

	int getShardId();

	// the source from which it retrieved
	EventBody[] getAllQueuedEvents(int shardId);

	BigInteger getTotalEventCount();

	int getNValue();

	EventStore getEventStore();

	int getCreatorId();

	// the destination to which sorted EventBodies were sent
	LinkedBlockingQueue<EventBody> getShardSortQueue(int shardId);
}
