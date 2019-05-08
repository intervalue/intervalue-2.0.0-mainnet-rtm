package one.inve.localfullnode2.hashnet;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.LinkedBlockingQueue;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import one.inve.localfullnode2.store.EventBody;

/**
 * Copyright © CHXX Co.,Ltd. All rights reserved.
 * 
 * @Description: From getAllQueuedEvents to getShardSortQueue,the class execute
 *               hashnet sorting algorithm,the templates comprise
 *               EventBody2HashnetThread and GeneralNode
 * @author: Francis.Deng
 * @date: July 30, 2018 3:03:10 AM
 * @version: V1.0
 */
public class Hashneter {
	private static final Logger logger = LoggerFactory.getLogger(Hashneter.class);

	private Hashnet hashnet;

	public void initHashnet(HashnetDependent dep) throws InterruptedException {
		if (dep.getTotalEventCount().compareTo(BigInteger.ZERO) <= 0) {
			// 创建hashnet
			createHashnet(dep);
		} else {
			// key condition - repair snapshot if possible
			// 根据最新快照，恢复相关快照参数： treeRootMap、snapshotPointMap等
			// DbUtils.detectAndRepairSnapshotData(node);
			// 重载hashnet
			reloadHashnet(dep);
		}
	}

	public void addToHashnet(HashnetDependent dep, int shardId) {
		EventBody[] ebs = dep.getAllQueuedEvents(shardId);
		for (EventBody eb : ebs) {
			hashnet.addEvent(eb);
		}
	}

	/**
	 * 重载hashnet
	 */
	private void reloadHashnet(HashnetDependent dep) throws InterruptedException {
		logger.info(">>>>>> reload Hashnet...");

		// temporal comment
		// initEventStore(node);
		// initEventFlow();

		Map<Integer, LinkedBlockingQueue<EventBody>> shardSortQueue = new HashMap<>();
		if (null == hashnet) {
			hashnet = new Hashnet(dep.getShardCount(), dep.getNValue());
			for (int i = 0; i < dep.getShardCount(); i++) {
				if (shardSortQueue.get(i) == null) {
					LinkedBlockingQueue<EventBody> queueInstance = new LinkedBlockingQueue<>();
					shardSortQueue.put(i, queueInstance);
				}

				// 读取所有Event
				ArrayList<EventBody> events = new ArrayList<>();
				Iterator iter = dep.getEventStore().genOrderedIterator(i, dep.getNValue());
				while (iter.hasNext()) {
					EventBody eb = (EventBody) iter.next();
					events.add(eb);
				}
				Collections.shuffle(events);
				if (events.size() > 0) {
					events.sort(Comparator.comparing(EventBody::getGeneration));
					logger.warn("node-({}, {}): reload event size: {}", dep.getShardId(), dep.getCreatorId(),
							events.size());
				}
				events.forEach(e -> hashnet.addEvent(e));
				logger.warn("node-({}, {}): reload events successfully. shard-{}'s lastSeqs: {} ", dep.getShardId(),
						dep.getCreatorId(), i, dep.getEventStore().getLastSeqsByShardId(i));

				// 恢复共识Event全排序等待队列
				Event[] evts = hashnet.getAllConsEvents(i);
				for (Event evt : evts) {
					dep.getShardSortQueue(i).put(new EventBody.Builder().shardId(i).creatorId(evt.getCreatorId())
							.creatorSeq(evt.getCreatorSeq()).otherId(evt.getOtherId()).otherSeq(evt.getOtherSeq())
							.timeCreated(evt.getTimeCreated()).trans(evt.getTransactions())
							.signature(evt.getSignature()).isFamous(evt.isFamous()).generation(evt.getGeneration())
							.hash(evt.getHash()).consTimestamp(evt.getConsensusTimestamp()).build());
				}

//				logger.warn("node-({}, {}): shard-{} all sort queue(shardSortQueue) size = {} ", node.getShardId(),
//						node.getCreatorId(), i, node.getShardSortQueue(i).size());
			}

		}

		/**
		 * 修复准备生成最新版本快照点需要的相关信息
		 */
		// temporal comment
		// repairCurrSnapshotPointInfo(node);

//        logger.info(">>>>>> reload Hashnet finished.");
//		logger.warn("node-({}, {}): reload Hashnet successfully. shard-0's lastSeqs: {} ", node.getShardId(),
//				node.getCreatorId(), eventStore.getLastSeqsByShardId(0));
	}

	private void createHashnet(HashnetDependent dep) {
		logger.info(">>>>>> init Hashnet...");
		// temporal comment
		// initEventStore(node);
		// initEventFlow();
		if (null == hashnet) {
			hashnet = new Hashnet(dep.getShardCount(), dep.getNValue());
		}

		// temporal comment
		// initFromScratch();
//		logger.warn("node-({}, {}): init Hashnet successfully. shard-0's lastSeqs: {} ", node.getShardId(),
//				node.getCreatorId(), eventStore.getLastSeqsByShardId(0));
	}

}