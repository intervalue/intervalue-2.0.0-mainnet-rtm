package one.inve.localfullnode2.nodes;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.math.BigInteger;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import org.apache.log4j.PropertyConfigurator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alibaba.fastjson.JSONArray;
import com.zeroc.Ice.Object;
import com.zeroc.Ice.Util;

import one.inve.localfullnode2.conf.Config;
import one.inve.localfullnode2.conf.NodeParameters;
import one.inve.localfullnode2.dep.DepItemsManager;
import one.inve.localfullnode2.dep.items.AllQueues;
import one.inve.localfullnode2.hashnet.Hashneter;
import one.inve.localfullnode2.http.HttpServiceDependency;
import one.inve.localfullnode2.lc.ILifecycle;
import one.inve.localfullnode2.lc.LazyLifecycle;
import one.inve.localfullnode2.staging.StagingArea;
import one.inve.localfullnode2.store.DbUtils;
import one.inve.core.EventBody;
import one.inve.localfullnode2.utilities.FileLockUtils;
import one.inve.localfullnode2.utilities.GracefulShutdown;
import one.inve.localfullnode2.utilities.PathUtils;
import one.inve.localfullnode2.utilities.http.NettyHttpServer;

/**
 * Copyright © CHXX Co.,Ltd. All rights reserved.
 * 
 * @Description: Core startup class
 * @author: Francis.Deng
 * @date: May 31, 2018 3:06:25 AM
 * @version: V1.0
 */
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
		loadDeps();
		GracefulShutdown gs = GracefulShutdown.with("TERM");// kill -15 process-id

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

			initOlympus();

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
				logger.warn("fall into a loop of asking for shard info from ({}:{})", seedPubIP, seedRpcPort);

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
			// initHashnet(this);
			Hashneter hashneter = initHashneter();
			if (hashneter == null)
				System.exit(-1);

			ILifecycle membersTask = startMembership(this);
			// membersTask.start();
			gs.addLcs(membersTask);

			// start up http server
			ILifecycle httpServer = startHttpServer(this);
			gs.addLcs(httpServer);

			// start up rpc server
			// loadRPC(this);
			ILifecycle rpcServer = startRPCServer(this);
			gs.addLcs(rpcServer);

			TimeUnit.SECONDS.sleep(5);

			ILifecycle coreTask = performCoreTasks(hashneter);
			gs.addLcs(coreTask);

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

	// initialize shardSortQueue($shardCount)
	protected void buildShardSortQueue() {
		AllQueues allQueues = DepItemsManager.getInstance().attachAllQueues(null);
		StagingArea stagingArea = allQueues.get();
		int shardCount = DepItemsManager.getInstance().attachShardCount(null).get();

		for (int i = 0; i < shardCount; i++) {
			synchronized (DbUtils.class) {
				if (stagingArea.getQueue(EventBody.class, StagingArea.ShardSortQueueName, i) == null) {
					LinkedBlockingQueue<EventBody> queueInstance = new LinkedBlockingQueue<>();
					stagingArea.createQueue(EventBody.class, StagingArea.ShardSortQueueName, Integer.MAX_VALUE, i,
							null);
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

	protected void initOlympus() {
		// contract module should have awareness of of them
		one.inve.contract.conf.Config.setCreationAddress(Config.CREATION_ADDRESSES);
		one.inve.contract.conf.Config.setGodAddress(Config.GOD_ADDRESS);
		one.inve.contract.conf.Config.setFoundationAddress(Config.FOUNDATION_ADDRESS);
	}

	protected ILifecycle startHttpServer(LocalFullNode1GeneralNode node) {
		LazyLifecycle llc = new LazyLifecycle() {
			private NettyHttpServer httpServer;

			@Override
			public void start() {
				super.start();

				// 启动http接口
				HttpServiceDependency httpServiceDependency = new HttpServiceDependency();
				httpServiceDependency.setNode(node);

				int port = node.nodeParameters().selfGossipAddress.httpPort;
				logger.info("Http server is listening to {}", port);
				httpServer = NettyHttpServer.boostrap(httpServiceDependency, port, 1);
			}

			@Override
			public void stop() {
				httpServer.shutdown();
				super.stop();

				logger.info("<<http server>> is stopped......");
			}

		};
		llc.start();

		return llc;
	}

	protected ILifecycle startRPCServer(LocalFullNode1GeneralNode node) {
		LazyLifecycle llc = new LazyLifecycle() {

			@Override
			public void start() {
				super.start();

				logger.info("start rpc service...");
				try {
					// add rpc
					for (int i = 0; i < Config.SERVICE_ARRAY.length; i++) {
						Class<?> t = Class.forName(Config.SERVICE_ARRAY[i]);
						Constructor<Object> cons = (Constructor<Object>) t
								.getConstructor(LocalFullNode1GeneralNode.class);
						Object object = cons.newInstance(node);

						String identity = Config.SERVICE_ARRAY[i]
								.substring(Config.SERVICE_ARRAY[i].lastIndexOf('.') + 1);
						if (identity.toLowerCase().endsWith("impl")) {
							identity = identity.substring(0, identity.length() - 4);
						}
						getAdapter().add(object, Util.stringToIdentity(identity));
					}
					getAdapter().activate();
				} catch (Exception e) {
					logger.error("load rpc error: {}", e);
				}
			}

			@Override
			public void stop() {
				// getAdapter().deactivate();
				// getAdapter().destroy();
				// getCommunicator().shutdown();
				getCommunicator().destroy();
//				while (!getCommunicator().isShutdown()) {
//					logger.info("communicator shutdown is going on");
//				}
				super.stop();

				logger.info("<<rpc server>> is stopped......");
			}

		};
		llc.start();

		return llc;
	}

	abstract protected Hashneter initHashneter();

	abstract protected ILifecycle startMembership(LocalFullNode1GeneralNode node);

	abstract protected ILifecycle performCoreTasks(Hashneter hashneter);

}
