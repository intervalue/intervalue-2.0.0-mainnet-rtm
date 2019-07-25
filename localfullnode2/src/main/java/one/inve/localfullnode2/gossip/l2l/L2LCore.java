package one.inve.localfullnode2.gossip.l2l;

import java.math.BigInteger;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import one.inve.bean.node.LocalFullNode;
import one.inve.bean.node.NodeStatus;
import one.inve.localfullnode2.conf.Config;
import one.inve.localfullnode2.gossip.vo.GossipObj;
import one.inve.core.EventBody;
import one.inve.localfullnode2.store.IEventStore;
import one.inve.localfullnode2.store.SnapshotDbService;
import one.inve.localfullnode2.store.SnapshotDbServiceImpl;
import one.inve.localfullnode2.utilities.StringUtils;
import one.inve.localfullnode2.vo.Event;

/**
 * 
 * 
 * Copyright © INVE FOUNDATION. All rights reserved.
 * 
 * @Description: raw function without zeroc,generalnode
 * @author: Francis.Deng
 * @date: May 27, 2018 7:28:34 PM
 * @version: V1.0
 */
public class L2LCore {
	private static final Logger logger = LoggerFactory.getLogger(L2LCore.class);

	private SnapshotDbService snapshotDbService = new SnapshotDbServiceImpl();

	/**
	 * gossip service implement
	 */
	public synchronized GossipObj gossipMyMaxSeqList4Consensus(String requesterPubkey, String requesterSig,
			String requesterSnapVersion, String requesterSnapHash, long[] requesterSeqs, BigInteger mySnapshotVersion,
			List<LocalFullNode> localFullnodes, IEventStore myStore, int shardId, String dbId) {
		GossipObj gossipObj = null;

		Instant first = Instant.now();
		if (!validate(requesterPubkey, localFullnodes)) {
			return null;
		}

		BigInteger currSnapshotVersion = mySnapshotVersion;
		if (currSnapshotVersion.equals(new BigInteger(requesterSnapVersion))
				|| currSnapshotVersion.subtract(BigInteger.ONE).equals(new BigInteger(requesterSnapVersion))
				|| currSnapshotVersion.add(BigInteger.ONE).equals(new BigInteger(requesterSnapVersion))) {

			List<Event> rpcEvents = getUnknownEvents(myStore, shardId, requesterSeqs).stream()
					.map(eventBody -> new Event(eventBody.getShardId(), eventBody.getCreatorId(),
							eventBody.getCreatorSeq(), eventBody.getOtherId(), eventBody.getOtherSeq(),
							eventBody.getTrans(), eventBody.getTimeCreated().getEpochSecond(),
							eventBody.getTimeCreated().getNano(), eventBody.getSignature(), eventBody.isFamous(),
							eventBody.getHash(), eventBody.getGeneration(),
							(null == eventBody.getConsTimestamp()) ? -1 : eventBody.getConsTimestamp().getEpochSecond(),
							(null == eventBody.getConsTimestamp()) ? -1 : eventBody.getConsTimestamp().getNano(),
							eventBody.getOtherHash(), eventBody.getParentHash()))
					.collect(Collectors.toList());

			gossipObj = (rpcEvents.size() > 0)
					? new GossipObj(currSnapshotVersion.toString(), rpcEvents.toArray(new Event[0]), null)
					: new GossipObj(currSnapshotVersion.toString(), null, null);

			long handleInterval = Duration.between(first, Instant.now()).toMillis();
//			if (handleInterval > Config.DEFAULT_GOSSIP_EVENT_INTERVAL) {
//				logger.warn(
//						"Warning:gossipMyMaxSeqList4Consensus in server-side costs {} ms.  Additional connection(remote address-->local address) is <<{}>>",
//						handleInterval, addressInfo);
//			}
			return gossipObj;

		} else if (currSnapshotVersion.compareTo(new BigInteger(requesterSnapVersion)) > 0) {
			/*
			 * logger.warn("hash:{}",SnapshotDbService.querySnapshotMessageHashByVersion(
			 * node.nodeParameters.dbId,snapVersion)==null?
			 * null:SnapshotDbService.querySnapshotMessageHashByVersion(node.nodeParameters.
			 * dbId,snapVersion));
			 * logger.warn("{}",SnapshotDbService.querySnapshotMessageHashByVersion(node.
			 * nodeParameters.dbId,snapVersion)==null?
			 * null:SnapshotDbService.querySnapshotMessageHashByVersion(node.nodeParameters.
			 * dbId,snapVersion).getBytes());
			 */
			return new GossipObj(currSnapshotVersion.toString(), null,
					snapshotDbService.querySnapshotMessageHashByVersion(dbId, requesterSnapVersion) == null ? null
							: snapshotDbService.querySnapshotMessageHashByVersion(dbId, requesterSnapVersion)
									.getBytes());
		} else {
			return new GossipObj(currSnapshotVersion.toString(), null, null);
		}

	}

	private boolean validate(String pubkey, List<LocalFullNode> localFullnodes) {
		Instant first = Instant.now();
		if (StringUtils.isEmpty(pubkey)) {
			logger.error("pubkey is null.");
			return false;
		}
		boolean isValid = localFullnodes.parallelStream().filter(n -> n.getStatus() == NodeStatus.HAS_SHARDED)
				.anyMatch(p -> p.getPubkey().equals(pubkey));
		long handleInterval = Duration.between(first, Instant.now()).toMillis();
		if (handleInterval > 10) {
			logger.warn("Local2local interface validate public keys interval: {}", handleInterval);
		}
		return isValid;
	}

	/**
	 * 获取片shardId内未知events
	 * 
	 * @param shardId     片号
	 * @param otherCounts 最大seq数组
	 * @return 未知events
	 */
	private ArrayList<EventBody> getUnknownEvents(IEventStore myStore, int shardId, long[] otherCounts) {
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
//		logger.info("\n{} \ngetUnknownEvents(): requestor's seqs: {}, my seqs: {}, gossip event size = {}",
//				(null == connInfo ? null : connInfo.split("\\n")[1]), JSON.toJSONString(otherCounts),
//				JSON.toJSONString(currMyCounts), diffEvents.size());

		return cutResultUnknownEvents(diffEvents, Config.DEFAULT_SYNC_EVENT_COUNT);

	}

	private ArrayList<EventBody> cutResultUnknownEvents(ArrayList<EventBody> events, long size) {
		if (events.size() > size) {

//			logger.info("node-({},{}): cutResultUnknownEvents()... size: {}", node.getShardId(), node.getCreatorId(),
//					size);

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

}
