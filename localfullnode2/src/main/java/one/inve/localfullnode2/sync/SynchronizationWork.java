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

	protected interface IterativePart {
		boolean isDone();

		void runOnce(IContext context);
	}

	public interface Whole {
		boolean run(IContext context);
	}

	public static abstract class BasedIterativePart implements IterativePart {
		private ISyncSourceProfile sourceProfile;

		protected boolean done = false;

		@Override
		public boolean isDone() {
			return done;
		}

		public ISyncSourceProfile getSourceProfile(IContext context) {
			if (sourceProfile == null) {
				ISyncSource synSource = context.getSyncSource();
				sourceProfile = synSource.getSyncSourceProfile();
			}

			return sourceProfile;
		}
	}

	// ensuring the works are executed in single thread.
	public void run() {
		IConf conf = null;
		IContext context = IContext.getDefault(conf);

		Whole initializer = context.getSynchronizationInitializer();
		IterativePart[] parts = context.getSynchronizationWorkParts();

		if (initializer.run(context)) {// ensure that initialization is complete
			for (IterativePart part : parts) {
				while (!part.isDone()) {
					part.runOnce(context);
				}
			}
		}

	}
}
