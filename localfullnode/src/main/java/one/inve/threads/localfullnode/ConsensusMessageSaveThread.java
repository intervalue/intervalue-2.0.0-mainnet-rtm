package one.inve.threads.localfullnode;

import com.alibaba.fastjson.JSONObject;
import one.inve.beans.dao.Transaction;
import one.inve.core.Config;
import one.inve.db.transaction.NewTableCreate;
import one.inve.node.Main;
import one.inve.rocksDB.RocksJavaUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

public class ConsensusMessageSaveThread extends Thread {
    private static final Logger logger = LoggerFactory.getLogger(ConsensusMessageSaveThread.class);

    private Main node;

    public ConsensusMessageSaveThread(Main node) {
        this.node = node;
    }

    StringBuilder statisticInfo = new StringBuilder()
            .append("\n===== node-({}, {}): Consensus message save thread =====")
            .append("\ntotal cost: {} ms\nmessage count: {}\navg cost: {} ms/t")
            .append("\nConsMessageHandleQueue size: {}\nConsMessageSaveQueue size: {}");

    @Override
    public void run() {
        logger.info(">>> start ConsensusMessageSaveThread...");

        Instant t0 = null;
        Instant t1 = null;
        NewTableCreate table;
        long messageCount = 0L;
        List<JSONObject> list = new ArrayList<>();
        while (true) {
            t0 = Instant.now();
            t1 = Instant.now();

            // 时间间隔和交易数量2个维度来控制交易的入库
            while (Duration.between(t0, t1).toMillis() < Config.TXS_COMMIT_TIMEOUT) {
                // 取共识事件
                for (int i=0; i<200; i++) {
                    if (!node.getConsMessageSaveQueue().isEmpty()) {
                        list.add(node.getConsMessageSaveQueue().poll());
                    } else {
                        break;
                    }
                }
                if (list.size() >= Config.MAX_TXS_COMMIT_COUNT) {
                    break;
                }
                t1 = Instant.now();
            }
            messageCount = list.size();
            if (messageCount > 0) {
                // 交易入库
                table = new NewTableCreate();
                BigInteger maxMsgId = table.addMessages(list, node.nodeParameters.dbId);
                list.clear();
                // 更新入库共识消息总数
                if (maxMsgId.compareTo(BigInteger.ZERO) > 0) {
                    node.setConsMessageCount(maxMsgId);
                    RocksJavaUtil rocksJavaUtil = new RocksJavaUtil(node.nodeParameters.dbId);
                    rocksJavaUtil.put(Config.CONS_MSG_COUNT_KEY, node.getConsMessageCount().toString());

                    // 打印日志
                    long interval = Duration.between(t0, t1).toMillis();
                    logger.info(statisticInfo.toString(), node.getShardId(), node.getCreatorId(),
                            interval, messageCount,
                            new BigDecimal(interval).divide(BigDecimal.valueOf(messageCount), 2, BigDecimal.ROUND_HALF_UP),
                            node.getConsMessageHandleQueue().size(),
                            node.getConsMessageSaveQueue().size());
                } else {
                    logger.error("node-({}, {}): update consMessageCount failed after save messages!!!",
                            node.getShardId(), node.getCreatorId());
                }
            }
        }
    }
}
