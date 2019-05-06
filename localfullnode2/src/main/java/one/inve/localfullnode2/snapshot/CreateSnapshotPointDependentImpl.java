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

public class CreateSnapshotPointDependentImpl implements CreateSnapshotPointDependent{

    @Override
    public BigInteger getCurrSnapshotVersion() {
        return new BigInteger("318");
    }

    HashMap<BigInteger, SnapshotPoint> snapshotPointMap = new HashMap<BigInteger, SnapshotPoint>();
    @Override
    public HashMap<BigInteger, SnapshotPoint> getSnapshotPointMap() {
        return this.snapshotPointMap;
    }

    @Override
    public long getCreatorId() {
        return 6;
    }

    @Override
    public int getShardId() {
        return 0;
    }

    @Override
    public int getnValue() {
        return 10;
    }

    HashMap<BigInteger, String> treeRootMap = new HashMap<BigInteger, String>();
    @Override
    public HashMap<BigInteger, String> getTreeRootMap() {
        return this.treeRootMap;
    }

    @Override
    public int getShardCount() {
        return 1;
    }

    @Override
    public BigInteger getTotalConsEventCount() {
        return new BigInteger("30");
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

    @Override
    public EventBody getEventBody() {
        String eventBodyStr = "{\"generation\":24096409,\"isFamous\":false,\"otherId\":4," +
                "\"creatorSeq\":14575190,\"signature\":\"EXoOxkG2IRPOWIp6Bx9GcR++w/lrxby6qhHjBP0YWljFdLCwUfLi" +
                "+gKjtdyUJcaiXCKVRJ2AhshFjfpDJoNKzMbFtyFkXj8JMXo+6yMqzkR0uAsDXtvZ9xQPfP4NjP15tEgaglnHWRK91" +
                "+aE6WQAWE1RD7zDCu/7+k1Lcej2emc=\",\"transCount\":2627,\"creatorId\":6,\"shardId\":0," +
                "\"consEventCount\":144900000,\"consTimestamp\":\"2019-05-04T16:37:20.286Z\"," +
                "\"timeCreated\":\"2019-05-04T16:36:52.241Z\"," +
                "\"hash\":\"YClht0S1KF+y5vKAwgKyCcnqztGBf3tlrWvwRBCm31OUHTv7BR5/p8P1iRcMFsbO\"," +
                "\"otherSeq\":13384060}";
        EventBody eventBody = JSONObject.parseObject(eventBodyStr,EventBody.class);
        return eventBody;
    }
}
