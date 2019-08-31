package one.inve.localfullnode2.sync;

import one.inve.localfullnode2.sync.source.ISyncSource;
import one.inve.localfullnode2.sync.source.ISyncSourceProfile;

/**
 * 
 * Copyright © INVE FOUNDATION. All rights reserved.
 * 
 * @ClassName: SynchronizationWork
 * @Description: stress that {@code Part} is a device that is capable of running
 *               over and over multiple times
 * @author Francis.Deng
 * @mailbox francis_xiiiv@163.com
 * @date Aug 23, 2019
 *
 */
public class SynchronizationWork {

	protected interface IterativePart {
		boolean isDone();

		void runOnce(ISyncContext context);
	}

	// initialization is used to reload target host attribution
	public interface SynchronizationWorkInitial {
		boolean run(ISyncConf conf, ISyncContext context);
	}

	public static abstract class BasedIterativePart implements IterativePart {
		private ISyncSourceProfile sourceProfile;

		protected boolean done = false;

		@Override
		public boolean isDone() {
			return done;
		}

		public ISyncSourceProfile getSourceProfile(ISyncContext context) {
			if (sourceProfile == null) {
				ISyncSource synSource = context.getSyncSourceProxy();
				sourceProfile = synSource.getSyncSourceProfile();
			}

			return sourceProfile;
		}
	}

	// ensuring the works are executed in single thread.
	public void run() {
		ISyncConf conf = DefSyncConf.getInstance();
		ISyncContext context = ISyncContext.getDefault(conf);

		SynchronizationWorkInitial initializer = context.getSynchronizationInitializer();
		IterativePart[] parts = context.getSynchronizationWorkParts();

		if (initializer.run(conf, context)) {// ensure that initialization is complete
			for (IterativePart part : parts) {
				while (!part.isDone()) {
					part.runOnce(context);
				}
			}
		}

	}
}
