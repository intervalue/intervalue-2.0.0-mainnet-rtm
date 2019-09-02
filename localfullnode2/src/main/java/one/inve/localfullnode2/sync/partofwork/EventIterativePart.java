package one.inve.localfullnode2.sync.partofwork;

import java.math.BigInteger;
import java.util.concurrent.BlockingQueue;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import one.inve.core.EventBody;
import one.inve.localfullnode2.gossip.persistence.NewGossipEventsPersistence;
import one.inve.localfullnode2.gossip.persistence.NewGossipEventsPersistenceDependent;
import one.inve.localfullnode2.staging.StagingArea;
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

		if (distributedObjects.getObjects() == null || distributedObjects.getObjects().length == 0) {
			done = true;
			return;
		}
		// int length = distributedObjects.getObjects().length;

		// handle the batch of EventBodies
		NewGossipEventsPersistence newGossipEventsPersistence = new NewGossipEventsPersistence();
		newGossipEventsPersistence.persistNewEvents(new NewGossipEventsPersistenceDependent() {

			@Override
			public BlockingQueue<EventBody> getEventSaveQueue() {
				StagingArea stagingArea = new StagingArea();
				stagingArea.createQueue(EventBody.class, StagingArea.EventSaveQueueName, 10000000, null);

				BlockingQueue<EventBody> q = stagingArea.getQueue(EventBody.class, StagingArea.EventSaveQueueName);
				for (EventBody eb : distributedObjects.getObjects()) {
					q.add(eb);
				}

				return q;
			}

			@Override
			public String getDbId() {
				return profile.getDBId();
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

		});

		context.join(distributedObjects.getDist());

	}

}
