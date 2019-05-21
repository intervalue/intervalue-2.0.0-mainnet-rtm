package one.inve.localfullnode2.postconsensus.sorting;

import java.math.BigInteger;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingQueue;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import one.inve.localfullnode2.store.EventBody;

/**
 * 
 * Copyright © CHXX Co.,Ltd. All rights reserved.
 * 
 * @Description: sort all events across all nodes.The algorithm is unknown.
 * @author: Francis.Deng
 * @see ConsensusEventAllSortThread
 * @date: May 3, 2019 8:00:21 PM
 * @version: V1.0
 */
public class EventsSorting {
	private static final Logger logger = LoggerFactory.getLogger("eventssorting");

	public void work(EventsSortingDependent dep) {
		// logger.info(">>> start ConsensusEventAllSortThread...");
		List<BlockingQueue<EventBody>> queueList = new ArrayList<>();
		EventBody[] events = new EventBody[dep.getShardCount()];
		EventBody temp = null;
		int smallIndex = 0;
		// 获取每个分片对应的阻塞队列放入List中
		for (int i = 0; i < dep.getShardCount(); i++) {
			queueList.add(dep.getShardSortQueue(i));
		}
		// 取第0个片的event作为比较大小的参照物
		try {
			temp = queueList.get(0).take();
		} catch (InterruptedException e) {
			logger.error("allsort thread queue 0 take error: {}", e);
		}
		// 从第1个片开始循环从每个片对应的队列中取出event塞入event数组中，用来与temp进行比较
		for (int i = 1; i < dep.getShardCount(); i++) {
			try {
				events[i] = queueList.get(i).take();
			} catch (InterruptedException e) {
				logger.error("allsort thread queue {} take error: {}", i, e);
			}
		}

		BigInteger count = BigInteger.ZERO;
		// 上面部分都是初始化，进入while循环后开始不停的比较入库
		// while (true) {
		// for循环比较temp和evnet数组中的每一个event， 直到for循环结束，temp就是共识时间戳最小（共识时间戳一致时片号最小）的event
		for (int i = 0; i < dep.getShardCount(); i++) {
			if (events[i] == null) {
				continue;
			}
			if (temp == null) {
				throw new RuntimeException();
			}
			if (events[i].getConsTimestamp().isBefore(temp.getConsTimestamp())) {
				events[smallIndex] = temp;
				temp = events[i];
				events[i] = null;
				smallIndex = i;
			}
		}

		if (temp != null) {
			// 将全排序后的共识Event放入共识Event处理队列
			Instant time1 = Instant.now();
			try {
				dep.getConsEventHandleQueue().put(temp);
			} catch (Exception e) {
				logger.error("allsort thread consensus event into save queue error: {}", e);
			}

			// temp入库后重新初始化temp，保证每次for循环时temp的初始值都为第0个片的event，就可保证时间戳相等时最小片号先入库
			try {
				// 上一次for循环结束时，如果第0个片的event最小，在入库后temp直接从第0个片对应的消息队列中获取event
				if (smallIndex == 0) {
					temp = queueList.get(smallIndex).take();
					// 上一次for循环结束时，如果最小event不是第0个片的，则将数组中的第0个片的event赋值给temp，并将event数组中第0个event清空。
					// 然后从入库event对应片号的阻塞队列获取新的event放入event数组中，最后初始化smallIndex与temp片号保持一致，开始下一次for循环比较
				} else {
					temp = events[0];
					events[0] = null;
					events[smallIndex] = queueList.get(smallIndex).take();
					smallIndex = 0;
				}
			} catch (InterruptedException e) {
				logger.error("allsort thread switch queue error: {}", e);
			}
//			long interval = Duration.between(time1, Instant.now()).toMillis();
			count = count.add(BigInteger.ONE);
//			if (interval > 150) {
//				logger.info("===^^^=== node-({}, {}): the {}-th ConsensusEventAllSortThread total cost: {} sec",
//						node.getShardId(), node.getCreatorId(), count.toString(), interval / 1000.0);
//			}
			// }
		}
	}
}
