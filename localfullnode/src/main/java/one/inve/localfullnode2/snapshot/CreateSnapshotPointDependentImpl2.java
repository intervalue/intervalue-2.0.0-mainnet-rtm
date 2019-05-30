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

public class CreateSnapshotPointDependentImpl2 implements CreateSnapshotPointDependent {

    private Main node;
    private EventBody eventBody;

    public CreateSnapshotPointDependentImpl2(Main node,EventBody eventBody) {
        this.node = node;
        this.eventBody = eventBody;
    }

    @Override
    public BigInteger getCurrSnapshotVersion() {
        return node.getCurrSnapshotVersion();
    }


    @Override
    public void setContributions(HashSet<Contribution> hashSet) {
        node.setContributions(hashSet);
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
    public HashMap<BigInteger, SnapshotPoint> getSnapshotPointMap() {
        return node.getSnapshotPointMap();
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
    public long getCreatorId() {
        return node.getCreatorId();
    }

    @Override
    public int getShardId() {
        return node.getShardId();
    }

    @Override
    public EventBody getEventBody() {
        return this.eventBody;
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
