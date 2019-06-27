package one.inve.localfullnode2.snapshot;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import one.inve.bean.message.SnapshotPoint;
import one.inve.bean.node.LocalFullNode;
import one.inve.core.EventBody;
import one.inve.localfullnode2.conf.Config;
import one.inve.localfullnode2.store.EventKeyPair;
import one.inve.localfullnode2.utilities.StringUtils;
import one.inve.utils.DSA;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigInteger;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class CreateSnapshotPoint {
    private static final Logger logger = LoggerFactory.getLogger(CreateSnapshotPoint.class);

    private CreateSnapshotPointDependent dep;
//    private String msgHashTreeRoot = null;
    private BigInteger vers;

    public void createSnapshotPoint(CreateSnapshotPointDependent dep, EventBody event) throws InterruptedException {
        this.dep = dep;
//        this.msgHashTreeRoot = dep.getMsgHashTreeRoot();

//        this.vers = dep.getCurrSnapshotVersion();

        if (dep.getTotalConsEventCount().mod(BigInteger.valueOf(Config.EVENT_NUM_PER_SNAPSHOT))
                .equals(BigInteger.ZERO)) {

            SnapshotPoint sp = dep.getSnapshotPointMap().get(dep.getCurrSnapshotVersion());
            if (null != sp) {
                this.vers = dep.getCurrSnapshotVersion().add(BigInteger.ONE);
            } else {
                this.vers = dep.getCurrSnapshotVersion();
            }

            logger.info(">>>>>START<<<<<createSnapshotPoint:\n eventBody: {}", JSON.toJSONString(event));
            EventKeyPair pair = new EventKeyPair(event.getShardId(), event.getCreatorId(), event.getCreatorSeq());
//            logger.info("node-({}, {}): snapshotpoint evt-{}, statistics contributions...",
//                    dep.getShardId(), dep.getCreatorId(), pair.toString());
            // 计算并更新贡献
            ConcurrentHashMap<String, Long> statistics = new ConcurrentHashMap<>();
            long[][] effectiveCounts = new long[dep.getShardCount()][dep.getnValue()];
            for (int i = 0; i < dep.getShardCount(); i++) {
                for (int j = 0; j < dep.getnValue(); j++) {
                    final int shardId = i;
                    final int creatorId = j;
                    effectiveCounts[i][j] = dep.getContributions().stream()
                            .filter(c -> c.getShardId() == shardId && c.getCreatorId() == creatorId).count();
                    if (effectiveCounts[i][j] > 0) {
                        Optional optional = dep.getLocalFullNodes().stream()
                                .filter(n -> n.getShard().equals("" + shardId) && n.getIndex().equals("" + creatorId))
                                .findFirst();
                        statistics.put(((LocalFullNode) optional.get()).getAddress(), effectiveCounts[i][j]);
                    }
                }
            }

            // 生成快照点
            final String eHash = DSA.encryptBASE64(event.getHash());
            if (StringUtils.isEmpty(dep.getMsgHashTreeRoot())) {
//                msgHashTreeRoot = eHash;
                dep.setMsgHashTreeRoot(eHash);
//                logger.info("\n=========== dep-({}, {}): vers:{}, msgHashTreeRoot=eHash: {}",
//                        dep.getShardId(), dep.getCreatorId(), vers, msgHashTreeRoot);
            }
            dep.putSnapshotPointMap(vers, new SnapshotPoint.Builder()
                    .eventBody(event).msgHashTreeRoot(dep.getMsgHashTreeRoot())
                    .contributions((null != statistics && statistics.size() <= 0) ? null : statistics)
                    .build());
            dep.putTreeRootMap(vers, dep.getMsgHashTreeRoot());

//            logger.info("\n=========== dep-({}, {}):  vers: {}, msgHashTreeRoot: {}",
//                    dep.getShardId(), dep.getCreatorId(), vers, msgHashTreeRoot);
            logger.info(">>>>>INFO<<<<<createSnapshotPoint:\n snapshotPointMap: {},\n treeRootMap: {}",
                    JSON.toJSONString(dep.getSnapshotPointMap()), JSON.toJSONString(dep.getTreeRootMap()));

            // 重置消息hash根
            dep.setContributions(new HashSet<>());
//            msgHashTreeRoot = null;
            dep.setMsgHashTreeRoot(null);
            vers = vers.add(BigInteger.ONE);

            // 增加创建快照触发器
            JSONObject o = new JSONObject();
            // id与前一个ID一样，可以批量排序，且在处理的时候可以根据type是否为空进入特殊受控消息类型处理分支
            o.put("id", dep.getConsMessageMaxId());
            o.put("eHash", eHash);
            o.put("lastIdx", true);
            dep.getConsMessageVerifyQueue().put(o);
            logger.info(">>>>>INFO<<<<<createSnapshotPoint:\n snapshotPointTrigger: {}",o);
            logger.info(">>>>>END<<<<<createSnapshotPoint");
        }

    }

}
