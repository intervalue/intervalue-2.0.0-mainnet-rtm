package one.inve.localfullnode2.conf;

import java.util.Arrays;
import java.util.List;

/**
 * 参数配置类
 * 
 * @author Clare
 * @date 2018/7/22 0022.
 */
public class Config {
	public static final String VERSION_VALUE = "3.0.0"; //

	public static final String KEYS_FILE = "local.keys.json";
	public static final String WALLET_FILE = "local.wallet.json";
	public static final String GOSSIP_SIGNATURE_DATA = "data4TheInterface2CreatedSignature";

	/**
	 * Francis.Deng 4/5/2019 AIASTTDTUOH aws ip addresses switch to these due to
	 * upgrade of hardware:
	 * ------------------------------------------------------------- From To
	 * Singapore 13.250.14.98 singapore.trilliontrust.com Virginia 3.94.202.66
	 * virginia.trilliontrust.com Frankfurt 3.121.162.184
	 * frankfurt.trilliontrust.com Sydney 13.211.5.129 sydney.trilliontrust.com
	 * Oregen 54.213.84.89 oregen.trilliontrust.com seed 34.220.63.1
	 * seed.trilliontrust.com
	 */
	public static final String DEFAULT_SEED_PUBIP = "34.220.63.1";
//    public static final String DEFAULT_SEED_PUBIP       = "34.220.63.1";
//    public static final String DEFAULT_SEED_PUBIP = "35.170.77.230";
//    public static final String DEFAULT_SEED_PUBIP = "172.17.2.125";
//    public static final String DEFAULT_SEED_PUBIP = "172.17.2.118";
	public static final String DEFAULT_SEED_GOSSIP_PORT = "25005";
	public static final String DEFAULT_SEED_RPC_PORT = "25004";
	public static final String DEFAULT_SEED_HTTP_PORT = "25003";
	// 网络节点gossip间隔(ms)
	public static final int DEFAULT_GOSSIP_NODE_INTERVAL = 4000;
	// gossip Event 线程类型
	public static final int GOSSIP_IN_SHARD = 1;
	public static final int GOSSIP_GLOBAL_SHARD = 2;

	// 缓存Event总数量
	public static final int DEFAULT_EVENTS_MAP_SIZE = 50000;
	// 默认每个event打包transaction最大数量
	public static final int DEFAULT_TRANSACTIONS_PER_EVENT = 500;
	// 同步event时间间隔(ms)
	public static final int DEFAULT_GOSSIP_EVENT_INTERVAL = 500;
	// 默认最大同步EVENT数量
	public static final int DEFAULT_SYNC_EVENT_COUNT = 50;
	// 默认最大同步EVENT总大小(byte)
	public static final int DEFAULT_SYNC_EVENT_SPACE = 512 * 1024;
	// 默认最大同步EVENT总大小(byte)
	public static final int DEFAULT_EVENT_MAX_PAYLOAD = 16 * 1024 * 1024;

	// 获取共识event时间间隔(ms)
	public static final int GET_CONSENSUS_TIMEOUT = 150;

	// transaction提交时间间隔(ms)
	public static final int TXS_COMMIT_TIMEOUT = 400;
	// 每次提交transactions最大数量
	public static final int MAX_TXS_COMMIT_COUNT = 6000;

	// transaction验证时间间隔(ms)
	public static final int TXS_VEFRIFY_TIMEOUT = 400;
	// 每次验证transactions最大数量
	public static final int MAX_TXS_VEFRIFY_COUNT = 6000;

	public static final int DEFAULT_EVENT_STATISTICS_COUNT = 5000;

	// 重构时每次从数据库读取每个hashnet柱子的event数量
	public static final int READ_SIZE_FROM_DB_PER_HASHNETNODE = 3000;
	// 一个消息表能保存200000份
	public static Integer MESSAGES_SPIT_TOTAL = 500000;
	// 交易中间产生的奖励，快照保存500000份
	public static Integer SYSTEM_SPIT_TOTAL = 500000;
	// 消息表前缀
	public static String MESSAGES = "messages";
	// 系统信息表前缀
	public static String SYSTEMAUTOTX = "system_auto_tx";
	public static String SPLIT = "_";

	// 快照点选取：每多少个Event做一次快照
	public static final int EVENT_NUM_PER_SNAPSHOT = 300000;
	// 删除第几代祖先快照之前的Event
	public static final int DEFAULT_SNAPSHOT_CLEAR_GENERATION = 10;
	// 默认最大交易留言字节数
	public static final int DEFAULT_MESSAGE_REMARK_MAX_SIZE = 50;

	// 区块链浏览器获取运行时间的数据库key
	public static final String CREATION_TIME_KEY = "creationTimeKey";
	// 共识message总数的数据库key
	public static final String CONS_MSG_COUNT_KEY = "consMessageCount";
	// 系统自动生成交易总数的数据库key
	public static final String SYS_TX_COUNT_KEY = "sysTxCount";
	// 共识Event总数的数据库key
	public static final String CONS_EVT_COUNT_KEY = "totalConsEventCount";
	// Event总数的数据库key
	public static final String EVT_COUNT_KEY = "eventCount";
	// Event对应Message数量的数据库key
	public static final String EVT_TX_COUNT_KEY = "eventMessageCount";
	// TPS数据的数据库key
	public static final String MESSAGE_TPS_KEY = "messageTps";

	/**
	 * 奖励比例
	 */
	public static final double NODE_REWARD_RATIO = 0.50D;
	/**
	 * 上帝钱包
	 */
	public static final String GOD_PUBKEY = "Ap6hlAbGc/0iup4uTuekC+WIK21pNo700yZVXV69wmx+";
	public static final String GOD_ADDRESS = "CMUORK2YLSE4UA6C6DSFVMRPGWKWH3QH";
	/**
	 * 创世钱包
	 */
	// 测试
//    public static final List<String> CREATION_ADDRESSES = Arrays.asList(
//            "KMV5N7L5DLXLVSMLYM62QM25CAOJNF5H",
//            "RGQKSWGADGHDBHUZUKROAHDEXGQ52R2Z",
//            "HXUN5WGPXQA4HDWPLL6VYOTYMUI2PJMD",
//            "ANWVDHK5LXLKLURFJM4ADZZ7L3AYI277",
//            "ZJ3JYURAY4GPFDFXZUHJ3OOLXJQXWZGF",
//            "7YTL7UYH73WQGTN5OQYGGRZETSAZN7MZ",
//            "DDDY2LJA6KAI4SYNL7DR6QEKA4CFNCFI",
//            "A5TVIJFVPTAMACSROVLPWAZ4UKNM4UQZ",
//            "DRBANQRT4FNACYOU5OATC2S6ZGOWJKKH",
//            "RTJXP4HUFIZDHO7EQLPNMOLTQREQ2SVC"
//    );
	// 线上
	public static final List<String> CREATION_ADDRESSES = Arrays.asList("XK37G6UQIZG26IG2Z7V3RF53PUMO2IZV",
			"65VPMI7HEWSSCKPFPHGJSUNJGAKPCY7B", "BYLSEJXAURGNJKSYN6WC6NLXSFBSVNZO", "2IO2MCZ2X5WKXL75MJ2CCM22CPEBCR7C",
			"ZWYTYGLAU3B4TLJ4JOBZWJEBB3A7TP6D", "SEI3IBWFZZJBWMNPZB3RAKQYARH3DY2S", "ZPUXUMBV6MOZ7FCAIX24ZK3YA62SIZZS",
			"MGQUBT4NROU62NFFXKGWSJFOLXVGDYZG", "YXNQFBKPGXACOT3CI2VRCODIQ2YVD26A", "6KN6O6Y7GQIUR7WWQWIFRZ7ZCPZYSS2H");
	/**
	 * 基金会钱包
	 */

	// 线上
	public static final String FOUNDATION_MNEMONIC = "";
	public static final String FOUNDATION_PUBKEY = "AzZ6psaaBIcYeCEQZRlagF3sSDpYwjeqf1LdA/aTNCL/";
	public static final String FOUNDATION_ADDRESS = "NUOX47THDUFUT7Z6XPNN75YJYRJK2LVC";

	public static final List<String> CREATION_TX_LIST = Arrays.asList(
			// 线上
			"{\"nrgPrice\":\"1000000000\",\"amount\":\"1054764968000000000000000000\",\"signature\":\"32OlO4xXEFaneRDWQeEGv/Jg1xaXmq0vxBhtBRaJ5z7VFiGCtBuEok1Fk2YJbBKJgADRxQddcjSpOuM3xlUsFV8g==\",\"fee\":\"500000\",\"vers\":\"2.0\",\"fromAddress\":\"CMUORK2YLSE4UA6C6DSFVMRPGWKWH3QH\",\"remark\":\"\",\"type\":1,\"toAddress\":\"XK37G6UQIZG26IG2Z7V3RF53PUMO2IZV\",\"timestamp\":1550404800000,\"pubkey\":\"Ap6hlAbGc/0iup4uTuekC+WIK21pNo700yZVXV69wmx+\"}",
			"{\"nrgPrice\":\"1000000000\",\"amount\":\"1421742521000000000000000000\",\"signature\":\"32E69VUOPARaLJD/zlE1msm4dK4n5T7T/dfe5y4N/xPwFeevxSYCUDfrWCWbQ01yfzpyg3A+rmEROlw1vkyzPWCw==\",\"fee\":\"500000\",\"vers\":\"2.0\",\"fromAddress\":\"CMUORK2YLSE4UA6C6DSFVMRPGWKWH3QH\",\"remark\":\"\",\"type\":1,\"toAddress\":\"65VPMI7HEWSSCKPFPHGJSUNJGAKPCY7B\",\"timestamp\":1550404800000,\"pubkey\":\"Ap6hlAbGc/0iup4uTuekC+WIK21pNo700yZVXV69wmx+\"}",
			"{\"nrgPrice\":\"1000000000\",\"amount\":\"813077380000000000000000000\",\"signature\":\"32MBx07e6MeXBjhDAYfhmUIl6xaVWR2Usqgh2MtSWfpLNiiMF+EUJ0HJn8EmQt7h+uYWBlhjOK5BWkD3MGzI5RBQ==\",\"fee\":\"500000\",\"vers\":\"2.0\",\"fromAddress\":\"CMUORK2YLSE4UA6C6DSFVMRPGWKWH3QH\",\"remark\":\"\",\"type\":1,\"toAddress\":\"BYLSEJXAURGNJKSYN6WC6NLXSFBSVNZO\",\"timestamp\":1550404800000,\"pubkey\":\"Ap6hlAbGc/0iup4uTuekC+WIK21pNo700yZVXV69wmx+\"}",
			"{\"nrgPrice\":\"1000000000\",\"amount\":\"1596093681000000000000000000\",\"signature\":\"33AKuaFHH3vXbjVBsT2bY0kmxmF292pCZncRDEXzNVGRNAZewGVGgsD4jvwTxQ0OlMr/BITsauWpfIRwYgt6PqgjE=\",\"fee\":\"500000\",\"vers\":\"2.0\",\"fromAddress\":\"CMUORK2YLSE4UA6C6DSFVMRPGWKWH3QH\",\"remark\":\"\",\"type\":1,\"toAddress\":\"2IO2MCZ2X5WKXL75MJ2CCM22CPEBCR7C\",\"timestamp\":1550404800000,\"pubkey\":\"Ap6hlAbGc/0iup4uTuekC+WIK21pNo700yZVXV69wmx+\"}",
			"{\"nrgPrice\":\"1000000000\",\"amount\":\"1580571863000000000000000000\",\"signature\":\"33AItqaa1WtfjSmgwgaCThvWIgFFES89Iky9Z8UZO1CfqQLCxUZIZ8YnQkp6nUew+rzRu7fV6V8sBqEpoqXk+gzKA=\",\"fee\":\"500000\",\"vers\":\"2.0\",\"fromAddress\":\"CMUORK2YLSE4UA6C6DSFVMRPGWKWH3QH\",\"remark\":\"\",\"type\":1,\"toAddress\":\"ZWYTYGLAU3B4TLJ4JOBZWJEBB3A7TP6D\",\"timestamp\":1550404800000,\"pubkey\":\"Ap6hlAbGc/0iup4uTuekC+WIK21pNo700yZVXV69wmx+\"}",
			"{\"nrgPrice\":\"1000000000\",\"amount\":\"773015568000000000000000000\",\"signature\":\"33AJdyPQhDkaSilzBsibN6lQIFY6A8sY0AqRv8mSDlJ8WxC9QYGgg9zuu0Pye9fb8hMiKGgtFDBSIWW6KHgSFct30=\",\"fee\":\"500000\",\"vers\":\"2.0\",\"fromAddress\":\"CMUORK2YLSE4UA6C6DSFVMRPGWKWH3QH\",\"remark\":\"\",\"type\":1,\"toAddress\":\"SEI3IBWFZZJBWMNPZB3RAKQYARH3DY2S\",\"timestamp\":1550404800000,\"pubkey\":\"Ap6hlAbGc/0iup4uTuekC+WIK21pNo700yZVXV69wmx+\"}",
			"{\"nrgPrice\":\"1000000000\",\"amount\":\"880633803000000000000000000\",\"signature\":\"32Nl054DnyOSjYNU27sau4cSgRm4XXxnbp4oixihsZmZV1sUvqHeFk2ENXxI6pGTr7iYuOfH46F81QBofUyt7Myw==\",\"fee\":\"500000\",\"vers\":\"2.0\",\"fromAddress\":\"CMUORK2YLSE4UA6C6DSFVMRPGWKWH3QH\",\"remark\":\"\",\"type\":1,\"toAddress\":\"ZPUXUMBV6MOZ7FCAIX24ZK3YA62SIZZS\",\"timestamp\":1550404800000,\"pubkey\":\"Ap6hlAbGc/0iup4uTuekC+WIK21pNo700yZVXV69wmx+\"}",
			"{\"nrgPrice\":\"1000000000\",\"amount\":\"736973049000000000000000000\",\"signature\":\"33ALP8b4/J9wzMQC4hdjZKtawL4xBTmC/sskDi6Kz+a/ZFYJp0r2kNDo7AP5UVcfoPWg90EOsmklv1WMb1qijKWiM=\",\"fee\":\"500000\",\"vers\":\"2.0\",\"fromAddress\":\"CMUORK2YLSE4UA6C6DSFVMRPGWKWH3QH\",\"remark\":\"\",\"type\":1,\"toAddress\":\"MGQUBT4NROU62NFFXKGWSJFOLXVGDYZG\",\"timestamp\":1550404800000,\"pubkey\":\"Ap6hlAbGc/0iup4uTuekC+WIK21pNo700yZVXV69wmx+\"}",
			"{\"nrgPrice\":\"1000000000\",\"amount\":\"512770506000000000000000000\",\"signature\":\"33AJXpYN5foZe5XXsJDngJ+deq/juBA3nEQ+bL2FbS8X3vMXSykxIAesQdOZSg3Nlspr5JA9jOcR9G4iX8Kp5cj/E=\",\"fee\":\"500000\",\"vers\":\"2.0\",\"fromAddress\":\"CMUORK2YLSE4UA6C6DSFVMRPGWKWH3QH\",\"remark\":\"\",\"type\":1,\"toAddress\":\"YXNQFBKPGXACOT3CI2VRCODIQ2YVD26A\",\"timestamp\":1550404800000,\"pubkey\":\"Ap6hlAbGc/0iup4uTuekC+WIK21pNo700yZVXV69wmx+\"}",
			"{\"nrgPrice\":\"1000000000\",\"amount\":\"630356661000000000000000000\",\"signature\":\"33ALSC69AR95uWjwto2sbBD10hJzX5Fd3SMnZ3xVD7iNmpfmKOSsJtnDdA6s9mBmy2GErBdx/vRZTTWEOE4qam0Fw=\",\"fee\":\"500000\",\"vers\":\"2.0\",\"fromAddress\":\"CMUORK2YLSE4UA6C6DSFVMRPGWKWH3QH\",\"remark\":\"\",\"type\":1,\"toAddress\":\"6KN6O6Y7GQIUR7WWQWIFRZ7ZCPZYSS2H\",\"timestamp\":1550404800000,\"pubkey\":\"Ap6hlAbGc/0iup4uTuekC+WIK21pNo700yZVXV69wmx+\"}"
	// 测试
//            "{\"nrgPrice\":\"1000000000\",\"amount\":\"1054764968000000000000000000\",\"signature\":\"32YDlqmoQgFfo8syu0P/WEg/2+7F4sJJk2tcEf6OzXNjhxB4Iptoa7qUiqyXaxgIsdFW8F0qE417+gs7FYBHCe3A==\",\"fee\":\"500000\",\"vers\":\"2.0\",\"fromAddress\":\"CMUORK2YLSE4UA6C6DSFVMRPGWKWH3QH\",\"remark\":\"\",\"type\":1,\"toAddress\":\"KMV5N7L5DLXLVSMLYM62QM25CAOJNF5H\",\"timestamp\":1550637677210,\"pubkey\":\"Ap6hlAbGc/0iup4uTuekC+WIK21pNo700yZVXV69wmx+\"}",
//            "{\"nrgPrice\":\"1000000000\",\"amount\":\"1421742521000000000000000000\",\"signature\":\"33ALiCeZCGW6+C7pyNaDleMx+pN2hnugzOdrlUWe9sGAmiX8bFQGm+GGDlNapalQJbgQz6zjOjD1dQA75dvuJkE0g=\",\"fee\":\"500000\",\"vers\":\"2.0\",\"fromAddress\":\"CMUORK2YLSE4UA6C6DSFVMRPGWKWH3QH\",\"remark\":\"\",\"type\":1,\"toAddress\":\"RGQKSWGADGHDBHUZUKROAHDEXGQ52R2Z\",\"timestamp\":1550637677284,\"pubkey\":\"Ap6hlAbGc/0iup4uTuekC+WIK21pNo700yZVXV69wmx+\"}",
//            "{\"nrgPrice\":\"1000000000\",\"amount\":\"813077380000000000000000000\",\"signature\":\"33AJnFz0a4UHaIWYDcVgICz/x7+dYcDALj+EA0Umg1LliWd0Txg2VpkWUmU09p8K/q1Qd6btCuZTrLQgol87n2plQ=\",\"fee\":\"500000\",\"vers\":\"2.0\",\"fromAddress\":\"CMUORK2YLSE4UA6C6DSFVMRPGWKWH3QH\",\"remark\":\"\",\"type\":1,\"toAddress\":\"HXUN5WGPXQA4HDWPLL6VYOTYMUI2PJMD\",\"timestamp\":1550637677313,\"pubkey\":\"Ap6hlAbGc/0iup4uTuekC+WIK21pNo700yZVXV69wmx+\"}",
//            "{\"nrgPrice\":\"1000000000\",\"amount\":\"1596093681000000000000000000\",\"signature\":\"32TJHoJuagqSsoaYWaSEYWQVinNN0Ch8pDCz9S+ZB8GOAY0sww6X/2grx0DoRuTPYUS20cxbZHSOCeOZLUXgHjAQ==\",\"fee\":\"500000\",\"vers\":\"2.0\",\"fromAddress\":\"CMUORK2YLSE4UA6C6DSFVMRPGWKWH3QH\",\"remark\":\"\",\"type\":1,\"toAddress\":\"ANWVDHK5LXLKLURFJM4ADZZ7L3AYI277\",\"timestamp\":1550637677375,\"pubkey\":\"Ap6hlAbGc/0iup4uTuekC+WIK21pNo700yZVXV69wmx+\"}",
//            "{\"nrgPrice\":\"1000000000\",\"amount\":\"1580571863000000000000000000\",\"signature\":\"33AKIViM8p+MxyXA9BX2Yi504ZxsxGNnZhLIQ933adOGtiONRa9QrVcdP88UfGKsKOnsY2NlwWa0Ano13PTkgCMx4=\",\"fee\":\"500000\",\"vers\":\"2.0\",\"fromAddress\":\"CMUORK2YLSE4UA6C6DSFVMRPGWKWH3QH\",\"remark\":\"\",\"type\":1,\"toAddress\":\"ZJ3JYURAY4GPFDFXZUHJ3OOLXJQXWZGF\",\"timestamp\":1550637677407,\"pubkey\":\"Ap6hlAbGc/0iup4uTuekC+WIK21pNo700yZVXV69wmx+\"}",
//            "{\"nrgPrice\":\"1000000000\",\"amount\":\"773015568000000000000000000\",\"signature\":\"33AIC7lr/LezOeYvuUmt5OH6BZQuEmX9D4C8FNrBEYfT9AdQauVye/yopyvc++KM0NNv7YVWmkJGaRpA+/KPanJc4=\",\"fee\":\"500000\",\"vers\":\"2.0\",\"fromAddress\":\"CMUORK2YLSE4UA6C6DSFVMRPGWKWH3QH\",\"remark\":\"\",\"type\":1,\"toAddress\":\"7YTL7UYH73WQGTN5OQYGGRZETSAZN7MZ\",\"timestamp\":1550637677447,\"pubkey\":\"Ap6hlAbGc/0iup4uTuekC+WIK21pNo700yZVXV69wmx+\"}",
//            "{\"nrgPrice\":\"1000000000\",\"amount\":\"880633803000000000000000000\",\"signature\":\"32B2Hc7pPmGOTCZEUv75R5GHHsqOPce2rBfWnIB9AdeScRg3v8F8ozFxUfBH9vWMds8WIrKVbjQS3iYUsSMEWXfA==\",\"fee\":\"500000\",\"vers\":\"2.0\",\"fromAddress\":\"CMUORK2YLSE4UA6C6DSFVMRPGWKWH3QH\",\"remark\":\"\",\"type\":1,\"toAddress\":\"DDDY2LJA6KAI4SYNL7DR6QEKA4CFNCFI\",\"timestamp\":1550637677474,\"pubkey\":\"Ap6hlAbGc/0iup4uTuekC+WIK21pNo700yZVXV69wmx+\"}",
//            "{\"nrgPrice\":\"1000000000\",\"amount\":\"736973049000000000000000000\",\"signature\":\"32DvVXEzRkHZ9kvSIkaa6BYQL0pGn5sLqQ79jX92vh7mpQJTV/FVh4d0UIe0xwgWZlBvM3bcwv8sNymFr55tpKsw==\",\"fee\":\"500000\",\"vers\":\"2.0\",\"fromAddress\":\"CMUORK2YLSE4UA6C6DSFVMRPGWKWH3QH\",\"remark\":\"\",\"type\":1,\"toAddress\":\"A5TVIJFVPTAMACSROVLPWAZ4UKNM4UQZ\",\"timestamp\":1550637677507,\"pubkey\":\"Ap6hlAbGc/0iup4uTuekC+WIK21pNo700yZVXV69wmx+\"}",
//            "{\"nrgPrice\":\"1000000000\",\"amount\":\"512770506000000000000000000\",\"signature\":\"32TmJPxqfgrcn1i4qtseqFQUu8Lqyre31pids7q3YyyyweBwk9ztYtAJx+p42WnmpmxPA8hE7l3CxxUkuN1vcRXQ==\",\"fee\":\"500000\",\"vers\":\"2.0\",\"fromAddress\":\"CMUORK2YLSE4UA6C6DSFVMRPGWKWH3QH\",\"remark\":\"\",\"type\":1,\"toAddress\":\"DRBANQRT4FNACYOU5OATC2S6ZGOWJKKH\",\"timestamp\":1550637677534,\"pubkey\":\"Ap6hlAbGc/0iup4uTuekC+WIK21pNo700yZVXV69wmx+\"}",
//            "{\"nrgPrice\":\"1000000000\",\"amount\":\"630356661000000000000000000\",\"signature\":\"32UamG+bEpIssBmgJGoUgZCSo4ImI/X+EsuRA85N2LXhcyam60KWhtx/BV5dV97nTIh5hSSO5iRtMhbIijdGw/xg==\",\"fee\":\"500000\",\"vers\":\"2.0\",\"fromAddress\":\"CMUORK2YLSE4UA6C6DSFVMRPGWKWH3QH\",\"remark\":\"\",\"type\":1,\"toAddress\":\"RTJXP4HUFIZDHO7EQLPNMOLTQREQ2SVC\",\"timestamp\":1550637677588,\"pubkey\":\"Ap6hlAbGc/0iup4uTuekC+WIK21pNo700yZVXV69wmx+\"}"
	);

	public static final List<String> WHITE_LIST = Arrays.asList(
			/**
			 * Francis.Deng 4/5/2019 AIASTTDTUOH aws ip addresses switch to these due to
			 * upgrade of hardware:
			 * -------------------------------------------------------------- From To
			 * Singapore 13.250.14.98 singapore.trilliontrust.com Virginia 3.94.202.66
			 * virginia.trilliontrust.com Frankfurt 3.121.162.184
			 * frankfurt.trilliontrust.com Sydney 13.211.5.129 sydney.trilliontrust.com
			 * Oregen 54.213.84.89 oregen.trilliontrust.com seed 34.220.63.1
			 * seed.trilliontrust.com
			 */
			"singapore.trilliontrust.com", "virginia.trilliontrust.com", "frankfurt.trilliontrust.com",
			"sydney.trilliontrust.com", "oregen.trilliontrust.com", "seed.trilliontrust.com", "13.251.88.167",
			"34.229.63.149", "54.93.192.131", "13.239.97.99", "34.209.62.237", "34.220.63.1",

			"172.17.2.125",

			"52.38.39.190", "18.191.18.104", "34.222.44.163", "18.197.142.235",

			"35.170.77.230", "34.220.63.1",

			// 4/11/2019 ip address list
			"52.221.119.220", "3.213.114.163", "3.121.158.99", "3.105.17.5", "52.38.78.194");

	public static final List<String> BLACK_LIST = Arrays.asList("192.168.0.1");

	// rpc服务
	public static final String[] SERVICE_ARRAY = { "one.inve.rpcimpl.localfullnode.Light2localImpl",
			"one.inve.rpcimpl.localfullnode.Local2localImpl" };

	/**
	 * coin round发生间隔
	 */
	static int coinFreq = 12;
	/**
	 * 使用RSA
	 */
	public static boolean useRSA = true;
	/**
	 *
	 */
	public static boolean logStack = true;
}
