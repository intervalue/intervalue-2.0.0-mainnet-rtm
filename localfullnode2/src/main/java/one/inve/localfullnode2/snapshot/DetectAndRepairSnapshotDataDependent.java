package one.inve.localfullnode2.snapshot;

import one.inve.bean.message.SnapshotMessage;
import one.inve.bean.message.SnapshotPoint;
import one.inve.core.EventBody;

import java.math.BigInteger;
import java.util.HashMap;

public interface DetectAndRepairSnapshotDataDependent {

    long getCreatorId();

    int getShardId();

    int getnValue();

    HashMap<BigInteger, String> getTreeRootMap();

    String getDbId();

    HashMap<BigInteger, SnapshotPoint> getSnapshotPointMap();

    void deleteEventsBeforeSnapshotPointEvent(String dbId, EventBody eb, int nValue);

    void setSnapshotMessage(SnapshotMessage snapshotMessage);
}
