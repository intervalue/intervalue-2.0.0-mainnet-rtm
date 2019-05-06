package one.inve.localfullnode2.snapshot;

import com.alibaba.fastjson.JSONObject;
import one.inve.bean.message.SnapshotMessage;
import one.inve.bean.message.SnapshotPoint;
import java.math.BigInteger;
import java.util.HashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

public interface HandleSnapshotPointMessageDependent {

    BigInteger getCurrSnapshotVersion();

    HashMap<BigInteger, SnapshotPoint> getSnapshotPointMap();

    JSONObject getMsgObject();

    void setTotalFeeBetween2Snapshots(BigInteger totalFeeBetween2Snapshots);

    SnapshotMessage getSnapshotMessage();

    //与上一个快照点之间的交易手续费总额
    BigInteger getTotalFeeBetween2Snapshots();

    String getPubKey();

    String getMnemonic();

    String getAddress();

    ConcurrentLinkedQueue<byte[]> getMessageQueue();

}
