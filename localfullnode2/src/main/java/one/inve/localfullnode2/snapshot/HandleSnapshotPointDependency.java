package one.inve.localfullnode2.snapshot;

import one.inve.bean.message.SnapshotMessage;
import one.inve.bean.message.SnapshotPoint;
import one.inve.localfullnode2.dep.DependentItem;
import one.inve.localfullnode2.dep.DependentItemConcerned;
import one.inve.localfullnode2.dep.items.*;
import one.inve.localfullnode2.staging.StagingArea;
import java.math.BigInteger;
import java.util.HashMap;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentLinkedQueue;

public class HandleSnapshotPointDependency implements HandleSnapshotPointDependent, DependentItemConcerned {

    private SS ss;
    private AllQueues allQueues;
    private Mnemonic mnemonic;
    private PublicKey publicKey;

    @Override
    public void update(DependentItem item) {
        set(this,item);
    }

    @Override
    public BigInteger getCurrSnapshotVersion() {
        return ss.getCurrSnapshotVersion();
    }

    @Override
    public HashMap<BigInteger, SnapshotPoint> getSnapshotPointMap() {
        return ss.getSnapshotPointMap();
    }

    @Override
    public void setTotalFeeBetween2Snapshots(BigInteger totalFeeBetween2Snapshots) {
        ss.setTotalFeeBetween2Snapshots(totalFeeBetween2Snapshots);
    }

    @Override
    public SnapshotMessage getSnapshotMessage() {
        return ss.getSnapshotMessage();
    }

    @Override
    public BigInteger getTotalFeeBetween2Snapshots() {
        return ss.getTotalFeeBetween2Snapshots();
    }

    @Override
    public String getPubKey() {
        return publicKey.get().toString();
    }

    @Override
    public String getMnemonic() {
        return mnemonic.get();
    }

    @Override
    public String getAddress() {
        //TODO getAddress
        return null;
    }

    @Override
    public BlockingQueue<byte[]> getMessageQueue() {
        return allQueues.get().getQueue(byte[].class, StagingArea.MessageQueueName);
    }
}
