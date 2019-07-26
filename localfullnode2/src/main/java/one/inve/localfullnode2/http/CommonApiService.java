package one.inve.localfullnode2.http;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingQueue;

import com.alibaba.fastjson.JSONObject;
import one.inve.localfullnode2.store.rocks.*;
import one.inve.localfullnode2.utilities.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import one.inve.localfullnode2.dep.DepItemsManager;
import one.inve.localfullnode2.dep.items.AllQueues;
import one.inve.localfullnode2.message.service.TransactionDbService;
import one.inve.localfullnode2.nodes.LocalFullNode1GeneralNode;
import one.inve.localfullnode2.staging.StagingArea;
import one.inve.localfullnode2.store.mysql.QueryTableSplit;
import one.inve.localfullnode2.utilities.TxVerifyUtils;

/**
 * Copyright © INVE FOUNDATION. All rights reserved.
 *
 * @Description: TODO
 * @author: Francis.Deng
 * @date: May 13, 2019 2:22:44 AM
 * @version: V1.0
 */
public class CommonApiService {
    private static final Logger logger = LoggerFactory.getLogger(CommonApiService.class);
    private static final Logger mlogger = LoggerFactory.getLogger("M");

    /**
     * 发送交易消息
     *
     * @param message 交易消息
     * @return 成功则返回空串，否则返回错误信息
     */
    public synchronized static String sendMessage(String message, LocalFullNode1GeneralNode node) {
        try {
            if (node.getShardId() == -1L || node.getShardCount() < 1) {
                return "service is not available.";
            }
            if (TxVerifyUtils.verifyMessage(message, node.getMessageHashCache(),
                    new RocksJavaUtil(node.nodeParameters().dbId), node.getShardId(), node.getShardCount())) {
//            if(TxVerifyUtils.verifyMessage(message, node.nodeParameters.dbId, node.getShardId(), node.getShardCount())) {
                logger.debug("receive a transaction, and verify success.");
                int multiple = node.nodeParameters().multiple;
                for (int i = 0; i < multiple; i++) {
                    //
                    // node.getMessageQueue().add(message.getBytes());
                    AllQueues allQueues = DepItemsManager.getInstance().attachAllQueues(null);

                    StagingArea stagingArea = allQueues.get();
                    BlockingQueue<byte[]> messageQueue = stagingArea.getQueue(byte[].class,
                            StagingArea.MessageQueueName);
                    messageQueue.add(message.getBytes());
                    allQueues.set(stagingArea);
                    mlogger.info(node.nodeParameters().dbId + "|" + message);
                    // Francis.Deng 04/26/2019
                    // back up messages from wallet client intentionally
                    // ValuableDataOutputters.getInstance().outputVerifiedMessage(message);
                }

                return "";
            } else {
                logger.error("--- sendMessage() verify failed. \nmsg: {}", message);
                return "verify failed.";
            }
        } catch (Exception e) {
            logger.error("??? sendMessage() verify exception: {} \nmsg: {}", e.getMessage(), message);
            return String.format("verify failed: %s", e.getMessage());
        }
    }

    /**
     * 用于交易地址查询接口
     *
     * @param tableIndex 表ID
     * @param offset     表中偏移量
     * @param address    地址
     * @param type       类型：1交易 2合约 3.快照 4文本
     * @param node       主对象
     * @return 交易列表
     */
    public synchronized static TransactionArray queryTransaction(BigInteger tableIndex, long offset, String address,
                                                                 String type, LocalFullNode1GeneralNode node) {
        QueryTableSplit queryTableSplit = new QueryTableSplit();
        return queryTableSplit.queryTransaction(tableIndex, offset, address, type, node.nodeParameters().dbId);
    }

    /**
     * 用于区块链浏览器
     *
     * @param tableIndex 表ID
     * @param offset     表中偏移量
     * @param type       类型：1交易 2合约 3.快照 4文本
     * @param node       主对象
     * @return 交易列表
     */
    public synchronized static TransactionArray queryTransaction(BigInteger tableIndex, long offset, Integer type,
                                                                 LocalFullNode1GeneralNode node) {
        QueryTableSplit queryTableSplit = new QueryTableSplit();
        return queryTableSplit.queryTransaction(tableIndex, offset, type, node.nodeParameters().dbId);
    }

    /**
     * 用于区块链浏览器 获取SystemAuto表信息
     *
     * @param tableIndex 表ID
     * @param offset     表中偏移量
     * @param node       主对象
     * @return 交易列表
     */
    public synchronized static SystemAutoArray querySystemAuto(BigInteger tableIndex, long offset,
                                                               LocalFullNode1GeneralNode node) {
        QueryTableSplit queryTableSplit = new QueryTableSplit();
        return queryTableSplit.querySystemAuto(tableIndex, offset, node.nodeParameters().dbId);
    }

    public synchronized static MsgArray querySystemAutoToMessageList(BigInteger tableIndex, long offset, LocalFullNode1GeneralNode node, String address) {
        QueryTableSplit queryTableSplit = new QueryTableSplit();
        SystemAutoArray systemAutoArray = queryTableSplit.querySystemAuto(tableIndex, offset, node.nodeParameters().dbId,address,"contract_fee_tx");
        if (systemAutoArray == null || systemAutoArray.getList() == null || systemAutoArray.getList().size() == 0) {
            return null;
        }
        MsgArray msgArray = new MsgArray();
        msgArray.setSysTableIndex(systemAutoArray.getTableIndex());
        msgArray.setSysOffset(systemAutoArray.getOffset());
        List<Message> list = new ArrayList<Message>();
        for (int i = 0; i < systemAutoArray.getList().size(); i++) {
            JSONObject feeTx = systemAutoArray.getList().get(i);
            if (StringUtils.isEmpty(feeTx.getString("mHash"))){
                continue;
            }
            String txHash = feeTx.getString("mHash").split("_")[0] + "_1";
            JSONObject tx = queryTableSplit.querySystemAuto(null, node.nodeParameters().dbId, txHash);
            Message msg = new Message();
            if (tx != null) {
                JSONObject message = new JSONObject();
                message.put("nrgPrice", "1000000000");
                message.put("amount", tx.getBigDecimal("amount").stripTrailingZeros().toPlainString());
                message.put("signature", tx.getString("mHash"));
                message.put("fee", "0");
                message.put("vers", "2.0");
                message.put("fromAddress", tx.getString("fromAddress"));
                message.put("remark", "");
                message.put("type", 2);
                message.put("toAddress", tx.getString("toAddress"));
                message.put("timestamp", tx.getLong("updateTime"));
                message.put("pubkey", "");
                msg.seteHash("");
                msg.setHash(tx.getString("mHash"));
                msg.setId(tx.getBigDecimal("id").toString());
                msg.setStable(true);
                msg.setValid(true);
                msg.setLastIdx(false);
                msg.setUpdateTime(tx.getLong("updateTime"));
                msg.setMessage(message.toString());
                list.add(msg);
            }
        }
        msgArray.setList(list);
        return msgArray;
    }

    public synchronized static MsgArray querySystemAutoToMessageList(BigInteger tableIndex, long offset, LocalFullNode1GeneralNode node) {
        QueryTableSplit queryTableSplit = new QueryTableSplit();
        SystemAutoArray systemAutoArray = queryTableSplit.querySystemAuto(tableIndex, offset, node.nodeParameters().dbId,"contract_fee_tx");
        if (systemAutoArray == null || systemAutoArray.getList() == null || systemAutoArray.getList().size() == 0) {
            return null;
        }
        MsgArray msgArray = new MsgArray();
        msgArray.setSysTableIndex(systemAutoArray.getTableIndex());
        msgArray.setSysOffset(systemAutoArray.getOffset());
        List<Message> list = new ArrayList<Message>();
        for (int i = 0; i < systemAutoArray.getList().size(); i++) {
            JSONObject feeTx = systemAutoArray.getList().get(i);
            if (StringUtils.isEmpty(feeTx.getString("mHash"))){
                continue;
            }
            String txHash = feeTx.getString("mHash").split("_")[0] + "_1";
            JSONObject tx = queryTableSplit.querySystemAuto(null, node.nodeParameters().dbId, txHash);
            Message msg = new Message();
            if (tx != null) {
                JSONObject message = new JSONObject();
                message.put("nrgPrice", "1000000000");
                message.put("amount", tx.getBigDecimal("amount").stripTrailingZeros().toPlainString());
                message.put("signature", tx.getString("mHash"));
                message.put("fee", "0");
                message.put("vers", "2.0");
                message.put("fromAddress", tx.getString("fromAddress"));
                message.put("remark", "");
                message.put("type", 2);
                message.put("toAddress", tx.getString("toAddress"));
                message.put("timestamp", tx.getLong("updateTime"));
                message.put("pubkey", "");
                msg.seteHash("");
                msg.setHash(tx.getString("mHash"));
                msg.setId(tx.getBigDecimal("id").toString());
                msg.setStable(true);
                msg.setValid(true);
                msg.setLastIdx(false);
                msg.setUpdateTime(tx.getLong("updateTime"));
                msg.setMessage(message.toString());
                list.add(msg);
            }
        }
        msgArray.setList(list);
        return msgArray;
    }

    public synchronized static Message querySystemAutoToMessage(LocalFullNode1GeneralNode node, String hash) {
        QueryTableSplit queryTableSplit = new QueryTableSplit();
        JSONObject tx = queryTableSplit.querySystemAuto(null, node.nodeParameters().dbId, hash);
        Message msg = new Message();
        if (tx != null) {
            JSONObject message = new JSONObject();
            message.put("nrgPrice", "1000000000");
            message.put("amount", tx.getBigDecimal("amount").stripTrailingZeros().toPlainString());
            message.put("signature", tx.getString("mHash"));
            message.put("fee", "0");
            message.put("vers", "2.0");
            message.put("fromAddress", tx.getString("fromAddress"));
            message.put("remark", "");
            message.put("type", 2);
            message.put("toAddress", tx.getString("toAddress"));
            message.put("timestamp", tx.getLong("updateTime"));
            message.put("pubkey", "");
            msg.seteHash("");
            msg.setHash(tx.getString("mHash"));
            msg.setId(tx.getBigDecimal("id").toString());
            msg.setStable(true);
            msg.setValid(true);
            msg.setLastIdx(false);
            msg.setUpdateTime(tx.getLong("updateTime"));
            msg.setMessage(message.toString());
        }
        return msg;
    }

    //    /**
//     * get all transactions for light node (wallet).
//     * @param address 收款地址
//     * @return 交易历史记录
//     */
    public static List<Message> getTransactionHistory(String address, LocalFullNode1GeneralNode node) {
        return (new TransactionDbService()).queryTransactionHistory(address, node.nodeParameters().dbId);
    }
//    /**
//     * 根据event的hash获取transaction属于第几张表
//     * @param eHash event的hash值
//     * @return Event所有交易
//     */
//    public String queryTransactionEvent(String eHash) {
//        QueryTableSplit queryTableSplit=new QueryTableSplit();
//        return  queryTableSplit.queryTransactionEvent(eHash, node.getTransDbName());
//    }
//    /**
//     * 获取event交易的数据库文件地址路径
//     * @return event数据库文件路径
//     */
//    public String getDataFileUrl(){
//        return node.eventDbName;
//    }
}
