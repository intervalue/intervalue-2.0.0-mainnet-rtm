package one.inve.localfullnode2.utilities.concurrent;

import io.netty.util.concurrent.SingleThreadEventExecutor;

public interface RejectedExecutionHandler {

	/**
	 * Called when someone tried to add a task to {@link SingleThreadEventExecutor}
	 * but this failed due capacity restrictions.
	 */
	void rejected(Runnable task, SingleThreadExecutor executor);
}
