package one.inve.localfullnode2.cons;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 
 * 
 * Copyright © INVE FOUNDATION. All rights reserved.
 * 
 * @Description: Test case list:
 * 
 *               a.4 sticker only
 *               <p>
 *               b.4 sticker expands to 5 stickers
 *               <p>
 *               c.4 sticker shrinks into 3 stickers
 *               <p>
 *               d.10 sticker expands to 12 stickers
 * 
 *               We proved that hashnet model provided spaces for the expansion
 *               of nodes.
 * 
 * @author: Francis.Deng [francis_xiiiv@163.com]
 * @date: Sep 20, 2019 2:23:14 AM
 * @version: V1.0
 */
public class HashnetStickerExpansionTest {
	private static final Logger logger = LoggerFactory.getLogger("HashnetTest");

	private void buildGrabFlagThread(int shardId, long creatorId, Stop stopIt, Flag flag, int size) {
		Thread t = new Thread(new Runnable() {

			private long currentSeq = 0;
			private Event[] selfParents = new Event[size];

			@Override
			public void run() {
				while (!stopIt.get()) {
					Event[] sps = flag.grab(shardId, creatorId, currentSeq++, selfParents);
					if (sps != null) {
						selfParents = sps;
					} else {
						currentSeq--;
					}
				}

			}

		});
		t.start();
	}

	// test case:4 sticker only
	// @Test
	public void testCoreFunction() {
		EventsBuilder eb = new EventsBuilder();
		Hashnet[] hs = { new Hashnet(1, 4) };
		Hashnet[] hashnets = eb.build(hs, 1000, () -> {
			Stop stopIt = new Stop();
			Flag flag = new Flag(hs);

			buildGrabFlagThread(0, 0, stopIt, flag, hs.length);
			buildGrabFlagThread(0, 1, stopIt, flag, hs.length);
			buildGrabFlagThread(0, 2, stopIt, flag, hs.length);
			buildGrabFlagThread(0, 3, stopIt, flag, hs.length);
		});
		Event[] consEvents = hashnets[0].getAllConsEvents(0);
		logger.info("the size of consensus events : {}", consEvents.length);
		for (Event event : consEvents) {
			hashnets[0].consRecordEvent(event);
			logger.info("self[{},{}],selfParent[{},{}],selfOtherParent[{},{}]", event.getCreatorId(),
					event.getCreatorSeq(), event.getSelfParent() != null ? event.getSelfParent().getCreatorId() : -1,
					event.getSelfParent() != null ? event.getSelfParent().getSeq() : -1,
					event.getOtherParent() != null ? event.getOtherParent().getCreatorId() : -1,
					event.getOtherParent() != null ? event.getOtherParent().getSeq() : -1);
		}
	}

	// test case:4 sticker expands to 5 stickers
	// @Test
	public void testCompare4stickerWith5stickers() {
		EventsBuilder eb = new EventsBuilder();
		Hashnet[] hs = { new Hashnet(1, 4), new Hashnet(1, 5) };
		Hashnet[] hashnets = eb.build(hs, 1000, () -> {
			Stop stopIt = new Stop();
			Flag flag = new Flag(hs);

			buildGrabFlagThread(0, 0, stopIt, flag, hs.length);
			buildGrabFlagThread(0, 1, stopIt, flag, hs.length);
			buildGrabFlagThread(0, 2, stopIt, flag, hs.length);
			buildGrabFlagThread(0, 3, stopIt, flag, hs.length);
		});
		Event[] consEvents0 = hashnets[0].getAllConsEvents(0);
		Event[] consEvents1 = hashnets[1].getAllConsEvents(0);
		int distinctiveSize = 0;

		logger.info("the size of consensus events0 : {}", consEvents0.length);
		logger.info("the size of consensus events1 : {}", consEvents1.length);

		for (int index = 0; index <= Integer.min(consEvents0.length, consEvents1.length) - 1; index++) {
			// logger.info("{} : {} : {}", index, consEvents0[index], consEvents1[index]);
			if (consEvents0[index].getCreatorSeq() != consEvents1[index].getCreatorSeq()
					|| consEvents0[index].getCreatorId() != consEvents1[index].getCreatorId()) {
				distinctiveSize++;
			}
		}

		logger.info("distinctive size : {}", distinctiveSize);

	}

	// test case:4 sticker shrinks into 3 stickers
	// @Test
	public void testCompare4stickerWith3stickers() {
		EventsBuilder eb = new EventsBuilder();
		Hashnet[] hs = { new Hashnet(1, 4), new Hashnet(1, 3) };
		Hashnet[] hashnets = eb.build(hs, 1000, () -> {
			Stop stopIt = new Stop();
			Flag flag = new Flag(hs);

			buildGrabFlagThread(0, 0, stopIt, flag, hs.length);
			buildGrabFlagThread(0, 1, stopIt, flag, hs.length);
			buildGrabFlagThread(0, 2, stopIt, flag, hs.length);
			buildGrabFlagThread(0, 3, stopIt, flag, hs.length);
		});
		Event[] consEvents0 = hashnets[0].getAllConsEvents(0);
		Event[] consEvents1 = hashnets[1].getAllConsEvents(0);
		int distinctiveSize = 0;

		logger.info("the size of consensus events0 : {}", consEvents0.length);
		logger.info("the size of consensus events1 : {}", consEvents1.length);

		for (int index = 0; index <= Integer.min(consEvents0.length, consEvents1.length) - 1; index++) {
			// logger.info("{} : {} : {}", index, consEvents0[index], consEvents1[index]);
			if (consEvents0[index].getCreatorSeq() != consEvents1[index].getCreatorSeq()
					|| consEvents0[index].getCreatorId() != consEvents1[index].getCreatorId()) {
				distinctiveSize++;
			}
		}

		logger.info("distinctive size : {}", distinctiveSize);

	}

	// test case:10 sticker expands to 12 stickers
	@Test
	public void testCompare10stickerWith12stickers() {
		EventsBuilder eb = new EventsBuilder();
		Hashnet[] hs = { new Hashnet(1, 10), new Hashnet(1, 12) };
		Hashnet[] hashnets = eb.build(hs, 800, () -> {
			Stop stopIt = new Stop();
			Flag flag = new Flag(hs);

			buildGrabFlagThread(0, 0, stopIt, flag, hs.length);
			buildGrabFlagThread(0, 1, stopIt, flag, hs.length);
			buildGrabFlagThread(0, 2, stopIt, flag, hs.length);
			buildGrabFlagThread(0, 3, stopIt, flag, hs.length);
			buildGrabFlagThread(0, 4, stopIt, flag, hs.length);
			buildGrabFlagThread(0, 5, stopIt, flag, hs.length);
			buildGrabFlagThread(0, 6, stopIt, flag, hs.length);
			buildGrabFlagThread(0, 7, stopIt, flag, hs.length);
			buildGrabFlagThread(0, 8, stopIt, flag, hs.length);
			buildGrabFlagThread(0, 9, stopIt, flag, hs.length);
		});
		Event[] consEvents0 = hashnets[0].getAllConsEvents(0);
		Event[] consEvents1 = hashnets[1].getAllConsEvents(0);
		int distinctiveSize = 0;

		logger.info("the size of consensus events0 : {}", consEvents0.length);
		logger.info("the size of consensus events1 : {}", consEvents1.length);

		for (int index = 0; index <= Integer.min(consEvents0.length, consEvents1.length) - 1; index++) {
			// logger.info("{} : {} : {}", index, consEvents0[index], consEvents1[index]);
			if (consEvents0[index].getCreatorSeq() != consEvents1[index].getCreatorSeq()
					|| consEvents0[index].getCreatorId() != consEvents1[index].getCreatorId()) {
				distinctiveSize++;
			}
		}

		logger.info("distinctive size : {}", distinctiveSize);

	}

	// @formatter:off
	//@formatter:on
//	private Hashnet[] popEventsIntoHashnet(Hashnet[] hashnets, int sleepingInMilliseconds) {
//		Stop stopIt = new Stop();
//		Flag flag = new Flag(hashnets);
//
//		buildGrabFlagThread(0, 0, stopIt, flag, hashnets.length);
//		buildGrabFlagThread(0, 1, stopIt, flag, hashnets.length);
//		buildGrabFlagThread(0, 2, stopIt, flag, hashnets.length);
//		buildGrabFlagThread(0, 3, stopIt, flag, hashnets.length);
//
//		try {
//			Thread.currentThread().sleep(sleepingInMilliseconds);
//		} catch (InterruptedException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//		stopIt.flip();
//
//		try {
//			Thread.currentThread().sleep(1 * 1000);
//		} catch (InterruptedException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//
//		return flag.getHashnet();
//	}

//	private void buildGrabFlagThread(int shardId, long creatorId, Stop stopIt, Flag flag, int size) {
//		Thread t = new Thread(new Runnable() {
//
//			private long currentSeq = 0;
//			private Event[] selfParents = new Event[size];
//
//			@Override
//			public void run() {
//				while (!stopIt.get()) {
//					Event[] sps = flag.grab(shardId, creatorId, currentSeq++, selfParents);
//					if (sps != null) {
//						selfParents = sps;
//					} else {
//						currentSeq--;
//					}
//				}
//
//			}
//
//		});
//		t.start();
//	}

//	private static class Stop {
//		private boolean stop = false;
//
//		public boolean get() {
//			return stop;
//		}
//
//		public void flip() {
//			stop = !stop;
//		}
//	}

//	private static class Flag {
//		private Lock lock = new ReentrantLock();
//
//		private Event[] lastBearer = null;
//		// private List<Event> list = Collections.synchronizedList(new
//		// ArrayList<Event>());
//		private Hashnet[] hashnets = null;
//
//		public Flag(Hashnet[] hashnets) {
//			super();
//			this.hashnets = hashnets;
//			lastBearer = new Event[hashnets.length];
//		}
//
//		public Event[] grab(int shardId, long creatorId, long creatorSeq, Event[] selfParents) {
//			lock.lock();
//			Event[] newEvents = new Event[hashnets.length];
//
//			try {
//				if (lastBearer != null && lastBearer[0] != null && lastBearer[0].getCreatorId() == creatorId) {
//					return null;
//				}
//
////				Event event = new Event(shardId, creatorId, creatorSeq, -1, -1, selfParent, lastBearer, Instant.now(),
////						new byte[0], -1, new byte[0], new byte[0][0]);
//				// hashnet.consRecordEvent(event);
//				for (int index = 0; index < hashnets.length; index++) {
//					newEvents[index] = new Event(shardId, creatorId, creatorSeq, -1, -1, selfParents[index],
//							lastBearer[index], Instant.now(), new byte[0], -1, new byte[0], new byte[0][0]);
//					hashnets[index].consRecordEvent(newEvents[index]);
//
//					lastBearer[index] = newEvents[index];
//				}
//
//				sizeofEvents++;
//
//				return newEvents;
//			} finally {
//				lock.unlock();
//			}
//		}
//
//		public Hashnet[] getHashnet() {
//			return hashnets;
//		}
//
////		public Event[] getOrderedEvent(int shardId) {
////			Event[] events = hashnet.getAllConsEvents(shardId);
////
////			return events;
////		}
//	}

}
