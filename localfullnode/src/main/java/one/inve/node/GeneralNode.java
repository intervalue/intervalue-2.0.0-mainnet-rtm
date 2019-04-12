package one.inve.node;

import com.alibaba.fastjson.JSONObject;
import com.zeroc.Ice.Communicator;
import com.zeroc.Ice.Object;
import com.zeroc.Ice.ObjectAdapter;
import com.zeroc.Ice.Util;
import one.inve.bean.message.Contribution;
import one.inve.bean.message.SnapshotMessage;
import one.inve.bean.message.SnapshotPoint;
import one.inve.bean.node.LocalFullNode;
import one.inve.bean.wallet.Keys;
import one.inve.bean.wallet.Wallet;
import one.inve.bean.wallet.WalletBuilder;
import one.inve.cluster.Member;
import one.inve.core.*;
import one.inve.rocksDB.RocksJavaUtil;
import one.inve.util.DbUtils;
import one.inve.util.HnKeyUtils;
import one.inve.util.PathUtils;
import one.inve.util.StringUtils;
import one.inve.utils.DSA;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.lang.reflect.Constructor;
import java.math.BigInteger;
import java.nio.file.Files;
import java.security.KeyPair;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * 一般（公共）节点
 * @author Clare
 * @date   2018/6/3.
 */
public class GeneralNode {
    private static final Logger logger = LoggerFactory.getLogger(Main.class);

    private ObjectAdapter adapter;
    private Communicator communicator;
    private int nValue=1;
    private int shardCount;

    private int shardId = -1;
    private long creatorId = -1L ;

    private List<LocalFullNode> localFullNodes;

    private List<String> whiteList;
    private List<String> blackList;
    private List<String> blackList4PubKey;

    private Hashnet hashnet;
    private EventFlow eventFlow;
    private EventStore eventStore;
    /**
     * 初始化时是否删除数据库
     */
    public Boolean isDrop = false;
    /**
     * 钱包功能数据
     */
    private Wallet wallet;

    /**
     * 节点参数
     */
    public NodeParameters nodeParameters = new NodeParameters();

    /**
     * 抗量子公私钥
     */
    public PublicKey publicKey = null;
    public PrivateKey privateKey = null;
    /**
     * 消息队列（用于共识，包括交易、合约、快照、文本）
     */
    private ConcurrentLinkedQueue<byte[]> messageQueue = new ConcurrentLinkedQueue<>();
    /**
     * 消息重复性验证缓存，用于在sendMsg接口接收时验证是否重复消息，20s清空一次
     */
    private ConcurrentHashMap<String, Long> messageHashCache = new ConcurrentHashMap<>();
    /**
     * 新Event入库队列
     */
    private LinkedBlockingQueue<EventBody> eventSaveQueue = new LinkedBlockingQueue<>(10000000);
    /**
     * 新Event入库队列
     */
    private LinkedBlockingQueue<EventBody> consEventHandleQueue = new LinkedBlockingQueue<>(10000000);
    /**
     * 共识消息（交易、合约、快照、文本、其他）签名验证队列
     */
    private LinkedBlockingQueue<JSONObject> consMessageVerifyQueue = new LinkedBlockingQueue<>(10000000);
    /**
     * 共识消息（交易、合约、快照、文本、其他）处理执行队列
     */
    private LinkedBlockingQueue<JSONObject> consMessageHandleQueue = new LinkedBlockingQueue<>(10000000);
    /**
     * 共识消息（交易、合约、快照、文本、其他）保存入库队列
     */
    private LinkedBlockingQueue<JSONObject> consMessageSaveQueue = new LinkedBlockingQueue<>(10000000);
    /**
     * 系统自动生成的交易（手续费收取、共识奖励、合约执行产生、等等）保存队列
     */
    private LinkedBlockingQueue<JSONObject> systemAutoTxSaveQueue = new LinkedBlockingQueue<>(10000000);

    /**
     * 全节点邻居池
     */
    public ArrayList<Member> fullNodeNeighborPools = new ArrayList<>();
    /**
     * 分片内局部全节点邻居池
     */
    public List<Member> inshardNeighborPools = new ArrayList<>();
    /**
     * 全局范围局部全节点邻居池
     */
    public List<Member> globalNeighborPools = new ArrayList<>();
    /**
     * 新Event总数
     */
    private BigInteger totalEventCount  = BigInteger.ZERO;
    /**
     * 共识Event总数
     */
    private BigInteger totalConsEventCount  = BigInteger.ZERO;
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
    /**
     * 最新快照消息
     */
    private SnapshotMessage snapshotMessage = null;
    /**
     * 最新快照点版本的消息hash tree root map < vers, msgHashTreeRoot >
     */
    private HashMap<BigInteger, String> treeRootMap = new HashMap<>();
    /**
     * 最新快照点map  < vers, SnapshotPoint >
     */
    private HashMap<BigInteger, SnapshotPoint> snapshotPointMap = new HashMap<>();
    /**
     * 最新快照点map  < vers, SnapshotPoint >
     */
    private HashMap<BigInteger, BigInteger> totalFeeMap = new HashMap<>();
    /**
     * 与上一个快照点之间的交易手续费总额
     */
    public BigInteger totalFeeBetween2Snapshots = BigInteger.ZERO;
    public String msgHashTreeRoot = null;
    /**
     * 各个节点的有效event数量(即两个快照点之间每个柱子上Event的other parent数量)
     */
    private HashSet<Contribution> contributions = new HashSet<>();

    public long[][] maxSeqs;

    public GeneralNode(){
        this.wallet = getWallet();
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

    public SnapshotMessage getSnapshotMessage() {
        return snapshotMessage;
    }

    public void setSnapshotMessage(SnapshotMessage snapshotMessage) {
        this.snapshotMessage = snapshotMessage;
    }

    public HashMap<BigInteger, String> getTreeRootMap() {
        return treeRootMap;
    }

    public BigInteger getCurrSnapshotVersion() {
        return (null==snapshotMessage)
                ? BigInteger.ONE : BigInteger.ONE.add(snapshotMessage.getSnapVersion());
    }

    public Wallet getWallet() {
        return wallet;
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

    public ConcurrentLinkedQueue<byte[]> getMessageQueue() {
        return messageQueue;
    }

    public ConcurrentHashMap<String, Long> getMessageHashCache() {
        return messageHashCache;
    }

    public Communicator getCommunicator() {
        return communicator;
    }

    public void setCommunicator(Communicator communicator) {
        this.communicator = communicator;
    }

    public ObjectAdapter getAdapter() {
        return adapter;
    }

    public void setAdapter(ObjectAdapter adapter) {
        this.adapter = adapter;
    }

    public Boolean getDrop() {
        return isDrop;
    }

    public void setDrop(Boolean drop) {
        isDrop = drop;
    }

    public BigInteger getTotalEventCount() {
        return totalEventCount;
    }

    public void setTotalEventCount(BigInteger totalEventCount) {
        this.totalEventCount = totalEventCount;
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

    public void setConsMessageMaxId(BigInteger consMessageMaxId) {
        this.consMessageMaxId = consMessageMaxId;
    }

    public BigInteger getSystemAutoTxMaxId() {
        return systemAutoTxMaxId;
    }

    public void setSystemAutoTxMaxId(BigInteger systemAutoTxMaxId) {
        this.systemAutoTxMaxId = systemAutoTxMaxId;
    }

    public BigInteger getConsMessageCount() {
        return consMessageCount;
    }

    public void setConsMessageCount(BigInteger consMessageCount) {
        this.consMessageCount = consMessageCount;
    }

    public BigInteger getTotalFeeBetween2Snapshots() {
        return totalFeeBetween2Snapshots;
    }

    public void setTotalFeeBetween2Snapshots(BigInteger totalFeeBetween2Snapshots) {
        this.totalFeeBetween2Snapshots = totalFeeBetween2Snapshots;
    }

    public HashSet<Contribution> getContributions() {
        return contributions;
    }

    public void setContributions(HashSet<Contribution> contributions) {
        this.contributions = contributions;
    }

    public HashMap<BigInteger, SnapshotPoint> getSnapshotPointMap() {
        return snapshotPointMap;
    }

    public LinkedBlockingQueue<EventBody> getEventSaveQueue() {
        return eventSaveQueue;
    }

    public LinkedBlockingQueue<EventBody> getConsEventHandleQueue() {
        return consEventHandleQueue;
    }

    public LinkedBlockingQueue<JSONObject> getConsMessageVerifyQueue() {
        return consMessageVerifyQueue;
    }

    public LinkedBlockingQueue<JSONObject> getConsMessageHandleQueue() {
        return consMessageHandleQueue;
    }

    public LinkedBlockingQueue<JSONObject> getConsMessageSaveQueue() {
        return consMessageSaveQueue;
    }

    public LinkedBlockingQueue<JSONObject> getSystemAutoTxSaveQueue() {
        return systemAutoTxSaveQueue;
    }

    public List<LocalFullNode> getLocalFullNodes() {
        return localFullNodes;
    }

    public void setLocalFullNodes(List<LocalFullNode> localFullNodes) {
        this.localFullNodes = localFullNodes;
    }

    public Hashnet getHashnet() {
        return hashnet;
    }

    public EventFlow getEventFlow() {
        return eventFlow;
    }

    public EventStore getEventStore() {
        return eventStore;
    }

    public void setEventStore(EventStore eventStore) {
        this.eventStore = eventStore;
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

    /**
     *
     * load rpc from default.config
     * @param node node object
     */
    public void loadRPC(GeneralNode node) {
        logger.info("start rpc service...");
        try{
            // add rpc
            for (int i=0; i<Config.SERVICE_ARRAY.length; i++) {
                Class<?> t = Class.forName(Config.SERVICE_ARRAY[i]);
                Constructor<Object> cons = (Constructor<Object>) t.getConstructor(GeneralNode.class);
                Object object = cons.newInstance(node);

                String identity = Config.SERVICE_ARRAY[i].substring(Config.SERVICE_ARRAY[i].lastIndexOf('.') + 1);
                if ( identity.toLowerCase().endsWith("impl") ) {
                    identity = identity.substring(0, identity.length()-4);
                }
                adapter.add(object, Util.stringToIdentity(identity));
            }
            adapter.activate();
        } catch (Exception e){
            logger.error("load rpc error: {}", e);
        }
    }

    /**
     * generate adapter with a specified port
     * @param communicator
     * @param adapterName
     * @param rpcPort
     * @return
     */
    public ObjectAdapter generateAdapter(Communicator communicator, String adapterName, int rpcPort){
        ObjectAdapter adapter = null;
        try {
            adapter = communicator.
                    createObjectAdapterWithEndpoints(adapterName, "default -p " + rpcPort);
        } catch (Exception e) {
            logger.error("error: {}", e);
            logger.warn("[ERROR]port {} bind failure!", rpcPort);
        }

        return adapter;
    }

    /**
     * 清除缓存
     */
    public void initClearCache() {
        String dir = PathUtils.getDataFileDir();
        String fileName = dir + Config.VERSION_VALUE;
        if ( !new File(fileName).exists() ) {
            logger.info(">>>>>> clear cache...");

            File path = new File(dir);
            if (!path.isDirectory()) {
                if (!path.mkdirs()) {
                    logger.error("create cache dir failed!!!\nexit...");
                    System.exit(-1);
                }
            }

            //删除其他
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
                FileWriter fw = new FileWriter(file,false);
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
            if (StringUtils.isNotEmpty(nodeParameters.mnemonic)) {
                newWallet(nodeParameters.mnemonic, fileName);
            } else {
                newWallet(fileName);
            }
        } else {
            reloadWallet(fileName);
            if (null==wallet) {
                newWallet(fileName);
            }
        }
    }

    /**
     * 创建钱包
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
     * 创建钱包
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
     * 恢复钱包
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
        wallet = new Wallet.Builder()
                .mnemonic(walletObj.getString("mnemonic"))
                .extKeys(extKeys)
                .keys(keys)
                .address(walletObj.getString("address"))
                .build() ;
    }

    /**
     * 初始化hnKey
     * 存在则读取，否则创建并保存
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
            publicKey = keyPair.getPublic();
            privateKey = keyPair.getPrivate();

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
            publicKey = keyPair.getPublic();
            privateKey = keyPair.getPrivate();
            logger.info("read pubkey: ", HnKeyUtils.getString4PublicKey(publicKey));
        }
    }

    /**
     * 初始化hashnet
     */
    public void initHashnet(Main node) throws InterruptedException {
        if (node.getTotalEventCount().compareTo(BigInteger.ZERO) <= 0) {
            // 创建hashnet
            createHashnet(node);
        } else {
            // 检测Event相关数据一致性并修复
//            DbUtils.detectAndRepairEventData(node);
//            // 检测共识Event相关数据一致性并修复
//            DbUtils.detectAndRepairConsEventData(node);
            // 根据最新快照，恢复相关快照参数： treeRootMap、snapshotPointMap等
            DbUtils.detectAndRepairSnapshotData(node);
            // 重载hashnet
            reloadHashnet(node);
        }
    }

    /**
     * 创建hashnet
     */
    private void createHashnet(Main node) {
        logger.info(">>>>>> init Hashnet...");
        initEventStore(node);
        initEventFlow();
        if (null == hashnet) {
            hashnet = new Hashnet(shardCount, nValue);
        }
        initFromScratch();
        logger.warn("node-({}, {}): init Hashnet successfully. shard-0's lastSeqs: {} ",
                node.getShardId(), node.getCreatorId(), eventStore.getLastSeqsByShardId(0));
    }

    /**
     * 重载hashnet
     */
    private void reloadHashnet(Main node) throws InterruptedException {
        logger.info(">>>>>> reload Hashnet...");
        initEventStore(node);
        initEventFlow();

        Map<Integer, LinkedBlockingQueue<EventBody>> shardSortQueue = new HashMap<>();
        if (null == hashnet) {
            hashnet = new Hashnet(shardCount, nValue);
            for (int i = 0; i < shardCount; i++) {
                if (shardSortQueue.get(i) == null) {
                    LinkedBlockingQueue<EventBody> queueInstance = new LinkedBlockingQueue<>();
                    shardSortQueue.put(i, queueInstance);
                }

                // 读取所有Event
                ArrayList<EventBody> events = new ArrayList<>();
                Iterator iter = eventStore.genOrderedIterator(i, nValue);
                while (iter.hasNext()) {
                    EventBody eb = (EventBody) iter.next();
                    events.add(eb);
                }
                Collections.shuffle(events);
                if(events.size() > 0) {
                    events.sort(Comparator.comparing(EventBody::getGeneration));
                    logger.warn("node-({}, {}): reload event size: {}",
                            node.getShardId(), node.getCreatorId(), events.size());
                }
                events.forEach(e -> hashnet.addEvent(e));
                logger.warn("node-({}, {}): reload events successfully. shard-{}'s lastSeqs: {} ",
                        node.getShardId(), node.getCreatorId(), i, eventStore.getLastSeqsByShardId(i));

                // 恢复共识Event全排序等待队列
                Event[] evts = hashnet.getAllConsEvents(i);
                for (Event evt : evts) {
                    node.getShardSortQueue(i).put(new EventBody.Builder()
                            .shardId(i)
                            .creatorId(evt.getCreatorId())
                            .creatorSeq(evt.getCreatorSeq())
                            .otherId(evt.getOtherId())
                            .otherSeq(evt.getOtherSeq())
                            .timeCreated(evt.getTimeCreated())
                            .trans(evt.getTransactions())
                            .signature(evt.getSignature())
                            .isFamous(evt.isFamous())
                            .generation(evt.getGeneration())
                            .hash(evt.getHash())
                            .consTimestamp(evt.getConsensusTimestamp())
                            .build());
                }

                logger.warn("node-({}, {}): shard-{} all sort queue(shardSortQueue) size = {} ",
                        node.getShardId(), node.getCreatorId(), i, node.getShardSortQueue(i).size());
            }

        }

        /**
         * 修复准备生成最新版本快照点需要的相关信息
         */
        repairCurrSnapshotPointInfo(node);

//        logger.info(">>>>>> reload Hashnet finished.");
        logger.warn("node-({}, {}): reload Hashnet successfully. shard-0's lastSeqs: {} ",
                node.getShardId(), node.getCreatorId(), eventStore.getLastSeqsByShardId(0));
    }

    /**
     * 修复准备生成最新版本快照点需要的相关信息(已有的最新快照消息之后的数据)
     * @param node
     */
    private void repairCurrSnapshotPointInfo(Main node) throws InterruptedException {
        SnapshotPoint latestSnapshotPoint = calculateLatestSnapshotPoint(node);
        EventBody latestSnapshotPointEb = null;
        String latestSnapshotPointEbHash = null;
        EventKeyPair pair0 = null;
        if (latestSnapshotPoint!=null) {
            latestSnapshotPointEb = latestSnapshotPoint.getEventBody();
            latestSnapshotPointEbHash = DSA.encryptBASE64(latestSnapshotPointEb.getHash());
            pair0 = new EventKeyPair(latestSnapshotPointEb.getShardId(),
                    latestSnapshotPointEb.getCreatorId(),
                    latestSnapshotPointEb.getCreatorSeq());
        }
        logger.error("node-({}, {}): The latest snapshotPoint's {} eventBody-{} hash: {}",
                node.getShardId(), node.getCreatorId(), node.getCurrSnapshotVersion().subtract(BigInteger.ONE),
                null==pair0?null:pair0.toString(), latestSnapshotPointEbHash);

        // 模拟全排序线程，排序并恢复contribution
        EventBody[] events = new EventBody[shardCount];
        boolean statisFlag = false;
        int allSortEvtSize = 0;
        BigInteger transCount = BigInteger.valueOf(Config.CREATION_TX_LIST.size());
        BigInteger consEventCount = BigInteger.ZERO;
        int l = 0;
        int m = 0;

        for (int i = 0; i < shardCount; i++) {
            logger.info("node-({}, {}): ShardSortQueue-{} size = {}",
                    node.getShardId(), node.getCreatorId(), i, node.getShardSortQueue(i).size());
        }
        while (true) {
            for (int i = 0; i < shardCount; i++) {
                if (null == events[i]) {
                    events[i] = node.getShardSortQueue(i).poll();
                    l++;
                }

                if (i == shardCount - 1) {
                    EventBody temp = events[0];
                    for (int j = 0; j < events.length; j++) {
                        if (temp == null || null == events[j]) {
                            logger.warn("node-({}, {}): evtSize={}, allSortEvtSize={}, forCalcuEvtSize={}, contribution size: {}",
                                    node.getShardId(), node.getCreatorId(),
                                    l, allSortEvtSize, m, node.getContributions().size() );
                            logger.info("node-({}, {}): repaired msgHashTreeRoot = {}",
                                    node.getShardId(), node.getCreatorId(), msgHashTreeRoot);
                            logger.info("node-({}, {}): repaired consEventCount = {}",
                                    node.getShardId(), node.getCreatorId(), consEventCount);
                            logger.info("node-({}, {}): repaired transCount = {}",
                                    node.getShardId(), node.getCreatorId(), transCount);
                            return;
                        } else if (events[j].getConsTimestamp().isBefore(temp.getConsTimestamp())) {
                            // 共识时间戳小的event排在前面
                            temp = events[j];
                            events[j] = null;
                        } else if (events[j].getConsTimestamp().equals(temp.getConsTimestamp()) ) {
                            // 共识时间戳相同的，以分片号小的的event排在前面
                            // 注意：同一个分片的2个共识Event的时间戳必然不相同，否则片内共识就失去意义
                            if (temp.getShardId() > j) {
                                temp = events[j];
                                events[j] = null;
                            }
                        }
                    }
                    if (null != temp) {
                        allSortEvtSize++;
                        if (!statisFlag) {
                            if (null == latestSnapshotPointEb) {
                                // 从0开始，或者从最新快照消息的快照点Event开始
                                statisFlag = true;
                            } else {
                                EventKeyPair pair = new EventKeyPair(temp.getShardId(), temp.getCreatorId(), temp.getCreatorSeq());
//                                logger.error("node-({}, {}): pair0: {}, pair: {}",
//                                        node.getShardId(), node.getCreatorId(), pair0.toString(), pair.toString());
                                if (DSA.encryptBASE64(temp.getHash()).equals(latestSnapshotPointEbHash)) {
                                    // 从0开始，或者从最新快照消息的快照点Event开始
                                    statisFlag = true;
                                    transCount = latestSnapshotPointEb.getTransCount();
                                    consEventCount = latestSnapshotPointEb.getConsEventCount();
                                } else {
                                    events[temp.getShardId()] = null;
                                    continue;
                                }
                            }
                        }
                        if (statisFlag ) {
                            EventKeyPair pair = new EventKeyPair(temp.getShardId(), temp.getCreatorId(), temp.getCreatorSeq());
                            if (pair.equals(pair0)) {
                                // 快照点Event滤掉
                                events[temp.getShardId()] = null;
                                continue;
                            }
                            m++;
                            // 修复Contribution
                            Contribution c = new Contribution.Builder()
                                    .shardId(temp.getShardId()).creatorId(temp.getCreatorId())
                                    .otherId(temp.getOtherId()).otherSeq(temp.getOtherSeq())
                                    .build();
                            node.getContributions().add(c);
                            // 修复msgHashTreeRoot
                            calculateMsgHashTreeRoot(temp);

                            // 没来的及更新入库的共识Event及时入库
                            consEventCount = consEventCount.add(BigInteger.ONE);
                            node.setTotalConsEventCount(consEventCount);
                            temp.setConsEventCount(node.getTotalConsEventCount());

                            RocksJavaUtil rocksJavaUtil = new RocksJavaUtil(node.nodeParameters.dbId);
                            if(temp.getTrans()!=null) {
                                transCount = transCount.add(BigInteger.valueOf(temp.getTrans().length));
                                rocksJavaUtil.put(Config.EVT_TX_COUNT_KEY, transCount.toString());
                                logger.warn("node-({}, {}): update transCount: {}",
                                        node.getShardId(), node.getCreatorId(), transCount);
                            }
                            temp.setTransCount(transCount);

                            byte[] evtByte = rocksJavaUtil.get(pair.toString());
                            if (null == evtByte) {
                                logger.error("node-({}, {}): missing event-{}",
                                        node.getShardId(), node.getCreatorId(), pair.toString());
                            } else {
                                String evtStr = new String(evtByte);
                                EventBody evt = JSONObject.parseObject(evtStr, EventBody.class);
                                if (evt.getConsTimestamp() == null || evt.getConsTimestamp().toEpochMilli() <= 0) {
                                    rocksJavaUtil.put(pair.toString(), JSONObject.toJSONString(temp));
                                    rocksJavaUtil.put(Config.CONS_EVT_COUNT_KEY, node.getTotalConsEventCount().toString());
                                } else if (!evt.getTransCount().equals(temp.getTransCount())) {
                                    logger.error("node-({}, {}): event-{}'s transCount diff, calcu: {}, db: {} ",
                                            node.getShardId(), node.getCreatorId(), pair.toString(),
                                            temp.getTransCount(), evt.getTransCount());
                                    System.exit(-1);
                                } else if (!evt.getConsEventCount().equals(temp.getConsEventCount())) {
                                    logger.error("node-({}, {}): event-{}'s consEventCount diff, calcu: {}, db: {} ",
                                            node.getShardId(), node.getCreatorId(), pair.toString(),
                                            temp.getConsEventCount(), evt.getConsEventCount());
                                    System.exit(-1);
                                }
                            }

                            // 没来的及解析入库的message继续入库
                            if (transCount.compareTo(node.getConsMessageMaxId()) > 0) {
                                int j = 1;
                                int msgCount = temp.getTrans().length;
                                for (byte[] msg : temp.getTrans()) {
                                    node.setConsMessageMaxId(node.getConsMessageMaxId().add(BigInteger.ONE));
                                    JSONObject o = new JSONObject();
                                    o.put("id", node.getConsMessageMaxId());
                                    o.put("eHash", DSA.encryptBASE64(temp.getHash()));
                                    o.put("eShardId", temp.getShardId());
                                    o.put("isStable", true);
                                    o.put("updateTime", temp.getConsTimestamp().toEpochMilli());
                                    o.put("msg", new String(msg));
                                    if (j++ == msgCount) {
                                        o.put("lastIdx", true);
                                    }

                                    try {
                                        node.getConsMessageVerifyQueue().put(o);
                                        logger.warn("node-({}, {}): message into ConsMessageVerifyQueue, id: {}",
                                                node.getShardId(), node.getCreatorId(), node.getConsMessageMaxId());
                                    } catch (InterruptedException e) {
                                        e.printStackTrace();
                                    }
                                }
                            }

                            // 没来的及生成的快照点及时生成快照点
                            createSnapshotPoint(node, temp);
                        }
                        events[temp.getShardId()] = null;
                    }
                }
            }
        }
    }

    /**
     * 到达生成快照点条件，则生成快照点
     * @param event 共识事件
     */
    private void createSnapshotPoint(Main node, EventBody event) throws InterruptedException {
        if (node.getTotalConsEventCount().mod(BigInteger.valueOf(Config.EVENT_NUM_PER_SNAPSHOT))
                .equals(BigInteger.ZERO)) {
            logger.info("node-({}, {}): repair to createSnapshotPoint, vers: {}", node.getCurrSnapshotVersion());
            // 计算并更新贡献
            ConcurrentHashMap<String, Long> statistics = new ConcurrentHashMap<>();
            long[][] effectiveCounts = new long[node.getShardCount()][node.getnValue()];
            for (int i=0; i<node.getShardCount(); i++) {
                for (int j = 0; j < node.getnValue(); j++) {
                    final int shardId = i;
                    final int creatorId = j;
                    effectiveCounts[i][j] = node.getContributions().stream()
                            .filter(c -> c.getShardId()==shardId && c.getCreatorId()==creatorId).count();
                    if (effectiveCounts[i][j]>0) {
                        Optional optional = node.getLocalFullNodes().stream()
                                .filter(n -> n.getShard().equals(""+shardId) && n.getIndex().equals(""+creatorId))
                                .findFirst();
                        statistics.put(((LocalFullNode)optional.get()).getAddress(), effectiveCounts[i][j]);
                    }
                }
            }

            // 生成快照点
            final String eHash = DSA.encryptBASE64(event.getHash());
            if (StringUtils.isEmpty(msgHashTreeRoot)) {
                msgHashTreeRoot = eHash;
            }
            node.getSnapshotPointMap().put(node.getCurrSnapshotVersion(), new SnapshotPoint.Builder()
                    .eventBody(event).msgHashTreeRoot(msgHashTreeRoot)
                    .contributions((null!=statistics && statistics.size()<=0) ? null: statistics)
                    .build());
            node.getTreeRootMap().put(node.getCurrSnapshotVersion(), msgHashTreeRoot);
            logger.info("\n=========== node-({}, {}):  vers: {}, msgHashTreeRoot: {}",
                    node.getShardId(), node.getCreatorId(), node.getCurrSnapshotVersion(), msgHashTreeRoot);

            // 重置消息hash根
            node.setContributions(new HashSet<>());
            msgHashTreeRoot = null;

            // 增加创建快照触发器
            JSONObject o = new JSONObject();
            // id与前一个ID一样，可以批量排序，且在处理的时候可以根据type是否为空进入特殊受控消息类型处理分支
            o.put("id", node.getConsMessageMaxId());
            o.put("eHash", eHash);
            o.put("lastIdx", true);
            node.getConsMessageVerifyQueue().put(o);
        }
    }

    /**
     * 查询已有的最新快照消息的快照点
     * @param node
     * @return SnapshotPoint
     */
    private SnapshotPoint calculateLatestSnapshotPoint(Main node) {
        SnapshotPoint lastSnapshotPoint = null;
        if (null != node.getSnapshotPointMap()
                && null != node.getSnapshotPointMap().get(node.getCurrSnapshotVersion().subtract(BigInteger.ONE))) {
            lastSnapshotPoint
                    = node.getSnapshotPointMap().get(node.getCurrSnapshotVersion().subtract(BigInteger.ONE));
        }
        return lastSnapshotPoint;
    }

    /**
     * 计算MsgHashTreeRoot
     * @param event
     */
    private void calculateMsgHashTreeRoot(EventBody event) {
        long eventMsgCount = (null!=event.getTrans() && event.getTrans().length > 0)
                ? event.getTrans().length : 0;
        // 共识消息放入消息签名验证队列
        if (eventMsgCount>0) {
            for (byte[] msg : event.getTrans()) {
                // 计算更新消息hash根
                JSONObject msgObj = JSONObject.parseObject(new String(msg));
                if(StringUtils.isEmpty(msgHashTreeRoot)) {
                    msgHashTreeRoot = DSA.encryptBASE64(Hash.hash(msgObj.getString("signature")));
                } else {
                    msgHashTreeRoot = DSA.encryptBASE64(Hash.hash(msgHashTreeRoot, msgObj.getString("signature")));
                }
            }
        }
    }

    private void initEventStore(Main node) {
        if (null == eventStore) {
            eventStore = new EventStoreImpl(node);
        }
    }
    private void initEventFlow() {
        if (null == eventFlow) {
            PublicKey[][] publicKeys = new PublicKey[shardCount][nValue];
            this.getLocalFullNodes().forEach(n -> {
                try {
                    publicKeys[Integer.parseInt(n.getShard())][Integer.parseInt(n.getIndex())]
                            = HnKeyUtils.getPublicKey4String(n.getPubkey());
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });
            eventFlow = new EventFlow(publicKeys, privateKey, eventStore);
        }
    }

    //
    private void initFromScratch() {
        for (int i = 0; i < this.getShardCount(); i++) {
            if (this.getShardId()!=-1 && i == this.getShardId()) {
                eventFlow.newEvent(i, (int)this.getCreatorId(), -1, null);
            }
            this.addToHashnet(i);
        }
    }

    public void addToHashnet(int shardId) {
        EventBody[] ebs = eventFlow.getAllQueuedEvents(shardId);
        for (EventBody eb : ebs) {
            hashnet.addEvent(eb);
        }
    }


    /**
     * 从配置文件读取参数值
     * @param name 参数名
     * @param isRequired 是否必要
     * @return 参数值
     */
    public String getDefaultConfigParam(String name, boolean isRequired) {
        String value = communicator.getProperties().getProperty(name);
        if (StringUtils.isEmpty(value) && isRequired) {
            logger.error("param {} is empty.", name);
            throw new RuntimeException("param " + name + " is empty.");
        }
        return value;
    }
}
