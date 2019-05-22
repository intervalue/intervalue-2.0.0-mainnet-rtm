package one.inve.localfullnode2.snapshot;

import java.math.BigInteger;
import java.time.Duration;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

import one.inve.bean.message.SnapshotMessage;
import one.inve.utils.SignUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

import one.inve.bean.node.NodeStatus;
import one.inve.cluster.Member;
import one.inve.localfullnode2.gossip.vo.GossipObj;
import one.inve.localfullnode2.snapshot.vo.SnapObj;
import one.inve.localfullnode2.utilities.HnKeyUtils;
import one.inve.localfullnode2.utilities.StringUtils;

/**
 * 
 * Copyright © CHXX Co.,Ltd. All rights reserved.
 * 
 * @Description: If there is snapshot version gap which is behind (n/3)+1 at
 *               least 2,the class is ready to run.
 *               <p/>
 *               {@link GossipEventThread::gossip2Local}
 *               {@link Local2localImpl::gossipMySnapVersion4Snap}
 * @author: Francis.Deng
 * @date: Nov 6, 2018 1:47:05 AM
 * @version: V1.0
 */
public class SnapshotSynchronizer {
	private static final Logger logger = LoggerFactory.getLogger("snapshotsynchronizer");

	private SnapshotSynchronizerDependent dep;
	private Member gossipedMember;
	private GossipObj gossipObj;
	private volatile Map<String, HashSet<String>> snapVersionMap = new HashMap<>();// {$snapHash:{$member.pubkey,...},
	// ...}


	public SnapshotSynchronizer() {
	}

	public SnapshotSynchronizer(SnapshotSynchronizerDependent dep) {
		super();
		this.dep = dep;
	}

	// TBD : put it into zeroc server impls
	// @formatter:off
	// call diagram -
	// SnapshotSynchronizer::synchronizeHigher--...(via network)...--SnapshotSynchronizer::offerSnapshot
	// @formatter:on
	public SnapObj offerSnapshot(String pubkey, String sig, String hash, String requestConsMessageMaxId) {
		Instant first = Instant.now();
		if (!validate(pubkey)) {
			return null;
		}
		// 获取第snapVersion版快照消息
		int selfId = (int) dep.getCreatorId();
//		logger.warn("hash:{}", hash);
		String snapshotStr = dep.getSnapshotDBService().querySnapshotMessageFormatStringByHash(dep.getDbId(), hash);
		String originalSnapshotStr = JSON.parseObject(snapshotStr).getString("message");
		// 获取交易信息
		List<JSONObject> trans = dep.getTransactionDbService().queryMissingTransactionsBeforeSnapshotPoint(
				originalSnapshotStr, new BigInteger(requestConsMessageMaxId), dep.getDbId());

		// 构建结果结构
		SnapObj snapObj = new SnapObj(snapshotStr, (null == trans) ? null : JSONArray.toJSONString(trans));

//		long handleInterval = Duration.between(first, Instant.now()).toMillis();
//		if (handleInterval > Config.DEFAULT_GOSSIP_EVENT_INTERVAL) {
//			logger.warn("----- gossipMySnapVersion4Snap() interval: {} ms", handleInterval);
//		}

		return snapObj;
	}

	// there is a constraint that {@code gossipObj} should been higher than current
	// snapshot version.
	// {@code false} means that snapshot sync process is not executed.
	public boolean synchronizeHigher(SnapshotSynchronizerDependent dep) {
		this.gossipedMember = dep.getGossipedMember();
		this.gossipObj = dep.getGossipObj();

		BigInteger snapVers = new BigInteger(gossipObj.snapVersion);
		if (snapVers.compareTo(dep.getCurrSnapshotVersion().add(BigInteger.ONE)) > 0) {
			logger.info(">>>>>START<<<<<synchronizeHigher:\n gossipedMember: {},\n gossipObj:{}",
					JSON.toJSONString(gossipedMember),JSON.toJSONString(gossipObj));
//			logger.warn("node-({}, {}): neighbor node snapshot version bigger than mine...", node.getShardId(),
//					node.getCreatorId());

			// snapHash is indispensable part.
			if (gossipObj.snapHash == null || gossipObj.snapHash.length == 0) {
//				logger.error("gossipObj.snapHash is null");
				// return eventSize + "_" + eventSpaces;
				logger.error(">>>>>RETURN<<<<<synchronizeHigher:\n snapHash: {}",new String(gossipObj.snapHash));
				return false;
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
			logger.info(">>>>>INFO<<<<<synchronizeHigher:\n snapVersionMap: {}",JSON.toJSONString(snapVersionMap));
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
//				logger.warn("synchronizing: more than f+1 neighbor node's snapshot version bigger than mine");

//				CompletableFuture<?> snapResult = prxMap.get(neighbor.address()).gossipMySnapVersion4SnapAsync(
//						HnKeyUtils.getString4PublicKey(node.publicKey), "", new String(gossipObj.snapHash),
//						node.getConsMessageMaxId().toString());

				/**
				 * Key calling:finally {@code SnapshotSynchronizer::offerSnapshot} is invoked.
				 */
				CompletableFuture<?> snapResult = dep.getSnapshotSync().gossipMySnapVersion4SnapAsync(gossipedMember,
						HnKeyUtils.getString4PublicKey(dep.getPublicKey()), "", new String(gossipObj.snapHash),
						dep.getConsMessageMaxId().toString());

				SnapObj snapObj = null;
				try {
					snapObj = (SnapObj) snapResult.get(30000, TimeUnit.MILLISECONDS);
					logger.info(">>>>>INFO<<<<<synchronizeHigher:\n snapObj: {}",JSON.toJSONString(snapObj));
//					snapVersionMap.clear();
//					return dep.execute(snapObj);
				} catch (Exception e) {
					// logger.error("gossip2Local for snapshot {} completableFuture.get() Exception:
					// {}-{} ", pre, neighbor.address().host(), e);
//					logger.error(
//							"gossip2Local for snapshot {} completableFuture.get() Exception: host={},rpcPort={},e={} ",
//							pre, neighbor.address().host(), neighbor.metadata().get("rpcPort"), e);
//
//					return eventSize + "_" + eventSpaces;
					logger.error(">>>>>ERROR<<<<<synchronizeHigher:\n error: {}",e);
					return false;
				}
				if (snapObj != null) {
//					logger.warn("snapObj:{}", JSONObject.toJSONString(snapObj));
					String snapMessageStr = snapObj.snapMessage;
					String originalSnapshotStr = JSON.parseObject(snapMessageStr).getString("message");
//					logger.warn("snapMessageStr:{}", snapMessageStr);
					SnapshotMessage snapshotMessage = JSONObject.parseObject(originalSnapshotStr,
							SnapshotMessage.class);
					String MsgHashTreeRoot = snapshotMessage.getSnapshotPoint().getMsgHashTreeRoot();
					if (StringUtils.isEmpty(MsgHashTreeRoot)) {
//						return eventSize + "_" + eventSpaces;
						logger.error(">>>>>RETURN<<<<<synchronizeHigher:\n MsgHashTreeRoot is empty");
						return false;
					}
					List<JSONObject> messages = null;
					if (!StringUtils.isEmpty(snapObj.messages)) {
						messages = JSONArray.parseArray(snapObj.messages, JSONObject.class);
						logger.info(">>>>>INFO<<<<<synchronizeHigher:\n messages: {}",messages.toString());
					}

					// 正在快照后
					if (SignUtil.verify(originalSnapshotStr)) {
						// transaction入库
						if (messages != null) {
							for (JSONObject msg : messages) {
								try {
//									logger.error(">>>>>each of messages in GossipEventThread= " + msg);

									dep.getConsMessageVerifyQueue().put(msg);
								} catch (InterruptedException e) {
									logger.error(">>>>>ERROR<<<<<synchronizeHigher:\n error: {}",e);
									e.printStackTrace();
								}
							}
						}
						try {
//							logger.error(
//									"node.getConsMessageVerifyQueue().put(JSONObject.parseObject(snapMessageStr))"
//											+ snapMessageStr);
                            //2019.5.21 数据结构不对应，ConsensusMessageHandleThread无法解析
                            JSONObject snapMessage = JSONObject.parseObject(snapMessageStr);
                            snapMessage.put("msg",originalSnapshotStr);
                            snapMessage.put("eShardId",snapshotMessage.getSnapshotPoint().getEventBody().getShardId());
                            dep.getConsMessageVerifyQueue().put(snapMessage);
						} catch (InterruptedException e) {
							logger.error(">>>>>ERROR<<<<<synchronizeHigher:\n error: {}",e);
							e.printStackTrace();
						}
						// 更新本节点当前快照信息
						dep.setMsgHashTreeRoot(null);//2019.05.22 重置treeRoot
						dep.setSnapshotMessage(snapshotMessage);
						dep.getSnapshotPointMap().put(snapshotMessage.getSnapVersion(),
								snapshotMessage.getSnapshotPoint());
						dep.getTreeRootMap().put(snapshotMessage.getSnapVersion(),
								snapshotMessage.getSnapshotPoint().getMsgHashTreeRoot());
						snapVersionMap.clear();
					} else {
//						return eventSize + "_" + eventSpaces;
						logger.error(">>>>>RETURN<<<<<synchronizeHigher:\n originalSnapshotStr verification failed");
						return false;
					}
				}
			}
			logger.info(">>>>>END<<<<<synchronizeHigher");
		}
		return true;
	}


	private boolean validate(String pubkey) {
		Instant first = Instant.now();
		if (StringUtils.isEmpty(pubkey)) {
			logger.error("pubkey is null.");
			return false;
		}
		boolean isValid =
				dep.getLocalFullNodes().parallelStream().filter(n -> n.getStatus() == NodeStatus.HAS_SHARDED)
						.anyMatch(p -> p.getPubkey().equals(pubkey));
		long handleInterval = Duration.between(first, Instant.now()).toMillis();
		if (handleInterval > 10) {
			logger.warn("Local2local interface validate public keys interval: {}", handleInterval);
		}
		return isValid;
	}
}