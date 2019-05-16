package one.inve.localfullnode2.store;

import java.util.concurrent.BlockingQueue;

public interface EventStoreDependent {
	String getDbId();

	int getnValue();

	int getShardCount();

	long getCreatorId();

	BlockingQueue<EventBody> getEventSaveQueue();
}
