package one.inve.core;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.security.KeyPair;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CyclicBarrier;

public class AlgoTest {
    private static final Logger logger = LoggerFactory.getLogger(AlgoTest.class);

    public static void main(String[] args) throws InterruptedException {
        int shardCount = 1;
        int nValue = 4;
        int gossipTimes = 10000;
        ConcurrentHashMap<Integer, CyclicBarrier> barriers = new ConcurrentHashMap<>();

        PublicKey[][] pubKeys = new PublicKey[shardCount][nValue];
        PrivateKey[][] privates = new PrivateKey[shardCount][nValue];
        for (int i = 0; i < shardCount; i++) {
            barriers.put(i, new CyclicBarrier(nValue));

            for (int j = 0; j < nValue; j++) {
                Cryptos cryptos = new Cryptos();
                KeyPair keyPair = null;
                try {
                    keyPair = cryptos.getKeyPair();
                } catch (NoSuchAlgorithmException e) {
                    e.printStackTrace();
                }
                pubKeys[i][j] = keyPair.getPublic();
                privates[i][j] = keyPair.getPrivate();
            }
        }
        SampleNode[][] nodes = new SampleNode[shardCount][nValue];
        for (int i = 0; i < shardCount; i++) {
            for (int j = 0; j < nValue; j++) {
                nodes[i][j] = new SampleNode(i, j, nodes, pubKeys, privates[i][j], gossipTimes);
                nodes[i][j].initFromScratch();
            }
        }

        for (int i = 0; i < shardCount; i++) {
            for (int j = 0; j < nValue; j++) {
                nodes[i][j].run();
            }
        }

        Thread.sleep(2 * 60 * 1000);

        Event[][][][] allConsEvents = new Event[shardCount][nValue][shardCount][];
        int minSize = Integer.MAX_VALUE;
        for (int i = 0; i < shardCount; i++) {
            for (int j = 0; j < nValue; j++) {
                for (int k = 0; k < shardCount; k++) {
                    allConsEvents[i][j][k] = nodes[i][j].getConsensusEvents(k);
                    logger.info("node-({},{}):  Shard-{} ConsEvents Number:  {}", i, j, k, allConsEvents[i][j][k].length);
                    if (allConsEvents[i][j][k].length < minSize) {
                        minSize = allConsEvents[i][j].length;
                    }
                }
            }
        }

//        for (int i = 0; i < shardCount; i++) {
//            for (int j = 0; j < nValue; j++) {
//                for (int k = 0; k < shardCount; k++) {
//                    StringBuilder sb = new StringBuilder();
//                    for (int l = 0; l < 20; l++) {
//                        sb.append("(")
//                                .append(allConsEvents[i][i][k][l].getShardId()).append(",")
//                                .append(allConsEvents[i][j][k][l].getCreatorId()).append(",")
//                                .append(allConsEvents[i][j][k][l].getCreatorSeq())
//                                .append("), ");
//                    }
//                    logger.info("node-({}, {})-shard-{}: \n{}", i, j, k, sb.toString() );
//                }
//            }
//        }

        // 以第一个分片的第一个节点标准，对比所有节点的所有Event是否共识顺序一致
        logger.info("strat to compare...");
        boolean flag = true;
        for (int j = 0; j < shardCount; j++) {
            for (int k = 0; k < nValue; k++) {
                for (int l = 0; l < shardCount ; l++) {
                    for (int i = 0; i < minSize; ++i) {
                        long shardId = allConsEvents[0][0][l][i].getShardId();
                        long creatorId = allConsEvents[0][0][l][i].getCreatorId();
                        long creatorSeq = allConsEvents[0][0][l][i].getCreatorSeq();

                        long sid = allConsEvents[j][k][l][i].getShardId();
                        long cid = allConsEvents[j][k][l][i].getCreatorId();
                        long cseq = allConsEvents[j][k][l][i].getCreatorSeq();
                        if (shardId != sid ||creatorId != cid || creatorSeq != cseq) {
                            logger.error("(shardId, id)-({},{}): event cons error (shardId,creatorId,creatorSeq): ({},{},{}), ({},{},{})",
                                    l, i, shardId, creatorId, creatorSeq, sid, cid, cseq);
                            flag = false;
                            break;
                        } else {
                            logger.info("(shardId, id)-({},{}) consensus.", l, i);
                        }
                    }
                }
            }
        }
        if (flag) {
            logger.info("all nodes' events consensus!!!");
        }

    }

}
