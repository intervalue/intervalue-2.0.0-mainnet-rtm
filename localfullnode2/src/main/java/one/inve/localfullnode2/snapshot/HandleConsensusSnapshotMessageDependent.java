package one.inve.localfullnode2.snapshot;

import com.alibaba.fastjson.JSONObject;
import one.inve.bean.message.SnapshotMessage;

import java.math.BigInteger;
import java.util.HashMap;
import java.util.concurrent.LinkedBlockingQueue;

public interface HandleConsensusSnapshotMessageDependent {

    BigInteger getCurrSnapshotVersion();

    JSONObject getMsgObject();

    HashMap<BigInteger, String> getTreeRootMap();

    BigInteger getSystemAutoTxMaxId();

    void setSystemAutoTxMaxId(BigInteger systemAutoTxMaxId);

    LinkedBlockingQueue<JSONObject> getSystemAutoTxSaveQueue();

    String getDbId();

    LinkedBlockingQueue<JSONObject> getConsMessageSaveQueue();

    int getMultiple();

    void setSnapshotMessage(SnapshotMessage snapshotMessage);

    void transfer(String dbId, String fromAddr, String toAddr, BigInteger value);

    int getShardCount();

    int getnValue();

}
