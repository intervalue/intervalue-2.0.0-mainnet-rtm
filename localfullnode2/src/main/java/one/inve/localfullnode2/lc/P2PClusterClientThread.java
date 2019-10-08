package one.inve.localfullnode2.lc;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeSet;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import one.inve.bean.node.NodeStatus;
import one.inve.bean.node.NodeTypes;
import one.inve.cfg.localfullnode.Config;
import one.inve.localfullnode2.membership.GossipNodeThread;
import one.inve.localfullnode2.nodes.LocalFullNode1GeneralNode;
import one.inve.localfullnode2.nodes.WithSeed;
import one.inve.localfullnode2.p2pcluster.ic.P2PClusterClientBridge;
import one.inve.localfullnode2.store.rocks.RocksJavaUtil;
import one.inve.localfullnode2.utilities.StringUtils;

/**
 * 
 * 
 * Copyright © INVE FOUNDATION. All rights reserved.
 * 
 * @Description: substitute for {@code GossipNodeThread} using p2p-cluster.Note
 *               that "-Dp2pcluster.icport={icport}" must be specified.
 * @author: Francis.Deng
 * @date: Aug 5, 2019 1:25:45 AM
 * @version: V1.0
 * @see P2PClusterClientBridge
 * @see WithSeed
 * @see GossipNodeThread
 */
public class P2PClusterClientThread extends Thread {
	private static final Logger logger = LoggerFactory.getLogger(P2PClusterClientThread.class);
	private LocalFullNode1GeneralNode node;
	private String pubkey;
	private BigInteger evtCounts;
	private BigInteger consEvtCounts;
	private BigInteger messageCounts;

	private volatile boolean interrupted = false;

	private Map<String, String> meta;
	private P2PClusterClientBridge p2pClusterClientBridge;

	StringBuilder statisticInfo = new StringBuilder()
			.append("\n*************** node-({},{}): {}-th statistics ***************")
			.append("\n*****total  evts: {}\n*****total cons evts: {}\n*****total cons msgs: {}\n*****interval : {} sec")
			.append("\n*****cons evts: {}\n*****eps: {}\n*****cons msgs: {}\n*****tps: {}");

	public P2PClusterClientThread(LocalFullNode1GeneralNode node, String pubkey) {
		this.node = node;
		this.pubkey = pubkey;

		this.evtCounts = node.getTotalEventCount();
		this.consEvtCounts = node.getTotalConsEventCount();
		this.messageCounts = node.getConsMessageCount();

		this.meta = new HashMap<>();

		p2pClusterClientBridge = new P2PClusterClientBridge(
				Integer.parseInt(System.getProperty("p2pcluster.icport", "43010")));
	}

	protected void broadcastMeta() {
		meta.put("level", "" + NodeTypes.LOCALFULLNODE);
		meta.put("shard", node.getShardId() < 0 ? "" : "" + this.node.getShardId());
		meta.put("index", node.getCreatorId() < 0 ? "" : "" + this.node.getCreatorId());
		meta.put("rpcPort", "" + node.nodeParameters().selfGossipAddress.getRpcPort());
		meta.put("httpPort", "" + node.nodeParameters().selfGossipAddress.getHttpPort());
		meta.put("pubkey", this.pubkey);
		meta.put("address", this.node.getWallet().getAddress());

		// enable sharding
		int shardCount = this.node.getShardCount();
		if (shardCount > 0) {
			meta.put("shard", "" + this.node.getShardId());
			meta.put("index", "" + this.node.getCreatorId());
		}

		p2pClusterClientBridge.setMeta(meta);
	}

	protected void setAliveMembers() {
		int shardCount = this.node.getShardCount();
		List<String> blackPubkeys = new ArrayList<>();
		if (null != node.getBlackList4PubKey() && node.getBlackList4PubKey().size() > 0) {
			blackPubkeys.addAll(node.getBlackList4PubKey());
		}

		p2pClusterClientBridge.getMembers();// retrieve members from p2p-cluster via remoting call

		if (shardCount > 0) {
			logger.warn("node size(in localfullnode point of view): {}", p2pClusterClientBridge.otherMembers().size());
			logger.warn("shard node size(in localfullnode point of view): {}",
					p2pClusterClientBridge.findMembersByShardId("" + node.getShardId()).size() + 1);
			// 片内邻居池（不含自己）
			node.inshardNeighborPools(p2pClusterClientBridge.findMembersByShardId("" + node.getShardId()).stream()
					.filter(member -> "2".equals("" + member.metadata().get("level"))
							&& Config.WHITE_LIST.contains(member.address().host())
							&& !Config.BLACK_LIST.contains(member.address().host())
							&& validatePubkey("" + member.metadata().get("pubkey"))
							&& !blackPubkeys
									.contains(member.metadata().get("pubkey"))
							&& StringUtils.isNotEmpty("" + member.metadata().get("index")))
					.collect(Collectors.collectingAndThen(
							Collectors.toCollection(() -> new TreeSet<>(
									Comparator.comparing(o -> new StringBuilder().append(o.address().host()).append("_")
											.append(o.address().port()).append("_").append(o.metadata().get("rpcPort"))
											.append("_").append(o.metadata().get("httpPort")).toString()))),
							ArrayList::new)));
			// 全局邻居池（不含自己）
			node.globalNeighborPools(p2pClusterClientBridge.otherMembers().stream()
					.filter(member -> "2".equals("" + member.metadata().get("level"))
							&& Config.WHITE_LIST.contains(member.address().host())
							&& !Config.BLACK_LIST.contains(member.address().host())
							&& validatePubkey("" + member.metadata().get("pubkey"))
							&& !blackPubkeys.contains(member.metadata().get("pubkey"))
							&& StringUtils.isNotEmpty("" + member.metadata().get("shard"))
							&& StringUtils.isNotEmpty("" + member.metadata().get("index")))
					.collect(
							Collectors.collectingAndThen(
									Collectors.toCollection(() -> new TreeSet<>(
											Comparator.comparing(o -> new StringBuilder().append(o.address().host())
													.append("_").append(o.metadata().get("rpcPort")).append("_")
													.append(o.metadata().get("httpPort")).toString()))),
									ArrayList::new)));

		}
	}

	@Override
	public void run() {
		// super.start();
		logger.info(">>>>>> start membership network...");

//		meta.put("level", "" + NodeTypes.LOCALFULLNODE);
//		meta.put("shard", node.getShardId() < 0 ? "" : "" + this.node.getShardId());
//		meta.put("index", node.getCreatorId() < 0 ? "" : "" + this.node.getCreatorId());
//		meta.put("rpcPort", "" + node.nodeParameters().selfGossipAddress.getRpcPort());
//		meta.put("httpPort", "" + node.nodeParameters().selfGossipAddress.getHttpPort());
//		meta.put("pubkey", this.pubkey);
//		meta.put("address", this.node.getWallet().getAddress());

		boolean shardFlag = (this.node.getShardId() < 0 && this.node.getCreatorId() < 0);
		long index = 0;
		// while (true) {
		while (!this.interrupted) {
			index++;
			Instant first = Instant.now();

			// broad meta -- wait awhile --setAliveMembers loop on and on
			broadcastMeta();
			waitInterval(first);
			setAliveMembers();

			// 清理过期的消息hash缓存
			clearExpiredMessageHash(node, Instant.now().toEpochMilli());
			// 统计tps
			statisticsAndShowTpsInfo(index);

		}

		logger.warn("interrupted={}", this.interrupted);
		logger.warn("<<localfullnode's membership>> is stopped......");
		return;

	}

	/**
	 * 清理过期的消息hash缓存
	 */
	private void clearExpiredMessageHash(LocalFullNode1GeneralNode node, long nowTime) {
		long count = 0;
		for (Map.Entry<String, Long> entry : node.getMessageHashCache().entrySet()) {
			long value = entry.getValue();
			if (nowTime - value > 5 * 1000) {
				node.getMessageHashCache().remove(entry.getKey());
				count++;
			}
		}
		if (count > 0) {
			logger.info("node-({}, {}): MessageHashCache remove {} expired key.", node.getShardId(),
					node.getCreatorId(), count);
		}
	}

	/**
	 * 统计和显示tps信息
	 * 
	 * @param index 第index轮循环
	 */
	private void statisticsAndShowTpsInfo(long index) {
		int freq = 15;
		if (index % freq == 0) {
			BigInteger totalEventCount = node.getTotalEventCount();
			BigInteger totalConsEventCount = node.getTotalConsEventCount();
			BigInteger totalMsgCount = node.getConsMessageCount();

			BigInteger newConsEventCount = totalConsEventCount.subtract(consEvtCounts);
			BigInteger newMgsCount = totalMsgCount.subtract(messageCounts);

			evtCounts = totalEventCount;
			consEvtCounts = totalConsEventCount;
			messageCounts = totalMsgCount;

			BigDecimal interval = new BigDecimal(Config.DEFAULT_GOSSIP_NODE_INTERVAL * freq / 1000.0);
			BigDecimal eps = new BigDecimal(newConsEventCount).divide(interval, 2, BigDecimal.ROUND_HALF_UP);
			BigDecimal tps = new BigDecimal(newMgsCount).divide(interval, 2, BigDecimal.ROUND_HALF_UP);

			setMaxTps(tps);

			logger.info(statisticInfo.toString(), node.getShardId(), node.getCreatorId(), index / freq, totalEventCount,
					totalConsEventCount, totalMsgCount, Config.DEFAULT_GOSSIP_NODE_INTERVAL * freq / 1000.0,
					newConsEventCount, eps, newMgsCount, tps);
		}
	}

	/**
	 * 保存最大tps
	 * 
	 * @param tps
	 */
	public void setMaxTps(BigDecimal tps) {
		try {
			if (tps != null) {
				RocksJavaUtil rocksJavaUtil = new RocksJavaUtil(node.nodeParameters().dbId);
				byte[] tpsByte = rocksJavaUtil.get(Config.MESSAGE_TPS_KEY);
				if (tpsByte != null && tpsByte.length > 0) {
					BigDecimal rocksTPS = new BigDecimal(new String(tpsByte));
					if (tps.compareTo(rocksTPS) > 0) {
						rocksJavaUtil.put(Config.MESSAGE_TPS_KEY, tps + "");
					}
				} else {
					rocksJavaUtil.put(Config.MESSAGE_TPS_KEY, tps + "");
				}
			}
		} catch (Exception ex) {
			logger.error("保存tps报错", ex);
		}
	}

	/**
	 * 等待轮循时间过去
	 * 
	 * @param first 开始事件
	 */
	private void waitInterval(Instant first) {
		long handleInterval = Duration.between(first, Instant.now()).toMillis();
		if (handleInterval < Config.DEFAULT_GOSSIP_NODE_INTERVAL) {
			try {
				Thread.sleep(Config.DEFAULT_GOSSIP_NODE_INTERVAL - handleInterval);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}

	/**
	 * 校验公钥合法性
	 * 
	 * @param pubkeyStr 公钥
	 * @return 是否合法
	 */
	private boolean validatePubkey(String pubkeyStr) {
		if (StringUtils.isEmpty(pubkeyStr)) {
			logger.error("pubkey is null.");
			return false;
		}
//        try {
//            HnKeyUtils.getPublicKey4String(pubkeyStr);
//        } catch (Exception e) {
//            logger.error("validate local full node publickey {}. error: {}", pubkeyStr, e.getMessage());
//            return false;
//        }

		return node.getLocalFullNodes().parallelStream().filter(n -> n.getStatus() == NodeStatus.HAS_SHARDED)
				.anyMatch(p -> p.getPubkey().equals(pubkeyStr));
	}

	public void interruptMe() {
		this.interrupted = true;
	}
}
