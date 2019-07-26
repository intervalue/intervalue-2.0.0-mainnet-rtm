package one.inve.localfullnode2.utilities.concurrent;

import java.util.Queue;
import java.util.concurrent.Executor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 
 * Copyright © INVE FOUNDATION. All rights reserved.
 * 
 * @Description: There is an assumption that a time-consuming task(I/O,like disk
 *               I/O or network I/O) will account for larger proportion than the
 *               rest(non-I/O).The time-consuming task is passed by
 *               construction.
 * @author: Francis.Deng
 * @date: Oct 5, 2018 12:50:09 AM
 * @version: V1.0
 */
public class ParadigmLoop extends SingleThreadExecutor {

	private static final Logger logger = LoggerFactory.getLogger("loop");

	private volatile int ioRatio = 50;
	private IO ioTask;

	public ParadigmLoop(Queue<Runnable> taskQueue, Executor executor, RejectedExecutionHandler rejectedHandler,
			boolean addTaskWakesUp, IO ioTask) {
		super(taskQueue, executor, rejectedHandler, addTaskWakesUp);
		this.ioTask = ioTask;
	}

	@FunctionalInterface
	public static interface IO {
		void handle();
	}

	/**
	 * Returns the percentage of the desired amount of time spent for I/O in the
	 * event loop.
	 */
	public int getIoRatio() {
		return ioRatio;
	}

	/**
	 * Sets the percentage of the desired amount of time spent for I/O in the event
	 * loop. The default value is {@code 50}, which means the loop will try to spend
	 * the same amount of time for I/O as for non-I/O tasks.
	 */
	public void setIoRatio(int ioRatio) {
		if (ioRatio <= 0 || ioRatio > 100) {
			throw new IllegalArgumentException("ioRatio: " + ioRatio + " (expected: 0 < ioRatio <= 100)");
		}
		this.ioRatio = ioRatio;
	}

	@Override
	protected void run() {
		for (;;) {
			try {
				final int ioRatio = this.ioRatio;
				if (ioRatio == 100) {
					try {
						processIO();
					} finally {
						// Ensure we always run tasks.
						runAllTasks();
					}
				} else {
					final long ioStartTime = System.nanoTime();
					try {
						processIO();
					} finally {
						// Ensure we always run tasks.
						final long ioTime = System.nanoTime() - ioStartTime;
						runAllTasks(ioTime * (100 - ioRatio) / ioRatio);
					}
				}
			} catch (Throwable t) {
				handleLoopException(t);
			}
			// Always handle shutdown even if the loop processing threw an exception.
			try {
				if (isShuttingDown()) {
					// closeAll();
					if (confirmShutdown()) {
						return;
					}
				}
			} catch (Throwable t) {
				handleLoopException(t);
			}
		}
	}

	private void processIO() {
		ioTask.handle();
	}

	private static void handleLoopException(Throwable t) {
		logger.warn("Unexpected exception in the selector loop.", t);

		// Prevent possible consecutive immediate failures that lead to
		// excessive CPU consumption.
		try {
			Thread.sleep(1000);
		} catch (InterruptedException e) {
			// Ignore.
		}
	}

}
