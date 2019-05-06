package one.inve.localfullnode2.snapshot;

import java.math.BigInteger;
import java.security.PublicKey;
import java.util.List;
import java.util.concurrent.BlockingQueue;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

import one.inve.bean.message.SnapshotMessage;
import one.inve.localfullnode2.snapshot.vo.SnapObj;
import one.inve.localfullnode2.utilities.StringUtils;
import one.inve.utils.SignUtil;

/**
 * 
 * Copyright © CHXX Co.,Ltd. All rights reserved.
 * 
 * @Description: snapshot sync dependent definition
 * @author: Francis.Deng
 * @date: May 6, 2019 1:45:36 AM
 * @version: V1.0
 */
public interface SnapshotSynchronizerDependent {
	static final Logger logger = LoggerFactory.getLogger("SnapshotSynchronizerDependent");

	BigInteger getCurrSnapshotVersion();

	int getShardId();

	int getShardCount();

	int getnValue();

	BigInteger getConsMessageMaxId();

	SnapshotSyncConsumable getSnapshotSync();

	PublicKey getPublicKey();

	BlockingQueue<JSONObject> getConsMessageVerifyQueue();

	// refresh the node info
	void refresh(SnapshotMessage syncedSnapshotMessage);

	default boolean execute(SnapObj snapObj) {
		if (snapObj != null) {
			logger.warn("snapObj:{}", JSONObject.toJSONString(snapObj));
			String snapMessageStr = snapObj.snapMessage;
			String originalSnapshotStr = JSON.parseObject(snapMessageStr).getString("message");
			logger.warn("snapMessageStr:{}", snapMessageStr);
			SnapshotMessage snapshotMessage = JSONObject.parseObject(originalSnapshotStr, SnapshotMessage.class);
			String MsgHashTreeRoot = snapshotMessage.getSnapshotPoint().getMsgHashTreeRoot();
			if (StringUtils.isEmpty(MsgHashTreeRoot)) {
				// return eventSize + "_" + eventSpaces;
				return false;
			}
			List<JSONObject> messages = null;
			if (!StringUtils.isEmpty(snapObj.messages)) {
				messages = JSONArray.parseArray(snapObj.messages, JSONObject.class);
			}

			// 正在快照后
			if (SignUtil.verify(originalSnapshotStr)) {
				// transaction入库
				if (messages != null) {
					for (JSONObject msg : messages) {
						try {
							logger.error(">>>>>each of messages in GossipEventThread= " + msg);

							getConsMessageVerifyQueue().put(msg);
						} catch (InterruptedException e) {
							e.printStackTrace();
						}
					}
				}
				try {
					logger.error("node.getConsMessageVerifyQueue().put(JSONObject.parseObject(snapMessageStr))"
							+ snapMessageStr);

					getConsMessageVerifyQueue().put(JSONObject.parseObject(snapMessageStr));
				} catch (InterruptedException e) {
					logger.error("", e);
				}
				// 更新本节点当前快照信息
//				node.setSnapshotMessage(snapshotMessage);
//				node.getSnapshotPointMap().put(snapshotMessage.getSnapVersion(),
//						snapshotMessage.getSnapshotPoint());
//				node.getTreeRootMap().put(snapshotMessage.getSnapVersion(),
//						snapshotMessage.getSnapshotPoint().getMsgHashTreeRoot());
				refresh(snapshotMessage);
				return true;

			} else {
				// return eventSize + "_" + eventSpaces;
				return false;
			}
		}

		return false;
	}

//	GossipObj getGossipObj();	

//	HashMap<BigInteger, SnapshotPoint> getSnapshotPointMap();
//
//	void setSnapshotMessage(SnapshotMessage snapshotMessage);
//
//	HashMap<BigInteger, String> getTreeRootMap();
//
//	Map<String, HashSet<String>> getSnapVersionMap();

//	Member getNeighbor();

}
