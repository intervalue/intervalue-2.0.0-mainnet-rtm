package one.inve.localfullnode2.snapshot;

import com.alibaba.fastjson.JSONObject;
import one.inve.bean.message.Contribution;
import one.inve.bean.message.SnapshotMessage;
import one.inve.bean.message.SnapshotPoint;
import one.inve.bean.node.LocalFullNode;
import one.inve.core.EventBody;
import one.inve.localfullnode2.conf.Config;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class SnapshotDependentImpl implements SnapshotDependent{

    private HashSet<Contribution> contributions;
    @Override
    public HashSet<Contribution> getContributions() {
        contributions = new HashSet<Contribution>();
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
        return this.contributions;
    }
    @Override
    public void setContributions(HashSet<Contribution> contributions) {
        this.contributions = contributions;
    }

    private SnapshotMessage snapshotMessage;
    @Override
    public SnapshotMessage getSnapshotMessage() {
        return this.snapshotMessage;
    }
    @Override
    public void setSnapshotMessage(SnapshotMessage snapshotMessage) {
        this.snapshotMessage = snapshotMessage;
    }

    private BigInteger systemAutoTxMaxId;
    @Override
    public BigInteger getSystemAutoTxMaxId() {
        return this.systemAutoTxMaxId;
    }
    @Override
    public void setSystemAutoTxMaxId(BigInteger systemAutoTxMaxId) {
        this.systemAutoTxMaxId = systemAutoTxMaxId;
    }

    private BigInteger totalFeeBetween2Snapshots;
    @Override
    public BigInteger getTotalFeeBetween2Snapshots() {
        return this.totalFeeBetween2Snapshots;
    }
    @Override
    public void setTotalFeeBetween2Snapshots(BigInteger totalFeeBetween2Snapshots) {
        this.totalFeeBetween2Snapshots = totalFeeBetween2Snapshots;
    }

    private BigInteger consMessageMaxId;
    @Override
    public BigInteger getConsMessageMaxId() {
        return this.consMessageMaxId;
    }
    @Override
    public void setConsMessageMaxId(BigInteger consMessageMaxId) {
        this.consMessageMaxId = consMessageMaxId;
    }

    private BigInteger totalConsEventCount;
    @Override
    public BigInteger getTotalConsEventCount() {
        totalConsEventCount = new BigInteger("30");
        return this.totalConsEventCount;
    }
    @Override
    public void setTotalConsEventCount(BigInteger totalConsEventCount) {
        this.totalConsEventCount = totalConsEventCount;
    }

    private BigInteger currSnapshotVersion;
    @Override
    public BigInteger getCurrSnapshotVersion() {
        currSnapshotVersion = new BigInteger("318");
        return this.currSnapshotVersion;
    }

    private HashMap<BigInteger, SnapshotPoint> snapshotPointMap = new HashMap<BigInteger, SnapshotPoint>();
    @Override
    public HashMap<BigInteger, SnapshotPoint> getSnapshotPointMap() {
        return this.snapshotPointMap;
    }

    private long creatorId;
    @Override
    public long getCreatorId() {
        creatorId = 2L;
        return this.creatorId;
    }

    private int shardId;
    @Override
    public int getShardId() {
        shardId = 0;
        return this.shardId;
    }

    private int nValue;
    @Override
    public int getnValue() {
        nValue = 10;
        return this.nValue;
    }

    private HashMap<BigInteger, String> treeRootMap = new HashMap<BigInteger, String>();
    @Override
    public HashMap<BigInteger, String> getTreeRootMap() {
        return this.treeRootMap;
    }

    private int shardCount;
    @Override
    public int getShardCount() {
        shardCount = 1;
        return this.shardCount;
    }

    private List<LocalFullNode> localFullNodes;
    @Override
    public List<LocalFullNode> getLocalFullNodes() {
        localFullNodes = new ArrayList<LocalFullNode>();
        for (int i = 0; i < 10; i++) {
            LocalFullNode localFullNode = new LocalFullNode.Builder()
                    .shard(String.valueOf(getShardId()))
                    .index(String.valueOf(i))
                    .address(Config.CREATION_ADDRESSES.get(i))
                    .build();
            localFullNodes.add(localFullNode);
        }
        return this.localFullNodes;
    }

    private LinkedBlockingQueue<JSONObject> consMessageVerifyQueue;
    @Override
    public LinkedBlockingQueue<JSONObject> getConsMessageVerifyQueue() {
        consMessageVerifyQueue = new LinkedBlockingQueue<JSONObject>();
        return this.consMessageVerifyQueue;
    }

    private EventBody eventBody;
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
        eventBody = JSONObject.parseObject(eventBodyStr,EventBody.class);
        return this.eventBody;
    }

    private String dbId;
    @Override
    public String getDbId() {
        return this.dbId;
    }

    private JSONObject msgObject;
    @Override
    public JSONObject getMsgObject() {
        return this.msgObject;
    }

    private LinkedBlockingQueue<JSONObject> systemAutoTxSaveQueue;
    @Override
    public LinkedBlockingQueue<JSONObject> getSystemAutoTxSaveQueue() {
        return this.systemAutoTxSaveQueue;
    }

    private LinkedBlockingQueue<JSONObject> consMessageSaveQueue;
    @Override
    public LinkedBlockingQueue<JSONObject> getConsMessageSaveQueue() {
        return this.consMessageSaveQueue;
    }

    private String pubKey;
    @Override
    public String getPubKey() {
        return this.pubKey;
    }

    private String mnemonic;
    @Override
    public String getMnemonic() {
        return this.mnemonic;
    }

    private String address;
    @Override
    public String getAddress() {
        return this.address;
    }

    private ConcurrentLinkedQueue<byte[]> messageQueue;
    @Override
    public ConcurrentLinkedQueue<byte[]> getMessageQueue() {
        return this.messageQueue;
    }

    private LinkedBlockingQueue<EventBody> shardSortQueue;
    @Override
    public LinkedBlockingQueue<EventBody> getShardSortQueue(int shardId) {
        return this.shardSortQueue;
    }

    @Override
    public void clearHistoryEventsBySnapshot(BigInteger vers, String preHash) {

    }

    @Override
    public boolean transfer(String dbId, String fromAddr, String toAddr, BigInteger value) {
        return true;
    }


}
