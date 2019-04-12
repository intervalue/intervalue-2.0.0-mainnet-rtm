package one.inve.threads.localfullnode;

import one.inve.core.Config;
import one.inve.core.EventBody;
import one.inve.node.Main;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import one.inve.core.Event;

import java.time.Duration;
import java.time.Instant;

/**
 * 批量处理 分片 插入数据
 * @author allen
 *
 */
public class GetConsensusEventsThread extends Thread {
    private static final Logger logger = LoggerFactory.getLogger(GetConsensusEventsThread.class);

    private Main node;
    
    public GetConsensusEventsThread(Main node) {
        this.node = node;
    }

    @Override
    public void run() {
        logger.info(">>>>>> start GetConsensusEventsThread...");
        Instant t0;
        Instant t1;
        Event[] evts;
        int[] count = new int[node.getShardCount()];

        while (true) {
            t0 = Instant.now();
            t1 = Instant.now();
            if (node.getShardCount() > count.length) {
                count = new int[node.getShardCount()];
            }

            while (Duration.between(t0, t1).toMillis() < Config.GET_CONSENSUS_TIMEOUT) {
                for (int shardId = 0; shardId < node.getShardCount(); shardId++) {
                    // 将收到的未知events加入到全排序待入库队列
                    evts = node.getHashnet().getAllConsEvents(shardId);
                    for (Event evt : evts) {
                        try {
                            node.getShardSortQueue(shardId).put(new EventBody.Builder()
                                    .shardId(shardId)
                                    .creatorId(evt.getCreatorId())
                                    .creatorSeq(evt.getCreatorSeq())
                                    .otherId(evt.getOtherId())
                                    .otherSeq(evt.getOtherSeq())
                                    .timeCreated(evt.getTimeCreated())
                                    .trans(evt.getTransactions())
                                    .signature(evt.getSignature())
                                    .isFamous(evt.isFamous())
                                    .generation(evt.getGeneration())
                                    .hash(evt.getHash())
                                    .consTimestamp(evt.getConsensusTimestamp())
                                    .build() );
                        } catch (InterruptedException e) {
                            logger.error(">>>>>> ERROR: {}", e);
                            e.printStackTrace();
                        }
                    }
                    if (evts.length > 0) {
                        count[shardId] += evts.length;
                    }
                }
                t1 = Instant.now();
                if (logger.isDebugEnabled()) {
                    long interval = Duration.between(t0, t1).toMillis();
                    if (interval > 350) {
                        logger.debug("===^^^=== node-({}, {}): GetConsensusEventsThread handle cost: {} sec",
                                node.getShardId(), node.getCreatorId(), interval / 1000.0);
                    }
                }
            }
        }
    }
}
