package one.inve.localfullnode2.snapshot;

import com.alibaba.fastjson.JSONObject;
import one.inve.bean.message.SnapshotMessage;
import one.inve.contract.MVM.WorldStateService;
import one.inve.localfullnode2.dep.DependentItem;
import one.inve.localfullnode2.dep.DependentItemConcerned;
import one.inve.localfullnode2.dep.items.*;
import one.inve.localfullnode2.staging.StagingArea;
import java.math.BigInteger;
import java.util.HashMap;
import java.util.concurrent.BlockingQueue;

public class HandleConsensusSnapshotMessageDependency implements HandleConsensusSnapshotMessageDependent,
        DependentItemConcerned {

    private SS ss;
    private Stat stat;
    private ShardCount shardCount;
    private NValue nValue;
    private AllQueues allQueues;
    private DBId dbId;

    @Override
    public void update(DependentItem item) {
        set(this,item);
    }

    @Override
    public BigInteger getCurrSnapshotVersion() {
        return ss.getCurrSnapshotVersion();
    }

    @Override
    public HashMap<BigInteger, String> getTreeRootMap() {
        return ss.getTreeRootMap();
    }

    @Override
    public BigInteger getSystemAutoTxMaxId() {
        return stat.getSystemAutoTxMaxId();
    }

    @Override
    public void setSystemAutoTxMaxId(BigInteger systemAutoTxMaxId) {
        stat.setSystemAutoTxMaxId(systemAutoTxMaxId);
    }

    @Override
    public BlockingQueue<JSONObject> getSystemAutoTxSaveQueue() {
        return allQueues.get().getQueue(JSONObject.class, StagingArea.SystemAutoTxSaveQueueName);
    }

    @Override
    public String getDbId() {
        return dbId.get();
    }

    @Override
    public BlockingQueue<JSONObject> getConsMessageSaveQueue() {
        return allQueues.get().getQueue(JSONObject.class,StagingArea.ConsMessageSaveQueueName);
    }

    @Override
    public int getMultiple() {
        return 1;
    }

    @Override
    public void setSnapshotMessage(SnapshotMessage snapshotMessage) {
        ss.setSnapshotMessage(snapshotMessage);
    }

    @Override
    public void transfer(String dbId, String fromAddr, String toAddr, BigInteger value) {
        WorldStateService.transfer(dbId,fromAddr,toAddr,value);
    }

    @Override
    public int getShardCount() {
        return shardCount.get();
    }

    @Override
    public int getnValue() {
        return nValue.get();
    }
}
