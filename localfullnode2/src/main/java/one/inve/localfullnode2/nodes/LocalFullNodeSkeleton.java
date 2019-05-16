package one.inve.localfullnode2.nodes;

import java.io.File;
import java.io.IOException;
import java.math.BigInteger;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;

import org.apache.log4j.PropertyConfigurator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alibaba.fastjson.JSONArray;
import com.zeroc.Ice.Util;

import one.inve.localfullnode2.conf.Config;
import one.inve.localfullnode2.conf.NodeParameters;
import one.inve.localfullnode2.dep.DepItemsManager;
import one.inve.localfullnode2.dep.items.AllQueues;
import one.inve.localfullnode2.http.HttpServiceDependency;
import one.inve.localfullnode2.membership.GossipNodeThread;
import one.inve.localfullnode2.staging.StagingArea;
import one.inve.localfullnode2.store.DbUtils;
import one.inve.localfullnode2.store.EventBody;
import one.inve.localfullnode2.utilities.FileLockUtils;
import one.inve.localfullnode2.utilities.HnKeyUtils;
import one.inve.localfullnode2.utilities.PathUtils;
import one.inve.localfullnode2.utilities.http.NettyHttpServer;

public abstract class LocalFullNodeSkeleton extends DepsPointcut implements NodeEnrolled {
	private static final Logger logger = LoggerFactory.getLogger(LocalFullNodeSkeleton.class);
	// private static Logger logger = null;

	private ConcurrentHashMap<String, BigInteger> accounts = new ConcurrentHashMap<>();
	private Map<Integer, LinkedBlockingQueue<EventBody>> shardSortQueue = new HashMap<>();

	public ConcurrentHashMap<String, BigInteger> getAccounts() {
		return accounts;
	}

	private void validateNode(String ip) {
		if (!Config.WHITE_LIST.contains(ip)) {
			logger.error("Invalid node: {}", ip);
			throw new Error("Invalid node");
		} else {
			logger.info("valid node: {}", ip);
		}
	}

	/**
	 * 判断是否已有运行进程
	 *
	 * @return 是否已有运行进程
	 */
	private boolean isRunning(String fileName) {
		logger.info(">>>>>> check if running a process.");
		FileLockUtils fileLockUtils = new FileLockUtils(PathUtils.getDataFileDir() + fileName);
		try {
			return !fileLockUtils.lock();
		} catch (IOException e) {
			logger.error("lock file error: {}", e);
			return false;
		}
	}

	/**
	 * 初始化
	 * 
	 * @param args 局部全节点运行所需参数 args[0]:
	 *             --Ice.Config={{default.config}}，注：这里的{{default.config}}是default.config文件的路径
	 */
	protected void start(String[] args) {
		registerDeps();

		try {
			setCommunicator(Util.initialize(args));
			NodeParameters np = nodeParameters();
			if (null == np) {
				np = new NodeParameters();
			}
			np.init(getCommunicator(), args);
			nodeParameters(np);

			logger.info("passed args in command line: {}", JSONArray.toJSONString(args));

			// 清除上一次的缓存
			initClearCache();

			// 初始化钱包
			initWallet(this.nodeParameters().prefix + Config.WALLET_FILE);
			logger.info("wallet mnemonic: {}", this.getWallet().getMnemonic());
			logger.info("wallet address : {}", this.getWallet().getAddress());

			// 初始化hnKey
			initHnKey(this.nodeParameters().prefix + Config.KEYS_FILE);
			if (isRunning(this.nodeParameters().prefix + Config.KEYS_FILE)) {
				logger.warn(">>>>>> There is already a running process!\nexit...");
				System.exit(0);
			}

			// 初始化节点信息
			setAdapter(this.generateAdapter(getCommunicator(), "LocalFullNodeAdapter",
					this.nodeParameters().selfGossipAddress.rpcPort));
			String seedPubIP = this.nodeParameters().seedGossipAddress.pubIP;
			String seedRpcPort = "" + this.nodeParameters().seedGossipAddress.rpcPort;

			// 注册
			asLocalFullNode(seedPubIP, seedRpcPort);

			// 请求分片信息
			// setShardId,setCreatorId,setShardCount,setnValue
			shardInfo(seedPubIP, seedRpcPort);

			int selfId = (int) this.getCreatorId();
			while (-1 == selfId) {
				shardInfo(seedPubIP, seedRpcPort);
				selfId = (int) this.getCreatorId();
			}

			nodeParameters().dbId = getShardId() + "_" + getCreatorId();
			dbId(nodeParameters().dbId);
//			// 1. 初始化智能合约数据库
//			logger.info("-------------> node id is: {} <--------------", (getShardId() + 1) * 100 + selfId);
//			// 设置全局变量，初始化世界状态
//			RepositoryProvider.getTrack(nodeParameters.dbId);
			// 初始化数据库： MySQL、RocksDB，创世交易记录
			DbUtils.initDataBase(this);

			// call seed node to get local full node list
			// setLocalFullNodes
			allLocalFullNodeList(seedPubIP, seedRpcPort);

			// 初始化黑白名单
			DbUtils.initBlackList(this);

			// 初始化统计信息
			DbUtils.initStatistics(this);

			buildShardSortQueue();

			// 初始化hashnet数据结构
			initHashnet(this);

			// 加入gossip网络
			new GossipNodeThread(this, HnKeyUtils.getString4PublicKey(publicKey())).start();

			// 启动http接口
			HttpServiceDependency httpServiceDependency = new HttpServiceDependency();
			httpServiceDependency.setNode(this);
			NettyHttpServer.boostrapHttpService(httpServiceDependency,
					this.nodeParameters().selfGossipAddress.httpPort);

			// 启动rpc接口
			loadRPC(this);

//			// 将新的eventbody添加到hashnet
//			new EventBody2HashnetThread(this).start();
//
//			// 片内共识
//			consensusThread = new GossipEventThread(this, Config.GOSSIP_IN_SHARD);
//			consensusThread.start();
//			// 片间同步
//			syncThread = new GossipEventThread(this, Config.GOSSIP_GLOBAL_SHARD);
//			syncThread.start();
//
//			// 新Event入库
//			new EventSaveThread(this).start();
//			// 提取共识event
//			new GetConsensusEventsThread(this).start();
//			// 共识event全排序
//			new ConsensusEventAllSortThread(this).start();
//			// 全排序共识event处理
//			new ConsensusEventHandleThread(this).start();
//			// 共识message签名验证
//			new ConsensusMessageVerifyThread(this).start();
//			// 共识message处理执行
//			new ConsensusMessageHandleThread(this).start();
//			// 共识message保存入库
//			new ConsensusMessageSaveThread(this).start();
//			// 共识系统自动生成交易入库
//			new ConsensusSystemAutoTxSaveThread(this).start();

			getCommunicator().waitForShutdown();
		} catch (Exception e) {
			logger.error("{}", e);
		}
	}

	protected void buildShardSortQueue() {
		AllQueues allQueues = DepItemsManager.getInstance().attachAllQueues(null);
		StagingArea stagingArea = allQueues.get();
		int shardCount = DepItemsManager.getInstance().attachShardCount(null).get();

		for (int i = 0; i < shardCount; i++) {
			synchronized (DbUtils.class) {
				if (stagingArea.getQueue(EventBody.class, StagingArea.ShardSortQueueName, i) == null) {
					LinkedBlockingQueue<EventBody> queueInstance = new LinkedBlockingQueue<>();
					stagingArea.createQueue(EventBody.class, StagingArea.ShardSortQueueName, 100000, i, null);
				}
			}
		}

		allQueues.set(stagingArea);

	}

//	/**
//	 * localfullnode入口
//	 *
//	 * @param args 参数
//	 */
//	public static void main(String[] args) {
//		StandaloneLocalFullNode lfn = new StandaloneLocalFullNode();
//
//		Log4jSystem log4jSystem = new Log4jSystem();
//		log4jSystem.overDefault(args);
//
//		logger = LoggerFactory.getLogger(StandaloneLocalFullNode.class);
//
//		lfn.init(args);
//	}

	// Francis 4/11/2019
	// divide the default log4j output by some command line arguments for
	// troubleshooting
	// ensure there is a CONF variable reference inside log4j.properties.
	public static class Log4jSystem {
		public void overDefault(String[] args) {
			String confValue = "nodefault";
			final String customLog4jConfFileName = "./log4j.properties";

			for (String arg : args) {
				if (arg.startsWith("--Ice.Config=")) {
					String iceConfigValue = arg.substring("--Ice.Config=".length());

					String splitted[] = iceConfigValue.split("\\.");

					confValue = splitted[0];
					break;
				}
			}

			System.setProperty("CONF", confValue);
			// URL url = this.getClass().getResource("/log4j.properties");
			// String path = url.toString();
			// path = path.substring(path.indexOf(":") + 1, path.length());

			File file = new File(customLog4jConfFileName);
			if (file.exists() && !file.isDirectory()) {
				PropertyConfigurator.configure("./log4j.properties");
			}

		}
	}

}
