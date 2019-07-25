package one.inve.localfullnode2.hashnet;

/**
 * 
 * Copyright © INVE FOUNDATION. All rights reserved.
 * 
 * @Description: replace addEvent::addEvent with this,which allow a caller to
 *               pass custom object definition
 * @author: Francis.Deng
 * @date: November 2, 2018 10:51:41 PM
 * @version: V1.0
 */
public class OpenHashnet<C> extends Hashnet {

	public OpenHashnet(int shardCount, int numNodes) {
		super(shardCount, numNodes);
	}

	// turn any objects into Event
	@FunctionalInterface
	public static interface EventChangeable<C> {
		Event toEvent(C c);
	}

	public synchronized boolean addEvent(C c, EventChangeable<C> ec) {
		this.consRecordEvent(ec.toEvent(c));
		return true;
	}

}
