package one.inve.localfullnode2.postconsensus.sorting;

import java.time.Instant;
import java.util.Collections;
import java.util.Comparator;
import java.util.Vector;
import java.util.concurrent.BlockingQueue;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import one.inve.core.EventBody;

/**
 * 
 * Copyright © INVE FOUNDATION. All rights reserved.
 * 
 * @Description: The sorting alg is concise which is fully using
 *               {@code SortingEventBodyComparator}
 * @author: Francis.Deng
 * @see ConsensusEventAllSortThread
 * @date: May 3, 2019 8:00:21 PM
 * @version: V1.0
 */
public class EventsSorting {
	private static final Logger logger = LoggerFactory.getLogger("eventssorting");

	static class SortingEventBody {
		private Instant consIns;
		private int shardId;
		private EventBody eventBody;

		public SortingEventBody(Instant consIns, int shardId, EventBody eventBody) {
			this.consIns = consIns;
			this.shardId = shardId;
			this.eventBody = eventBody;
		}

		public Instant getConsIns() {
			return consIns;
		}

		public void setConsIns(Instant consIns) {
			this.consIns = consIns;
		}

		public int getShardId() {
			return shardId;
		}

		public void setShardId(int shardId) {
			this.shardId = shardId;
		}

		public EventBody getEventBody() {
			return eventBody;
		}

		public void setEventBody(EventBody eventBody) {
			this.eventBody = eventBody;
		}

	}

	/**
	 * sorting priority: consTimestamp - shardId
	 */
	@SuppressWarnings("hiding")
	static class SortingEventBodyComparator implements Comparator<SortingEventBody> {

		@Override
		public int compare(SortingEventBody s1, SortingEventBody s2) {
			if (s1.getConsIns().isBefore(s2.getConsIns())) {
				return -1;
			} else if (s1.getConsIns().equals(s2.getConsIns())) {
				return s1.getShardId() - s2.getShardId();
			}

			return 1;

		}

	}

	public void work(EventsSortingDependent dep) {
		logger.info(">>> start up multiple sharding's sorting...");

		Vector<SortingEventBody> allEventBodiesThisRound = new Vector<>();
		BlockingQueue<EventBody> eventBodies = null;
		EventBody eventBody = null;

		for (int i = 0; i < dep.getShardCount(); i++) {
			if ((eventBodies = dep.getShardSortQueue(i)) != null && eventBodies.size() > 0) {
				while ((eventBody = eventBodies.poll()) != null) {
					allEventBodiesThisRound.add(new SortingEventBody(eventBody.getConsTimestamp(), i, eventBody));
				}

			}

		}

		if (!allEventBodiesThisRound.isEmpty()) {
			Collections.sort(allEventBodiesThisRound, new SortingEventBodyComparator());

			allEventBodiesThisRound.forEach((e) -> {
				try {
//					if (e.getEventBody().getTrans() != null) {
//						for (byte[] msg : e.getEventBody().getTrans()) {
//							System.out.println("for acceptance test");
//						}
//					}

					dep.getConsEventHandleQueue().put(e.getEventBody());
				} catch (InterruptedException e1) {
					e1.printStackTrace();
				}
			});

			allEventBodiesThisRound.clear();
		}

	}
}
