package one.inve.localfullnode2.store;

import java.util.concurrent.LinkedBlockingQueue;

public interface EventStoreDependent {
	String getDbId();

	int getnValue();

	int getShardCount();

	int getCreatorId();

	LinkedBlockingQueue<EventBody> getEventSaveQueue();
}
