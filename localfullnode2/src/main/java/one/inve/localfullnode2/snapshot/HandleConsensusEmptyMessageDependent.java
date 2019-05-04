package one.inve.localfullnode2.snapshot;

import com.alibaba.fastjson.JSONObject;
import one.inve.bean.message.SnapshotMessage;
import one.inve.bean.message.SnapshotPoint;
import java.math.BigInteger;
import java.util.HashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

public interface HandleConsensusEmptyMessageDependent {

    HashMap<BigInteger, SnapshotPoint> getSnapshotPointMap();

    BigInteger getCurrSnapshotVersion();

    JSONObject getMsgObject();

    void setTotalFeeBetween2Snapshots(BigInteger totalFeeBetween2Snapshots);

    SnapshotMessage getSnapshotMessage();

    BigInteger getTotalFeeBetween2Snapshots();

    String getPubKey();

    String getMnemonic();

    String getAddress();

    ConcurrentLinkedQueue<byte[]> getMessageQueue();

}
