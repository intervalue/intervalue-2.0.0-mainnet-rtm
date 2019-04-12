package one.inve.threads;

import com.alibaba.fastjson.JSON;
import one.inve.bean.node.BaseNode;
import one.inve.bean.node.NodeStatus;
import one.inve.bean.node.NodeTypes;
import one.inve.cluster.Cluster;
import one.inve.cluster.ClusterConfig;
import one.inve.cluster.Member;
import one.inve.node.Main;
import one.inve.util.HnKeyUtils;
import one.inve.util.StringUtils;
import org.apache.log4j.Logger;

import java.time.Duration;
import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

/**
 * fullNode gossip网络维护线程
 * Created by Clare  on 2018/9/6 0006.
 */
public class GossipNodeThread extends Thread {
    public final static Logger logger = Logger.getLogger("GossipNodeThread.class");
    private Main node;
    public static ArrayList<Member> FullNodeNeighborPools = new ArrayList<>();
    public static ArrayList<Member> LocalFullNodeNeighborPools = new ArrayList<>();

    public GossipNodeThread(Main node) {
        this.node = node;
    }

    @Override
    public void run() {
        logger.info("start gossip network...");
        ClusterConfig NodeConfig = ClusterConfig.builder()
                .seedMembers(
                        one.inve.transport.Address.create(
                            this.node.parameters.seedGossipAddress.pubIP,
                            this.node.parameters.seedGossipAddress.gossipPort)
                )
                .memberHost(this.node.parameters.selfGossipAddress.pubIP)
                .port(this.node.parameters.selfGossipAddress.gossipPort)
                .portAutoIncrement(false)
                .addMetadata("level", ""+NodeTypes.FULLNODE)
                .addMetadata("rpcPort", "" +this.node.parameters.selfGossipAddress.rpcPort)
                .addMetadata("httpPort", "" +this.node.parameters.selfGossipAddress.httpPort)
                .addMetadata("pubkey", HnKeyUtils.getString4PublicKey(this.node.publicKey))
                .build();
        Cluster cluster = Cluster.joinAwait(NodeConfig);
        logger.info("full node has started!");

        while (true) {
            Instant first = Instant.now();
            logger.warn("node size: " + cluster.members().size());
            this.updateFullNodeNeighborPools(cluster.members());
            logger.warn("node otherMembers size: " + cluster.otherMembers().size());
            this.updateLocalFullNodeNeighborPools(cluster.otherMembers());
            long interval = Duration.between(first, Instant.now()).toMillis();
            if (interval < node.parameters.gossipInterval) {
                try {
                    sleep(node.parameters.gossipInterval-interval);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * 更新全节点邻居列表
     * @param list 全节点邻居列表
     */
    private void updateFullNodeNeighborPools(Collection<Member> list) {
//        node.getFullNodeList().values().forEach(n -> {
//            logger.warn("> fullnode ip: " + n.getIp()
//                    + ", port: " + n.getRpcPort()
//                    + ", type: " + n.getType()
//                    + ", status: " + n.getStatus());
//        });
//        list.forEach(m -> {
//            logger.warn(this.node.parameters.selfGossipAddress.gossipPort
//                    + "= member ip: " + m.address().host()
//                    + ", port: " + m.metadata().get("rpcPort")
//                    + ", level: " + m.metadata().getOrDefault("level", "-1"));
//        });
        FullNodeNeighborPools = list.stream()
                .filter(member -> node.parameters.whiteList.contains(member.address().host())
                        && !node.parameters.blackList.contains(member.address().host())
                        && checkFullNodePubkey(""+member.metadata().get("pubkey"))
                        && (NodeTypes.FULLNODE==Integer.parseInt(member.metadata().getOrDefault("level", "-1"))
                            || NodeTypes.SEED==Integer.parseInt(member.metadata().getOrDefault("level", "-1"))) )
                .collect( Collectors.collectingAndThen(
                        Collectors.toCollection(() -> new TreeSet<>(Comparator.comparing(o ->
                                o.address().host()
                                        +"_"+o.address().port()
                                        +"_"+o.metadata().get("rpcPort")
                                        +"_"+o.metadata().get("httpPort")))),
                        ArrayList::new));
        Optional<Member> optional = FullNodeNeighborPools.stream()
                .filter(member -> "0".equals(""+member.metadata().get("level"))).findFirst();
        optional.ifPresent(m -> this.node.setSeedPubkey(""+m.metadata().get("pubkey")));

        logger.info("============== "+ this.node.parameters.selfGossipAddress.gossipPort
				+ " FullNodeNeighborPools size: " + FullNodeNeighborPools.size());
    }

    /**
     * 更新局部全节点邻居列表
     * @param list 局部全节点邻居列表
     */
    private void updateLocalFullNodeNeighborPools(Collection<Member> list) {
        LocalFullNodeNeighborPools = list.stream()
                .filter(member -> node.parameters.whiteList.contains(member.address().host())
                        && !node.parameters.blackList.contains(member.address().host())
                        && checkLocalFullNodePubkey(""+member.metadata().get("pubkey"))
                        && NodeTypes.LOCALFULLNODE==Integer.parseInt(member.metadata().getOrDefault("level", "-1")) )
                .collect( Collectors.collectingAndThen(
                        Collectors.toCollection(() -> new TreeSet<>(Comparator.comparing(o ->
                                o.address().host()
                                        +"_"+o.address().port()
                                        +"_"+o.metadata().get("rpcPort")
                                        +"_"+o.metadata().get("httpPort")))),
                        ArrayList::new));

        logger.info("============== "+ this.node.parameters.selfGossipAddress.gossipPort
                + " LocalFullNodeNeighborPools size: " + LocalFullNodeNeighborPools.size());
        LocalFullNodeNeighborPools.forEach(member -> {
            logger.info(member.address().host() + ":" + member.address().port()
                    + ", rpcPort=" + member.metadata().get("rpcPort")
                    + ", httpPort=" + member.metadata().get("httpPort")
                    + ", shardId=" + member.metadata().get("shard")
                    + ", index=" + member.metadata().get("index"));
        });
    }

    /**
     * 校验公钥合法性
     * @param pubkey 公钥
     * @return 是否合法
     */
    private boolean checkFullNodePubkey(String pubkey) {
        if (StringUtils.isEmpty(pubkey)) {
            logger.error("pubkey is null.");
            return false;
        }
        return node.getFullNodeList().values().parallelStream()
                .filter(BaseNode::checkIfConsensusNode)
                .anyMatch(p->p.getPubkey().equals(pubkey));
    }

    /**
     * 校验公钥合法性
     * @param pubkey 公钥
     * @return 是否合法
     */
    private boolean checkLocalFullNodePubkey(String pubkey) {
        if (StringUtils.isEmpty(pubkey)) {
            logger.error("pubkey is null.");
            return false;
        }
        return node.getLocalFullNodeList().values().parallelStream()
                .filter(n -> n.getStatus()==NodeStatus.HAS_SHARDED)
                .anyMatch(p->p.getPubkey().equals(pubkey));
    }

}
