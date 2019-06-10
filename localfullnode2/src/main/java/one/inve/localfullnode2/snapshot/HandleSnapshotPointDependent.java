package one.inve.localfullnode2.snapshot;

import com.alibaba.fastjson.JSONObject;
import one.inve.bean.message.SnapshotMessage;
import one.inve.bean.message.SnapshotPoint;
import java.math.BigInteger;
import java.util.HashMap;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentLinkedQueue;

public interface HandleSnapshotPointDependent {

    BigInteger getCurrSnapshotVersion();

    HashMap<BigInteger, SnapshotPoint> getSnapshotPointMap();

    void setTotalFeeBetween2Snapshots(BigInteger totalFeeBetween2Snapshots);

    SnapshotMessage getSnapshotMessage();

    BigInteger getTotalFeeBetween2Snapshots();

    String getPubKey();

    String getMnemonic();

    String getAddress();

    BlockingQueue<byte[]> getMessageQueue();

}
