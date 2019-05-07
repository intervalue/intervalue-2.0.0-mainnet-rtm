package one.inve.localfullnode2.store;

import com.alibaba.fastjson.JSON;
import one.inve.bean.message.SnapshotMessage;
import one.inve.core.EventBody;

import java.util.Map;

/**
 * 快照消息持久化逻辑处理类
 * @author Clare
 * @date   2018/11/2 0002.
 */
public class SnapshotDbServiceImpl implements SnapshotDbService {
//    private static final Logger logger = LoggerFactory.getLogger(SnapshotDbServiceImpl2.class);

    /**
     * 查询最新快照消息
     *
     * @param dbId 数据库ID
     * @return 最新快照消息
     */
    public SnapshotMessage queryLatestSnapshotMessage(String dbId) {
        String snapshotMessageStr = "{\"preHash\":\"32YuK4odYOmKj3tHKAhWw81a/A+0abndzn2020/1PrudslVQ1HP0" +
                "/szqBLN80RXtvTl4ZC6DwYDmNeo0hG22qQlQ==\",\"snapVersion\":318," +
                "\"signature\":\"33ALXfpA019Woj/fuuX02QK/irq7nfymjVuDAM+XP1" +
                "+mbmZSCeekoBeCol2DxtEKD3SvaMRC8nXerfTaSuc9db3qE=\",\"vers\":\"2.0\"," +
                "\"fromAddress\":\"NUOX47THDUFUT7Z6XPNN75YJYRJK2LVC\",\"type\":3," +
                "\"hash\":\"33ALXfpA019Woj/fuuX02QK/irq7nfymjVuDAM+XP1+mbmZSCeekoBeCol2DxtEKD3SvaMRC8nXerfTaSuc9db3qE" +
                "=\",\"pubkey\":\"AzZ6psaaBIcYeCEQZRlagF3sSDpYwjeqf1LdA/aTNCL/\"," +
                "\"snapshotPoint\":{\"contributions\":{\"TBFHMMQINYWJNDT74GJLNPF5JNROEWDG\":1734," +
                "\"JC3ULYLYIZXHY25EMBWWDMPSVCHPVX7T\":1743,\"KNR6NYNGWAEPAX7VYOI2KKXBTD5ABQDT\":1723," +
                "\"Q3K5BITCGSOYNB7LSNDPX6XM52HA7CEK\":1733,\"5LNNLSEQIIGAB5FP7YMBM2N5Q6ZQ4NBA\":1732," +
                "\"ZXDFYOU7EKSJZBV2ZM4J7MR466Q2KPAI\":1738,\"NUOX47THDUFUT7Z6XPNN75YJYRJK2LVC\":1729," +
                "\"73SRW2DPLN7YFTSIVVGHRCMDRO7G6HZQ\":1732,\"WQE2GM2BSEBH6MREP2Q4MDG7LDDL2NRY\":1737," +
                "\"UVTNWYRJUDBPV3GRL2DY7HKYLC6JNJDL\":1739},\"rewardRatio\":0.5,\"msgMaxId\":2627,\"totalFee\":0," +
                "\"msgHashTreeRoot\":\"YClht0S1KF+y5vKAwgKyCcnqztGBf3tlrWvwRBCm31OUHTv7BR5/p8P1iRcMFsbO\"," +
                "\"eventBody\":{\"generation\":24096409,\"isFamous\":false,\"otherId\":4,\"creatorSeq\":14575190," +
                "\"signature\":\"EXoOxkG2IRPOWIp6Bx9GcR++w/lrxby6qhHjBP0YWljFdLCwUfLi" +
                "+gKjtdyUJcaiXCKVRJ2AhshFjfpDJoNKzMbFtyFkXj8JMXo+6yMqzkR0uAsDXtvZ9xQPfP4NjP15tEgaglnHWRK91" +
                "+aE6WQAWE1RD7zDCu/7+k1Lcej2emc=\",\"transCount\":2627,\"creatorId\":6,\"shardId\":0," +
                "\"consEventCount\":144900000,\"consTimestamp\":\"2019-05-04T16:37:20.286Z\"," +
                "\"timeCreated\":\"2019-05-04T16:36:52.241Z\"," +
                "\"hash\":\"YClht0S1KF+y5vKAwgKyCcnqztGBf3tlrWvwRBCm31OUHTv7BR5/p8P1iRcMFsbO\"," +
                "\"otherSeq\":13384060}},\"timestamp\":1557200315096}";
        SnapshotMessage snapshotMessage = JSON.parseObject(snapshotMessageStr,SnapshotMessage.class);
        return snapshotMessage;
//        return null;
    }

    /**
     * 查询最新快照消息的hash值
     *
     * @param dbId 数据库ID
     * @return 最新快照消息的hash值
     */
    public String queryLatestSnapshotMessageHash(String dbId){
        return null;
    }

    /**
     * 根据快照消息hash值，查询对应的快照消息，并以字符串形式返回
     *
     * @param dbId 数据库ID
     * @param hash 快照消息hash值
     * @return 快照消息（以字符串形式返回）
     */
    public String querySnapshotMessageFormatStringByHash(String dbId, String hash) {
        return null;
    }

    /**
     * 根据快照消息hash值，查询对应的快照消息，并以实体对象形式返回
     *
     * @param dbId 数据库ID
     * @param hash 快照消息hash值
     * @return 快照消息（以实体对象形式返回）
     */
    public SnapshotMessage querySnapshotMessageByHash(String dbId, String hash) {
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
//        return null;
    }

    /**
     * 根据快照消息版本号，查询对应的快照消息，并以字符串形式返回
     *
     * @param dbId 数据库ID
     * @param version 快照消息版本号
     * @return 版本{version}快照消息，并以字符串形式返回
     */
    public String querySnapshotMessageFormatStringByVersion(String dbId, String version){
        return null;
    }

    /**
     * 根据快照消息hash值，查询对应的快照消息，并以实体对象形式返回
     *
     * @param dbId 数据库ID
     * @param version 快照消息版本号
     * @return 版本{version}快照消息（以实体对象形式返回）
     */
    public SnapshotMessage querySnapshotMessageByVersion(String dbId, String version) {
        return null;
    }

    /**
     * 根据快照消息版本号，查询对应的快照消息hash值
     *
     * @param dbId 数据库ID
     * @param version 快照消息版本号
     * @return 版本{version}快照消息hash值
     */
    public String querySnapshotMessageHashByVersion(String dbId, String version){
        return null;
    }

    /**
     * 删除快照点Event之前的所有Event
     *
     * @param dbId 数据库ID
     * @param eb 快照点Event
     * @param nValue 分片总节点数
     */
    public void deleteEventsBeforeSnapshotPointEvent(String dbId, EventBody eb, int nValue) {

    }


    /**
     * 递归获取每根柱子上离得最近的第一个event
     *
     * @param dbId 数据库ID
     * @param eb
     * @param map
     * @param nValue 分片总节点数
     * @return 每根柱子上离指定event最近的前一个event
     */
    public Map<Long, EventKeyPair> getPrevEventKeyPairsForEachNode(String dbId,
                                                                          EventBody eb,
                                                                          Map<Long, EventKeyPair> map,
                                                                          int nValue) {
        return null;
    }

    public static void main(String[] args) {
//        String dbId = "0_0";
//
//        String hash = queryLatestSnapshotMessageHash(dbId);
//        System.out.println("hash: " + hash);
//
//        SnapshotMessage sm0 = null;
//        do {
//            if (StringUtils.isEmpty(hash)) {
//                break;
//            }
//            sm0 = querySnapshotMessageByHash(dbId, hash);
//            if (null == sm0) {
//                break;
//            }
//            String root = sm0.getSnapshotPoint().getMsgHashTreeRoot();
//            BigInteger version = sm0.getSnapVersion();
//            System.out.println("\nversion: " + version + ", tree root : " + root);
//            System.out.println("snapshotPoint : " + JSON.toJSONString(sm0.getSnapshotPoint()));
//
//            EventBody eb = sm0.getSnapshotPoint().getEventBody();
//            int sharId      = eb.getShardId();
//            Long creatId    = eb.getCreatorId();
//            Long creatSeq   = eb.getCreatorSeq();
//            EventKeyPair keyPair = new EventKeyPair(sharId, creatId, creatSeq);
//            System.out.println("keyPair: " + keyPair + ", transCount: " + eb.getTransCount());
//            RocksJavaUtil rocksJavaUtil = new RocksJavaUtil(dbId);
//            byte[] keyPairByte = rocksJavaUtil.get(keyPair.toString());
//            if (keyPairByte != null) {
//                EventBody ebWhole = JSON.parseObject(new String(keyPairByte), EventBody.class);
//                byte[][] trans = ebWhole.getTrans();
//                if (null!=trans && trans.length>0) {
//                    String msg = new String(trans[trans.length-1]);
//                    System.out.println("msg: " + msg);
//                } else {
//                    System.out.println("msg: null");
//                }
//            }
//
//            String eHash = sm0.geteHash();
//            EventBody eb1 = JSON.parseObject(new String(keyPairByte), EventBody.class);
//            int sharId1      = eb1.getShardId();
//            Long creatId1    = eb1.getCreatorId();
//            Long creatSeq1   = eb1.getCreatorSeq();
//            EventKeyPair keyPair1 = new EventKeyPair(sharId1, creatId1, creatSeq1);
//            System.out.println("eHash: " + eHash + ", keyPair: " + keyPair1 + ", transCount: " + eb1.getTransCount());
//
//
//            hash = sm0.getPreHash();
//        } while (StringUtils.isNotEmpty(sm0.getPreHash()));
//
//        for (int i = 0; i < 2; i++) {
//            SnapshotMessage sm = querySnapshotMessageByHash(dbId, hash);
//            String root = sm.getSnapshotPoint().getMsgHashTreeRoot();
//            BigInteger version = sm.getSnapVersion();
//            System.out.println("version: " + version + ", tree root : " + root);
//
//            hash = sm.getPreHash();
//        }
//
//        String hash7 = querySnapshotMessageHashByVersion(dbId, "2");
//        System.out.println("\nhash7: \n" + hash7);
//
//        SnapshotMessage sm = queryLatestSnapshotMessage(dbId);
//        System.out.println("\nsm: \n" + JSON.toJSONString(sm));
//
//        BigInteger snapVersion = sm.getSnapVersion();
//        System.out.println("\nsnapVersion: \n" + snapVersion);
//        String preHash = sm.getPreHash();
//        System.out.println("\npreHash: \n" + preHash);
//        String snapHash = sm.getHash();
//        System.out.println("\nsnapHash: \n" + snapHash);
//        String eHash = sm.geteHash();
//        System.out.println("\neHash: \n" + eHash);
//        SnapshotPoint sp = sm.getSnapshotPoint();
//        System.out.println("\nsp: \n" + JSON.toJSONString(sp));
//        EventBody eb = sp.getEventBody();
//        System.out.println("\neb: \n" + JSON.toJSONString(eb));
//
//        SnapshotMessage sm1 = querySnapshotMessageByHash(dbId, snapHash);
//        System.out.println("\nsp: \n" + JSON.toJSONString(sm1 != null ? sm1.getSnapshotPoint() : null));
//
//        SnapshotMessage sm2 = querySnapshotMessageByVersion(dbId, "7");
//        System.out.println("\nsp: \n" + JSON.toJSONString(sm2 != null ? sm2.getSnapshotPoint() : null));
//
//        String snapmsg = querySnapshotMessageFormatStringByHash(dbId, snapHash);
//        System.out.println("\nsnap msg: \n" + snapmsg);
//
//        String snapmsg1 = querySnapshotMessageFormatStringByVersion(dbId, "7");
//        System.out.println("\nsnap msg1: \n" + snapmsg1);
    }

}
