package one.inve.core;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Random;

public class SampleNode implements Runnable {
    private static final Logger logger = LoggerFactory.getLogger(SampleNode.class);

    public int shardId;
    public int selfId;
    public int shardCount;
    public int nValue;
    public PublicKey[][] publicKeys;
    public PrivateKey selfPrivKey;
    public SampleNode[][] nodes;
    public Hashnet hashnet;
    public EventStore eventStore;
    public EventFlow eventFlow;
    public long gossipTimes;

    public SampleNode(int shardId, int selfId, SampleNode[][] nodes, PublicKey[][] pubkeys,
                   PrivateKey privateKey, long gossipTimes) {
        this.selfId = selfId;
        this.shardId = shardId;
        this.nodes = nodes;
        this.shardCount = pubkeys.length;
        this.nValue = pubkeys[0].length;
        this.publicKeys = pubkeys;
        this.selfPrivKey = privateKey;
        this.gossipTimes = gossipTimes;
        this.hashnet = new Hashnet(shardCount, nValue);
        this.eventStore = new EventStoreInMem(shardCount, nValue, selfId);
        this.eventFlow = new EventFlow(this.publicKeys, this.selfPrivKey, eventStore);
    }

    //如果是第一次运行需要创建第一个Event
    public void initFromScratch() {
        for (int i = 0; i < this.shardCount; i++) {
            if (i == shardId) {
                eventFlow.newEvent(i, selfId, -1, null);
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

    // 从对方hashnet中获取自己没有的Event
    // 并逐个添加到自己的graph中
    // 每次添加之前都必须保证被添加的Event的selfParent和
    // otherParent在graph内是存在的
    // 方法是首先将所有获取本地没有的event，然后将event按照
    // generation排序以后，再添加到graph中，以确保每个event
    // 都可以在graph中的获取到selfParent和otherParent
    // 这一点在实际运行中有发送方保证，因为Event中的generation
    // 信息不会发送，因为两边的generation值不一定是相同的
    // 但是需要注意的是，本地序列化保存时，必须将该信息保存
    // 否则从磁盘加载Event时无法按照顺序加载
    //
    // 同时需要注意的是由于一直再删除达成共识的Event，如果有节点比较
    // 慢，则可能无法获取到正确的Event
    public ArrayList<EventBody> getUnknownEvents(int shardId, long[] otherCounts) {
//        logger.warn("getUnknownEvents: requestor shard id : {}", shardId);
        long[] currMyCounts = this.eventStore.getLastSeqsByShardId(shardId);
        ArrayList<EventBody> diffEvents = new ArrayList<EventBody>();
        for (int i = 0; i < currMyCounts.length; ++i) {
            for (long j = otherCounts[i] + 1L; j <= currMyCounts[i]; ++j) {
                EventBody e = this.eventStore.getEventInMem(shardId, (long) i, j);
                if (e != null) {
                    diffEvents.add(e);
                }
            }
        }

        Collections.shuffle(diffEvents);
        diffEvents.sort(Comparator.comparing(EventBody::getGeneration));
        logger.warn("getUnknownEvents return shard-{} event size: {}", shardId, diffEvents.size());
        return diffEvents;
    }

    @Override
    public void run() {
        logger.info("node-({},{}): GossipEventThread start...", this.shardId, this.selfId);
        GossipEventThread[] threads = new GossipEventThread[this.shardCount];
        for (int i = 0; i < this.shardCount; i++) {
            threads[i] = new GossipEventThread(this, i);
        }
        for (int i = 0; i < this.shardCount; i++) {
            threads[i].start();
        }
//        for (int i = 0; i < this.shardCount; i++) {
//            try {
//                threads[i].join();
//            } catch (InterruptedException e) {
//                e.printStackTrace();
//            }
//        }
        logger.info("node-({},{}): GossipEventThread stop", this.shardId, this.selfId);
    }

    public Event[] getAllEvents() {
        return this.hashnet.getAllEvents(shardId);
    }

    public Event[] getConsensusEvents(int shardId) {
        return this.hashnet.getAllConsEvents(shardId);
    }
}

class GossipEventThread extends Thread {
    private static final Logger logger = LoggerFactory.getLogger(SampleNode.class);
    SampleNode sampleNode;
    int shardId;

    public GossipEventThread(SampleNode sampleNode, int shardId) {
        this.sampleNode = sampleNode;
        this.shardId = shardId;
    }

    @Override
    public void run() {
        logger.info("node-({},{}): GossipEventThread-{} start...", sampleNode.shardId, sampleNode.selfId, this.shardId);
        long times = sampleNode.gossipTimes;
        for (int i = 0; i < times; ++i) {
            SyncWithAnNeighbor(this.shardId);
            try {
                Thread.sleep(5);
            } catch (Exception e) {
                logger.error("", e);
            }
        }
        logger.info("node-({},{}): GossipEventThread-{} stop...", sampleNode.shardId, sampleNode.selfId, this.shardId);
    }

    private void SyncWithAnNeighbor(int shardId) {
        //获取一个随机邻居
        int neighborIdx;
        Random rand = new Random();
        if (shardId == this.shardId) {
            do {
                neighborIdx = rand.nextInt(sampleNode.nodes[shardId].length);
            } while (neighborIdx == sampleNode.selfId);
        } else {
            neighborIdx = rand.nextInt(sampleNode.nodes[shardId].length);
        }
        logger.info("node-({},{}) sync events with an neighbor node-({},{})",
                 sampleNode.shardId, sampleNode.selfId, shardId, neighborIdx);

        SampleNode otherNode = sampleNode.nodes[shardId][neighborIdx];
        //获取对方graph中本地未知的Event，并逐一添加到本地graph中
        ArrayList<EventBody> unknownEvents =
                otherNode.getUnknownEvents(shardId, sampleNode.eventStore.getLastSeqsByShardId(shardId));

        for (EventBody evt : unknownEvents) {
            sampleNode.eventFlow.addEvent(evt);
        }

        sampleNode.addToHashnet(shardId);

        //建立一个指向对方的Event，并添加到graph中
        if (shardId == this.shardId) {
            logger.info("node-({},{}) create an new event...",
                    sampleNode.shardId, sampleNode.selfId, shardId, neighborIdx);
            sampleNode.eventFlow.newEvent(sampleNode.shardId, sampleNode.selfId, neighborIdx, null);
        }
    }
}
