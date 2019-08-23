package one.inve.localfullnode2.sync;

/**
 * 
 * Copyright © INVE FOUNDATION. All rights reserved.
 * 
 * @ClassName: SynchronizationWork
 * @Description: stress that {@code Part} is a device that is able to run over
 *               and over multiple times
 * @author Francis.Deng
 * @mailbox francis_xiiiv@163.com
 * @date Aug 23, 2019
 *
 */
public class SynchronizationWork {

	protected interface Part {
		boolean isDone();

		void runOnce(IContext context);
	}

	// ensuring the works are executed in single thread.
	public void run() {
		IConf conf = null;
		IContext context = conf.getDefaultContext();

		Part[] parts = context.getSynchronizationWorkParts();

		for (Part part : parts) {
			while (!part.isDone()) {
				part.runOnce(context);
			}
		}
	}
}
