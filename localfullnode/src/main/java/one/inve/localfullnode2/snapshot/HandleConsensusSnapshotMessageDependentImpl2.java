package one.inve.localfullnode2.snapshot;

import com.alibaba.fastjson.JSONObject;
import one.inve.bean.message.SnapshotMessage;
import one.inve.bean.message.SnapshotPoint;
import one.inve.contract.MVM.WorldStateService;
import one.inve.node.Main;

import java.math.BigInteger;
import java.util.HashMap;
import java.util.concurrent.LinkedBlockingQueue;

public class HandleConsensusSnapshotMessageDependentImpl2 implements HandleConsensusSnapshotMessageDependent {

    private Main node;
    private JSONObject msgObject;

    public HandleConsensusSnapshotMessageDependentImpl2(Main node, JSONObject msgObject) {
        this.node = node;
        this.msgObject = msgObject;
    }

    @Override
    public BigInteger getCurrSnapshotVersion() {
        return node.getCurrSnapshotVersion();
    }

    @Override
    public JSONObject getMsgObject() {
        return this.msgObject = msgObject;
    }

    @Override
    public HashMap<BigInteger, String> getTreeRootMap() {
        return node.getTreeRootMap();
    }

    @Override
    public BigInteger getSystemAutoTxMaxId() {
        return node.getSystemAutoTxMaxId();
    }

    @Override
    public void setSystemAutoTxMaxId(BigInteger bigInteger) {
        node.setSystemAutoTxMaxId(bigInteger);
    }

    @Override
    public LinkedBlockingQueue<JSONObject> getSystemAutoTxSaveQueue() {
        return node.getSystemAutoTxSaveQueue();
    }

    @Override
    public String getDbId() {
        return node.nodeParameters.dbId;
    }

    @Override
    public LinkedBlockingQueue<JSONObject> getConsMessageSaveQueue() {
        return node.getConsMessageSaveQueue();
    }

    @Override
    public int getMultiple() {
        return node.nodeParameters.multiple;
    }

    @Override
    public void setSnapshotMessage(SnapshotMessage snapshotMessage) {
        node.setSnapshotMessage(snapshotMessage);
    }

    @Override
    public void transfer(String dbId, String fromAddr, String toAddr, BigInteger value) {
        WorldStateService.transfer(dbId,fromAddr,toAddr,value);
    }

    @Override
    public int getShardCount() {
        return node.getShardCount();
    }

    @Override
    public int getnValue() {
        return node.getnValue();
    }

}
