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
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public interface RepairCurrSnapshotPointInfoDependent {

    HashMap<BigInteger, SnapshotPoint> getSnapshotPointMap();

    BigInteger getCurrSnapshotVersion();

    BigInteger getTotalConsEventCount();

    int getShardCount();

    long getCreatorId();

    int getShardId();

    int getnValue();

    HashSet<Contribution> getContributions();

    List<LocalFullNode> getLocalFullNodes();

    HashMap<BigInteger, String> getTreeRootMap();

    BigInteger getConsMessageMaxId();

    BlockingQueue<JSONObject> getConsMessageVerifyQueue();

    void setContributions(HashSet<Contribution> contributions);

    BlockingQueue<EventBody> getShardSortQueue(int shardId);

    void setTotalConsEventCount(BigInteger totalConsEventCount);

    String getDbId();

    void setConsMessageMaxId(BigInteger consMessageMaxId);

    String getMsgHashTreeRoot();

    void setMsgHashTreeRoot(String msgHashTreeRoot);

}
