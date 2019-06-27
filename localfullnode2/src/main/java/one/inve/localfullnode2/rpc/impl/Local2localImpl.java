package one.inve.localfullnode2.rpc.impl;

import java.math.BigInteger;
import java.security.PublicKey;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.zeroc.Ice.Current;

import one.inve.bean.node.NodeStatus;
import one.inve.core.EventBody;
import one.inve.localfullnode2.conf.Config;
import one.inve.localfullnode2.gossip.HandleSplitReportThread;
import one.inve.localfullnode2.gossip.l2l.L2LCore;
import one.inve.localfullnode2.gossip.vo.AppointEvent;
import one.inve.localfullnode2.gossip.vo.GossipObj;
import one.inve.localfullnode2.gossip.vo.SplitResult;
import one.inve.localfullnode2.message.service.ITransactionDbService;
import one.inve.localfullnode2.message.service.TransactionDbService;
import one.inve.localfullnode2.nodes.LocalFullNode1GeneralNode;
import one.inve.localfullnode2.rpc.Local2local;
import one.inve.localfullnode2.snapshot.vo.SnapObj;
import one.inve.localfullnode2.store.DbUtils;
import one.inve.localfullnode2.store.EventKeyPair;
import one.inve.localfullnode2.store.IEventStore;
import one.inve.localfullnode2.store.SnapshotDbService;
import one.inve.localfullnode2.store.SnapshotDbServiceImpl;
import one.inve.localfullnode2.utilities.Cryptos;
import one.inve.localfullnode2.utilities.HnKeyUtils;
import one.inve.localfullnode2.utilities.StringUtils;
import one.inve.localfullnode2.vo.Event;
import one.inve.utils.DSA;

/**
 * local full node to local full node
 * 
 * @date 2018/6/5.
 */
public class Local2localImpl implements Local2local {
	private static final Logger logger = LoggerFactory.getLogger(Light2localImpl.class);
	private volatile LocalFullNode1GeneralNode node;
	List<Map<EventKeyPair, Map<String, Set<String>>>> splitReportCache;

	private SnapshotDbService snapshotDbService = new SnapshotDbServiceImpl();
	private ITransactionDbService transactionDbService = new TransactionDbService();

	public Local2localImpl(LocalFullNode1GeneralNode node) {
		this.node = node;
		this.splitReportCache = new ArrayList<>();
		for (int i = 0; i < node.getShardCount(); i++) {
			Map<EventKeyPair, Map<String, Set<String>>> map = new HashMap<>();
			splitReportCache.add(map);
		}
	}

	private static long maxTime = 0L;
	/**
	 * 本地gossip的开关，若关闭，则其他节点gossip拿数据时直接return;
	 */
	private static boolean gossipFlag = true;

	private boolean validate(String pubkey) {
		Instant first = Instant.now();
		if (StringUtils.isEmpty(pubkey)) {
			logger.error("pubkey is null.");
			return false;
		}
		boolean isValid = node.getLocalFullNodes().parallelStream().filter(n -> n.getStatus() == NodeStatus.HAS_SHARDED)
				.anyMatch(p -> p.getPubkey().equals(pubkey));
		long handleInterval = Duration.between(first, Instant.now()).toMillis();
		if (handleInterval > 10) {
			logger.warn("Local2local interface validate public keys interval: {}", handleInterval);
		}
		return isValid;
	}

	/**
	 * Consensus In the same shard: The rpc server receives a max sequence list of
	 * Hashnet from a random node in the same shard 1. obtain my own seqs 2. compare
	 * both seqs, and get my events that the receiver does not have. 3. call the
	 * sender's rpc: gossipHashnet4Consensus
	 * 
	 * @param pubkey      requestor's public key
	 * @param sig         signature
	 * @param snapVersion requestor's snapshot version
	 * @param snapHash    requestor's snapshot hash value
	 * @param seqs        list of keys that index the event in Hashnet. Each keys
	 *                    contains a creatorId and a sequenceId.
	 * @param current     rpc connection info
	 */
//	@Override
//	public synchronized GossipObj gossipMyMaxSeqList4Consensus(String pubkey, String sig, String snapVersion,
//			String snapHash, long[] seqs, Current current) {
//		GossipObj gossipObj = null;
//		if (gossipFlag) {
//			Instant first = Instant.now();
//			if (!validate(pubkey)) {
//				return null;
//			}
//
//			// Francis.Deng 4/4/2019
//			// There was an accident that x.xx.202.66:35791(who had higher snapVersion
//			// "273")
//			// retained lower seqs watermark than other's(who had snapVersion "272"
//			// meanwhile),
//			// which caused serious gossip problem.
//
//			// Each thing is fine if the gap between requester's snapVersion and
//			// responder‘snapVersion is 0 or 1
//			BigInteger currSnapshotVersion = node.getCurrSnapshotVersion();
//			if (currSnapshotVersion.equals(new BigInteger(snapVersion))
//					|| currSnapshotVersion.subtract(BigInteger.ONE).equals(new BigInteger(snapVersion))
//					|| currSnapshotVersion.add(BigInteger.ONE).equals(new BigInteger(snapVersion))) {
//				// 请求者地址信息
//				String addressInfo = (null == current.con) ? null : current.con.toString();
//				logger.info("requestor's pubkey = {}", pubkey);
//				List<Event> rpcEvents = getUnknownEvents(seqs, addressInfo).stream()
//						.map(eventBody -> new Event(eventBody.getShardId(), eventBody.getCreatorId(),
//								eventBody.getCreatorSeq(), eventBody.getOtherId(), eventBody.getOtherSeq(),
//								eventBody.getTrans(), eventBody.getTimeCreated().getEpochSecond(),
//								eventBody.getTimeCreated().getNano(), eventBody.getSignature(), eventBody.isFamous(),
//								eventBody.getHash(), eventBody.getGeneration(),
//								(null == eventBody.getConsTimestamp()) ? -1
//										: eventBody.getConsTimestamp().getEpochSecond(),
//								(null == eventBody.getConsTimestamp()) ? -1 : eventBody.getConsTimestamp().getNano(),
//								eventBody.getOtherHash(), eventBody.getParentHash()))
//						.collect(Collectors.toList());
//
//				gossipObj = (rpcEvents.size() > 0)
//						? new GossipObj(currSnapshotVersion.toString(), rpcEvents.toArray(new Event[0]), null)
//						: new GossipObj(currSnapshotVersion.toString(), null, null);
//
//				long handleInterval = Duration.between(first, Instant.now()).toMillis();
//				if (handleInterval > Config.DEFAULT_GOSSIP_EVENT_INTERVAL) {
//					logger.warn(
//							"Warning:gossipMyMaxSeqList4Consensus in server-side costs {} ms.  Additional connection(remote address-->local address) is <<{}>>",
//							handleInterval, addressInfo);
//				}
//				return gossipObj;
//
//			} else if (currSnapshotVersion.compareTo(new BigInteger(snapVersion)) > 0) {
//				/*
//				 * logger.warn("hash:{}",SnapshotDbService.querySnapshotMessageHashByVersion(
//				 * node.nodeParameters.dbId,snapVersion)==null?
//				 * null:SnapshotDbService.querySnapshotMessageHashByVersion(node.nodeParameters.
//				 * dbId,snapVersion));
//				 * logger.warn("{}",SnapshotDbService.querySnapshotMessageHashByVersion(node.
//				 * nodeParameters.dbId,snapVersion)==null?
//				 * null:SnapshotDbService.querySnapshotMessageHashByVersion(node.nodeParameters.
//				 * dbId,snapVersion).getBytes());
//				 */
//				return new GossipObj(currSnapshotVersion.toString(), null,
//						snapshotDbService.querySnapshotMessageHashByVersion(node.nodeParameters().dbId,
//								snapVersion) == null ? null
//										: snapshotDbService.querySnapshotMessageHashByVersion(
//												node.nodeParameters().dbId, snapVersion).getBytes());
//			} else {
//				return new GossipObj(currSnapshotVersion.toString(), null, null);
//			}
//		} else { // gossipFlag is false
//			return gossipObj;
//		}
//	}
	/**
	 * delegate core task to {@link L2LCore}
	 */
	@Override
	public synchronized GossipObj gossipMyMaxSeqList4Consensus(String pubkey, String sig, String snapVersion,
			String snapHash, long[] seqs, Current current) {
		GossipObj gossipObj = null;
		if (gossipFlag) {
			// disable gossip write-read lock due to more time to complete message.
			// 2019.2.20 by Francis.Deng
//			ReadLock readLock = node.gossipAndRPCExclusiveLock().readLock();
//			readLock.lock();
//			try {
			logger.info("gossipMyMaxSeqList4Consensus is running(answering the gossip)");

			L2LCore l2l = new L2LCore();
			gossipObj = l2l.gossipMyMaxSeqList4Consensus(pubkey, sig, snapVersion, snapHash, seqs,
					node.getCurrSnapshotVersion(), node.getLocalFullNodes(), node.getEventStore(), node.getShardId(),
					node.nodeParameters().dbId);
//			} finally {
//				readLock.unlock();
//			}

			return gossipObj;
		} else { // gossipFlag is false
			return gossipObj;
		}
	}

	/**
	 * Consensus In another shard: The rpc server receives a max sequence list of
	 * Hashnet from a random node in another shard 1. obtain my own cspList 2.
	 * compare both cspLists, and get my "confirmed" events that the receiver does
	 * not have. 3. call the sender's rpc: gossipHashnet4Sync
	 * 
	 * @param pubkey       requestor's public key
	 * @param sig          signature
	 * @param otherShardId request shard id
	 * @param snapVersion  requestor's snapshot version
	 * @param snapHash     requestor's snapshot hash value
	 * @param seqs         list of keys that index the event in Hashnet. Each keys
	 *                     contains a creatorId and a sequenceId.
	 * @param current      rpc connection info
	 */
	@Override
	public synchronized GossipObj gossipMyMaxSeqList4Sync(String pubkey, String sig, int otherShardId,
			String snapVersion, String snapHash, long[] seqs, Current current) {
		GossipObj gossipObj = null;
		if (StringUtils.isEmpty(snapVersion)) {
			return gossipObj;
		}
		if (gossipFlag) {
			Instant first = Instant.now();
			if (!validate(pubkey)) {
				return null;
			}
			BigInteger currSnapshotVersion = node.getCurrSnapshotVersion();

			// Francis.Deng 4/4/2019
			// There was an accident that x.xx.202.66:35791(who had higher snapVersion
			// "273")
			// retained lower seqs watermark than other's(who had snapVersion "272"
			// meanwhile),
			// which caused serious gossip problem.

			// Each thing is fine if the gap between requester's snapVersion and
			// responder‘snapVersion is 0 or 1
			if (currSnapshotVersion.equals(new BigInteger(snapVersion))
					|| currSnapshotVersion.subtract(BigInteger.ONE).equals(new BigInteger(snapVersion))
					|| currSnapshotVersion.add(BigInteger.ONE).equals(new BigInteger(snapVersion))) {
				// 请求者地址信息
				String addressInfo = (null == current.con) ? null : current.con.toString();
				logger.info("requestor's pubkey = {}", pubkey);
				List<Event> rpcEvents = getUnknownEvents(otherShardId, seqs, addressInfo).stream()
						.map(eventBody -> new Event(eventBody.getShardId(), eventBody.getCreatorId(),
								eventBody.getCreatorSeq(), eventBody.getOtherId(), eventBody.getOtherSeq(),
								eventBody.getTrans(), eventBody.getTimeCreated().getEpochSecond(),
								eventBody.getTimeCreated().getNano(), eventBody.getSignature(), eventBody.isFamous(),
								eventBody.getHash(), eventBody.getGeneration(),
								(null == eventBody.getConsTimestamp()) ? -1
										: eventBody.getConsTimestamp().getEpochSecond(),
								(null == eventBody.getConsTimestamp()) ? -1 : eventBody.getConsTimestamp().getNano(),
								eventBody.getOtherHash(), eventBody.getParentHash()))
						.collect(Collectors.toList());

				gossipObj = (rpcEvents.size() > 0)
						? new GossipObj(currSnapshotVersion.toString(), rpcEvents.toArray(new Event[0]), null)
						: new GossipObj(currSnapshotVersion.toString(), null, null);

				long handleInterval = Duration.between(first, Instant.now()).toMillis();
				if (handleInterval > Config.DEFAULT_GOSSIP_EVENT_INTERVAL) {
					logger.warn("----- gossipMyMaxSeqList4Sync() {} interval: {} ms", addressInfo, handleInterval);
				}
				return gossipObj;

			} else if (currSnapshotVersion.compareTo(new BigInteger(snapVersion)) > 0) {
				/*
				 * SnapshotMessage snapshotMessage =
				 * SnapshotDbService.querySnapshotMessageByHash(node.nodeParameters.dbId,
				 * snapHash);
				 */
				return new GossipObj(currSnapshotVersion.toString(), null,
						snapshotDbService.querySnapshotMessageHashByVersion(node.nodeParameters().dbId,
								snapVersion) == null ? null
										: snapshotDbService.querySnapshotMessageHashByVersion(
												node.nodeParameters().dbId, snapVersion).getBytes());
			} else {
				return new GossipObj(currSnapshotVersion.toString(), null, null);
			}
		} else { // gossipFlag is false
			return gossipObj;
		}
	}

	/**
	 * 同步快照
	 * 
	 * @param pubkey     requestor's public key
	 * @param sig        signature
	 * @param hash       requestor's hash
	 * @param transCount requestor max transaction id.
	 * @param current    rpc connection info
	 * @return snapshot object
	 */
	@Override
	public SnapObj gossipMySnapVersion4Snap(String pubkey, String sig, String hash, String transCount,
			Current current) {
		Instant first = Instant.now();
		if (!validate(pubkey)) {
			return null;
		}
		// 获取第snapVersion版快照消息
		int selfId = (int) node.getCreatorId();
		logger.warn("hash:{}", hash);
		String snapshotStr = snapshotDbService.querySnapshotMessageFormatStringByHash(node.nodeParameters().dbId, hash);
		String originalSnapshotStr = JSON.parseObject(snapshotStr).getString("message");
		// 获取交易信息
		List<JSONObject> trans = transactionDbService.queryMissingTransactionsBeforeSnapshotPoint(originalSnapshotStr,
				new BigInteger(transCount), node.nodeParameters().dbId);

		// 构建结果结构
		SnapObj snapObj = new SnapObj(snapshotStr, (null == trans) ? null : JSONArray.toJSONString(trans));

		long handleInterval = Duration.between(first, Instant.now()).toMillis();
		if (handleInterval > Config.DEFAULT_GOSSIP_EVENT_INTERVAL) {
			logger.warn("----- gossipMySnapVersion4Snap() interval: {} ms", handleInterval);
		}

		return snapObj;
	}

	/**
	 * 获取本片内未知events
	 * 
	 * @param otherCounts 最大seq数组
	 * @return 未知events
	 */
	private ArrayList<EventBody> getUnknownEvents(long[] otherCounts, String connInfo) {
		return getUnknownEvents(node.getShardId(), otherCounts, connInfo);
	}

	/**
	 * 获取片shardId内未知events
	 * 
	 * @param shardId     片号
	 * @param otherCounts 最大seq数组
	 * @return 未知events
	 */
	private ArrayList<EventBody> getUnknownEvents(int shardId, long[] otherCounts, String connInfo) {
		IEventStore myStore = node.getEventStore();
		long[] currMyCounts = myStore.getLastSeqsByShardId(shardId);

		// verbose output to compare two seqs.
		logger.info("requester's height is {}", Arrays.toString(otherCounts));
		logger.info("my height is          {}", Arrays.toString(currMyCounts));

		ArrayList<EventBody> diffEvents = new ArrayList<>();
		EventBody eventBody = null;

		for (int i = 0; i < currMyCounts.length; ++i) {
			for (long j = otherCounts[i] + 1L; j <= currMyCounts[i]; ++j) {
				eventBody = myStore.getEventInMem(shardId, (long) i, j);
				if (eventBody != null) {
					diffEvents.add(eventBody);
				}
			}
		}

		Collections.shuffle(diffEvents);
		if (diffEvents.size() > 1) {
			diffEvents.sort(Comparator.comparing(EventBody::getGeneration));
		}
		logger.info("\n{} \ngetUnknownEvents(): requestor's seqs: {}, my seqs: {}, gossip event size = {}",
				(null == connInfo ? null : connInfo.split("\\n")[1]), JSON.toJSONString(otherCounts),
				JSON.toJSONString(currMyCounts), diffEvents.size());

		return cutResultUnknownEvents(diffEvents, Config.DEFAULT_SYNC_EVENT_COUNT);

//      EventStore myStore = node.getEventStore();
//		long[] currMyCounts = myStore.getLastSeqsByShardId(shardId);
//		ArrayList<EventBody> diffEvents = new ArrayList<>();
//		EventBody eventBody = null;
//
//		for(int i = 0; i < currMyCounts.length; ++i) {
//			for(long j = otherCounts[i] + 1L; j <= currMyCounts[i]; ++j) {
//				eventBody = myStore.getEventInMem(shardId, (long)i, j);
//				if(eventBody!=null) {
//					diffEvents.add(eventBody);
//				}
//			}
//		}
//
//		Collections.shuffle(diffEvents);
//		if (diffEvents.size()>1) {
//            diffEvents.sort(Comparator.comparing(EventBody::getGeneration));
//            logger.info("{}: gossip event size = {}", connInfo, diffEvents.size());
//        }
//		return diffEvents;
	}

	/**
	 * 截取前面限定数量的event列表
	 * 
	 * @param events event列表
	 * @return 限定数量的event列表
	 */
	private ArrayList<EventBody> cutResultUnknownEvents(ArrayList<EventBody> events, long size) {
		if (events.size() > size) {
			// size += 1;
//Francis.Deng from Mar.25.2019
//issue of UST(unavailable submitted transaction): any submitted transaction could not been retrieved via consensus mechanism.

			logger.info("node-({},{}): cutResultUnknownEvents()... size: {}", node.getShardId(), node.getCreatorId(),
					size);

			ArrayList<EventBody> result = new ArrayList<>();
			for (int i = 0; i < size; i++) {
				EventBody eb = events.get(i);
				if (null != eb) {
					result.add(eb);
				}
			}
			return result;
		} else {
			return events;
		}
	}

	/**
	 * 弱中心化节点提供的接收split分叉举报的接口
	 */
	@Override
	public boolean gossipReport4split(String pubkey, String sig, String data, int shardId, String event,
			Current current) {
		if (checkSig(data, sig, pubkey)) {
			try {
				EventBody eb = JSONObject.parseObject(event, EventBody.class);
				EventKeyPair keyPair = new EventKeyPair(eb.getShardId(), eb.getCreatorId(), eb.getCreatorSeq());
				Map<String, Set<String>> eventMap = splitReportCache.get(shardId).get(keyPair);
				if (eventMap != null) {
					Set<String> pubkeySet = eventMap.get(DSA.encryptBASE64(eb.getHash()));
					if (pubkeySet != null) {
						pubkeySet.add(pubkey);
						if (pubkeySet.size() >= ((node.getShardCount() * node.getnValue()) / 3) + 1) {
							// 调用广播接口。
							new HandleSplitReportThread(node, shardId, eb.getCreatorId(), eb.getCreatorSeq(),
									DSA.encryptBASE64(eb.getHash()), false).start();
							splitReportCache.get(shardId).remove(keyPair);
						}
						int mapCount = 0;
						String mostKey = null;
						for (String mapKey : eventMap.keySet()) {
							if (eventMap.get(mapKey) == null) {
								continue;
							}
							mapCount += eventMap.get(mapKey).size();
							if (mostKey == null || eventMap.get(mapKey).size() > eventMap.get(mostKey).size()) {
								mostKey = mapKey;
							}
						}
						if (mapCount > (node.getShardCount() * node.getnValue() * 2) / 3) {
							new HandleSplitReportThread(node, shardId, eb.getCreatorId(), eb.getCreatorSeq(), mostKey,
									false).start();
							splitReportCache.get(shardId).remove(keyPair);
						}
					} else {
						pubkeySet = new HashSet<>();
						pubkeySet.add(pubkey);
					}
				} else {
					eventMap = new HashMap<>();
					Set<String> pubkeySet = new HashSet<>();
					pubkeySet.add(pubkey);
					eventMap.put(DSA.encryptBASE64(eb.getHash()), pubkeySet);
					splitReportCache.get(shardId).put(keyPair, eventMap);
				}
				return true;
			} catch (Exception e) {
				logger.error("gossipReport4splitErr:", e);
				return false;
			}
		} else {
			return false;
		}
	}

	private boolean checkSig(String data, String sig, String pubkey) {
		if (StringUtils.isEmpty(pubkey) || StringUtils.isEmpty(sig) || StringUtils.isEmpty(data)) {
			return false;
		}
		try {
			PublicKey publicKey = HnKeyUtils.getPublicKey4String(pubkey);
			return Cryptos.verifySignature(Base64.getDecoder().decode(data), Base64.getDecoder().decode(sig),
					publicKey);
		} catch (Exception e) {
			logger.error("checkSigErr:", e);
			return false;
		}

	}

	// temporal comment
//	@Override
//	public boolean gossip4SplitDel(String pubkey, String sig, String data, int shardId, long creatorId, long creatorSeq,
//			String eventHash, boolean isNeedGossip2Center, Current current) {
//		PublicKey centerPubkey = node.getEventFlow().getPubKeys()[0][0];
//		if (centerPubkey != null && HnKeyUtils.getString4PublicKey(centerPubkey).equals(pubkey)
//				&& checkSig(data, sig, pubkey)) {
//			PublicKey pubKey = node.getEventFlow().getPubKeys()[shardId][(int) creatorId];
//			DbUtils.writeBlackInDb(HnKeyUtils.getString4PublicKey(pubKey), 2, node.nodeParameters().dbId);
//			EventBody localEvent = node.getEventStore().getEvent(shardId, creatorId, creatorSeq);
//			if (localEvent != null && localEvent.getHash() != null
//					&& !DSA.encryptBASE64(localEvent.getHash()).equals(eventHash)) {
//				List<EventBody> eventBodys = new ArrayList<>();
//				long lastSeq = node.getEventStore().getLastSeq(shardId, creatorId);
//				for (long seq = creatorSeq; seq <= lastSeq; seq++) {
//					EventBody eb = node.getEventStore().getEvent(shardId, creatorId, seq);
//					if (null != eb) {
//						eventBodys.add(eb);
//					}
//				}
//				long[] minSeq = null;
//				Set<String> delKey = new HashSet<>();
//				Arrays.fill(minSeq, -1);
//				for (int i = 0; i < eventBodys.size(); i++) {
//					if (eventBodys.get(i) != null) {
//						SplitResult splitResult = splitDel(shardId, eventBodys.get(i).getCreatorId(),
//								eventBodys.get(i).getCreatorSeq());
//						if (minSeq == null) {
//							minSeq = splitResult.minSeq;
//						} else {
//							for (int j = 0; j < minSeq.length; j++) {
//								if (splitResult.minSeq[j] != -1
//										&& (minSeq[j] == -1 || minSeq[j] > splitResult.minSeq[j])) {
//									minSeq[j] = splitResult.minSeq[j];
//								}
//							}
//						}
//						if (delKey.size() == 0) {
//							delKey = splitResult.delKey;
//						} else {
//							delKey.addAll(splitResult.delKey);
//						}
//					}
//				}
//				for (int i = 0; i < minSeq.length; i++) {
//					if (minSeq[i] != -1) {
//						// node.maxSeqs[shardId][i] = minSeq[i];
//						node.getLastSeqs()[shardId][i] = minSeq[i];
//						node.getEventStore().setLastSeq(shardId, i, minSeq[i]);
//						int selfId = (int) node.getCreatorId();
//						RocksJavaUtil rocksJavaUtil = new RocksJavaUtil(node.nodeParameters().dbId);
//						for (long seq = minSeq[i]; seq <= lastSeq; seq++) {
//							EventKeyPair pair = new EventKeyPair(shardId, creatorId, seq);
//							rocksJavaUtil.delete(pair.toString());
//
//						}
//						logger.info("del local event set lastSeq:--" + i + "--" + minSeq[i]);
//					}
//				}
//				for (String key : delKey) {
//					String[] param = key.split("_");
//					node.getEventStore().delEventInCache(shardId, Long.parseLong(param[0]), Long.parseLong(param[1]));
//				}
//				// 是否需要与中心节点先gossip同步数据
//				if (isNeedGossip2Center) {
//					((Main) this.node).getConsensusThread()
//							.gossip2AppointLocalFullNode(HnKeyUtils.getString4PublicKey(centerPubkey));
//					((Main) this.node).getSyncThread()
//							.gossip2AppointLocalFullNode(HnKeyUtils.getString4PublicKey(centerPubkey));
//				}
//				try {
//					Thread.sleep(2000);
//				} catch (InterruptedException e) {
//					e.printStackTrace();
//				}
//			}
//			gossipFlag = true;
//			((Main) this.node).getConsensusThread().gossipFlag = true;
//			((Main) this.node).getSyncThread().gossipFlag = true;
//			if ((this.node).getShardId() == shardId) {
//				if ((shardId + "_" + creatorId + "_" + creatorSeq)
//						.equals(((Main) this.node).getConsensusThread().gossipAppointEventKey)) {
//					((Main) this.node).getConsensusThread().gossipAppointEventFlag = false;
//				}
//			} else {
//				if ((shardId + "_" + creatorId + "_" + creatorSeq)
//						.equals(((Main) this.node).getSyncThread().gossipAppointEventKey)) {
//					((Main) this.node).getSyncThread().gossipAppointEventFlag = false;
//				}
//			}
//			return true;
//		} else {
//			return false;
//		}
//	}
	public boolean gossip4SplitDel(String pubkey, String sig, String data, int shardId, long creatorId, long creatorSeq,
			String eventHash, boolean isNeedGossip2Center, Current current) {
		return false;
	}

	private SplitResult splitDel(int shardId, long otherId, long otherSeq) {
		long[] minSeq = new long[node.getnValue()];
		Set<String> delKey = new HashSet<>();
		Arrays.fill(minSeq, -1);
		minSeq[(int) otherId] = otherSeq - 1;
		int selfId = (int) node.getCreatorId();
		List<EventBody> list = DbUtils.getEventFromOther(shardId, otherId, otherSeq, this.node);
		if (list != null && list.size() > 0) {
			for (EventBody eb : list) {
				delKey.add(eb.getCreatorId() + "_" + eb.getCreatorSeq());
				SplitResult resultSeq = splitDel(shardId, eb.getCreatorId(), eb.getCreatorSeq());
				for (int i = 0; i < resultSeq.minSeq.length; i++) {
					if (resultSeq.minSeq[i] != -1 && (minSeq[i] == -1 || minSeq[i] > resultSeq.minSeq[i])) {
						minSeq[i] = resultSeq.minSeq[i];
					}
				}
				if (resultSeq.delKey.size() > 0) {
					delKey.addAll(resultSeq.delKey);
				}
			}
		}
		return new SplitResult.Builder().minSeq(minSeq).delKey(delKey).build();
	}

	@Override
	public AppointEvent gossip4AppointEvent(String pubkey, String sig, int shardId, long creatorId, long creatorSeq,
			Current current) {
		if (!validate(pubkey)) {
			return null;
		}
		EventBody eventBody = node.getEventStore().getEventInMem(shardId, creatorId, creatorSeq);

		if (eventBody != null) {
			return new AppointEvent(null, new Event(eventBody.getShardId(), eventBody.getCreatorId(),
					eventBody.getCreatorSeq(), eventBody.getOtherId(), eventBody.getOtherSeq(), eventBody.getTrans(),
					eventBody.getTimeCreated().getEpochSecond(), eventBody.getTimeCreated().getNano(),
					eventBody.getSignature(), eventBody.isFamous(), eventBody.getHash(), eventBody.getGeneration(),
					(null == eventBody.getConsTimestamp()) ? -1 : eventBody.getConsTimestamp().getEpochSecond(),
					(null == eventBody.getConsTimestamp()) ? -1 : eventBody.getConsTimestamp().getNano(),
					eventBody.getOtherHash(), eventBody.getParentHash()));

		} else {
			return new AppointEvent(node.getCurrSnapshotVersion().toString(), null);
		}
	}

	/**
	 * allow peer to be aware of the height of other peers.
	 */
	@Override
	public long[] getHeight(Current current) {
		long[] height = node.getEventStore().getLastSeqsByShardId(node.getShardId());
		return height;
	}

}
