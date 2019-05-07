package one.inve.localfullnode2.snapshot;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import one.inve.bean.message.SnapshotMessage;
import one.inve.bean.message.SnapshotPoint;
import java.math.BigInteger;
import java.util.HashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

public class HandleSnapshotPointMessageDependentImpl implements HandleSnapshotPointMessageDependent{

    @Override
    public BigInteger getCurrSnapshotVersion() {
        return new BigInteger("318");
    }

    HashMap<BigInteger, SnapshotPoint> snapshotPointMap = new HashMap<BigInteger, SnapshotPoint>();
    @Override
    public HashMap<BigInteger, SnapshotPoint> getSnapshotPointMap() {
        String snapshotPointStr = "{\"contributions\":{\"TBFHMMQINYWJNDT74GJLNPF5JNROEWDG\":1734," +
                "\"JC3ULYLYIZXHY25EMBWWDMPSVCHPVX7T\":1743,\"KNR6NYNGWAEPAX7VYOI2KKXBTD5ABQDT\":1723," +
                "\"Q3K5BITCGSOYNB7LSNDPX6XM52HA7CEK\":1733,\"5LNNLSEQIIGAB5FP7YMBM2N5Q6ZQ4NBA\":1732," +
                "\"ZXDFYOU7EKSJZBV2ZM4J7MR466Q2KPAI\":1738,\"NUOX47THDUFUT7Z6XPNN75YJYRJK2LVC\":1729," +
                "\"73SRW2DPLN7YFTSIVVGHRCMDRO7G6HZQ\":1732,\"WQE2GM2BSEBH6MREP2Q4MDG7LDDL2NRY\":1737," +
                "\"UVTNWYRJUDBPV3GRL2DY7HKYLC6JNJDL\":1739},\"eventBody\":{\"consEventCount\":144900000," +
                "\"consTimestamp\":\"2019-05-04T16:37:20.286Z\",\"creatorId\":6,\"creatorSeq\":14575190," +
                "\"generation\":24096409,\"hash\":\"YClht0S1KF+y5vKAwgKyCcnqztGBf3tlrWvwRBCm31OUHTv7BR5/p8P1iRcMFsbO" +
                "\",\"isFamous\":false,\"otherId\":4,\"otherSeq\":13384060,\"shardId\":0," +
                "\"signature\":\"EXoOxkG2IRPOWIp6Bx9GcR++w/lrxby6qhHjBP0YWljFdLCwUfLi" +
                "+gKjtdyUJcaiXCKVRJ2AhshFjfpDJoNKzMbFtyFkXj8JMXo+6yMqzkR0uAsDXtvZ9xQPfP4NjP15tEgaglnHWRK91" +
                "+aE6WQAWE1RD7zDCu/7+k1Lcej2emc=\",\"timeCreated\":\"2019-05-04T16:36:52.241Z\",\"transCount\":2627}," +
                "\"msgHashTreeRoot\":\"YClht0S1KF+y5vKAwgKyCcnqztGBf3tlrWvwRBCm31OUHTv7BR5/p8P1iRcMFsbO\"," +
                "\"rewardRatio\":0}";
        SnapshotPoint snapshotPoint = JSON.parseObject(snapshotPointStr,SnapshotPoint.class);
        snapshotPointMap.put(getCurrSnapshotVersion(),snapshotPoint);
        return this.snapshotPointMap;
    }

    JSONObject msgObject = new JSONObject();
    @Override
    public JSONObject getMsgObject() {
        msgObject = JSON.parseObject("{\"eHash\":\"YClht0S1KF+y5vKAwgKyCcnqztGBf3tlrWvwRBCm31OUHTv7BR5" +
                "/p8P1iRcMFsbO\",\"lastIdx\":true,\"id\":2627}");
        return this.msgObject;
    }

    @Override
    public void setTotalFeeBetween2Snapshots(BigInteger totalFeeBetween2Snapshots) {

    }

    @Override
    public SnapshotMessage getSnapshotMessage() {
        String snapshotMessageStr = "{\"preHash\":\"32XpYVyRvRJ/Vb5Xjo7tTqplD/L" +
                "+BUpgh62xqWZOP1YNFAMyx0boLkrtFDYu8g4rSbcb9+uHmg0XXFdAGpTF3WDA==\",\"snapVersion\":317," +
                "\"signature\":\"32YuK4odYOmKj3tHKAhWw81a/A+0abndzn2020/1PrudslVQ1HP0" +
                "/szqBLN80RXtvTl4ZC6DwYDmNeo0hG22qQlQ==\",\"vers\":\"2.0\"," +
                "\"fromAddress\":\"NUOX47THDUFUT7Z6XPNN75YJYRJK2LVC\",\"type\":3," +
                "\"hash\":\"32YuK4odYOmKj3tHKAhWw81a/A+0abndzn2020/1PrudslVQ1HP0/szqBLN80RXtvTl4ZC6DwYDmNeo0hG22qQlQ" +
                "==\",\"pubkey\":\"AzZ6psaaBIcYeCEQZRlagF3sSDpYwjeqf1LdA/aTNCL/\"," +
                "\"snapshotPoint\":{\"contributions\":{\"TBFHMMQINYWJNDT74GJLNPF5JNROEWDG\":4938," +
                "\"JC3ULYLYIZXHY25EMBWWDMPSVCHPVX7T\":4967,\"KNR6NYNGWAEPAX7VYOI2KKXBTD5ABQDT\":4950," +
                "\"Q3K5BITCGSOYNB7LSNDPX6XM52HA7CEK\":4976,\"5LNNLSEQIIGAB5FP7YMBM2N5Q6ZQ4NBA\":4971," +
                "\"ZXDFYOU7EKSJZBV2ZM4J7MR466Q2KPAI\":4950,\"NUOX47THDUFUT7Z6XPNN75YJYRJK2LVC\":4975," +
                "\"73SRW2DPLN7YFTSIVVGHRCMDRO7G6HZQ\":4953,\"WQE2GM2BSEBH6MREP2Q4MDG7LDDL2NRY\":4965," +
                "\"UVTNWYRJUDBPV3GRL2DY7HKYLC6JNJDL\":4980},\"eventBody\":{\"consEventCount\":144600000," +
                "\"consTimestamp\":\"2019-05-01T00:36:04.585Z\",\"creatorId\":6,\"creatorSeq\":14544726," +
                "\"generation\":24028809,\"hash\":\"ONTyVaBdXWnxbt5UFtenLzliaXM1dMKkBwcCyISaSVd2ipBacOC3VyEymjMmexF" +
                "/\",\"isFamous\":false,\"otherId\":7,\"otherSeq\":14487822,\"shardId\":0," +
                "\"signature\":\"fvy3MvBrKKQcjjQw9bulkyo0Nnj054euRtNI3pwYUOEixJmZ4ar2rq5gwHF6oqakVIVhZorKV6AeUu" +
                "+YUoRuyXbNoHI0a9AXkPpMd+NXAbW5986jyKEFe5vU0C8MTPX3lovNxnOWMRsNXShW6lLeNPWPAQAX9C5wLQlxHvuYEBM=\"," +
                "\"timeCreated\":\"2019-05-01T00:35:44.737Z\",\"transCount\":2573}," +
                "\"msgHashTreeRoot\":\"DC0kO/HMe8QDuhh7+MQBoXZ2oMeAw+zHHmMx6SR9YMT0VOgMhASAC3btEPTJGo3y\"," +
                "\"msgMaxId\":2573,\"rewardRatio\":0.5,\"totalFee\":3000000000000000},\"timestamp\":1556671162065}";
        SnapshotMessage snapshotMessage = JSON.parseObject(snapshotMessageStr,SnapshotMessage.class);
        return snapshotMessage;
    }

    @Override
    public BigInteger getTotalFeeBetween2Snapshots() {
        return BigInteger.ZERO;
    }

    @Override
    public String getPubKey() {
        return "AzZ6psaaBIcYeCEQZRlagF3sSDpYwjeqf1LdA/aTNCL/";
    }

    @Override
    public String getMnemonic() {
        return "wheat monster sad area matrix ostrich alcohol boost sort parrot kiwi virtual";
    }

    @Override
    public String getAddress() {
        return "NUOX47THDUFUT7Z6XPNN75YJYRJK2LVC";
    }

    @Override
    public ConcurrentLinkedQueue<byte[]> getMessageQueue() {
        return new ConcurrentLinkedQueue<byte[]>();
    }
}
