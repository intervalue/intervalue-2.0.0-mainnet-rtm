package one.inve.localfullnode2.snapshot;

import com.alibaba.fastjson.JSONObject;
import one.inve.bean.message.Contribution;
import one.inve.bean.message.SnapshotPoint;
import one.inve.bean.node.LocalFullNode;
import one.inve.core.EventBody;

import java.math.BigInteger;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.concurrent.LinkedBlockingQueue;

public interface CreateSnapshotPointDependent {

    BigInteger getTotalConsEventCount();

    int getShardCount();

    long getCreatorId();

    int getShardId();

    int getnValue();

    HashSet<Contribution> getContributions();

    List<LocalFullNode> getLocalFullNodes();

    String getMsgHashTreeRoot();

    HashMap<BigInteger, SnapshotPoint> getSnapshotPointMap();

    BigInteger getCurrSnapshotVersion();

    HashMap<BigInteger, String> getTreeRootMap();

    BigInteger getConsMessageMaxId();

    LinkedBlockingQueue<JSONObject> getConsMessageVerifyQueue();

    void setContributions(HashSet<Contribution> contributions);

    EventBody getEventBody();

}
