package one.inve.localfullnode2.snapshot;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import one.inve.bean.message.SnapshotMessage;
import one.inve.bean.message.SnapshotPoint;
import one.inve.localfullnode2.conf.Config;
import one.inve.utils.DSA;

import java.math.BigInteger;

public class HandleSnapshotPointMessage {

    private HandleSnapshotPointMessageDependent dep;
    private BigInteger vers;
    private JSONObject msgObject;


    public void handleConsensusEmptyMessage(HandleSnapshotPointMessageDependent dep) throws Exception{
        this.dep = dep;
        this.vers = dep.getCurrSnapshotVersion();
        this.msgObject = dep.getMsgObject();

        Boolean lastIdx = msgObject.getBoolean("lastIdx");
        String eHash = msgObject.getString("eHash");
        SnapshotPoint sp = dep.getSnapshotPointMap().get(vers);
//        logger.warn("\nspecif msg: {}, \nvers-{} sp: {}",
//                msgObject.toJSONString(), vers, JSON.toJSONString(sp));
        if (sp == null) {
//            logger.error("node-({}, {}): snapshotPoint-{} missing\nexit...",
//                    node.getShardId(), node.getCreatorId(), vers);
            System.exit(-1);
        } else {
            if (null!=lastIdx && lastIdx
                    && eHash.equals(DSA.encryptBASE64(sp.getEventBody().getHash()))) {
                // 快照点event时，判断是否基金会节点，然后生成快照
                handleSnapshotPoint(msgObject.getBigInteger("id"));
            } else {
//                logger.warn("node-({}, {}): unknown message: {}",
//                        node.getShardId(), node.getCreatorId(), msgObject.toJSONString());
            }
        }
    }

    private void handleSnapshotPoint(BigInteger maxMessageId) throws Exception {
        // 快照事件，则生成快照消息并丢入队列
        createSnapshotMessage(dep.getSnapshotPointMap().get(vers), maxMessageId);
        // 恢复参数状态
        dep.getSnapshotPointMap().remove(vers);
        dep.setTotalFeeBetween2Snapshots(BigInteger.ZERO);

//        logger.info("node-({}, {}): handle snapshotPoint-{} finished!", node.getShardId(), node.getCreatorId(), vers);
        vers = vers.add(BigInteger.ONE);
    }

    private void createSnapshotMessage(SnapshotPoint snapshotPoint, BigInteger maxMessageId) throws Exception {
//        logger.info("====== node-({}, {}): createSnapshotMessage...", node.getShardId(), node.getCreatorId());
        // 快照消息丢入队列
        if ( Config.FOUNDATION_PUBKEY.equals(dep.getPubKey()) ) {
            BigInteger totalFee = dep.getTotalFeeBetween2Snapshots();
            snapshotPoint.setMsgMaxId(maxMessageId);
            snapshotPoint.setTotalFee(totalFee);
            snapshotPoint.setRewardRatio(Config.NODE_REWARD_RATIO);
            // 构造快照消息
            SnapshotMessage snapshotMessage = new SnapshotMessage(
                    dep.getMnemonic(), dep.getAddress(),
                    vers, getPreHash(), snapshotPoint);

            System.out.println(JSON.toJSONString(snapshotMessage));

            // 加入消息队列
            String msg = snapshotMessage.getMessage();
//            logger.info("node-({}, {}): new version-{}, snapshotMsg: {}",
//                    node.getShardId(), node.getCreatorId(), vers, msg);

            dep.getMessageQueue().add(JSON.parseObject(msg).getString("message").getBytes());

            System.out.println(JSON.toJSONString(JSON.parseObject(msg).getString("message")));
        } else {
//            logger.warn("node-({}, {}): new version-{}, no permission!!",
//                    node.getShardId(), node.getCreatorId(), vers);
        }
    }

    private String getPreHash() {
        SnapshotMessage preSnapshotMessage = dep.getSnapshotMessage();
        String preHash;
        if (null == preSnapshotMessage) {
//            logger.warn("\n====== node-({}, {}): preSnapshotMessage: null", node.getShardId(), node.getCreatorId());
            preHash = null;
        } else if (null == preSnapshotMessage.getPreHash()) {
//            logger.warn(
//                    "\n====== node-({}, {}): preSnapshotMessage.version: {}, preSnapshotMessage.snapHash: {}, " +
//                    "preSnapshotMessage.getPreHash(): {}",
//                    node.getShardId(), node.getCreatorId(), preSnapshotMessage.getSnapVersion(),
//                    preSnapshotMessage.getHash(),
//                    preSnapshotMessage.getPreHash());
            preHash = preSnapshotMessage.getHash();
        } else {
            preHash = preSnapshotMessage.getHash();
        }
        return preHash;
    }

    public static void main(String[] args) {
        HandleSnapshotPointMessageDependent dep = new HandleSnapshotPointMessageDependentImpl();
        try {
            new HandleSnapshotPointMessage().handleConsensusEmptyMessage(dep);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
