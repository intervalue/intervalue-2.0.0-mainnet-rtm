package one.inve.localfullnode2.snapshot;

import com.alibaba.fastjson.JSONObject;
import one.inve.bean.message.Contribution;
import one.inve.bean.message.SnapshotPoint;
import one.inve.bean.node.LocalFullNode;
import one.inve.core.EventBody;
import one.inve.node.Main;

import java.math.BigInteger;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.concurrent.LinkedBlockingQueue;

public class RepairCurrSnapshotPointInfoDependentImpl2 implements RepairCurrSnapshotPointInfoDependent {

    private Main node;

    public RepairCurrSnapshotPointInfoDependentImpl2(Main node) {
        this.node = node;
    }

    @Override
    public HashMap<BigInteger, SnapshotPoint> getSnapshotPointMap() {
        return node.getSnapshotPointMap();
    }

    @Override
    public BigInteger getCurrSnapshotVersion() {
        return node.getCurrSnapshotVersion();
    }

    @Override
    public BigInteger getTotalConsEventCount() {
        return node.getTotalConsEventCount();
    }

    @Override
    public int getShardCount() {
        return node.getShardCount();
    }

    @Override
    public long getCreatorId() {
        return node.getCreatorId();
    }

    @Override
    public int getShardId() {
        return node.getShardId();
    }

    @Override
    public int getnValue() {
        return node.getnValue();
    }

    @Override
    public HashSet<Contribution> getContributions() {
        return node.getContributions();
    }

    @Override
    public List<LocalFullNode> getLocalFullNodes() {
        return node.getLocalFullNodes();
    }

    @Override
    public HashMap<BigInteger, String> getTreeRootMap() {
        return node.getTreeRootMap();
    }

    @Override
    public BigInteger getConsMessageMaxId() {
        return node.getConsMessageMaxId();
    }

    @Override
    public LinkedBlockingQueue<JSONObject> getConsMessageVerifyQueue() {
        return node.getConsMessageVerifyQueue();
    }

    @Override
    public void setContributions(HashSet<Contribution> hashSet) {
        node.setContributions(hashSet);
    }

    @Override
    public LinkedBlockingQueue<EventBody> getShardSortQueue(int i) {
        return node.getShardSortQueue(i);
    }

    @Override
    public void setTotalConsEventCount(BigInteger bigInteger) {
        node.setTotalConsEventCount(bigInteger);
    }

    @Override
    public String getDbId() {
        return node.nodeParameters.dbId;
    }

    @Override
    public void setConsMessageMaxId(BigInteger bigInteger) {
        node.setConsMessageMaxId(bigInteger);
    }

    @Override
    public String getMsgHashTreeRoot() {
        return node.msgHashTreeRoot;
    }

    @Override
    public void setMsgHashTreeRoot(String msgHashTreeRoot) {
        node.msgHashTreeRoot = msgHashTreeRoot;
    }
}
