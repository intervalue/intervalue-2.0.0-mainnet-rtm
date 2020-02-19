package one.inve.localfullnode2.store;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

import one.inve.cfg.core.DBConnectionDescriptorsConf;
import one.inve.cfg.localfullnode.Config;
import one.inve.contract.MVM.WorldStateService;
import one.inve.core.EventBody;
import one.inve.localfullnode2.dep.DepItemsManager;
import one.inve.localfullnode2.nodes.LocalFullNode1GeneralNode;
import one.inve.localfullnode2.store.mysql.MysqlHelper;
import one.inve.localfullnode2.store.mysql.NewTableCreate;
import one.inve.localfullnode2.store.rocks.Message;
import one.inve.localfullnode2.store.rocks.RocksJavaUtil;
import one.inve.localfullnode2.store.rocks.TransactionSplit;
import one.inve.localfullnode2.utilities.StringUtils;
import one.inve.localfullnode2.utilities.TxVerifyUtils;

/**
 * 数据库操作工具包
 * 
 * @author Clare
 * @date 2018/7/25 0025.
 */
public class DbUtils {
	private static final Logger logger = LoggerFactory.getLogger(DbUtils.class);

	/**
	 * 查询events总数
	 * 
	 * @return events总数
	 */
	public static BigInteger getEventCountFromDatabase(String dbId) {
		RocksJavaUtil rocksJavaUtil = new RocksJavaUtil(dbId);
		byte[] rs = rocksJavaUtil.get(Config.EVT_COUNT_KEY);
		return null == rs ? BigInteger.ZERO : new BigInteger(rs);
	}

	public static List<EventBody> queryEventAfterSeq(int shardId, long creatorId, long creatorSeq,
			LocalFullNode1GeneralNode node) {
		try {
			int selfId = (int) node.getCreatorId();
			RocksJavaUtil rocksJavaUtil = new RocksJavaUtil(node.nodeParameters().dbId);

			long lastSeq = node.getEventStore().getLastSeq(shardId, creatorId);
			logger.info("query node-({},{}) events which seq between {} and {} ...", shardId, creatorId, creatorSeq,
					lastSeq);
			List<EventBody> list = new ArrayList<>();
			for (long i = creatorSeq; i <= lastSeq; i++) {
				EventKeyPair pair = new EventKeyPair(shardId, creatorId, i);
				byte[] evt = rocksJavaUtil.get(pair.toString());
				if (null != evt) {
					list.add(JSONObject.parseObject(evt, EventBody.class));
				}
			}

//        MysqlHelper h = null;
//        try {
//            h = new MysqlHelper(selfId);
//            String sql = String.format("SELECT e.creatorId, e.seq, e.otherId, e.otherSeq, " +
//                    " e.transactions, e.timeCreatedSecond, e.timeCreatedNano, e.signature, e.hash, e.generation, " +
//                    " e.isFamous, e.consensusTimestampSecond, e.consensusTimestampNano " +
//                    " FROM events e "+
//                    " where e.shardId = '%d' and e.creatorId = '%d' and e.seq>= '%d' ", shardId,creatorId,seq);
//            List<EventBody> list = h.executeQuery(sql, (rs, index) -> {
//                long creatorSeq = rs.getLong("seq");
//                long otherId = rs.getLong("otherId");
//                long otherSeq = rs.getLong("otherSeq");
//                long generation = rs.getLong("generation");
//                boolean isFamous = (rs.getInt("isFamous") == 1);
//                byte[] signature = Base64.getDecoder().decode(rs.getString("signature"));
//                byte[] hash = Base64.getDecoder().decode(rs.getString("hash"));
//
//                byte[][] transactions = null;
//                if (!StringUtils.isEmpty(rs.getString("transactions"))) {
//                    String[] trans = rs.getString("transactions").split(",");
//                    transactions = new byte[trans.length][];
//                    for (int i = 0; i < trans.length; i++) {
//                        transactions[i] = Base64.getDecoder().decode(trans[i]);
//                    }
//                }
//                Instant timeCreated = null;
//                if (rs.getLong("timeCreatedSecond")>0) {
//                    timeCreated = Instant.ofEpochSecond(rs.getLong("timeCreatedSecond"),
//                            rs.getLong("timeCreatedNano"));
//                }
//                Instant consensusTimestamp = null;
//                if (rs.getLong("consensusTimestampSecond")>0) {
//                    consensusTimestamp = Instant.ofEpochSecond(rs.getLong("consensusTimestampSecond"),
//                            rs.getLong("consensusTimestampNano"));
//                }
//                return new EventBody.Builder().shardId(shardId)
//                        .creatorId(creatorId).creatorSeq(creatorSeq).otherId(otherId).otherSeq(otherSeq)
//                        .timeCreated(timeCreated).messages(transactions).signature(signature).hash(hash)
//                        .generation(generation).isFamous(isFamous).consTimestamp(consensusTimestamp)
//                        .otherHash(null).parentHash(null)
//                        .build();
//            });
			return list;
		} catch (Exception e) {
			logger.error(">>>>>> getAllEvent4DB() ERROR: ", e);
			return queryEventAfterSeq(shardId, creatorId, creatorSeq, node);
		} finally {
//            if(h!=null) {
//                h.destroyed();
//            }
		}
	}

	/**
	 * 根据属性删除event
	 * 
	 * @param shardId    片号
	 * @param creatorId  节点索引
	 * @param creatorSeq 节点event seq
	 * @return eventbody
	 */
	public static boolean splitDelEvent(int shardId, long creatorId, long creatorSeq, LocalFullNode1GeneralNode node) {
//        MysqlHelper h = null;
		try {
			int selfId = (int) node.getCreatorId();
			RocksJavaUtil rocksJavaUtil = new RocksJavaUtil(node.nodeParameters().dbId);

			long lastSeq = node.getEventStore().getLastSeq(shardId, creatorId);
			logger.info("Delete node-({},{}) events which seq between {} and {} ...", shardId, creatorId, creatorSeq,
					lastSeq);
			for (long i = creatorSeq; i <= lastSeq; i++) {
				EventKeyPair pair = new EventKeyPair(shardId, creatorId, i);
				byte[] evt = rocksJavaUtil.get(pair.toString());
				if (null != evt) {
					rocksJavaUtil.delete(pair.toString());
				}
			}
			return true;

//            h = new MysqlHelper(selfId);
//            Statement stmt = h.getStatement();
//            String sql = String.format("DELETE from events where shardId = '%d' and creatorId = '%d' and seq >= '%d'",
//                    shardId, creatorId, creatorSeq);
//            return stmt.executeUpdate(sql) > 0;
		} catch (Exception e) {
			logger.error(">>>>>> delEvent() ERROR: ", e);
			throw new Error(e);
		} finally {
//            if (null != h) {
//                h.destroyed();
//            }
		}
	}

	/**
	 * 根据otherParent查询event
	 * 
	 * @param shardId  片号
	 * @param otherId  other节点索引
	 * @param otherSeq other节点event seq
	 * @return eventbody
	 */
	public static List<EventBody> getEventFromOther(int shardId, long otherId, long otherSeq,
			LocalFullNode1GeneralNode node) {
//        MysqlHelper h = null;
		try {
			List<EventBody> list = new ArrayList<>();
			int selfId = (int) node.getCreatorId();
			final RocksJavaUtil rocksJavaUtil = new RocksJavaUtil(node.nodeParameters().dbId);
			EventKeyPair pair = new EventKeyPair(shardId, otherId, otherSeq);
			byte[] value = rocksJavaUtil.get("o_" + pair.toString());
			if (null != value) {
				String data = new String(value);
				String[] eventKeys = data.split(",");
				Arrays.stream(eventKeys).forEach(key -> {
					byte[] valueE = rocksJavaUtil.get(key);
					if (null != valueE) {
						list.add(JSONObject.parseObject(new String(valueE), EventBody.class));
					}
				});
				logger.info("All descendants of event-({},{},{}) size: {}", shardId, otherId, otherSeq, list.size());
			}

//            h = new MysqlHelper(selfId);
//            String sql = String.format("SELECT e.creatorId, e.seq, e.selfId, e.selfSeq, e.otherId, e.otherSeq, " +
//                    " e.transactions, e.timeCreatedSecond, e.timeCreatedNano, e.signature, e.hash, e.generation, " +
//                    "  ec.isFamous, ec.consensusTimestampSecond, ec.consensusTimestampNano " +
//                    " FROM events e LEFT JOIN event_consensus ec on ec.shardId=e.shardId and ec.creatorId=e.creatorId and ec.seq=e.seq " +
//                    " where e.shardId = '%d' and e.otherId = '%d' and e.otherSeq = '%d'", shardId, otherId, otherSeq);
//            logger.info(sql);
//            List<EventBody> list = h.executeQuery(sql, (rs, index) -> {
//                long creatorId = rs.getLong("creatorId");
//                long creatorSeq = rs.getLong("seq");
//                long generation = rs.getLong("generation");
//                boolean isFamous = (rs.getInt("isFamous") == 1);
//                byte[] signature = Base64.getDecoder().decode(rs.getString("signature"));
//                byte[] hash = Base64.getDecoder().decode(rs.getString("hash"));
//
//                byte[][] transactions = null;
//                if (StringUtils.isNotEmpty(rs.getString("transactions"))) {
//                    String[] trans = rs.getString("transactions").split(",");
//                    transactions = new byte[trans.length][];
//                    for (int i = 0; i < trans.length; i++) {
//                        transactions[i] = Base64.getDecoder().decode(trans[i]);
//                    }
//                }
//                Instant timeCreated = null;
//                if (rs.getLong("timeCreatedSecond")>0) {
//                    timeCreated = Instant.ofEpochSecond(rs.getLong("timeCreatedSecond"),
//                            rs.getLong("timeCreatedNano"));
//                }
//                Instant consensusTimestamp = null;
//                if (rs.getLong("consensusTimestampSecond")>0) {
//                    consensusTimestamp = Instant.ofEpochSecond(rs.getLong("consensusTimestampSecond"),
//                            rs.getLong("consensusTimestampNano"));
//                }
//
//                return new EventBody.Builder().shardId(shardId)
//                        .creatorId(creatorId).creatorSeq(creatorSeq).otherId(otherId).otherSeq(otherSeq)
//                        .timeCreated(timeCreated).messages(transactions).signature(signature).hash(hash)
//                        .generation(generation).isFamous(isFamous).consTimestamp(consensusTimestamp)
//                        .otherHash(null).parentHash(null)
//                        .build();
//            });

			return list;
		} catch (Exception e) {
			logger.error(">>>>>> getEventFromOther() ERROR: ", e);
			throw new Error(e);
		} finally {
//            if(h!=null) {
//                h.destroyed();
//            }
		}
	}

	/**
	 * 查询白名单列表或者黑名单列表
	 * 
	 * @param dbId 节点ID
	 * @param type 类型
	 * @return 列表
	 */
	public static List<String> getBlackOrWhiteList(String dbId, int type) {
		MysqlHelper h = null;
		String sql = String.format("select * from black_white_name_info where type = '%d' ", type);
		try {
			h = new MysqlHelper(dbId);
			return h.executeQuery(sql, (rs, index) -> rs.getString("value"));
		} catch (SQLException e) {
			e.printStackTrace();
			return null;
		} finally {
			if (h != null) {
				h.destroyed();
			}
		}
	}

	/**
	 * 写入黑名单
	 * 
	 * @param pubKey 公钥
	 * @param type   类型
	 * @param dbId   节点ID
	 */
	public static void writeBlackInDb(String pubKey, int type, String dbId) {
		MysqlHelper h = null;
		try {
			h = new MysqlHelper(dbId);
			Statement stmt = h.getStatement();
			String sql = String.format("insert into black_white_name_info(type,value) values('%d','%s') ", type,
					pubKey);
			stmt.executeUpdate(sql);
		} catch (Exception e) {
			logger.error(">>>>> writeBlackInDb() error: ", e);
		} finally {
			if (h != null) {
				h.destroyed();
			}
		}
	}

	/**
	 * genesis:create sql tables,fill it with creation transaction,white list
	 */
	public static void initializeGenesis(LocalFullNode1GeneralNode node,
			DBConnectionDescriptorsConf dbConnectionDescriptorsConf, boolean isTankFullofWater) {
		MysqlHelper mysqlHelper = new MysqlHelper(node.nodeParameters().dbId, dbConnectionDescriptorsConf,
				node.nodeParameters().clearDb == 1);
		try {
			// 建消息表
			NewTableCreate.createMessagesTable(mysqlHelper, Config.MESSAGES + "_0");
			/* NewTableCreate.createTableSplit(sqliteHelper); */
			// 初始化水龙头
			// 如果已经初始化，就不需要再次初始化
			Long count = mysqlHelper.executeQuery("select count(id) as count from " + Config.MESSAGES + "_0 ", rs -> {
				Long count1 = 0L;
				try {
					if (rs.next()) {
						count1 = rs.getLong("count");
					}
				} catch (Exception ex) {
					logger.error("error: {}", ex);
				}
				return count1;
			});
			if (count < 1 && isTankFullofWater) {
				// 初始化十个水龙头
				initCreationTx(node);

				printInfo();
			}
			// 建黑白名单表
			NewTableCreate.createTableBlackWhiteNameInfo(mysqlHelper);
			// 建系统消息表
			NewTableCreate.createTransactionsMsgTable(mysqlHelper, Config.SYSTEMAUTOTX + Config.SPLIT + "0");
		} catch (Exception ex) {
			logger.error("createTableSplit异常{}", ex);
		} finally {
			mysqlHelper.destroyedPreparedStatement();
		}

	}

	/**
	 * 初始化mysql,which is deprecated and replaced by {@code initializeGenesis}
	 */
	@Deprecated
	public static void initDataBase(LocalFullNode1GeneralNode node,
			DBConnectionDescriptorsConf dbConnectionDescriptorsConf) {
		initializeGenesis(node, dbConnectionDescriptorsConf, true);
//		MysqlHelper mysqlHelper = new MysqlHelper(node.nodeParameters().dbId, dbConnectionDescriptorsConf,
//				node.nodeParameters().clearDb == 1);
//		try {
//			// 建消息表
//			NewTableCreate.createMessagesTable(mysqlHelper, Config.MESSAGES + "_0");
//			/* NewTableCreate.createTableSplit(sqliteHelper); */
//			// 初始化水龙头
//			// 如果已经初始化，就不需要再次初始化
//			Long count = mysqlHelper.executeQuery("select count(id) as count from " + Config.MESSAGES + "_0 ", rs -> {
//				Long count1 = 0L;
//				try {
//					if (rs.next()) {
//						count1 = rs.getLong("count");
//					}
//				} catch (Exception ex) {
//					logger.error("error: {}", ex);
//				}
//				return count1;
//			});
//			if (count < 1) {
//				// 初始化十个水龙头
//				initCreationTx(node);
//
//				printInfo();
//			}
//			// 建黑白名单表
//			NewTableCreate.createTableBlackWhiteNameInfo(mysqlHelper);
//			// 建系统消息表
//			NewTableCreate.createTransactionsMsgTable(mysqlHelper, Config.SYSTEMAUTOTX + Config.SPLIT + "0");
//		} catch (Exception ex) {
//			logger.error("createTableSplit异常{}", ex);
//		} finally {
//			mysqlHelper.destroyedPreparedStatement();
//		}

	}

	private static void printInfo() {
		logger.info(
				"\n****************************************************************************************************\n\n"
						+ "\n"
						+ "     mNNNNN-    hNNNNNNNNh          yNNNNN`  yNNNNNd              yNNNNN-  hNNNNNNNNNNNNNNNNNN+ \n"
						+ "     NMMMMM:    dMMMMMMMMMh         yMMMMM`  .NMMMMMo            /MMMMMs   hMMMMMMMMMMMMMMMMMM+ \n"
						+ "     NMMMMM:    dMMMMNdMMMMy        yMMMMM`   :MMMMMM-          .NMMMMd    hMMMMMy++++++++++++- \n"
						+ "     NMMMMM:    dMMMMN`dMMMMy       yMMMMM`    oMMMMMd          hMMMMN.    hMMMMM+ \n"
						+ "     NMMMMM:    dMMMMN `mMMMMy      yMMMMM`     dMMMMMo        /MMMMM:     hMMMMM+ \n"
						+ "     NMMMMM:    dMMMMN  .mMMMMy     yMMMMM`     .NMMMMM-      .NMMMMs      hMMMMMs:::::::::::. \n"
						+ "     NMMMMM:    dMMMMN   .NMMMMs    yMMMMM`      :MMMMMd      hMMMMd       hMMMMMMMMMMMMMMMMMo \n"
						+ "     NMMMMM:    dMMMMN    .NMMMMs   yMMMMM`       sMMMMMo    /MMMMN.       hMMMMMMMMMMMMMMMMMo \n"
						+ "     NMMMMM:    dMMMMN     -NMMMMs  yMMMMM`        dMMMMM-  .NMMMM:        hMMMMMo```````````` \n"
						+ "     NMMMMM:    dMMMMN      -NMMMMo yMMMMM`        .NMMMMd  hMMMMs         hMMMMM+ \n"
						+ "     NMMMMM:    dMMMMN       :MMMMMoyMMMMM`         :MMMMMo/MMMMd          hMMMMM+ \n"
						+ "     NMMMMM:    dMMMMN        :MMMMMNMMMMM`          sMMMMMMMMMN.          hMMMMMo------------. \n"
						+ "     NMMMMM:    dMMMMN         /MMMMMMMMMM`           dMMMMMMMM:           hMMMMMMMMMMMMMMMMMMN \n"
						+ "     NMMMMM:    dMMMMN          /MMMMMMMMM`           .NMMMMMMs            hMMMMMMMMMMMMMMMMMMN \n"
						+ "     ......`    ......           .........             `......             .................... \n"
						+ "\n" + "\n" + " Congratulation! InterValue mainnet has been launched!\n"
						+ " 10,000,000,000 INVEs have been created and issued successfully!\n" + "\n"
						+ "****************************************************************************************************\n");
	}

	public static void initBlackList(LocalFullNode1GeneralNode node) {
		node.setWhiteList(DbUtils.getBlackOrWhiteList(node.nodeParameters().dbId, 0));
		node.setBlackList(DbUtils.getBlackOrWhiteList(node.nodeParameters().dbId, 1));
		node.setBlackList4PubKey(DbUtils.getBlackOrWhiteList(node.nodeParameters().dbId, 2));
	}

	/**
	 * 检测Event相关数据一致性并修复
	 * 
	 * @param node
	 */
	public static void detectAndRepairEventData(LocalFullNode1GeneralNode node) {
		RocksJavaUtil rocksJavaUtil = new RocksJavaUtil(node.nodeParameters().dbId);
		// 先通过key获取rocksdb中存储的每根柱子的lastSeq，
		// 然后获取并计算每个柱子上最后一个Event的lastSeq，
		// 最后进行比较，以通过Event计算得到的为准
		BigInteger[][] realSeqs = new BigInteger[node.getShardCount()][node.getnValue()];
		BigInteger[][] seqs = new BigInteger[node.getShardCount()][node.getnValue()];
		for (int i = 0; i < node.getShardCount(); i++) {
			for (int j = 0; j < node.getnValue(); j++) {
				// key直接获取
				byte[] lastSeqByte = rocksJavaUtil.get(i + "_" + j);

				if (null != lastSeqByte && lastSeqByte.length >= 0) {
					seqs[i][j] = new BigInteger(new String(lastSeqByte));
					BigInteger lastSeq = seqs[i][j];

					EventKeyPair pair = new EventKeyPair(i, j, lastSeq.longValue());
					byte[] ebByte = rocksJavaUtil.get(pair.toString());
					if (null == ebByte || ebByte.length <= 0) {
						// 向前获取存在的Event
						while (null == ebByte || ebByte.length <= 0) {
							lastSeq = lastSeq.subtract(BigInteger.ONE);
							if (lastSeq.equals(BigInteger.ZERO)) {
								break;
							}
							pair = new EventKeyPair(i, j, lastSeq.longValue());
							ebByte = rocksJavaUtil.get(pair.toString());
						}
					} else {
						// 向后获取更新的Event
						while (null != ebByte && ebByte.length > 0) {
							lastSeq = lastSeq.add(BigInteger.ONE);

							pair = new EventKeyPair(i, j, lastSeq.longValue());
							ebByte = rocksJavaUtil.get(pair.toString());
						}
						lastSeq = lastSeq.subtract(BigInteger.ONE);
					}
					// 修复柱子-(i,j)上的lastSeq
					if (!seqs[i][j].equals(lastSeq)) {
						realSeqs[i][j] = lastSeq;
						logger.warn("node-({}, {}): ({}, {}) lastSeq diff: db-{}, calcu-{}", node.getShardId(),
								node.getCreatorId(), i, j, seqs[i][j], lastSeq);
						if (seqs[i][j].compareTo(lastSeq) < 0) {
							logger.warn("node-({}, {}): diff events as follow: ");
							for (BigInteger k = seqs[i][j].add(BigInteger.ONE); k.compareTo(lastSeq) < 0; k = k
									.add(BigInteger.ONE)) {
								EventKeyPair pair1 = new EventKeyPair(i, j, k.longValue());
								byte[] ebByte1 = rocksJavaUtil.get(pair1.toString());
								if (null != ebByte1 && ebByte1.length > 0) {
									logger.warn("node-({}, {}): {} = \n{}", pair1.toString(), new String(ebByte1));
								} else {
									logger.error("node-({}, {}): False alarm: {}", pair1.toString());
								}
							}
						} else {
							logger.warn("node-({}, {}): diff events as follow: ");
							for (BigInteger k = lastSeq.add(BigInteger.ONE); k.compareTo(seqs[i][j]) < 0; k = k
									.add(BigInteger.ONE)) {
								EventKeyPair pair1 = new EventKeyPair(i, j, k.longValue());
								byte[] ebByte1 = rocksJavaUtil.get(pair1.toString());
								if (null != ebByte1 && ebByte1.length > 0) {
									logger.warn("node-({}, {}): {} = \n{}", pair1.toString(), new String(ebByte1));
								} else {
									logger.error("node-({}, {}): False alarm: {}", pair1.toString());
								}
							}
						}
					} else {
						realSeqs[i][j] = seqs[i][j];
					}
				} else {
					logger.warn("node-({}, {}): event-({}, {}, 0) not exist!!!", node.getShardId(), node.getCreatorId(),
							i, j);
					realSeqs[i][j] = BigInteger.valueOf(-1);
					seqs[i][j] = BigInteger.valueOf(-1);
				}
			}
		}

		BigInteger realTotalEventCount = BigInteger.ZERO;
		for (int i = 0; i < node.getShardCount(); i++) {
			for (int j = 0; j < node.getnValue(); j++) {
				if (!realSeqs[i][j].equals(seqs[i][j])) {
					rocksJavaUtil.put(i + "_" + j, realSeqs[i][j].toString());
					logger.warn("node-({}, {}): ({}, {}) lastSeq {}", node.getShardId(), node.getCreatorId(), i, j,
							realSeqs[i][j]);
				}
				realTotalEventCount = realTotalEventCount.add(realSeqs[i][j].add(BigInteger.ONE));
			}
		}
		logger.warn("node-({}, {}): lastseqs = {}", node.getShardId(), node.getCreatorId(),
				JSONArray.toJSONString(realSeqs));

		// totalEventCount 比较并修复
		byte[] totalEventCountByte = rocksJavaUtil.get(Config.EVT_COUNT_KEY);
		BigInteger totalEventCount = (null == totalEventCountByte) ? BigInteger.ZERO
				: new BigInteger(new String(totalEventCountByte));
		if (!realTotalEventCount.equals(totalEventCount)) {
			logger.warn("node-({}, {}): total event count diff: db-{}, calcu-{}", node.getShardId(),
					node.getCreatorId(), totalEventCount, realTotalEventCount);
			rocksJavaUtil.put(Config.EVT_COUNT_KEY, realTotalEventCount.toString());
		}

		// o_pair如何修复？
	}

//    /**
//     * 检测共识Event相关数据一致性并修复
//     * @param node
//     */
//    public static void detectAndRepairConsEventData(Main node) {
//        RocksJavaUtil rocksJavaUtil = new RocksJavaUtil(node.nodeParameters.dbId);
//        // transCount
//        byte[] transCountByte = rocksJavaUtil.get(Config.EVT_TX_COUNT_KEY);
//        BigInteger transCount = (null==transCountByte)
//                ? BigInteger.ZERO : new BigInteger(new String(transCountByte));
//        // totalConsEventCount
//        byte[] totalConsEventCountByte = rocksJavaUtil.get(Config.CONS_EVT_COUNT_KEY);
//        BigInteger totalConsEventCount = (null==totalConsEventCountByte)
//                ? BigInteger.ZERO : new BigInteger(new String(totalConsEventCountByte));
//        // last cons event
//        String[][] lastConsEbStrs = new String[node.getShardCount()][node.getnValue()];
//        for (int i = 0; i < node.getShardCount(); i++) {
//            for (int j = 0; j < node.getnValue(); j++) {
//                // key直接获取
//                byte[] lastSeqByte = rocksJavaUtil.get(i + "_" + j);
//
//                if (null !=lastSeqByte  && lastSeqByte.length > 0) {
//                    BigInteger lastSeq0 = new BigInteger(new String(lastSeqByte));
//                    BigInteger lastSeq = lastSeq0;
//
//                    EventKeyPair pair = new EventKeyPair(i, j, lastSeq.longValue());
//                    byte[] ebByte = rocksJavaUtil.get(pair.toString());
//
//                    if (null !=ebByte  && ebByte.length > 0
//                            && JSONObject.parseObject(new String(ebByte)).getString("consTimestamp")!=null) {
//                        // 向后获取更新的Event
//                        while(null !=ebByte  && ebByte.length > 0
//                                && JSONObject.parseObject(new String(ebByte)).getString("consTimestamp")!=null) {
//                            lastSeq = lastSeq.add(BigInteger.ONE);
//
//                            pair = new EventKeyPair(i, j, lastSeq.longValue());
//                            ebByte = rocksJavaUtil.get(pair.toString());
//                        }
//                    } else {
//                        // 向前获取存在的Event
//                        while (null==ebByte || ebByte.length<=0
//                                || JSONObject.parseObject(new String(ebByte)).getString("consTimestamp")==null) {
//                            lastSeq = lastSeq.subtract(BigInteger.ONE);
//                            if (lastSeq.equals(BigInteger.ZERO)) {
//                                break;
//                            }
//                            pair = new EventKeyPair(i, j, lastSeq.longValue());
//                            ebByte = rocksJavaUtil.get(pair.toString());
//                        }
//                    }
//                    if (null !=ebByte  && ebByte.length > 0) {
//                        lastConsEbStrs[i][j] = new String(ebByte);
//                    }
//                }
//            }
//        }
//        //
//        BigInteger realConsEventCount = BigInteger.ZERO;
//        for (int i = 0; i < node.getShardCount(); i++) {
//            for (int j = 0; j < node.getnValue(); j++) {
//                if (StringUtils.isNotEmpty(lastConsEbStrs[i][j])) {
//                    JSONObject eb = JSONObject.parseObject(lastConsEbStrs[i][j]);
//                    BigInteger consEventCount = eb.getBigInteger("consEventCount");
//                    if (null != consEventCount && consEventCount.compareTo(realConsEventCount) > 0) {
//                        realConsEventCount = consEventCount;
//                    }
//                }
//            }
//        }
//        if (totalConsEventCount.compareTo(realConsEventCount) > 0) {
//            logger.warn("node-({}, {}): totalConsEventCount diff: db: {}, calcu: {}",
//                    node.getShardId(), node.getCreatorId(), totalConsEventCount, realConsEventCount);
//            totalConsEventCount = realConsEventCount;
//            rocksJavaUtil.put(Config.CONS_EVT_COUNT_KEY, totalConsEventCount.toString());
//            logger.warn("node-({}, {}): totalConsEventCount by repair: {}", node.getShardId(),
//                    node.getCreatorId(), totalConsEventCount);
//        }
//        //
//        BigInteger transCountByCalcuEb = BigInteger.ZERO;
//        for (int i = 0; i < node.getShardCount(); i++) {
//            for (int j = 0; j < node.getnValue(); j++) {
//                if (StringUtils.isNotEmpty(lastConsEbStrs[i][j])) {
//                    EventBody eb = JSONObject.parseObject(lastConsEbStrs[i][j], EventBody.class);
//                    if (null != eb.getTransCount() && eb.getTransCount().compareTo(transCountByCalcuEb) > 0) {
//                        transCountByCalcuEb = eb.getTransCount();
//                    }
//                }
//            }
//        }
//        if (transCount.compareTo(transCountByCalcuEb) > 0) {
//            logger.warn("node-({}, {}): transCount diff: db: {}, calcu: {}",
//                    node.getShardId(), node.getCreatorId(), transCount, transCountByCalcuEb);
//            transCount = transCountByCalcuEb;
//            rocksJavaUtil.put(Config.EVT_TX_COUNT_KEY, transCount.toString());
//            logger.warn("node-({}, {}): transCount by repair: {}",
//                    node.getShardId(), node.getCreatorId(), transCount);
//        }
//    }

	// key condition
//	/**
//	 * 根据最新快照，恢复相关快照参数： treeRootMap、snapshotPointMap等
//	 * 
//	 * @param node
//	 */
//	public static void detectAndRepairSnapshotData(LocalFullNode1GeneralNode node) {
//		SnapshotMessage snapshotMessage = SnapshotDbService.queryLatestSnapshotMessage(node.nodeParameters.dbId);
//		if (snapshotMessage != null) {
//			node.setSnapshotMessage(snapshotMessage);
//			node.getSnapshotPointMap().put(snapshotMessage.getSnapVersion(), snapshotMessage.getSnapshotPoint());
//			node.getTreeRootMap().put(snapshotMessage.getSnapVersion(),
//					snapshotMessage.getSnapshotPoint().getMsgHashTreeRoot());
//			EventKeyPair pair = new EventKeyPair(snapshotMessage.getSnapshotPoint().getEventBody().getShardId(),
//					snapshotMessage.getSnapshotPoint().getEventBody().getCreatorId(),
//					snapshotMessage.getSnapshotPoint().getEventBody().getCreatorSeq());
//			logger.warn("node-({},{}) snap vers: {}, eb-{}, treeRoot: {}", node.getShardId(), node.getCreatorId(),
//					snapshotMessage.getSnapVersion(), pair.toString(),
//					snapshotMessage.getSnapshotPoint().getMsgHashTreeRoot());
//		} else {
//			logger.warn("node-({},{}): LatestSnapshotMessage is null.", node.getShardId(), node.getCreatorId());
//		}
//		// 之前DEFAULT_SNAPSHOT_CLEAR_GENERATION个版本的快照
//		SnapshotMessage sm = snapshotMessage;
//		if (null != sm && StringUtils.isNotEmpty(sm.getPreHash())) {
//			clearHistoryEventsBySnapshot(sm.getSnapVersion(), sm.getPreHash(), node);
//		}
//
//		for (int i = 0; i < Config.DEFAULT_SNAPSHOT_CLEAR_GENERATION; i++) {
//			if (snapshotMessage != null && StringUtils.isNotEmpty(snapshotMessage.getPreHash())) {
//				snapshotMessage = SnapshotDbService.querySnapshotMessageByHash(node.nodeParameters.dbId,
//						snapshotMessage.getPreHash());
//				node.getSnapshotPointMap().put(snapshotMessage.getSnapVersion(), snapshotMessage.getSnapshotPoint());
//				node.getTreeRootMap().put(snapshotMessage.getSnapVersion(),
//						snapshotMessage.getSnapshotPoint().getMsgHashTreeRoot());
//
//				logger.warn("node-({},{}) snap vers: {}, treeRoot: {}", node.getShardId(), node.getCreatorId(),
//						snapshotMessage.getSnapVersion(), snapshotMessage.getSnapshotPoint().getMsgHashTreeRoot());
//			} else {
//				break;
//			}
//		}
//	}

	// key condition
//	/**
//	 * 清除当前快照vers之前第 Config.DEFAULT_SNAPSHOT_CLEAR_GENERATION) 个快照的快照点之前的所有Event
//	 * 
//	 * @param vers 当前版本
//	 */
//	public static void clearHistoryEventsBySnapshot(BigInteger vers, String preHash, Main node) {
//		// 快照消息入库
//		if (vers.compareTo(BigInteger.valueOf(Config.DEFAULT_SNAPSHOT_CLEAR_GENERATION)) > 0) {
//			logger.warn("node-({},{}): start to clear history events", node.getShardId(), node.getCreatorId());
//			// 查询之前第 Config.DEFAULT_SNAPSHOT_CLEAR_GENERATION) 个快照
//			int i = Config.DEFAULT_SNAPSHOT_CLEAR_GENERATION - 1;
//			while (i > 0) {
//				logger.warn("node-({}, {}): Generation: {}, i: {}, preHash: {}", node.getShardId(), node.getCreatorId(),
//						Config.DEFAULT_SNAPSHOT_CLEAR_GENERATION, i, preHash);
//				if (StringUtils.isEmpty(preHash)) {
//					logger.error("node-({}, {}): snapshot is null. can not delete events...", node.getShardId(),
//							node.getCreatorId());
//					break;
//				} else {
//					SnapshotMessage sm = SnapshotDbService.querySnapshotMessageByHash(node.nodeParameters.dbId,
//							preHash);
//					if (null == sm) {
//						logger.error("node-({}, {}): snapshot is null.", node.getShardId(), node.getCreatorId());
//						break;
//					}
//					preHash = sm.getPreHash();
//					i--;
//					if (i == 0) {
//						// 删除其快照点Event之前的所有Event
//						logger.warn("node-({}, {}): clear event before snap version {}...", node.getShardId(),
//								node.getCreatorId(), sm.getSnapVersion());
//						SnapshotDbService.deleteEventsBeforeSnapshotPointEvent(node.nodeParameters.dbId,
//								sm.getSnapshotPoint().getEventBody(), node.getnValue());
//						// 清除之前版本的treeRootMap
//						node.getTreeRootMap()
//								.remove(vers.subtract(BigInteger.valueOf(Config.DEFAULT_SNAPSHOT_CLEAR_GENERATION)));
//					}
//				}
//			}
//			if (logger.isDebugEnabled()) {
//				logger.debug("========= snapshot message version-{} delete events success.", vers);
//			}
//		}
//	}

	public static void initStatistics(LocalFullNode1GeneralNode node) {
		// 检测消息数据一致性（mysql和rocksdb）
		DetectMessageConsistency.DetectMessageConsistency(node.nodeParameters().dbId);

		RocksJavaUtil rocksJavaUtil = new RocksJavaUtil(node.nodeParameters().dbId);

		// 共识Event总数
		byte[] totalConsEventCount = rocksJavaUtil.get(Config.CONS_EVT_COUNT_KEY);
		node.setTotalConsEventCount(
				null == totalConsEventCount ? BigInteger.ZERO : new BigInteger(new String(totalConsEventCount)));
		logger.info("node-({},{}): initStatistics() totalConsEventCount = {}", node.getShardId(), node.getCreatorId(),
				node.getTotalConsEventCount());

		// 共识message总数和最大Id
		BigInteger idx = BigInteger.ZERO;
		byte[] transaSplitBytes = rocksJavaUtil.get(Config.MESSAGES);
		if (null != transaSplitBytes) {
			TransactionSplit tableInfo = JSONObject.parseObject(new String(transaSplitBytes), TransactionSplit.class);
			idx = tableInfo.getTableIndex();
		}
		MysqlHelper h = null;
		List<BigInteger> list = null;
		try {
			h = new MysqlHelper(node.nodeParameters().dbId);
			String sql = "select max(id) as id from " + Config.MESSAGES + Config.SPLIT + idx.toString();
			list = h.executeQuery(sql, (rs, index) -> new BigInteger(rs.getString("id")));
		} catch (Exception ex) {
			logger.error("error: {}", ex);
			System.exit(-1);
		} finally {
			if (h != null) {
				h.destroyed();
			}
		}
		if (null != list && list.size() > 0) {
			// node.setConsMessageMaxId(list.get(0));
			node.addConsMessageMaxId(list.get(0).longValue());
			node.setConsMessageCount(node.getConsMessageMaxId());

			byte[] consMessageMaxId = rocksJavaUtil.get(Config.CONS_MSG_COUNT_KEY);
			if (null == consMessageMaxId || !new BigInteger(new String(consMessageMaxId)).equals(list.get(0))) {
//				logger.warn("node-({},{}): initStatistics() fix Config.CONS_MSG_COUNT_KEY value from {} to {}",
//						node.getShardId(), node.getCreatorId(), new String(consMessageMaxId), list.get(0));
				rocksJavaUtil.put(Config.CONS_MSG_COUNT_KEY, list.get(0).toString());
			}
		} else {
			logger.error("initStatistics() Inve did not creation.");
			System.exit(-1);
		}
		logger.info("node-({},{}): initStatistics() table idx = {}, ConsMessageMaxId = {}, ConsMessageCount = {}",
				node.getShardId(), node.getCreatorId(), idx, node.getConsMessageMaxId(), node.getConsMessageCount());

		// Event总数
		byte[] eventCount = rocksJavaUtil.get(Config.EVT_COUNT_KEY);
		node.setTotalEventCount(null == eventCount ? BigInteger.ZERO : new BigInteger(new String(eventCount)));
		logger.info("node-({},{}): initStatistics() totalEventCount = {}", node.getShardId(), node.getCreatorId(),
				node.getTotalEventCount());

		// 系统自动生成交易总数
		BigInteger idx1 = BigInteger.ZERO;
		byte[] sysMsgSplitBytes = rocksJavaUtil.get(Config.SYSTEMAUTOTX);
		if (null != sysMsgSplitBytes) {
			TransactionSplit tableInfo = JSONObject.parseObject(new String(sysMsgSplitBytes), TransactionSplit.class);
			idx1 = tableInfo.getTableIndex();
		}
		List<BigInteger> list1 = null;
		try {
			h = new MysqlHelper(node.nodeParameters().dbId);
			String sql = "select max(id) as id from " + Config.SYSTEMAUTOTX + Config.SPLIT + idx1.toString();
			// 2019.3.27杨泽斌
			// 解决创世启动初始化空指针异常
			list1 = h.executeQuery(sql,
					(rs, index) -> rs == null || StringUtils.isEmpty(rs.getString("id")) ? BigInteger.ZERO
							: new BigInteger(rs.getString("id")));
		} catch (Exception ex) {
			logger.error("error: {}", ex);
			System.exit(-1);
		} finally {
			if (h != null) {
				h.destroyed();
			}
		}
		if (null != list1 && list1.size() > 0) {
			// node.setSystemAutoTxMaxId(list1.get(0));
			node.addSystemAutoTxMaxId(list1.get(0).longValue());

			byte[] sysTxCount = rocksJavaUtil.get(Config.SYS_TX_COUNT_KEY);
			if (null == sysTxCount || !new BigInteger(new String(sysTxCount)).equals(list1.get(0))) {
//                logger.warn("node-({},{}): initStatistics() fix Config.SYS_TX_COUNT_KEY value from {} to {}",
//                        node.getShardId(), node.getCreatorId(), new String(sysTxCount), list1.get(0));
				rocksJavaUtil.put(Config.SYS_TX_COUNT_KEY, list1.get(0).toString());
			}
		} else {
			// node.setSystemAutoTxMaxId(BigInteger.ZERO);
			rocksJavaUtil.put(Config.SYS_TX_COUNT_KEY, BigInteger.ZERO.toString());
			logger.warn("node-({},{}): initStatistics() fix Config.SYS_TX_COUNT_KEY value to {}", node.getShardId(),
					node.getCreatorId(), list1.get(0));
		}
		logger.info("node-({},{}): initStatistics() SystemAutoTxMaxId = {}", node.getShardId(), node.getCreatorId(),
				node.getSystemAutoTxMaxId());

		DepItemsManager.getInstance().attachStat(null).addTotalEventCount(node.getTotalConsEventCount().longValue());
	}

	private static void saveCreationTransacstionMessage(LocalFullNode1GeneralNode node, int id, String message)
			throws Exception {
		if (StringUtils.isEmpty(message)) {
			throw new RuntimeException("Creation tx is empty.");
		}
		String dbId = node.nodeParameters().dbId;
		boolean isDrop = (node.nodeParameters().clearDb == 1);
//        long updateTime = Instant.now().toEpochMilli();

		JSONObject o = JSON.parseObject(message);
		// 验证
		boolean valid = TxVerifyUtils.verifyCreationMessage(o, new RocksJavaUtil(node.nodeParameters().dbId),
				node.nodeParameters().multiple);

		// temporal comment
//		if (!valid) {
//			throw new RuntimeException("Creation tx is illegal.");
//		}
		String fromAddress = o.getString("fromAddress");
		String toAddress = o.getString("toAddress");
		String signature = o.getString("signature");
		BigInteger amount = o.getBigInteger("amount");
		BigInteger fee = o.getBigInteger("fee");
		BigInteger nrgPrice = o.getBigInteger("nrgPrice");
		long timestamp = o.getLong("timestamp");
		String pubkey = o.getString("pubkey");
		String vers = o.getString("vers");
		String eHash = StringUtils.isEmpty(o.getString("eHash")) ? "" : o.getString("eHash");
		String remark = StringUtils.isEmpty(o.getString("remark")) ? "" : o.getString("remark");

		// mysql
		MysqlHelper mysqlHelper = new MysqlHelper(dbId, isDrop);
		StringBuilder sql = new StringBuilder();
		sql.append("INSERT INTO ");
		sql.append(Config.MESSAGES).append("_0 VALUES (").append(id).append(",'").append(fromAddress).append("', '")
				.append(toAddress).append("', '").append(signature).append("', 1, '").append(eHash).append("', 1, ")
				.append(timestamp).append(", null)");
		mysqlHelper.executeUpdate(sql.toString());

		// rocksdb
		RocksJavaUtil rocksJavaUtil = new RocksJavaUtil(dbId);
		Message msg = new Message.Builder().id("" + id).hash(signature).isStable(true).isValid(valid)
				.updateTime(timestamp).message(message).eHash(eHash).lastIdx(false).build();
		rocksJavaUtil.put(signature, JSON.toJSONString(msg));

		// 更新总message数
		// node.setConsMessageMaxId(node.getConsMessageMaxId().add(BigInteger.ONE));
		node.addConsMessageMaxId(1);
		rocksJavaUtil.put(Config.CONS_MSG_COUNT_KEY, node.getConsMessageMaxId().toString());

//		// 更新世界状态
		WorldStateService.transfer(dbId, fromAddress, toAddress, amount);
		WorldStateService.transfer(dbId, fromAddress, Config.FOUNDATION_ADDRESS, fee.multiply(nrgPrice));
	}

	/**
	 * 初始化十个水龙头
	 */
	public static void initCreationTx(LocalFullNode1GeneralNode node) {
		List<String> createTxs = Config.CREATION_TX_LIST;
		try {
			for (int i = 0; i < createTxs.size(); i++) {
				try {
					saveCreationTransacstionMessage(node, i + 1, createTxs.get(i));
				} catch (Exception e) {
					logger.error("creation exception\nexit...");
					System.exit(-1);
				}
			}
			// 初始化分表信息
//			RocksJavaUtil rocksJavaUtil = new RocksJavaUtil(node.nodeParameters().dbId);
//			TransactionSplit split = new TransactionSplit();
//			split.setTableName(Config.MESSAGES + "_0");
//			split.setTableIndex(BigInteger.ZERO);
//			split.setTableNamePrefix(Config.MESSAGES);
//			split.setTotal(createTxs.size());
//
//			rocksJavaUtil.put(Config.MESSAGES, JSONArray.toJSONString(split));
//			byte[] value = new RocksJavaUtil(node.nodeParameters().dbId).get(Config.MESSAGES);
//			while (null == value) {
//				rocksJavaUtil.put(Config.MESSAGES, JSONArray.toJSONString(split));
//				value = new RocksJavaUtil(node.nodeParameters().dbId).get(Config.MESSAGES);
//			}
			RocksJavaUtil rocksJavaUtil = new RocksJavaUtil(node.nodeParameters().dbId);
			byte[] value = initializeOrRetrieveTableSplit(rocksJavaUtil, createTxs.size());

			logger.warn("db main{} save split table info: {}", node.nodeParameters().dbId, new String(value));
			rocksJavaUtil.put(Config.CREATION_TIME_KEY, "1550409802757");
		} catch (Exception ex) {
			logger.error("initTen error>>>>>>>>>>>", ex);
		}
	}

	/**
	 * strip initial tablesplit code snippet from {@code initCreationTx}
	 */
	public static byte[] initializeOrRetrieveTableSplit(RocksJavaUtil rocksJavaUtil, int genesisTxesSize) {
		TransactionSplit firstSplit = new TransactionSplit();
		firstSplit.setTableName(Config.MESSAGES + "_0");
		firstSplit.setTableIndex(BigInteger.ZERO);
		firstSplit.setTableNamePrefix(Config.MESSAGES);
		firstSplit.setTotal(genesisTxesSize);

		rocksJavaUtil.put(Config.MESSAGES, JSONArray.toJSONString(firstSplit));
		byte[] retrievedSplitBytes = rocksJavaUtil.get(Config.MESSAGES);

		while (retrievedSplitBytes == null) {
			rocksJavaUtil.put(Config.MESSAGES, JSONArray.toJSONString(firstSplit));
			retrievedSplitBytes = rocksJavaUtil.get(Config.MESSAGES);
		}

		return retrievedSplitBytes;
	}

	public static void main(String[] args) {

		int num = 4;
		for (int j = 0; j < num; j++) {
			RocksJavaUtil rocksJavaUtil = new RocksJavaUtil("0_" + j);

			byte[] createTime = rocksJavaUtil.get(Config.CREATION_TIME_KEY);
			System.out.println("createTime: " + (null == createTime ? 0 : new String(createTime)));

			byte[] tps = rocksJavaUtil.get(Config.MESSAGE_TPS_KEY);
			System.out.println("tps: " + (null == tps ? BigDecimal.ZERO : new BigDecimal(new String(tps))));

			byte[] sysTxCount = rocksJavaUtil.get(Config.SYS_TX_COUNT_KEY);
			System.out.println(
					"sysTxCount: " + (null == sysTxCount ? BigInteger.ZERO : new BigInteger(new String(sysTxCount))));

			byte[] transCount = rocksJavaUtil.get(Config.EVT_TX_COUNT_KEY);
			System.out.println(
					"transCount: " + (null == transCount ? BigInteger.ZERO : new BigInteger(new String(transCount))));

			byte[] consMessageCount = rocksJavaUtil.get(Config.CONS_MSG_COUNT_KEY);
			System.out.println("consMessageCount: "
					+ (null == consMessageCount ? BigInteger.ZERO : new BigInteger(new String(consMessageCount))));

			byte[] eventCount = rocksJavaUtil.get(Config.EVT_COUNT_KEY);
			System.out.println(
					"eventCount: " + (null == eventCount ? BigInteger.ZERO : new BigInteger(new String(eventCount))));

			byte[] totalConsEventCount = rocksJavaUtil.get(Config.CONS_EVT_COUNT_KEY);
			System.out.println("totalConsEventCount: " + (null == totalConsEventCount ? BigInteger.ZERO
					: new BigInteger(new String(totalConsEventCount))));

			for (int i = 0; i < num; i++) {
				byte[] lastSeq = rocksJavaUtil.get("0_" + i);
				String lastSeqStr = new String(lastSeq);
				System.out.println(
						"0_" + i + " lastSeq: " + (null == lastSeq ? BigInteger.ZERO : new BigInteger(lastSeqStr)));

//                byte[] event = rocksJavaUtil.get("0_" + i + "_" + lastSeqStr);
//                System.out.println("event-(0_" + i + "_" + lastSeqStr + "): " + new String(event));
			}

			System.out.println("\n");
		}

		BigInteger[] lastSeqs = new BigInteger[num];
		for (int j = 0; j < num; j++) {
			RocksJavaUtil rocksJavaUtil = new RocksJavaUtil("0_" + j);
			byte[] lastSeq = rocksJavaUtil.get("0_2");
			BigInteger lastSeqInt = new BigInteger(new String(lastSeq));
			lastSeqs[j] = lastSeqInt;
		}

		int i = 2;
//        for (int i = 0; i < num; i++) {
		BigInteger max = lastSeqs[i];
		for (BigInteger k = max, min = max.subtract(BigInteger.valueOf(20)); k.compareTo(min) > 0; k = k
				.subtract(BigInteger.ONE)) {
			for (int j = 0; j < num; j++) {
				RocksJavaUtil rocksJavaUtil = new RocksJavaUtil("0_" + j);
				byte[] event = rocksJavaUtil.get("0_" + i + "_" + k.toString());
				if (null != event && event.length > 0) {
					System.out.println(
							"node-0," + j + ": event-(0_" + i + "_" + k.toString() + "): " + new String(event));
				}
			}
		}
//        }
	}
}
