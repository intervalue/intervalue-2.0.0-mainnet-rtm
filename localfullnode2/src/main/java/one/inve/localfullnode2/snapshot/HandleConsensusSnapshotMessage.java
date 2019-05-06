package one.inve.localfullnode2.snapshot;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import one.inve.bean.message.SnapshotMessage;
import one.inve.localfullnode2.conf.Config;
import one.inve.localfullnode2.utilities.StringUtils;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.Instant;
import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class HandleConsensusSnapshotMessage {

    private HandleConsensusSnapshotMessageDependent dep;
    private JSONObject msgObject;
    private String dbId;

    public void handleConsensusSnapshotMessage(HandleConsensusSnapshotMessageDependent dep) throws InterruptedException {
        this.dep = dep;
        this.msgObject = dep.getMsgObject();
        this.dbId = dep.getDbId();

//        logger.info("node-({}, {}): Handle Consensus Snapshot Message...msg: {}",
//                node.getShardId(), node.getCreatorId(), msgObject.toJSONString());
        String message = msgObject.getString("msg");
        // 保存快照消息
        SnapshotMessage snapMsg = JSON.parseObject(message, SnapshotMessage.class);
        boolean valid = msgObject.getBoolean("isValid");
        if (valid) {
            int validState = 0;
            try {
                validState = 1;
//                validState = TxVerifyUtils.verifyMessageWithoutSign(
//                        JSONObject.parseObject(message),
//                        msgObject.getInteger("eShardId"),
//                        node);
            } catch (Exception e) {
//                logger.error("node-({}, {}): Snapshot Message verify error. msgObj:{}, exception: {}",
//                        node.getShardId(), node.getCreatorId(), msgObject.toJSONString(), e);
            }
            if (validState==1) {
                valid = true;
            } else if (validState==2) {
//                logger.error("node-({}, {}): Snapshot message exist, throw away!!! msgObj: {}",
//                        node.getShardId(), node.getCreatorId(), msgObject.toJSONString());
                return;
            } else {
                valid = false;
            }
            if (!valid) {
//                logger.error("node-({}, {}): Snapshot Message verify failed. msgObj: {}",
//                        node.getShardId(), node.getCreatorId(), msgObject.toJSONString());
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
        dep.getConsMessageSaveQueue().add(msgObject);

        if (valid) {
//            logger.warn("node-({}, {}): preHash: {}, hash: {}",
//                    node.getShardId(), node.getCreatorId(), snapMsg.getPreHash(), snapMsg.getHash());
            // 处理本快照阶段的所有节点奖励
            handleRewardOfSnapshot(snapMsg, msgObject.getString("eHash"));

            // 清除当前快照之前第 Config.DEFAULT_SNAPSHOT_CLEAR_GENERATION) 个快照的快照点之前的所有Event
//          DbUtils.clearHistoryEventsBySnapshot(snapMsg.getSnapVersion(), snapMsg.getPreHash(), node);
            // TODO
            dep.clearHistoryEventsBySnapshot(snapMsg.getSnapVersion(), snapMsg.getPreHash());

            // 补充更新本地快照消息的部分参数(snapHash, signature, pubkey, timestamp),
            // 避免本地生成下一个快照时getPreHash()为null导致验证快照getPreHash()失败问题问题
            dep.setSnapshotMessage(snapMsg);
        } else {
//            logger.error("node-({}, {}): Snapshot message invalid!!!\nexit...", node.getShardId(),
//                    node.getCreatorId());
            System.exit(-1);
        }

//        if (logger.isDebugEnabled()) {
//            logger.debug("========= node-({}, {}): snapshot version-{} success.",
//                    node.getShardId(), node.getCreatorId(), snapMsg.getSnapVersion());
//        }
    }

    private boolean verifySnapshotMessage(SnapshotMessage snapshotMessage) {
//        logger.info(" node-({}, {}): vers: {}, \nsnap pubkey: {}, \nfromAddress: {}, \nsnap tree: {}, node tree: {}",
//                node.getShardId(), node.getCreatorId(), snapshotMessage.getSnapVersion(),
//                snapshotMessage.getPubkey(), snapshotMessage.getFromAddress(),
//                snapshotMessage.getSnapshotPoint().getMsgHashTreeRoot(),
//                node.getTreeRootMap().get(snapshotMessage.getSnapVersion()) );

        return snapshotMessage.getPubkey().equals(Config.FOUNDATION_PUBKEY)
                && snapshotMessage.getFromAddress().equals(Config.FOUNDATION_ADDRESS)
                && snapshotMessage.getSnapshotPoint().getMsgHashTreeRoot()
                .equals(dep.getTreeRootMap().get(snapshotMessage.getSnapVersion()));
    }

    private void handleRewardOfSnapshot(SnapshotMessage snapMsg, String eHash) throws InterruptedException {
        // 保存奖励记录
        boolean valid = verifySnapshotMessage(snapMsg);
        if (!valid) {
//            logger.error("========= node-({}, {}): snapshot version-{} verify failed.",
//                    node.getShardId(), node.getCreatorId(), snapMsg.getSnapVersion());
        } else {
//            if (logger.isDebugEnabled()) {
//                logger.debug("========= node-({}, {}): snapshot message version-{} verify success.",
//                        node.getShardId(), node.getCreatorId(), snapMsg.getSnapVersion());
//            }

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
//                    WorldStateService.transfer(dbId, Config.FOUNDATION_ADDRESS, entry.getKey(), amount);
                    dep.transfer(dbId, Config.FOUNDATION_ADDRESS, entry.getKey(), amount);
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
        dep.setSystemAutoTxMaxId(dep.getSystemAutoTxMaxId().add(BigInteger.ONE));
        JSONObject o = new JSONObject();
        o.put("type", "reward_tx");
        o.put("id", dep.getSystemAutoTxMaxId());
        o.put("mHash", mHash);
        o.put("fromAddress", Config.FOUNDATION_ADDRESS);
        o.put("toAddress", toAddress);
        o.put("amount", amount);
        o.put("snapVersion", snapVersion.toString());
        o.put("updateTime", Instant.now().toEpochMilli());
        dep.getSystemAutoTxSaveQueue().put(o);
    }

    public static void main(String[] args) {
        HandleConsensusSnapshotMessageDependent dep = new HandleConsensusSnapshotMessageDependentImpl();
        try {
            new HandleConsensusSnapshotMessage().handleConsensusSnapshotMessage(dep);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
