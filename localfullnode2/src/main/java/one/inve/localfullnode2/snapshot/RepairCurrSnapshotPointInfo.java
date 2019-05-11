package one.inve.localfullnode2.snapshot;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import one.inve.bean.message.Contribution;
import one.inve.bean.message.SnapshotPoint;
import one.inve.bean.node.LocalFullNode;
import one.inve.core.EventBody;
import one.inve.localfullnode2.conf.Config;
import one.inve.localfullnode2.snapshot.vo.EventKeyPair;
import one.inve.localfullnode2.store.rocks.INosql;
import one.inve.localfullnode2.store.rocks.INosqlSnapshotImpl;
import one.inve.localfullnode2.store.rocks.RocksJavaUtil;
import one.inve.localfullnode2.utilities.Hash;
import one.inve.localfullnode2.utilities.StringUtils;
import one.inve.utils.DSA;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigInteger;
import java.util.HashSet;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

public class RepairCurrSnapshotPointInfo {
    private static final Logger logger = LoggerFactory.getLogger(RepairCurrSnapshotPointInfo.class);

    private RepairCurrSnapshotPointInfoDependent dep;
    private String msgHashTreeRoot;

    public void repairCurrSnapshotPointInfo(RepairCurrSnapshotPointInfoDependent dep) throws InterruptedException {
        logger.info(">>>>>START<<<<<repairCurrSnapshotPointInfo");
        this.dep = dep;

        SnapshotPoint latestSnapshotPoint = calculateLatestSnapshotPoint();
        EventBody latestSnapshotPointEb = null;
        String latestSnapshotPointEbHash = null;
        EventKeyPair pair0 = null;
        if (latestSnapshotPoint!=null) {
            latestSnapshotPointEb = latestSnapshotPoint.getEventBody();
            latestSnapshotPointEbHash = DSA.encryptBASE64(latestSnapshotPointEb.getHash());
            pair0 = new EventKeyPair(latestSnapshotPointEb.getShardId(),
                    latestSnapshotPointEb.getCreatorId(),
                    latestSnapshotPointEb.getCreatorSeq());
        }
//        logger.error("node-({}, {}): The latest snapshotPoint's {} eventBody-{} hash: {}",
//                node.getShardId(), node.getCreatorId(), node.getCurrSnapshotVersion().subtract(BigInteger.ONE),
//                null==pair0?null:pair0.toString(), latestSnapshotPointEbHash);

        // 模拟全排序线程，排序并恢复contribution
        EventBody[] events = new EventBody[dep.getShardCount()];
        boolean statisFlag = false;
        int allSortEvtSize = 0;
        BigInteger transCount = BigInteger.valueOf(Config.CREATION_TX_LIST.size());
        BigInteger consEventCount = BigInteger.ZERO;
        int l = 0;
        int m = 0;

//        for (int i = 0; i < dep.getShardCount(); i++) {
//            logger.info("node-({}, {}): ShardSortQueue-{} size = {}",
//                    node.getShardId(), node.getCreatorId(), i, node.getShardSortQueue(i).size());
//       }
        while (true) {
            for (int i = 0; i < dep.getShardCount(); i++) {
                if (null == events[i]) {
                    events[i] = dep.getShardSortQueue(i).poll();
                    logger.info(">>>>>INFO<<<<<repairCurrSnapshotPointInfo:\n eventBody[{}]: {}",i,
                            JSON.toJSONString(events[i]));
                    l++;
                }

                if (i == dep.getShardCount() - 1) {
                    EventBody temp = events[0];
                    for (int j = 0; j < events.length; j++) {
                        if (temp == null || null == events[j]) {
//                            logger.warn("node-({}, {}): evtSize={}, allSortEvtSize={}, forCalcuEvtSize={}, " +
//                                            "contribution size: {}",
//                                    node.getShardId(), node.getCreatorId(),
//                                    l, allSortEvtSize, m, node.getContributions().size() );
//                            logger.info("node-({}, {}): repaired msgHashTreeRoot = {}",
//                                    node.getShardId(), node.getCreatorId(), msgHashTreeRoot);
//                            logger.info("node-({}, {}): repaired consEventCount = {}",
//                                    node.getShardId(), node.getCreatorId(), consEventCount);
//                            logger.info("node-({}, {}): repaired transCount = {}",
//                                    node.getShardId(), node.getCreatorId(), transCount);
                            return;
                        } else if (events[j].getConsTimestamp().isBefore(temp.getConsTimestamp())) {
                            // 共识时间戳小的event排在前面
                            temp = events[j];
                            events[j] = null;
                        } else if (events[j].getConsTimestamp().equals(temp.getConsTimestamp()) ) {
                            // 共识时间戳相同的，以分片号小的的event排在前面
                            // 注意：同一个分片的2个共识Event的时间戳必然不相同，否则片内共识就失去意义
                            if (temp.getShardId() > j) {
                                temp = events[j];
                                events[j] = null;
                            }
                        }
                    }
                    if (null != temp) {
                        allSortEvtSize++;
                        if (!statisFlag) {
                            if (null == latestSnapshotPointEb) {
                                // 从0开始，或者从最新快照消息的快照点Event开始
                                statisFlag = true;
                            } else {
                                EventKeyPair pair = new EventKeyPair(temp.getShardId(), temp.getCreatorId(), temp.getCreatorSeq());
//                                logger.error("node-({}, {}): pair0: {}, pair: {}",
//                                        node.getShardId(), node.getCreatorId(), pair0.toString(), pair.toString());
                                if (DSA.encryptBASE64(temp.getHash()).equals(latestSnapshotPointEbHash)) {
                                    // 从0开始，或者从最新快照消息的快照点Event开始
                                    statisFlag = true;
                                    transCount = latestSnapshotPointEb.getTransCount();
                                    consEventCount = latestSnapshotPointEb.getConsEventCount();
                                    logger.info(">>>>>INFO<<<<<repairCurrSnapshotPointInfo:\n transCount: {},\n " +
                                            "consEventCount: {}",transCount,consEventCount);
                                } else {
                                    events[temp.getShardId()] = null;
                                    continue;
                                }
                            }
                        }
                        if (statisFlag ) {
                            EventKeyPair pair = new EventKeyPair(temp.getShardId(), temp.getCreatorId(), temp.getCreatorSeq());
                            if (pair.equals(pair0)) {
                                // 快照点Event滤掉
                                events[temp.getShardId()] = null;
                                continue;
                            }
                            m++;
                            // 修复Contribution
                            Contribution c = new Contribution.Builder()
                                    .shardId(temp.getShardId()).creatorId(temp.getCreatorId())
                                    .otherId(temp.getOtherId()).otherSeq(temp.getOtherSeq())
                                    .build();
                            dep.getContributions().add(c);
                            // 修复msgHashTreeRoot
                            calculateMsgHashTreeRoot(temp);

                            // 没来的及更新入库的共识Event及时入库
                            consEventCount = consEventCount.add(BigInteger.ONE);
                            dep.setTotalConsEventCount(consEventCount);
                            temp.setConsEventCount(dep.getTotalConsEventCount());

                            INosql rocksJavaUtil = new RocksJavaUtil(dep.getDbId());
//                            INosql rocksJavaUtil = new INosqlSnapshotImpl();
                            if(temp.getTrans()!=null) {
                                transCount = transCount.add(BigInteger.valueOf(temp.getTrans().length));
                                rocksJavaUtil.put(Config.EVT_TX_COUNT_KEY, transCount.toString());
//                                logger.warn("node-({}, {}): update transCount: {}",
//                                        node.getShardId(), node.getCreatorId(), transCount);
                            }
                            temp.setTransCount(transCount);

                            byte[] evtByte = rocksJavaUtil.get(pair.toString());
                            if (null == evtByte) {
//                                logger.error("node-({}, {}): missing event-{}",
//                                        node.getShardId(), node.getCreatorId(), pair.toString());
                            } else {
                                String evtStr = new String(evtByte);
                                EventBody evt = JSONObject.parseObject(evtStr, EventBody.class);
                                if (evt.getConsTimestamp() == null || evt.getConsTimestamp().toEpochMilli() <= 0) {
                                    rocksJavaUtil.put(pair.toString(), JSONObject.toJSONString(temp));
                                    rocksJavaUtil.put(Config.CONS_EVT_COUNT_KEY,
                                            dep.getTotalConsEventCount().toString());
                                } else if (!evt.getTransCount().equals(temp.getTransCount())) {
//                                    logger.error("node-({}, {}): event-{}'s transCount diff, calcu: {}, db: {} ",
//                                            node.getShardId(), node.getCreatorId(), pair.toString(),
//                                            temp.getTransCount(), evt.getTransCount());
                                    logger.error(">>>>>ERROR<<<<<repairCurrSnapshotPointInfo:\n transCount diff,\n " +
                                            "calcu: {},db: {}",temp.getTransCount(), evt.getTransCount());
                                    System.exit(-1);
                                } else if (!evt.getConsEventCount().equals(temp.getConsEventCount())) {
//                                    logger.error("node-({}, {}): event-{}'s consEventCount diff, calcu: {}, db: {} ",
//                                            node.getShardId(), node.getCreatorId(), pair.toString(),
//                                            temp.getConsEventCount(), evt.getConsEventCount());
                                    logger.error(">>>>>ERROR<<<<<repairCurrSnapshotPointInfo:\n consEventCount diff,\n " +
                                            "calcu: {},db: {}",temp.getConsEventCount(), evt.getConsEventCount());
                                    System.exit(-1);
                                }
                            }

                            // 没来的及解析入库的message继续入库
                            if (transCount.compareTo(dep.getConsMessageMaxId()) > 0) {
                                int j = 1;
                                int msgCount = temp.getTrans().length;
                                for (byte[] msg : temp.getTrans()) {
                                    dep.setConsMessageMaxId(dep.getConsMessageMaxId().add(BigInteger.ONE));
                                    JSONObject o = new JSONObject();
                                    o.put("id", dep.getConsMessageMaxId());
                                    o.put("eHash", DSA.encryptBASE64(temp.getHash()));
                                    o.put("eShardId", temp.getShardId());
                                    o.put("isStable", true);
                                    o.put("updateTime", temp.getConsTimestamp().toEpochMilli());
                                    o.put("msg", new String(msg));
                                    if (j++ == msgCount) {
                                        o.put("lastIdx", true);
                                    }

                                    try {
                                        dep.getConsMessageVerifyQueue().put(o);
//                                        logger.warn("node-({}, {}): message into ConsMessageVerifyQueue, id: {}",
//                                                node.getShardId(), node.getCreatorId(), node.getConsMessageMaxId());
                                        logger.info(">>>>>INFO<<<<<repairCurrSnapshotPointInfo:\n message into " +
                                                "ConsMessageVerifyQueue: {}",o);
                                    } catch (InterruptedException e) {
                                        logger.error(">>>>>ERROR<<<<<repairCurrSnapshotPointInfo:\n error: {}",e);
                                        e.printStackTrace();
                                    }
                                }
                            }
                            // 没来的及生成的快照点及时生成快照点
                            createSnapshotPoint(temp);
                        }
                        events[temp.getShardId()] = null;
                    }
                }
            }
            logger.info(">>>>>END<<<<<repairCurrSnapshotPointInfo");
        }
    }

    private void createSnapshotPoint(EventBody event) throws InterruptedException {
        logger.info(">>>>>START<<<<<createSnapshotPoint:\n eventBody: {}", JSON.toJSONString(event));
        if (dep.getTotalConsEventCount().mod(BigInteger.valueOf(Config.EVENT_NUM_PER_SNAPSHOT))
                .equals(BigInteger.ZERO)) {
            // 计算并更新贡献
            ConcurrentHashMap<String, Long> statistics = new ConcurrentHashMap<>();
            long[][] effectiveCounts = new long[dep.getShardCount()][dep.getnValue()];
            for (int i=0; i<dep.getShardCount(); i++) {
                for (int j = 0; j < dep.getnValue(); j++) {
                    final int shardId = i;
                    final int creatorId = j;
                    effectiveCounts[i][j] = dep.getContributions().stream()
                            .filter(c -> c.getShardId()==shardId && c.getCreatorId()==creatorId).count();
                    if (effectiveCounts[i][j]>0) {
                        Optional optional = dep.getLocalFullNodes().stream()
                                .filter(n -> n.getShard().equals(""+shardId) && n.getIndex().equals(""+creatorId))
                                .findFirst();
                        statistics.put(((LocalFullNode)optional.get()).getAddress(), effectiveCounts[i][j]);
                    }
                }
            }

            // 生成快照点
            final String eHash = DSA.encryptBASE64(event.getHash());
            if (StringUtils.isEmpty(msgHashTreeRoot)) {
                msgHashTreeRoot = eHash;
            }
            dep.getSnapshotPointMap().put(dep.getCurrSnapshotVersion(), new SnapshotPoint.Builder()
                    .eventBody(event).msgHashTreeRoot(msgHashTreeRoot)
                    .contributions((null!=statistics && statistics.size()<=0) ? null: statistics)
                    .build());
            dep.getTreeRootMap().put(dep.getCurrSnapshotVersion(), msgHashTreeRoot);
            logger.info(">>>>>INFO<<<<<createSnapshotPoint:\n snapshotPointMap: {},\n treeRootMap: {}",
                    JSON.toJSONString(dep.getSnapshotPointMap()), JSON.toJSONString(dep.getTreeRootMap()));

            // 重置消息hash根
            dep.setContributions(new HashSet<>());
            msgHashTreeRoot = null;

            // 增加创建快照触发器
            JSONObject o = new JSONObject();
            // id与前一个ID一样，可以批量排序，且在处理的时候可以根据type是否为空进入特殊受控消息类型处理分支
            o.put("id", dep.getConsMessageMaxId());
            o.put("eHash", eHash);
            o.put("lastIdx", true);
            dep.getConsMessageVerifyQueue().put(o);
            logger.info(">>>>>INFO<<<<<createSnapshotPoint:\n snapshotPointTrigger: {}",o);
        }
        logger.info(">>>>>END<<<<<createSnapshotPoint");
    }

    /**
     * 查询已有的最新快照消息的快照点
     * @return SnapshotPoint
     */
    private SnapshotPoint calculateLatestSnapshotPoint() {
        logger.info(">>>>>START<<<<<calculateLatestSnapshotPoint");
        SnapshotPoint lastSnapshotPoint = null;
        if (null != dep.getSnapshotPointMap()
                && null != dep.getSnapshotPointMap().get(dep.getCurrSnapshotVersion().subtract(BigInteger.ONE))) {
            lastSnapshotPoint
                    = dep.getSnapshotPointMap().get(dep.getCurrSnapshotVersion().subtract(BigInteger.ONE));
        }
        logger.info(">>>>>RETURN<<<<<calculateLatestSnapshotPoint: lastSnapshotPoint: {}",JSON.toJSONString(lastSnapshotPoint));
        return lastSnapshotPoint;
    }

    private void calculateMsgHashTreeRoot(EventBody event) {
        logger.info(">>>>>START<<<<<calculateMsgHashTreeRoot:\n eventBody: {}", JSON.toJSONString(event));
        long eventMsgCount = (null!=event.getTrans() && event.getTrans().length > 0)
                ? event.getTrans().length : 0;
        // 共识消息放入消息签名验证队列
        if (eventMsgCount>0) {
            for (byte[] msg : event.getTrans()) {
                // 计算更新消息hash根
                JSONObject msgObj = JSONObject.parseObject(new String(msg));
                if(StringUtils.isEmpty(msgHashTreeRoot)) {
                    msgHashTreeRoot = DSA.encryptBASE64(Hash.hash(msgObj.getString("signature")));
                } else {
                    msgHashTreeRoot = DSA.encryptBASE64(Hash.hash(msgHashTreeRoot, msgObj.getString("signature")));
                }
            }
        }
        logger.info(">>>>>END<<<<<calculateMsgHashTreeRoot:\n msgHashTreeRoot: {}",msgHashTreeRoot);
    }

}
