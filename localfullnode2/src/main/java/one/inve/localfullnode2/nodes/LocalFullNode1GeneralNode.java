package one.inve.localfullnode2.nodes;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.math.BigInteger;
import java.nio.file.Files;
import java.security.KeyPair;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alibaba.fastjson.JSONObject;
import com.zeroc.Ice.Communicator;
import com.zeroc.Ice.Object;
import com.zeroc.Ice.ObjectAdapter;
import com.zeroc.Ice.Util;

import one.inve.bean.message.SnapshotMessage;
import one.inve.bean.node.LocalFullNode;
import one.inve.bean.wallet.Keys;
import one.inve.bean.wallet.Wallet;
import one.inve.bean.wallet.WalletBuilder;
import one.inve.cluster.Member;
import one.inve.core.EventBody;
import one.inve.localfullnode2.conf.Config;
import one.inve.localfullnode2.conf.NodeParameters;
import one.inve.localfullnode2.dep.DepItemsManager;
import one.inve.localfullnode2.dep.items.Wal;
import one.inve.localfullnode2.hashnet.Event;
import one.inve.localfullnode2.hashnet.Hashnet;
import one.inve.localfullnode2.staging.StagingArea;
import one.inve.localfullnode2.store.EventFlow;
import one.inve.localfullnode2.store.EventStoreDependency;
import one.inve.localfullnode2.store.EventStoreImpl;
import one.inve.localfullnode2.store.IEventFlow;
import one.inve.localfullnode2.store.IEventStore;
import one.inve.localfullnode2.utilities.Cryptos;
import one.inve.localfullnode2.utilities.HnKeyUtils;
import one.inve.localfullnode2.utilities.PathUtils;
import one.inve.localfullnode2.utilities.StringUtils;

public class LocalFullNode1GeneralNode {

	private Communicator communicator;

	/**
	 * 节点参数
	 */
	private NodeParameters nodeParameters = new NodeParameters();

	/**
	 * 钱包功能数据
	 */
	private Wallet wallet;

	private ObjectAdapter adapter;

	/**
	 * 最新快照消息
	 */
	private SnapshotMessage snapshotMessage = null;

	/**
	 * 抗量子公私钥
	 */
	private PublicKey publicKey = null;
	private PrivateKey privateKey = null;

	private Hashnet hashnet;
	private volatile EventFlow eventFlow;
	private volatile IEventStore eventStore;

	private int nValue = 1;
	private int shardCount;

	private int shardId = -1;
	private long creatorId = -1L;

	private List<LocalFullNode> localFullNodes;

	private List<String> whiteList;
	private List<String> blackList;
	private List<String> blackList4PubKey;

	// the height of events
	private volatile long[][] lastSeqs;

	/**
	 * 分片内局部全节点邻居池
	 */
	private List<Member> inshardNeighborPools = Collections.synchronizedList(new ArrayList<Member>());

	public List<Member> inshardNeighborPools() {
		return inshardNeighborPools;
	}

	public void inshardNeighborPools(List<Member> members) {
		if (members != null && members.size() > 0) {
			this.inshardNeighborPools = members;
		}
	}

	/**
	 * 全局范围局部全节点邻居池
	 */
	private List<Member> globalNeighborPools = Collections.synchronizedList(new ArrayList<Member>());

	public List<Member> globalNeighborPools() {
		return globalNeighborPools;
	}

	public void globalNeighborPools(List<Member> members) {
		this.globalNeighborPools = members;
	}

	/**
	 * 消息重复性验证缓存，用于在sendMsg接口接收时验证是否重复消息，20s清空一次
	 */
	private ConcurrentHashMap<String, Long> messageHashCache = new ConcurrentHashMap<>();

	/**
	 * 新Event总数
	 */
	private BigInteger totalEventCount = BigInteger.ZERO;
	/**
	 * 共识Event总数
	 */
	private BigInteger totalConsEventCount = BigInteger.ZERO;
	/**
	 * 共识消息（包含在队列里暂未入库的消息）最大ID
	 */
	private BigInteger consMessageMaxId = BigInteger.ZERO;
	/**
	 * 系统自动生成的交易（手续费收取、共识奖励、合约执行产生、等等）最大ID
	 */
	private BigInteger systemAutoTxMaxId = BigInteger.ZERO;
	/**
	 * 共识消息（已入库的消息）总数量
	 */
	private BigInteger consMessageCount = BigInteger.ZERO;

	private ReentrantReadWriteLock gossipAndRPCExclusiveLock = new ReentrantReadWriteLock();

	private static final Logger logger = LoggerFactory.getLogger(LocalFullNode1GeneralNode.class);

	public void setCommunicator(Communicator communicator) {
		this.communicator = communicator;
	}

	public Communicator getCommunicator() {
		return communicator;
	}

	public Wallet getWallet() {
		return wallet;
	}

	public void setAdapter(ObjectAdapter adapter) {
		this.adapter = adapter;
	}

	public ObjectAdapter getAdapter() {
		return adapter;
	}

	public int getShardId() {
		return shardId;
	}

	public void setShardId(int shardId) {
		this.shardId = shardId;
	}

	public long getCreatorId() {
		return creatorId;
	}

	public void setCreatorId(long creatorId) {
		this.creatorId = creatorId;
	}

	public IEventStore getEventStore() {
		return eventStore;
	}

	public List<String> getWhiteList() {
		return whiteList;
	}

	public void setWhiteList(List<String> whiteList) {
		this.whiteList = whiteList;
	}

	public List<String> getBlackList() {
		return blackList;
	}

	public void setBlackList(List<String> blackList) {
		this.blackList = blackList;
	}

	public List<String> getBlackList4PubKey() {
		return blackList4PubKey;
	}

	public void setBlackList4PubKey(List<String> blackList4PubKey) {
		this.blackList4PubKey = blackList4PubKey;
	}

	public int getnValue() {
		return nValue;
	}

	public void setnValue(int nValue) {
		this.nValue = nValue;
	}

	public int getShardCount() {
		return shardCount;
	}

	public void setShardCount(int shardCount) {
		this.shardCount = shardCount;
	}

	public BigInteger getTotalConsEventCount() {
		return totalConsEventCount;
	}

	public void setTotalConsEventCount(BigInteger totalConsEventCount) {
		this.totalConsEventCount = totalConsEventCount;
	}

	public BigInteger getConsMessageMaxId() {
		return consMessageMaxId;
	}

//	public void setConsMessageMaxId(BigInteger consMessageMaxId) {
//		this.consMessageMaxId = consMessageMaxId;
//	}
	public void addConsMessageMaxId(long delta) {
		consMessageMaxId = consMessageMaxId.add(BigInteger.valueOf(delta));
	}

	public BigInteger getConsMessageCount() {
		return consMessageCount;
	}

	public void setConsMessageCount(BigInteger consMessageCount) {
		this.consMessageCount = consMessageCount;
	}

	public BigInteger getTotalEventCount() {
		return totalEventCount;
	}

	public void setTotalEventCount(BigInteger totalEventCount) {
		this.totalEventCount = totalEventCount;
	}

//	public BigInteger getSystemAutoTxMaxId() {
//		return systemAutoTxMaxId;
//	}
//
//	public void setSystemAutoTxMaxId(BigInteger systemAutoTxMaxId) {
//		this.systemAutoTxMaxId = systemAutoTxMaxId;
//	}

	public BigInteger getSystemAutoTxMaxId() {
		return systemAutoTxMaxId;
	}

	public void addSystemAutoTxMaxId(long delta) {
		systemAutoTxMaxId = systemAutoTxMaxId.add(BigInteger.valueOf(delta));
	}

	public List<LocalFullNode> getLocalFullNodes() {
		return localFullNodes;
	}

	public void setLocalFullNodes(List<LocalFullNode> localFullNodes) {
		this.localFullNodes = localFullNodes;
	}

	public ConcurrentHashMap<String, Long> getMessageHashCache() {
		return messageHashCache;
	}

	public PublicKey publicKey() {
		return publicKey;
	}

	public void publicKey(PublicKey publicKey) {
		this.publicKey = publicKey;
	}

	public PrivateKey privateKey() {
		return privateKey;
	}

	public void privateKey(PrivateKey privateKey) {
		this.privateKey = privateKey;
	}

	public SnapshotMessage getSnapshotMessage() {
		return snapshotMessage;
	}

	public void setSnapshotMessage(SnapshotMessage snapshotMessage) {
		this.snapshotMessage = snapshotMessage;
	}

	public BigInteger getCurrSnapshotVersion() {
		return (null == snapshotMessage) ? BigInteger.ONE : BigInteger.ONE.add(snapshotMessage.getSnapVersion());
	}

	public long[][] getLastSeqs() {
		return lastSeqs;
	}

	public void setLastSeqs(long[][] lastSeqs) {
		this.lastSeqs = lastSeqs;
	}

	/**
	 * 初始化hnKey 存在则读取，否则创建并保存
	 */
	public void initHnKey(String keyfile) {
		logger.info(">>>>>> init hashnet keys...");
		File dir = new File(PathUtils.getDataFileDir());
		if (!dir.isDirectory()) {
			if (!dir.mkdirs()) {
				logger.error("create cache dir failed!!!\nexit...");
				System.exit(-1);
			}
		}
		String fileName = PathUtils.getDataFileDir() + keyfile;
		logger.info(">>>>>> path: {}", fileName);
		File file = new File(fileName);
		if (!file.exists()) {
			// 生成key
			Cryptos cryptos = new Cryptos();
			KeyPair keyPair = null;
			try {
				keyPair = cryptos.getKeyPair();
			} catch (NoSuchAlgorithmException e) {
				e.printStackTrace();
			}
			publicKey(keyPair.getPublic());
			privateKey(keyPair.getPrivate());

			// 写入文件
			HnKeyUtils.writeKey2File(keyPair, fileName);
		} else {
			// 读取文件
			KeyPair keyPair = HnKeyUtils.readKeyFromFile(fileName);
			if (null == keyPair) {
				logger.error("readKeyFromFile null.");
				// 生成key
				Cryptos cryptos = new Cryptos();
				try {
					keyPair = cryptos.getKeyPair();
				} catch (NoSuchAlgorithmException e) {
					e.printStackTrace();
				}
				// 写入文件
				HnKeyUtils.writeKey2File(keyPair, fileName);
			}
			assert keyPair != null;
			publicKey(keyPair.getPublic());
			privateKey(keyPair.getPrivate());
			logger.info("read pubkey: ", HnKeyUtils.getString4PublicKey(publicKey));
		}
	}

	public NodeParameters nodeParameters() {
		return nodeParameters;
	}

	public void nodeParameters(NodeParameters nodeParameters) {
		this.nodeParameters = nodeParameters;
	}

	/**
	 * 清除缓存
	 */
	public void initClearCache() {
		String dir = PathUtils.getDataFileDir();
		String fileName = dir + Config.VERSION_VALUE;
		if (!new File(fileName).exists()) {
			logger.info(">>>>>> clear cache...");

			File path = new File(dir);
			if (!path.isDirectory()) {
				if (!path.mkdirs()) {
					logger.error("create cache dir failed!!!\nexit...");
					System.exit(-1);
				}
			}

			// 删除其他
			try {
				Files.newDirectoryStream(new File(dir).toPath()).forEach(p -> {
					if (!p.endsWith(Config.WALLET_FILE)) {
						return;
					}
					try {
						logger.info(">>>>>> delete cache {}", p.toString());
						Files.delete(p);
					} catch (IOException e) {
						logger.error("error: {}", e);
					}
				});
			} catch (IOException e) {
				logger.error("error: {}", e);
			}

			File file = new File(fileName);
			try {
				file.createNewFile();
				FileWriter fw = new FileWriter(file, false);
				BufferedWriter bw = new BufferedWriter(fw);
				bw.write(Config.VERSION_VALUE);
				bw.flush();
				bw.close();
				fw.close();
			} catch (IOException e) {
				logger.error("error: {}", e);
			}
		}
	}

	/**
	 * 初始化钱包
	 * 
	 * @throws Exception 异常信息
	 */
	public void initWallet(String walletFile) throws Exception {
		logger.info(">>>>>> init wallet...");
		File dir = new File(PathUtils.getDataFileDir());
		if (!dir.isDirectory()) {
			if (!dir.mkdirs()) {
				logger.error("create cache dir failed!!!\nexit...");
				System.exit(-1);
			}
		}
		String fileName = PathUtils.getDataFileDir() + walletFile;
		logger.info(">>>>>> path: {}", fileName);
		File file = new File(fileName);
		if (!file.exists()) {
			if (StringUtils.isNotEmpty(nodeParameters().mnemonic)) {
				newWallet(nodeParameters().mnemonic, fileName);
			} else {
				newWallet(fileName);
			}
		} else {
			reloadWallet(fileName);
			if (null == wallet) {
				newWallet(fileName);
			}
		}

		Wal wal = DepItemsManager.getInstance().attachWal(null);
		wal.set(wallet);
	}

	/**
	 * 创建钱包
	 * 
	 * @param fileName 钱包基本信息保存文件
	 * @throws Exception IO异常
	 */
	private void newWallet(String word, String fileName) throws Exception {
		wallet = WalletBuilder.generateWallet(word);
		// 写入文件
		FileOutputStream outputStream = new FileOutputStream(new File(fileName));
		outputStream.write(JSONObject.toJSONString(wallet).getBytes());
		outputStream.close();
	}

	/**
	 * 创建钱包
	 * 
	 * @param fileName 钱包基本信息保存文件
	 * @throws Exception IO异常
	 */
	private void newWallet(String fileName) throws Exception {
		wallet = WalletBuilder.generateWallet();
		// 写入文件
		FileOutputStream outputStream = new FileOutputStream(new File(fileName));
		outputStream.write(JSONObject.toJSONString(wallet).getBytes());
		outputStream.close();
	}

	/**
	 * 恢复钱包
	 * 
	 * @param fileName 钱包基本信息保存文件
	 * @throws Exception IO异常
	 */
	private void reloadWallet(String fileName) throws Exception {
		FileInputStream inputStream = new FileInputStream(new File(fileName));
		byte[] data = new byte[inputStream.available()];
		inputStream.read(data);
		inputStream.close();

		JSONObject walletObj = JSONObject.parseObject(new String(data));
		Keys extKeys = JSONObject.parseObject(walletObj.getString("extKeys"), Keys.class);
		Keys keys = JSONObject.parseObject(walletObj.getString("keys"), Keys.class);
		wallet = new Wallet.Builder().mnemonic(walletObj.getString("mnemonic")).extKeys(extKeys).keys(keys)
				.address(walletObj.getString("address")).build();
	}

	/**
	 * 初始化hashnet
	 */
	public void initHashnet(LocalFullNode1GeneralNode node) throws InterruptedException {
		if (node.getTotalEventCount().compareTo(BigInteger.ZERO) <= 0) {
			// 创建hashnet
			createHashnet(node);
		} else {
			// 检测Event相关数据一致性并修复
//            DbUtils.detectAndRepairEventData(node);
//            // 检测共识Event相关数据一致性并修复
//            DbUtils.detectAndRepairConsEventData(node);
			// 根据最新快照，恢复相关快照参数： treeRootMap、snapshotPointMap等
			// key condition
			// DbUtils.detectAndRepairSnapshotData(node);
			// 重载hashnet
			reloadHashnet(node);
		}
	}

	/**
	 * 创建hashnet
	 */
	private void createHashnet(LocalFullNode1GeneralNode node) {
		logger.info(">>>>>> init Hashnet...");
		initEventStore(node);
		initEventFlow();
		if (null == hashnet) {
			hashnet = new Hashnet(shardCount, nValue);
		}
		initFromScratch();
		logger.warn("node-({}, {}): init Hashnet successfully. shard-0's lastSeqs: {} ", node.getShardId(),
				node.getCreatorId(), eventStore.getLastSeqsByShardId(0));
	}

	private void initEventStore(LocalFullNode1GeneralNode node) {
		if (null == eventStore) {
			// eventStore = new EventStoreImpl(node);
			EventStoreDependency eventStoreDep = DepItemsManager.getInstance()
					.getItemConcerned(EventStoreDependency.class);

			eventStore = new EventStoreImpl(eventStoreDep);
		}
	}

	private void initEventFlow() {
		if (null == eventFlow) {
			PublicKey[][] publicKeys = new PublicKey[shardCount][nValue];
			this.getLocalFullNodes().forEach(n -> {
				try {
					publicKeys[Integer.parseInt(n.getShard())][Integer.parseInt(n.getIndex())] = HnKeyUtils
							.getPublicKey4String(n.getPubkey());
				} catch (Exception e) {
					e.printStackTrace();
				}
			});
			eventFlow = new EventFlow(publicKeys, privateKey, eventStore);

			DepItemsManager.getInstance().attachEventFlow(null).set(eventFlow);
		}
	}

	public IEventFlow getEventFlow() {
		return eventFlow;
	}

	//
	private void initFromScratch() {
		for (int i = 0; i < this.getShardCount(); i++) {
			if (this.getShardId() != -1 && i == this.getShardId()) {
				eventFlow.newEvent(i, (int) this.getCreatorId(), -1, null);
			}
			this.addToHashnet(i);
		}
	}

	/**
	 * 重载hashnet
	 */
	private void reloadHashnet(LocalFullNode1GeneralNode node) throws InterruptedException {
		logger.info(">>>>>> reload Hashnet...");
		initEventStore(node);
		initEventFlow();

		// Map<Integer, LinkedBlockingQueue<EventBody>> shardSortQueue = new
		// HashMap<>();
		if (null == hashnet) {
			hashnet = new Hashnet(shardCount, nValue);
			for (int i = 0; i < shardCount; i++) {
//				if (shardSortQueue.get(i) == null) {
//					LinkedBlockingQueue<EventBody> queueInstance = new LinkedBlockingQueue<>();
//					shardSortQueue.put(i, queueInstance);
//				}
				// Francis.Deng {@code LocalFullNodeSkeleton::buildShardSortQueue()} initializes
				// them
				StagingArea stagingArea = DepItemsManager.getInstance().attachAllQueues(null).get();
				BlockingQueue<EventBody> shardSortQueue = stagingArea.getQueue(EventBody.class,
						StagingArea.ShardSortQueueName, i);

				// 读取所有Event
				ArrayList<EventBody> events = new ArrayList<>();
				Iterator iter = eventStore.genOrderedIterator(i, nValue);
				while (iter.hasNext()) {
					EventBody eb = (EventBody) iter.next();
					events.add(eb);
				}
				Collections.shuffle(events);
				if (events.size() > 0) {
					events.sort(Comparator.comparing(EventBody::getGeneration));
					logger.warn("node-({}, {}): reload event size: {}", node.getShardId(), node.getCreatorId(),
							events.size());
				}
				events.forEach(e -> hashnet.addEvent(e));
				logger.warn("node-({}, {}): reload events successfully. shard-{}'s lastSeqs: {} ", node.getShardId(),
						node.getCreatorId(), i, eventStore.getLastSeqsByShardId(i));

				// 恢复共识Event全排序等待队列
				Event[] evts = hashnet.getAllConsEvents(i);
				for (Event evt : evts) {
//					node.getShardSortQueue(i).put(new EventBody.Builder().shardId(i).creatorId(evt.getCreatorId())
//							.creatorSeq(evt.getCreatorSeq()).otherId(evt.getOtherId()).otherSeq(evt.getOtherSeq())
//							.timeCreated(evt.getTimeCreated()).trans(evt.getTransactions())
//							.signature(evt.getSignature()).isFamous(evt.isFamous()).generation(evt.getGeneration())
//							.hash(evt.getHash()).consTimestamp(evt.getConsensusTimestamp()).build());
					shardSortQueue.put(new EventBody.Builder().shardId(i).creatorId(evt.getCreatorId())
							.creatorSeq(evt.getCreatorSeq()).otherId(evt.getOtherId()).otherSeq(evt.getOtherSeq())
							.timeCreated(evt.getTimeCreated()).trans(evt.getTransactions())
							.signature(evt.getSignature()).isFamous(evt.isFamous()).generation(evt.getGeneration())
							.hash(evt.getHash()).consTimestamp(evt.getConsensusTimestamp()).build());
				}

				logger.warn("node-({}, {}): shard-{} all sort queue(shardSortQueue) size = {} ", node.getShardId(),
						node.getCreatorId(), i, shardSortQueue.size());
			}

		}

		/**
		 * 修复准备生成最新版本快照点需要的相关信息
		 */
		// key condition
		// repairCurrSnapshotPointInfo(node);

//        logger.info(">>>>>> reload Hashnet finished.");
		logger.warn("node-({}, {}): reload Hashnet successfully. shard-0's lastSeqs: {} ", node.getShardId(),
				node.getCreatorId(), eventStore.getLastSeqsByShardId(0));
	}

	public void addToHashnet(int shardId) {
		EventBody[] ebs = eventFlow.getAllQueuedEvents(shardId);
		for (EventBody eb : ebs) {
			hashnet.addEvent(eb);
		}
	}

	/**
	 * generate adapter with a specified port
	 * 
	 * @param communicator
	 * @param adapterName
	 * @param rpcPort
	 * @return
	 */
	public ObjectAdapter generateAdapter(Communicator communicator, String adapterName, int rpcPort) {
		ObjectAdapter adapter = null;
		try {
			adapter = communicator.createObjectAdapterWithEndpoints(adapterName, "default -p " + rpcPort);
		} catch (Exception e) {
			logger.error("error: {}", e);
			logger.warn("[ERROR]port {} bind failure!", rpcPort);
		}

		return adapter;
	}

	/**
	 *
	 * load rpc from default.config
	 * 
	 * @param node node object
	 */
	public void loadRPC(LocalFullNode1GeneralNode node) {
		logger.info("start rpc service...");
		try {
			// add rpc
			for (int i = 0; i < Config.SERVICE_ARRAY.length; i++) {
				Class<?> t = Class.forName(Config.SERVICE_ARRAY[i]);
				Constructor<Object> cons = (Constructor<Object>) t.getConstructor(LocalFullNode1GeneralNode.class);
				Object object = cons.newInstance(node);

				String identity = Config.SERVICE_ARRAY[i].substring(Config.SERVICE_ARRAY[i].lastIndexOf('.') + 1);
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

	public ReentrantReadWriteLock gossipAndRPCExclusiveLock() {
		return gossipAndRPCExclusiveLock;
	}

}
