package one.inve.localfullnode2.snapshot;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import one.inve.bean.message.SnapshotMessage;

import java.math.BigInteger;
import java.util.HashMap;
import java.util.concurrent.LinkedBlockingQueue;

public class HandleConsensusSnapshotMessageDependentImpl implements HandleConsensusSnapshotMessageDependent{

    @Override
    public BigInteger getCurrSnapshotVersion() {
        return new BigInteger("318");
    }


    JSONObject msgObject = new JSONObject();
    @Override
    public JSONObject getMsgObject() {
        msgObject =  JSON.parseObject("{\"msg\":\"{\\\"preHash\\\":\\\"32YuK4odYOmKj3tHKAhWw81a/A+0abndzn2020" +
                "/1PrudslVQ1HP0/szqBLN80RXtvTl4ZC6DwYDmNeo0hG22qQlQ==\\\",\\\"snapVersion\\\":318," +
                "\\\"signature\\\":\\\"32O0YpHVThJzSvP2U1vHREJCiFYrPArzdceFRHGISkdi4o" +
                "+ca97WhWZtY7b1quOyND4UB8btuwRdrl28brYOIvZA==\\\",\\\"vers\\\":\\\"2.0\\\"," +
                "\\\"fromAddress\\\":\\\"NUOX47THDUFUT7Z6XPNN75YJYRJK2LVC\\\",\\\"type\\\":3," +
                "\\\"hash\\\":\\\"32O0YpHVThJzSvP2U1vHREJCiFYrPArzdceFRHGISkdi4o" +
                "+ca97WhWZtY7b1quOyND4UB8btuwRdrl28brYOIvZA==\\\"," +
                "\\\"pubkey\\\":\\\"AzZ6psaaBIcYeCEQZRlagF3sSDpYwjeqf1LdA/aTNCL/\\\"," +
                "\\\"snapshotPoint\\\":{\\\"contributions\\\":{\\\"TBFHMMQINYWJNDT74GJLNPF5JNROEWDG\\\":1734," +
                "\\\"JC3ULYLYIZXHY25EMBWWDMPSVCHPVX7T\\\":1743,\\\"KNR6NYNGWAEPAX7VYOI2KKXBTD5ABQDT\\\":1723," +
                "\\\"Q3K5BITCGSOYNB7LSNDPX6XM52HA7CEK\\\":1733,\\\"5LNNLSEQIIGAB5FP7YMBM2N5Q6ZQ4NBA\\\":1732," +
                "\\\"ZXDFYOU7EKSJZBV2ZM4J7MR466Q2KPAI\\\":1738,\\\"NUOX47THDUFUT7Z6XPNN75YJYRJK2LVC\\\":1729," +
                "\\\"73SRW2DPLN7YFTSIVVGHRCMDRO7G6HZQ\\\":1732,\\\"WQE2GM2BSEBH6MREP2Q4MDG7LDDL2NRY\\\":1737," +
                "\\\"UVTNWYRJUDBPV3GRL2DY7HKYLC6JNJDL\\\":1739},\\\"rewardRatio\\\":0.5,\\\"msgMaxId\\\":2627," +
                "\\\"totalFee\\\":0,\\\"msgHashTreeRoot\\\":\\\"YClht0S1KF+y5vKAwgKyCcnqztGBf3tlrWvwRBCm31OUHTv7BR5" +
                "/p8P1iRcMFsbO\\\",\\\"eventBody\\\":{\\\"generation\\\":24096409,\\\"isFamous\\\":false," +
                "\\\"otherId\\\":4,\\\"creatorSeq\\\":14575190," +
                "\\\"signature\\\":\\\"EXoOxkG2IRPOWIp6Bx9GcR++w/lrxby6qhHjBP0YWljFdLCwUfLi" +
                "+gKjtdyUJcaiXCKVRJ2AhshFjfpDJoNKzMbFtyFkXj8JMXo+6yMqzkR0uAsDXtvZ9xQPfP4NjP15tEgaglnHWRK91" +
                "+aE6WQAWE1RD7zDCu/7+k1Lcej2emc=\\\",\\\"transCount\\\":2627,\\\"creatorId\\\":6,\\\"shardId\\\":0," +
                "\\\"consEventCount\\\":144900000,\\\"consTimestamp\\\":\\\"2019-05-04T16:37:20.286Z\\\"," +
                "\\\"timeCreated\\\":\\\"2019-05-04T16:36:52.241Z\\\"," +
                "\\\"hash\\\":\\\"YClht0S1KF+y5vKAwgKyCcnqztGBf3tlrWvwRBCm31OUHTv7BR5/p8P1iRcMFsbO\\\"," +
                "\\\"otherSeq\\\":13384060}},\\\"timestamp\\\":1556988080596}\"," +
                "\"eHash\":\"4pEUdDt2wTmXVTgGolnZLFOUnafeqj/XDqGkpeZSoV9lMExm91crIdGiT4ksMgCL\",\"eShardId\":0," +
                "\"lastIdx\":true,\"isValid\":true,\"updateTime\":1556988119016,\"id\":2628,\"type\":3," +
                "\"isStable\":true}");
        return this.msgObject;
    }


    HashMap<BigInteger, String> treeRootMap = new HashMap<BigInteger, String>();
    @Override
    public HashMap<BigInteger, String> getTreeRootMap() {
        treeRootMap.put(getCurrSnapshotVersion(),"YClht0S1KF+y5vKAwgKyCcnqztGBf3tlrWvwRBCm31OUHTv7BR5/p8P1iRcMFsbO");
        return this.treeRootMap;
    }

    @Override
    public BigInteger getSystemAutoTxMaxId() {
        return null;
    }

    @Override
    public void setSystemAutoTxMaxId(BigInteger systemAutoTxMaxId) {
        System.out.println(JSON.toJSONString(systemAutoTxMaxId));
    }

    @Override
    public LinkedBlockingQueue<JSONObject> getSystemAutoTxSaveQueue() {
        return new LinkedBlockingQueue<JSONObject>();
    }

    @Override
    public String getDbId() {
        return "0_2";
    }

    @Override
    public LinkedBlockingQueue<JSONObject> getConsMessageSaveQueue() {
        return new LinkedBlockingQueue<JSONObject>();
    }

    @Override
    public int getMultiple() {
        return 0;
    }

    @Override
    public void setSnapshotMessage(SnapshotMessage snapshotMessage) {
        System.out.println(JSON.toJSONString(snapshotMessage));
    }

    @Override
    public void transfer(String dbId, String fromAddr, String toAddr, BigInteger value) {

    }

    @Override
    public int getShardCount() {
        return 1;
    }

    @Override
    public int getnValue() {
        return 10;
    }
}
