package one.inve.localfullnode2.snapshot;

import one.inve.bean.message.SnapshotMessage;
import one.inve.localfullnode2.conf.Config;
import one.inve.localfullnode2.snapshot.vo.EventKeyPair;
import one.inve.localfullnode2.utilities.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigInteger;

public class DetectAndRepairSnapshotData {
    private static final Logger logger = LoggerFactory.getLogger(DetectAndRepairSnapshotData.class);

    private DetectAndRepairSnapshotDataDependent dep;
    private String dbId;

    public void detectAndRepairSnapshotData(DetectAndRepairSnapshotDataDependent dep) {
        this.dep = dep;
        this.dbId = dep.getDbId();

        SnapshotMessage snapshotMessage = dep.queryLatestSnapshotMessage(dbId);
        if( snapshotMessage != null ) {
            dep.setSnapshotMessage(snapshotMessage);
            dep.getSnapshotPointMap().put(snapshotMessage.getSnapVersion(), snapshotMessage.getSnapshotPoint());
            dep.getTreeRootMap().put(snapshotMessage.getSnapVersion(),
                    snapshotMessage.getSnapshotPoint().getMsgHashTreeRoot());
            EventKeyPair pair = new EventKeyPair(snapshotMessage.getSnapshotPoint().getEventBody().getShardId(),
                    snapshotMessage.getSnapshotPoint().getEventBody().getCreatorId(),
                    snapshotMessage.getSnapshotPoint().getEventBody().getCreatorSeq());
//            logger.warn("node-({},{}) snap vers: {}, eb-{}, treeRoot: {}", dep.getShardId(), dep.getCreatorId(),
//                    snapshotMessage.getSnapVersion(), pair.toString(),
//                    snapshotMessage.getSnapshotPoint().getMsgHashTreeRoot());
        } else {
//            logger.warn("node-({},{}): LatestSnapshotMessage is null.", dep.getShardId(), dep.getCreatorId());
        }
        // 之前DEFAULT_SNAPSHOT_CLEAR_GENERATION个版本的快照
        SnapshotMessage sm = snapshotMessage;
        if (null != sm && StringUtils.isNotEmpty(sm.getPreHash())) {
            clearHistoryEventsBySnapshot(sm.getSnapVersion(), sm.getPreHash());
        }

        for(int i = 0; i< Config.DEFAULT_SNAPSHOT_CLEAR_GENERATION; i++){
            if( snapshotMessage!=null && StringUtils.isNotEmpty(snapshotMessage.getPreHash()) ) {
                snapshotMessage = dep.querySnapshotMessageByHash(
                        dbId, snapshotMessage.getPreHash() );
                dep.getSnapshotPointMap().put(snapshotMessage.getSnapVersion(),
                        snapshotMessage.getSnapshotPoint());
                dep.getTreeRootMap().put(snapshotMessage.getSnapVersion(),
                        snapshotMessage.getSnapshotPoint().getMsgHashTreeRoot());

//                logger.warn("node-({},{}) snap vers: {}, treeRoot: {}", dep.getShardId(), dep.getCreatorId(),
//                        snapshotMessage.getSnapVersion(), snapshotMessage.getSnapshotPoint().getMsgHashTreeRoot());
            } else {
                break;
            }
        }
    }


    private void clearHistoryEventsBySnapshot(BigInteger vers, String preHash) {
        // 快照消息入库
        if (vers.compareTo(BigInteger.valueOf(Config.DEFAULT_SNAPSHOT_CLEAR_GENERATION)) > 0 ) {
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
                    SnapshotMessage sm = dep.querySnapshotMessageByHash(dbId, preHash);
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
                        dep.deleteEventsBeforeSnapshotPointEvent(
                                dbId, sm.getSnapshotPoint().getEventBody(), dep.getnValue());
                        // 清除之前版本的treeRootMap
                        dep.getTreeRootMap().remove(
                                vers.subtract(BigInteger.valueOf(Config.DEFAULT_SNAPSHOT_CLEAR_GENERATION)) );
                    }
                }
            }
            if (logger.isDebugEnabled()) {
 //               logger.debug("========= snapshot message version-{} delete events success.", vers);
            }
        }
    }

}
