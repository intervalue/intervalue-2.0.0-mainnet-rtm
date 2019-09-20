package one.inve.localfullnode2.cons;

import java.time.Instant;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class Flag {
	private Lock lock = new ReentrantLock();

	private Event[] lastBearer = null;
	// private List<Event> list = Collections.synchronizedList(new
	// ArrayList<Event>());
	private Hashnet[] hashnets = null;

	public Flag(Hashnet[] hashnets) {
		super();
		this.hashnets = hashnets;
		lastBearer = new Event[hashnets.length];
	}

	public Event[] grab(int shardId, long creatorId, long creatorSeq, Event[] selfParents) {
		lock.lock();
		Event[] newEvents = new Event[hashnets.length];

		try {
			if (lastBearer != null && lastBearer[0] != null && lastBearer[0].getCreatorId() == creatorId) {
				return null;
			}

//			Event event = new Event(shardId, creatorId, creatorSeq, -1, -1, selfParent, lastBearer, Instant.now(),
//					new byte[0], -1, new byte[0], new byte[0][0]);
			// hashnet.consRecordEvent(event);
			for (int index = 0; index < hashnets.length; index++) {
				newEvents[index] = new Event(shardId, creatorId, creatorSeq, -1, -1, selfParents[index],
						lastBearer[index], Instant.now(), new byte[0], -1, new byte[0], new byte[0][0]);
				hashnets[index].consRecordEvent(newEvents[index]);

				lastBearer[index] = newEvents[index];
			}
//
//			sizeofEvents++;

			return newEvents;
		} finally {
			lock.unlock();
		}
	}

	public Hashnet[] getHashnet() {
		return hashnets;
	}

//	public Event[] getOrderedEvent(int shardId) {
//		Event[] events = hashnet.getAllConsEvents(shardId);
//
//		return events;
//	}
}
