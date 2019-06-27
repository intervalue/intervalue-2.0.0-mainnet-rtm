package one.inve.localfullnode2.snapshot;

import com.alibaba.fastjson.JSONObject;
import one.inve.bean.message.SnapshotMessage;
import one.inve.bean.message.SnapshotPoint;
import one.inve.node.Main;

import java.math.BigInteger;
import java.util.HashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

public class HandleSnapshotPointMessageDependentImpl2 implements HandleSnapshotPointMessageDependent {

    private Main node;
    private JSONObject msgObject;

    public HandleSnapshotPointMessageDependentImpl2(Main node, JSONObject msgObject) {
        this.node = node;
        this.msgObject = msgObject;
    }

    @Override
    public BigInteger getCurrSnapshotVersion() {
        return node.getCurrSnapshotVersion();
    }

    @Override
    public HashMap<BigInteger, SnapshotPoint> getSnapshotPointMap() {
        return node.getSnapshotPointMap();
    }

    @Override
    public JSONObject getMsgObject() {
        return this.msgObject;
    }

    @Override
    public void setTotalFeeBetween2Snapshots(BigInteger bigInteger) {
        node.setTotalFeeBetween2Snapshots(bigInteger);
    }

    @Override
    public SnapshotMessage getSnapshotMessage() {
        return node.getSnapshotMessage();
    }

    @Override
    public BigInteger getTotalFeeBetween2Snapshots() {
        return node.getTotalFeeBetween2Snapshots();
    }

    @Override
    public String getPubKey() {
        return node.getWallet().getExtKeys().getPubKey();
    }

    @Override
    public String getMnemonic() {
        return node.getWallet().getMnemonic();
    }

    @Override
    public String getAddress() {
        return node.getWallet().getAddress();
    }

    @Override
    public ConcurrentLinkedQueue<byte[]> getMessageQueue() {
        return node.getMessageQueue();
    }

}
