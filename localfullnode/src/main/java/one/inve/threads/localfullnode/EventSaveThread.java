package one.inve.threads.localfullnode;

import com.alibaba.fastjson.JSONObject;
import one.inve.core.Config;
import one.inve.core.EventBody;
import one.inve.core.EventKeyPair;
import one.inve.node.Main;
import one.inve.rocksDB.RocksJavaUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigInteger;
import java.time.Duration;
import java.time.Instant;
import java.util.Objects;


/**
 * Event入库线程
 * @author Clare
 * @date   2018/7/30 0030.
 */
public class EventSaveThread extends Thread {
    private static final Logger logger = LoggerFactory.getLogger(ConsensusMessageVerifyThread.class);

    private Main node;
    public EventSaveThread(Main node) {
        this.node = node;
    }

    @Override
    public void run() {
        logger.info(">>> start EventSaveThread...");

        StringBuilder statisticInfo = new StringBuilder()
                .append("\n===== node-({}, {}): event save thread =====")
                .append("\ncost: {} ms\ntotal event count: {}")
                .append("\nEventSaveQueue size: {}");
        try {
            int i = 0;
            Instant t0 = Instant.now();
            while (true) {
                if ( !node.getEventSaveQueue().isEmpty() ) {
                    saveEvent(Objects.requireNonNull(node.getEventSaveQueue().poll()));
                    i++;
                }

                if (i == Config.DEFAULT_EVENT_STATISTICS_COUNT) {
                    logger.info(statisticInfo.toString(), node.getShardId(), node.getCreatorId(),
                            Duration.between(t0, Instant.now()).toMillis(),
                            node.getTotalEventCount(),
                            node.getEventSaveQueue().size());
                    i = 0;
                    t0 = Instant.now();
                }
            }
        } catch (Exception e) {
            logger.error("EventSaveThread error: {}\nexit...", e);
            System.exit(-1);
        }
    }

    /**
     * 保存新Event
     * @param eb Event内容
     */
    private void saveEvent(EventBody eb) {
        EventKeyPair pair = new EventKeyPair(eb.getShardId(), eb.getCreatorId(), eb.getCreatorSeq());
        try {
            RocksJavaUtil rocksJavaUtil = new RocksJavaUtil(node.nodeParameters.dbId);
            // 保存event(shardId,creatorId,creatorSeq作为key)
            rocksJavaUtil.put(pair.toString(), JSONObject.toJSONString(eb));

            // 保存每个节点的lastSeq
            rocksJavaUtil.put(eb.getShardId()+"_"+eb.getCreatorId(), ""+eb.getCreatorSeq());

            // 保存event key关系(shardId,otherId,otherSeq作为key，查询Event的真正的key)
            EventKeyPair otherPair = new EventKeyPair(eb.getShardId(), eb.getOtherId(), eb.getOtherSeq());
            String key = "o_"+otherPair.toString();
            byte[] data = rocksJavaUtil.get(key);
            if (null==data) {
                rocksJavaUtil.put(key, pair.toString() );
            } else {
                StringBuilder sb = new StringBuilder();
                String value = new String(data);
                if (!value.contains(pair.toString())) {
                    rocksJavaUtil.put(key, sb.append(value).append(",").append(pair.toString()).toString());
                } else {
                    logger.warn("o_pair value exist! key: {}, value: {}", key, pair.toString());
                }
            }

            // 保存总的Event数量
            node.setTotalEventCount(node.getTotalEventCount().add(BigInteger.ONE));
            rocksJavaUtil.put(Config.EVT_COUNT_KEY, node.getTotalEventCount().toString());
        } catch (Exception e) {
            logger.error("saveEvent {} error: {}", pair.toString(), e);
            saveEvent(eb);
        }
    }

}
