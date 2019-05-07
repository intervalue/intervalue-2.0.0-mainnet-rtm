package one.inve.localfullnode2.snapshot;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import one.inve.bean.message.Contribution;
import one.inve.bean.message.SnapshotPoint;
import one.inve.bean.node.LocalFullNode;
import one.inve.core.EventBody;
import one.inve.localfullnode2.conf.Config;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.concurrent.LinkedBlockingQueue;

public class RepairCurrSnapshotPointInfoDependentImpl implements RepairCurrSnapshotPointInfoDependent {

    HashMap<BigInteger, SnapshotPoint> snapshotPointMap = new HashMap<BigInteger, SnapshotPoint>();
    @Override
    public HashMap<BigInteger, SnapshotPoint> getSnapshotPointMap() {
        String snapshotPointStr = "{\"contributions\":{\"TBFHMMQINYWJNDT74GJLNPF5JNROEWDG\":1734," +
                "\"JC3ULYLYIZXHY25EMBWWDMPSVCHPVX7T\":1743,\"KNR6NYNGWAEPAX7VYOI2KKXBTD5ABQDT\":1723," +
                "\"Q3K5BITCGSOYNB7LSNDPX6XM52HA7CEK\":1733,\"5LNNLSEQIIGAB5FP7YMBM2N5Q6ZQ4NBA\":1732," +
                "\"ZXDFYOU7EKSJZBV2ZM4J7MR466Q2KPAI\":1738,\"NUOX47THDUFUT7Z6XPNN75YJYRJK2LVC\":1729," +
                "\"73SRW2DPLN7YFTSIVVGHRCMDRO7G6HZQ\":1732,\"WQE2GM2BSEBH6MREP2Q4MDG7LDDL2NRY\":1737," +
                "\"UVTNWYRJUDBPV3GRL2DY7HKYLC6JNJDL\":1739},\"eventBody\":{\"consEventCount\":144900000," +
                "\"consTimestamp\":\"2019-05-04T16:37:20.286Z\",\"creatorId\":6,\"creatorSeq\":14575190," +
                "\"generation\":24096409,\"hash\":\"YClht0S1KF+y5vKAwgKyCcnqztGBf3tlrWvwRBCm31OUHTv7BR5/p8P1iRcMFsbO" +
                "\",\"isFamous\":false,\"otherId\":4,\"otherSeq\":13384060,\"shardId\":0," +
                "\"signature\":\"EXoOxkG2IRPOWIp6Bx9GcR++w/lrxby6qhHjBP0YWljFdLCwUfLi" +
                "+gKjtdyUJcaiXCKVRJ2AhshFjfpDJoNKzMbFtyFkXj8JMXo+6yMqzkR0uAsDXtvZ9xQPfP4NjP15tEgaglnHWRK91" +
                "+aE6WQAWE1RD7zDCu/7+k1Lcej2emc=\",\"timeCreated\":\"2019-05-04T16:36:52.241Z\",\"transCount\":2627}," +
                "\"msgHashTreeRoot\":\"YClht0S1KF+y5vKAwgKyCcnqztGBf3tlrWvwRBCm31OUHTv7BR5/p8P1iRcMFsbO\"," +
                "\"rewardRatio\":0}";
        SnapshotPoint snapshotPoint = JSON.parseObject(snapshotPointStr,SnapshotPoint.class);
        snapshotPointMap.put(getCurrSnapshotVersion().subtract(BigInteger.ONE),snapshotPoint);
        return this.snapshotPointMap;
    }

    @Override
    public BigInteger getCurrSnapshotVersion() {
        return new BigInteger("318");
    }

    @Override
    public BigInteger getTotalConsEventCount() {
        return new BigInteger("30");
    }

    @Override
    public int getShardCount() {
        return 1;
    }

    @Override
    public long getCreatorId() {
        return 2;
    }

    @Override
    public int getShardId() {
        return 0;
    }

    @Override
    public int getnValue() {
        return 10;
    }

    @Override
    public HashSet<Contribution> getContributions() {
        HashSet<Contribution> contributions = new HashSet<Contribution>();
        long[] otherIds = {0, 1, 2, 3, 4, 5, 6, 7, 8, 9};
        long otherSeq = 13384096;
        for (int i = 0; i < 10; i++) {
            Contribution contribution = new Contribution();
            contribution.setShardId(getShardId());
            contribution.setCreatorId(getCreatorId());
            contribution.setOtherId(otherIds[(int) (1 + Math.random() * otherIds.length - 1)]);
            contribution.setOtherSeq(otherSeq);
            contributions.add(contribution);
            otherSeq += 10;
        }
        return contributions;
    }

    @Override
    public List<LocalFullNode> getLocalFullNodes() {
        List<LocalFullNode> list = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            LocalFullNode localFullNode = new LocalFullNode.Builder()
                    .shard(String.valueOf(getShardId()))
                    .index(String.valueOf(i))
                    .address(Config.CREATION_ADDRESSES.get(i))
                    .build();
            list.add(localFullNode);
        }
        return list;
    }

    HashMap<BigInteger, String> treeRootMap = new HashMap<BigInteger, String>();
    @Override
    public HashMap<BigInteger, String> getTreeRootMap() {
        return this.treeRootMap;
    }

    @Override
    public BigInteger getConsMessageMaxId() {
        return new BigInteger("2627");
    }

    @Override
    public LinkedBlockingQueue<JSONObject> getConsMessageVerifyQueue() {
        return new LinkedBlockingQueue<JSONObject>();
    }

    @Override
    public void setContributions(HashSet<Contribution> contributions) {

    }

    LinkedBlockingQueue<EventBody> shardSortQueue = new LinkedBlockingQueue<EventBody>();
    @Override
    public LinkedBlockingQueue<EventBody> getShardSortQueue(int shardId) {
        String eventBodyStr = "{\"generation\":24096409,\"isFamous\":false,\"otherId\":4," +
                "\"creatorSeq\":14575190,\"signature\":\"EXoOxkG2IRPOWIp6Bx9GcR++w/lrxby6qhHjBP0YWljFdLCwUfLi" +
                "+gKjtdyUJcaiXCKVRJ2AhshFjfpDJoNKzMbFtyFkXj8JMXo+6yMqzkR0uAsDXtvZ9xQPfP4NjP15tEgaglnHWRK91" +
                "+aE6WQAWE1RD7zDCu/7+k1Lcej2emc=\",\"transCount\":2627,\"creatorId\":6,\"shardId\":0," +
                "\"consEventCount\":144900000,\"consTimestamp\":\"2019-05-04T16:37:20.286Z\"," +
                "\"timeCreated\":\"2019-05-04T16:36:52.241Z\"," +
                "\"hash\":\"YClht0S1KF+y5vKAwgKyCcnqztGBf3tlrWvwRBCm31OUHTv7BR5/p8P1iRcMFsbO\"," +
                "\"otherSeq\":13384060}";
        EventBody eventBody = JSONObject.parseObject(eventBodyStr,EventBody.class);
        try {
            shardSortQueue.put(eventBody);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return shardSortQueue;
    }

    @Override
    public void setTotalConsEventCount(BigInteger totalConsEventCount) {

    }

    @Override
    public String getDbId() {
        return "0_2";
    }

    @Override
    public void setConsMessageMaxId(BigInteger consMessageMaxId) {

    }
}
