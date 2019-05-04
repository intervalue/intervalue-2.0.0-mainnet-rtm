package one.inve.localfullnode2.snapshot;

import com.alibaba.fastjson.JSONObject;
import one.inve.bean.message.SnapshotMessage;
import one.inve.bean.message.SnapshotPoint;
import one.inve.cluster.Member;
import one.inve.localfullnode2.snapshot.vo.GossipObj;
import java.math.BigInteger;
import java.security.PublicKey;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.concurrent.LinkedBlockingQueue;

public interface SyncSnapshotDependent {

    GossipObj getGossipObj();

    BigInteger getCurrSnapshotVersion();

    int getShardCount();

    int getnValue();

    int getShardId();

    BigInteger getConsMessageMaxId();

    LinkedBlockingQueue<JSONObject> getConsMessageVerifyQueue();

    HashMap<BigInteger, SnapshotPoint> getSnapshotPointMap();

    void setSnapshotMessage(SnapshotMessage snapshotMessage);

    HashMap<BigInteger, String> getTreeRootMap();

    Map<String, HashSet<String>> getSnapVersionMap();

    Member getNeighbor();

    PublicKey getPublicKey();


}
