package one.inve.localfullnode2.postconsensus.readout;

import java.time.Duration;
import java.time.Instant;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import one.inve.localfullnode2.conf.Config;
import one.inve.localfullnode2.hashnet.Event;
import one.inve.localfullnode2.store.EventBody;

/**
 * 
 * Copyright © CHXX Co.,Ltd. All rights reserved.
 * 
 * @Description: read out events inside hashnet algorithm,push them to sorting
 *               queue.
 *               <p>
 *               <code>one.inve.threads.localfullnode.GetConsensusEventsThread</code>
 * @author: Francis.Deng
 * @date: Oct 3, 2018 7:47:48 PM
 * @version: V1.0
 */
public class EventsReadout {
	private static final Logger logger = LoggerFactory.getLogger("readout");

	public void read(EventsReadoutDependent dep) {

		Instant t0;
		Instant t1;
		Event[] evts;
		int[] count = new int[dep.getShardCount()];

		// while (true) {
		t0 = Instant.now();
		t1 = Instant.now();
		if (dep.getShardCount() > count.length) {
			count = new int[dep.getShardCount()];
		}

		while (Duration.between(t0, t1).toMillis() < Config.GET_CONSENSUS_TIMEOUT) {
			for (int shardId = 0; shardId < dep.getShardCount(); shardId++) {
				// 将收到的未知events加入到全排序待入库队列
				evts = dep.getAllConsEvents(shardId);
				for (Event evt : evts) {
					try {
						dep.getShardSortQueue(shardId)
								.put(new EventBody.Builder().shardId(shardId).creatorId(evt.getCreatorId())
										.creatorSeq(evt.getCreatorSeq()).otherId(evt.getOtherId())
										.otherSeq(evt.getOtherSeq()).timeCreated(evt.getTimeCreated())
										.trans(evt.getTransactions()).signature(evt.getSignature())
										.isFamous(evt.isFamous()).generation(evt.getGeneration()).hash(evt.getHash())
										.consTimestamp(evt.getConsensusTimestamp()).build());
					} catch (InterruptedException e) {
						logger.error(">>>>>> ERROR: {}", e);
						e.printStackTrace();
					}
				}
				if (evts.length > 0) {
					count[shardId] += evts.length;
				}
			}
			t1 = Instant.now();
//                if (logger.isDebugEnabled()) {
//                    long interval = Duration.between(t0, t1).toMillis();
//                    if (interval > 350) {
//                        logger.debug("===^^^=== node-({}, {}): GetConsensusEventsThread handle cost: {} sec",
//                                node.getShardId(), node.getCreatorId(), interval / 1000.0);
//                    }
//                }
		}
		// }

	}
}
