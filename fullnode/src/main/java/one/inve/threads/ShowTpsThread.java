package one.inve.threads;

import one.inve.bean.node.LocalFullNode;
import one.inve.bean.node.NodeStatus;
import one.inve.cluster.Member;
import one.inve.core.Config;
import one.inve.node.Main;
import one.inve.util.ExcelUtils;
import one.inve.util.StringUtils;
import org.apache.log4j.Logger;

import java.io.File;
import java.util.Collection;
import java.util.stream.Collectors;

/**
 * 统计及记录tps线程
 * Created by Clare  on 2018/7/2 0002.
 */
public class ShowTpsThread extends Thread {
    public final static Logger logger = Logger.getLogger("ShowTpsThread.class");
    private Main node;

    //单批次每个节点
    long[][] nodeTransactions = null;
    long[][] nodeEvents = null;
    long[][] nodeTimes = null;

    //单批次每个分片
    long[] shardTransactions = null;
    long[] shardEvents = null;
    long[] shardTimes = null;

    // 单批次每个分片节点数
    int[] shardLocalCount = null;

    // 批次号
    int index=0;

    // 每个分片所有批次总和
    double[] shardTransactionsOfAllBatches = null;
    double[] shardEventsOfAllBatches = null;

    // 所有批次的分片统计数据
    double[] shardAvgTransactions = null;
    double[] shardAvgEvents = null;
    double[] shardAvgTimes = null;
    double[] shardAvgTps = null;
    double[] shardAvgEps = null;

    // 所有批次的总统计数据
    double[] avgTotalTransactionsSeries = null;
    double[] avgTotalEventsSeries = null;
    double[] avgTotalTimesSeries = null;
    double[] tpsPlusSeries = null;
    double[] tpsAvgSeries = null;
    double[] epsPlusSeries = null;
    double[] epsAvgSeries = null;

    public ShowTpsThread(Main node) {
        this.node = node;
    }

    @Override
    public void run() {

        int interval = Config.DEFAULT_STATISTICS_INTERVAL;
        int batches = Config.DEFAULT_STATISTICS_BATCHES;
        long count = node.getLocalFullNodeList().values().stream().filter(n -> n.getStatus()== NodeStatus.HAS_SHARDED)
                .map(LocalFullNode::getShard).distinct().count();
        while (count==0) {
            try {
                Thread.sleep(interval);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            count = node.getLocalFullNodeList().values().stream().filter(n -> n.getStatus()== NodeStatus.HAS_SHARDED)
                    .map(LocalFullNode::getShard).distinct().count();;
        }
        int shardNodeCount = node.parameters.shardNodeSize;

        int shardCount = (int) count;
        shardLocalCount = new int[shardCount];
        shardTransactionsOfAllBatches = new double[shardCount];
        shardEventsOfAllBatches = new double[shardCount];

        avgTotalTimesSeries = new double[batches];
        avgTotalTransactionsSeries = new double[batches];
        avgTotalEventsSeries = new double[batches];
        tpsPlusSeries = new double[batches];
        tpsAvgSeries = new double[batches];
        epsPlusSeries = new double[batches];
        epsAvgSeries = new double[batches];

        shardAvgTimes = new double[shardCount];
        shardAvgTransactions = new double[shardCount];
        shardAvgEvents = new double[shardCount];
        shardAvgTps = new double[shardCount];
        shardAvgEps = new double[shardCount];

        int fileCount = 0;

        while (true) {
            try {
                Thread.sleep(interval);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            showInfo(shardCount, shardNodeCount);
            index++;

            if (index >= batches) {
                // 写文件
                try {
                    writeResult(fileCount);
                } catch (Exception e) {
                    logger.error("write excel exception", e);
                }
                index = 0;
            }
        }
    }

    /**
     * 打印信息：
     *      TPS、EPS、total transactions、total events
     *      Average transactions latency
     *      Average events latency
     */
    private void showInfo(int shardCount, int shardNodeCount) {
        nodeTransactions = new long[shardCount][shardNodeCount];
        nodeEvents = new long[shardCount][shardNodeCount];
        nodeTimes = new long[shardCount][shardNodeCount];

        shardTransactions = new long[shardCount];
        shardEvents = new long[shardCount];
        shardTimes = new long[shardCount];

        int shardId = -1;
        int creatorId = -1;
        int shardExist = 0;

        String transData = "";
        String eventData = "";
        String firstTime = "";
        String latestTime = "";
        String avgLatency = "";

        for (int i=0; i<shardCount; i++) {
            //获取分片i的局部全节点信息
            final int shard = i;
            Collection<Member> shardNodes = GossipNodeThread.LocalFullNodeNeighborPools.stream()
                    .filter(member -> (""+shard).equals(""+member.metadata().get("shard")))
                    .collect(Collectors.toList());   //分片i的局部全节点
            if ( null==shardNodes || shardNodes.size()<=0 ) {
                shardLocalCount[i] = 0;
                continue;
            } else {
                shardLocalCount[i] = shardNodes.size();
            }
            for (Member member : shardNodes) {
                if (StringUtils.isEmpty(member.metadata().get("index")) ) {
                    continue;
                }
                shardId = Integer.parseInt(member.metadata().get("shard"));
                creatorId = Integer.parseInt(member.metadata().get("index"));

                firstTime = member.metadata().get("firstTime");
                latestTime = member.metadata().get("latestTime");
                transData = member.metadata().get("transactionCounts");
                eventData = member.metadata().get("eventCounts");
                avgLatency = member.metadata().get("avgLatency");

                if(!StringUtils.isEmpty(transData)) {       // transactions
//                    logger.info("transData: " + transData);
                    nodeTransactions[shardId][creatorId] = Long.parseLong(transData);
                }
                if (!StringUtils.isEmpty(eventData)) {      // events
//                    logger.info("eventData: " + eventData);
                    nodeEvents[shardId][creatorId] = Long.parseLong(eventData);
                }
                if (!StringUtils.isEmpty(firstTime) && !StringUtils.isEmpty(latestTime)) {  // total costTime, firstTime不变
//                    logger.info("costTime: " + (Long.parseLong(latestTime)-Long.parseLong(firstTime)));
                    nodeTimes[shardId][creatorId] = Long.parseLong(latestTime)-Long.parseLong(firstTime);
                } else {
                    logger.error("############################### time error.");
                }

                //统计分片信息
                shardTransactions[shardId] += nodeTransactions[shardId][creatorId];
                shardEvents[shardId] += nodeEvents[shardId][creatorId];
                shardTimes[shardId] += nodeTimes[shardId][creatorId];

                member = null;
            }
            shardNodes = null;

            // 统计每个分片中单节点的trans、events、cost、latency以及每个分片的tps和eps
//            logger.info("shardLocalCount["+i+"]: " + shardLocalCount[i]);
            if( 0==shardLocalCount[i] ) {
                shardAvgTransactions[i] = 0;
                shardAvgEvents[i] = 0;
                shardAvgTimes[i] = 0;

                shardAvgTps[i] = 0.0;
                shardAvgEps[i] = 0.0;
            } else {
                shardAvgTransactions[i] = shardTransactions[i]/(shardLocalCount[i]*1.0);        //i分片平均单个节点的总交易

//                logger.info("++++++++++++++++++++++++++++ 分片-"+i+"本批次平均单个节点的总交易: " + shardAvgTransactions[i]);
//                logger.info("++++++++++++++++++++++++++++ 分片-"+i+"本批次平均单个节点的总event: " + shardAvgEvents[i]);
//                logger.info("++++++++++++++++++++++++++++ 分片-"+i+"本批次平均单个节点的总延迟: " + shardAvgLatencies[i] + " sec");
                shardAvgTimes[i] = shardTimes[i]/1000.0/(shardLocalCount[i]*1.0);               //i分片平均单个节点的总花费时间

                shardAvgTps[i] = (shardTimes[i]==0) ? 0.0 : shardAvgTransactions[i]/(shardAvgTimes[i]);  //i分片TPS
                shardAvgEps[i] = (shardTimes[i]==0) ? 0.0 : shardAvgEvents[i]/(shardAvgTimes[i]);        //i分片EPS
                shardExist++;
            }
//
//            logger.info("++++++++++++++++++++++++++++ the average total cost time of shard-" + i + ": " + shardAvgTimes[i] + " sec");
//            logger.info("++++++++++++++++++++++++++++ the average total transactions of shard-" + i + ": " + shardTransactionsOfAllBatches[i]);
//            logger.info("++++++++++++++++++++++++++++ the average total events of shard-" + i + ": " + shardEventsOfAllBatches[i]);
//            logger.info("++++++++++++++++++++++++++++ the average latency of shard-" + i + ": " + shardAvgLatencies[i] + " sec");
//            logger.info("++++++++++++++++++++++++++++ the tps of shard-" + i + ": " + shardAvgTps[i]);
//            logger.info("++++++++++++++++++++++++++++ the eps of shard-" + i + ": " + shardAvgEps[i]);

            tpsPlusSeries[index] += shardAvgTps[i];     //本批次所有分片TPS之和
            epsPlusSeries[index] += shardAvgEps[i];     //本批次所有分片EPS之和

            avgTotalTransactionsSeries[index] += shardAvgTransactions[i];
            avgTotalEventsSeries[index] += shardAvgEvents[i];
            avgTotalTimesSeries[index] += shardAvgTimes[i];
        }
        if (shardExist>0) {
            avgTotalTimesSeries[index] /= shardExist;
            tpsAvgSeries[index] = avgTotalTransactionsSeries[index]/avgTotalTimesSeries[index];
            epsAvgSeries[index] = avgTotalEventsSeries[index]/avgTotalTimesSeries[index];
        } else {
            avgTotalTimesSeries[index] = 0.0;
            tpsAvgSeries[index] = 0.0;
            epsAvgSeries[index] = 0.0;
        }

        logger.info("********************************************* "+index+" *******************************************");
        logger.info("**************************** the average total cost time for all shards: " + avgTotalTimesSeries[index] + " sec");
        logger.info("**************************** the average total transactions for all shards: " + avgTotalTransactionsSeries[index]);
        logger.info("**************************** the average total events for all shards: " + avgTotalEventsSeries[index]);
        logger.info("**************************** the average latency for all shards: " + (avgTotalTimesSeries[index]/avgTotalEventsSeries[index]) + " sec");
        logger.info("**************************** transactions per sec(shard tps plus): " + tpsPlusSeries[index]);
        logger.info("**************************** transactions per sec(total transactions / total times): " + tpsAvgSeries[index]);
        logger.info("**************************** events per sec(shard eps plus): " + epsPlusSeries[index]);
        logger.info("**************************** events per sec(total events / total times): " + epsAvgSeries[index]);
    }

    private void writeResult(int fileCount) throws Exception {
        ExcelUtils excelUtils = new ExcelUtils(Config.DEFAULT_CONFIG_PATH +File.separator+"result-"+fileCount+".xls");

        int idx = 0;
        excelUtils.write(avgTotalTimesSeries, idx++, "average total cost time for all shards(sec)");
        excelUtils.write(avgTotalTransactionsSeries, idx++, "average total transactions for all shards");
        excelUtils.write(avgTotalEventsSeries, idx++, "average total events for all shards");
        excelUtils.write(tpsPlusSeries, idx++, "tps(the sum of the tps of all shards)");
        excelUtils.write(tpsAvgSeries, idx++, "tps(total transactions/average cost time)");
        excelUtils.write(epsPlusSeries, idx++, "eps(the sum of the eps of all shards)");
        excelUtils.write(epsAvgSeries, idx++, "eps(total events/average cost time)");

        logger.info("write excel success.");
    }
}
