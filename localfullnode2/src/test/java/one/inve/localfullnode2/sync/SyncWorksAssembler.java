package one.inve.localfullnode2.sync;

public class SyncWorksAssembler extends SyncWorksInLab {

	public void run(ISyncContext context) {
		if (context == null) {
			ISyncConf conf = DefSyncTemplate.getInstance();
			context = ISyncContext.getDefault(conf);
		}

		SynchronizationWorkInitial initializer = context.getSynchronizationInitializer();
		IterativePart[] parts = context.getSynchronizationWorkParts();

		if (initializer.run(context)) {// ensure that initialization is complete
//			for (IterativePart part : parts) {
//				while (!part.isDone()) {
//					part.runOnce(context);
//				}
//			}
		}

		SynchronizationNativeRunnable nativeRunner = context.getSynchronizationNativeRunner();
		nativeRunner.run(context);
	}
}
