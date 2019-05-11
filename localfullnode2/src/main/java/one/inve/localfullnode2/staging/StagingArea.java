package one.inve.localfullnode2.staging;

import java.util.HashMap;
import java.util.Queue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import one.inve.localfullnode2.staging.RemovableBlockingMechanismQueue.ElementModifiable;
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
	public static final String MessageQueueName = "messageQueue";
	public static final String EventSaveQueueName = "eventSaveQueue";
	public static final String ConsEventHandleQueueName = "consEventHandleQueue";
	public static final String ConsMessageVerifyQueueName = "consMessageVerifyQueue";
	public static final String ConsMessageHandleQueueName = "consMessageHandleQueue";
	public static final String ConsMessageSaveQueueName = "consMessageSaveQueue";
	public static final String SystemAutoTxSaveQueueName = "systemAutoTxSaveQueue";

	private Lock lock = new ReentrantLock();
	@SuppressWarnings("rawtypes")
	private HashMap<String, Queue> queues = new HashMap<>();

	// an economic approach to share a queue by class
	@SuppressWarnings("rawtypes")
	public <T> BlockingQueue<T> createQueue(Class<T> clazz) {
		return createQueue(clazz, null, Integer.MAX_VALUE, null);
	}

	@SuppressWarnings("rawtypes")
	public <T> BlockingQueue<T> getQueue(Class<T> clazz, String tp) {
		Queue q = queues.get(toName(clazz, tp));
		return q == null ? null : (BlockingQueue) q;
	}

	@SuppressWarnings("rawtypes")
	public <T> BlockingQueue<T> createQueue(Class<T> clazz, String tp, int size, ElementModifiable modifier) {
		lock.lock();
		Queue queue = null;
		try {
			queue = queues.get(toName(clazz, tp));
			if (queue == null) {
				queue = new RemovableBlockingMechanismQueue<T>(size, modifier);
				queues.put(toName(clazz, tp), queue);
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
