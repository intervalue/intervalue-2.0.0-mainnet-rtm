package one.inve.localfullnode2.staging;

import java.util.HashMap;
import java.util.Queue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import one.inve.localfullnode2.utilities.StringUtils;

/**
 * 
 * Copyright © CHXX Co.,Ltd. All rights reserved.
 * 
 * @Description: Manage all queues by which the individual process communicate
 *               with others.A drawback is {@link LinkedBlockingQueue} which is
 *               widely used.
 * @author: Francis.Deng
 * @date: Dec 7, 2018 12:52:45 AM
 * @version: V1.0
 */
public class StagingArea {

	private Lock lock = new ReentrantLock();
	@SuppressWarnings("rawtypes")
	private HashMap<String, Queue> queues = new HashMap<>();

	// an economic approach to share a queue by class
	@SuppressWarnings("rawtypes")
	public <T> BlockingQueue getQueue(Class<T> clazz, int size) {
		return getQueue(clazz, null, size);
	}

	@SuppressWarnings("rawtypes")
	public <T> BlockingQueue getQueue(Class<T> clazz, String tp, int size) {
		lock.lock();
		Queue queue = null;
		try {
			queue = queues.get(toName(clazz, tp));
			if (queue == null) {
				queue = new LinkedBlockingQueue<T>(size);
			}

		} finally {
			lock.unlock();
		}

		return (BlockingQueue) queue;
	}

	protected String toName(Class clazz, String tp) {
		String type = StringUtils.isEmpty(tp) ? "" : "[[" + tp;

		return clazz.getCanonicalName() + type;
	}
}
