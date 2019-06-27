package one.inve.localfullnode2.snapshot;

import one.inve.bean.message.SnapshotMessage;
import one.inve.bean.message.SnapshotPoint;
import one.inve.localfullnode2.dep.DependentItem;
import one.inve.localfullnode2.dep.DependentItemConcerned;
import one.inve.localfullnode2.dep.items.*;

import java.math.BigInteger;
import java.util.HashMap;

public class DetectAndRepairSnapshotDataDependency implements DetectAndRepairSnapshotDataDependent, DependentItemConcerned {

    private SS ss;
    private NValue nValue;
    private ShardId shardId;
    private CreatorId creatorId;
    private DBId dbId;

    @Override
    public void update(DependentItem item) {
        set(this,item);
    }

    @Override
    public BigInteger getCurrSnapshotVersion() {
        return ss.getCurrSnapshotVersion();
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
    public HashMap<BigInteger, String> getTreeRootMap() {
        return ss.getTreeRootMap();
    }

    @Override
    public void removeTreeRootMap(BigInteger snapVersion) {
        ss.removeTreeRootMap(snapVersion);
    }

    @Override
    public String getDbId() {
        return dbId.get();
    }

    @Override
    public HashMap<BigInteger, SnapshotPoint> getSnapshotPointMap() {
        return ss.getSnapshotPointMap();
    }

    @Override
    public void setSnapshotMessage(SnapshotMessage snapshotMessage) {
        ss.setSnapshotMessage(snapshotMessage);
    }

    @Override
    public void putTreeRootMap(BigInteger snapVersion, String msgHashTreeRoot) {
        ss.putTreeRootMap(snapVersion, msgHashTreeRoot);
    }

    @Override
    public void putSnapshotPointMap(BigInteger snapVersion, SnapshotPoint snapshotPoint) {
        ss.putSnapshotPointMap(snapVersion, snapshotPoint);
    }
}
