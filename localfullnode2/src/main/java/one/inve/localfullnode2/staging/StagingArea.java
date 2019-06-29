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
	public static final String ShardSortQueueName = "shardSortQueue";
	public static final String ConsEventHandleQueueName = "consEventHandleQueue";
	public static final String ConsMessageVerifyQueueName = "consMessageVerifyQueue";
	public static final String ConsMessageHandleQueueName = "consMessageHandleQueue";
	public static final String ConsMessageSaveQueueName = "consMessageSaveQueue";
	public static final String SystemAutoTxSaveQueueName = "systemAutoTxSaveQueue";

	private Lock lock = new ReentrantLock();
	@SuppressWarnings("rawtypes")
	private HashMap<String, Queue> queues = new HashMap<>();

	public <T> BlockingQueue<T> getQueue(Class<T> clazz, String tp) {
		return getQueue(clazz, tp, 0);
	}

	@SuppressWarnings("rawtypes")
	public <T> BlockingQueue<T> getQueue(Class<T> clazz, String tp, int index) {
		Queue q = queues.get(toName(clazz, tp, index));
		return q == null ? null : (BlockingQueue) q;
	}

	@SuppressWarnings("rawtypes")
	public <T> void setQueue(Class<T> clazz, String tp, int size, BlockingQueue<T> queue) {
		queues.put(toName(clazz, tp, size), queue);
	}

	// an economic approach to share a queue by class
	@SuppressWarnings("rawtypes")
	public <T> BlockingQueue<T> createQueue(Class<T> clazz) {
		return createQueue(clazz, null, Integer.MAX_VALUE, null);
	}

	public <T> BlockingQueue<T> createQueue(Class<T> clazz, String tp, int size, ElementModifiable modifier) {
		return createQueue(clazz, tp, size, 0, modifier);
	}

	@SuppressWarnings("rawtypes")
	public <T> BlockingQueue<T> createQueue(Class<T> clazz, String tp, int size, int index,
			ElementModifiable modifier) {
		lock.lock();
		Queue queue = null;
		try {
			queue = queues.get(toName(clazz, tp, index));
			if (queue == null) {
				RemovableBlockingMechanismQueue rbmQueue = new RemovableBlockingMechanismQueue<T>(size, modifier);
				rbmQueue.setIdentifier(toName(clazz, tp, index));
				queues.put(toName(clazz, tp, index), rbmQueue);
			}

		} finally {
			lock.unlock();
		}

		return (BlockingQueue) queue;
	}

	protected String toName(Class clazz, String tp, int index) {
		String type = StringUtils.isEmpty(tp) ? "" : "[[" + tp + "[[" + index;

		return clazz.getCanonicalName() + type;
	}
}
