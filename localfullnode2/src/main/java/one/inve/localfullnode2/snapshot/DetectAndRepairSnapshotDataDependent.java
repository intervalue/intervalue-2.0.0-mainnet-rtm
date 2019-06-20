package one.inve.localfullnode2.snapshot;

import one.inve.bean.message.SnapshotMessage;
import one.inve.bean.message.SnapshotPoint;
import one.inve.core.EventBody;

import java.math.BigInteger;
import java.util.HashMap;

public interface DetectAndRepairSnapshotDataDependent {

    BigInteger getCurrSnapshotVersion();

    long getCreatorId();

    int getShardId();

    int getnValue();

    HashMap<BigInteger, String> getTreeRootMap();

    void removeTreeRootMap(BigInteger snapVersion);

    String getDbId();

    HashMap<BigInteger, SnapshotPoint> getSnapshotPointMap();

    void setSnapshotMessage(SnapshotMessage snapshotMessage);

    void putTreeRootMap(BigInteger snapVersion, String msgHashTreeRoot);

    void putSnapshotPointMap(BigInteger snapVersion, SnapshotPoint snapshotPoint);
}
