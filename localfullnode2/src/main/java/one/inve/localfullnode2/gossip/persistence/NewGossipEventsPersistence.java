package one.inve.localfullnode2.gossip.persistence;

import java.time.Instant;
import java.util.Objects;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alibaba.fastjson.JSONObject;

import one.inve.localfullnode2.conf.Config;
import one.inve.localfullnode2.store.EventBody;
import one.inve.localfullnode2.store.EventKeyPair;
import one.inve.localfullnode2.store.rocks.RocksJavaUtil;

/**
 * 
 * 
 * Copyright © CHXX Co.,Ltd. All rights reserved.
 * 
 * @Description: The new event after gossip should been persisted
 *               immediately.Persist them as much as possible
 * @author: Francis.Deng {@link EventSaveThread}
 * @date: Oct 17, 2018 8:17:02 PM
 * @version: V1.0
 */
public class NewGossipEventsPersistence {
	private static final Logger logger = LoggerFactory.getLogger(NewGossipEventsPersistence.class);
	private RocksJavaUtil rocksJavaUtil;
	private NewGossipEventsPersistenceDependent dep;

	public void persistNewEvents(NewGossipEventsPersistenceDependent dep) {
		logger.info(">>> start up events persistence...");

		this.dep = dep;
		rocksJavaUtil = new RocksJavaUtil(dep.getDbId());

		StringBuilder statisticInfo = new StringBuilder().append("\n===== node-({}, {}): event save thread =====")
				.append("\ncost: {} ms\ntotal event count: {}").append("\nEventSaveQueue size: {}");
		try {
			int i = 0;
			Instant t0 = Instant.now();
			// while (true) {
			// if (!dep.getEventSaveQueue().isEmpty()) {
			while (!dep.getEventSaveQueue().isEmpty()) {
				// desfcp
				// saveEvent(Objects.requireNonNull(node.getEventSaveQueue().poll()));
				saveEvent0(Objects.requireNonNull(dep.getEventSaveQueue().poll()));
				i++;
			}

//				if (i == Config.DEFAULT_EVENT_STATISTICS_COUNT) {
//					logger.info(statisticInfo.toString(), node.getShardId(), node.getCreatorId(),
//							Duration.between(t0, Instant.now()).toMillis(), node.getTotalEventCount(),
//							node.getEventSaveQueue().size());
//					i = 0;
//					t0 = Instant.now();
//				}
			// }
		} catch (Exception e) {
			logger.error("NewGossipEventsPersistence::persistNewEvents error: {}\nexit...", e);
			System.exit(-1);
		}

	}

	private void saveEvent0(EventBody eb) {
		EventKeyPair pair = new EventKeyPair(eb.getShardId(), eb.getCreatorId(), eb.getCreatorSeq());
		try {
			// CachedRockPutter rocksJavaUtil = crp;
			// 保存event(shardId,creatorId,creatorSeq作为key)
			rocksJavaUtil.put(pair.toString(), JSONObject.toJSONString(eb));

			// 保存每个节点的lastSeq
			rocksJavaUtil.put(eb.getShardId() + "_" + eb.getCreatorId(), "" + eb.getCreatorSeq());

			// 保存event key关系(shardId,otherId,otherSeq作为key，查询Event的真正的key)
			EventKeyPair otherPair = new EventKeyPair(eb.getShardId(), eb.getOtherId(), eb.getOtherSeq());
			String key = "o_" + otherPair.toString();
			byte[] data = rocksJavaUtil.get(key);
			if (null == data) {
				rocksJavaUtil.put(key, pair.toString());
			} else {
				StringBuilder sb = new StringBuilder();
				String value = new String(data);
				if (!value.contains(pair.toString())) {
					rocksJavaUtil.put(key, sb.append(value).append(",").append(pair.toString()).toString());
				} else {
					logger.warn("o_pair value exist! key: {}, value: {}", key, pair.toString());
				}
			}

			// 保存总的Event数量
			// dep.setTotalEventCount(dep.getTotalEventCount().add(BigInteger.ONE));
			dep.addTotalEventCount(1);
			rocksJavaUtil.put(Config.EVT_COUNT_KEY, dep.getTotalEventCount().toString());
		} catch (Exception e) {
			logger.error("saveEvent {} error: {}", pair.toString(), e);
			saveEvent0(eb);
		}
	}

}
