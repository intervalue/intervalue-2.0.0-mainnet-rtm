package one.inve.localfullnode2.snapshot;

import one.inve.bean.message.SnapshotPoint;
import java.math.BigInteger;
import java.util.HashMap;

public class SnapshotDependentImpl implements SnapshotDependent{

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
}
