package one.inve.threads.localfullnode;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import one.inve.beans.dao.TransactionMsg;
import one.inve.beans.dao.TransactionSplit;
import one.inve.core.Config;
import one.inve.db.transaction.MysqlHelper;
import one.inve.db.transaction.NewTableCreate;
import one.inve.node.Main;
import one.inve.rocksDB.RocksJavaUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.sql.PreparedStatement;
import java.time.Duration;
import java.time.Instant;

public class ConsensusSystemAutoTxSaveThread extends Thread {
    private static final Logger logger = LoggerFactory.getLogger(ConsensusSystemAutoTxSaveThread.class);

    private Main node;

    public ConsensusSystemAutoTxSaveThread(Main node) {
        this.node = node;
    }

    StringBuilder statisticInfo = new StringBuilder()
            .append("\n===== node-({}, {}): System Auto tx save thread =====")
            .append("\nhandle cost: {} ms")
            .append("\nSystemAutoTxSaveQueue size: {}");

    @Override
    public void run() {
        logger.info(">>> start ConsensusSystemAutoTxSaveThread...");
        try {
            int i = 0;
            Instant t0 = Instant.now();
            while (true) {
                if ( !node.getSystemAutoTxSaveQueue().isEmpty() ) {
                    saveSystemAutoTx(node.getSystemAutoTxSaveQueue().poll());
                    i++;
                } else {
                    sleep(100);
                    Instant t1 = Instant.now();
                    if (i>0) {
                        // 交易入库
                        logger.info(statisticInfo.toString(),
                                node.getShardId(), node.getCreatorId(),
                                Duration.between(t0, t1).toMillis(),
                                i,
                                node.getSystemAutoTxSaveQueue().size());
                    }
                    t0 = t1;
                    i=0;
                }
            }
        } catch (Exception e) {
            logger.error("EventSaveThread error: {}\nexit...", e);
            System.exit(-1);
        }
    }

    private void saveSystemAutoTx(JSONObject sysAutoTx){
        TransactionMsg msg=new TransactionMsg();

        if(sysAutoTx!=null) {
            try {
                if (sysAutoTx.containsKey("id")) {
                    msg.setId(sysAutoTx.getString("id"));
                }
                if (sysAutoTx.containsKey("type")) {
                    msg.setType(sysAutoTx.getString("type"));
                }
                if (sysAutoTx.containsKey("mHash")) {
                    msg.setmHash( sysAutoTx.getString("mHash"));
                }
                if (sysAutoTx.containsKey("fromAddress")) {
                    msg.setFromAddress(sysAutoTx.getString("fromAddress"));
                }
                if (sysAutoTx.containsKey("toAddress")) {
                    msg.setToAddress( sysAutoTx.getString("toAddress"));
                }
                if (sysAutoTx.containsKey("amount")) {
                    msg.setAmount( sysAutoTx.getBigInteger("amount"));
                }
                if (sysAutoTx.containsKey("updateTime")) {
                    msg.setUpdateTime(sysAutoTx.getLong("updateTime"));
                }
                MysqlHelper mysqlHelper = new MysqlHelper(node.nodeParameters.dbId);
                String id = msg.getId();
                String type = msg.getType();
                String tableName=Config.SYSTEMAUTOTX+Config.SPLIT+"0";
                RocksJavaUtil rocksJavaUtil = new RocksJavaUtil(node.nodeParameters.dbId);
                byte[] bytes=rocksJavaUtil.get(Config.SYSTEMAUTOTX);
                TransactionSplit split=null;
                if(bytes==null){
                    split = new TransactionSplit();
                    split.setTableName(tableName);
                    split.setTableIndex(BigInteger.ZERO);
                    split.setTableNamePrefix(Config.SYSTEMAUTOTX);
                    split.setTotal(1);
                    rocksJavaUtil.put(Config.SYSTEMAUTOTX, JSONArray.toJSONString(split));
                }else{
                    String json = new String(bytes);
                    split = JSONArray.parseObject(json, TransactionSplit.class);
                    if(Config.SYSTEM_SPIT_TOTAL<=split.getTotal()){
                        BigInteger tableIndex=split.getTableIndex().add(BigInteger.ONE);
                        tableName=Config.SYSTEMAUTOTX+Config.SPLIT+tableIndex;
                        split.setTableName(tableName);
                        split.setTableIndex(tableIndex);
                        split.setTableNamePrefix(Config.SYSTEMAUTOTX);
                        split.setTotal(1);
                        //创建system_auto_tx表 1：交易产生的消息，如奖励 2：快照产生的信息 3:合约信息
                        NewTableCreate.createTransactionsMsgTable(mysqlHelper,tableName);
                    }else{
                        tableName=split.getTableName();
                        split.setTotal(split.getTotal()+1);
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

                //添加如rocksDB中
                rocksJavaUtil.put(type + id, JSONArray.toJSONString(sysAutoTx));
                rocksJavaUtil.put(Config.SYS_TX_COUNT_KEY, id);
            } catch (Exception ex) {
                logger.error(">>>>>>>saveSystemAutoTx error: {}", ex);
            }
        }
    }
}