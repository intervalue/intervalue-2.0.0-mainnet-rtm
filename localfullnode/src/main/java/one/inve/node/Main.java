package one.inve.node;

import java.io.File;
import java.io.IOException;
import java.math.BigInteger;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;

import org.apache.log4j.PropertyConfigurator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.zeroc.Ice.Util;

import one.inve.bean.node.LocalFullNode;
import one.inve.beans.dao.ShardInfo;
import one.inve.contract.provider.RepositoryProvider;
import one.inve.core.Config;
import one.inve.core.EventBody;
import one.inve.http.NettyHttpServer;
import one.inve.rpc.fullnode.RegisterPrx;
import one.inve.service.TransactionDbService;
import one.inve.threads.gossip.GossipEventThread;
import one.inve.threads.gossip.GossipNodeThread;
import one.inve.threads.localfullnode.ConsensusEventAllSortThread;
import one.inve.threads.localfullnode.ConsensusEventHandleThread;
import one.inve.threads.localfullnode.ConsensusMessageHandleThread;
import one.inve.threads.localfullnode.ConsensusMessageSaveThread;
import one.inve.threads.localfullnode.ConsensusMessageVerifyThread;
import one.inve.threads.localfullnode.ConsensusSystemAutoTxSaveThread;
import one.inve.threads.localfullnode.EventBody2HashnetThread;
import one.inve.threads.localfullnode.EventSaveThread;
import one.inve.threads.localfullnode.GetConsensusEventsThread;
import one.inve.util.DbUtils;
import one.inve.util.FileLockUtils;
import one.inve.util.HnKeyUtils;
import one.inve.util.PathUtils;
import one.inve.util.StringUtils;

/**
 * Francis.Deng replace parent{@link GeneralNode} with
 * {@link Localfullnode2Pointcut},which helps localfullnode2 obtain some
 * information.
 * 
 */
public class Main extends Localfullnode2Pointcut/* GeneralNode */ {
	// private static final Logger logger = LoggerFactory.getLogger(Main.class);
	private static Logger logger = null;

	private GossipEventThread consensusThread;
	private GossipEventThread syncThread;

	private ConcurrentHashMap<String, BigInteger> accounts = new ConcurrentHashMap<>();
	private Map<Integer, LinkedBlockingQueue<EventBody>> shardSortQueue = new HashMap<>();

	public Main() {
	}

	public GossipEventThread getConsensusThread() {
		return consensusThread;
	}

	public GossipEventThread getSyncThread() {
		return syncThread;
	}

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
	 * 打印节点信息
	 */
	private void printNodeInfo() {
		logger.info(">>>>>> pubkey: {}", HnKeyUtils.getString4PublicKey(publicKey));
		logger.info(">>>>>> pubIP: {}", this.nodeParameters.selfGossipAddress.pubIP);
		logger.info(">>>>>> rpcPort: {}", this.nodeParameters.selfGossipAddress.rpcPort);
		logger.info(">>>>>> gossipPort: {}", this.nodeParameters.selfGossipAddress.gossipPort);
		logger.info(">>>>>> shard: {}", this.getShardId());
		logger.info(">>>>>> index: {}", this.getCreatorId());
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
	 * 向seed注册成为局部全节点
	 * 
	 * @param seedPubIP   seed ip地址
	 * @param seedRpcPort seed rpc端口
	 */
	private void registerAsLocalFullNode(String seedPubIP, String seedRpcPort) {
		logger.info("node-{}: register as a local full node...", this.nodeParameters.selfGossipAddress.rpcPort);
		try {
			RegisterPrx prx = RpcConnectionService.buildConnection2Seed(getCommunicator(), seedPubIP, seedRpcPort);
			String pubkey = HnKeyUtils.getString4PublicKey(this.publicKey);
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

	/**
	 * 向全节点请求自己的分片信息
	 * 
	 * @param seedPubIP   seed ip地址
	 * @param seedRpcPort seed rpc端口
	 */
	private void requestShardInfo(String seedPubIP, String seedRpcPort) {
		logger.info("request self shard info...");
		String pubkey = HnKeyUtils.getString4PublicKey(this.publicKey);
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
			requestShardInfo(seedPubIP, seedRpcPort);

		} catch (Exception e) {
			logger.error("error: {}\nexit...", e);
			System.exit(-1);
		}

		// 打印节点相关信息
		printNodeInfo();
	}

	/**
	 * 请求所有局部全节点的信息
	 * 
	 * @param seedPubIP   seed公网IP
	 * @param seedRpcPort seed rpc端口
	 */
	private void requestAllLocalFullNodeList(String seedPubIP, String seedRpcPort) {
		logger.info("request all local full node list...");
		String result;
		try {
			RegisterPrx registerPrx = RpcConnectionService.buildConnection2Seed(getCommunicator(), seedPubIP,
					seedRpcPort);
			result = registerPrx.getLocalFullNodeList(HnKeyUtils.getString4PublicKey(this.publicKey));
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
			requestAllLocalFullNodeList(seedPubIP, seedRpcPort);
			return;
		}

		String nodeListStr = JSONObject.parseObject(result).getString("data");
		this.setLocalFullNodes(JSONArray.parseArray(nodeListStr, LocalFullNode.class));

		// 入库
		if (!TransactionDbService.saveLocalFullNodes2Database(this.getLocalFullNodes(), nodeParameters.dbId)) {
			logger.error("local full node list save into database failure.\nexit...");
			System.exit(-1);
		}
	}

	/**
	 * 初始化
	 * 
	 * @param args 局部全节点运行所需参数 args[0]:
	 *             --Ice.Config={{default.config}}，注：这里的{{default.config}}是default.config文件的路径
	 */
	private void init(String[] args) {
		try {
			setCommunicator(Util.initialize(args));
			if (null == this.nodeParameters) {
				this.nodeParameters = new NodeParameters();
			}
			this.nodeParameters.init(getCommunicator(), args);
			logger.warn("params: {}", JSONArray.toJSONString(args));

			// 清除上一次的缓存
			initClearCache();

			// 初始化钱包
			initWallet(this.nodeParameters.prefix + Config.WALLET_FILE);
			logger.info("wallet mnemonic: {}", this.getWallet().getMnemonic());
			logger.info("wallet address : {}", this.getWallet().getAddress());

			// 初始化hnKey
			initHnKey(this.nodeParameters.prefix + Config.KEYS_FILE);
			if (isRunning(this.nodeParameters.prefix + Config.KEYS_FILE)) {
				logger.warn(">>>>>> There is already a running process!\nexit...");
				System.exit(0);
			}

			// 初始化节点信息
			setAdapter(this.generateAdapter(getCommunicator(), "LocalFullNodeAdapter",
					this.nodeParameters.selfGossipAddress.rpcPort));
			String seedPubIP = this.nodeParameters.seedGossipAddress.pubIP;
			String seedRpcPort = "" + this.nodeParameters.seedGossipAddress.rpcPort;

			// 注册
			registerAsLocalFullNode(seedPubIP, seedRpcPort);

			// 请求分片信息
			requestShardInfo(seedPubIP, seedRpcPort);

			int selfId = (int) this.getCreatorId();
			while (-1 == selfId) {
				requestShardInfo(seedPubIP, seedRpcPort);
				selfId = (int) this.getCreatorId();
			}

			nodeParameters.dbId = getShardId() + "_" + getCreatorId();
			// 1. 初始化智能合约数据库
			logger.info("-------------> node id is: {} <--------------", (getShardId() + 1) * 100 + selfId);
			// 设置全局变量，初始化世界状态
			RepositoryProvider.getTrack(nodeParameters.dbId);
			// 初始化数据库： MySQL、RocksDB，创世交易记录
			DbUtils.initDataBase(this);

			// 向全节点请求所有局部全节点信息
			requestAllLocalFullNodeList(seedPubIP, seedRpcPort);

			// 初始化黑白名单
			DbUtils.initBlackList(this);

			// 初始化统计信息
			DbUtils.initStatistics(this);

			// 初始化hashnet数据结构
			initHashnet(this);

			// 将新的eventbody添加到hashnet
			new EventBody2HashnetThread(this).start();

			// 加入gossip网络
			new GossipNodeThread(this, HnKeyUtils.getString4PublicKey(publicKey)).start();

			// 启动http接口
			NettyHttpServer.boostrapHttpService(this);

			// 启动rpc接口
			loadRPC(this);

			// 片内共识
			consensusThread = new GossipEventThread(this, Config.GOSSIP_IN_SHARD);
			consensusThread.start();
			// 片间同步
			syncThread = new GossipEventThread(this, Config.GOSSIP_GLOBAL_SHARD);
			syncThread.start();

			// 新Event入库
			new EventSaveThread(this).start();
			// 提取共识event
			new GetConsensusEventsThread(this).start();
			// 共识event全排序
			new ConsensusEventAllSortThread(this).start();
			// 全排序共识event处理
			new ConsensusEventHandleThread(this).start();
			// 共识message签名验证
			new ConsensusMessageVerifyThread(this).start();
			// 共识message处理执行
			new ConsensusMessageHandleThread(this).start();
			// 共识message保存入库
			new ConsensusMessageSaveThread(this).start();
			// 共识系统自动生成交易入库
			new ConsensusSystemAutoTxSaveThread(this).start();

			getCommunicator().waitForShutdown();
		} catch (Exception e) {
			logger.error("{}", e);
		}
	}

	/**
	 * localfullnode入口
	 *
	 * @param args 参数
	 */
	public static void main(String[] args) {
		Main main = new Main();

		// Francis 4/2/2019
		// distinguish lfn's log4j outputting file by placing different directory. The
		// directory names are like "logs.1default","logs.0default","logs.nodefault"
		Log4jSystem log4jSystem = new Log4jSystem();
		log4jSystem.overDefault(args);

		logger = LoggerFactory.getLogger(Main.class);

		main.init(args);
	}

	/**
	 * 查询排序队列
	 * 
	 * @param shardId 分片Id
	 * @return 排序队列
	 */
	public LinkedBlockingQueue<EventBody> getShardSortQueue(int shardId) {
		if (shardSortQueue.get(shardId) == null) {
			synchronized (DbUtils.class) {
				if (shardSortQueue.get(shardId) == null) {
					LinkedBlockingQueue<EventBody> queueInstance = new LinkedBlockingQueue<>();
					shardSortQueue.put(shardId, queueInstance);
					return queueInstance;
				} else {
					return shardSortQueue.get(shardId);
				}
			}
		} else {
			return shardSortQueue.get(shardId);
		}
	}

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
