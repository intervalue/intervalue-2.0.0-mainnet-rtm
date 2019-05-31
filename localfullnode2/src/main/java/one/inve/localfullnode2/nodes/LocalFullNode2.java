package one.inve.localfullnode2.nodes;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

import one.inve.bean.node.LocalFullNode;
import one.inve.localfullnode2.hashnet.Hashneter;
import one.inve.localfullnode2.lc.FormalEventMessageLoop;
import one.inve.localfullnode2.lc.ILifecycle;
import one.inve.localfullnode2.lc.LazyLifecycle;
import one.inve.localfullnode2.membership.GossipNodeThread;
import one.inve.localfullnode2.message.service.TransactionDbService;
import one.inve.localfullnode2.rpc.RegisterPrx;
import one.inve.localfullnode2.rpc.RpcConnectionService;
import one.inve.localfullnode2.store.rocks.ShardInfo;
import one.inve.localfullnode2.utilities.HnKeyUtils;
import one.inve.localfullnode2.utilities.StringUtils;

/**
 * 
 * Copyright © CHXX Co.,Ltd. All rights reserved.
 * 
 * @Description: The class is able to communicate with seed node to complete the
 *               tasks like registering,retrieving sharding,maintaining
 *               membership.
 * @author: Francis.Deng
 * @date: May 14, 2019 11:34:32 PM
 * @version: V1.0
 */
public class LocalFullNode2 extends HashneterInitializer {
	private static final Logger logger = LoggerFactory.getLogger(LocalFullNode2.class);

	@Override
	public void asLocalFullNode(String seedPubIP, String seedRpcPort) {
		logger.info("node-{}: register as a local full node...", this.nodeParameters().selfGossipAddress.rpcPort);
		try {
			RegisterPrx prx = RpcConnectionService.buildConnection2Seed(getCommunicator(), seedPubIP, seedRpcPort);
			String pubkey = HnKeyUtils.getString4PublicKey(this.publicKey());
			String address = this.getWallet().getAddress();
			String result = prx.registerLocalFullNode(pubkey, address);
			JSONObject object = JSONObject.parseObject(result);
			if (object.getString("code").equals("200")) {
				logger.info("register as a local full node success!!!");
			} else {
				logger.warn("register as a local full node failed: {}\nexit...", object.getString("data"));
				System.exit(-1);
			}
		} catch (Exception e) {
			logger.error("error: {}\nexit...", e);
			System.exit(-1);
		}
	}

	@Override
	public void shardInfo(String seedPubIP, String seedRpcPort) {
		logger.info("request self shard info...");
		String pubkey = HnKeyUtils.getString4PublicKey(publicKey());
		try {
			RegisterPrx prx = RpcConnectionService.buildConnection2Seed(getCommunicator(), seedPubIP, seedRpcPort);
			String shardInfoMsg = prx.getShardInfoList();
			if (StringUtils.isNotEmpty(shardInfoMsg)) {
				JSONObject object = JSON.parseObject(shardInfoMsg);
				if (object.getString("code").equalsIgnoreCase("200")) {
					String shardInfoStr = object.getString("data");
					if (!StringUtils.isEmpty(shardInfoStr)) {
						List<ShardInfo> shardInfos = JSONArray.parseArray(shardInfoStr, ShardInfo.class);
						shardInfos.stream().forEach(shardInfo -> {
							if (StringUtils.isEmpty(shardInfo.getShard())
									|| StringUtils.isEmpty(shardInfo.getIndex())) {
								logger.error("shard info exception: {}\nexit...", shardInfoStr);
								System.exit(-1);
							} else if (pubkey.equals(shardInfo.getPubkey())) {
								this.setShardId(Integer.parseInt(shardInfo.getShard()));
								this.setCreatorId(Long.parseLong(shardInfo.getIndex()));
								logger.info("shardId: {}", this.getShardId());
								logger.info("creatorId: {}", this.getCreatorId());
							} else {
								logger.warn("\nthis pubkey : {} \nother pubkey: {}", pubkey, shardInfo.getPubkey());
							}
						});

						this.setShardCount((int) shardInfos.stream().map(ShardInfo::getShard).distinct().count());
						this.setnValue(shardInfos.size() / this.getShardCount());
						logger.info("shardCount: {}", this.getShardCount());
						logger.info("nValue: {}", this.getnValue());
						return;
					}
				} else {
					logger.info(object.getString("data"));
				}
			}

			logger.warn("wait for consensus shard info...");
			Thread.sleep(5000);
			shardInfo(seedPubIP, seedRpcPort);

		} catch (Exception e) {
			logger.error("error: {}\nexit...", e);
			System.exit(-1);
		}

		// 打印节点相关信息
		printNodeInfo();
	}

	@Override
	public void allLocalFullNodeList(String seedPubIP, String seedRpcPort) {
		logger.info("request all local full node list...");
		String result;
		try {
			RegisterPrx registerPrx = RpcConnectionService.buildConnection2Seed(getCommunicator(), seedPubIP,
					seedRpcPort);
			result = registerPrx.getLocalFullNodeList(HnKeyUtils.getString4PublicKey(publicKey()));
			if (null == result || JSONObject.parseObject(result).getInteger("code") != 200) {
				throw new Exception("");
			}
		} catch (Exception e) {
			logger.warn("wait for all local full node list...");
			try {
				Thread.sleep(2000);
			} catch (InterruptedException e1) {
				e1.printStackTrace();
			}
			allLocalFullNodeList(seedPubIP, seedRpcPort);
			return;
		}

		String nodeListStr = JSONObject.parseObject(result).getString("data");
		this.setLocalFullNodes(JSONArray.parseArray(nodeListStr, LocalFullNode.class));

		// 入库
		if (!(new TransactionDbService()).saveLocalFullNodes2Database(this.getLocalFullNodes(),
				nodeParameters().dbId)) {
			logger.error("local full node list save into database failure.\nexit...");
			System.exit(-1);
		}
	}

	/**
	 * 打印节点信息
	 */
	private void printNodeInfo() {
		logger.info(">>>>>> pubkey: {}", HnKeyUtils.getString4PublicKey(publicKey()));
		logger.info(">>>>>> pubIP: {}", this.nodeParameters().selfGossipAddress.pubIP);
		logger.info(">>>>>> rpcPort: {}", this.nodeParameters().selfGossipAddress.rpcPort);
		logger.info(">>>>>> gossipPort: {}", this.nodeParameters().selfGossipAddress.gossipPort);
		logger.info(">>>>>> shard: {}", this.getShardId());
		logger.info(">>>>>> index: {}", this.getCreatorId());
	}

	@Override
	protected ILifecycle performCoreTasks(Hashneter hashneter) {
		ILifecycle lc = new FormalEventMessageLoop();
		lc.start();

		return lc;

	}

	@Override
	protected ILifecycle startMembership(LocalFullNode1GeneralNode node) {
		LazyLifecycle llc = new LazyLifecycle() {
			private Thread gossipTh;

			@Override
			public void start() {
				super.start();

				// 加入gossip网络
				gossipTh = new GossipNodeThread(node, HnKeyUtils.getString4PublicKey(publicKey()));
				gossipTh.start();
			}

			@Override
			public void stop() {
				gossipTh.interrupt();
				super.stop();

				logger.info("<<membership>> is stopped......");
			}
		};
		llc.start();

		return llc;
	}

}
