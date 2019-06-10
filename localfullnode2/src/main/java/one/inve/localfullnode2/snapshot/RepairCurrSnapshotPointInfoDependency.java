package one.inve.localfullnode2.snapshot;

import com.alibaba.fastjson.JSONObject;
import one.inve.bean.message.Contribution;
import one.inve.bean.message.SnapshotPoint;
import one.inve.bean.node.LocalFullNode;
import one.inve.core.EventBody;
import one.inve.localfullnode2.dep.DependentItem;
import one.inve.localfullnode2.dep.DependentItemConcerned;
import one.inve.localfullnode2.dep.items.*;
import one.inve.localfullnode2.staging.StagingArea;

import java.math.BigInteger;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.concurrent.BlockingQueue;

public class RepairCurrSnapshotPointInfoDependency implements RepairCurrSnapshotPointInfoDependent, DependentItemConcerned {

    private SS ss;
    private Stat stat;
    private ShardCount shardCount;
    private NValue nValue;
    private LocalFullNodes localFullNodes;
    private ShardId shardId;
    private CreatorId creatorId;
    private AllQueues allQueues;
    private DBId dbId;

    @Override
    public void update(DependentItem item) {
        set(this,item);
    }

    @Override
    public HashMap<BigInteger, SnapshotPoint> getSnapshotPointMap() {
        return ss.getSnapshotPointMap();
    }

    @Override
    public BigInteger getCurrSnapshotVersion() {
        return ss.getCurrSnapshotVersion();
    }

    @Override
    public BigInteger getTotalConsEventCount() {
        return stat.getTotalConsEventCount();
    }

    @Override
    public int getShardCount() {
        return shardCount.get();
    }

    @Override
    public long getCreatorId() {
        return creatorId.get();
    }

    @Override
    public int getShardId() {
        return shardId.get();
    }

    @Override
    public int getnValue() {
        return nValue.get();
    }

    @Override
    public HashSet<Contribution> getContributions() {
        return ss.getContributions();
    }

    @Override
    public List<LocalFullNode> getLocalFullNodes() {
        return localFullNodes.get();
    }

    @Override
    public HashMap<BigInteger, String> getTreeRootMap() {
        return ss.getTreeRootMap();
    }

    @Override
    public BigInteger getConsMessageMaxId() {
        return stat.getConsMessageMaxId();
    }

    @Override
    public BlockingQueue<JSONObject> getConsMessageVerifyQueue() {
        return allQueues.get().getQueue(JSONObject.class, StagingArea.ConsMessageVerifyQueueName);
    }

    @Override
    public void setContributions(HashSet<Contribution> contributions) {
        ss.setContributions(contributions);
    }

    @Override
    public BlockingQueue<EventBody> getShardSortQueue(int shardId) {
        return allQueues.get().getQueue(EventBody.class,StagingArea.ShardSortQueueName);
    }

    @Override
    public void setTotalConsEventCount(BigInteger totalConsEventCount) {
        stat.setTotalConsEventCount(totalConsEventCount);
    }

    @Override
    public String getDbId() {
        return dbId.get();
    }

    @Override
    public void setConsMessageMaxId(BigInteger consMessageMaxId) {
        stat.setConsMessageMaxId(consMessageMaxId);
    }

    @Override
    public String getMsgHashTreeRoot() {
        return ss.getMsgHashTreeRoot();
    }

    @Override
    public void setMsgHashTreeRoot(String msgHashTreeRoot) {
        ss.setMsgHashTreeRoot(msgHashTreeRoot);
    }
}
