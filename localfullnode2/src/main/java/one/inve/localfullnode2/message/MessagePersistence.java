package one.inve.localfullnode2.message;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.sql.PreparedStatement;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

import one.inve.cfg.localfullnode.Config;
import one.inve.localfullnode2.store.mysql.MysqlHelper;
import one.inve.localfullnode2.store.mysql.NewTableCreate;
import one.inve.localfullnode2.store.rocks.INosql;
import one.inve.localfullnode2.store.rocks.TransactionMsg;
import one.inve.localfullnode2.store.rocks.TransactionSplit;
import one.inve.localfullnode2.store.rocks.key.MessageIndexes;
import one.inve.localfullnode2.utilities.QueuePoller;

/**
 * 
 * Copyright © INVE FOUNDATION. All rights reserved.
 * 
 * @Description: persist messages or system messages(transaction fee)
 * @author: Francis.Deng
 * @see ConsensusMessageSaveThread
 * @see ConsensusSystemAutoTxSaveThread
 * @date: Oct 7, 2018 8:29:20 PM
 * @version: V1.0
 */
public class MessagePersistence {
	private static final Logger logger = LoggerFactory.getLogger(MessagePersistence.class);

	private final MessagePersistenceDependent dep;

	public MessagePersistence(MessagePersistenceDependent dep) {
		super();
		this.dep = dep;
	}

	public void persisMessages() {
		logger.info(">>> start up message persistence...");

		Instant t0 = null;
		Instant t1 = null;
		NewTableCreate table;
		long messageCount = 0L;
		List<JSONObject> list = new ArrayList<>();
		// while (true) {
		t0 = Instant.now();
		t1 = Instant.now();

//		// 时间间隔和交易数量2个维度来控制交易的入库
//		while (Duration.between(t0, t1).toMillis() < Config.TXS_COMMIT_TIMEOUT) {
//			// 取共识事件
//			for (int i = 0; i < 200; i++) {
//				if (!dep.getConsMessageSaveQueue().isEmpty()) {
//					list.add(dep.getConsMessageSaveQueue().poll());
//				} else {
//					break;
//				}
//			}
//			if (list.size() >= Config.MAX_TXS_COMMIT_COUNT) {
//				break;
//			}
//			t1 = Instant.now();
//		}
		// list = QueuePoller.poll(dep.getConsMessageSaveQueue(),
		// Config.TXS_COMMIT_TIMEOUT, Config.MAX_TXS_COMMIT_COUNT);
		list = QueuePoller.poll(dep.getConsMessageSaveQueue());

		messageCount = list.size();
		if (messageCount > 0) {
			// 交易入库
			table = new NewTableCreate();
			BigInteger maxMsgId = table.addMessages(list, dep.getDbId());
			list.clear();
			// 更新入库共识消息总数
			if (maxMsgId.compareTo(BigInteger.ZERO) > 0) {
				dep.setConsMessageCount(maxMsgId);
				// RocksJavaUtil rocksJavaUtil = new RocksJavaUtil(dep.getDbId());
				INosql rocksJavaUtil = dep.getNosql();
				rocksJavaUtil.put(Config.CONS_MSG_COUNT_KEY, dep.getConsMessageCount().toString());

				// 打印日志
				long interval = Duration.between(t0, t1).toMillis();
//					logger.info(statisticInfo.toString(), node.getShardId(), node.getCreatorId(), interval,
//							messageCount,
//							new BigDecimal(interval).divide(BigDecimal.valueOf(messageCount), 2,
//									BigDecimal.ROUND_HALF_UP),
//							node.getConsMessageHandleQueue().size(), node.getConsMessageSaveQueue().size());
			} else {
//					logger.error("node-({}, {}): update consMessageCount failed after save messages!!!",
//							node.getShardId(), node.getCreatorId());
			}
		}
		// }
	}

	public void persistSystemMessages() {
		logger.info(">>> start up system messages persistence...");
		try {
			int i = 0;
			Instant t0 = Instant.now();
			// while (true) {
			if (!dep.getSystemAutoTxSaveQueue().isEmpty()) {
				// saveSystemAutoTx(dep.getSystemAutoTxSaveQueue().poll());
				saveSystemAutoTxes(QueuePoller.poll(dep.getSystemAutoTxSaveQueue()));
				i++;
			} else {
				// sleep(100);
				Instant t1 = Instant.now();
				if (i > 0) {
					// 交易入库
//					logger.info(statisticInfo.toString(), node.getShardId(), node.getCreatorId(),
//							Duration.between(t0, t1).toMillis(), i, node.getSystemAutoTxSaveQueue().size());
				}
				t0 = t1;
				i = 0;
			}
			// }
		} catch (Exception e) {
			logger.error("EventSaveThread error: {}\nexit...", e);
			System.exit(-1);
		}
	}

	private void saveSystemAutoTxes(List<JSONObject> sysAutoTxes) {
		for (JSONObject sysAutoTx : sysAutoTxes) {
			saveSystemAutoTx(sysAutoTx);
		}
	}

	private void saveSystemAutoTx(JSONObject sysAutoTx) {
		TransactionMsg msg = new TransactionMsg();

		if (sysAutoTx != null) {
			try {
				if (sysAutoTx.containsKey("id")) {
					msg.setId(sysAutoTx.getString("id"));
				}
				if (sysAutoTx.containsKey("type")) {
					msg.setType(sysAutoTx.getString("type"));
				}
				if (sysAutoTx.containsKey("mHash")) {
					msg.setmHash(sysAutoTx.getString("mHash"));
				}
				if (sysAutoTx.containsKey("fromAddress")) {
					msg.setFromAddress(sysAutoTx.getString("fromAddress"));
				}
				if (sysAutoTx.containsKey("toAddress")) {
					msg.setToAddress(sysAutoTx.getString("toAddress"));
				}
				if (sysAutoTx.containsKey("amount")) {
					msg.setAmount(sysAutoTx.getBigInteger("amount"));
				}
				if (sysAutoTx.containsKey("updateTime")) {
					msg.setUpdateTime(sysAutoTx.getLong("updateTime"));
				}
				MysqlHelper mysqlHelper = new MysqlHelper(dep.getDbId());
				String id = msg.getId();
				String type = msg.getType();
				String tableName = Config.SYSTEMAUTOTX + Config.SPLIT + "0";
				// RocksJavaUtil rocksJavaUtil = new RocksJavaUtil(dep.getDbId());
				INosql rocksJavaUtil = dep.getNosql();
				byte[] bytes = rocksJavaUtil.get(Config.SYSTEMAUTOTX);
				TransactionSplit split = null;
				if (bytes == null) {
					split = new TransactionSplit();
					split.setTableName(tableName);
					split.setTableIndex(BigInteger.ZERO);
					split.setTableNamePrefix(Config.SYSTEMAUTOTX);
					split.setTotal(1);
					rocksJavaUtil.put(Config.SYSTEMAUTOTX, JSONArray.toJSONString(split));
				} else {
					String json = new String(bytes);
					split = JSONArray.parseObject(json, TransactionSplit.class);
					if (Config.SYSTEM_SPIT_TOTAL <= split.getTotal()) {
						BigInteger tableIndex = split.getTableIndex().add(BigInteger.ONE);
						tableName = Config.SYSTEMAUTOTX + Config.SPLIT + tableIndex;
						split.setTableName(tableName);
						split.setTableIndex(tableIndex);
						split.setTableNamePrefix(Config.SYSTEMAUTOTX);
						split.setTotal(1);
						// 创建system_auto_tx表 1：交易产生的消息，如奖励 2：快照产生的信息 3:合约信息
						NewTableCreate.createTransactionsMsgTable(mysqlHelper, tableName);
					} else {
						tableName = split.getTableName();
						split.setTotal(split.getTotal() + 1);
					}
					rocksJavaUtil.put(Config.SYSTEMAUTOTX, JSONArray.toJSONString(split));
				}
				try {

					StringBuilder transSql = new StringBuilder("insert into  ");
					transSql.append(tableName);
					transSql.append(" (`id`,`fromAddress`,`type`,`mHash`,`toAddress`,`amount`,")
							.append("`updateTime`) values (?,?,?,?,?,?,?)");
					PreparedStatement preparedStatement = mysqlHelper.getPreparedStatement(transSql.toString());

					preparedStatement.setString(1, id);
					preparedStatement.setString(2, msg.getFromAddress());
					preparedStatement.setString(3, msg.getType());
					preparedStatement.setString(4, msg.getmHash());
					preparedStatement.setString(5, msg.getToAddress());
					preparedStatement.setBigDecimal(6, new BigDecimal(msg.getAmount()));
					preparedStatement.setLong(7, Instant.now().toEpochMilli());
					preparedStatement.executeUpdate();
				} catch (Exception ex) {
					logger.error(">>>>>>>saveSystemAutoTx error: {}", ex);
				} finally {
					mysqlHelper.destroyedPreparedStatement();
				}

				// 添加如rocksDB中
				rocksJavaUtil.put(type + id, JSONArray.toJSONString(sysAutoTx));
				rocksJavaUtil.put(Config.SYS_TX_COUNT_KEY, id);

				// system messages introspection via rocksdb
				// by Francis.Deng Sep,4th,2019
				rocksJavaUtil.put(MessageIndexes.getSysMessageTypeIdKey(type + id), new byte[0]);
			} catch (Exception ex) {
				logger.error(">>>>>>>saveSystemAutoTx error: {}", ex);
			}
		}
	}
}
