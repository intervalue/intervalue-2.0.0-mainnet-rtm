package one.inve.localfullnode2.snapshot;

import com.alibaba.fastjson.JSONObject;
import one.inve.bean.message.SnapshotMessage;
import one.inve.bean.message.SnapshotPoint;
import one.inve.bean.node.LocalFullNode;
import one.inve.cluster.Member;
import one.inve.localfullnode2.gossip.vo.GossipObj;
import one.inve.localfullnode2.message.service.ITransactionDbService;
import one.inve.localfullnode2.snapshot.vo.SnapObj;
import one.inve.localfullnode2.store.SnapshotDbService;
import one.inve.node.GeneralNode;

import java.math.BigInteger;
import java.security.PublicKey;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.BlockingQueue;

public class SnapshotSynchronizerDependentImpl2 implements SnapshotSynchronizerDependent {

    private GeneralNode node;
    private Member gossipedMember;
    private GossipObj gossipObj;

    public SnapshotSynchronizerDependentImpl2(GeneralNode node, Member gossipedMember,
                                              GossipObj gossipObj) {
        this.node = node;
        this.gossipedMember = gossipedMember;
        this.gossipObj = gossipObj;
    }

    @Override
    public BigInteger getCurrSnapshotVersion() {
        return node.getCurrSnapshotVersion();
    }

    @Override
    public int getShardId() {
        return node.getShardId();
    }

    @Override
    public int getShardCount() {
        return node.getShardCount();
    }

    @Override
    public int getnValue() {
        return node.getnValue();
    }

    @Override
    public BigInteger getConsMessageMaxId() {
        return node.getConsMessageMaxId();
    }

    private SnapshotSyncConsumable snapshotSyncConsumable;
    @Override
    public SnapshotSyncConsumable getSnapshotSync() {
        return snapshotSyncConsumable;
    }

    @Override
    public PublicKey getPublicKey() {
        return node.publicKey;
    }

    @Override
    public BlockingQueue<JSONObject> getConsMessageVerifyQueue() {
        return node.getConsMessageVerifyQueue();
    }

    @Override
    public void refresh(SnapshotMessage snapshotMessage) {

    }

    @Override
    public boolean execute(SnapObj snapObj) {
        return false;
    }

    @Override
    public long getCreatorId() {
        return node.getCreatorId();
    }

    @Override
    public String getDbId() {
        return node.nodeParameters.dbId;
    }

    @Override
    public List<LocalFullNode> getLocalFullNodes() {
        return node.getLocalFullNodes();
    }

    @Override
    public SnapshotDbService getSnapshotDBService() {
        return null;
    }

    @Override
    public ITransactionDbService getTransactionDbService() {
        return null;
    }

    @Override
    public HashMap<BigInteger, SnapshotPoint> getSnapshotPointMap() {
        return node.getSnapshotPointMap();
    }

    @Override
    public HashMap<BigInteger, String> getTreeRootMap() {
        return node.getTreeRootMap();
    }

    @Override
    public void setSnapshotMessage(SnapshotMessage snapshotMessage) {
        node.setSnapshotMessage(snapshotMessage);
    }

    @Override
    public Member getGossipedMember() {
        return this.gossipedMember;
    }

    @Override
    public GossipObj getGossipObj() {
        return this.gossipObj;
    }

    @Override
    public void setMsgHashTreeRoot(String msgHashTreeRoot) {
        node.msgHashTreeRoot = msgHashTreeRoot;
    }

}
