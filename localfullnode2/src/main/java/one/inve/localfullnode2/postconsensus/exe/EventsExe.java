package one.inve.localfullnode2.postconsensus.exe;

import java.math.BigInteger;
import java.time.Duration;
import java.time.Instant;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alibaba.fastjson.JSONObject;

import one.inve.localfullnode2.conf.Config;
import one.inve.localfullnode2.store.EventBody;
import one.inve.localfullnode2.store.EventKeyPair;
import one.inve.localfullnode2.store.rocks.INosql;
import one.inve.utils.DSA;

/**
 * 
 * Copyright © CHXX Co.,Ltd. All rights reserved.
 * 
 * @Description: The process consists of saving event, calculate
 *               statistics(Config.EVT_TX_COUNT_KEY，Config.CONS_EVT_COUNT_KEY，msgHashTreeRoot),check
 *               snapshot point
 * @author: Francis.Deng
 * @see ConsensusEventHandleThread
 * @date: May 3, 2019 8:40:56 PM
 * @version: V1.0
 */
public class EventsExe {
	private static final Logger logger = LoggerFactory.getLogger("eventsexe");

	private EventsExeDependent dep;

	private int selfId = -1;
	// 消息hash根
	private String msgHashTreeRoot = null;
	private BigInteger transCount;

	public EventsExe(EventsExeDependent dep) {
		this.selfId = (int) dep.getCreatorId();
		this.dep = dep;

		this.msgHashTreeRoot = dep.msgHashTreeRoot();
		// key condition - SnapshotPoint
		// SnapshotPoint sp =
		// node.getSnapshotPointMap().get(node.getCurrSnapshotVersion());
//		if (null != sp) {
//			this.vers = node.getCurrSnapshotVersion().add(BigInteger.ONE);
//			logger.warn("node-({}, {}): snapshotpoint repaired, new vers: ", node.getShardId(), node.getCreatorId(),
//					this.vers);
//		} else {
//			this.vers = node.getCurrSnapshotVersion();
//		}

		this.transCount = dep.getConsMessageMaxId();
	}

	public void run() {
//        logger.info(">>> start ConsensusEventHandleThread...");
//        logger.info("node-({}, {}): curr snap vers = {}, transCount = {}, " +
//                        "totalConsEventCount = {}, contributions.size = {}, curr msgHashTreeRoot = {}",
//                node.getShardId(), node.getCreatorId(), this.vers, this.transCount,
//                node.getTotalConsEventCount(), node.getContributions().size(), this.msgHashTreeRoot);

		Instant t0 = Instant.now();
		Instant t1;
		long eventCount = 0L;
		// while (true) {
//            while (-1==selfId) {
//                // 节点在片内的ID不存在，在一直等待
//                try {
//                    sleep(1000);
//                } catch (InterruptedException e) {
//                    e.printStackTrace();
//                }
//            }

		try {
			if (!dep.getConsEventHandleQueue().isEmpty()) {
				// 取共识Event
				EventBody event = dep.getConsEventHandleQueue().poll();
				// 更新共识Event数
				// dep.setTotalConsEventCount(dep.getTotalConsEventCount().add(BigInteger.ONE));
				dep.addTotalConsEventCount(1);
				// key condition
				// 累计各分片各节点event数
//				dep.getContributions()
//						.add(new Contribution.Builder().shardId(event.getShardId()).creatorId(event.getCreatorId())
//								.otherId(event.getOtherId()).otherSeq(event.getOtherSeq()).build());

				// 保存共识Event
				saveConsEvent(event);

				// 将Event打包的交易放入待签名验证共识消息队列，并计算更新本快照版本的所有消息hash根
				addConsMessage2VerifyQueue(event);

				// key condition - 达到生成快照点条件，则生成快照点
				// createSnapshotPoint(event);

				// 打印信息
				eventCount++;
				t1 = Instant.now();
				long interval = Duration.between(t0, t1).toMillis();
//					if (interval > 5000) {
//						logger.info(statisticInfo.toString(), node.getShardId(), node.getCreatorId(), interval,
//								eventCount, node.getConsEventHandleQueue().size(),
//								node.getConsMessageVerifyQueue().size());
//
//						t0 = t1;
//						eventCount = 0L;
//					}
			} else {
				// sleep(50);
			}
		} catch (Exception e) {
			e.printStackTrace();
			logger.error("error: {}", e);
		}
		// }
	}

	/**
	 * 保存共识Event
	 * 
	 * @param event 保存共识event
	 */
	private void saveConsEvent(EventBody event) {
		// RocksJavaUtil rocksJavaUtil = new RocksJavaUtil(dep.dbId());
		INosql rocksJavaUtil = dep.getNosql();
		// 计算并set eventBody的最大transaction的ID
		if (event.getTrans() != null) {
			transCount = transCount.add(BigInteger.valueOf(event.getTrans().length));
			rocksJavaUtil.put(Config.EVT_TX_COUNT_KEY, transCount.toString());
		}
		event.setTransCount(transCount);
		event.setConsEventCount(dep.getTotalConsEventCount());

		EventKeyPair pair = new EventKeyPair(event.getShardId(), event.getCreatorId(), event.getCreatorSeq());
		rocksJavaUtil.put(pair.toString(), JSONObject.toJSONString(event));
		rocksJavaUtil.put(Config.CONS_EVT_COUNT_KEY, dep.getTotalConsEventCount().toString());
	}

	/**
	 * 将Event打包的交易放入待签名验证共识消息队列，并计算更新本快照版本的所有消息hash根
	 * 
	 * @param event 共识Event
	 */
	private void addConsMessage2VerifyQueue(EventBody event) {

		long eventMsgCount = (null != event.getTrans() && event.getTrans().length > 0) ? event.getTrans().length : 0;
//		if (logger.isDebugEnabled()) {
//			if (eventMsgCount > 0) {
//				logger.debug("node-({}, {}): consensus message size: {}", node.getShardId(), node.getCreatorId(),
//						eventMsgCount);
//			}
//		}
		// 共识消息放入消息签名验证队列
		if (eventMsgCount > 0) {
			final String eHash = DSA.encryptBASE64(event.getHash());
			int j = 1;
			int msgCount = event.getTrans().length;

			logger.error(">>>>>>before event.getTrans() " + event.getTrans().length);

			for (byte[] msg : event.getTrans()) {
				// dep.setConsMessageMaxId(dep.getConsMessageMaxId().add(BigInteger.ONE));
				dep.addConsMessageMaxId(1);
				JSONObject o = new JSONObject();
				logger.error(">>>>>>before node.getConsMessageMaxId():" + dep.getConsMessageMaxId());
				o.put("id", dep.getConsMessageMaxId());
				o.put("eHash", eHash);
				o.put("eShardId", event.getShardId());
				o.put("isStable", true);
				o.put("updateTime", event.getConsTimestamp().toEpochMilli());
				o.put("msg", new String(msg));
				if (j++ == msgCount) {
					o.put("lastIdx", true);
				}
//                logger.warn("id: {}", o.getString("id"));

				// key condition
				// 计算更新消息hash根
//				JSONObject msgObj = JSONObject.parseObject(new String(msg));
//				if (StringUtils.isEmpty(msgHashTreeRoot)) {
//					msgHashTreeRoot = DSA.encryptBASE64(Hash.hash(msgObj.getString("signature")));
//				} else {
//					msgHashTreeRoot = DSA.encryptBASE64(Hash.hash(msgHashTreeRoot, msgObj.getString("signature")));
//				}

				try {
					dep.getConsMessageVerifyQueue().put(o);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}
	}
}
