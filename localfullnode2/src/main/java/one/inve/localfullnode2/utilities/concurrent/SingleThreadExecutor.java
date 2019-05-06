package one.inve.localfullnode2.utilities.concurrent;

import java.util.List;
import java.util.Queue;
import java.util.concurrent.AbstractExecutorService;
import java.util.concurrent.Executor;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 
 * Copyright © CHXX Co.,Ltd. All rights reserved.
 * 
 * @Description: Abstract base class for {@link ParadigmLoop}'s that execute all
 *               its submitted tasks in a single thread.
 * @author: Francis.Deng
 * @date: Oct 4, 2018 2:52:35 AM
 * @version: V1.0
 */
public abstract class SingleThreadExecutor extends AbstractExecutorService {
	private static final Logger logger = LoggerFactory.getLogger("singlethreadexecutor");

	private static final int ST_NOT_STARTED = 1;
	private static final int ST_STARTED = 2;
	private static final int ST_SHUTTING_DOWN = 3;
	private static final int ST_SHUTDOWN = 4;
	private static final int ST_TERMINATED = 5;

	private final Queue<Runnable> taskQueue;
	private final Executor executor;
	private final RejectedExecutionHandler rejectedExecutionHandler;

	private volatile Thread thread;
	private volatile boolean interrupted;

	private final Semaphore threadLock = new Semaphore(0);

	private final boolean addTaskWakesUp;

	private volatile int state = ST_NOT_STARTED;

	private long lastExecutionTime;

	private volatile long gracefulShutdownQuietPeriod;
	private volatile long gracefulShutdownTimeout;
	private long gracefulShutdownStartTime;

	private static final long START_TIME = System.nanoTime();

	static long nanoTime() {
		return System.nanoTime() - START_TIME;
	}

	private static final Runnable WAKEUP_TASK = new Runnable() {
		@Override
		public void run() {
			// Do nothing.
		}
	};
	private static final Runnable NOOP_TASK = new Runnable() {
		@Override
		public void run() {
			// Do nothing.
		}
	};

	protected SingleThreadExecutor(Queue<Runnable> taskQueue, Executor executor,
			RejectedExecutionHandler rejectedHandler, boolean addTaskWakesUp) {
		super();
		this.taskQueue = taskQueue;
		this.executor = executor;
		this.rejectedExecutionHandler = rejectedHandler;
		this.addTaskWakesUp = addTaskWakesUp;
	}

	@Override
	@Deprecated
	public void shutdown() {
		// TODO Auto-generated method stub

	}

	@Override
	@Deprecated
	public List<Runnable> shutdownNow() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean isShutdown() {
		return state >= ST_SHUTDOWN;
	}

	@Override
	public boolean isTerminated() {
		return state == ST_TERMINATED;
	}

	@Override
	public boolean awaitTermination(long timeout, TimeUnit unit) throws InterruptedException {
		if (unit == null) {
			throw new NullPointerException("unit");
		}

		if (inLoop()) {
			throw new IllegalStateException("cannot await termination of the current thread");
		}

		if (threadLock.tryAcquire(timeout, unit)) {
			threadLock.release();
		}

		return isTerminated();
	}

	@Override
	public void execute(Runnable task) {
		if (task == null) {
			throw new NullPointerException("task");
		}

		boolean inLoop = inLoop();
		addTask(task);
		if (!inLoop) {
			startThread();
			if (isShutdown() && removeTask(task)) {
				reject();
			}
		}

		if (!addTaskWakesUp && wakesUpForTask(task)) {
			wakeup(inLoop);
		}
	}

	protected void addTask(Runnable task) {
		if (task == null) {
			throw new NullPointerException("task");
		}
		if (!offerTask(task)) {
			reject(task);
		}
	}

	final boolean offerTask(Runnable task) {
		if (isShutdown()) {
			reject();
		}
		return taskQueue.offer(task);
	}

	protected final void reject(Runnable task) {
		rejectedExecutionHandler.rejected(task, this);
	}

	protected static void reject() {
		throw new RejectedExecutionException("event executor terminated");
	}

	public boolean inLoop() {
		return inLoop(Thread.currentThread());
	}

	public boolean inLoop(Thread thread) {
		return thread == this.thread;
	}

	private void startThread() {
		if (state == ST_NOT_STARTED) {
			// if (STATE_UPDATER.compareAndSet(this, ST_NOT_STARTED, ST_STARTED)) {
			try {
				doStartThread();
				state = ST_STARTED;
			} catch (Throwable cause) {
				// STATE_UPDATER.set(this, ST_NOT_STARTED);
				// PlatformDependent.throwException(cause);
				state = ST_NOT_STARTED;
				throw new RuntimeException(cause);

			}
			// }
		}
	}

	private void doStartThread() {
		assert thread == null;
		executor.execute(new Runnable() {
			@Override
			public void run() {
				thread = Thread.currentThread();
				if (interrupted) {
					thread.interrupt();
				}

				boolean success = false;
				// updateLastExecutionTime();
				try {
					SingleThreadExecutor.this.run();
					success = true;
				} catch (Throwable t) {
					logger.warn("Unexpected exception from an executor: ", t);
				} finally {
					for (;;) {
						int oldState = state;
						if (oldState >= ST_SHUTTING_DOWN) {
							state = ST_SHUTTING_DOWN;
							break;
						}
					}

					// Check if confirmShutdown() was called at the end of the loop.
					if (success && gracefulShutdownStartTime == 0) {
						if (logger.isErrorEnabled()) {
							logger.error("Buggy " + AbstractExecutorService.class.getSimpleName() + " implementation; "
									+ SingleThreadExecutor.class.getSimpleName() + ".confirmShutdown() must "
									+ "be called before run() implementation terminates.");
						}
					}

					try {
						// Run all remaining tasks and shutdown hooks.
						for (;;) {
							if (confirmShutdown()) {
								break;
							}
						}
					} finally {
						try {
							cleanup();
						} finally {
							state = ST_TERMINATED;
							threadLock.release();
							if (!taskQueue.isEmpty()) {
								if (logger.isWarnEnabled()) {
									logger.warn("An event executor terminated with " + "non-empty task queue ("
											+ taskQueue.size() + ')');
								}
							}

							// terminationFuture.setSuccess(null);
						}
					}
				}
			}
		});
	}

	protected abstract void run();

	protected boolean removeTask(Runnable task) {
		if (task == null) {
			throw new NullPointerException("task");
		}
		return taskQueue.remove(task);
	}

	@SuppressWarnings("unused")
	protected boolean wakesUpForTask(Runnable task) {
		return true;
	}

	protected void wakeup(boolean inLoop) {
		if (!inLoop || state == ST_SHUTTING_DOWN) {
			// Use offer as we actually only need this to unblock the thread and if offer
			// fails we do not care as there
			// is already something in the queue.
			taskQueue.offer(WAKEUP_TASK);
		}
	}

	/**
	 * Confirm that the shutdown if the instance should be done now!
	 */
	protected boolean confirmShutdown() {
		if (!isShuttingDown()) {
			return false;
		}

		if (!inLoop()) {
			throw new IllegalStateException("must be invoked from an event loop");
		}

		// cancelScheduledTasks();

		if (gracefulShutdownStartTime == 0) {
			gracefulShutdownStartTime = nanoTime();
		}

		if (runAllTasks()) {
			if (isShutdown()) {
				// Executor shut down - no new tasks anymore.
				return true;
			}

			// There were tasks in the queue. Wait a little bit more until no tasks are
			// queued for the quiet period or
			// terminate if the quiet period is 0.
			if (gracefulShutdownQuietPeriod == 0) {
				return true;
			}
			wakeup(true);
			return false;
		}

		final long nanoTime = nanoTime();

		if (isShutdown() || nanoTime - gracefulShutdownStartTime > gracefulShutdownTimeout) {
			return true;
		}

		if (nanoTime - lastExecutionTime <= gracefulShutdownQuietPeriod) {
			// Check if any tasks were added to the queue every 100ms.
			// TODO: Change the behavior of takeTask() so that it returns on timeout.
			wakeup(true);
			try {
				Thread.sleep(100);
			} catch (InterruptedException e) {
				// Ignore
			}

			return false;
		}

		// No tasks were added for last quiet period - hopefully safe to shut down.
		// (Hopefully because we really cannot make a guarantee that there will be no
		// execute() calls by a user.)
		return true;
	}

	public boolean isShuttingDown() {
		return state >= ST_SHUTTING_DOWN;
	}

	/**
	 * Do nothing, sub-classes may override
	 */
	protected void cleanup() {
		// NOOP
	}

	/**
	 * Poll all tasks from the task queue and run them via {@link Runnable#run()}
	 * method. This method stops running the tasks in the task queue and returns if
	 * it ran longer than {@code timeoutNanos}.
	 */
	protected boolean runAllTasks(long timeoutNanos) {
		// fetchFromScheduledTaskQueue();
		Runnable task = pollTask();
		if (task == null) {
			return false;
		}

		final long deadline = nanoTime() + timeoutNanos;
		long runTasks = 0;
		long lastExecutionTime;
		for (;;) {
			safeExecute(task);

			runTasks++;

			// Check timeout every 64 tasks because nanoTime() is relatively expensive.
			// XXX: Hard-coded value - will make it configurable if it is really a problem.
			if ((runTasks & 0x3F) == 0) {
				lastExecutionTime = nanoTime();
				if (lastExecutionTime >= deadline) {
					break;
				}
			}

			task = pollTask();
			if (task == null) {
				lastExecutionTime = nanoTime();
				break;
			}
		}

		this.lastExecutionTime = lastExecutionTime;
		return true;
	}

	/**
	 * @see Queue#poll()
	 */
	protected Runnable pollTask() {
		assert inLoop();
		return pollTaskFrom(taskQueue);
	}

	protected static Runnable pollTaskFrom(Queue<Runnable> taskQueue) {
		for (;;) {
			Runnable task = taskQueue.poll();
			if (task == WAKEUP_TASK) {
				continue;
			}
			return task;
		}
	}

	/**
	 * Try to execute the given {@link Runnable} and just log if it throws a
	 * {@link Throwable}.
	 */
	protected static void safeExecute(Runnable task) {
		try {
			task.run();
		} catch (Throwable t) {
			logger.warn("A task raised an exception. Task: {}", task, t);
		}
	}

	/**
	 * Runs all tasks from the passed {@code taskQueue}.
	 *
	 * @param taskQueue To poll and execute all tasks.
	 *
	 * @return {@code true} if at least one task was executed.
	 */
	protected final boolean runAllTasksFrom(Queue<Runnable> taskQueue) {
		Runnable task = pollTaskFrom(taskQueue);
		if (task == null) {
			return false;
		}
		for (;;) {
			safeExecute(task);
			task = pollTaskFrom(taskQueue);
			if (task == null) {
				return true;
			}
		}
	}

	/**
	 * Poll all tasks from the task queue and run them via {@link Runnable#run()}
	 * method.
	 *
	 * @return {@code true} if and only if at least one task was run
	 */
	protected boolean runAllTasks() {
		assert inLoop();
		boolean fetchedAll;
		boolean ranAtLeastOne = false;

//        do {
//            fetchedAll = fetchFromScheduledTaskQueue();
//            if (runAllTasksFrom(taskQueue)) {
//                ranAtLeastOne = true;
//            }
//        } while (!fetchedAll); // keep on processing until we fetched all scheduled tasks.
		if (runAllTasksFrom(taskQueue)) {
			ranAtLeastOne = true;
		}

		if (ranAtLeastOne) {
			lastExecutionTime = nanoTime();
		}

		return ranAtLeastOne;
	}

	public void shutdownGracefully(long quietPeriod, long timeout, TimeUnit unit) {
		if (quietPeriod < 0) {
			throw new IllegalArgumentException("quietPeriod: " + quietPeriod + " (expected >= 0)");
		}
		if (timeout < quietPeriod) {
			throw new IllegalArgumentException(
					"timeout: " + timeout + " (expected >= quietPeriod (" + quietPeriod + "))");
		}
		if (unit == null) {
			throw new NullPointerException("unit");
		}

		if (isShuttingDown()) {
			return;
		}

		boolean inLoop = inLoop();
		boolean wakeup;
		int oldState;
		for (;;) {
			if (isShuttingDown()) {
				return;
			}
			int newState;
			wakeup = true;
			oldState = state;
			if (inLoop) {
				newState = ST_SHUTTING_DOWN;
			} else {
				switch (oldState) {
				case ST_NOT_STARTED:
				case ST_STARTED:
					newState = ST_SHUTTING_DOWN;
					break;
				default:
					newState = oldState;
					wakeup = false;
				}
			}
//            if (STATE_UPDATER.compareAndSet(this, oldState, newState)) {
//                break;
//            }
			this.state = newState;
			break;
		}
		gracefulShutdownQuietPeriod = unit.toNanos(quietPeriod);
		gracefulShutdownTimeout = unit.toNanos(timeout);

		if (oldState == ST_NOT_STARTED) {
			try {
				doStartThread();
			} catch (Throwable cause) {
				this.state = ST_TERMINATED;
				// terminationFuture.tryFailure(cause);

				if (!(cause instanceof Exception)) {
					// Also rethrow as it may be an OOME for example
					throw new RuntimeException(cause);
				}
				return;
			}
		}

		if (wakeup) {
			wakeup(inLoop);
		}

		return;
	}

	/**
	 * Interrupt the current running {@link Thread}.
	 */
	protected void interruptThread() {
		Thread currentThread = thread;
		if (currentThread == null) {
			interrupted = true;
		} else {
			currentThread.interrupt();
		}
	}

}
