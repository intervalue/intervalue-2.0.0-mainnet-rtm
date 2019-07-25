package one.inve.localfullnode2.store.mysql;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONException;
import com.alibaba.fastjson.JSONObject;
import one.inve.localfullnode2.conf.Config;
import one.inve.localfullnode2.store.rocks.*;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigInteger;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * 分表查询
 *
 * @author Administrator
 */
public class QueryTableSplit {
    private static final Logger logger = LoggerFactory.getLogger(QueryTableSplit.class);
    /**
     * 查询默认每页条数
     */
    private static final Integer PAGE_SIZE = 10;

    /**
     * 查询默认每页条数
     */
    private static final Integer PAGE_SIZE_BROWSER = 100;

    /**
     * 查询一次无数据，再次查询下一张表(最多查询两次)
     *
     * @param tableIndex 表索引 第一次查询请输入0
     * @param offset     跳过多少条记录 第一次查询请输入0
     * @param address    查询地址
     * @param type       类型：1交易 2合约 3.快照 4文本
     * @return 交易记录
     */
    public TransactionArray queryTransaction(BigInteger tableIndex, Long offset, String address, String type, String dbId) {
        TransactionArray array = new TransactionArray();
        MysqlHelper h = null;
        try {
            if (null != tableIndex && tableIndex.compareTo(BigInteger.ZERO) >= 0) {
                if (offset < 0) {
                    offset = 0L;
                }
                // 地址多为空则直接返回
                if (StringUtils.isBlank(address)) {
                    return array;
                }
                h = new MysqlHelper(dbId);
                array = findTransaction(array, h, tableIndex, offset, address, type, offset, dbId);
                List<Message> list = array.getList();
                // 查询不到，看是否有下一个表，如果有，继续查询
                if (list == null || list.size() < 1) {
                    tableIndex = tableIndex.add(BigInteger.ONE);
                    // tableIndex跳到下一张表后,offset从零开始
                    array = findTransaction(array, h, tableIndex, 0L, address, type, offset, dbId);
                }
            }
        } catch (Exception ex) {
            logger.error("error: {}", ex);
        } finally {
            if (h != null) {
                h.destroyed();
            }
        }
        return array;
    }

    /**
     * 查询表是否存在
     *
     * @return 交易表索引
     */
    public static TransactionSplit tableExist(String dbId) {
        // 是否存在TransactionSplit
        TransactionSplit split = null;
        try {
            byte[] value = new RocksJavaUtil(dbId).get(Config.MESSAGES);
            if (value != null) {
                String json = new String(value);
                split = JSONArray.parseObject(json, TransactionSplit.class);
            }
        } catch (Exception e) {
            logger.error("error: {}", e);
        }
        return split;
    }

    /**
     * 根据地址查询交易
     *
     * @param array
     * @param h
     * @param tableIndex
     * @param address    查询地址
     * @param type       类型：1交易 2合约 3.快照 4文本
     * @return
     */
    private TransactionArray findTransaction(TransactionArray array, MysqlHelper h, BigInteger tableIndex, long offset,
                                             String address, String type, long oldOffset, String dbId) {
//    	logger.info("findTransaction()...");
        array.setOffset(offset);
        array.setTableIndex(tableIndex);
        // logger.info("tableExist...");
        TransactionSplit split = tableExist(dbId);
        // logger.info("getTableIndex...");
        BigInteger splitIndex = split.getTableIndex();
        logger.info("tableIndex: {}, splitIndex: {}", tableIndex, splitIndex);
        if (tableIndex != null && tableIndex.compareTo(splitIndex) > 0) {
            array.setOffset(oldOffset);
            array.setTableIndex(splitIndex);
            return array;
        }
        String tableName = Config.MESSAGES + Config.SPLIT + tableIndex;
        StringBuilder sql = new StringBuilder("select hash from ");
        sql.append(tableName).append(" where 1=1");
        if (StringUtils.isNotBlank(type)) {
            sql.append(" and type in (").append(type).append(")");
        }
        if (StringUtils.isNotBlank(address)) {
            sql.append(" and ( fromAddress=?");
            sql.append(" or toAddress=? )");
        }

        List<Message> entityArrayList = array.getList();
        int pageSize = PAGE_SIZE;
        // 如果已经查询一次了，则查询减去上次的
        if (entityArrayList != null && entityArrayList.size() > 0) {
            pageSize = PAGE_SIZE - entityArrayList.size();
        }
        sql.append(" order by id asc limit ").append(pageSize).append(" offset ").append(offset);
        // logger.info("sql==========================" + sql.toString());
        try {
            PreparedStatement preparedStatement = h.getPreparedStatement(sql.toString());
            preparedStatement.setString(1, address.trim());
            preparedStatement.setString(2, address.trim());
            ResultSet rs = preparedStatement.executeQuery();
            List<Message> list = new ArrayList<Message>();
            while (rs.next()) {
                String hash = rs.getString("hash");
                byte[] transationByte = new RocksJavaUtil(dbId).get(hash);
                if (transationByte != null) {
                    String a = new String(transationByte);
                    // list.add(JSONArray.parseObject(transationByte, Message.class));
                    try {
                        list.add(JSONArray.parseObject(transationByte, Message.class));
                    } catch (Exception e) {
                        logger.error("message object format is illegal:({})", a);
                        list.add(new Message());
                    }
                } else {
                    logger.error("this hash rocksDB not exist");
                    return null;
                }
            }
            ;
            rs.close();
            preparedStatement.close();
            List<Message> entityList = array.getList();
            if (entityList == null) {
                entityList = new ArrayList<Message>();
            }

            array.setTableIndex(tableIndex);
            if (list != null) {
                entityList.addAll(list);
                array.setOffset(offset + list.size());
            } else {
                array.setOffset(offset);
            }
            array.setList(entityList);
        } catch (Exception e) {
            logger.error("error: {}", e);
        }
        return array;
    }

    /**
     * 用于区块链浏览器 获取SystemAuto表 查询一次无数据，再次查询下一张表(最多查询两次) 查询所有记录
     *
     * @param tableIndex 表索引 第一次查询请输入0
     * @param offset     跳过多少条记录 第一次查询请输入0
     * @return 交易记录
     */
    public SystemAutoArray querySystemAuto(BigInteger tableIndex, Long offset, String dbId) {
        SystemAutoArray array = new SystemAutoArray();
        MysqlHelper h = null;
        try {
            if (null != tableIndex && tableIndex.compareTo(BigInteger.ZERO) >= 0) {
                if (offset < 0) {
                    offset = 0L;
                }

                h = new MysqlHelper(dbId);
                array = querySystemAuto(array, h, tableIndex, offset, offset, dbId);
                List<JSONObject> list = array.getList();
                // 查询不到，看是否有下一个表，如果有，继续查询
                if (list == null || list.size() < 1) {
                    tableIndex = tableIndex.add(BigInteger.ONE);
                    // tableIndex跳到下一张表后,offset从零开始
                    array = querySystemAuto(array, h, tableIndex, 0L, offset, dbId);
                }
            }
        } catch (Exception ex) {
            logger.error("error: {}", ex);
        } finally {
            if (h != null) {
                h.destroyed();
            }
        }
        return array;
    }

    public JSONObject querySystemAuto(BigInteger tableIndex, String dbId, String hash) {
        MysqlHelper h = null;
        JSONObject systemAuto = null;
        try {
            if (null == tableIndex || tableIndex.compareTo(BigInteger.ZERO) < 0) {
                tableIndex = BigInteger.ZERO;
            }
            h = new MysqlHelper(dbId);
            systemAuto = querySystemAuto(h,tableIndex, dbId, hash);
            // 查询不到，看是否有下一个表，如果有，继续查询
            if (systemAuto == null) {
                tableIndex = tableIndex.add(BigInteger.ONE);
                TransactionSplit split = tableSystemAutoExist(dbId);
                BigInteger splitIndex = split.getTableIndex();
                if (tableIndex != null && tableIndex.compareTo(splitIndex) > 0) {
                    return systemAuto;
                }
                systemAuto = querySystemAuto(h,tableIndex, dbId, hash);
            }
        } catch (Exception ex) {
            logger.error("error: {}", ex);
        } finally {
            if (h != null) {
                h.destroyed();
            }
        }
        return systemAuto;
    }

    public SystemAutoArray querySystemAuto(BigInteger tableIndex, Long offset, String dbId, String address, String type) {
        SystemAutoArray array = new SystemAutoArray();
        MysqlHelper h = null;
        try {
            if (null != tableIndex && tableIndex.compareTo(BigInteger.ZERO) >= 0) {
                if (offset < 0) {
                    offset = 0L;
                }
//                if (StringUtils.isBlank(address)) {
//                    return array;
//                }
                h = new MysqlHelper(dbId);
                array = querySystemAuto(array,h,tableIndex,offset,offset,dbId,address,type);
                List<JSONObject> list = array.getList();
                // 查询不到，看是否有下一个表，如果有，继续查询
                if (list == null || list.size() < 1) {
                    tableIndex = tableIndex.add(BigInteger.ONE);
                    // tableIndex跳到下一张表后,offset从零开始
                    array = querySystemAuto(array, h, tableIndex, 0L, offset, dbId,address,type);
                }
            }
        } catch (Exception ex) {
            logger.error("error: {}", ex);
        } finally {
            if (h != null) {
                h.destroyed();
            }
        }
        return array;
    }

    /**
     * 查询表是否存在
     *
     * @return 交易表索引
     */
    public static TransactionSplit tableSystemAutoExist(String dbId) {
        // 是否存在TransactionSplit
        TransactionSplit split = null;
        try {
            byte[] value = new RocksJavaUtil(dbId).get(Config.SYSTEMAUTOTX);
            if (value != null) {
                String json = new String(value);
                split = JSONArray.parseObject(json, TransactionSplit.class);
            }
        } catch (Exception e) {
            logger.error("error: {}", e);
        }
        return split;
    }

    /**
     * 用于区块链浏览器 查询一次无数据，再次查询下一张表(最多查询两次) 查询所有记录
     *
     * @param tableIndex 表索引 第一次查询请输入0
     * @param offset     跳过多少条记录 第一次查询请输入0
     * @param type       类型：1交易 2合约 3.快照 4文本
     * @return 交易记录
     */
    public TransactionArray queryTransaction(BigInteger tableIndex, Long offset, Integer type, String dbId) {
        TransactionArray array = new TransactionArray();
        MysqlHelper h = null;
        try {
            if (null != tableIndex && tableIndex.compareTo(BigInteger.ZERO) >= 0) {
                if (offset < 0) {
                    offset = 0L;
                }

                h = new MysqlHelper(dbId);
                array = findTransaction(array, h, tableIndex, offset, offset, dbId, type);
                List<Message> list = array.getList();
                // 查询不到，看是否有下一个表，如果有，继续查询
                if (list == null || list.size() < 1) {
                    tableIndex = tableIndex.add(BigInteger.ONE);
                    // tableIndex跳到下一张表后,offset从零开始
                    array = findTransaction(array, h, tableIndex, 0L, offset, dbId, type);
                }
            }
        } catch (Exception ex) {
            logger.error("error: {}", ex);
        } finally {
            if (h != null) {
                h.destroyed();
            }
        }
        return array;
    }

    /**
     * 用于区块链浏览器 查询所有地址交易
     *
     * @param array
     * @param h
     * @param tableIndex
     * @return
     */
    private TransactionArray findTransaction(TransactionArray array, MysqlHelper h, BigInteger tableIndex, long offset,
                                             long oldOffset, String dbId, Integer type) {
//    	logger.info("findTransaction()...");
        array.setOffset(offset);
        array.setTableIndex(tableIndex);
        // logger.info("tableExist...");
        TransactionSplit split = tableExist(dbId);
        // logger.info("getTableIndex...");
        BigInteger splitIndex = split.getTableIndex();
        logger.info("tableIndex: {}, splitIndex: {}", tableIndex, splitIndex);
        if (tableIndex != null && tableIndex.compareTo(splitIndex) > 0) {
            array.setOffset(oldOffset);
            array.setTableIndex(splitIndex);
            return array;
        }
        String tableName = Config.MESSAGES + Config.SPLIT + tableIndex;
        StringBuilder sql = new StringBuilder("select hash from ");
        sql.append(tableName);
        List<Message> entityArrayList = array.getList();
        int pageSize = PAGE_SIZE_BROWSER;
        // 如果已经查询一次了，则查询减去上次的
        if (entityArrayList != null && entityArrayList.size() > 0) {
            pageSize = PAGE_SIZE_BROWSER - entityArrayList.size();
        }
        if (type != null) {
            sql.append(" where type = ").append(type);
            sql.append(" and id in (select min(id) from messages_0 group by hash)");
        } else {
            sql.append(" where id in (select min(id) from messages_0 group by hash)");
        }
        sql.append(" order by id asc limit ").append(pageSize).append(" offset ").append(offset);
        // logger.info("Blockchain Browser sql==========================" +
        // sql.toString());
        try {
            List<Message> list = h.executeQuery(sql.toString(), (rs, index) -> {
                String hash = rs.getString("hash");
                // logger.info("hash====================" + hash);
                byte[] transationByte = new RocksJavaUtil(dbId).get(hash);
                if (transationByte != null) {
                    // @formatter:off

                    // Franics.Deng 4/10/2019
                    // To work well in crappy environment,the bad cases should been
                    // supported(WWIBETBCSBET).
                    //
                    // bad case of
                    // (transationByte):"32RANBoQucE4DYD7/WmxHhOoZSaSr+2LZvM4/ttbw5TxVhXfRGgyR8C8hN8N70TI3Jk0tiVuLNTxc85v8K12yfHA=="
                    // correct case of
                    // (transationByte):"{"eHash":"MmPmhDIlHwhBc8JYgVgMB517luOEBKW1uup5Xm/7BjwAaUC8Vs+KUenZ0saeM2e4","hash":"32teVCbsQZjXYJLwGdrThycRfUb9FugmJ1f9S+/TVwtMZNLOsHTepO5qqCafWAeYBMOsvw7+FTx5MYnbMEzajKag==","id":"937","isStable":true,"isValid":false,"lastIdx":true,"message":"{\"nrgPrice\":\"1000000000\",\"amount\":\"9000000000000000000\",\"signature\":\"32teVCbsQZjXYJLwGdrThycRfUb9FugmJ1f9S+/TVwtMZNLOsHTepO5qqCafWAeYBMOsvw7+FTx5MYnbMEzajKag==\",\"vers\":\"2.0\",\"fee\":\"500000\",\"fromAddress\":\"5DRZKXVSZ2JEDQ6VCWB4PUMKO4IETLJD\",\"remark\":\"\",\"type\":1,\"toAddress\":\"6GTHF7OFAGTZ6HS6KHLD44HDUC6XCJMG\",\"timestamp\":1552643532150,\"pubkey\":\"Amt323XAyonbYpusWhX4L8viGwz2ILNvuyOeKXmScE/M\"}","updateTime":1552643538398}"
                    // @formatter:on
                    // return JSONArray.parseObject(transationByte, Message.class);

                    try {
                        Message m = JSONArray.parseObject(transationByte, Message.class);
                        return m;
                    } catch (JSONException e) {
                        logger.error("WWIBETBCSBET - transationByte=" + new String(transationByte));
                        return null;
                    }
                } else {
                    logger.error("this hash rocksDB not exist");
                    return null;
                }
            });
            List<Message> entityList = array.getList();
            if (entityList == null) {
                entityList = new ArrayList<Message>();
            }

            array.setTableIndex(tableIndex);
            if (list != null) {
                entityList.addAll(list);
                array.setOffset(offset + list.size());
            } else {
                array.setOffset(offset);
            }
            array.setList(entityList);
        } catch (Exception e) {
            logger.error("error: {}", e);
        }
        return array;
    }

    /**
     * 用于区块链浏览器 SystemAuto表 查询所有地址交易
     *
     * @param array
     * @param h
     * @param tableIndex
     * @return
     */
    private SystemAutoArray querySystemAuto(SystemAutoArray array, MysqlHelper h, BigInteger tableIndex, long offset,
                                            long oldOffset, String dbId) {
//    	logger.info("findTransaction()...");
        array.setOffset(offset);
        array.setTableIndex(tableIndex);
        // logger.info("tableExist...");
        TransactionSplit split = tableSystemAutoExist(dbId);
        // logger.info("getTableIndex...");
        BigInteger splitIndex = split.getTableIndex();
        logger.info("tableIndex: {}, splitIndex: {}", tableIndex, splitIndex);
        if (tableIndex != null && tableIndex.compareTo(splitIndex) > 0) {
            array.setOffset(oldOffset);
            array.setTableIndex(splitIndex);
            return array;
        }
        String tableName = Config.SYSTEMAUTOTX + Config.SPLIT + tableIndex;
        StringBuilder sql = new StringBuilder("select id,type from ");
        sql.append(tableName);
        List<JSONObject> entityArrayList = array.getList();
        int pageSize = PAGE_SIZE_BROWSER;
        // 如果已经查询一次了，则查询减去上次的
        if (entityArrayList != null && entityArrayList.size() > 0) {
            pageSize = PAGE_SIZE_BROWSER - entityArrayList.size();
        }

        sql.append(" order by id asc limit ").append(pageSize).append(" offset ").append(offset);
        // logger.info("Blockchain Browser sql==========================" +
        // sql.toString());
        try {
            List<JSONObject> list = h.executeQuery(sql.toString(), (rs, index) -> {
                String type = rs.getString("type");
                String id = rs.getString("id");
                // logger.info("hash====================" + hash);
                byte[] transationByte = new RocksJavaUtil(dbId).get(type + id);
                if (transationByte != null) {
                    return JSONArray.parseObject(transationByte, JSONObject.class);
                } else {
                    logger.error("this hash rocksDB not exist");
                    return null;
                }
            });
            List<JSONObject> entityList = array.getList();
            if (entityList == null) {
                entityList = new ArrayList<JSONObject>();
            }

            array.setTableIndex(tableIndex);
            if (list != null) {
                entityList.addAll(list);
                array.setOffset(offset + list.size());
            } else {
                array.setOffset(offset);
            }
            array.setList(entityList);
        } catch (Exception e) {
            logger.error("error: {}", e);
        }
        return array;
    }

    private JSONObject querySystemAuto(MysqlHelper h, BigInteger tableIndex, String dbId, String hash) {
        String tableName = Config.SYSTEMAUTOTX + Config.SPLIT + tableIndex;
        StringBuilder sql = new StringBuilder("select id,type from ");
        sql.append(tableName).append(" where mHash = \'").append(hash).append("\'");
        try {
            JSONObject jsonObject = h.executeQuery(sql.toString(), rs -> {
                JSONObject value = null;
                try {
                    if (rs.next()) {
                        byte[] transactionByte = new RocksJavaUtil(dbId).get(rs.getString("type") + rs.getBigDecimal("id"));
                        if (transactionByte != null) {
                            value = JSONObject.parseObject(transactionByte, JSONObject.class);
                        } else {
                            logger.error("this hash rocksDB not exist");
                            value = null;
                        }
                    }
                } catch (SQLException e) {
                    logger.error("error: {}", e);
                }
                return value;
            });
            return jsonObject;
        } catch (Exception e) {
            logger.error("error: {}", e);
        }
        return null;
    }

    private SystemAutoArray querySystemAuto(SystemAutoArray array, MysqlHelper h, BigInteger tableIndex, long offset,
                                            long oldOffset, String dbId, String address, String type) {
        array.setOffset(offset);
        array.setTableIndex(tableIndex);
        TransactionSplit split = tableSystemAutoExist(dbId);
        if (split == null){
            return array;
        }
        BigInteger splitIndex = split.getTableIndex();
        logger.info("tableIndex: {}, splitIndex: {}", tableIndex, splitIndex);
        if (tableIndex != null && tableIndex.compareTo(splitIndex) > 0) {
            array.setOffset(oldOffset);
            array.setTableIndex(splitIndex);
            return array;
        }
        String tableName = Config.SYSTEMAUTOTX + Config.SPLIT + tableIndex;
        StringBuilder sql = new StringBuilder("select id,type from ");
        sql.append(tableName).append(" where 1=1");
        if (StringUtils.isNotBlank(type)) {
            sql.append(" and type =?");
        }
        if (StringUtils.isNotBlank(address)) {
            sql.append(" and ( fromAddress=?");
            sql.append(" or toAddress=? )");
        }
        List<JSONObject> entityArrayList = array.getList();
        int pageSize = PAGE_SIZE_BROWSER;
        // 如果已经查询一次了，则查询减去上次的
        if (entityArrayList != null && entityArrayList.size() > 0) {
            pageSize = PAGE_SIZE_BROWSER - entityArrayList.size();
        }

        sql.append(" order by id asc limit ").append(pageSize).append(" offset ").append(offset);
        // logger.info("Blockchain Browser sql==========================" +
        // sql.toString());
        try {
            PreparedStatement preparedStatement = h.getPreparedStatement(sql.toString());
            if (StringUtils.isNotBlank(type)) {
                preparedStatement.setString(1, type.trim());
            }
            if (StringUtils.isNotBlank(address)) {
                preparedStatement.setString(2, address.trim());
                preparedStatement.setString(3, address.trim());
            }
            ResultSet rs = preparedStatement.executeQuery();
            List<JSONObject> list = new ArrayList<JSONObject>();
            while (rs.next()){
                byte[] transationByte = new RocksJavaUtil(dbId).get(rs.getString("type") + rs.getString("id"));
                if (transationByte != null) {
                    String tx = new String(transationByte);
                    try {
                        list.add(JSONArray.parseObject(transationByte, JSONObject.class));
                    }catch (Exception e){
                        logger.error("systemauto object format is illegal:({})", tx);
                        list.add(new JSONObject());
                    }
                } else {
                    logger.error("this hash rocksDB not exist");
                    return array;
                }

            }
            rs.close();
            preparedStatement.close();
            List<JSONObject> entityList = array.getList();
            if (entityList == null) {
                entityList = new ArrayList<JSONObject>();
            }
            array.setTableIndex(tableIndex);
            if (list != null) {
                entityList.addAll(list);
                array.setOffset(offset + list.size());
            } else {
                array.setOffset(offset);
            }
            array.setList(entityList);
        } catch (Exception e) {
            logger.error("error: {}", e);
        }
        return array;
    }
}
