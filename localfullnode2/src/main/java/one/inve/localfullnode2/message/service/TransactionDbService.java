package one.inve.localfullnode2.message.service;

import java.math.BigInteger;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

import one.inve.bean.message.SnapshotMessage;
import one.inve.bean.node.LocalFullNode;
import one.inve.core.EventBody;
import one.inve.localfullnode2.conf.Config;
import one.inve.localfullnode2.store.EventKeyPair;
import one.inve.localfullnode2.store.mysql.MysqlHelper;
import one.inve.localfullnode2.store.mysql.QueryTableSplit;
import one.inve.localfullnode2.store.rocks.Message;
import one.inve.localfullnode2.store.rocks.RocksJavaUtil;
import one.inve.localfullnode2.store.rocks.TransactionSplit;
import one.inve.utils.DSA;
import one.inve.utils.StringUtils;

/**
 * 交易数据库操作类
 * 
 */
public class TransactionDbService implements ITransactionDbService {
	private static final Logger logger = LoggerFactory.getLogger(TransactionDbService.class);

	private static Integer OFFSET = 5000;

	/**
	 * 将从seed获取得到的局部全节点列表入库，以备重启
	 * 
	 * @param localFullNodes 所有节点公钥
	 */
	public boolean saveLocalFullNodes2Database(List<LocalFullNode> localFullNodes, String dbId) {
		logger.info("saveLocalFullNodes2Database...");
		if (null == localFullNodes || localFullNodes.size() <= 0) {
			return false;
		}
		// 没有分片则创建
		createTableLocalfullnode(dbId);
		// 清空分片信息
		clearLocalFullNodes(dbId);

		Connection conn = null;
		Statement stmt = null;
		try {
			conn = new MysqlHelper(dbId).getConnection();
			StringBuilder sql = new StringBuilder();
			stmt = conn.createStatement();
			localFullNodes.forEach(n -> {
				sql.append(String.format(
						"insert into localfullnode (pubkey, shard, idx, address) values('%s', %s, '%s', '%s');",
						n.getPubkey(), n.getShard(), n.getIndex(), n.getAddress()));

			});
			stmt.executeUpdate(sql.toString());
			return true;
		} catch (Exception e) {
			logger.error("{}", e);
			return false;
		} finally {
			try {
				if (null != stmt) {
					stmt.close();
				}
			} catch (SQLException e) {
				logger.error("{}", e);
			}
			try {
				if (null != conn) {
					conn.close();
				}
			} catch (SQLException e) {
				logger.error("{}", e);
			}
		}
	}

	/**
	 * 没有分片则创建
	 *
	 */
	private static void createTableLocalfullnode(String dbId) {
		MysqlHelper helper = null;
		try {
			helper = new MysqlHelper(dbId);
			StringBuilder sql = new StringBuilder("CREATE TABLE IF NOT EXISTS  localfullnode (");
			sql.append("id bigint(20) NOT NULL AUTO_INCREMENT,");
			sql.append("pubkey  varchar(3000),");
			sql.append("shard  varchar(5),");
			sql.append("idx  varchar(10),");
			sql.append("address  varchar(32), ");
			sql.append("PRIMARY KEY (`id`)");
			sql.append(")ENGINE=InnoDB DEFAULT CHARSET=utf8");

			helper.executeUpdate(sql.toString());

		} catch (Exception ex) {
			logger.error("error: {}", ex);
		} finally {
			try {
				if (helper != null) {
					helper.destroyedPreparedStatement();
				}
			} catch (Exception ex) {
				logger.error("SqliteHelper().destroyedPreparedStatement()异常error: {}", ex);
			}
		}
		logger.info("createTableShards created successfully==============================");
	}

	/**
	 * 清空分片信息
	 */
	private static boolean clearLocalFullNodes(String dbId) {
		logger.info("clearShards...");
		Connection conn = null;
		Statement stmt = null;
		try {
			conn = new MysqlHelper(dbId).getConnection();
			String sql = "delete from localfullnode;";
			stmt = conn.createStatement();
			stmt.executeUpdate(sql);
			return true;
		} catch (Exception e) {
			logger.error("{}", e);
			return false;
		} finally {
			try {
				if (null != stmt) {
					stmt.close();
				}
			} catch (SQLException e) {
				logger.error("{}", e);
			}
			try {
				if (null != conn) {
					conn.close();
				}
			} catch (SQLException e) {
				logger.error("{}", e);
			}
		}
	}

	/**
	 * 从数据库查询shardInfo（主要是pubkeys）
	 * 
	 * @return shardInfo
	 */
	public byte[][][] queryPubkeysFromDatabase(String dbId) {
		logger.info("queryPubkeysFromDatabase...");
		byte[][][] pubkeys = null;
		Connection conn = null;
		Statement stmt = null;
		ResultSet rs = null;
		int rowCount = 0;
		try {
			conn = new MysqlHelper(dbId).getConnection();
			stmt = conn.createStatement();
			String sql = "select * from shards";
			rs = stmt.executeQuery(sql);
			while (rs.next()) {
				int shardCount = rs.getInt("shardCount");
				int nValue = rs.getInt("nvalue");
				if (null == pubkeys) {
					pubkeys = new byte[shardCount][nValue][];
				}
				int shard = rs.getInt("shard");
				int index = rs.getInt("idx");
				pubkeys[shard][index] = DSA.decryptBASE64(rs.getString("pubkey"));
				rowCount++;
			}
			logger.info("rowCount: {}", rowCount);
			return pubkeys;
		} catch (Exception e) {
			logger.error("{}", e);
			return pubkeys;
		} finally {
			try {
				if (null != rs) {
					rs.close();
				}
			} catch (SQLException e) {
				logger.error("{}", e);
			}
			try {
				if (null != stmt) {
					stmt.close();
				}
			} catch (SQLException e) {
				logger.error("{}", e);
			}
			try {
				if (null != conn) {
					conn.close();
				}
			} catch (SQLException e) {
				logger.error("{}", e);
			}
		}
	}

	/**
	 * 获取地址对应账户所有历史交易记录
	 * 
	 * @param address 收款地址
	 * @return 账户所有历史交易记录
	 */
	public ArrayList<Message> queryTransactionHistory(String address, String dbId) {
		ArrayList<Message> transactions = new ArrayList<>();
		Connection connection = null;
		Statement stmt = null;
		ResultSet rs = null;
		try {
			connection = new MysqlHelper(dbId).getConnection();
			stmt = connection.createStatement();

			BigInteger num = BigInteger.ZERO;
			TransactionSplit split = QueryTableSplit.tableExist(dbId);
			if (split != null) {
				num = split.getTableIndex().add(BigInteger.ONE);
			}

			for (BigInteger i = BigInteger.ZERO; i.compareTo(num) < 0; i = i.add(BigInteger.ONE)) {
				StringBuilder tsql = new StringBuilder("select hash");
				tsql.append(" from ").append(Config.MESSAGES).append(Config.SPLIT)
						.append("_%s where fromAddress='%s' or toAddress='%s'");
				String sql = String.format(tsql.toString(), i, address, address);
				rs = stmt.executeQuery(sql);
				while (rs.next()) {
					String hash = rs.getString("hash");
					RocksJavaUtil rocksJavaUtil = new RocksJavaUtil(dbId);
					byte[] transationByte = rocksJavaUtil.get(hash);
					if (transationByte != null) {
						transactions.add(JSONArray.parseObject(transationByte, Message.class));
					}
				}
			}
			return transactions;
		} catch (SQLException e) {
			logger.error("{}", e);
			return transactions;
		} finally {
			try {
				if (null != rs) {
					rs.close();
				}
			} catch (SQLException e) {
				logger.error("{}", e);
			}
			try {
				if (null != stmt) {
					stmt.close();
				}
			} catch (SQLException e) {
				logger.error("{}", e);
			}
			try {
				if (null != connection) {
					connection.close();
				}
			} catch (SQLException e) {
				logger.error("{}", e);
			}
		}
	}

	public List<JSONObject> queryMissingTransactionsBeforeSnapshotPoint(String message,
			BigInteger requestConsMessageMaxId, String dbId) {
		BigInteger selfTranId = BigInteger.ZERO;
		if (StringUtils.isEmpty(message)) {
			SnapshotMessage sm = JSONObject.parseObject(message, SnapshotMessage.class);
			long shardId = sm.getSnapshotPoint().getEventBody().getShardId();
			long creatorId = sm.getSnapshotPoint().getEventBody().getCreatorId();
			long creatorSeq = sm.getSnapshotPoint().getEventBody().getCreatorSeq();
			// 查询快照点事件对应的最大交易Id
			selfTranId = queryTransactionIdByEvent(shardId, creatorId, creatorSeq, dbId);
		}
		if (selfTranId.compareTo(requestConsMessageMaxId) <= 0) {
			return null;
		}

		// 根据范围查询交易记录
		return queryTransactionsByRange(requestConsMessageMaxId, selfTranId, dbId);
	}

	/**
	 * 根据范围查询交易记录
	 * 
	 * @param firstTranId 开始交易ID
	 * @param lastTranId  结束交易ID
	 * @return 交易记录列表
	 */
	public List<JSONObject> queryTransactionsByRange(BigInteger firstTranId, BigInteger lastTranId, String dbId) {
		if (firstTranId.compareTo(lastTranId) > 0) {
			return null;
		}
		Connection con = null;
		Statement stmt = null;
		ResultSet rs = null;
		List<JSONObject> transactions = new ArrayList<>();
		try {
			con = new MysqlHelper(dbId).getConnection();
			stmt = con.createStatement();
			// 不含firstTranId
			BigInteger firstTableId = (firstTranId.compareTo(BigInteger.ZERO) <= 0) ? BigInteger.ZERO
					: firstTranId.divide(BigInteger.valueOf(Config.MESSAGES_SPIT_TOTAL));
			BigInteger lastTableId = (lastTranId.compareTo(BigInteger.ZERO) <= 0) ? BigInteger.ZERO
					: firstTranId.subtract(BigInteger.ONE).divide(BigInteger.valueOf(Config.MESSAGES_SPIT_TOTAL));

			String tablePrx = Config.MESSAGES + Config.SPLIT;
			String sql = null;
			for (BigInteger i = firstTableId; i.compareTo(lastTableId) <= 0; i = i.add(BigInteger.ONE)) {
				sql = String.format("select hash from %s where id>'%d' and id<='%d';", tablePrx + i, firstTranId,
						lastTranId);
				rs = stmt.executeQuery(sql);
				while (rs.next()) {
					String hash = rs.getString("hash");
					RocksJavaUtil rocksJavaUtil = new RocksJavaUtil(dbId);
					byte[] transationByte = rocksJavaUtil.get(hash);
					if (transationByte != null) {
						transactions.add(JSONObject.parseObject(new String(transationByte)));
					}
				}
			}
			return transactions;
		} catch (Exception e) {
			logger.error("error: {}", e);
			return queryTransactionsByRange(firstTranId, lastTranId, dbId);
		} finally {
			try {
				if (null != rs) {
					rs.close();
				}
			} catch (SQLException e) {
				logger.error("error: {}", e);
			}
			try {
				if (null != stmt) {
					stmt.close();
				}
			} catch (SQLException e) {
				logger.error("error: {}", e);
			}
			try {
				if (null != con) {
					con.close();
				}
			} catch (SQLException e) {
				logger.error("error: {}", e);
			}
		}
	}

	/**
	 * 根据范围查询交易记录
	 * 
	 * @param firstTranId 开始交易ID
	 * @param lastTranId  结束交易ID
	 * @return 交易记录列表
	 */
	public List<String> queryMessageHashByRange(BigInteger firstTranId, BigInteger lastTranId, String dbId) {
		if (firstTranId.compareTo(lastTranId) > 0) {
			return null;
		}
		Connection con = null;
		Statement stmt = null;
		ResultSet rs = null;
		List<String> hashs = new ArrayList<>();
		try {
			con = new MysqlHelper(dbId).getConnection();
			stmt = con.createStatement();
			// 不含firstTranId
			BigInteger firstTableId = (firstTranId.compareTo(BigInteger.ZERO) <= 0) ? BigInteger.ZERO
					: firstTranId.divide(BigInteger.valueOf(Config.MESSAGES_SPIT_TOTAL));
			BigInteger lastTableId = (lastTranId.compareTo(BigInteger.ZERO) <= 0) ? BigInteger.ZERO
					: firstTranId.subtract(BigInteger.ONE).divide(BigInteger.valueOf(Config.MESSAGES_SPIT_TOTAL));

			String tablePrx = Config.MESSAGES + Config.SPLIT;
			String sql = null;
			for (BigInteger i = firstTableId; i.compareTo(lastTableId) <= 0; i = i.add(BigInteger.ONE)) {
				sql = String.format("select hash from %s where id>'%d' and id<='%d';", tablePrx + i, firstTranId,
						lastTranId);
				rs = stmt.executeQuery(sql);
				while (rs.next()) {
					String hash = rs.getString("hash");
					hashs.add(hash);
				}
			}
			return hashs;
		} catch (Exception e) {
			logger.error("error: {}", e);
			return queryMessageHashByRange(firstTranId, lastTranId, dbId);
		} finally {
			try {
				if (null != rs) {
					rs.close();
				}
			} catch (SQLException e) {
				logger.error("error: {}", e);
			}
			try {
				if (null != stmt) {
					stmt.close();
				}
			} catch (SQLException e) {
				logger.error("error: {}", e);
			}
			try {
				if (null != con) {
					con.close();
				}
			} catch (SQLException e) {
				logger.error("error: {}", e);
			}
		}
	}

	/**
	 * 根据Event查询事件包含的最大交易Id
	 * 
	 * @param shardId    Event对应的分片号
	 * @param creatorId  Event对应的节点Id
	 * @param creatorSeq Event对应的节点上的event序号
	 * @return 事件包含的最大交易Id
	 */
	private static BigInteger queryTransactionIdByEvent(long shardId, long creatorId, long creatorSeq, String dbId) {
		BigInteger transIndex = BigInteger.ZERO;
		EventKeyPair pair = new EventKeyPair((int) shardId, creatorId, creatorSeq);
		RocksJavaUtil rocksJavaUtil = new RocksJavaUtil(dbId);
		byte[] event = rocksJavaUtil.get(pair.toString());
		if (event != null) {
			EventBody eb = JSONObject.parseObject(event, EventBody.class);
			if (eb.getTransCount() != null) {
				transIndex = eb.getTransCount();
			}
		}
		return transIndex;
	}

}
