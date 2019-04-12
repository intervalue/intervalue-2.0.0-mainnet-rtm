package one.inve.threads;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import one.inve.bean.wallet.KeyPair;
import one.inve.bean.node.*;
import one.inve.core.*;
import one.inve.exception.InveException;
import one.inve.node.GeneralNode;
import one.inve.node.SqliteDAO;
import one.inve.util.StringUtils;
import one.inve.utils.DSA;
import org.apache.log4j.Logger;

import java.net.ConnectException;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * @author Clarelau61803@gmail.com
 * @date 2018/10/16 0016 下午 6:49
 **/
public class PbftThread extends Thread {
    public Logger logger = Logger.getLogger("PbftThread.class");
    private GeneralNode node;

    public PbftThread(GeneralNode node) {
        this.node = node;
    }

    @Override
    public void run() {
        Pbft algo = new Pbft(this.node);
        int num =0;
        Instant first = Instant.now();
        while (true) {
            logger.info("### node-" + node.parameters.selfGossipAddress.pubIP+":"
                    +node.parameters.selfGossipAddress.rpcPort + " STARTING ROUND " + (num++));
            try {
                String data = algo.normalFunction("");
                logger.info("### CONSENSUS RESULT: \n" + data);
                if (!StringUtils.isEmpty(data)) {
                    // 反转共识区块
                    Block consensusBlock = this.convertBlock(data);
                    if (null != consensusBlock) {
                        logger.info("============================ " + Duration.between(first, Instant.now()).toMillis());
                        // 共识区块入库
                        saveNewConsensusBlock(consensusBlock);
                        // 彻底清空缓存
                        algo.wholeCleanUp();
                        num = 0;
                        // 更新最新区块
                        this.node.setCurrBlock(consensusBlock);
                        // 更新共识中区块
                        this.node.setPendingBlock(null);
                    } else {
                        logger.warn("consensus Block is null.");
                    }
                } else {
                    logger.warn("consensus data is null.");
                }
                if (false) {
                    throw new ConnectException();
                    // java complained that ConnectException would never be raised in here, BUT IT IS!
                    // This sh*t is why java is so ugly.
                }
            } catch (Exception e) {
                logger.error("### Round Abort ###" + e.getMessage());
            } finally {
                logger.info("Main.class invoke clean up...");
                algo.cleanUp();
            }
        }
    }

    /**
     * 共识数据反转成区块
     * @param data 共识数据
     * @return 区块
     */
    private Block convertBlock(String data) {
        try {
            return JSON.parseObject(data, Block.class);
        } catch (Exception e) {
            logger.error(e);
            return null;
        }
    }

    /**
     * 判断并封装待共识的节点
     * @param n 节点
     * @return 待共识节点
     */
    private BaseNode handleWaiConsensusNode(BaseNode n) {
        switch (n.getStatus()) {
            case NodeStatus.WAIT_CONSENSUS:
                n.setStatus(NodeStatus.IN_CONSENSUS);
                break;
            case NodeStatus.WAIT_DELETE_HAS_CONSENSUSED:
                n.setStatus(NodeStatus.DELETTING_HAS_CONSENSUSED);
                break;
            case NodeStatus.WAIT_DELETE_IN_CONSENSUS:
                n.setStatus(NodeStatus.DELETTING_IN_CONSENSUS);
                break;
            case NodeStatus.WAIT_UPDATE_HAS_CONSENSUSED:
                n.setStatus(NodeStatus.UPDATTING_HAS_CONSENSUSED);
                break;
            case NodeStatus.WAIT_UPDATE_IN_CONSENSUS:
                n.setStatus(NodeStatus.UPDATTING_IN_CONSENSUS);
                break;
            default: n = null; break;
        }
        return n;
    }

    /**
     * 新建一个分片区块
     * @return 新区块
     */
    private Block createBlock() throws InveException {
        while (this.node.getFullNodeList().values().stream().filter(BaseNode::checkIfConsensusNode).count() < 4 ) {
            logger.warn("Insufficient full nodes...");
            try {
                sleep(3000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        while (null!=this.node.getPendingBlock()) {
            logger.warn("exist a pending block...");
            try {
                sleep(3000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        // get last block
        long shardIndex = this.node.getLocalFullNodeList().values().stream()
                .map(LocalFullNode::getShard).filter(Objects::nonNull).distinct().count();
        Block preBlock = this.node.getCurrBlock();
        Hash preHash = (null == preBlock || null == preBlock.getHash()) ? new Hash("-1".getBytes()) : preBlock.getHash();
        long index = (null == preBlock || 0 > preBlock.getIndex()) ? 0 : preBlock.getIndex() + 1;

        // full node
        long size = this.node.getFullNodeList().values().stream().filter(BaseNode::checkIfWaiConsensusNode).count();
        logger.info("full node wait consensus size: " + size);
        // relay node
        size += this.node.getRelayNodeList().values().stream().filter(BaseNode::checkIfWaiConsensusNode).count();
        logger.info("full & relay node wait consensus size:  " + size);
        // local full node
        long consShardNodeSize = this.node.getLocalFullNodeList().values().stream().filter(BaseNode::checkIfConsensusNode).count();
        if (!node.parameters.staticSharding || consShardNodeSize<=0) {
            long size1 = this.node.getLocalFullNodeList().values().stream()
                    .filter(n -> n.getStatus()==NodeStatus.WAIT_CONSENSUS)
                    .limit(node.parameters.shardNodeSize).count();
            size += size1;
            logger.info("full & relay & local full node wait consensus size:  " + size);
        }

        // 如果存在需要共识的信息，则生成新的block
        if (size>0) {
            // get data and construct
            // full node
            List<BaseNode> waitHandleNodes = new ArrayList<>();
            for (BaseNode n : this.node.getFullNodeList().values()) {
                n = handleWaiConsensusNode(n);
                if (null!=n) {
                    waitHandleNodes.add(n);
                }
            }
            logger.info("create block...0 node size: " + waitHandleNodes.size());
            // relay node
            for (BaseNode n : this.node.getRelayNodeList().values()) {
                n = handleWaiConsensusNode(n);
                if (null!=n) {
                    waitHandleNodes.add(n);
                }
            }
            logger.info("create block...1 node size: " + waitHandleNodes.size());
            // local full node
            long count = (node.parameters.staticSharding)
                    ? node.parameters.shardNodeSize*node.parameters.shardSize : node.parameters.shardNodeSize;
            if (!node.parameters.staticSharding || (consShardNodeSize<=0)) {
                List<LocalFullNode> waitConsensusNodes = this.node.getLocalFullNodeList().values().stream()
                        .filter(n -> n.getStatus()==NodeStatus.WAIT_CONSENSUS)
                        .limit(count)
                        .collect(Collectors.toList());
                if (waitConsensusNodes.size()==count) {
                    int i=0;
                    for (LocalFullNode localFullNode : waitConsensusNodes) {
                        localFullNode.setShard(""+shardIndex);
                        localFullNode.setIndex(""+(i++));
                        localFullNode.setStatus(NodeStatus.IN_CONSENSUS);
                        if (i==node.parameters.shardNodeSize) {
                            shardIndex++;
                            i=0;
                        }
                    }
                    waitHandleNodes.addAll(waitConsensusNodes);
                }
            }
            logger.info("create block...2 node size: " + waitHandleNodes.size());
            // new block
            long timestamp = Instant.now().toEpochMilli();
            byte[] hash = Hash.hash(preHash.getHash(), index, timestamp, JSONArray.toJSONString(waitHandleNodes).getBytes());
            Block block = new Block.Builder().preHash(preHash)
                    .timestamp(timestamp).index(index)
                    .data(JSONArray.toJSONString(waitHandleNodes).getBytes())
                    .hash(new Hash(hash)).build();
            block.setSignature(Crypto.sign(hash, this.node.privateKey));
            printBlock(block);
            return block;
        } else {
            try {
                sleep(500);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            return createBlock();
        }
    }

    /**
     * 保存新区块
     * @param consensusBlock 新区块
     */
    private void saveNewConsensusBlock(Block consensusBlock) {
        final String hash = DSA.encryptBASE64(consensusBlock.getHash().getHash());
        // 保存区块
        SqliteDAO.addBlock(this.node.parameters.dbFile, consensusBlock);
        // 保存共识信息
        JSONArray objects = JSONArray.parseArray(new String(consensusBlock.getData()));
        objects.forEach(object -> {
            BaseNode baseNode = BaseNode.nodeConvert((JSONObject) object);
            logger.info("consensus-" + consensusBlock.getIndex() + ": " + JSON.toJSONString(baseNode));
            // full node
            if (baseNode instanceof FullNode){
                FullNode n = (FullNode) baseNode;
                switch (n.getStatus()) {
                    case NodeStatus.DELETTING_HAS_CONSENSUSED:
                    case NodeStatus.DELETTING_IN_CONSENSUS:
                        SqliteDAO.deleteFullNode(this.node.parameters.dbFile, n.getPubkey());
                        this.node.getFullNodeList().remove(n.getPubkey());
                        break;
                    case NodeStatus.UPDATTING_HAS_CONSENSUSED:
                    case NodeStatus.UPDATTING_IN_CONSENSUS:
                    case NodeStatus.IN_CONSENSUS:
                    default:
                        n.setStatus(NodeStatus.HAS_CONSENSUSED);
                        n.setHash(hash);
                        SqliteDAO.saveFullNode(this.node.parameters.dbFile, n);
                        this.node.getFullNodeList().put(n.getPubkey(), n);
                        break;
                }
            }
            // relay node
            if (baseNode instanceof RelayNode){
                RelayNode n = (RelayNode) baseNode;
                switch (n.getStatus()) {
                    case NodeStatus.DELETTING_HAS_CONSENSUSED:
                    case NodeStatus.DELETTING_IN_CONSENSUS:
                        SqliteDAO.deleteRelayNode(this.node.parameters.dbFile, n.getPubkey());
                        this.node.getRelayNodeList().remove(n.getPubkey());
                        break;
                    case NodeStatus.UPDATTING_HAS_CONSENSUSED:
                    case NodeStatus.UPDATTING_IN_CONSENSUS:
                    case NodeStatus.IN_CONSENSUS:
                    default:
                        n.setStatus(NodeStatus.HAS_CONSENSUSED);
                        n.setHash(hash);
                        SqliteDAO.saveRelayNode(this.node.parameters.dbFile, n);
                        this.node.getRelayNodeList().put(n.getPubkey(), n);
                        break;
                }
            }
            // local full node
            if (baseNode instanceof LocalFullNode){
                LocalFullNode n = (LocalFullNode)baseNode;
                n.setStatus(NodeStatus.HAS_SHARDED);
                n.setHash(hash);
                SqliteDAO.saveLocalfullnode(this.node.parameters.dbFile, n);
                this.node.getLocalFullNodeList().put(n.getPubkey(), n);
            }
        });
    }

    private void printBlock(Block block) {
        System.out.println("preHash: " + new String(block.getPreHash().getHash()));
        System.out.println("hash: " + new String(block.getHash().getHash()));
        System.out.println("signature: " + new String(block.getSignature()));
        System.out.println("timestamp: " + block.getTimestamp());
        System.out.println("index: " + block.getIndex());
        System.out.println("data: " + new String(block.getData()));

        System.out.println("verify sign: "
                + Crypto.verifySignature(block.getHash().getHash(), block.getSignature(), this.node.publicKey));
    }

    public static void main(String[] args) throws InveException {
        String data = "{\"data\":\"W3siZW1haWwiOiI0NDRAcXEuY29tIiwiZXhjaGFuZ2VSYXRpb3MiOnsiSU5WRTpFVEgiOjUuNEUtNSwiQlRDOkVUSCI6MzguMzg5MzMsIklOVkU6QlRDIjoxLjBFLTZ9LCJmZWUiOjEwLCJoYXNoIjoiRjVoeGszQmdBY1Rtc0hieDVEMGxaNlcrQUJBMEt1dkNxQUkxZUJlSWx1bGJWeEZSbllnS25ROTAvR1h5ekNoWW1kZHY5MVFTcUNOVi9Lb3JzRGZzMHc9PSIsImh0dHBQb3J0IjoyMiwiaXAiOiIxOTIuMTY4LjAuMSIsImxhc3RBbGl2ZVRpbWVzdGFtcCI6MTU0NDY4MjAzMTIzMSwicGhvbmUiOiIxMjMxMjMxMjMiLCJwdWJrZXkiOiJycnJycnJycnJycnJyciIsInJlZ2lzdGVyVGltZXN0YW1wIjoxNTQ0NjgxOTcwMTE2LCJycGNQb3J0IjowLCJzdGF0dXMiOjIyMiwidHlwZSI6M31d\",\"hash\":{\"hash\":\"sW12dunICBlL/fIXQDfhaBVmXVa6lgyl6Q1eP4VyRfazzqs6u5azcXpYrri7Q7QwWZe/y2eqhfyz0MlmQRRS4g==\"},\"index\":1,\"preHash\":{\"hash\":\"F5hxk3BgAcTmsHbx5D0lZ6W+ABA0KuvCqAI1eBeIlulbVxFRnYgKnQ90/GXyzChYmddv91QSqCNV/KorsDfs0w==\"},\"signature\":\"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\",\"timestamp\":1544682031363}";
        Block consensusBlock = JSON.parseObject(data, Block.class);
        JSONArray objects = JSONArray.parseArray(new String(consensusBlock.getData()));
        objects.forEach(object -> {
            int type = ((JSONObject)object).getInteger("type");
            // relay node
            if (NodeTypes.RELAYNODE == type){
                RelayNode n = JSONObject.toJavaObject((JSONObject)object, RelayNode.class);
                switch (n.getStatus()) {
                    case NodeStatus.DELETTING_HAS_CONSENSUSED:
                    case NodeStatus.DELETTING_IN_CONSENSUS:
                        break;
                    case NodeStatus.UPDATTING_HAS_CONSENSUSED:
                    case NodeStatus.UPDATTING_IN_CONSENSUS:
                    case NodeStatus.IN_CONSENSUS:
                    default:
                        n.setStatus(NodeStatus.HAS_CONSENSUSED);
                        break;
                }
            }
        });

        Crypto crypto = new Crypto();
        KeyPair keyPair = crypto.getKeyPair();
//
//        Seed seed = new Seed();
//        seed.privateKey = keyPair.getPrivateKey();
//        seed.publicKey = keyPair.getPublicKey();

        byte[] preHash = "Hash".getBytes();
        byte[] hash = Hash.hash(preHash, -1, Instant.now().toEpochMilli(), "test".getBytes());
//        System.out.println("\n++++++++++++hash: " + DSA.encryptBASE64(hash));
//
//        byte[] signature = Crypto.sign(hash, keyPair.getPrivateKey());
//        System.out.println("\n++++++++++++signature: " + DSA.encryptBASE64(signature));
//
//        boolean verify = Crypto.verifySignature(hash, signature, keyPair.getPublicKey());
//        System.out.println("\n++++++++++++verify: " + verify);

//
//        Block block0 = new Block.Builder().preHash(new Hash(preHash)).hash(new Hash(hash))
//                .signature(Crypto.sign(hash, seed.privateKey))
//                .timestamp(Instant.now().toEpochMilli()).index(0).data("sdfsdfasdfsdfasdf".getBytes()).build();
//        seed.setCurrBlock(block0);
//
//        PbftThread thread = new PbftThread(seed);
//        thread.printBlock(seed.getCurrBlock());
//        thread.start();
    }
}
