package one.inve.localfullnode2.snapshot;

import one.inve.bean.message.SnapshotMessage;
import one.inve.bean.message.SnapshotPoint;
import one.inve.node.Main;

import java.math.BigInteger;
import java.util.HashMap;

public class DetectAndRepairSnapshotDataDependentImpl2 implements DetectAndRepairSnapshotDataDependent {

    private Main node;

    public DetectAndRepairSnapshotDataDependentImpl2(Main node) {
        this.node = node;
    }

    @Override
    public BigInteger getCurrSnapshotVersion() {
        return node.getCurrSnapshotVersion();
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
    public HashMap<BigInteger, String> getTreeRootMap() {
        return node.getTreeRootMap();
    }

    @Override
    public String getDbId() {
        return node.nodeParameters.dbId;
    }

    @Override
    public HashMap<BigInteger, SnapshotPoint> getSnapshotPointMap() {
        return node.getSnapshotPointMap();
    }

    @Override
    public void setSnapshotMessage(SnapshotMessage snapshotMessage) {
        node.setSnapshotMessage(snapshotMessage);
    }
}
