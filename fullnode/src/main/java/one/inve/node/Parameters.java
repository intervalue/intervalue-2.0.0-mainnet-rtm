package one.inve.node;

import com.alibaba.fastjson.JSONArray;
import com.zeroc.Ice.Communicator;
import one.inve.bean.node.GossipAddress;
import one.inve.core.Config;
import one.inve.util.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

public class Parameters {
    private static final Logger logger = LoggerFactory.getLogger(Parameters.class);

    /**
     * 配置文件
     */
    public String configFile;
    /**
     * 数据库文件
     */
    public String dbFile;
    /**
     * 钱包信息文件
     */
    public String walletFile;
    /**
     * 公私钥对文件
     */
    public String keysFile;
    /**
     * seed的IP和端口信息
     */
    public GossipAddress seedGossipAddress;
    /**
     * 节点自己的IP和端口信息
     */
    public GossipAddress selfGossipAddress;
    /**
     * 分片数
     */
    public int shardSize;
    /**
     * 每个分片的局部全节点书
     */
    public int shardNodeSize;
    /**
     * 客户端每次获取局部全节点列表的最大数量
     */
    public int neighborMaxSize;
    /**
     * 可以进行pbft共识的最小（全节点）数量
     */
    public int pbftNodeMinSize;
    /**
     * 全节点和局部全节点gossip网络维护轮循时间(毫秒)
     */
    public long gossipInterval;
    /**
     * 中继节点跨链手续费
     */
    public double exchangeFeeRatio;
    /**
     * 中继节点心跳超时时间（超过这个时间表明节点不在线）(毫秒)
     */
    public long relayAliveTimeout;
    /**
     * （中继）节点信息（lastAliveTimestamp变化除外）更新的最小间隔时间(毫秒)
     */
    public long nodeUpdateMinInterval;
    /**
     * 轮循获取多链币种兑换汇率并更新的最小间隔时间(毫秒)
     */
    public long ratioUpdateMinInterval;
    /**
     * 白名单列表
     */
    public List<String> whiteList;
    /**
     * 黑名单列表
     */
    public List<String> blackList;
    /**
     * 是否清理旧数据库
     */
    public boolean clearDb;
    /**
     * 是否静态分片
     */
    public boolean staticSharding;
    /**
     * 环境： 开发测试环境还是线上环境
     *      dev
     *      test
     *      online
     */
    public String env;
    /**
     * 默认文件前缀
     */
    public String prefix;

    private void checkCommandParameters(String args[]) {
        logger.info("check command parameters... ");
        if (null==args || args.length<1) {
            logger.error("args is illegal.");
            System.exit(-1);
        }
    }

    public void init(Communicator communicator, String[] args) {
        if (null==seedGossipAddress) {
            seedGossipAddress = new GossipAddress();
        }
        if (null==selfGossipAddress) {
            selfGossipAddress = new GossipAddress();
        }
        logger.warn("params: {}", JSONArray.toJSONString(args));
        // 检查命令行参数
        checkCommandParameters(args);

        boolean existConfigFile = false;
        HashMap<String, String> paramMap = new HashMap<>();
        for(int i=0; i<args.length; i++) {
            String p = args[i];
            if (p.startsWith("-D")) {
                p = p.substring(2);
                String[] keyValuePair = p.split("=");
                paramMap.put(keyValuePair[0].toLowerCase(), keyValuePair[1]);
                System.setProperty(keyValuePair[0].toLowerCase(), keyValuePair[1]);
//                logger.warn("{}: {}", keyValuePair[0], System.getProperty(keyValuePair[0]));
            }
            if (!existConfigFile && p.startsWith("--Ice.Config=")) {
                existConfigFile = true;
                String[] keyValuePair = p.split("=");
                this.configFile = keyValuePair[1];
            }
        }

        if (!existConfigFile ) {
            logger.error("need --Ice.Config command parameter!!!");
            throw new RuntimeException("need --Ice.Config command parameter!!!");
        }

        // seed
        try {
            this.seedGossipAddress.pubIP = getConfigParam(communicator, paramMap, "seed.pubIP",
                    Config.DEFAULT_SEED_PUBIP, true);
            this.seedGossipAddress.gossipPort = getConfigParam(communicator, paramMap, "seed.gossipPort",
                            Config.DEFAULT_SEED_GOSSIP_PORT, true);
            this.seedGossipAddress.rpcPort = getConfigParam(communicator, paramMap, "seed.rpcPort",
                            Config.DEFAULT_SEED_RPC_PORT, true);
            this.seedGossipAddress.httpPort = getConfigParam(communicator, paramMap, "seed.httpPort",
                            Config.DEFAULT_SEED_HTTP_PORT, true);
            // 自己节点
            this.selfGossipAddress.pubIP = getConfigParam(communicator, paramMap, "myself.pubIP",
                    "", true);
            this.selfGossipAddress.gossipPort = getConfigParam(communicator, paramMap, "myself.gossipPort",
                    -1, true);
            this.selfGossipAddress.rpcPort = getConfigParam(communicator, paramMap, "myself.rpcPort",
                    -1, true);
            this.selfGossipAddress.httpPort = getConfigParam(communicator, paramMap, "myself.httpPort",
                    -1, true);
            this.env = getConfigParam(communicator, paramMap, "env", Config.DEFAULT_VERSION, true);
            this.clearDb = getConfigParam(communicator, paramMap, "test.clearDb", true, true );
            this.prefix = "online".equals(this.env)
                    ? "" : getConfigParam(communicator, paramMap, "test.prefix", "", false);
            this.staticSharding = getConfigParam(communicator, paramMap, "sharding.static", false, false);

            this.dbFile = getConfigParam(communicator, paramMap, "dbFile",
                    this.prefix+Config.DEFAULT_REAL_SQLITE_FILE, true);
            this.keysFile = getConfigParam(communicator, paramMap, "keysFile",
                    this.prefix+Config.DEFAULT_KEYS_FILE, true);
            this.walletFile = getConfigParam(communicator, paramMap, "walletFile",
                    this.prefix+Config.DEFAULT_WALLET_FILE, true);
            this.shardSize = getConfigParam(communicator, paramMap, "shard.size",
                    Config.DEFAULT_SHARD_SIZE, true);
            this.shardNodeSize = getConfigParam(communicator, paramMap, "shard.node.size",
                    Config.DEFAULT_SHARD_NODE_SIZE, true);
            this.neighborMaxSize = getConfigParam(communicator, paramMap, "neighbor.maxsize",
                    Config.DEFAULT_NEIGHBOR_SIZE, true);
            this.pbftNodeMinSize = getConfigParam(communicator, paramMap, "pbft.node.minsize",
                    Config.DEFAULT_PBFT_NODE_MIN_SIZE, true);
            this.gossipInterval = getConfigParam(communicator, paramMap, "gossip.interval",
                    Config.DEFAULT_NODE_GOSSIP_INTERVAL, true);
            this.relayAliveTimeout = getConfigParam(communicator, paramMap, "relay.timeout",
                    Config.DEFAULT_RELAY_ALIVE_TIME_OUT, true);
            this.nodeUpdateMinInterval = getConfigParam(communicator, paramMap, "interface.frequency.interval",
                    Config.DEFAULT_NODE_UPDATE_MIN_INTERVAL, true);
            this.ratioUpdateMinInterval = getConfigParam(communicator, paramMap, "update.exchange.ratio.interval",
                    Config.DEFAULT_RATIO_UPDATE_MIN_INTERVAL, true);
            this.exchangeFeeRatio = getConfigParam(communicator, paramMap, "exchange.ratio",
                    Config.DEFAULT_EXCHANGE_FEE_RATIO, false);

            this.whiteList = getConfigParam(communicator, paramMap, "whitelist",
                    Config.DEFAULT_WHITE_LIST, false);
            this.blackList = getConfigParam(communicator, paramMap, "blackList",
                    Config.DEFAULT_BLACK_LIST, false);

            logger.info("node ip        : {}", this.selfGossipAddress.pubIP);
            logger.info("node gossipPort: {}", this.selfGossipAddress.gossipPort);
            logger.info("node rpcPort   : {}", this.selfGossipAddress.rpcPort);
            logger.info("node httpPort  : {}", this.selfGossipAddress.httpPort);
            logger.info("test.clearDb : {}", this.clearDb);
            logger.info("test.env : {}", this.env);
            logger.info("test.prefix : {}", this.prefix);
        } catch (Exception e) {
            throw new RuntimeException(">>>>> param is not valid. error: " + e);
        }
    }

    private Boolean getConfigParam(Communicator communicator, HashMap<String, String> paramMap, String name,
                                   boolean defaultValue) {
        return getConfigParam(communicator, paramMap, name, defaultValue, false);
    }

    private Boolean getConfigParam(Communicator communicator, HashMap<String, String> paramMap, String name,
                                   boolean defaultValue, boolean isRequired) {
        return 1==getConfigParam(communicator, paramMap, name, defaultValue?1:0, isRequired);
    }

    private Integer getConfigParam(Communicator communicator, HashMap<String, String> paramMap, String name,
                                   int defaultValue) {
        return getConfigParam(communicator, paramMap, name, defaultValue, false);
    }

    private Integer getConfigParam(Communicator communicator, HashMap<String, String> paramMap, String name,
                                   int defaultValue, boolean isRequired) {
        int rst = Integer.parseInt(getConfigParam(communicator, paramMap, name, ""+defaultValue, isRequired));
        if (-1==rst && isRequired) {
            logger.error("param {} is empty.", name);
            throw new RuntimeException("param " + name + " is empty.");
        }
        return rst;
    }

    private Long getConfigParam(Communicator communicator, HashMap<String, String> paramMap, String name,
                                long defaultValue) {
        return getConfigParam(communicator, paramMap, name, defaultValue, false);
    }

    private Long getConfigParam(Communicator communicator, HashMap<String, String> paramMap, String name,
                                long defaultValue, boolean isRequired) {
        return Long.parseLong(getConfigParam(communicator, paramMap, name, ""+defaultValue, isRequired));
    }

    private Double getConfigParam(Communicator communicator, HashMap<String, String> paramMap, String name,
                                double defaultValue) {
        return getConfigParam(communicator, paramMap, name, defaultValue, false);
    }

    private Double getConfigParam(Communicator communicator, HashMap<String, String> paramMap, String name,
                                double defaultValue, boolean isRequired) {
        return Double.parseDouble(getConfigParam(communicator, paramMap, name, ""+defaultValue, isRequired));
    }

    private List<String> getConfigParam(Communicator communicator, HashMap<String, String> paramMap, String name,
                                List<String> defaultValue) {
        return getConfigParam(communicator, paramMap, name, defaultValue, false);
    }

    private List<String> getConfigParam(Communicator communicator, HashMap<String, String> paramMap, String name,
                                  List<String> defaultValue, boolean isRequired) {
        String value = paramMap.get(name.toLowerCase());
        if (StringUtils.isEmpty(value)) {
            value = getConfigParamFromFile(communicator, name);
            if (StringUtils.isEmpty(value)) {
                if (isRequired && (null==defaultValue || defaultValue.size()<=0 )) {
                    logger.error("param {} is empty.", name);
                    throw new RuntimeException("param " + name + " is empty.");
                }
                return defaultValue;
            } else {
                return Arrays.asList(value.split(","));
            }
        } else {
            return Arrays.asList(value.split(","));
        }
    }

    private String getConfigParam(Communicator communicator, HashMap<String, String> paramMap, String name) {
        return getConfigParam(communicator, paramMap, name, "", false);
    }

    private String getConfigParam(Communicator communicator, HashMap<String, String> paramMap, String name,
                                  String defaultValue) {
        return getConfigParam(communicator, paramMap, name, defaultValue, false);
    }

    private String getConfigParam(Communicator communicator, HashMap<String, String> paramMap, String name,
                                  String defaultValue, boolean isRequired) {
        String value = paramMap.get(name.toLowerCase());
        if (StringUtils.isEmpty(value)) {
            value = getConfigParamFromFile(communicator, name);
            if (StringUtils.isEmpty(value)) {
                if (isRequired && StringUtils.isEmpty(defaultValue)) {
                    logger.error("param {} is empty.", name);
                    throw new RuntimeException("param " + name + " is empty.");
                }
                return defaultValue;
            } else {
                return value;
            }
        } else {
            return value;
        }
    }

    private String getConfigParamFromFile(Communicator communicator, String name) {
        return communicator.getProperties().getProperty(name);
    }
}
