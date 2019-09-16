package one.inve.localfullnode2.sync.partofwork;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

import one.inve.localfullnode2.store.rocks.RocksJavaUtil;
import one.inve.localfullnode2.store.rocks.key.MessageIndexes;
import one.inve.localfullnode2.sync.DistributedObjects;
import one.inve.localfullnode2.sync.ISyncContext;
import one.inve.localfullnode2.sync.SyncWorksInLab.BasedIterativePart;
import one.inve.localfullnode2.sync.measure.ChunkDistribution;
import one.inve.localfullnode2.sync.source.ISyncSource;

/**
 * 
 * Copyright © INVE FOUNDATION. All rights reserved.
 * 
 * @ClassName: SystemMessageIterativePart
 * @Description: sysmessage synchronizer
 * @author Francis.Deng [francis_xiiiv@163.com]
 * @date Sep 7, 2019
 *
 */
public class SystemMessageIterativePart extends BasedIterativePart {
	private static final Logger logger = LoggerFactory.getLogger(SystemMessageIterativePart.class);

	@Override
	public void runOnce(ISyncContext context) {
//		Distribution myDist = context.getDistribution();
//		ISyncSource synSource = context.getSyncSourceProxy();
//
//		ILFN2Profile srcProfile = getSourceProfile(context);
//
//		DistributedObjects<JSONObject> distributedObjects = synSource.getNotInDistributionMessages(myDist);
//		if (distributedObjects.getObjects() == null || distributedObjects.getObjects().length == 0) {
//			done = true;
//			return;
//		}
//		// int length = distributedObjects.getObjects().length;
//
//		// handle the batch of Messges
//		MessagePersistence messagePersistence = new MessagePersistence(new MessagePersistenceDependent() {
//
//			@Override
//			public BlockingQueue<JSONObject> getConsMessageSaveQueue() {
//				return null;
//			}
//
//			@Override
//			public void setConsMessageCount(BigInteger consMessageCount) {
//				return;
//			}
//
//			@Override
//			public BigInteger getConsMessageCount() {
//				return BigInteger.ZERO;
//			}
//
//			@Override
//			public String getDbId() {
//				return srcProfile.getDBId();
//			}
//
//			@Override
//			public INosql getNosql() {
//				return new RocksJavaUtil(srcProfile.getDBId());
//			}
//
//			@Override
//			public BlockingQueue<JSONObject> getSystemAutoTxSaveQueue() {
//				StagingArea stagingArea = new StagingArea();
//				stagingArea.createQueue(EventBody.class, StagingArea.SystemAutoTxSaveQueueName, 10000000, null);
//
//				BlockingQueue<JSONObject> q = stagingArea.getQueue(JSONObject.class,
//						StagingArea.SystemAutoTxSaveQueueName);
//				for (JSONObject jsonizedMessage : distributedObjects.getObjects()) {
//					q.add(jsonizedMessage);
//				}
//
//				return q;
//			}
//
//		});
//		messagePersistence.persisMessages();
//
//		context.join(distributedObjects.getDist());

		ChunkDistribution<String> myDist = context.getMessageDistribution();
		ISyncSource synSource = context.getSyncSourceProxy();

		// ILFN2Profile srcProfile = getSourceProfile(context);

		DistributedObjects<ChunkDistribution<String>, String> distributedObjects = synSource
				.getNotInDistributionSysMessages(myDist);

		if (distributedObjects.getObjects() != null && distributedObjects.getObjects().length >= 0) {
			RocksJavaUtil rocksJavaUtil = new RocksJavaUtil(context.getProfile().getDBId());
			for (String sysMessageJson : distributedObjects.getObjects()) {
				JSONObject sysAutoTx = JSON.parseObject(sysMessageJson, JSONObject.class);

				String id = sysAutoTx.getString("id");
				String type = sysAutoTx.getString("type");
				String typedId = type + id;

				rocksJavaUtil.put(typedId, JSONArray.toJSONString(sysAutoTx));
				// rocksJavaUtil.put(Config.SYS_TX_COUNT_KEY, id);

				rocksJavaUtil.put(MessageIndexes.getSysMessageTypeIdKey(typedId), new byte[0]);

				logger.info("persistence sysMessage keys : {}", typedId);
			}
		}

		if (!context.joinMessageDistribution(distributedObjects.getDist())) {
			done = true;
		}

	}

}
