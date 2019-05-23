package one.inve.localfullnode2.message;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alibaba.fastjson.JSONObject;

import one.inve.localfullnode2.conf.Config;
import one.inve.localfullnode2.utilities.QueuePoller;
import one.inve.localfullnode2.utilities.StringUtils;
import one.inve.utils.SignUtil;

/**
 * 
 * Copyright © CHXX Co.,Ltd. All rights reserved.
 * 
 * @Description: verify messages from queue.
 * @author: Francis.Deng
 * @see ConsensusMessageVerifyThread
 * @date: Oct 7, 2018 2:44:24 AM
 * @version: V1.0
 */
public class MessagesVerification {
	private static final Logger logger = LoggerFactory.getLogger(MessagesVerification.class);

	private final MessagesVerificationDependent dep;

	public MessagesVerification(MessagesVerificationDependent dep) {
		this.dep = dep;
	}

	public void verifyMessages() {
		logger.info(">>> start verifyMessages...");
		// 消息
		List<JSONObject> messages = new ArrayList<>();
		Instant t0;
		Instant t1;
		long interval1;
		long interval2;
		// while (true) {
		try {
			t0 = Instant.now();
			t1 = Instant.now();

//			// 时间间隔和交易数量2个维度来控制共识message签名验证数量和频率
//			while (Duration.between(t0, t1).toMillis() < Config.TXS_VEFRIFY_TIMEOUT) {
//				// 取共识message
//				JSONObject msgObject = dep.getConsMessageVerifyQueue().poll();
//				if (msgObject != null) {
//					messages.add(msgObject);
//				}
//				if (messages.size() >= Config.MAX_TXS_VEFRIFY_COUNT) {
//					break;
//				}
//				t1 = Instant.now();
//			}
			messages = QueuePoller.poll(dep.getConsMessageVerifyQueue(), Config.TXS_VEFRIFY_TIMEOUT,
					Config.MAX_TXS_VEFRIFY_COUNT);

			long msgCount = messages.size();
			if (msgCount > 0) {
//				if (logger.isDebugEnabled()) {
//					logger.debug("========= node-({}, {}): messages size: {}", dep.getShardId(), dep.getCreatorId(),
//							messages.size());
//				}
				// 签名验证和排序
				messages = messages.parallelStream().peek(o -> {
					if (StringUtils.isNotEmpty(o.getString("msg"))) {
						o.put("isValid", SignUtil.verify(o.getString("msg")));
					}
				}).sorted(Comparator.comparing(n -> n.getBigInteger("id"))).collect(Collectors.toList());
				// 共识消息放入消息处理执行队列
				dep.getConsMessageHandleQueue().addAll(deepClone(messages));
				messages.clear();
			}
			if (msgCount > 0) {
				interval1 = Duration.between(t0, t1).toMillis();
				interval2 = Duration.between(t1, Instant.now()).toMillis();
//				logger.info(statisticInfo.toString(), node.getShardId(), node.getCreatorId(), interval1, interval2,
//						msgCount,
//						new BigDecimal(interval2).divide(BigDecimal.valueOf(msgCount), 2, BigDecimal.ROUND_HALF_UP),
//						node.getConsMessageVerifyQueue().size(), node.getConsMessageHandleQueue().size());
			}
		} catch (Exception e) {
//			logger.error("node-({}, {}): messages verify thread error: {}", node.getShardId(), node.getCreatorId(), e);
			logger.error("message signature verification error: {}", e.toString());
		}
		// }
	}

	private static ArrayList<JSONObject> deepClone(List<JSONObject> messages) {
		ArrayList<JSONObject> listCopy = new ArrayList<>();
		for (JSONObject message : messages) {
			listCopy.add((JSONObject) message.clone());
		}
		return listCopy;
	}
}
