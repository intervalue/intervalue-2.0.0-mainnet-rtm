package one.inve.rpcImpl.fullnode;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.TypeReference;
import one.inve.bean.node.*;
import one.inve.cluster.Member;
import one.inve.node.GeneralNode;
import one.inve.node.SqliteDAO;
import one.inve.rpc.fullnode.RegisterPrx;
import one.inve.threads.GossipNodeThread;
import one.inve.util.HnKeyUtils;
import one.inve.util.ResponseUtils;
import one.inve.util.StringUtils;
import one.inve.util.ValidateUtils;
import org.apache.log4j.Logger;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

/**
 * http接口和rpc接口功能逻辑实现
 * @author Clare
 * @date   2018/6/29 0029.
 */
public class RegisterService {
    public final static Logger logger = Logger.getLogger("RegisterService.class");

    /**
     * 获取局部全节点邻居列表(返回个数由COnfig.DEFAULT_NEIGHBOR_SIZE限制)
     * @param shardId 分片ID
     * @return 局部全节点邻居列表
     */
    public static List<LocalFullNode> queryLocalfullnodeNeighborList(String shardId, GeneralNode node) {
        List<Member> localfullnodes = GossipNodeThread.LocalFullNodeNeighborPools.stream()
                .filter(m -> shardId.equals(""+m.metadata().get("shard")))
                .collect(Collectors.toList());
        logger.info("current local full node list size : " + localfullnodes.size());
        return localfullnodes.stream().map(m -> new LocalFullNode.Builder().ip(m.address().host())
                                .rpcPort(Integer.parseInt(""+m.metadata().get("rpcPort")))
                                .httpPort(Integer.parseInt(""+m.metadata().get("httpPort"))).build() )
                .collect(Collectors.toList())
                .subList(0,  Math.min(node.parameters.neighborMaxSize, localfullnodes.size()));
    }

    /**
     * 获取局部全节点列表
     * @return 局部全节点列表
     */
    public static List<LocalFullNode> queryLocalfullnodeList(GeneralNode node) {
        List<LocalFullNode> localfullnodes = node.getLocalFullNodeList().values().parallelStream()
                .filter(n -> n.getStatus()== NodeStatus.HAS_SHARDED
                        || n.getStatus()== NodeStatus.WAIT_UPDATE_HAS_CONSENSUSED
                        || n.getStatus()== NodeStatus.UPDATTING_HAS_CONSENSUSED)
                .collect(Collectors.toList());
        logger.info("current local full node list size : " + localfullnodes.size());
        return localfullnodes;
    }

    /**
     * 注册中继节点
     * @param pubkey 申请者的公钥
     * @param ip 申请者的IP地址
     * @param port 申请者的对外服务端口
     * @param data 其他数据
     * @param node seed节点对象
     * @return 处理结果
     *      code: 200-成功, 其他-失败
     *      data: 成功则为"", 否则返回失败错误提示
     */
    public static String registerRelayNode(String pubkey, String ip, int port, String data, GeneralNode node) throws Exception {
        if (node.openServiceState >= 1 && !node.getRatios().isEmpty()) {
            // 验证公钥合法性
            if (StringUtils.isEmpty(pubkey)) {
                String msg = "public key is null.";
                logger.error(msg);
                return ResponseUtils.response(203, msg);
            }
            JSONObject o = null;
            try {
               o = JSONObject.parseObject(data);
            } catch (Exception e) {
                logger.error("parameter data cannot be cast to JSONObject.");
                return ResponseUtils.paramIllegalResponse("parameter data cannot be cast to JSONObject");
            }
            if (null==o) {
                logger.error("parameter data is null.");
                return ResponseUtils.paramIllegalResponse();
            }
            String name = o.getString("name");
            String phone = o.getString("phone");
            String email = o.getString("email");
            double feeRatio = (null==o.getDouble("feeRatio"))
                    ? node.parameters.exchangeFeeRatio : o.getDouble("feeRatio");
            String addressesStr = o.getString("addresses");
            if (StringUtils.isEmpty(phone)
                    || StringUtils.isEmpty(email) || !ValidateUtils.isEmail(email)
                    || StringUtils.isEmpty(name) || name.length()>10
                    || StringUtils.isEmpty(addressesStr)
                    || (feeRatio>1.0 && feeRatio<0.0)) {
                logger.error("parameter name or phone or email or addresses or feeRatio is illegal.");
                return ResponseUtils.paramIllegalResponse();
            }
            HashMap<String, String> addresses;
            try {
                addresses = JSON.parseObject(addressesStr, new TypeReference<HashMap<String, String>>() {});
            } catch (Exception e) {
                String msg = "param address parser error.";
                logger.error("param address parser error: ", e);
                return ResponseUtils.response(203, msg);
            }

            RelayNode relay = node.getRelayNodeList().get(pubkey);
            if (null!=relay ) {
                long nowTime = Instant.now().toEpochMilli();
                if (nowTime - relay.getLastAliveTimestamp() < node.parameters.nodeUpdateMinInterval) {
                    String msg = "update too often.";
                    logger.warn(msg);
                    return ResponseUtils.response(203, msg);
                }
                if (relay.getIp().equalsIgnoreCase(ip)
                        && relay.getHttpPort()== port
                        && relay.getPhone().equals(phone)
                        && relay.getName().equals(name)
                        && relay.getEmail().equals(email)
                        && relay.getFeeRatio() == feeRatio
                        && relay.getAddresses().equals(addresses) ) {
                    logger.warn("relay node public key exist.");
                    return ResponseUtils.response(203, "repeat register.");
                } else {
                    if (null!=node.getPendingBlock()) {
                        return ResponseUtils.handleExceptionResponse("busy: some nodes are in consensus.");
                    }
                    relay.setIp(ip);
                    relay.setHttpPort(port);
                    relay.setName(name);
                    relay.setPhone(phone);
                    relay.setEmail(email);
                    relay.setFeeRatio(feeRatio);
                    relay.setAddresses(addresses);
                    relay.setLastAliveTimestamp(nowTime);
                    switch (relay.getStatus()) {
                        case NodeStatus.WAIT_CONSENSUS:
                        case NodeStatus.WAIT_UPDATE_IN_CONSENSUS:
                        case NodeStatus.WAIT_UPDATE_HAS_CONSENSUSED:
                            break;
                        case NodeStatus.UPDATTING_IN_CONSENSUS:
                        case NodeStatus.IN_CONSENSUS:
                        case NodeStatus.WAIT_DELETE_IN_CONSENSUS:
                        case NodeStatus.DELETTING_IN_CONSENSUS:
                            relay.setStatus(NodeStatus.WAIT_UPDATE_IN_CONSENSUS);
                            break;
                        case NodeStatus.UPDATTING_HAS_CONSENSUSED:
                        case NodeStatus.HAS_CONSENSUSED:
                        case NodeStatus.WAIT_DELETE_HAS_CONSENSUSED:
                        case NodeStatus.DELETTING_HAS_CONSENSUSED:
                            relay.setStatus(NodeStatus.WAIT_UPDATE_HAS_CONSENSUSED);
                            break;
                        default:
                            relay.setStatus(NodeStatus.WAIT_CONSENSUS);
                            break;
                    }
                }
                node.getRelayNodeList().put(pubkey, relay);
            } else {
                //不存在则待打包
                long timestamp = Instant.now().toEpochMilli();
                relay = new RelayNode.Builder().type(NodeTypes.RELAYNODE).feeRatio(feeRatio).addresses(addresses)
                        .pubkey(pubkey).ip(ip).httpPort(port).status(NodeStatus.WAIT_CONSENSUS)
                        .phone(phone).email(email).name(name)
                        .registerTimestamp(timestamp).lastAliveTimestamp(timestamp).build();
                node.getRelayNodeList().put(pubkey, relay);
                SqliteDAO.addRelayNode(node.parameters.dbFile, relay);
            }
            return ResponseUtils.normalResponse();
        } else {
            String msg = "register service not begin.";
            logger.warn(msg);
            return ResponseUtils.response(203, msg);
        }
    }

    /**
     * 注销中继节点
     * @param pubkey 申请者的公钥
     * @param node seed节点对象
     * @return 处理结果
     *      code: 200-成功, 其他-失败
     *      data: 成功则为"", 否则返回失败错误提示
     */
    public static String logoutRelayNode(String pubkey, GeneralNode node) {
        if (node.openServiceState >= 1 && !node.getRatios().isEmpty()) {
            // 验证公钥合法性
            if (StringUtils.isEmpty(pubkey)) {
                String msg = "public key is null.";
                logger.error(msg);
                return ResponseUtils.response(203, msg);
            }

            RelayNode relay = node.getRelayNodeList().get(pubkey);
            if (null!=relay) {
                if (null!=node.getPendingBlock()) {
                    return ResponseUtils.handleExceptionResponse("busy: some nodes are in consensus.");
                }
                long nowTime = Instant.now().toEpochMilli();
                if (nowTime - relay.getLastAliveTimestamp() < node.parameters.nodeUpdateMinInterval) {
                    String msg = "update too often.";
                    logger.warn(msg);
                    return ResponseUtils.response(203, msg);
                }
                relay.setLastAliveTimestamp(Instant.now().toEpochMilli());
                switch (relay.getStatus()) {
                    case NodeStatus.WAIT_CONSENSUS:
                        node.getRelayNodeList().remove(relay.getPubkey());
                        return ResponseUtils.normalResponse();
                    case NodeStatus.DELETTING_IN_CONSENSUS:
                    case NodeStatus.DELETTING_HAS_CONSENSUSED:
                        return ResponseUtils.normalResponse();
                    case NodeStatus.WAIT_UPDATE_IN_CONSENSUS:
                    case NodeStatus.WAIT_DELETE_IN_CONSENSUS:
                    case NodeStatus.IN_CONSENSUS:
                    case NodeStatus.UPDATTING_IN_CONSENSUS:
                        relay.setStatus(NodeStatus.WAIT_DELETE_IN_CONSENSUS);
                        return ResponseUtils.normalResponse();
                    case NodeStatus.WAIT_UPDATE_HAS_CONSENSUSED:
                    case NodeStatus.WAIT_DELETE_HAS_CONSENSUSED:
                    case NodeStatus.HAS_CONSENSUSED:
                    case NodeStatus.UPDATTING_HAS_CONSENSUSED:
                    default:
                        relay.setStatus(NodeStatus.WAIT_DELETE_HAS_CONSENSUSED);
                        return ResponseUtils.normalResponse();
                }
            } else {
                logger.warn("relay node not exist.");
                return ResponseUtils.normalResponse("relay node not exist.");
            }
        } else {
            String msg = "register service not begin";
            logger.warn(msg);
            return ResponseUtils.response(203, msg);
        }
    }

    /**
     * 查询中继节点列表(心跳超时的不提供)
     * @param data 查询所需数据,默认为空
     * @param node seed节点对象
     * @return 处理结果
     *      code: 200-成功, 其他-失败
     *      data: 成功则为""或者节点列表, 否则返回失败错误提示
     */
    public static String queryRelayNodeList(String data, GeneralNode node) {
        if (node.openServiceState >= 1 && !node.getRatios().isEmpty()) {
            HashMap<String, String> ratios = new HashMap<>();
            for (Iterator<Map.Entry<String, String>> iterator = node.getRatios().entrySet().iterator(); iterator.hasNext();) {
                Map.Entry<String, String> entry = iterator.next();
                ratios.put(entry.getKey(), entry.getValue());

                logger.info("exchange ratio key: " + entry.getKey() + ", value: " + entry.getValue());
            }
            List<RelayNode> list = node.getRelayNodeList().values().stream()
                    .filter(BaseNode::checkIfConsensusNode)
                    .filter(RelayNode::checkIfConsensusNode)
                    .filter(n->RelayNode.checkIfAliveNode(n, node.parameters.relayAliveTimeout))
                    .peek(n -> n.setExchangeRatios((HashMap<String, String>)ratios.clone()))
                    .collect(Collectors.toList());
            return list.size()>0
                    ? ResponseUtils.normalResponse(JSONArray.toJSONString(list)) : ResponseUtils.normalResponse();
        } else {
            String msg = "register service not begin.";
            logger.warn(msg);
            return ResponseUtils.response(203, msg);
        }
    }

    /**
     * 更新中继节点的心跳时间
     * @param pubkey 中继节点公钥
     * @param node seed节点对象
     * @return 处理结果
     *      code: 200-成功, 其他-失败
     *      data: 成功则为"", 否则返回失败错误提示
     */
    public static String updateHeartbeatAlive(String pubkey, GeneralNode node) {
        if (node.openServiceState >= 1 && !node.getRatios().isEmpty()) {
            final RelayNode relay = node.getRelayNodeList().get(pubkey);
            if(null!=relay) {
                final long nowTime = Instant.now().toEpochMilli();
                if (nowTime - relay.getLastAliveTimestamp() < node.parameters.nodeUpdateMinInterval) {
                    String msg = "update too often.";
                    logger.warn(msg);
                    return ResponseUtils.response(203, msg);
                }
                GossipNodeThread.FullNodeNeighborPools.forEach(member -> {
                    String ip = member.address().host();
                    String port = "" + member.metadata().get("rpcPort");
                    RegisterPrx prx = node.buildRegisterConnection2FullNode(ip, port);
                    while (null == prx) {
                        prx = node.buildRegisterConnection2FullNode(ip, port);
                    }
                    prx.aliveHeartRelayNodeAsync(pubkey);
                });
                return ResponseUtils.normalResponse();
            } else {
                return ResponseUtils.normalResponse("relay node not exist.");
            }
        } else {
            String msg = "register service not begin.";
            logger.warn(msg);
            return ResponseUtils.response(203, msg);
        }
    }

    /**
     * 注册全节点
     * @param pubkey 申请者的公钥
     * @param ip 申请者的IP地址
     * @param rpcPort 申请者的对外http服务端口
     * @param httpPort 申请者的对外rpc服务端口
     * @param data 其他数据
     * @param node seed节点对象
     * @return 处理结果
     *      code: 200-成功, 其他-失败
     *      data: 成功则为"", 否则返回失败错误提示
     */
    public static String registerFullNode(String pubkey, String ip, int rpcPort, int httpPort, String data, GeneralNode node) {
        try {
            // 验证公钥合法性(非空、重复、)
            if (StringUtils.isEmpty(pubkey)) {
                String msg = "public key is null.";
                return ResponseUtils.response(203, msg);
            }

            FullNode fullNode = node.getFullNodeList().get(pubkey);
            if (null!=fullNode ) {
                if (fullNode.getIp().equalsIgnoreCase(ip)
                        && fullNode.getHttpPort()==httpPort
                        && fullNode.getRpcPort()==rpcPort) {
                    logger.warn("full node public key exist.");
                    return ResponseUtils.response(203, "repeat register.");
                } else {
                    if (null!=node.getPendingBlock()) {
                        return ResponseUtils.handleExceptionResponse("busy: some nodes are in consensus.");
                    }
                    fullNode.setIp(ip);
                    fullNode.setRpcPort(rpcPort);
                    fullNode.setHttpPort(httpPort);
                    switch (fullNode.getStatus()) {
                        case NodeStatus.WAIT_CONSENSUS:
                        case NodeStatus.WAIT_UPDATE_IN_CONSENSUS:
                        case NodeStatus.WAIT_UPDATE_HAS_CONSENSUSED:
                            break;
                        case NodeStatus.UPDATTING_IN_CONSENSUS:
                        case NodeStatus.IN_CONSENSUS:
                        case NodeStatus.WAIT_DELETE_IN_CONSENSUS:
                        case NodeStatus.DELETTING_IN_CONSENSUS:
                            fullNode.setStatus(NodeStatus.WAIT_UPDATE_IN_CONSENSUS);
                            break;
                        case NodeStatus.UPDATTING_HAS_CONSENSUSED:
                        case NodeStatus.HAS_CONSENSUSED:
                        case NodeStatus.WAIT_DELETE_HAS_CONSENSUSED:
                        case NodeStatus.DELETTING_HAS_CONSENSUSED:
                            fullNode.setStatus(NodeStatus.WAIT_UPDATE_HAS_CONSENSUSED);
                            break;
                        default:
                            fullNode.setStatus(NodeStatus.WAIT_CONSENSUS);
                            break;
                    }
                }
            } else {
                //不存在则待打包
                fullNode = new FullNode.Builder().type(NodeTypes.FULLNODE)
                        .pubkey(pubkey).ip(ip).rpcPort(rpcPort).httpPort(httpPort)
                        .status(NodeStatus.WAIT_CONSENSUS).build();
                SqliteDAO.addFullNode(node.parameters.dbFile, fullNode);
            }
            node.getFullNodeList().put(pubkey, fullNode);
            return ResponseUtils.normalResponse();
        } catch (Exception e) {
            logger.error(e);
            return ResponseUtils.response(203, e.getMessage());
        }
    }

    /**
     * 注销全节点
     * @param pubkey 申请者的公钥
     * @param node seed节点对象
     * @return 处理结果
     *      code: 200-成功, 其他-失败
     *      data: 成功则为"", 否则返回失败错误提示
     */
    public static String logoutFullNode(String pubkey, GeneralNode node) {
        try {
            // 验证公钥合法性
            if (StringUtils.isEmpty(pubkey)) {
                String msg = "public key is null.";
                logger.error(msg);
                return ResponseUtils.response(203, msg);
            }
            try {
                HnKeyUtils.getPublicKey4String(pubkey);
            } catch (Exception e) {
                logger.error(e);
                return ResponseUtils.response(203, "public key is illegal.");
            }

            FullNode fullNode = node.getFullNodeList().get(pubkey);
            if (null!=fullNode) {
                if (null!=node.getPendingBlock()) {
                    return ResponseUtils.handleExceptionResponse("busy: some nodes are in consensus.");
                }
                switch (fullNode.getStatus()) {
                    case NodeStatus.WAIT_CONSENSUS:
                        node.getFullNodeList().remove(fullNode.getPubkey());
                        return ResponseUtils.normalResponse();
                    case NodeStatus.DELETTING_IN_CONSENSUS:
                    case NodeStatus.DELETTING_HAS_CONSENSUSED:
                        return ResponseUtils.normalResponse();
                    case NodeStatus.WAIT_UPDATE_IN_CONSENSUS:
                    case NodeStatus.WAIT_DELETE_IN_CONSENSUS:
                    case NodeStatus.IN_CONSENSUS:
                    case NodeStatus.UPDATTING_IN_CONSENSUS:
                        fullNode.setStatus(NodeStatus.WAIT_DELETE_IN_CONSENSUS);
                        return ResponseUtils.normalResponse();
                    case NodeStatus.WAIT_UPDATE_HAS_CONSENSUSED:
                    case NodeStatus.WAIT_DELETE_HAS_CONSENSUSED:
                    case NodeStatus.HAS_CONSENSUSED:
                    case NodeStatus.UPDATTING_HAS_CONSENSUSED:
                    default:
                        fullNode.setStatus(NodeStatus.WAIT_DELETE_HAS_CONSENSUSED);
                        return ResponseUtils.normalResponse();
                }
            } else {
                logger.warn("full node not exist.");
                return ResponseUtils.normalResponse("full node not exist.");
            }
        } catch (Exception e) {
            logger.error(e);
            return ResponseUtils.response(203, e.getMessage());
        }
    }

    /**
     * 查询全节点节点列表
     * @param pubkey 申请者公钥
     * @param node seed节点对象
     * @return 处理结果
     *      code: 200-成功, 其他-失败
     *      data: 成功则为""或者节点列表, 否则返回失败错误提示
     */
    public static String queryFullNodeList(String pubkey, GeneralNode node) {
        List<FullNode> list = node.getFullNodeList().values().stream()
                .filter(BaseNode::checkIfConsensusNode)
                .collect(Collectors.toList());
        return list.size()>0
                ? ResponseUtils.normalResponse(JSONArray.toJSONString(list)) : ResponseUtils.normalResponse();
    }

    public static String updateVersion(String pubkey, String version, GeneralNode node) {

        return ResponseUtils.normalResponse();
    }

    /**
     * 通过pubkey计算片ID
     * @param shardCount 片数
     * @param pubkey 公钥
     * @return 片ID
     */
    public static String generateShardIdByPubKey(int shardCount, String pubkey) {
        if (StringUtils.isEmpty(pubkey)) {
            String msg = "Parameter pubkey is empty！";
            logger.error(msg);
            throw new Error(msg);
        }
        if (StringUtils.isEmpty(shardCount+"")) {
            String msg = "Parameter shardCount is empty！";
            logger.error(msg);
            throw new Error(msg);
        }
        return "" + (pubkey.charAt(pubkey.length()-1) & (shardCount-1));
    }

    public static void main(String[] args) {
        generateShardIdByPubKey(2, "0xA5RA5jwayqjubpbtYFd87zaphu8erm5iNDBqiNOB6TB2");
//        System.out.println(generateShardIdByPubKey(2, "0"));
//        System.out.println(generateShardIdByPubKey(2, "11"));
//        System.out.println(generateShardIdByPubKey(2, "21"));
//        System.out.println(generateShardIdByPubKey(2, "32"));
//        System.out.println(generateShardIdByPubKey(2, "48"));
//        System.out.println(generateShardIdByPubKey(2, "5A"));

        ArrayList<GossipAddress> gossipAddresses = new ArrayList<>();
        gossipAddresses.add(new GossipAddress.Builder().ip("192.168.0.111").gossipPort(10001).rpcPort(100).build());
        gossipAddresses.add(new GossipAddress.Builder().ip("192.168.0.113").gossipPort(10001).rpcPort(100).build());
        gossipAddresses.add(new GossipAddress.Builder().ip("192.168.0.113").gossipPort(10002).rpcPort(100).build());
        gossipAddresses.add(new GossipAddress.Builder().ip("192.168.0.111").gossipPort(10001).rpcPort(100).build());
        gossipAddresses.add(new GossipAddress.Builder().ip("192.168.0.115").gossipPort(10001).rpcPort(100).build());
        gossipAddresses.add(new GossipAddress.Builder().ip("192.168.0.116").gossipPort(10001).rpcPort(100).build());
        gossipAddresses.add(new GossipAddress.Builder().ip("192.168.0.117").gossipPort(10001).rpcPort(100).build());

        List<LocalFullNode> localfullnodes = gossipAddresses.stream()
                .map(a -> new LocalFullNode.Builder().ip(a.ip).rpcPort(a.rpcPort).build())
                .collect(Collectors.toList());
        localfullnodes.stream().forEach(b -> System.out.println(JSON.toJSONString(b)));
        logger.info("current local full node list size : " + localfullnodes.size());
    }
}
