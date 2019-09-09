package one.inve.localfullnode2.sync.partofwork;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import one.inve.core.EventBody;

/**
 * 
 * Copyright © INVE FOUNDATION. All rights reserved.
 * 
 * @ClassName: NativeEventsLoaderConsumer
 * @Description: mainly handle the events process without message part.If there
 *               are 30 seconds left to do nothing,the process is finished.
 * @author Francis.Deng [francis_xiiiv@163.com]
 * @date Sep 9, 2019
 *
 */
public class NativeEventsLoaderConsumer extends NativeEventsLoader {
	private static final Logger logger = LoggerFactory.getLogger(NativeEventsLoaderConsumer.class);

	public void consumeEvents(BlockingQueue<EventBody> queue) {
		try {
			EventBody eb = null;
			while ((eb = queue.poll(30, TimeUnit.SECONDS)) != null) {
				logger.debug("seq,shard : {},{}", eb.getCreatorId(), eb.getCreatorSeq());
			}
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
}
