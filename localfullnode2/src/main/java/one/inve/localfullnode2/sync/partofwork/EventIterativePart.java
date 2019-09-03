package one.inve.localfullnode2.sync.partofwork;

import java.math.BigInteger;
import java.util.concurrent.BlockingQueue;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import one.inve.core.EventBody;
import one.inve.localfullnode2.gossip.persistence.NewGossipEventsPersistence;
import one.inve.localfullnode2.gossip.persistence.NewGossipEventsPersistenceDependent;
import one.inve.localfullnode2.staging.StagingArea;
import one.inve.localfullnode2.store.mysql.MysqlHelper;
import one.inve.localfullnode2.sync.DistributedObjects;
import one.inve.localfullnode2.sync.ISyncContext;
import one.inve.localfullnode2.sync.SyncException;
import one.inve.localfullnode2.sync.SyncWorksInLab.BasedIterativePart;
import one.inve.localfullnode2.sync.measure.Distribution;
import one.inve.localfullnode2.sync.source.ILFN2Profile;
import one.inve.localfullnode2.sync.source.ISyncSource;

/**
 * 
 * Copyright © INVE FOUNDATION. All rights reserved.
 * 
 * @ClassName: EventSynchronizer
 * @Description: event synchronizer
 * @author Francis.Deng [francis_xiiiv@163.com]
 * @date Aug 24, 2019
 *
 */
public class EventIterativePart extends BasedIterativePart {
	private static final Logger logger = LoggerFactory.getLogger(EventIterativePart.class);

	@Override
	public void runOnce(ISyncContext context) {
		Distribution myDist = context.getDistribution();
		ISyncSource synSource = context.getSyncSourceProxy();
		DistributedObjects<EventBody> distributedObjects;

		ILFN2Profile profile = getSourceProfile(context);

		try {
			distributedObjects = synSource.getNotInDistributionEvents(myDist);
		} catch (Exception e) {
			logger.error("error in retrieving {}", myDist);
			e.printStackTrace();

			throw new SyncException(e);
		}

		if (distributedObjects == null || distributedObjects.getObjects() == null
				|| distributedObjects.getObjects().length == 0) {
			done = true;
			return;
		}
		// int length = distributedObjects.getObjects().length;

		// handle the batch of EventBodies
		NewGossipEventsPersistenceDependency dep = new NewGossipEventsPersistenceDependency(new StagingArea(),
				distributedObjects, profile);
		NewGossipEventsPersistence newGossipEventsPersistence = new NewGossipEventsPersistence();
		newGossipEventsPersistence.persistNewEvents(dep);
//		newGossipEventsPersistence.persistNewEvents(new NewGossipEventsPersistenceDependent() {
//
//			@Override
//			public BlockingQueue<EventBody> getEventSaveQueue() {
//				StagingArea stagingArea = new StagingArea();
//				stagingArea.createQueue(EventBody.class, StagingArea.EventSaveQueueName, 10000000, null);
//
//				BlockingQueue<EventBody> q = stagingArea.getQueue(EventBody.class, StagingArea.EventSaveQueueName);
//				for (EventBody eb : distributedObjects.getObjects()) {
//					q.add(eb);
//				}
//
//				return q;
//			}
//
//			@Override
//			public String getDbId() {
//				// return profile.getDBId();
//
//				new MysqlHelper("0_8", profile.getDBId(), Boolean.FALSE);
//				return "0_8";
//			}
//
//			// ignore {@code addTotalEventCount} and {@code getTotalEventCount}
//			@Override
//			public void addTotalEventCount(long delta) {
//				return;
//			}
//
//			@Override
//			public BigInteger getTotalEventCount() {
//				return BigInteger.ZERO;
//			}
//
//		});

		context.join(distributedObjects.getDist());
		logger.info(context.getDistribution().toString());

	}

	private static class NewGossipEventsPersistenceDependency implements NewGossipEventsPersistenceDependent {

//		private final StagingArea stagingArea;
//		private final DistributedObjects<EventBody> distributedObjects;
		private final ILFN2Profile profile;
		private final BlockingQueue<EventBody> eventBodyQueue;

		@SuppressWarnings("unused")
		public NewGossipEventsPersistenceDependency(StagingArea stagingArea,
				DistributedObjects<EventBody> distributedObjects, ILFN2Profile profile) {
//			this.stagingArea = stagingArea;
//			this.distributedObjects = distributedObjects;
			this.profile = profile;
			this.eventBodyQueue = initBlockingQueue(stagingArea, distributedObjects);
		}

		@SuppressWarnings("unused")
		private BlockingQueue<EventBody> initBlockingQueue(StagingArea stagingArea,
				DistributedObjects<EventBody> distributedObjects) {
			stagingArea.createQueue(EventBody.class, StagingArea.EventSaveQueueName, 10000000, null);

			BlockingQueue<EventBody> q = stagingArea.getQueue(EventBody.class, StagingArea.EventSaveQueueName);
			for (EventBody eb : distributedObjects.getObjects()) {
				q.add(eb);
			}

			return q;
		}

		@Override
		public BlockingQueue<EventBody> getEventSaveQueue() {
//			StagingArea stagingArea = new StagingArea();
//			stagingArea.createQueue(EventBody.class, StagingArea.EventSaveQueueName, 10000000, null);
//
//			BlockingQueue<EventBody> q = stagingArea.getQueue(EventBody.class, StagingArea.EventSaveQueueName);
//			for (EventBody eb : distributedObjects.getObjects()) {
//				q.add(eb);
//			}
//
//			return q;
			return eventBodyQueue;
		}

		@Override
		public String getDbId() {
			// return profile.getDBId();

			new MysqlHelper("0_8", profile.getDBId(), Boolean.FALSE);
			return "0_8";
		}

		// ignore {@code addTotalEventCount} and {@code getTotalEventCount}
		@Override
		public void addTotalEventCount(long delta) {
			return;
		}

		@Override
		public BigInteger getTotalEventCount() {
			return BigInteger.ZERO;
		}

	}

}
