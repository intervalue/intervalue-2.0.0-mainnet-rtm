package one.inve.localfullnode2.snapshot;

import com.alibaba.fastjson.JSONObject;
import one.inve.bean.message.SnapshotMessage;

import java.math.BigInteger;
import java.util.HashMap;
import java.util.concurrent.LinkedBlockingQueue;

public interface HandleConsensusSnapshotMessageDependent {

    JSONObject getMsgObject();

    HashMap<BigInteger, String> getTreeRootMap();

    BigInteger getSystemAutoTxMaxId();

    void setSystemAutoTxMaxId(BigInteger systemAutoTxMaxId);

    LinkedBlockingQueue<JSONObject> getSystemAutoTxSaveQueue();

    String getDbId();

    LinkedBlockingQueue<JSONObject> getConsMessageSaveQueue();

    void clearHistoryEventsBySnapshot(BigInteger vers, String preHash);

    void setSnapshotMessage(SnapshotMessage snapshotMessage);

    boolean transfer(String dbId, String fromAddr, String toAddr, BigInteger value);

}
