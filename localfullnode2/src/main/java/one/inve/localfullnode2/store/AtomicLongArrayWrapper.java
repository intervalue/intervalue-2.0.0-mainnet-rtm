package one.inve.localfullnode2.store;

import java.util.concurrent.atomic.AtomicLongArray;

/**
 * 
 * 
 * Copyright © CHXX Co.,Ltd. All rights reserved.
 * 
 * @Description: attempt to build {@link AtomicLongArray} model(implements part
 *               of methods) and be aware of writer operations.
 * @author: Francis.Deng
 * @date: May 16, 2019 7:35:17 PM
 * @version: V1.0
 */
public final class AtomicLongArrayWrapper {
	private AtomicLongArray wrapped;
	private WriteNotifiable notifier = null;

	private AtomicLongArrayWrapper(AtomicLongArray wrapped) {
		super();
		this.wrapped = wrapped;
	}

	public static AtomicLongArrayWrapper of(AtomicLongArray wrapped) {
		return new AtomicLongArrayWrapper(wrapped);
	}

	public void set(int i, long newValue) {
		wrapped.set(i, newValue);

		if (notifier != null)
			notifier.notify();
	}

	public long get(int i) {
		return wrapped.get(i);
	}

	public final int length() {
		return wrapped.length();
	}

	public AtomicLongArrayWrapper setNotifier(WriteNotifiable notifier) {
		this.notifier = notifier;
		return this;
	}

	public static interface WriteNotifiable {
		void notify(AtomicLongArrayWrapper atomicLongArrayWrapper);
	}
}
