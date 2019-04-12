package one.inve.threads.localfullnode;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.RecursiveTask;
import java.util.stream.Collectors;

import one.inve.util.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alibaba.fastjson.JSONObject;

import one.inve.beans.dao.Transaction;
import one.inve.core.Config;
import one.inve.node.Main;
import one.inve.utils.SignUtil;

/**
 * 共识消息签名验证线程
 * 
 * @author Clare
 * @date 2018/7/30 0030.
 */

public class ConsensusMessageVerifyThread extends Thread {
	private static final Logger logger = LoggerFactory.getLogger(ConsensusMessageVerifyThread.class);

	Main node;
	StringBuilder statisticInfo = new StringBuilder()
			.append("\n===== node-({}, {}): Consensus message verify thread =====")
			.append("\ngenerate cost: {} ms\nverify cost: {} ms\nmessage count: {}\navg verify cost: {} ms/t")
			.append("\nConsMessageVerifyQueue rest size: {}\nConsMessageHandleQueue rest size: {}");
	public ConsensusMessageVerifyThread(Main node) {
		this.node = node;
	}

	@Override
	public void run() {
		logger.info(">>> start ConsensusMessageVerifyThread...");
		// 消息
		List<JSONObject> messages = new ArrayList<>();
		Instant t0;
		Instant t1;
		long interval1;
		long interval2;
		while (true) {
			try {
				t0 = Instant.now();
				t1 = Instant.now();

				// 时间间隔和交易数量2个维度来控制共识message签名验证数量和频率
				while (Duration.between(t0, t1).toMillis() < Config.TXS_VEFRIFY_TIMEOUT) {
					// 取共识message
					JSONObject msgObject = node.getConsMessageVerifyQueue().poll();
					if (msgObject != null) {
						messages.add(msgObject);
					}
					if (messages.size() >= Config.MAX_TXS_VEFRIFY_COUNT) {
						break;
					}
					t1 = Instant.now();
				}

				long msgCount = messages.size();
				if (msgCount > 0) {
					if (logger.isDebugEnabled()) {
						logger.debug("========= node-({}, {}): messages size: {}", node.getShardId(),
								node.getCreatorId(), messages.size());
					}
					// 签名验证和排序
					messages = messages.parallelStream()
							.peek(o -> {
								if (StringUtils.isNotEmpty(o.getString("msg"))) {
									o.put("isValid", SignUtil.verify(o.getString("msg")));
								}
							})
							.sorted(Comparator.comparing(n -> n.getBigInteger("id")))
							.collect(Collectors.toList());
					// 共识消息放入消息处理执行队列
					node.getConsMessageHandleQueue().addAll(deepClone(messages));
					messages.clear();
				}
				if (msgCount > 0) {
					interval1 = Duration.between(t0, t1).toMillis();
					interval2 = Duration.between(t1, Instant.now()).toMillis();
					logger.info(statisticInfo.toString(), node.getShardId(), node.getCreatorId(),
							interval1, interval2, msgCount,
							new BigDecimal(interval2).divide(BigDecimal.valueOf(msgCount), 2, BigDecimal.ROUND_HALF_UP),
							node.getConsMessageVerifyQueue().size(),
							node.getConsMessageHandleQueue().size());
				}
			} catch (Exception e) {
				logger.error("node-({}, {}): messages verify thread error: {}", node.getShardId(), node.getCreatorId(), e);
			}
		}
	}

	private static ArrayList<JSONObject> deepClone(List<JSONObject> messages) {
		ArrayList<JSONObject> listCopy = new ArrayList<>();
		for (JSONObject message : messages) {
			listCopy.add((JSONObject)message.clone());
		}
		return listCopy;
	}

	private static List<Transaction> parallelStreamVerifyTxsSignature(List<JSONObject> list) {
		return list.parallelStream()
				.map(o -> new Transaction.Builder().id(o.getBigInteger("id")).eHash(o.getString("eHash"))
						.hash(o.getString("signature")).signature(o.getString("signature"))
						.pubkey(o.getString("pubkey")).fromAddress(o.getString("fromAddress"))
						.toAddress(o.getString("toAddress")).amount(o.getBigInteger("amount"))
						.fee(o.getBigInteger("fee")).time(o.getLong("timestamp")).remark(o.getString("remark"))
						.updateTime(Instant.now().toEpochMilli()).isStable(true).isValid(SignUtil.verify(o)).build())
				.collect(Collectors.toList());
	}

	private static List<Transaction> forEachVerifyTxsSignature(List<JSONObject> list) {
		List<Transaction> txs = new ArrayList<>();
		for (JSONObject o : list) {
			txs.add(new Transaction.Builder().id(o.getBigInteger("id")).eHash(o.getString("eHash"))
					.hash(o.getString("signature")).signature(o.getString("signature")).pubkey(o.getString("pubkey"))
					.fromAddress(o.getString("fromAddress")).toAddress(o.getString("toAddress"))
					.amount(o.getBigInteger("amount")).fee(o.getBigInteger("fee")).time(o.getLong("timestamp"))
					.remark(o.getString("remark")).updateTime(Instant.now().toEpochMilli()).isStable(true)
					.isValid(SignUtil.verify(o)).build());
		}
		return txs;
	}

	private static List<Transaction> forkJoinVerifyTxsSignature(List<JSONObject> list) {
		logger.warn("available processors: {}", Runtime.getRuntime().availableProcessors() >> 1);
		ForkJoinPool pool = new ForkJoinPool(Runtime.getRuntime().availableProcessors() >> 1);
		AddTask task = new AddTask(list);
		pool.submit(task);
		return task.join();
	}

	static class AddTask extends RecursiveTask<List<Transaction>> {
		List<JSONObject> o;

		public AddTask(List<JSONObject> o) {
			this.o = o;
		}

		@Override
		protected List<Transaction> compute() {
			List<Transaction> txs = new ArrayList<>();
			if (null == o || o.size() < 1) {
				return null;
			} else if (o.size() == 1) {
				txs.add(new Transaction.Builder().id(o.get(0).getBigInteger("id")).eHash(o.get(0).getString("eHash"))
						.hash(o.get(0).getString("signature")).signature(o.get(0).getString("signature"))
						.pubkey(o.get(0).getString("pubkey")).fromAddress(o.get(0).getString("fromAddress"))
						.toAddress(o.get(0).getString("toAddress")).amount(o.get(0).getBigInteger("amount"))
						.fee(o.get(0).getBigInteger("fee")).time(o.get(0).getLong("timestamp"))
						.remark(o.get(0).getString("remark")).updateTime(Instant.now().toEpochMilli()).isStable(true)
						.isValid(SignUtil.verify(o.get(0))).build());
				return txs;
			} else {
				AddTask task1 = new AddTask(o.subList(0, o.size() >> 1));
				AddTask task2 = new AddTask(o.subList(o.size() >> 1, o.size()));
				task1.fork();
				task2.fork();
				if (null != task1.join()) {
					txs.addAll(task1.join());
				}
				if (null != task2.join()) {
					txs.addAll(task2.join());
				}
				return txs;
			}
		}
	}



	public static void main(String[] args) {
		long num = 5;
		long size = num;
		String message = " {\"fromAddress\":\"4PS6MZX6T7ELDSD2RUOZRSYGCC5RHOS7\",\"toAddress\":\"NSM733ES3F5JCVE4ZLCXSXKBIGGCYKID\",\"amount\":\"10\",\"timestamp\":1542079566756,\"pubkey\":\"A78IhF6zjQIGzuzKwrjG9HEISz7/oAoEhyr7AnBr3RWn\",\"fee\":\"0\",\"type\":1,\"remark\":\"\",\"signature\":\"32MX5Bi2oVTM4xrUGTV+EZkPw8cFsKM6G0nLn/gdJQqbFMq4vYYFLARAERZugbBjYgc46DcB8sSwe8jpcWWVKexQ==\"}";
//        Instant first = Instant.now();
//        ArrayList<JSONObject> list = new ArrayList<>();
//        while (size-- > 0) {
////            String type = getMessageType(message).split(":")[1].trim();
////            logger.info("msg type: {}, len: {}", type, type.length());
////            TransactionMessage transactionMessage = JSON.parseObject(message, TransactionMessage.class);
//
//            JSONObject obj = parseMessage(message, "333333", num-size);
//            if (null!=obj) {
//                list.add(obj);
//            }
//        }
//
//        logger.info("1 num: {}, total cost: {} ms", num, Duration.between(first, Instant.now()).toMillis());
//        List<JSONObject> list1 = verifySignature(list);
//
//        logger.info("2 num: {}, total cost: {} ms", num, Duration.between(first, Instant.now()).toMillis());

	}
}
