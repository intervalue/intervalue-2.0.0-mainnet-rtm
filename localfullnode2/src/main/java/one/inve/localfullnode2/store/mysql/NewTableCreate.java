package one.inve.localfullnode2.store.mysql;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

import one.inve.localfullnode2.conf.Config;
import one.inve.localfullnode2.store.rocks.Message;
import one.inve.localfullnode2.store.rocks.RocksJavaUtil;
import one.inve.localfullnode2.store.rocks.TableInfo;
import one.inve.localfullnode2.store.rocks.TransactionSplit;
import one.inve.localfullnode2.store.rocks.key.MessageIndexes;

/**
 * 创建新表
 * 
 * @author Allen
 */
public class NewTableCreate {
	private static final Logger logger = LoggerFactory.getLogger(NewTableCreate.class);

	/**
	 * 创建新表 transactions_msg 1：交易产生的消息，如奖励 2：快照产生的信息 3:合约信息
	 */
	public static void createTransactionsMsgTable(MysqlHelper msyqlHelper, String tableName) {
		try {
			StringBuilder sql = new StringBuilder("CREATE TABLE IF NOT EXISTS ");
			sql.append(tableName);
			sql.append(" (id decimal(65,0) ,fromAddress varchar(600),");
			sql.append("type   varchar(100),");
			sql.append("mHash   varchar(600),");
			sql.append("toAddress  varchar(600),");
			sql.append("amount   decimal(64,0),");
			sql.append("updateTime   bigint(20),");
			sql.append("PRIMARY KEY (`id`)");
			sql.append(")ENGINE=InnoDB DEFAULT CHARSET=utf8 ");

			msyqlHelper.executeUpdate(sql.toString());

		} catch (Exception ex) {
			logger.error("error: {}", ex);
		}
		logger.info("Table created successfully==============================");
	}

	/**
	 * 创建新表
	 * 
	 * @param tableName 表名
	 */
	public static void createMessagesTable(MysqlHelper msyqlHelper, String tableName) {
		try {
			StringBuilder sql = new StringBuilder("CREATE TABLE IF NOT EXISTS ");
			sql.append(tableName);
			sql.append("(id decimal(65,0) ,fromAddress varchar(600),");
			sql.append("toAddress  varchar(600),");
			sql.append("hash   varchar(600),");
			sql.append("type   varchar(100),");
			sql.append("eHash   varchar(600),");
			sql.append("isValid   bigint(20),");
			sql.append("updateTime   bigint(20),");
			sql.append("snapshot   varchar(200),");
			sql.append("PRIMARY KEY (`id`)");
			sql.append(")ENGINE=InnoDB DEFAULT CHARSET=utf8 ");

			msyqlHelper.executeUpdate(sql.toString());

		} catch (Exception ex) {
			logger.error("error: {}", ex);
		}
		logger.info("Table created successfully==============================");
	}

	/**
	 * 创建新表 Snapshotmessages 表名
	 */
	/*
	 * public static void createTableSnapshotmessages(MysqlHelper mysqlHelper) { try
	 * { StringBuilder sql = new
	 * StringBuilder("CREATE TABLE IF NOT EXISTS snapshotMessages (");
	 * sql.append("id bigint(20) NOT NULL AUTO_INCREMENT,PRIMARY KEY (`id`),");
	 * sql.append("snapVersion bigint(20),"); sql.append("snapHash varchar(3000),");
	 * sql.append("snapshotPoint LONGTEXT,"); sql.append("accounts LONGTEXT,");
	 * sql.append("pubkey varchar(3000),"); sql.append("signature TEXT,");
	 * sql.append("timestamp bigint(20), "); sql.append("preHash varchar(3000), ");
	 * sql.append("totalFee decimal(29), ");
	 * sql.append("nodeRewardRatio double(8,6),");
	 * sql.append("rewardTransactions TEXT ");
	 * sql.append(")ENGINE=InnoDB DEFAULT CHARSET=utf8  ");
	 * 
	 * mysqlHelper.executeUpdate(sql.toString());
	 * 
	 * } catch ( Exception ex ) { logger.error("error: {}", ex); } logger.
	 * info("Table createTableSnapshotmessages successfully=============================="
	 * ); }
	 */

	public static void createTableBlackWhiteNameInfo(MysqlHelper mysqlHelper) {
		try {
			StringBuilder sql = new StringBuilder("CREATE TABLE IF NOT EXISTS black_white_name_info (");
			sql.append("type  INTEGER,");
			sql.append("value TEXT");
			sql.append(")ENGINE=MyISAM DEFAULT CHARSET=utf8  ");

			mysqlHelper.executeUpdate(sql.toString());

		} catch (Exception ex) {
			logger.error("error: {}", ex);
		}
		logger.info("Table createTableBlackWhiteNameInfo successfully==============================");
	}

	/**
	 * 创建新表 tableNamePrefix 表名前缀 如:transactions tableName 表名 如:transactions_0 total
	 * 表中现在多少数量 不能超过TRANSACTIONS_SPIT_TOTAL tableIndex 当前分表到第几张表
	 */
	public static void createTableEvents(MysqlHelper mysqlHelper) {
		try {
			StringBuilder sql = new StringBuilder("CREATE TABLE IF NOT EXISTS events (");
			sql.append("shardId   smallint,");
			sql.append("creatorId   bigint(20),");
			sql.append("seq   bigint(20),");
			sql.append("selfId   bigint(20),");
			sql.append("selfSeq   bigint(20),");
			sql.append("otherId   bigint(20),");
			sql.append("otherSeq   bigint(20),");
			sql.append("transactions  LONGTEXT,");
			sql.append("timeCreatedSecond   bigint(20),");
			sql.append("timeCreatedNano   bigint(20),");
			sql.append("isFamous   tinyint(1),");
			sql.append("signature  varchar(3000),");
			sql.append("hash  varchar(600),");
			sql.append("generation   bigint(20),");
			sql.append("consensusTimestampSecond   bigint(20),");
			sql.append("consensusTimestampNano   bigint(20),");
			sql.append("transCount   bigint(20), ");
			sql.append("INDEX teTableEventsIndex (shardId,creatorId,seq), ");
			sql.append("INDEX consensusTimestampSecondIndex (consensusTimestampSecond), ");
			sql.append("INDEX consensusTimestampNanoIndex (consensusTimestampNano)");
			sql.append(")ENGINE=InnoDB DEFAULT CHARSET=utf8 ");

			mysqlHelper.executeUpdate(sql.toString());

		} catch (Exception ex) {
			logger.error("error: {}", ex);
		}
		logger.info("createTableEvents created successfully==============================");
	}

	/**
	 * 保存一组message
	 * 
	 * @param entityList 一组message
	 * @param dbId       数据库ID(Mysql-main_dbId, Rocksdb-dbId)
	 * @return 这组message的最大ID，异常时返回0
	 */
	public BigInteger addMessages(List<JSONObject> entityList, String dbId) {
		BigInteger maxMsgId = BigInteger.ZERO;
		MysqlHelper h = null;
		if (entityList == null || entityList.size() <= 0) {
			return maxMsgId;
		}
		Integer size = entityList.size();
		try {
			h = new MysqlHelper(dbId);
			TableInfo tableInfo = exitsTable(h, size, dbId);

			Integer total = (Config.MESSAGES_SPIT_TOTAL - tableInfo.getTotal());
			// 待新增的数据小于规定的数量，直接保存
			if (size <= total) {
				maxMsgId = this.batchInsert(h, entityList, tableInfo, dbId);
			} else {
				// 1:list分两部分保存，先填充第一张表
				List<JSONObject> firstList = entityList.subList(0, total);
				maxMsgId = this.batchInsert(h, firstList, tableInfo, dbId);

				// 保存到下一张表中
				List<JSONObject> secondList = entityList.subList(total, size);
				BigInteger tableIndex = BigInteger.ONE.add(tableInfo.getTableIndex());
				String tableName = Config.MESSAGES + Config.SPLIT + tableIndex;
				tableInfo.setTableIndex(tableIndex);
				tableInfo.setTableName(tableName);
				tableInfo.setTotal(0);
				// 建表 如果表创建了不会新增表
				createMessagesTable(h, tableName);
				maxMsgId = this.batchInsert(h, secondList, tableInfo, dbId);
			}

		} catch (Exception e) {
			logger.error("error: {}", e);
		} finally {
			if (h != null) {
				h.destroyedPreparedStatement();
			}
		}
		return maxMsgId;
	}

	public static void main(String[] args) {
		BigInteger i = new BigInteger("11111111111111111");
		System.out.println(JSONArray.toJSONString(i.divideAndRemainder(BigInteger.valueOf(1000000000000000000L))));
	}

	/**
	 * 批量保存一组message
	 * 
	 * @param mysqlHelper mysql连接
	 * @param entityList  一组message
	 * @param tableInfo   MySQL分表信息
	 * @param dbId        Rocksdb数据库
	 * @return 这组message的最大ID，异常时返回0
	 */
	public BigInteger batchInsert(MysqlHelper mysqlHelper, List<JSONObject> entityList, TableInfo tableInfo,
			String dbId) {
		PreparedStatement preparedStatement = null;

		BigInteger maxMsgId = BigInteger.ZERO;
		try {
			// 保存交易信息
			Integer size = entityList.size();
			StringBuilder msgSql = new StringBuilder("insert into `");
			msgSql.append(tableInfo.getTableName());
			msgSql.append("` (`id`,`fromAddress`,`toAddress`,`hash`,")
					.append("`eHash`,`isValid`,`updateTime`,`type`,`snapshot`) values (?,?,?,?,?,?,?,?,?)");

			preparedStatement = mysqlHelper.getPreparedStatement(msgSql.toString());
			List<Message> messageList = new ArrayList<>();
			for (JSONObject json : entityList) {
				String msg = null;
				if (json.containsKey("msg")) {
					msg = json.getString("msg");
				}
				Message message = JSONArray.parseObject(msg, Message.class);
				if (message == null) {
					message = new Message();
				}
				message.setMessage(msg);
				Boolean lastIdx = null;
				if (json.containsKey("lastIdx")) {
					lastIdx = json.getBoolean("lastIdx");
					message.setLastIdx(lastIdx);
				}
				Boolean stable = null;
				if (json.containsKey("isStable")) {
					stable = json.getBoolean("isStable");
					message.setStable(stable);
				}

				BigInteger id = null;
				if (json.containsKey("id")) {
					id = json.getBigInteger("id");
					message.setId(id + "");
					if (id.compareTo(maxMsgId) > 0) {
						maxMsgId = id;
					}
				} else {
					logger.error("batchInsert方法中id为空放弃保存");
					return BigInteger.ZERO;
				}
				String hash = null;
				if (json.containsKey("hash")) {
					hash = json.getString("hash");
					message.setHash(hash);
				} else {
					logger.error("batchInsert方法中hash为空放弃保存");
					return BigInteger.ZERO;
				}
				String fromAddress = null;
				if (json.containsKey("fromAddress")) {
					fromAddress = json.getString("fromAddress");
				}
				String toAddress = null;
				if (json.containsKey("toAddress")) {
					toAddress = json.getString("toAddress");
				}
				Boolean isValid = null;
				if (json.containsKey("isValid")) {
					isValid = json.getBoolean("isValid");
					message.setValid(isValid);
				} else {
					logger.error("batchInsert方法中isValid为空放弃保存");
					return BigInteger.ZERO;
				}
				Long updateTime = null;
				if (json.containsKey("updateTime")) {
					updateTime = json.getLong("updateTime");
					message.setUpdateTime(updateTime);
				}
				Integer type = null;
				if (json.containsKey("type")) {
					type = json.getInteger("type");
				}
				String eHash = null;
				if (json.containsKey("eHash")) {
					eHash = json.getString("eHash");
					message.seteHash(eHash);
				} else {
					logger.error("batchInsert方法中eHash为空放弃保存");
					return BigInteger.ZERO;
				}
				String snapshot = null;
				if (json.containsKey("snapVersion")) {
					snapshot = json.getString("snapVersion");
					message.setSnapVersion(snapshot);
				}
				int validState = 1;
				if (json.containsKey("validState")) {
					validState = json.getIntValue("validState");
				}
				// 保存transaction
				preparedStatement.setBigDecimal(1, new BigDecimal(id));
				preparedStatement.setString(2, fromAddress);
				preparedStatement.setString(3, toAddress);
				preparedStatement.setString(4, hash);
				preparedStatement.setString(5, eHash);
				preparedStatement.setInt(6, (isValid) ? 1 : 0);
				preparedStatement.setLong(7, updateTime);
				preparedStatement.setInt(8, type);
				preparedStatement.setString(9, snapshot);
				preparedStatement.addBatch();

				if (validState != 2) {
					messageList.add(message);
				}

			}
			Instant t2 = Instant.now();
//			logger.info("------ preparedStatement add data cost: {} ms ", Duration.between(t1, t2).toMillis());
			preparedStatement.executeBatch();
			closePreparedStatement(preparedStatement);
			mysqlHelper.destroyedPreparedStatement();
//			logger.info("------ preparedStatement execute cost: {} ms ", Duration.between(t2, Instant.now()).toMillis());

			// 交易保存到rocksDB
			RocksJavaUtil rocksJavaUtil = new RocksJavaUtil(dbId);
			addTransactionToRocksDB(rocksJavaUtil, messageList);

			// 保存分表信息
			TransactionSplit split = new TransactionSplit();
			split.setTableName(tableInfo.getTableName());
			split.setTableIndex(tableInfo.getTableIndex());
			split.setTableNamePrefix(Config.MESSAGES);
			split.setTotal(tableInfo.getTotal() + size);

			rocksJavaUtil.put(Config.MESSAGES, JSONArray.toJSONString(split));
		} catch (Exception ex) {
			logger.error("error: {}", ex);

			try {
				closePreparedStatement(preparedStatement);
				mysqlHelper.commitFail();
			} catch (Exception e) {
				logger.error("error: {}", e);
			}
			maxMsgId = batchInsert(mysqlHelper, entityList, tableInfo, dbId);
		}
		return maxMsgId;
	}

	/**
	 * 保存一组message到rocksDB
	 * 
	 * @param rocksJavaUtil rocksdb数据库连接
	 * @param entityList    一组message
	 */
	public void addTransactionToRocksDB(RocksJavaUtil rocksJavaUtil, List<Message> entityList) {
		try {
			for (Message msg : entityList) {
				rocksJavaUtil.put(msg.getHash(), JSON.toJSONString(msg));

				// message introspections via rocksdb
				// by Francis.Deng Sep,4th,2019
				rocksJavaUtil.put(MessageIndexes.getMessageHashKey(msg.getHash()).getBytes(), new byte[0]);
			}
		} catch (Exception ex) {
			logger.error("addTransactionToRocksDB交易保存到rocksDB报错", ex);
			addTransactionToRocksDB(rocksJavaUtil, entityList);
		}
	}

	/**
	 * 关闭连接
	 * 
	 * @param preparedStatement
	 */
	public void closePreparedStatement(PreparedStatement preparedStatement) {
		if (preparedStatement != null) {
			try {
				preparedStatement.close();
			} catch (SQLException ex) {
				logger.error("error: {}", ex);
			}
		}
	}

	/**
	 * 是否存在表transactions 1:如果不存在，新建表并返回新建表 2:如果存在，返回存在的表
	 */
	public TableInfo exitsTable(MysqlHelper h, Integer size, String dbId) {
		try {
			TableInfo tableInfo = new TableInfo();
			// 查找最大的TransactionSplit
			TransactionSplit split = QueryTableSplit.tableExist(dbId);

			Integer total = split.getTotal();
			// 超过表的最大额度
			if (total != null && total + size > Config.MESSAGES_SPIT_TOTAL) {
				BigInteger tableIndex = BigInteger.ONE.add(tableInfo.getTableIndex());
				String tableName = Config.MESSAGES + Config.SPLIT + tableIndex;

				// 建表
				if (null != tableIndex && tableIndex.compareTo(BigInteger.ZERO) >= 0) {
					createMessagesTable(h, tableName);
				}

				// 初始化下张表信息
				TransactionSplit transaSplit = new TransactionSplit();
				transaSplit.setTableName(tableName);
				transaSplit.setTableIndex(tableIndex);
				transaSplit.setTableNamePrefix(Config.MESSAGES);
				transaSplit.setTotal(0);
				new RocksJavaUtil(dbId).put(Config.MESSAGES, JSONArray.toJSONString(transaSplit));

				tableInfo.setTableIndex(split.getTableIndex());
				tableInfo.setTableNamePrefix(split.getTableNamePrefix());
				tableInfo.setTableName(split.getTableName());
				tableInfo.setTotal(total);
				return tableInfo;
			} else {
				tableInfo.setTableIndex(split.getTableIndex());
				tableInfo.setTableNamePrefix(split.getTableNamePrefix());
				tableInfo.setTableName(split.getTableName());
				tableInfo.setTotal(total);
				return tableInfo;
			}
		} catch (Exception e) {
			logger.error("error: {}", e);
			return null;
		}
	}
}
