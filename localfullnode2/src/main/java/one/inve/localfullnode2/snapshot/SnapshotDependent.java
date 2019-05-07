package one.inve.localfullnode2.snapshot;

import one.inve.bean.message.SnapshotPoint;
import java.math.BigInteger;
import java.util.HashMap;

public interface SnapshotDependent {

    BigInteger getCurrSnapshotVersion();

    HashMap<BigInteger, SnapshotPoint> getSnapshotPointMap();

    long getCreatorId();

    int getShardId();

    int getnValue();

    HashMap<BigInteger, String> getTreeRootMap();

    int getShardCount();
}
