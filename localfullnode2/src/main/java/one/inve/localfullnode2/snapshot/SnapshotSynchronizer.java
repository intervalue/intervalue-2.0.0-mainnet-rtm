package one.inve.localfullnode2.snapshot;

import java.math.BigInteger;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

import one.inve.localfullnode2.gossip.vo.GossipObj;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

import one.inve.bean.message.SnapshotMessage;
import one.inve.cluster.Member;
import one.inve.localfullnode2.snapshot.vo.SnapObj;
import one.inve.localfullnode2.utilities.HnKeyUtils;
import one.inve.localfullnode2.utilities.StringUtils;
import one.inve.utils.SignUtil;

/**
 * 
 * Copyright © CHXX Co.,Ltd. All rights reserved.
 * 
 * @Description: If there is snapshot version gap which is behind (n/3)+1 at
 *               least 2,the class is ready to run.
 *               <p/>
 *               {@link GossipEventThread::gossip2Local}
 * @author: Francis.Deng
 * @date: Nov 6, 2018 1:47:05 AM
 * @version: V1.0
 */
public class SnapshotSynchronizer {
	private static final Logger logger = LoggerFactory.getLogger("snapshotsynchronizer");

	private Map<String, HashSet<String>> snapVersionMap = new HashMap<>();// {$snapHash:{$member.pubkey...}...}

	public void synchronize(SnapshotSynchronizerDependent dep, Member gossipedMember, GossipObj gossipObj) {
		BigInteger snapVers = new BigInteger(gossipObj.snapVersion);
		if (snapVers.compareTo(dep.getCurrSnapshotVersion().add(BigInteger.ONE)) > 0) {
//			logger.warn("node-({}, {}): neighbor node snapshot version bigger than mine...", node.getShardId(),
//					node.getCreatorId());

			// snapHash is indispensable part.
			if (gossipObj.snapHash == null || gossipObj.snapHash.length == 0) {
				logger.error("gossipObj.snapHash is null");
				// return eventSize + "_" + eventSpaces;
				return;
			}

			if (snapVersionMap.get(new String(gossipObj.snapHash)) == null) {
				HashSet<String> pubKeySet = new HashSet<>();
				pubKeySet.add(gossipedMember.metadata().get("pubkey"));
				snapVersionMap.put(new String(gossipObj.snapHash), pubKeySet);
			} else {
				HashSet<String> pubKeySet = snapVersionMap.get(new String(gossipObj.snapHash));
				pubKeySet.add(gossipedMember.metadata().get("pubkey"));
				snapVersionMap.put(new String(gossipObj.snapHash), pubKeySet);
			}
			// 当收集到超过f+1个节点的快照版本比我高过1个时,直接同步快照
//			logger.warn("1:{}", new String(gossipObj.snapHash));
//			logger.warn("2:{}", JSONObject.toJSONString(snapVersionMap.get(new String(gossipObj.snapHash))));
//			logger.warn("3:{}", gossipObj.snapHash.length);
			if (snapVersionMap.get(new String(gossipObj.snapHash)) != null && snapVersionMap
					.get(new String(gossipObj.snapHash)).size() > (dep.getShardCount() * dep.getnValue()) / 3 + 1) {
//				logger.warn("node-({}, {}): more than f+1 neighbor node's snapshot version bigger than mine, "
//						+ "neighbor synchronize snapshot...", node.getShardId(), node.getCreatorId());
//				logger.warn("node-({}, {}): new String(gossipObj.snapHash):{}", node.getShardId(), node.getCreatorId(),
//						new String(gossipObj.snapHash));
				logger.warn("synchronizing: more than f+1 neighbor node's snapshot version bigger than mine");

//				CompletableFuture<?> snapResult = prxMap.get(neighbor.address()).gossipMySnapVersion4SnapAsync(
//						HnKeyUtils.getString4PublicKey(node.publicKey), "", new String(gossipObj.snapHash),
//						node.getConsMessageMaxId().toString());

				CompletableFuture<?> snapResult = dep.getSnapshotSync().gossipMySnapVersion4SnapAsync(gossipedMember,
						HnKeyUtils.getString4PublicKey(dep.getPublicKey()), "", new String(gossipObj.snapHash),
						dep.getConsMessageMaxId().toString());

				SnapObj snapObj = null;
				try {
					snapObj = (SnapObj) snapResult.get(30000, TimeUnit.MILLISECONDS);
				} catch (Exception e) {
					// logger.error("gossip2Local for snapshot {} completableFuture.get() Exception:
					// {}-{} ", pre, neighbor.address().host(), e);
//					logger.error(
//							"gossip2Local for snapshot {} completableFuture.get() Exception: host={},rpcPort={},e={} ",
//							pre, neighbor.address().host(), neighbor.metadata().get("rpcPort"), e);
//
//					return eventSize + "_" + eventSpaces;
					return;
				}
				if (snapObj != null) {
					logger.warn("snapObj:{}", JSONObject.toJSONString(snapObj));
					String snapMessageStr = snapObj.snapMessage;
					String originalSnapshotStr = JSON.parseObject(snapMessageStr).getString("message");
					logger.warn("snapMessageStr:{}", snapMessageStr);
					SnapshotMessage snapshotMessage = JSONObject.parseObject(originalSnapshotStr,
							SnapshotMessage.class);
					String MsgHashTreeRoot = snapshotMessage.getSnapshotPoint().getMsgHashTreeRoot();
					if (StringUtils.isEmpty(MsgHashTreeRoot)) {
						// return eventSize + "_" + eventSpaces;
						return;
					}
					List<JSONObject> messages = null;
					if (!StringUtils.isEmpty(snapObj.messages)) {
						messages = JSONArray.parseArray(snapObj.messages, JSONObject.class);
					}

					// 正在快照后
					if (SignUtil.verify(originalSnapshotStr)) {
						// transaction入库
						if (messages != null) {
							for (JSONObject msg : messages) {
								try {
									logger.error(">>>>>each of messages in GossipEventThread= " + msg);

									dep.getConsMessageVerifyQueue().put(msg);
								} catch (InterruptedException e) {
									e.printStackTrace();
								}
							}
						}
						try {
							logger.error("node.getConsMessageVerifyQueue().put(JSONObject.parseObject(snapMessageStr))"
									+ snapMessageStr);

							dep.getConsMessageVerifyQueue().put(JSONObject.parseObject(snapMessageStr));
						} catch (InterruptedException e) {
							logger.error("", e);
						}
						// 更新本节点当前快照信息
//						node.setSnapshotMessage(snapshotMessage);
//						node.getSnapshotPointMap().put(snapshotMessage.getSnapVersion(),
//								snapshotMessage.getSnapshotPoint());
//						node.getTreeRootMap().put(snapshotMessage.getSnapVersion(),
//								snapshotMessage.getSnapshotPoint().getMsgHashTreeRoot());
						dep.refresh(snapshotMessage);
						snapVersionMap.clear();
					} else {
						// return eventSize + "_" + eventSpaces;
						return;
					}
				}
			}
		}

	}
}
