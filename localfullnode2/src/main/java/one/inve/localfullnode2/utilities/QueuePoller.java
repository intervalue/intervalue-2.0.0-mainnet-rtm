package one.inve.localfullnode2.utilities;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Queue;

/**
 * 
 * 
 * Copyright © INVE FOUNDATION. All rights reserved.
 * 
 * @Description: The class provides very convenient way to fetch elements from
 *               queue.
 * @author: Francis.Deng
 * @date: May 22, 2019 11:33:41 PM
 * @version: V1.0
 */
public class QueuePoller {

	/**
	 * Retrieves and removes elements until reaching a timeout of
	 * {@code timeoutInMillis} or exceeding {@code amount}
	 */
	public static <T> List<T> poll(Queue<T> queue, long timeoutInMillis, int amount) {
		List<T> l = new ArrayList<>();
		if (queue.size() == 0)
			return Collections.emptyList();

		Instant t0 = Instant.now();
		Instant t1 = Instant.now();

		while (Duration.between(t0, t1).toMillis() < timeoutInMillis) {
			T e = queue.poll();

			if (e != null) {
				l.add(e);
			} else {
				break;
			}

			if (l.size() >= amount) {
				break;
			}
			t1 = Instant.now();
		}

		return l;
	}

	/**
	 * Retrieves and removes elements until reaching a timeout of
	 * {@code timeoutInMillis}
	 */
	public static <T> List<T> poll(Queue<T> queue, long timeoutInMillis) {
		List<T> l = new ArrayList<>();
		if (queue.size() == 0)
			return Collections.emptyList();

		Instant t0 = Instant.now();
		Instant t1 = Instant.now();

		while (Duration.between(t0, t1).toMillis() < timeoutInMillis) {
			T e = queue.poll();

			if (e != null) {
				l.add(e);
			} else {
				break;
			}

			t1 = Instant.now();
		}

		return l;
	}

	/**
	 * Retrieves and removes elements until exceeding {@code amount}
	 */
	public static <T> List<T> poll(Queue<T> queue, int amount) {
		T e;
		List<T> l = new ArrayList<>();
		if (queue.size() == 0)
			return Collections.emptyList();

		Instant t0 = Instant.now();
		Instant t1 = Instant.now();

		while ((e = queue.poll()) != null) {
			l.add(e);

			if (l.size() >= amount) {
				break;
			}
			t1 = Instant.now();
		}

		return l;
	}

	/**
	 * Retrieves and removes all elements
	 */
	public static <T> List<T> poll(Queue<T> queue) {
		T e;
		List<T> l = new ArrayList<>();
		if (queue.size() == 0)
			return Collections.emptyList();

		while ((e = queue.poll()) != null) {
			l.add(e);
		}

		return l;
	}
}
