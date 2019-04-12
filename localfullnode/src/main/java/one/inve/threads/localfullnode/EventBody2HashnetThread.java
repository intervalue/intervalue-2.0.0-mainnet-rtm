package one.inve.threads.localfullnode;

import one.inve.node.Main;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.time.Instant;

/**
 * @author Clare
 * @date   2018/6/25 0025.
 */
public class EventBody2HashnetThread extends Thread {
    private static final Logger logger = LoggerFactory.getLogger(EventBody2HashnetThread.class);

    private Main node;

    public EventBody2HashnetThread(Main node) {
        this.node = node;
    }

    @Override
    public void run() {
        logger.info(">>>>>> start EventBody2HashnetThread...");
        Instant t0;
        while (true) {
            t0 = Instant.now();
            for(int i = 0;i<node.getShardCount();i++) {
            	node.addToHashnet(i);
            }
            if (logger.isDebugEnabled()) {
                long interval = Duration.between(t0, Instant.now()).toMillis();
                if (interval > 150) {
                    logger.debug("===^^^=== node-({}, {}): GetConsensusEventsThread handle cost: {} sec",
                            node.getShardId(), node.getCreatorId(), interval / 1000.0);
                }
            }
        }
    }

}
