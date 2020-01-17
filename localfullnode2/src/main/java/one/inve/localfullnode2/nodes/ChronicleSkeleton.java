package one.inve.localfullnode2.nodes;

import java.io.IOException;
import java.math.BigInteger;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alibaba.fastjson.JSONArray;
import com.zeroc.Ice.Util;

import one.inve.cfg.core.DBConnectionDescriptorsConf;
import one.inve.cfg.core.IConfImplant;
import one.inve.cfg.core.InterValueConfImplant;
import one.inve.cfg.localfullnode.Config;
import one.inve.core.EventBody;
import one.inve.localfullnode2.dep.DepItemsManager;
import one.inve.localfullnode2.dep.items.AllQueues;
import one.inve.localfullnode2.firstseq.EventStoreBility;
import one.inve.localfullnode2.firstseq.FirstSeqsDependency;
import one.inve.localfullnode2.firstseq.FirstSeqsbility;
import one.inve.localfullnode2.http.HttpServiceDependency;
import one.inve.localfullnode2.lc.ILifecycle;
import one.inve.localfullnode2.lc.LazyLifecycle;
import one.inve.localfullnode2.rpc.mgmt.LocalFullNode2RPCInvocationDriver;
import one.inve.localfullnode2.staging.StagingArea;
import one.inve.localfullnode2.store.DbUtils;
import one.inve.localfullnode2.utilities.FileLockUtils;
import one.inve.localfullnode2.utilities.GracefulShutdown;
import one.inve.localfullnode2.utilities.PathUtils;
import one.inve.localfullnode2.utilities.http.NettyHttpServer;

/**
 * 
 * Copyright © INVE FOUNDATION. All rights reserved.
 * 
 * @ClassName: ChronicleSkeleton
 * @Description: has part of LocalFullNodeSkeleton function like
 *               wallet,registration,sharding remained.The key function is
 *               chronicle services.
 * @author Francis.Deng [francis_xiiiv@163.com]
 * @date Jan 15, 2020
 *
 */
public abstract class ChronicleSkeleton extends DepsPointcut implements NodeEnrolled {
	private static final Logger logger = LoggerFactory.getLogger(ChronicleSkeleton.class);
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
			// command line - intervalue_conf_file=your configuration file
			// directory - ./intervalue_conf_file.yaml
			// environment - intervalue_conf_file=your configuration file
			DBConnectionDescriptorsConf dbConnectionDescriptorsConf = loadConf(args);

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

			// initOlympus();

			// 694020aee76b4e4a955dd5e899c69874
			// enable ice rpc driver module to keep a close eye on rpc
//			LocalFullNode2RPCInvocationDriver rpcInvocationDriver = new LocalFullNode2RPCInvocationDriver(
//					getCommunicator(), this.nodeParameters().selfGossipAddress.rpcPort, this);

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

			DbUtils.initDataBase(this, dbConnectionDescriptorsConf);

			// call seed node to get local full node list
			// setLocalFullNodes
			allLocalFullNodeList(seedPubIP, seedRpcPort);

			// 初始化黑白名单
			DbUtils.initBlackList(this);

			// 初始化统计信息
			DbUtils.initStatistics(this);

			buildShardSortQueue();

			// build indexes for old,rusty messages and system messages in mysql.see {@
			// Indexer}
			// buildMessagesAndSysMessagesIndexOnce();

			// initSnapshotData();
			// 初始化hashnet数据结构
//			Hashneter hashneter = initHashneter();
//			if (hashneter == null)
//				System.exit(-1);

			// for the sake of system stabilization,the first seqs initialization is put
			// here.
			// probeFirstSeqs();

			// ILifecycle membersTask = startMembership(this);
			// membersTask.start();
			// gs.addLcs(membersTask);

//			while (inshardNeighborPools() == null || inshardNeighborPools().isEmpty()) {
//				logger.warn("#####    #####  ###### ###### #####  ");
//				logger.warn("    #     #    # #      #      #    # ");
//				logger.warn("   #      #    # #####  #####  #    # ");
//				logger.warn("  #       #####  #      #      #####  ");
//				logger.warn(" #        #      #      #      #   #  ");
//				logger.warn("######    #      ###### ###### #    # ");
//				logger.warn("z peer");
//				logger.warn("");
//
//				Thread.sleep(5000);
//			}

			// start up http server
//			ILifecycle httpServer = startHttpServer(this);
//			gs.addLcs(httpServer);

			// start up rpc server
			// 694020aee76b4e4a955dd5e899c69874
			// enable ice rpc driver module to keep a close eye on rpc
			// ILifecycle rpcServer = startRPCServer(this);
//			ILifecycle rpcServer = startRPCServer(rpcInvocationDriver, this);
//			gs.addLcs(rpcServer);
//
//			TimeUnit.SECONDS.sleep(5);
//
//			ILifecycle coreTask = performCoreTasks(hashneter);
//			gs.addLcs(coreTask);

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

	// 694020aee76b4e4a955dd5e899c69874
	// enable ice rpc driver module to keep a close eye on rpc
	// protected ILifecycle startRPCServer(LocalFullNode1GeneralNode node) {
	protected ILifecycle startRPCServer(LocalFullNode2RPCInvocationDriver rpcInvocationDriver,
			LocalFullNode1GeneralNode node) {
		LazyLifecycle llc = new LazyLifecycle() {

			@Override
			public void start() {
				super.start();

				logger.info("start rpc service...");
//				try {
//					// add rpc
//					for (int i = 0; i < Config.SERVICE_ARRAY.length; i++) {
//						Class<?> t = Class.forName(Config.SERVICE_ARRAY[i]);
//						Constructor<Object> cons = (Constructor<Object>) t
//								.getConstructor(LocalFullNode1GeneralNode.class);
//						Object object = cons.newInstance(node);
//
//						String identity = Config.SERVICE_ARRAY[i]
//								.substring(Config.SERVICE_ARRAY[i].lastIndexOf('.') + 1);
//						if (identity.toLowerCase().endsWith("impl")) {
//							identity = identity.substring(0, identity.length() - 4);
//						}
//						getAdapter().add(object, Util.stringToIdentity(identity));
//					}
//					getAdapter().activate();
//				} catch (Exception e) {
//					logger.error("load rpc error: {}", e);
//				}
				rpcInvocationDriver.registerServices();
				rpcInvocationDriver.activateServices();
			}

			@Override
			public void stop() {
				// getAdapter().deactivate();
				// getAdapter().destroy();
				// getCommunicator().shutdown();
				// getCommunicator().destroy();
//				while (!getCommunicator().isShutdown()) {
//					logger.info("communicator shutdown is going on");
//				}
				rpcInvocationDriver.getCommunicator().shutdown();

				super.stop();

				logger.info("<<rpc server>> is stopped......");
			}

		};
		llc.start();

		return llc;
	}

	protected void probeFirstSeqs() {
		FirstSeqsDependency newFirstSeqsDep = DepItemsManager.getInstance().getItemConcerned(FirstSeqsDependency.class);
		FirstSeqsbility firstSeqsbility = new FirstSeqsbility();

		firstSeqsbility.probe(newFirstSeqsDep,
				new EventStoreBility(newFirstSeqsDep.getDbId(), newFirstSeqsDep.getLastSeqs()));
	}

	// abstract protected Hashneter initHashneter();

	// abstract protected ILifecycle startMembership(LocalFullNode1GeneralNode
	// node);

	// abstract protected ILifecycle performCoreTasks(Hashneter hashneter);

	// abstract protected void buildMessagesAndSysMessagesIndexOnce();

	protected IConfImplant loadConfObject(String[] args) {
		InterValueConfImplant implant = new InterValueConfImplant();
		implant.init(args);

		return implant;
	}

	protected DBConnectionDescriptorsConf loadConf(String[] args) {
		IConfImplant implant = loadConfObject(args);
		implant.implantStaticConfig();
		implant.implantEnv();
		setCommunicator(Util.initialize(implant.implantZerocConf()));
		nodeParameters(implant.implantNodeParameters());

		return implant.getDbConnection();
	}

//	protected void initSnapshotData() {
//		DepItemsManager.getInstance().attachSS(null).setContributions(new HashSet<>());
//		DepItemsManager.getInstance().attachSS(null).setTreeRootMap(new HashMap<>());
//		DepItemsManager.getInstance().attachSS(null).setSnapshotPointMap(new HashMap<>());
//		DepItemsManager.getInstance().attachSS(null).setMsgHashTreeRoot(null);
//		DepItemsManager.getInstance().attachSS(null).setTotalFeeBetween2Snapshots(BigInteger.ZERO);
//		DepItemsManager.getInstance().attachSS(null).setSnapshotMessage(null);
//	}

}
