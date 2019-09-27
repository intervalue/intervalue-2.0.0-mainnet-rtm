package one.inve.localfullnode2.gossip.persistence;

import java.math.BigInteger;
import java.util.concurrent.BlockingQueue;

import one.inve.core.EventBody;

/**
 * 
 * Copyright © INVE FOUNDATION. All rights reserved.
 * 
 * @Description: a dependence description of gossip new events
 * @author: Francis.Deng
 * @date: May 17, 2019 8:23:18 PM
 * @version: V1.0
 */
public interface NewGossipEventsPersistenceDependent {
	BlockingQueue<EventBody> getEventSaveQueue();

	String getDbId();

	/**
	 * <snippet>node.setTotalEventCount(node.getTotalEventCount().add(BigInteger.ONE));</snippet>
	 */
	void addTotalEventCount(long delta);

	BigInteger getTotalEventCount();
}
