package one.inve.localfullnode2.store;

import java.util.concurrent.BlockingQueue;

public interface EventStoreDependent {
	String getDbId();

	int getnValue();

	int getShardCount();

	int getCreatorId();

	BlockingQueue<EventBody> getEventSaveQueue();
}
