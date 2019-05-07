package one.inve.localfullnode2.snapshot;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import one.inve.bean.message.SnapshotMessage;
import one.inve.bean.message.SnapshotPoint;
import java.math.BigInteger;
import java.util.HashMap;

public class DetectAndRepairSnapshotDataDependentImpl implements DetectAndRepairSnapshotDataDependent {

    @Override
    public BigInteger getCurrSnapshotVersion() {
        return new BigInteger("318");
    }

    @Override
    public long getCreatorId() {
        return 2;
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
//        treeRootMap.put(getCurrSnapshotVersion(),"YClht0S1KF+y5vKAwgKyCcnqztGBf3tlrWvwRBCm31OUHTv7BR5/p8P1iRcMFsbO");
        return this.treeRootMap;
    }

    @Override
    public String getDbId() {
        return "0_2";
    }

    HashMap<BigInteger, SnapshotPoint> snapshotPointMap = new HashMap<BigInteger, SnapshotPoint>();
    @Override
    public HashMap<BigInteger, SnapshotPoint> getSnapshotPointMap() {
        return this.snapshotPointMap;
    }

    @Override
    public void setSnapshotMessage(SnapshotMessage snapshotMessage) {

    }
}
