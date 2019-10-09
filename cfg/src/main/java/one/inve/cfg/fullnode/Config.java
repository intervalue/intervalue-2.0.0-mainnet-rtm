package one.inve.cfg.fullnode;

import java.util.Arrays;
import java.util.List;

/**
 * 配置参数常量类
 */
public class Config {
	public static final String INIT_SQLITE_FILE = "initial.hashnet.sqlite";
	public static final String DEFAULT_REAL_SQLITE_FILE = "hashnet.sqlite";
	public static final String DEFAULT_WALLET_FILE = "wallet.json";
	public static final String DEFAULT_KEYS_FILE = "keys.json";

	public static final int DEFAULT_SHARD_SIZE = 1;
	public static final int DEFAULT_SHARD_NODE_SIZE = 4;
	public static final int DEFAULT_NEIGHBOR_SIZE = 10;

	public static final int DEFAULT_STATISTICS_INTERVAL = 5000;
	public static final int DEFAULT_STATISTICS_BATCHES = 20000;

	/**
	 * seed全节点地址信息
	 */
	public static String DEFAULT_SEED_PUBIP = "11.11.11.11";
//    public static final String DEFAULT_SEED_PUBIP = "35.170.77.230";
//    public static final String DEFAULT_SEED_PUBIP = "172.17.2.125";
//    public static final String DEFAULT_SEED_PUBIP = "172.17.2.118";
	public static int DEFAULT_SEED_GOSSIP_PORT = 50000;
	public static int DEFAULT_SEED_RPC_PORT = 50001;
	public static int DEFAULT_SEED_HTTP_PORT = 50003;

	/**
	 * 可以进行pbft共识的最小（全节点）数量
	 */
	public static final int DEFAULT_PBFT_NODE_MIN_SIZE = 4;
	/**
	 * 全节点和局部全节点gossip网络维护轮循时间
	 */
	public static final long DEFAULT_NODE_GOSSIP_INTERVAL = 3 * 1000;
	/**
	 * 中继节点跨链手续费
	 */
	public static final double DEFAULT_EXCHANGE_FEE_RATIO = 0.005;
	/**
	 * 中继节点心跳超时时间（超过这个时间表明节点不在线）
	 */
	public static final long DEFAULT_RELAY_ALIVE_TIME_OUT = 5 * 60 * 1000;
	/**
	 * （中继）节点信息（lastAliveTimestamp变化除外）更新的最小间隔时间
	 */
	public static final long DEFAULT_NODE_UPDATE_MIN_INTERVAL = 6 * 1000;
	/**
	 * 轮循获取多链币种兑换汇率并更新的最小间隔时间
	 */
	public static final long DEFAULT_RATIO_UPDATE_MIN_INTERVAL = 5 * 60 * 1000;

	public static final String DEFAULT_VERSION = "dev";
	public static final String DEFAULT_CONFIG_PATH = ".";

	/**
	 * 白名单
	 */
	public static final List<String> DEFAULT_WHITE_LIST = Arrays.asList(
			// Franics.Deng 4/6/2019
			// One lfn is able to join LocalFullNodeNeighborPools if it's in the list
			"singapore.trilliontrust.com", "virginia.trilliontrust.com", "frankfurt.trilliontrust.com",
			"sydney.trilliontrust.com", "oregen.trilliontrust.com", "seed.trilliontrust.com", "13.251.88.167",
			"34.229.63.149", "54.93.192.131", "13.239.97.99", "34.209.62.237", "34.220.63.1",

			"172.17.2.125", "172.17.2.118", "172.17.2.119",

			"52.38.39.190", "18.191.18.104", "34.222.44.163", "18.197.142.235",

			"35.170.77.230", "34.220.63.1",

			// 4/11/2019 ip address list
			"52.221.119.220", "3.213.114.163", "3.121.158.99", "3.105.17.5", "52.38.78.194");

	/**
	 * 黑名单
	 */
	public static final List<String> DEFAULT_BLACK_LIST = Arrays.asList("117.149.24.124");

	/**
	 * 提供的rpc服务列表
	 */
	public static final List<String> RPC_SERVICE_NAMES = Arrays.asList("one.inve.rpcImpl.fullnode.RegisterRpcImpl");
}
