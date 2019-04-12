package one.inve.node;

import com.alibaba.fastjson.JSONArray;
import com.zeroc.Ice.Communicator;
import one.inve.bean.node.GossipAddress;
import one.inve.core.Config;
import one.inve.util.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;

public class NodeParameters {
    private static final Logger logger = LoggerFactory.getLogger(NodeParameters.class);
    /**
     * seed的IP和端口信息
     */
    public GossipAddress seedGossipAddress;
    /**
     * 节点自己的IP和端口信息
     */
    public GossipAddress selfGossipAddress;

    public int multiple;
    public int clearDb;
    public String env;
    public String prefix;
    public String mnemonic;

    public String dbId;

    public void init(Communicator communicator, String[] args) {
        if (null==seedGossipAddress) {
            seedGossipAddress = new GossipAddress();
        }
        if (null==selfGossipAddress) {
            selfGossipAddress = new GossipAddress();
        }
        logger.warn("params: {}", JSONArray.toJSONString(args));
        boolean existConfigFile = false;
        HashMap<String, String> paramMap = new HashMap<>();
        for(int i=0; i<args.length; i++) {
            String p = args[i];
            if (StringUtils.isEmpty(p)) {
                continue;
            }
            if (p.startsWith("-D")) {
                p = p.substring(2);
                String[] keyValuePair = p.split("=");
                if(keyValuePair.length>1) {
                    paramMap.put(keyValuePair[0], keyValuePair[1]);
                    System.setProperty(keyValuePair[0], keyValuePair[1]);
                }
                logger.warn("{}: {}", keyValuePair[0], System.getProperty(keyValuePair[0]));
            }
            if (!existConfigFile && p.startsWith("--Ice.Config=")) {
                existConfigFile = true;
            }
        }

        if (!existConfigFile ) {
            logger.error("need --Ice.Config command parameter!!!");
            throw new RuntimeException("need --Ice.Config command parameter!!!");
        }

        // seed
        try {
            this.seedGossipAddress.pubIP =
                    getConfigParam(communicator, paramMap, "seed.pubIP", Config.DEFAULT_SEED_PUBIP, true);
            this.seedGossipAddress.gossipPort = Integer.parseInt(
                    getConfigParam(communicator, paramMap, "seed.gossipPort", Config.DEFAULT_SEED_GOSSIP_PORT, true) );
            this.seedGossipAddress.rpcPort = Integer.parseInt(
                    getConfigParam(communicator, paramMap, "seed.rpcPort", Config.DEFAULT_SEED_RPC_PORT, true) );
            this.seedGossipAddress.httpPort = Integer.parseInt(
                    getConfigParam(communicator, paramMap, "seed.httpPort", Config.DEFAULT_SEED_HTTP_PORT, true) );
            // 自己节点
            this.selfGossipAddress.pubIP =
                    getConfigParam(communicator, paramMap, "myself.pubIP", Config.DEFAULT_SEED_PUBIP, true);
            this.selfGossipAddress.gossipPort = Integer.parseInt(
                    getConfigParam(communicator, paramMap, "myself.gossipPort", null, true) );
            this.selfGossipAddress.rpcPort = Integer.parseInt(
                    getConfigParam(communicator, paramMap, "myself.rpcPort", null, true) );
            this.selfGossipAddress.httpPort = Integer.parseInt(
                    getConfigParam(communicator, paramMap, "myself.httpPort", null, true) );
            // 测试用的消息倍数
//            this.multiple = Integer.parseInt(
//                    getConfigParam(communicator, paramMap, "test.multiple", "1", true) );
//            if (this.multiple<=0) {
//                throw new RuntimeException("param multiple is illegal. Please input integer bigger than 0.");
//            }
            this.env = getConfigParam(communicator, paramMap, "test.env");
            this.clearDb = 0;
            this.multiple = 1;
//            this.clearDb = Integer.parseInt(
//                        getConfigParam(communicator, paramMap, "test.clearDb", "1", true) );
            this.prefix = "online".equals(this.env)
                    ? "" : getConfigParam(communicator, paramMap, "test.prefix", "", true);
            this.mnemonic =
                    getConfigParam(communicator, paramMap, "mnemonic", "", false);
            if (StringUtils.isNotEmpty(this.mnemonic)) {
                this.mnemonic = this.mnemonic.replaceAll("'", "");
            }

            logger.info("node ip        : {}", this.selfGossipAddress.pubIP);
            logger.info("node gossipPort: {}", this.selfGossipAddress.gossipPort);
            logger.info("node rpcPort   : {}", this.selfGossipAddress.rpcPort);
            logger.info("node httpPort  : {}", this.selfGossipAddress.httpPort);
            logger.info("test.multiple : {}", this.multiple);
            logger.info("test.clearDb : {}", this.clearDb);
            logger.info("test.env : {}", this.env);
            logger.info("test.prefix : {}", this.prefix);
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException(">>>>> param is not valid. error: " + e);
        }
    }

    private String getConfigParam(Communicator communicator, HashMap<String, String> paramMap, String name) {
        return getConfigParam(communicator, paramMap, name, null, false);
    }

    private String getConfigParam(Communicator communicator, HashMap<String, String> paramMap, String name, String defaultValue) {
        return getConfigParam(communicator, paramMap, name, defaultValue, false);
    }

    private String getConfigParam(Communicator communicator, HashMap<String, String> paramMap, String name, String defaultValue, boolean isRequired) {
        String value = paramMap.get(name);
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
