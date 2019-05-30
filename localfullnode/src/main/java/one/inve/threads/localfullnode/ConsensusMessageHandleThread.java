package one.inve.threads.localfullnode;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import one.inve.bean.message.*;
import one.inve.contract.MVM.InternalTransferData;
import one.inve.contract.MVM.WorldStateService;
import one.inve.core.Config;
import one.inve.exception.InveException;
import one.inve.localfullnode2.snapshot.*;
import one.inve.localfullnode2.store.SnapshotDbService;
import one.inve.localfullnode2.store.SnapshotDbServiceImpl2;
import one.inve.node.Main;
import one.inve.util.DbUtils;
import one.inve.util.StringUtils;
import one.inve.util.TxVerifyUtils;
import one.inve.utils.DSA;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.Duration;
import java.time.Instant;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class ConsensusMessageHandleThread extends Thread {
    private static final Logger logger = LoggerFactory.getLogger(ConsensusMessageHandleThread.class);

    Main node;
    BigInteger vers;
    StringBuilder statisticInfo = new StringBuilder()
            .append("\n===== node-({}, {}): Consensus message handle thread =====")
            .append("\ntotal cost: {} sec\nmessage count: {}\navg cost: {} ms/t")
            .append("\nConsMessageVerifyQueue rest size: {}\nConsMessageHandleQueue rest size: {}")
            .append("\nConsMessageSaveQueue rest size: {}");
    public ConsensusMessageHandleThread(Main node) {
        this.node = node;
        vers = node.getCurrSnapshotVersion();
    }

    @Override
    public void run() {
        logger.info(">>> start ConsensusMessageHandleThread...");
        logger.info("node-({}, {}): latest snap vers = {}, latest msgHashTreeRoot = {}, " +
                        "totalConsEventCount = {}, consMessageMaxId = {}, contributions.size = {}",
                node.getShardId(), node.getCreatorId(), this.vers.subtract(BigInteger.ONE),
                node.getTreeRootMap().get(this.vers.subtract(BigInteger.ONE)),
                node.getTotalConsEventCount(), node.getConsMessageMaxId(), node.getContributions().size());
        Instant t0 = Instant.now();
        Instant t1;
        long handleCount = 0L;
        while (true) {
            try {
                if (!node.getConsMessageHandleQueue().isEmpty()) {
                    // 取共识message
                    JSONObject msgObject = node.getConsMessageHandleQueue().poll();

                    assert msgObject != null;
                    String eHash = msgObject.getString("eHash");
                    Boolean lastIdx = msgObject.getBoolean("lastIdx");
                    if (StringUtils.isNotEmpty(msgObject.getString("msg"))) {
                        JSONObject tm = JSONObject.parseObject(msgObject.getString("msg"));
                        msgObject.put("type", tm.getInteger("type"));
                        if (tm.getInteger("type") == MessageType.TRANSACTIONS.getIndex()) {
                            // 普通交易消息处理
                            handleConsensusTransactionMessage(msgObject);
                        } else if (tm.getInteger("type") == MessageType.CONTRACT.getIndex()) {
                            // 合约消息处理
                            handleConsensusContractMessage(msgObject);
                        } else if (tm.getInteger("type") == MessageType.SNAPSHOT.getIndex()) {
                            // 快照消息处理
//                             handleConsensusSnapshotMessage(msgObject);
                            HandleConsensusSnapshotMessageDependent dep = new HandleConsensusSnapshotMessageDependentImpl2(node,msgObject);
                            SnapshotDbService store = new SnapshotDbServiceImpl2();
                            new HandleConsensusSnapshotMessage().handleConsensusSnapshotMessage(dep,store);

                        } else if (tm.getInteger("type") == MessageType.TEXT.getIndex()) {
                            // 文本数据消息处理
                            handleConsensusTextMessage(msgObject);
                        }  else {
                            logger.error("not supposed message type.");
                        }
                    } else {
//                        SnapshotPoint sp = node.getSnapshotPointMap().get(vers);
//                        //logger.warn("\nspecif msg: {}, \nvers-{} sp: {}",
//                         //       msgObject.toJSONString(), vers, JSON.toJSONString(sp));
//                        if (sp == null) {
//                            //logger.error("node-({}, {}): snapshotPoint-{} missing\nexit...",
//                        //            node.getShardId(), node.getCreatorId(), vers);
//                            System.exit(-1);
//                        } else {
//                            if (null!=lastIdx && lastIdx
//                                    && eHash.equals(DSA.encryptBASE64(sp.getEventBody().getHash()))) {
//                                // 快照点event时，判断是否基金会节点，然后生成快照
//                                handleSnapshotPoint(msgObject.getBigInteger("id"));
//                            } else {
//                                //logger.warn("node-({}, {}): unknown message: {}",
//                             //           node.getShardId(), node.getCreatorId(), msgObject.toJSONString());
//                            }
//                        }

                        HandleSnapshotPointMessageDependent dep = new HandleSnapshotPointMessageDependentImpl2(node,
                                msgObject);
                        new HandleSnapshotPointMessage().handleSnapshotPointMessage(dep);
                    }

                    handleCount++;
                    t1 = Instant.now();
                    long interval = Duration.between(t0, t1).toMillis();
                    if (interval >= 5000) {
                        logger.info(statisticInfo.toString(), node.getShardId(), node.getCreatorId(),
                                interval, handleCount,
                                new BigDecimal(interval).divide(BigDecimal.valueOf(handleCount), 2, BigDecimal.ROUND_HALF_UP),
                                node.getConsMessageVerifyQueue().size(),
                                node.getConsMessageHandleQueue().size(),
                                node.getConsMessageSaveQueue().size());

                        t0 = t1;
                        handleCount = 0L;
                    }
                } else {
                    sleep(50);
                }
            } catch (Exception e) {
                e.printStackTrace();
                logger.error("node-({}, {}): error: {}", node.getShardId(), node.getCreatorId(), e);
            }
        }
    }

    /**
     * 发送地址账户是否金额足够
     * @param fromAddress 发送者地址
     * @param fee 手续费
     * @return 是否双花
     */
    private boolean verifyDoubleCost(String fromAddress, BigInteger fee) {
        return verifyDoubleCost(fromAddress, fee, BigInteger.ZERO);
    }

    /**
     * 发送地址账户是否金额足够
     * @param fromAddress 发送者地址
     * @param fee 手续费
     * @param amount 金额
     * @return 是否双花
     */
    private boolean verifyDoubleCost(String fromAddress, BigInteger fee, BigInteger amount) {
        if (StringUtils.isEmpty(fromAddress) && fromAddress.equals(Config.FOUNDATION_ADDRESS)) {
            return true;
        } else {
            // 发送地址可用余额
            BigInteger fromAddressAvailAtoms = WorldStateService.getBalanceByAddr(node.nodeParameters.dbId, fromAddress);
            logger.info("node-({}, {}): from address {} avail atoms: {}, need cost atoms: {}",
                    node.getShardId(), node.getCreatorId(), fromAddress, fromAddressAvailAtoms, amount);
            // 双花验证
            return fee.equals(BigInteger.ZERO)
                    ? fromAddressAvailAtoms.compareTo(amount) >= 0
                    : fromAddressAvailAtoms.compareTo(fee.add(amount)) >= 0;
        }
    }

    /**
     * 发送地址账户是否金额足够
     * @param fromAddress 发送者地址
     * @param fee 手续费
     * @return 是否双花
     */
    private boolean verifyDoubleCost(String fromAddress, String toAddress, BigInteger fee) {
        return verifyDoubleCost(fromAddress, toAddress, fee, BigInteger.ZERO);
    }

    /**
     * 发送地址账户是否金额足够
     * @param fromAddress 发送者地址
     * @param fee 手续费
     * @param amount 金额
     * @return 是否双花
     */
    private boolean verifyDoubleCost(String fromAddress, String toAddress, BigInteger fee, BigInteger amount) {
        if (verifylegalCreationMessage(fromAddress, toAddress)) {
            return true;
        } else if (verifyIllegalCreationMessage(fromAddress, toAddress)) {
            return false;
        } else {
            // 发送地址可用余额
            BigInteger fromAddressAvailAtoms = WorldStateService.getBalanceByAddr(node.nodeParameters.dbId, fromAddress);
            if (logger.isDebugEnabled()) {
                logger.info("node-({}, {}): from address {} avail atoms: {}, need cost atoms: {}",
                        node.getShardId(), node.getCreatorId(), fromAddress, fromAddressAvailAtoms, amount);
            }
            // 双花验证
            return fee.equals(BigInteger.ZERO)
                    ? fromAddressAvailAtoms.compareTo(amount) >= 0
                    : fromAddressAvailAtoms.compareTo(fee.add(amount)) >= 0;
        }
    }

    private boolean verifylegalCreationMessage(String fromAddress, String toAddress) {
        return Config.GOD_ADDRESS.equals(fromAddress) && Config.CREATION_ADDRESSES.contains(toAddress);
    }
    private boolean verifyIllegalCreationMessage(String fromAddress, String toAddress) {
        return Config.GOD_ADDRESS.equals(fromAddress) && !Config.CREATION_ADDRESSES.contains(toAddress);
    }

    /**
     * 处理共识后的普通交易消息
     * @param msgObject 共识后的普通交易消息
     */
    private void handleConsensusTransactionMessage(JSONObject msgObject) throws InterruptedException {
        Instant t1 = Instant.now();
        String message = msgObject.getString("msg");
        if (logger.isDebugEnabled()) {
            logger.debug("node-({}, {}): TransactionMessage: {}", node.getShardId(), node.getCreatorId(), message);
        }
        // 保存交易消息
        boolean valid = msgObject.getBoolean("isValid");
        TransactionMessage tm = JSONObject.parseObject(message, TransactionMessage.class);
        String fromAddress  = tm.getFromAddress();
        String toAddress  = tm.getToAddress();
        BigInteger fee = tm.getNrgPrice().multiply(tm.getFee());
        if (StringUtils.isNotEmpty(fromAddress)) {
            msgObject.put("fromAddress", fromAddress);
        }
        if (StringUtils.isNotEmpty(fromAddress)) {
            msgObject.put("toAddress", toAddress);
        }
        if (valid) {
            int validState = 0;
            try {
                validState = TxVerifyUtils.verifyMessageWithoutSign(
                        JSONObject.parseObject(message),
                        msgObject.getInteger("eShardId"),
                        node);
            } catch (Exception e) {
                logger.error("node-({}, {}): TextMessage verify error. msgObj: {}, exception: {}",
                        node.getShardId(), node.getCreatorId(), msgObject.toJSONString(), e);
            }
            if (validState==1) {
                valid = true;
            } else if (validState==2) {
                logger.error("node-({}, {}): Transaction message exist, throw away!!! msgObj: {}",
                        node.getShardId(), node.getCreatorId(), msgObject.toJSONString());
                return;
            } else {
                valid = false;
            }
            if (!valid) {
                logger.error("node-({}, {}): Transaction message verify failed. msgObj: {}",
                        node.getShardId(), node.getCreatorId(), msgObject.toJSONString());
            } else if (verifyIllegalCreationMessage(fromAddress, toAddress)) {
                valid = false;
                logger.error("node-({}, {}): Transaction message illegal. msgObj: {}",
                        node.getShardId(), node.getCreatorId(), msgObject.toJSONString());
            } else {
                valid = verifyDoubleCost(fromAddress, toAddress, fee, tm.getAmount());
                if (!valid) {
                    logger.error("node-({}, {}): Transaction message double cost. msgObj: {}",
                            node.getShardId(), node.getCreatorId(), msgObject.toJSONString());
                }
            }
        }
        Instant t2 = Instant.now();
        msgObject.put("hash", tm.getSignature());
        msgObject.put("isValid", valid);
        node.getConsMessageSaveQueue().add(msgObject);

        // 变更世界状态
        if (valid) {
            // 变更世界状态-金额
            WorldStateService.transfer(node.nodeParameters.dbId, fromAddress, toAddress, tm.getAmount());
            // 变更世界状态-收取手续费
            if (fee.compareTo(BigInteger.ZERO) > 0) {
                node.setTotalFeeBetween2Snapshots(node.getTotalFeeBetween2Snapshots().add(fee));
                addTransactionFeeTx2SaveQueue(tm.getSignature(), fromAddress, fee);
                WorldStateService.transfer(node.nodeParameters.dbId, fromAddress, Config.FOUNDATION_ADDRESS, fee);
            }
        }
        long interval = Duration.between(t1, t2).toMillis();
        long interval1 = Duration.between(t2, Instant.now()).toMillis();
        long interval3 = Duration.between(t1, Instant.now()).toMillis();
        logger.warn("node-({}, {}): handleConsensusTransactionMessage() cost: {} ms, verify: {} ms, update world state: {} ms",
                node.getShardId(), node.getCreatorId(), interval3, interval, interval1);
    }
    private void addTransactionFeeTx2SaveQueue(String mHash, String fromAddress, BigInteger fee) throws InterruptedException {
        node.setSystemAutoTxMaxId(node.getSystemAutoTxMaxId().add(BigInteger.ONE));
        JSONObject o = new JSONObject();
        o.put("id", node.getSystemAutoTxMaxId());
        o.put("type", "transaction_fee_tx");
        o.put("mHash", mHash);
        o.put("fromAddress", fromAddress);
        o.put("toAddress", Config.FOUNDATION_ADDRESS);
        o.put("amount", fee);
        o.put("updateTime", Instant.now().toEpochMilli());
        node.getSystemAutoTxSaveQueue().put(o);
    }

    /**
     * 处理共识后的文本数据消息
     * @param msgObject 共识后的文本数据消息
     */
    private void handleConsensusTextMessage(JSONObject msgObject) throws InterruptedException {
        String message = msgObject.getString("msg");
        if (logger.isDebugEnabled()) {
            logger.debug("node-({}, {}): TextMessage: {}", node.getShardId(), node.getCreatorId(), message);
        }
        // 保存文本消息
        boolean valid = msgObject.getBoolean("isValid");
        TextMessage tm = JSONObject.parseObject(message, TextMessage.class);
        String fromAddress  = tm.getFromAddress();
        String toAddress  = tm.getToAddress();
        BigInteger fee = tm.getNrgPrice().multiply(tm.getFee());
        if (StringUtils.isNotEmpty(fromAddress)) {
            msgObject.put("fromAddress", fromAddress);
        }
        if (StringUtils.isNotEmpty(fromAddress)) {
            msgObject.put("toAddress", toAddress);
        }
        if (valid) {
            int validState = 0;
            try {
                validState = TxVerifyUtils.verifyMessageWithoutSign(
                        JSONObject.parseObject(message),
                        msgObject.getInteger("eShardId"),
                        node);
            } catch (Exception e) {
                logger.error("node-({}, {}): Text Message verify error. msgObj: {}, exception: {}",
                        node.getShardId(), node.getCreatorId(), msgObject.toJSONString(), e);
            }
            if (validState==1) {
                valid = true;
            } else if (validState==2) {
                logger.error("node-({}, {}): Text message exist, throw away!!! msgObj: {}",
                        node.getShardId(), node.getCreatorId(), msgObject.toJSONString());
                return;
            } else {
                valid = false;
            }
            if (!valid) {
                logger.error("node-({}, {}): Text Message verify failed. msgObj: {}",
                        node.getShardId(), node.getCreatorId(), msgObject.toJSONString());
            } else if (verifylegalCreationMessage(fromAddress, toAddress)) {
                valid = false;
                logger.error("node-({}, {}): Text Message illegal. msgObj: {}",
                        node.getShardId(), node.getCreatorId(), msgObject.toJSONString());
            } else {
                if (fee.compareTo(BigInteger.ZERO) > 0) {
                    valid = verifyDoubleCost(fromAddress, toAddress, fee);
                    if (!valid) {
                        logger.error("node-({}, {}): Text Message double cost. msgObj: {}",
                                node.getShardId(), node.getCreatorId(), msgObject.toJSONString());
                    }
                }
            }
        }
        msgObject.put("hash", tm.getSignature());
        msgObject.put("isValid", valid);
        node.getConsMessageSaveQueue().add(msgObject);

        // 变更世界状态
        if (valid) {
            if (fee.compareTo(BigInteger.ZERO) > 0) {
                // 变更世界状态-收取手续费
                node.setTotalFeeBetween2Snapshots(node.getTotalFeeBetween2Snapshots().add(fee));
                addTextFeeTx2SaveQueue(tm.getSignature(), fromAddress, fee);
                WorldStateService.transfer(node.nodeParameters.dbId, fromAddress, Config.FOUNDATION_ADDRESS, fee);
            }
        }
    }
    private void addTextFeeTx2SaveQueue(String mHash, String fromAddress, BigInteger fee) throws InterruptedException {
        node.setSystemAutoTxMaxId(node.getSystemAutoTxMaxId().add(BigInteger.ONE));
        JSONObject o = new JSONObject();
        o.put("id", node.getSystemAutoTxMaxId());
        o.put("type", "transaction_fee_tx");
        o.put("mHash", mHash);
        o.put("fromAddress", fromAddress);
        o.put("toAddress", Config.FOUNDATION_ADDRESS);
        o.put("amount", fee);
        o.put("updateTime", Instant.now().toEpochMilli());
        node.getSystemAutoTxSaveQueue().put(o);
    }

    /**
     * 处理共识后的智能合约消息
     * @param msgObject 共识后的智能合约消息
     */
    private void handleConsensusContractMessage(JSONObject msgObject) throws InterruptedException {
        logger.info("node-({}, {}): Handle Consensus Contract Message...", node.getShardId(), node.getCreatorId());
        String message = msgObject.getString("msg");
        if (logger.isDebugEnabled()) {
            logger.debug("node-({}, {}): ContractMessage: {}", node.getShardId(), node.getCreatorId(), message);
        }
        ContractMessage cm = JSON.parseObject(message, ContractMessage.class);
        String fromAddress  = cm.getFromAddress();
        boolean valid = msgObject.getBoolean("isValid");
        List<InternalTransferData> list = null;
        if (valid) {
            int validState = 0;
            try {
                validState = TxVerifyUtils.verifyMessageWithoutSign(
                        JSONObject.parseObject(message),
                        msgObject.getInteger("eShardId"),
                        node);
            } catch (Exception e) {
                logger.error("node-({}, {}): Contract Message verify error. msgObj: {}, exception: {}",
                        node.getShardId(), node.getCreatorId(), msgObject.toJSONString(), e);
            }
            if (validState==1) {
                valid = true;
            } else if (validState==2) {
                logger.error("node-({}, {}): Contract message exist, throw away!!! msgObj: {}",
                        node.getShardId(), node.getCreatorId(), msgObject.toJSONString());
                return;
            } else {
                valid = false;
            }
            if (!valid) {
                logger.error("node-({}, {}): Contract Message verify failed. msgObj: {}",
                        node.getShardId(), node.getCreatorId(), msgObject.toJSONString());
            } else {
                // 执行智能合约
                list = WorldStateService.executeContractMessage(node.nodeParameters.dbId, cm);
                valid = null != list && list.size() > 0;
            }
        }
        // 保存智能合约消息
        if (StringUtils.isNotEmpty(fromAddress)) {
            msgObject.put("fromAddress", fromAddress);
        }
        msgObject.put("hash", cm.getSignature());
        msgObject.put("isValid", valid);
        node.getConsMessageSaveQueue().add(msgObject);

        if (valid) {
            // 合约执行产生的交易入库
            boolean needRecordFee = true;
            for(InternalTransferData data : list) {
                if (needRecordFee) {
                    if (data.getFee().compareTo(BigInteger.ZERO) > 0) {
                        // 扣除和收集手续费
                        node.setTotalFeeBetween2Snapshots(node.getTotalFeeBetween2Snapshots().add(data.getFee()));
                        addContractFeeTx2SaveQueue(cm.getHash(), data);
                    }
                    needRecordFee = false;
                }
                // 合约执行产生的交易入库
                addContractTx2SaveQueue(cm.getHash(), data);
            }
        }
    }
    private void addContractFeeTx2SaveQueue(String mHash, InternalTransferData data) throws InterruptedException {
        node.setSystemAutoTxMaxId(node.getSystemAutoTxMaxId().add(BigInteger.ONE));
        JSONObject o = new JSONObject();
        o.put("id", node.getSystemAutoTxMaxId());
        o.put("type", "contract_fee_tx");
        o.put("mHash", mHash);
        o.put("fromAddress", data.getFromAddress());
        o.put("toAddress", Config.FOUNDATION_ADDRESS);
        o.put("amount", data.getFee());
        o.put("updateTime", Instant.now().toEpochMilli());
        node.getSystemAutoTxSaveQueue().put(o);
    }
    private void addContractTx2SaveQueue(String mHash, InternalTransferData data) throws InterruptedException {
        node.setSystemAutoTxMaxId(node.getSystemAutoTxMaxId().add(BigInteger.ONE));
        JSONObject o = new JSONObject();
        o.put("id", node.getSystemAutoTxMaxId());
        o.put("type", "contract_tx");
        o.put("mHash", mHash);
        o.put("fromAddress", data.getFromAddress());
        o.put("toAddress", data.getToAddress());
        o.put("amount", data.getValue());
        o.put("updateTime", Instant.now().toEpochMilli());
        node.getSystemAutoTxSaveQueue().put(o);
    }

    /**
     * 处理共识后的快照消息
     * @param msgObject 共识后的快照消息
     */
    private void handleConsensusSnapshotMessage(JSONObject msgObject) throws InterruptedException {
        logger.info("node-({}, {}): Handle Consensus Snapshot Message...msg: {}",
                node.getShardId(), node.getCreatorId(), msgObject.toJSONString());
        String message = msgObject.getString("msg");
        // 保存快照消息
        SnapshotMessage snapMsg = JSON.parseObject(message, SnapshotMessage.class);
        boolean valid = msgObject.getBoolean("isValid");
        if (valid) {
            int validState = 0;
            try {
                validState = TxVerifyUtils.verifyMessageWithoutSign(
                        JSONObject.parseObject(message),
                        msgObject.getInteger("eShardId"),
                        node);
            } catch (Exception e) {
                logger.error("node-({}, {}): Snapshot Message verify error. msgObj:{}, exception: {}",
                        node.getShardId(), node.getCreatorId(), msgObject.toJSONString(), e);
            }
            if (validState==1) {
                valid = true;
            } else if (validState==2) {
                logger.error("node-({}, {}): Snapshot message exist, throw away!!! msgObj: {}",
                        node.getShardId(), node.getCreatorId(), msgObject.toJSONString());
                return;
            } else {
                valid = false;
            }
            if (!valid) {
                logger.error("node-({}, {}): Snapshot Message verify failed. msgObj: {}",
                        node.getShardId(), node.getCreatorId(), msgObject.toJSONString());
            } else {
                valid = verifySnapshotMessage(snapMsg);
            }
        }
        String fromAddress  = snapMsg.getFromAddress();
        if (StringUtils.isNotEmpty(fromAddress)) {
            msgObject.put("fromAddress", fromAddress);
        }
        msgObject.put("isValid", valid);
        msgObject.put("hash", snapMsg.getSignature());
        msgObject.put("snapVersion", snapMsg.getSnapVersion().toString());
        node.getConsMessageSaveQueue().add(msgObject);

        if (valid) {
            logger.warn("node-({}, {}): preHash: {}, hash: {}",
                    node.getShardId(), node.getCreatorId(), snapMsg.getPreHash(), snapMsg.getHash());
            // 处理本快照阶段的所有节点奖励
            handleRewardOfSnapshot(snapMsg, msgObject.getString("eHash"));

            // 清除当前快照之前第 Config.DEFAULT_SNAPSHOT_CLEAR_GENERATION) 个快照的快照点之前的所有Event
            DbUtils.clearHistoryEventsBySnapshot(snapMsg.getSnapVersion(), snapMsg.getPreHash(), node);

            // 补充更新本地快照消息的部分参数(snapHash, signature, pubkey, timestamp),
            // 避免本地生成下一个快照时getPreHash()为null导致验证快照getPreHash()失败问题问题
            node.setSnapshotMessage(snapMsg);
        } else {
            logger.error("node-({}, {}): Snapshot message invalid!!!\nexit...", node.getShardId(), node.getCreatorId());
            System.exit(-1);
        }

        if (logger.isDebugEnabled()) {
            logger.debug("========= node-({}, {}): snapshot version-{} success.",
                    node.getShardId(), node.getCreatorId(), snapMsg.getSnapVersion());
        }
    }

    /**
     * 处理本快照阶段的所有节点奖励
     * @param snapMsg 快照消息
     */
    private void handleRewardOfSnapshot(SnapshotMessage snapMsg, String eHash) throws InterruptedException {
        // 保存奖励记录
        boolean valid = verifySnapshotMessage(snapMsg);
        if (!valid) {
            logger.error("========= node-({}, {}): snapshot version-{} verify failed.",
                    node.getShardId(), node.getCreatorId(), snapMsg.getSnapVersion());
        } else {
            if (logger.isDebugEnabled()) {
                logger.debug("========= node-({}, {}): snapshot message version-{} verify success.",
                        node.getShardId(), node.getCreatorId(), snapMsg.getSnapVersion());
            }

            // 发放奖励
            ConcurrentHashMap<String, Long> contributions = snapMsg.getSnapshotPoint().getContributions();
            Collection<Long> cValues = contributions.values();
            // 计算总贡献数，当总贡献数量为0时，即无需任何奖励，则直接结束，否则根据每个地址占总贡献比例计算奖励
            BigDecimal totalContribution = BigDecimal.ZERO;
            for (Long v : cValues) {
                if (null != v) {
                    totalContribution.add(BigDecimal.valueOf(v));
                }
            }
            if (totalContribution.equals(BigDecimal.ZERO)) {
                return;
            }

            double rewardRatio = snapMsg.getSnapshotPoint().getRewardRatio();
            BigInteger totalFee = snapMsg.getSnapshotPoint().getTotalFee();
            for (Map.Entry<String, Long> entry : contributions.entrySet()) {
                if (null != entry.getValue()) {
                    BigInteger amount = new BigDecimal("" + rewardRatio).multiply(new BigDecimal(totalFee))
                            .multiply(BigDecimal.valueOf(entry.getValue()))
                            .divide(totalContribution, 0, BigDecimal.ROUND_HALF_UP).toBigInteger();
                    if (amount.equals(BigInteger.ZERO)) {
                        continue;
                    }
                    // 记录发放奖励金交易
                    addRewardTx2SaveQueue(eHash, entry.getKey(), amount, snapMsg.getSnapVersion());

                    // 变更世界状态-发放节点奖励金
//                    logger.warn("node-({}, {}): from account {}'s balance={} to account {}'s balance={} after update state for snapVersion-{} amount={}",
//                            node.getShardId(), node.getCreatorId(), Config.FOUNDATION_ADDRESS,
//                            WorldStateService.getBalanceByAddr(node.nodeParameters.dbId,  Config.FOUNDATION_ADDRESS),
//                            entry.getKey(),
//                            WorldStateService.getBalanceByAddr(node.nodeParameters.dbId, entry.getKey()),
//                            snapMsg.getSnapVersion(), amount);
                    WorldStateService.transfer(node.nodeParameters.dbId, Config.FOUNDATION_ADDRESS, entry.getKey(), amount);
//                    logger.warn("node-({}, {}): from account {}'s balance={} to account {}'s balance={}  after update state for snapVersion-{} amount={}",
//                            node.getShardId(), node.getCreatorId(), Config.FOUNDATION_ADDRESS,
//                            WorldStateService.getBalanceByAddr(node.nodeParameters.dbId,  Config.FOUNDATION_ADDRESS),
//                            entry.getKey(),
//                            WorldStateService.getBalanceByAddr(node.nodeParameters.dbId, entry.getKey()),
//                            snapMsg.getSnapVersion(), amount);
                }
            }
        }
    }
    private void addRewardTx2SaveQueue(String mHash, String toAddress, BigInteger amount, BigInteger snapVersion) throws InterruptedException {
        // 生成奖励交易信息进入入库队列
        node.setSystemAutoTxMaxId(node.getSystemAutoTxMaxId().add(BigInteger.ONE));
        JSONObject o = new JSONObject();
        o.put("type", "reward_tx");
        o.put("id", node.getSystemAutoTxMaxId());
        o.put("mHash", mHash);
        o.put("fromAddress", Config.FOUNDATION_ADDRESS);
        o.put("toAddress", toAddress);
        o.put("amount", amount);
        o.put("snapVersion", snapVersion.toString());
        o.put("updateTime", Instant.now().toEpochMilli());
        node.getSystemAutoTxSaveQueue().put(o);
    }

    /**
     * 处理快照点事件，如果是基金会节点，则负责生成snapshotMessage，放入messageQueue
     */
    private void handleSnapshotPoint(BigInteger maxMessageId) throws Exception {
        // 快照事件，则生成快照消息并丢入队列
        createSnapshotMessage(node.getSnapshotPointMap().get(vers), maxMessageId);
        // 恢复参数状态
        node.getSnapshotPointMap().remove(vers);
        node.setTotalFeeBetween2Snapshots(BigInteger.ZERO);

        logger.info("node-({}, {}): handle snapshotPoint-{} finished!", node.getShardId(), node.getCreatorId(), vers);
        vers = vers.add(BigInteger.ONE);
    }

    /**
     * 生成新快照消息
     *
     * @param snapshotPoint 快照点
     * @throws InveException 异常
     */
    private void createSnapshotMessage(SnapshotPoint snapshotPoint, BigInteger maxMessageId) throws Exception {
        logger.info("====== node-({}, {}): createSnapshotMessage...", node.getShardId(), node.getCreatorId());
        // 快照消息丢入队列
        if ( Config.FOUNDATION_PUBKEY.equals(node.getWallet().getExtKeys().getPubKey()) ) {
            BigInteger totalFee = node.getTotalFeeBetween2Snapshots();
            snapshotPoint.setMsgMaxId(maxMessageId);
            snapshotPoint.setTotalFee(totalFee);
            snapshotPoint.setRewardRatio(Config.NODE_REWARD_RATIO);
            // 构造快照消息
            SnapshotMessage snapshotMessage = new SnapshotMessage(
                    node.getWallet().getMnemonic(), node.getWallet().getAddress(),
                    vers, getPreHash(), snapshotPoint);

            // 加入消息队列
            String msg = snapshotMessage.getMessage();
            logger.info("node-({}, {}): new version-{}, snapshotMsg: {}",
                    node.getShardId(), node.getCreatorId(), vers, msg);

            node.getMessageQueue().add(JSON.parseObject(msg).getString("message").getBytes());
        } else {
            logger.warn("node-({}, {}): new version-{}, no permission!!",
                    node.getShardId(), node.getCreatorId(), vers);
        }
    }

    /**
     * 获取上一快照版本的hash
     * @return  上一快照版本的hash
     */
    private String getPreHash() {
        SnapshotMessage preSnapshotMessage = node.getSnapshotMessage();
        String preHash;
        if (null == preSnapshotMessage) {
            logger.warn("\n====== node-({}, {}): preSnapshotMessage: null", node.getShardId(), node.getCreatorId());
            preHash = null;
        } else if (null == preSnapshotMessage.getPreHash()) {
            logger.warn(
                    "\n====== node-({}, {}): preSnapshotMessage.version: {}, preSnapshotMessage.snapHash: {}, preSnapshotMessage.getPreHash(): {}",
                    node.getShardId(), node.getCreatorId(), preSnapshotMessage.getSnapVersion(), preSnapshotMessage.getHash(),
                    preSnapshotMessage.getPreHash());
            preHash = preSnapshotMessage.getHash();
        } else {
            preHash = preSnapshotMessage.getHash();
        }
        return preHash;
    }

    /**
     * 验证快照消息
     *
     * @param snapshotMessage 快照消息
     * @return true-通过验证, false-验证失败
     */
    private boolean verifySnapshotMessage(SnapshotMessage snapshotMessage) {
        logger.info(" node-({}, {}): vers: {}, \nsnap pubkey: {}, \nfromAddress: {}, \nsnap tree: {}, node tree: {}",
                node.getShardId(), node.getCreatorId(), snapshotMessage.getSnapVersion(),
                snapshotMessage.getPubkey(), snapshotMessage.getFromAddress(),
                snapshotMessage.getSnapshotPoint().getMsgHashTreeRoot(),
                node.getTreeRootMap().get(snapshotMessage.getSnapVersion()) );

        return snapshotMessage.getPubkey().equals(Config.FOUNDATION_PUBKEY)
                && snapshotMessage.getFromAddress().equals(Config.FOUNDATION_ADDRESS)
                && snapshotMessage.getSnapshotPoint().getMsgHashTreeRoot()
                    .equals(node.getTreeRootMap().get(snapshotMessage.getSnapVersion()));
    }
}

