package one.inve.localfullnode2.snapshot;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import one.inve.bean.message.SnapshotMessage;
import one.inve.cluster.Member;
import one.inve.localfullnode2.conf.Config;
import one.inve.localfullnode2.snapshot.vo.Event;
import one.inve.localfullnode2.snapshot.vo.GossipObj;
import one.inve.localfullnode2.snapshot.vo.SnapObj;
import one.inve.localfullnode2.utilities.HnKeyUtils;
import one.inve.localfullnode2.utilities.StringUtils;
import one.inve.transport.Address;
import one.inve.utils.SignUtil;

import java.math.BigInteger;
import java.security.PublicKey;
import java.time.Duration;
import java.time.Instant;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

public class SyncSnapshot {

    private SyncSnapshotDependent dep;
    private GossipObj gossipObj;
    private Map<String, HashSet<String>> snapVersionMap;
    private Member neighbor;
    private PublicKey publicKey;

    public void SyncSnapshot(SyncSnapshotDependent dep) {
        this.dep = dep;
        this.gossipObj = dep.getGossipObj();
        this.snapVersionMap = dep.getSnapVersionMap();
        this.neighbor = dep.getNeighbor();
        this.publicKey = dep.getPublicKey();

        BigInteger snapVers = new BigInteger(gossipObj.snapVersion);
        if (snapVers.compareTo(dep.getCurrSnapshotVersion().add(BigInteger.ONE)) > 0) {
//            logger.warn("node-({}, {}): neighbor node snapshot version bigger than mine...",
//                    node.getShardId(), node.getCreatorId());
            // 记录比自己快照版本高过1个版本的节点数
            if (gossipObj.snapHash == null || gossipObj.snapHash.length == 0) {
//                logger.error("gossipObj.snapHash is null");
//                return eventSize + "_" + eventSpaces;
            }
            if (snapVersionMap.get(new String(gossipObj.snapHash)) == null) {
                HashSet<String> pubKeySet = new HashSet<>();
                pubKeySet.add(neighbor.metadata().get("pubkey"));
                snapVersionMap.put(new String(gossipObj.snapHash), pubKeySet);
            } else {
                HashSet<String> pubKeySet = snapVersionMap.get(new String(gossipObj.snapHash));
                pubKeySet.add(neighbor.metadata().get("pubkey"));
                snapVersionMap.put(new String(gossipObj.snapHash), pubKeySet);
            }
            // 当收集到超过f+1个节点的快照版本比我高过1个时,直接同步快照
//            logger.warn("1:{}", new String(gossipObj.snapHash));
//            logger.warn("2:{}", JSONObject.toJSONString(snapVersionMap.get(new String(gossipObj.snapHash))));
//           logger.warn("3:{}", gossipObj.snapHash.length);
            if (snapVersionMap.get(new String(gossipObj.snapHash)) != null
                    && snapVersionMap.get(new String(gossipObj.snapHash)).size() > (dep.getShardCount() * dep.getnValue()) / 3 + 1) {
//                logger.warn("node-({}, {}): more than f+1 neighbor node's snapshot version bigger than mine, " +
//                        "neighbor synchronize snapshot...", node.getShardId(), node.getCreatorId());
//                logger.warn("node-({}, {}): new String(gossipObj.snapHash):{}",
//                        node.getShardId(), node.getCreatorId(), new String(gossipObj.snapHash));
//                CompletableFuture<?> snapResult = prxMap.get(neighbor.address())
//                        .gossipMySnapVersion4SnapAsync(HnKeyUtils.getString4PublicKey(publicKey),
//                                "", new String(gossipObj.snapHash),
//                                dep.getConsMessageMaxId().toString());
                CompletableFuture<?> snapResult = null;
                SnapObj snapObj = null;
                try {
                    snapObj = (SnapObj) snapResult.get(30000, TimeUnit.MILLISECONDS);
                } catch (Exception e) {
                    //logger.error("gossip2Local for snapshot {} completableFuture.get() Exception: {}-{} ", pre,
                    // neighbor.address().host(), e);
//                    logger.error("gossip2Local for snapshot {} completableFuture.get() Exception: host={}," +
//                                    "rpcPort={}," +
//                                    "e={} ",
//                            pre, neighbor.address().host(), neighbor.metadata().get("rpcPort"), e);

//                    return eventSize + "_" + eventSpaces;
                }
                if (snapObj != null) {
//                    logger.warn("snapObj:{}", JSONObject.toJSONString(snapObj));
                    String snapMessageStr = snapObj.snapMessage;
                    String originalSnapshotStr = JSON.parseObject(snapMessageStr).getString("message");
//                    logger.warn("snapMessageStr:{}", snapMessageStr);
                    SnapshotMessage snapshotMessage = JSONObject.parseObject(originalSnapshotStr,
                            SnapshotMessage.class);
                    String MsgHashTreeRoot = snapshotMessage.getSnapshotPoint().getMsgHashTreeRoot();
                    if (StringUtils.isEmpty(MsgHashTreeRoot)) {
//                        return eventSize + "_" + eventSpaces;
                    }
                    List<JSONObject> messages = null;
                    if (!StringUtils.isEmpty(snapObj.messages)) {
                        messages = JSONArray.parseArray(snapObj.messages, JSONObject.class);
                    }

                    // 正在快照后
                    if (SignUtil.verify(originalSnapshotStr)) {
                        //transaction入库
                        if (messages != null) {
                            for (JSONObject msg : messages) {
                                try {
//                                    logger.error(">>>>>each of messages in GossipEventThread= " + msg);
                                    dep.getConsMessageVerifyQueue().put(msg);
                                } catch (InterruptedException e) {
                                    e.printStackTrace();
                                }
                            }
                        }
                        try {
//                            logger.error("node.getConsMessageVerifyQueue().put(JSONObject.parseObject" +
//                                    "(snapMessageStr)" +
//                                    ")" + snapMessageStr);

                            dep.getConsMessageVerifyQueue().put(JSONObject.parseObject(snapMessageStr));
                        } catch (InterruptedException e) {
//                            logger.error("", e);
                        }
                        // 更新本节点当前快照信息
                        dep.setSnapshotMessage(snapshotMessage);
                        dep.getSnapshotPointMap().put(snapshotMessage.getSnapVersion(),
                                snapshotMessage.getSnapshotPoint());
                        dep.getTreeRootMap().put(snapshotMessage.getSnapVersion(),
                                snapshotMessage.getSnapshotPoint().getMsgHashTreeRoot());
                        snapVersionMap.clear();
                    } else {
//                        return eventSize + "_" + eventSpaces;
                    }
                }
            }
        }

//        long itv = Duration.between(first, Instant.now()).toMillis();
//        if (itv > 50) {
//            logger.debug("node-({}, {}) {}: 2 get events (async future) cost: {}", node.getShardId(),
//                    node.getCreatorId(), pre, itv);
//        }

    }
}

