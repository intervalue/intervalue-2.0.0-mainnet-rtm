package one.inve.localfullnode2.sync.partofwork;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

import one.inve.core.EventBody;
import one.inve.localfullnode2.sync.ISyncContext;
import one.inve.localfullnode2.sync.SyncWorksInLab.NativeRunnable;
import one.inve.localfullnode2.sync.source.ILFN2Profile;

/**
 * 
 * Copyright © INVE FOUNDATION. All rights reserved.
 * 
 * @ClassName: NativeEventsRunner
 * @Description: coordinate its work with {@code NativeEventsLoaderConsumer}
 * @author Francis.Deng [francis_xiiiv@163.com]
 * @date Sep 9, 2019
 *
 */
public abstract class NativeEventsRunner extends NativeEventsLoaderConsumer implements NativeRunnable {

	private BlockingQueue<EventBody> queue = new ArrayBlockingQueue<>(500);

	@Override
	public boolean run(ISyncContext context) {
		ILFN2Profile profile = context.getProfile();
		loadEventsInto(queue, profile.getDBId());

		return true;
	}

}
