package test;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import one.inve.beans.dao.Message;
import one.inve.beans.dao.TransactionSplit;
import one.inve.core.Config;
import one.inve.db.transaction.MysqlHelper;
import one.inve.rocksDB.RocksJavaUtil;
import one.inve.util.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

public class DetectMessageConsistency {
    private static final Logger logger = LoggerFactory.getLogger(DetectMessageConsistency.class);

    public static void main(String[] args) {
        // 设置数据库名
        String dbName = "0_0";
        DetectMessageConsistency(dbName);
    }

    public static void DetectMessageConsistency(String dbName) {
        // 读取MySQL数据
        List<MsgPair> list = queryAllMessagesFromMysql(dbName);

        // 逐条查找rocksdb是否存在，如果不存在则打印错误信息
        detectMessagesWhetherInRocksdb(dbName, list);

        // 修复重复记录导致rocksdb被错误覆盖更新的记录
        repairRepeatMessageInfo(dbName);

        // 打印指定记录
        printInfo(dbName);
    }

    public static void printInfo(String dbName) {
        List<String> list = Arrays.asList(
                "32RANBoQucE4DYD7/WmxHhOoZSaSr+2LZvM4/ttbw5TxVhXfRGgyR8C8hN8N70TI3Jk0tiVuLNTxc85v8K12yfHA==",
                "32K8iDd2mz801UL6M2R0Uwo336ETalo4Yzprn9KLblBZcbEzILne8Nkppbn8cmbtpUyc/8rLHbTsSa9SqNzP8w1g==",
                "32ON/lRvs01i7acR0uoGuKlHZDd++zpAa85ldZH6CNrfornxrv9abQU5mL9LCf6OQA6j5+p4rh3RlIf/Zp8gxrcg==",
                "32G0SkbiWrHn66NnwoP9jL1FfSHU+0Xunay3FYtZN67oQrz+RKyrK096YqlK0H+zruBbMnBg58GU9KBqQmFouC+g==");
    }

//    public static void repairMessageNonConsistency(String dbName) {
//        // 读取MySQL数据
//        List<MsgPair> list = queryAllMessagesFromMysql(dbName);
//
//        // 待补充的完善msg
//        HashMap<String, String> msgMap = initMsgs();
//
//        // 逐条查找rocksdb是否存在，如果不存在则补充
//        repairMessagesNotInRocksdb(dbName, list, msgMap);
//    }

    private static HashMap<String, String> initMsgs() {
//        HashMap<String, String> msgMap = new HashMap<>();
//        msgMap.put("", "");
//        msgMap.put("", "");
//        msgMap.put("", "");
//        msgMap.put("", "");
//        msgMap.put("", "");
//        msgMap.put("", "");
//        return msgMap;
        return null;
    }

    private static List<MsgPair> queryAllMessagesFromMysql(String dbName){
        MysqlHelper h = new MysqlHelper(dbName);
        List<MsgPair> list = null;

        // 共识message总数和最大Id
        BigInteger idx = BigInteger.ZERO;
        RocksJavaUtil rocksJavaUtil = new RocksJavaUtil(dbName);
        byte[] transaSplitBytes = rocksJavaUtil.get(Config.MESSAGES);
        if (null != transaSplitBytes) {
            TransactionSplit tableInfo =
                    JSONObject.parseObject(new String(transaSplitBytes), TransactionSplit.class);
            idx = tableInfo.getTableIndex();
        }

        for (BigInteger i = BigInteger.ZERO; i.compareTo(idx) <= 0; i=i.add(BigInteger.ONE)) {
            try {
                String sql = "select id, hash, isValid from " + Config.MESSAGES + Config.SPLIT + idx.toString()
                        + " where id in (select min(id) from messages_0 group by hash)";
                if (list == null ) {
                    list = h.executeQuery(sql,
                            (rs, index) -> new MsgPair(new BigInteger(rs.getString("id")),
                                    rs.getString("hash"), rs.getInt("isValid")==1));
                } else {
                    list.addAll(h.executeQuery(sql,
                            (rs, index) -> new MsgPair(new BigInteger(rs.getString("id")),
                                    rs.getString("hash"), rs.getInt("isValid")==1)));
                }
            } catch(Exception ex) {
                logger.error("queryAllMessagesFromMysql error: {}", ex);
                System.exit(-1);
            } finally {
                if(h!=null) {
                    h.destroyed();
                }
            }
        }

        return list;
    }

    public static void repairRepeatMessageInfo(String dbName){
        MysqlHelper h = new MysqlHelper(dbName);

        // 共识message总数和最大Id
        BigInteger idx = BigInteger.ZERO;
        RocksJavaUtil rocksJavaUtil = new RocksJavaUtil(dbName);
        byte[] transaSplitBytes = rocksJavaUtil.get(Config.MESSAGES);
        if (null != transaSplitBytes) {
            TransactionSplit tableInfo =
                    JSONObject.parseObject(new String(transaSplitBytes), TransactionSplit.class);
            idx = tableInfo.getTableIndex();
        }

        List<String> repList = null;
        for (BigInteger i = BigInteger.ZERO; i.compareTo(idx) <= 0; i=i.add(BigInteger.ONE)) {
            try {
                String sql = "select hash from " + Config.MESSAGES + Config.SPLIT + idx.toString() + " group by hash having count(*) >1";
                if (repList == null ) {
                    repList = h.executeQuery(sql, (rs, index) -> rs.getString("hash"));
                } else {
                    repList.addAll(h.executeQuery(sql,
                            (rs, index) -> rs.getString("hash")));
                }
            } catch(Exception ex) {
                logger.error("repairRepeatMessageInfo error: {}", ex);
                System.exit(-1);
            } finally {
                if(h!=null) {
                    h.destroyed();
                }
            }
        }

        for (String hash: repList) {
            logger.warn("handle repeat message-{}", hash);
            try {
                String sql = "select id, hash, isValid, updateTime from "
                        + Config.MESSAGES + Config.SPLIT + idx.toString() + " where hash='" + hash + "' limit 1";
                List<JSONObject> list0= h.executeQuery(sql, (rs, index) -> {
                    JSONObject o = new JSONObject();
                    o.put("id", rs.getString("id"));
                    o.put("hash", rs.getString("hash"));
                    o.put("isValid", rs.getInt("isValid")==1);
                    o.put("updateTime", rs.getString("updateTime"));
                    return o;
                });

                for (JSONObject o : list0) {
                    byte[] msgBytes = rocksJavaUtil.get(hash);
                    if (null != msgBytes) {
                        JSONObject o1 = JSONObject.parseObject(new String(msgBytes));
                        if (StringUtils.isEmpty(o1.getString("id"))
                                || !o.getString("id").equals(o1.getString("id"))) {
                            o1.put("id", o.getString("id"));
                        }
                        if (null == o1.getBoolean("isStable")) {
                            o1.put("isStable", true);
                        }
                        if (null == o.getBoolean("isValid")
                                || !o.getBoolean("isValid").equals(o1.getBoolean("isValid"))) {
                            o1.put("isValid", o.getBoolean("isValid"));
                        }
                        if (StringUtils.isEmpty(o1.getString("updateTime"))
                                || !o.getString("updateTime").equals(o1.getString("updateTime"))) {
                            o1.put("updateTime", o.getString("updateTime"));
                        }

                        rocksJavaUtil.put(hash, o1.toJSONString());
                    } else {
                        logger.error("\nmessage missing: \nhash = {}", hash);
                    }
                }
            } catch(Exception ex) {
                logger.error("repairRepeatMessageInfo error: {}", ex);
                System.exit(-1);
            } finally {
                if(h!=null) {
                    h.destroyed();
                }
            }
        }
    }

    private static List<String> detectMessagesWhetherInRocksdb(String dbName, List<MsgPair> list) {
        if (null == list || list.size() <=0 ) {
            logger.error("message list is null.");
            return null;
        }
        List<String> result = new ArrayList<>();

        RocksJavaUtil rocksJavaUtil = new RocksJavaUtil(dbName);
        for(MsgPair pair : list) {
            byte[] msgBytes = rocksJavaUtil.get(pair.getHash());
            if (null != msgBytes) {
                try {
                    JSONObject o = JSONObject.parseObject(new String(msgBytes));

                    if (null == o.getBoolean("isStable") ) {
                        logger.error("\nmessage-{} not consensus: \nhash = {}\nmsg: {}",
                                pair.getId().toString(), pair.getHash(), new String(msgBytes));
                    }
                    if (StringUtils.isEmpty(o.getString("id"))) {
                        logger.error("\nmessage-{} not consensus: \nhash = {}\nmsg: {}",
                                pair.getId().toString(), pair.getHash(), new String(msgBytes));
                    } else if (!pair.getId().toString().equals(o.getString("id"))) {
                        logger.error("\nmessage-{}'s id error: \nhash = {}\nmsg: {}",
                                pair.getId().toString(), pair.getHash(), new String(msgBytes));
                    }
                    if (null == o.getBoolean("isValid")) {
                        logger.error("\nmessage-{} not consensus: \nhash = {}\nmsg: {}",
                                pair.getId().toString(), pair.getHash(), new String(msgBytes));
                    } else if (!pair.getValid().toString().equals(o.getString("isValid"))) {
                        logger.error("\nmessage-{}'s isValid error: \nhash = {}\nmysql isValid:{}\nmsg: {}",
                                pair.getId().toString(), pair.getHash(), pair.getValid(), new String(msgBytes));
                    }
                } catch (Exception e) {
                    logger.error("\nmessage parse error: id(mysql) = {}, isValid(mysql) = {}\nhash(mysql) = {}\nmsg(rocksdb): {}",
                            pair.getId().toString(),  pair.getValid(), pair.getHash(), new String(msgBytes));
                    result.add(pair.getHash());
                }
            } else {
                logger.error("\nmessage missing: id(mysql) = {}, isValid(mysql) = {}\nhash(mysql) = {}",
                        pair.getId().toString(), pair.getValid(), pair.getHash());
                result.add(pair.getHash());
            }
        }

        return result;
    }

//    private static void repairMessagesNotInRocksdb(String dbName, List<MsgPair> list, HashMap<String, String> msgMap) {
//        if (null == list || list.size() <=0 ) {
//            logger.warn("message list is null.");
//            return;
//        }
//
//        RocksJavaUtil rocksJavaUtil = new RocksJavaUtil(dbName);
//        for(MsgPair pair : list) {
//            byte[] msgBytes = rocksJavaUtil.get(pair.getHash());
//            boolean needRepair = false;
//            if (null != msgBytes) {
//                try {
//                    JSONObject o = JSONObject.parseObject(new String(msgBytes));
//
//                    if (null == o.getBoolean("isStable") ) {
//                        needRepair = true;
//                    }
//                } catch (Exception e) {
//                    needRepair = true;
//                }
//            } else {
//                needRepair = true;
//            }
//
//            if (needRepair) {
//                rocksJavaUtil.put(pair.getHash(), msgMap.get(pair.getHash()));
//                logger.warn("\nmessage-{} repair success: \nhash = {}", pair.getId().toString(), pair.getHash());
//            }
//        }
//    }

    static class MsgPair {
        BigInteger id;
        String hash;
        Boolean isValid;

        public MsgPair() {
        }

        public BigInteger getId() {
            return id;
        }

        public void setId(BigInteger id) {
            this.id = id;
        }

        public String getHash() {
            return hash;
        }

        public void setHash(String hash) {
            this.hash = hash;
        }

        public Boolean getValid() {
            return isValid;
        }

        public void setValid(Boolean valid) {
            isValid = valid;
        }

        public MsgPair(BigInteger id, String hash, Boolean isValid) {
            this.id = id;
            this.hash = hash;
            this.isValid = isValid;
        }
    }
}
