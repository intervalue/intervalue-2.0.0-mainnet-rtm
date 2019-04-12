package test;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Clare
 * @date   2018/10/25 0025.
 */
public class DbDataTest {
    private static final Logger logger = LoggerFactory.getLogger(DbDataTest.class);
    final static int START_NUM = 0;
    final static int NUM = 4;

//    public static void main(String[] args) {
//        DbDataTest test = new DbDataTest();
//
//        logger.info("\n============================ Test New  Event  =================================");
//        test.testEventData(NUM);
//
////        logger.info("\n============================ Test Transaction =================================");
////        test.testTransactionData(NUM);
////
////        logger.info("\n============================ Test Snapshot =================================");
////        test.testSnapshotData(NUM);
//    }
//
//
//    private void testTransactionData(int num) {
//        String dbNameSuffix = "trans.hashnet.sqlite";
//        String tableNamePrx = "transactions_";
//
//        long[][] tranCounts = new long[num][];
//        for(int i=0; i<num; i++) {
//            tranCounts[i] = countTransactions((START_NUM+i) , tableNamePrx);
//            logger.info(JSONArray.toJSONString(tranCounts[i]));
//        }
//
//        boolean flag = true;
//        for(int j=0; j<tranCounts[0].length; j++) {
//            for(int i=0; i<num-1; i++) {
//                if (tranCounts[i][j] != tranCounts[i+1][j]) {
//                    logger.error("node-{}'s trans count(table index: {}) not equal node-{}'s trans count(table index: {})", i, j, i+1, j);
//                    if (flag) {
//                        flag = false;
//                    }
//                }
//            }
//        }
//        if(flag) {
//            logger.info(">>>> all nodes' trans count are in same!");
//        }
//
//        long size = Arrays.stream(tranCounts).map(x -> x[0]).min(Long::compareTo).orElse(0L);
//        Transaction[][] transactions = new Transaction[num][];
//        for (int i=0; i<num; i++) {
//            transactions[i] = queryTransactions((START_NUM+i)  , tableNamePrx, tranCounts[0].length);
//        }
//
//        for(int j=0; j<size; j++) {
//            for(int i=0; i<num-1; i++) {
//                if ( !JSON.toJSONString(transactions[i][j]).equals(JSON.toJSONString(transactions[i+1][j])) ) {
//                    logger.error("tran-({}, {}) not equal tran-({},{}): ", i, j, i+1, j);
//                    logger.error("tran-({}, {}): {}", i, j, JSON.toJSONString(transactions[i][j]));
//                    logger.error("tran-({}, {}): {}", i+1, j, JSON.toJSONString(transactions[i+1][j]));
//                    if (flag) {
//                        flag = false;
//                    }
//                }
//            }
//        }
//
//        if (flag) {
//            logger.info(">>>> all nodes' trans are in same!");
//        }
//    }
//
//    private void testEventData(int num) {
//        long[][] lastSeq4AllNode = new long[NUM][NUM];
//        for(int i=0; i<NUM; i++) {
//            for (int j = 0; j < NUM; j++) {
//                lastSeq4AllNode[i][j] = countEvent(i, j);
//            }
//        }
//        logger.info(JSONArray.toJSONString(lastSeq4AllNode));
//        long[] seqs = new long[NUM];
//        boolean flag = true;
//        for(int i=0; i<NUM-1; i++) {
//            for (int j = 0; j < NUM; j++) {
//                if (lastSeq4AllNode[i][j] != lastSeq4AllNode[i+1][j]) {
//                    logger.error("(db, node)-({},{}) last seq {} not equal (db, node)-({},{}) last seq {}!",
//                            i, j, lastSeq4AllNode[i][j], i+1, j, lastSeq4AllNode[i+1][j]);
//                    if (flag) {
//                        flag = false;
//                    }
//                }
//                seqs[j] = Math.min(lastSeq4AllNode[i][j], lastSeq4AllNode[i+1][j]);
//            }
//        }
//
//        logger.info(JSONArray.toJSONString(seqs));
//        String[][][] events = new String[NUM][NUM][];
//        for(int i=0; i<NUM; i++) {
//            for (int j = 0; j < NUM; j++) {
//                events[i][j] = queryEvent(i, j, seqs[j]);
//            }
//        }
//
//        flag = true;
//        for(int i=0; i<NUM-1; i++) {
//            for (int j = 0; j < NUM; j++) {
//                for (int k = 0; k < seqs[j]; k++) {
//                    if ( !events[i][j][k].equals(events[i+1][j][k]) ) {
//                        EventBody eb1 = JSONObject.parseObject(events[i][j][k], EventBody.class);
//                        EventBody eb2 = JSONObject.parseObject(events[i+1][j][k], EventBody.class);
//                        if (null==eb1.getOtherHash() || null==eb1.getParentHash()) {
//                            logger.error("db-{} event-(0, {}, {}) otherHash: {}, parentHash: {}.", i, j, k, eb1.getOtherHash(), eb1.getParentHash());
//                            if (flag) {
//                                flag = false;
//                            }
//                            continue;
//                        }
//                        if (null==eb2.getOtherHash() || null==eb2.getParentHash()) {
//                            logger.error("db-{} event-(0, {}, {}) otherHash: {}, parentHash: {}.", i+1, j, k, eb2.getOtherHash(), eb2.getParentHash());
//                            if (flag) {
//                                flag = false;
//                            }
//                            continue;
//                        }
//                        if (eb1.hashCode() != eb2.hashCode() ) {
//                            logger.error("db-{} event-(0, {}, {}) hashCode not equal db-{} event-(0, {},{}) hashCode.", i, j, k, i+1, j, k);
//                            if (flag) {
//                                flag = false;
//                            }
//                            continue;
//                        } else if (!eb1.equals(eb2)) {
//                            logger.error("db-{} event-(0, {}, {}) toString not equal db-{} event-(0, {},{}) toString.", i, j, k, i+1, j, k);
//                            logger.error("db-{} event-(0, {}, {}): {}", i, j, k, JSON.toJSONString(eb1));
//                            logger.error("db-{} event-(0, {}, {}): {}", i+1, j, k, JSON.toJSONString(eb2));
//                            if (flag) {
//                                flag = false;
//                            }
//                            continue;
//                        }
//                    }
//                }
//            }
//        }
//
//        if (flag) {
//            logger.info(">>>> all nodes' event are in same!");
//        }
//    }
//
//    private void testSnapshotData(int num) {
//        String dbNameSuffix = "event.hashnet.sqlite";
//        String tableName = "snapshotMessages";
//
//        long[] snapshotCount = new long[num];
//        for(int i=0; i<num; i++) {
//            snapshotCount[i] = countSnapshot((START_NUM+i)  , tableName);
//        }
//        logger.info(JSONArray.toJSONString(snapshotCount));
//        boolean flag = true;
//        for(int i=0; i<num-1; i++) {
//            if (snapshotCount[i] != snapshotCount[i+1]) {
//                logger.error("node-{}'s snapshot count not equal node-{}'s snapshot count!", i, i+1);
//                if (flag) {
//                    flag = false;
//                }
//            }
//        }
//
//        long size = Arrays.stream(snapshotCount).min().orElse(0);
//        logger.info("same snapshot size: {}", size);
//
//        SnapshotMessage[][] snapshots = new SnapshotMessage[num][];
//        for (int i=0; i<num; i++) {
//            snapshots[i] = querySnapshot((START_NUM+i) , tableName);
//        }
//
//        flag = true;
//        for(int j=0; j<size; j++) {
//            for(int i=0; i<num-1; i++) {
//                if ( !JSON.toJSONString(snapshots[i][j]).equals(JSON.toJSONString(snapshots[i+1][j])) ) {
//                    logger.error("snapshot-({}, {}) not equal snapshot-({},{}): ", i, j, i+1, j);
//                    logger.error("snapshot-({}, {}): {}", i, j, JSON.toJSONString(snapshots[i][j]));
//                    logger.error("snapshot-({}, {}): {}", i+1, j, JSON.toJSONString(snapshots[i+1][j]));
//                    if (flag) {
//                        flag = false;
//                    }
//                }
//            }
//        }
//
//        if (flag) {
//            logger.info(">>>> all nodes' snapshots are in same!");
//        }
//    }
//
//
//    private long countEvent(Integer id, Integer creatorId) {
//        RocksJavaUtil rocksJavaUtil = new RocksJavaUtil(id);
//        String key = "0_"+creatorId;
//        byte[] lastSeqBytes = rocksJavaUtil.get(key);
//        long eventCount = 0;
//        if (null!=lastSeqBytes) {
//            eventCount = Long.parseLong(new String(lastSeqBytes));
//        }
//        return eventCount;
//    }
//
//    private long countSnapshot(Integer number, String tableName) {
//        Connection con = null;
//        Statement stmt = null;
//        ResultSet rs = null;
//
//        long snapshotCount = 0L;
//        try {
//            con = new MysqlHelper(number).getConnection();
//            stmt = con.createStatement();
//            String sql =  String.format("SELECT count(*) as snapshotCount from %s;", tableName);
//            rs = stmt.executeQuery(sql);
//            if (rs.next()) {
//                snapshotCount = rs.getLong("snapshotCount");
//            }
//            return snapshotCount;
//        } catch (Exception e) {
//            logger.error(">>>>>> countSnapshot() ERROR: " , e);
//            return snapshotCount;
//        } finally {
//            try {
//                if (null != rs) {
//                    rs.close();
//                }
//            } catch (SQLException e) {
//                logger.error("{}", e);
//            }
//            try {
//                if (null != stmt) {
//                    stmt.close();
//                }
//            } catch (SQLException e) {
//                logger.error("{}", e);
//            }
//            try {
//                if (null != con) {
//                    con.close();
//                }
//            } catch (SQLException e) {
//                logger.error("{}", e);
//            }
//        }
//    }
//
//    private long[] countTransactions(Integer number, String tableNamePrx) {
//        Connection con = null;
//        Statement stmt = null;
//        ResultSet rs = null;
//
//        long[] tranCounts = new long[1];
//        try {
//            con = new MysqlHelper(number).getConnection();
//            stmt = con.createStatement();
//            String sql;
//            for (int i=0; i<tranCounts.length; i++) {
//                sql =  String.format("SELECT count(*) as transCount from %s;",  tableNamePrx + i);
//                rs = stmt.executeQuery(sql);
//                if (rs.next()) {
//                    tranCounts[i] = rs.getLong("transCount");
//                }
//            }
//            return tranCounts;
//        } catch (Exception e) {
//            logger.error(">>>>>> countTransactions() ERROR: " , e);
//            return tranCounts;
//        } finally {
//            try {
//                if (null != rs) {
//                    rs.close();
//                }
//            } catch (SQLException e) {
//                logger.error("{}", e);
//            }
//            try {
//                if (null != stmt) {
//                    stmt.close();
//                }
//            } catch (SQLException e) {
//                logger.error("{}", e);
//            }
//            try {
//                if (null != con) {
//                    con.close();
//                }
//            } catch (SQLException e) {
//                logger.error("{}", e);
//            }
//        }
//    }
//
//    private String[] queryEvent(Integer id, long creatorId, long lastSeq) {
//        RocksJavaUtil rocksJavaUtil = new RocksJavaUtil(id);
//        String[] eventBodies = new String[(int)lastSeq];
//        for (int i = 0; i < lastSeq; i++) {
//            EventKeyPair pair = new EventKeyPair(0, creatorId, i);
//            byte[] eventBytes = rocksJavaUtil.get(pair.toString());
//            if (eventBytes != null) {
//                eventBodies[i] = new String(eventBytes);
////                System.out.println( new String(eventBytes));
//            } else {
//                logger.error("db-{} missing event-(0, {}, {})!!!", id, creatorId, i);
//            }
//        }
//        return eventBodies;
//    }
//
//    private SnapshotMessage[] querySnapshot(Integer number, String tableName) {
//        Connection con = null;
//        Statement stmt = null;
//        ResultSet rs = null;
//
//        ArrayList<SnapshotMessage> snapshots = new ArrayList<>();
//        try {
//            con = new MysqlHelper(number).getConnection();
//            stmt = con.createStatement();
//            String sql =  String.format("SELECT * from %s;", tableName);
//            rs = stmt.executeQuery(sql);
//            while (rs.next()) {
//                String snapshotPointStr = rs.getString("snapshotPoint");
//                String accountsStr = rs.getString("accounts");
//                Map<String, Object> accounts = JSON.parseObject(accountsStr);
//                HashMap<String, BigInteger> map = new HashMap<>();
//                for (Map.Entry entry : accounts.entrySet()) {
//                    map.put(""+entry.getKey(), new BigInteger(""+entry.getValue()));
//                }
//                snapshots.add(new SnapshotMessage.Builder()
//                        .type(MessageType.SNAPSHOT.getIndex())
//                        .snapVersion(new BigInteger(rs.getString("snapVersion")))
//                        .snapHash(rs.getString("snapHash"))
//                        .snapshotPoint(JSONObject.parseObject(snapshotPointStr, SnapshotPoint.class))
//                        .accounts(map)
//                        .pubkey(rs.getString("pubkey"))
//                        .preHash(rs.getString("signature"))
//                        .signature(rs.getString("preHash"))
//                        .timestamp(rs.getLong("timestamp"))
//                        .build());
//            }
//            return snapshots.toArray(new SnapshotMessage[0]);
//        } catch (Exception e) {
//            logger.error(">>>>>> queryConsensusEvent() ERROR: " , e);
//            return null;
//        } finally {
//            try {
//                if (null != rs) {
//                    rs.close();
//                }
//            } catch (SQLException e) {
//                logger.error("{}", e);
//            }
//            try {
//                if (null != stmt) {
//                    stmt.close();
//                }
//            } catch (SQLException e) {
//                logger.error("{}", e);
//            }
//            try {
//                if (null != con) {
//                    con.close();
//                }
//            } catch (SQLException e) {
//                logger.error("{}", e);
//            }
//        }
//    }
//
//    private Transaction[] queryTransactions(Integer number, String tableNamePrx, long tableSize) {
//        Connection con = null;
//        Statement stmt = null;
//        ResultSet rs = null;
//
//        ArrayList<Transaction> transactions = new ArrayList<>();
//        try {
//            con = new MysqlHelper(number).getConnection();
//            stmt = con.createStatement();
//            String sql;
//            for (int i=0; i<tableSize; i++) {
//                sql =  String.format("SELECT * from %s order by id;",  tableNamePrx + i);
//                rs = stmt.executeQuery(sql);
//                while (rs.next()) {
//                    transactions.add(new one.inve.beans.dao.Transaction.Builder()
//                            .id(new BigInteger(rs.getString("id")))
//                            .eHash(rs.getString("eHash"))
//                            .hash(rs.getString("hash"))
//                            .fromAddress(rs.getString("fromAddress"))
//                            .toAddress(rs.getString("toAddress"))
//                            .amount(new BigInteger(rs.getString("amount")))
//                            .fee(new BigInteger(rs.getString("fee")))
//                            .time(rs.getLong("time"))
//                            .remark(rs.getString("remark"))
//                            .isStable(rs.getBoolean("isStable"))
//                            .isValid(rs.getBoolean("isValid"))
//                            .build());
//                }
//            }
//            return transactions.toArray(new Transaction[0]);
//        } catch (Exception e) {
//            logger.error(">>>>>> queryEvents() ERROR: " , e);
//            return null;
//        } finally {
//            try {
//                if (null != rs) {
//                    rs.close();
//                }
//            } catch (SQLException e) {
//                logger.error("{}", e);
//            }
//            try {
//                if (null != stmt) {
//                    stmt.close();
//                }
//            } catch (SQLException e) {
//                logger.error("{}", e);
//            }
//            try {
//                if (null != con) {
//                    con.close();
//                }
//            } catch (SQLException e) {
//                logger.error("{}", e);
//            }
//        }
//    }
}
