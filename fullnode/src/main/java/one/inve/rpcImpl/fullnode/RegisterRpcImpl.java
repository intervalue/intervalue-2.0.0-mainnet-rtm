package one.inve.rpcImpl.fullnode;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONException;
import com.alibaba.fastjson.JSONObject;
import com.zeroc.Ice.Current;
import one.inve.bean.node.*;
import one.inve.core.*;
import one.inve.node.GeneralNode;
import one.inve.node.SqliteDAO;
import one.inve.rpc.fullnode.Register;
import one.inve.util.ResponseUtils;
import one.inve.util.StringUtils;
import org.apache.log4j.Logger;

import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;

/**
 * register rpc接口实现类
 * Created by Clarelau61803@gmail.com on 2018/6/5.
 */
public class RegisterRpcImpl implements Register {
    public Logger logger = Logger.getLogger("RegisterRpcImpl.class");
    GeneralNode node;
    public RegisterRpcImpl(GeneralNode node){
        this.node = node;
    }

    /**
     * 注册新的全节点
     * @param pubkey 新注册节点公钥
     * @param current 连接信息
     * @return 注册成功""，注册失败则返回null
     */
    @Override
    public String registerFullNode(String ip, int rpcPort, int httpPort,  String pubkey, Current current) {
        logger.info("registerFullNode...");
        return RegisterService.registerFullNode(pubkey, ip, rpcPort, httpPort, "", node);
    }

    @Override
    public String addNewFullNode(String fullnodeStr, Current current) {
        return "invalid interface.";
    }

    /**
     * 注销已有的全节点
     * @param pubkey 被注销节点公钥
     * @param current 连接信息
     * @return 注销成功则返回“”，否则返回失败信息
     */
    @Override
    public String logoutFullNode(String pubkey, Current current) {
        return RegisterService.logoutFullNode(pubkey, node);
    }

    /**
     * 查询全节点列表
     * @param pubkey 请求者公钥
     * @param current 连接信息
     * @return 全节点列表
     */
    @Override
    public String getFullNodeList(String pubkey, Current current) {
        return RegisterService.queryFullNodeList(pubkey, node);
    }

    /**
     * （轻节点等）获取（局部全节点）邻居列表
     * @param pubkey 请求者公钥
     * @param current 连接信息
     * @return 局部全节点邻居列表
     */
    @Override
    public String getNeighborLocalFullNodeList(String pubkey, Current current) {
        System.out.println(">>> getLocalfullnodeListInShard() ... ");
        long shardCount = node.getLocalFullNodeList().values().stream()
                .filter(n -> n.getStatus()== NodeStatus.HAS_SHARDED)
                .map(LocalFullNode::getShard).distinct().count();
        if (node.openServiceState == 2 && shardCount > 0) {
            List<LocalFullNode> neighbors = RegisterService.queryLocalfullnodeNeighborList(
                    (shardCount==1) ? "0" : RegisterService.generateShardIdByPubKey((int)shardCount, pubkey),
                    node);
            return (neighbors.size()<=0)
                        ? ResponseUtils.normalResponse()
                        : ResponseUtils.normalResponse(JSONArray.toJSONString(neighbors));
        } else {
            return ResponseUtils.handleExceptionResponse("service not start now.");
        }
    }

    /**
     * 获取现有所有局部全节点列表
     * @param pubkey 请求者公钥
     * @param current 连接信息
     * @return 所有局部全节点列表
     */
    @Override
    public String getLocalFullNodeList(String pubkey, Current current) {
        if (node.openServiceState == 2) {
            List<LocalFullNode> localfullnodes = RegisterService.queryLocalfullnodeList(node);
            return (null==localfullnodes || localfullnodes.size()<=0)
                    ? ResponseUtils.normalResponse()
                    : ResponseUtils.normalResponse(JSONArray.toJSONString(localfullnodes));
        } else {
            return ResponseUtils.handleExceptionResponse("service not start now.");
        }
    }

    /**
     * (局部全节点)获取自己分片信息
     * @param pubkey 请求者公钥
     * @param current 连接信息
     * @return 请求者分片信息
     */
    @Override
    public String getNodeShardInfo(String pubkey, Current current) {
        if (node.openServiceState == 2) {
            if (StringUtils.isEmpty(pubkey)) {
                logger.error("ERROR: need param pubkey!");
                throw new RuntimeException("ERROR: need param pubkey!");
            }
            LocalFullNode n = node.getLocalFullNodeList().get(pubkey);
            return (null!=n && n.getStatus()==NodeStatus.HAS_SHARDED)
                    ? ResponseUtils.normalResponse() : ResponseUtils.normalResponse(JSON.toJSONString(n));
        } else {
            return ResponseUtils.handleExceptionResponse("service not start now.");
        }
    }

    /**
     * 获取所有分片信息
     * @param current 连接信息
     * @return 所有局部全节点分片信息，没有则返回null
     */
    @Override
    public String getShardInfoList(Current current) {
        if (node.openServiceState == 2) {
            List<LocalFullNode> list = node.getLocalFullNodeList().values().parallelStream()
                    .filter(n -> n.getStatus()== NodeStatus.HAS_SHARDED ).collect(Collectors.toList());
            return (list.size()<=0)
                    ? ResponseUtils.normalResponse() : ResponseUtils.normalResponse(JSONArray.toJSONString(list));
        } else {
            return ResponseUtils.handleExceptionResponse("service not start now.");
        }
    }

    /**
     * 获取所有全节点公钥
     * @param current 连接信息
     * @return 所有全节点公钥
     */
    @Override
    public String[] getFullNodePublicKeyList(Current current) {
        return (null==node.getFullNodeList() || node.getFullNodeList().size()<=0) ? null
                : node.getFullNodeList().values().stream()
                .filter(BaseNode::checkIfConsensusNode)
                .map(BaseNode::getPubkey).toArray(String[]::new);
    }

    /**
     * 获取所有局部全节点公钥
     * @param current 连接信息
     * @return 所有局部全节点公钥
     */
    @Override
    public String[][] getLocalFullNodePublicKeyList(Current current) {
        logger.info(">>> getLocalFullNodePublicKeyList()...");
        if (node.openServiceState == 2 ) {
            List<LocalFullNode> list = node.getLocalFullNodeList().values().stream()
                    .filter(n -> n.getStatus()== NodeStatus.HAS_SHARDED ).collect(Collectors.toList());
            long shardCount = list.stream().map(LocalFullNode::getShard).distinct().count();
            if (list.size() < shardCount*node.parameters.shardNodeSize) {
                return null;
            }

            String[][] results = new String[(int)shardCount][node.parameters.shardNodeSize];
            list.forEach(n -> results[Integer.parseInt(n.getShard())][Integer.parseInt(n.getIndex())] = n.getPubkey());
            return results;
        } else {
            return null;
        }
    }

    /**
     * 更新中继节点的心跳时间
     * @param pubkey 中继节点的公钥
     * @param current ice连接对象
     * @return 无
     */
    @Override
    public String aliveHeartRelayNode(String pubkey, Current current) {
        RelayNode relay = node.getRelayNodeList().get(pubkey);
        if(null!=relay) {
            relay.setLastAliveTimestamp(Instant.now().toEpochMilli());
            node.getRelayNodeList().put(relay.getPubkey(), relay);
            SqliteDAO.updateRelayNode(node.parameters.dbFile, relay);
        }
        return "";
    }

    /**
     * 发送pbft相关消息
     * @param message 消息
     * @param current 连接信息
     * @return 是否成功
     */
    @Override
    public boolean sendPbftMessage(String message, Current current) {
        if (node.openServiceState >= 1 ) {
            boolean result = false;
            try {
                JSONObject json = JSON.parseObject(message);
                Message msg = Message.messageConvert(json);
                if (msg.sequence_no < this.node.getCurrentSequenceNo()) {
                    logger.info("Dropped old " + msg.getTypeString() + " message "
                                    + " with sequence_no " + msg.sequence_no + ". Delay: " +
                                    (this.node.getCurrentSequenceNo() - msg.sequence_no)
                    );
                    return false;
                } else {
                    logger.info(">>>> received " + msg.getTypeString() + " message "
                            + " with sequence_no " + msg.sequence_no);
                }
                node.mq.add(msg);
            } catch (JSONException e) {
                logger.error("Convert the String to JSONObject failed.", e);
            }
            return result;
        } else {
            return false;
        }
    }

    /**
     * 查询分片区块
     * @param index 区块索引即高度
     * @param current 连接信息
     * @return 区块
     */
    @Override
    public String queryBlock(String index, Current current) {
        Block block = SqliteDAO.queryBlockFromDatabase(node.parameters.dbFile, index);
        return (null==block) ? null : JSON.toJSONString(block);
    }

    public static void main(String[] args) {
        ShardInfo shardInfo = new ShardInfo.Builder().pubkey("1111").shard("0").index("1").rpcPort("1111").build();
        String data = JSON.toJSONString(shardInfo);
        ProposeMessage proposeMessage = new ProposeMessage(
                System.currentTimeMillis()/5000,
                "pubkey".getBytes(),
                true,
                "sign".getBytes(),
                data.getBytes()
        );
        try {
            JSONObject json = JSON.parseObject(proposeMessage.messageEncode().toString());
            Message msg = Message.messageConvert(json);
            if (msg.sequence_no < System.currentTimeMillis()/5000) {
                System.out.println("44");
            } else {
                System.out.println("55");
            }
            new MessageQueue().add(msg);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
}
