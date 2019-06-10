package one.inve.localfullnode2.snapshot;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import one.inve.bean.message.SnapshotMessage;
import one.inve.core.EventBody;
import one.inve.localfullnode2.conf.Config;
import one.inve.localfullnode2.store.SnapshotDbService;
import one.inve.localfullnode2.store.rocks.RocksJavaUtil;
import one.inve.localfullnode2.utilities.StringUtils;
import one.inve.localfullnode2.utilities.TxVerifyUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.Instant;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class HandleConsensusSnapshotMessage {
    private static final Logger logger = LoggerFactory.getLogger(HandleConsensusSnapshotMessage.class);

    private HandleConsensusSnapshotMessageDependent dep;
    private SnapshotDbService store;

    public void handleConsensusSnapshotMessage(HandleConsensusSnapshotMessageDependent dep, SnapshotDbService store,
                                               JSONObject msgObject) throws InterruptedException {
        this.dep = dep;
        this.store = store;

        logger.info(">>>>>START<<<<<handleConsensusSnapshotMessage:\n msgObject: {}", msgObject);

//        logger.info("node-({}, {}): Handle Consensus Snapshot Message...msg: {}",
//                node.getShardId(), node.getCreatorId(), msgObject.toJSONString());
        String message = msgObject.getString("msg");
        // 保存快照消息
        SnapshotMessage snapMsg = JSON.parseObject(message, SnapshotMessage.class);
        boolean valid = msgObject.getBoolean("isValid");
        if (valid) {
            int validState = 0;
            try {
                validState = TxVerifyUtils.verifyMessageWithoutSign(
                        JSONObject.parseObject(message),
                        msgObject.getInteger("eShardId"),new RocksJavaUtil(dep.getDbId()),dep.getMultiple(),dep.getShardCount());
            } catch (Exception e) {
//                logger.error("node-({}, {}): Snapshot Message verify error. msgObj:{}, exception: {}",
//                        node.getShardId(), node.getCreatorId(), msgObject.toJSONString(), e);
                logger.error(">>>>>ERROR<<<<<handleConsensusSnapshotMessage:\n error: {}", e);
            }
            if (validState==1) {
                valid = true;
            } else if (validState==2) {
//                logger.error("node-({}, {}): Snapshot message exist, throw away!!! msgObj: {}",
//                        node.getShardId(), node.getCreatorId(), msgObject.toJSONString());
                logger.error(">>>>>RETRUN<<<<<handleConsensusSnapshotMessage:\n snapshotMessage exist, throw away");
                return;
            } else {
                valid = false;
            }
            if (!valid) {
//                logger.error("node-({}, {}): Snapshot Message verify failed. msgObj: {}",
//                        node.getShardId(), node.getCreatorId(), msgObject.toJSONString());
                logger.error(">>>>>ERROR<<<<<handleConsensusSnapshotMessage:\n snapshotMessage verify failed");
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

//        logger.info(">>>>>INFO<<<<<handleConsensusSnapshotMessage:\n consMessageSaveQueue.add: {}",msgObject);

        if (valid) {
//            logger.warn("node-({}, {}): preHash: {}, hash: {}",
//                    node.getShardId(), node.getCreatorId(), snapMsg.getPreHash(), snapMsg.getHash());
            // 处理本快照阶段的所有节点奖励
            handleRewardOfSnapshot(snapMsg, msgObject.getString("eHash"));

            // 清除当前快照之前第 Config.DEFAULT_SNAPSHOT_CLEAR_GENERATION) 个快照的快照点之前的所有Event
          clearHistoryEventsBySnapshot(snapMsg.getSnapVersion(), snapMsg.getPreHash(),dep.getDbId(),dep.getnValue(),
                  dep.getTreeRootMap());

            // 补充更新本地快照消息的部分参数(snapHash, signature, pubkey, timestamp),
            // 避免本地生成下一个快照时getPreHash()为null导致验证快照getPreHash()失败问题问题
            dep.setSnapshotMessage(snapMsg);
        } else {
//            logger.error("node-({}, {}): Snapshot message invalid!!!\nexit...", node.getShardId(),
//                    node.getCreatorId());
            logger.error(">>>>>ERROR<<<<<handleConsensusSnapshotMessage:\n snapshotMessage invalid");
            System.exit(-1);
        }

//        if (logger.isDebugEnabled()) {
//            logger.debug("========= node-({}, {}): snapshot version-{} success.",
//                    node.getShardId(), node.getCreatorId(), snapMsg.getSnapVersion());
//        }
        logger.info(">>>>>END<<<<<handleConsensusSnapshotMessage");
    }

    private boolean verifySnapshotMessage(SnapshotMessage snapshotMessage) {
//        logger.info(" node-({}, {}): vers: {}, \nsnap pubkey: {}, \nfromAddress: {}, \nsnap tree: {}, node tree: {}",
//                node.getShardId(), node.getCreatorId(), snapshotMessage.getSnapVersion(),
//                snapshotMessage.getPubkey(), snapshotMessage.getFromAddress(),
//                snapshotMessage.getSnapshotPoint().getMsgHashTreeRoot(),
//                node.getTreeRootMap().get(snapshotMessage.getSnapVersion()) );

        logger.info(">>>>>START<<<<<verifySnapshotMessage:\n snapshotMessage: {},\n treeRootMap: {}",JSON.toJSONString(snapshotMessage),JSON.toJSONString(dep.getTreeRootMap()));
        boolean verifySnapshotMessage = snapshotMessage.getPubkey().equals(Config.FOUNDATION_PUBKEY)
                && snapshotMessage.getFromAddress().equals(Config.FOUNDATION_ADDRESS)
                && snapshotMessage.getSnapshotPoint().getMsgHashTreeRoot()
                .equals(dep.getTreeRootMap().get(snapshotMessage.getSnapVersion()));
        logger.info(">>>>>RETURN<<<<<verifySnapshotMessage:\n verifySnapshotMessage: {}", verifySnapshotMessage);
        return verifySnapshotMessage;
    }

    private void handleRewardOfSnapshot(SnapshotMessage snapMsg, String eHash) throws InterruptedException {
        logger.info(">>>>>START<<<<<handleRewardOfSnapshot:\n snapshotMessage: {},\n eHash: {}",JSON.toJSONString(snapMsg),
                eHash);
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
                logger.info(">>>>>RETURN<<<<<handleRewardOfSnapshot:\n totalContribution is zero");
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
//                            WorldStateService.getBalanceByAddr(dep.getDbId(),  Config.FOUNDATION_ADDRESS),
//                            entry.getKey(),
//                            WorldStateService.getBalanceByAddr(dep.getDbId(), entry.getKey()),
//                            snapMsg.getSnapVersion(), amount);
//                    WorldStateService.transfer(dep.getDbId(), Config.FOUNDATION_ADDRESS, entry.getKey(), amount);
                    dep.transfer(dep.getDbId(), Config.FOUNDATION_ADDRESS, entry.getKey(), amount);
//                    logger.warn("node-({}, {}): from account {}'s balance={} to account {}'s balance={}  after update state for snapVersion-{} amount={}",
//                            node.getShardId(), node.getCreatorId(), Config.FOUNDATION_ADDRESS,
//                            WorldStateService.getBalanceByAddr(dep.getDbId(),  Config.FOUNDATION_ADDRESS),
//                            entry.getKey(),
//                            WorldStateService.getBalanceByAddr(dep.getDbId(), entry.getKey()),
//                            snapMsg.getSnapVersion(), amount);
                }
            }
        }
        logger.info(">>>>>END<<<<<handleRewardOfSnapshot");
    }

    private void addRewardTx2SaveQueue(String mHash, String toAddress, BigInteger amount, BigInteger snapVersion) throws InterruptedException {
        logger.info(">>>>>START<<<<<addRewardTx2SaveQueue:\n mHash: {},\n toAddress:{},\n amount: {},\n snapVersion: " +
                "{}",mHash,toAddress,amount,snapVersion);
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
        logger.info(">>>>>END<<<<<addRewardTx2SaveQueue:\n systemAutoTxSaveQueue.put: {}",o);
    }

    /**
     * 清除当前快照vers之前第 Config.DEFAULT_SNAPSHOT_CLEAR_GENERATION) 个快照的快照点之前的所有Event
     * @param vers 当前版本
     */
    private void clearHistoryEventsBySnapshot(BigInteger vers, String preHash, String dbId, int nValue,
                                              HashMap<BigInteger, String> treeRootMap) {
        // 快照消息入库
        if (vers.compareTo(BigInteger.valueOf(Config.DEFAULT_SNAPSHOT_CLEAR_GENERATION)) > 0 ) {
            logger.info(">>>>>START<<<<<clearHistoryEventsBySnapshot:\n snapshotVersion: {},\n preHash: {},\n dbId: {}," +
                    "\n nValue: {},\n treeRootMap: {}",vers,preHash,dbId,nValue,treeRootMap);
//            logger.warn("node-({},{}): start to clear history events", node.getShardId(), node.getCreatorId());
            // 查询之前第 Config.DEFAULT_SNAPSHOT_CLEAR_GENERATION) 个快照
            int i = Config.DEFAULT_SNAPSHOT_CLEAR_GENERATION-1;
            while (i>0) {
//                logger.warn("node-({}, {}): Generation: {}, i: {}, preHash: {}",
//                        node.getShardId(), node.getCreatorId(), Config.DEFAULT_SNAPSHOT_CLEAR_GENERATION, i, preHash);
                if (StringUtils.isEmpty(preHash)) {
//                    logger.error("node-({}, {}): snapshot is null. can not delete events...",
//                            node.getShardId(), node.getCreatorId());
                    break;
                } else {
                    SnapshotMessage sm = store.querySnapshotMessageByHash(dbId, preHash);
                    if (null == sm) {
//                        logger.error("node-({}, {}): snapshot is null.", node.getShardId(), node.getCreatorId());
                        break;
                    }
                    preHash = sm.getPreHash();
                    i--;
                    if (i==0) {
                        // 删除其快照点Event之前的所有Event
//                        logger.warn("node-({}, {}): clear event before snap version {}...",
//                                node.getShardId(), node.getCreatorId(), sm.getSnapVersion());
                        store.deleteEventsBeforeSnapshotPointEvent(dbId, sm.getSnapshotPoint().getEventBody(), nValue);
                        // 清除之前版本的treeRootMap
                        treeRootMap.remove(vers.subtract(BigInteger.valueOf(Config.DEFAULT_SNAPSHOT_CLEAR_GENERATION)));
                    }
                }
            }
            if (logger.isDebugEnabled()) {
                logger.debug("========= snapshot message version-{} delete events success.", vers);
            }
            logger.info(">>>>>END<<<<<clearHistoryEventsBySnapshot");
        }
    }

}
