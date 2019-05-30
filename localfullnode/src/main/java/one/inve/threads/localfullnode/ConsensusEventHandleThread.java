package one.inve.threads.localfullnode;

import com.alibaba.fastjson.JSONObject;
import one.inve.bean.message.Contribution;
import one.inve.bean.message.SnapshotPoint;
import one.inve.bean.node.LocalFullNode;
import one.inve.core.Config;
import one.inve.core.EventBody;
import one.inve.core.EventKeyPair;
import one.inve.core.Hash;
import one.inve.localfullnode2.snapshot.CreateSnapshotPoint;
import one.inve.localfullnode2.snapshot.CreateSnapshotPointDependent;
import one.inve.localfullnode2.snapshot.CreateSnapshotPointDependentImpl2;
import one.inve.node.Main;
import one.inve.rocksDB.RocksJavaUtil;
import one.inve.util.StringUtils;
import one.inve.utils.DSA;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigInteger;
import java.time.Duration;
import java.time.Instant;
import java.util.HashSet;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

public class ConsensusEventHandleThread extends Thread {
    private static final Logger logger = LoggerFactory.getLogger(ConsensusEventHandleThread.class);

    StringBuilder statisticInfo = new StringBuilder()
            .append("\n===== node-({}, {}): Consensus event handle thread =====")
            .append("\ntotal cost: {} ms\nevent count: {}")
            .append("\nConsEventHandleQueue rest size: {}\nConsMessageVerifyQueue rest size: {}");

    Main node;
    int selfId = -1;
    BigInteger vers;
    // 消息hash根
    String msgHashTreeRoot = null;
    private BigInteger transCount;
    public ConsensusEventHandleThread(Main node) {
        this.node = node;
        this.selfId = (int)node.getCreatorId();

        this.msgHashTreeRoot = node.msgHashTreeRoot;
        SnapshotPoint sp = node.getSnapshotPointMap().get(node.getCurrSnapshotVersion());
        if (null!=sp) {
            this.vers = node.getCurrSnapshotVersion().add(BigInteger.ONE);
            logger.warn("node-({}, {}): snapshotpoint repaired, new vers: ",
            node.getShardId(), node.getCreatorId(), this.vers);
        } else {
            this.vers = node.getCurrSnapshotVersion();
        }

        this.transCount = node.getConsMessageMaxId();
    }

    @Override
    public void run() {
        logger.info(">>> start ConsensusEventHandleThread...");
        logger.info("node-({}, {}): curr snap vers = {}, transCount = {}, " +
                        "totalConsEventCount = {}, contributions.size = {}, curr msgHashTreeRoot = {}",
                node.getShardId(), node.getCreatorId(), this.vers, this.transCount,
                node.getTotalConsEventCount(), node.getContributions().size(), this.msgHashTreeRoot);

        Instant t0 = Instant.now();
        Instant t1;
        long eventCount = 0L;
        while (true) {
            while (-1==selfId) {
                // 节点在片内的ID不存在，在一直等待
                try {
                    sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }

            try {
                if (!node.getConsEventHandleQueue().isEmpty()) {
                    // 取共识Event
                    EventBody event = node.getConsEventHandleQueue().poll();
                    // 更新共识Event数
                    node.setTotalConsEventCount(node.getTotalConsEventCount().add(BigInteger.ONE));
                    // 累计各分片各节点event数
                    node.getContributions().add(new Contribution.Builder()
                            .shardId(event.getShardId()).creatorId(event.getCreatorId())
                            .otherId(event.getOtherId()).otherSeq(event.getOtherSeq())
                            .build());

                    // 保存共识Event
                    saveConsEvent(event);

                    // 将Event打包的交易放入待签名验证共识消息队列，并计算更新本快照版本的所有消息hash根
                    addConsMessage2VerifyQueue(event);

                    // 达到生成快照点条件，则生成快照点
//                    createSnapshotPoint(event);

                    CreateSnapshotPointDependent dep = new CreateSnapshotPointDependentImpl2(node,event);
                    new CreateSnapshotPoint().createSnapshotPoint(dep);

                    // 打印信息
                    eventCount++;
                    t1 = Instant.now();
                    long interval = Duration.between(t0, t1).toMillis();
                    if ( interval > 5000) {
                        logger.info(statisticInfo.toString(), node.getShardId(), node.getCreatorId(),
                                interval, eventCount,
                                node.getConsEventHandleQueue().size(),
                                node.getConsMessageVerifyQueue().size());

                        t0 = t1;
                        eventCount = 0L;
                    }
                } else {
                    sleep(50);
                }
            } catch (Exception e) {
                e.printStackTrace();
                logger.error("error: {}", e);
            }
        }
    }

    /**
     * 保存共识Event
     * @param event 保存共识event
     */
    private void saveConsEvent(EventBody event) {
        RocksJavaUtil rocksJavaUtil = new RocksJavaUtil(node.nodeParameters.dbId);
        //计算并set eventBody的最大transaction的ID
        if(event.getTrans()!=null){
            transCount = transCount.add(BigInteger.valueOf(event.getTrans().length)) ;
            rocksJavaUtil.put(Config.EVT_TX_COUNT_KEY, transCount.toString());
        }
        event.setTransCount(transCount);
        event.setConsEventCount(node.getTotalConsEventCount());

        EventKeyPair pair = new EventKeyPair(event.getShardId(), event.getCreatorId(), event.getCreatorSeq());
        rocksJavaUtil.put(pair.toString(), JSONObject.toJSONString(event));
        rocksJavaUtil.put(Config.CONS_EVT_COUNT_KEY, node.getTotalConsEventCount().toString());
    }

    /**
     * 将Event打包的交易放入待签名验证共识消息队列，并计算更新本快照版本的所有消息hash根
     * @param event 共识Event
     */
    private void addConsMessage2VerifyQueue(EventBody event) {

        long eventMsgCount = (null!=event.getTrans() && event.getTrans().length > 0)
                ? event.getTrans().length: 0;
        if (logger.isDebugEnabled()) {
            if (eventMsgCount > 0) {
                logger.debug("node-({}, {}): consensus message size: {}",
                        node.getShardId(), node.getCreatorId(), eventMsgCount);
            }
        }
        // 共识消息放入消息签名验证队列
        if (eventMsgCount>0) {
            final String eHash = DSA.encryptBASE64(event.getHash());
            int j = 1;
            int msgCount = event.getTrans().length;

            logger.error(">>>>>>before event.getTrans() " + event.getTrans().length);

            for (byte[] msg : event.getTrans()) {
                node.setConsMessageMaxId(node.getConsMessageMaxId().add(BigInteger.ONE));
                JSONObject o = new JSONObject();
                logger.error(">>>>>>before node.getConsMessageMaxId():"+node.getConsMessageMaxId());
                o.put("id", node.getConsMessageMaxId());
                o.put("eHash", eHash);
                o.put("eShardId", event.getShardId());
                o.put("isStable", true);
                o.put("updateTime", event.getConsTimestamp().toEpochMilli());
                o.put("msg", new String(msg));
                if (j++ == msgCount) {
                    o.put("lastIdx", true);
                }
//                logger.warn("id: {}", o.getString("id"));

                // 计算更新消息hash根
                JSONObject msgObj = JSONObject.parseObject(new String(msg));
                if(StringUtils.isEmpty(node.msgHashTreeRoot)) {
//                    msgHashTreeRoot = DSA.encryptBASE64(Hash.hash(msgObj.getString("signature")));
                    node.msgHashTreeRoot = DSA.encryptBASE64(Hash.hash(msgObj.getString("signature")));
                } else {
//                    msgHashTreeRoot = DSA.encryptBASE64(Hash.hash(msgHashTreeRoot, msgObj.getString("signature")));
                    node.msgHashTreeRoot = DSA.encryptBASE64(Hash.hash(node.msgHashTreeRoot, msgObj.getString("signature")));
                }

                try {
                    node.getConsMessageVerifyQueue().put(o);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * 到达生成快照点条件，则生成快照点
     * @param event 共识事件
     */
    private void createSnapshotPoint(EventBody event) throws InterruptedException {
//        logger.info("createSnapshotPoint... cons event count: {}", node.getTotalConsEventCount());
        if (node.getTotalConsEventCount().mod(BigInteger.valueOf(Config.EVENT_NUM_PER_SNAPSHOT))
                .equals(BigInteger.ZERO)) {
            EventKeyPair pair = new EventKeyPair(event.getShardId(), event.getCreatorId(), event.getCreatorSeq());
            logger.info("node-({}, {}): snapshotpoint evt-{}, statistics contributions...",
                    node.getShardId(), node.getCreatorId(), pair.toString());
            // 计算并更新贡献
            ConcurrentHashMap<String, Long> statistics = new ConcurrentHashMap<>();
            long[][] effectiveCounts = new long[node.getShardCount()][node.getnValue()];
            for (int i=0; i<node.getShardCount(); i++) {
                for (int j = 0; j < node.getnValue(); j++) {
                    final int shardId = i;
                    final int creatorId = j;
                    effectiveCounts[i][j] = node.getContributions().stream()
                            .filter(c -> c.getShardId()==shardId && c.getCreatorId()==creatorId).count();
                    if (effectiveCounts[i][j]>0) {
                        Optional optional = node.getLocalFullNodes().stream()
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
                logger.info("\n=========== node-({}, {}): vers:{}, msgHashTreeRoot=eHash: {}",
                        node.getShardId(), node.getCreatorId(), vers, msgHashTreeRoot);
            }
            node.getSnapshotPointMap().put(vers, new SnapshotPoint.Builder()
                    .eventBody(event).msgHashTreeRoot(msgHashTreeRoot)
                    .contributions((null!=statistics && statistics.size()<=0) ? null: statistics)
                    .build());
            node.getTreeRootMap().put(vers, msgHashTreeRoot);
            logger.info("\n=========== node-({}, {}):  vers: {}, msgHashTreeRoot: {}",
                    node.getShardId(), node.getCreatorId(), vers, msgHashTreeRoot);

            // 重置消息hash根
            node.setContributions(new HashSet<>());
            msgHashTreeRoot = null;
            vers = vers.add(BigInteger.ONE);

            // 增加创建快照触发器
            JSONObject o = new JSONObject();
            // id与前一个ID一样，可以批量排序，且在处理的时候可以根据type是否为空进入特殊受控消息类型处理分支
            o.put("id", node.getConsMessageMaxId());
            o.put("eHash", eHash);
            o.put("lastIdx", true);
            node.getConsMessageVerifyQueue().put(o);
        }
    }
}
