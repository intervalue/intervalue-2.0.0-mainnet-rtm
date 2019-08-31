package one.inve.localfullnode2.sync.partofwork;

import java.math.BigInteger;
import java.util.concurrent.BlockingQueue;

import one.inve.core.EventBody;
import one.inve.localfullnode2.gossip.persistence.NewGossipEventsPersistence;
import one.inve.localfullnode2.gossip.persistence.NewGossipEventsPersistenceDependent;
import one.inve.localfullnode2.staging.StagingArea;
import one.inve.localfullnode2.sync.DistributedObjects;
import one.inve.localfullnode2.sync.ISyncContext;
import one.inve.localfullnode2.sync.SynchronizationWork.BasedIterativePart;
import one.inve.localfullnode2.sync.measure.Distribution;
import one.inve.localfullnode2.sync.source.ISyncSource;
import one.inve.localfullnode2.sync.source.ISyncSourceProfile;

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

	@Override
	public void runOnce(ISyncContext context) {
		Distribution myDist = context.getDistribution();
		ISyncSource synSource = context.getSyncSourceProxy();

		ISyncSourceProfile srcProfile = getSourceProfile(context);

		DistributedObjects<EventBody> distributedObjects = synSource.getNotInDistributionEvents(myDist);
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
				return srcProfile.getDBId();
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

		context.joinDistribution(distributedObjects.getDist());

	}

}
