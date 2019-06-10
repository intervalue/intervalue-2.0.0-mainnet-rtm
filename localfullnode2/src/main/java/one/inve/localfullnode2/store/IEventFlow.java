package one.inve.localfullnode2.store;

import one.inve.core.EventBody;

import java.security.PublicKey;
import java.util.List;
import java.util.Map;

public interface IEventFlow {
	PublicKey[][] getPubKeys();

	EventBody[] getAllQueuedEvents(int shardId);

	EventBody newEvent(int shardId, long creatorId, long otherId, byte[][] trans);// Gossiper

	void addEvent2Store(EventBody eb);// Gossiper

	Map<String, String> addEvent(EventBody eb);// Gossiper

	boolean checkSplitEvent(EventBody eb1, byte[] otherHash1, EventBody eb2, byte[] otherHash2, byte[] parentHash);

	void initSnapEvent(List<EventBody> snapEvents, List<EventBody> cacheEvents);
}
