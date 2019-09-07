package one.inve.localfullnode2.sync.partofwork;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alibaba.fastjson.JSON;

import one.inve.localfullnode2.store.rocks.Message;
import one.inve.localfullnode2.store.rocks.RocksJavaUtil;
import one.inve.localfullnode2.store.rocks.key.MessageIndexes;
import one.inve.localfullnode2.sync.DistributedObjects;
import one.inve.localfullnode2.sync.ISyncContext;
import one.inve.localfullnode2.sync.SyncWorksInLab.BasedIterativePart;
import one.inve.localfullnode2.sync.measure.ChunkDistribution;
import one.inve.localfullnode2.sync.source.ILFN2Profile;
import one.inve.localfullnode2.sync.source.ISyncSource;

/**
 * 
 * Copyright © INVE FOUNDATION. All rights reserved.
 * 
 * @ClassName: MessageSynchronizer
 * @Description: message synchronizer
 * @author Francis.Deng [francis_xiiiv@163.com]
 * @date Aug 24, 2019
 *
 */
public class MessageIterativePart extends BasedIterativePart {
	private static final Logger logger = LoggerFactory.getLogger(MessageIterativePart.class);

	@Override
	public void runOnce(ISyncContext context) {
		ChunkDistribution<String> myDist = context.getMessageDistribution();
		ISyncSource synSource = context.getSyncSourceProxy();

		ILFN2Profile srcProfile = getSourceProfile(context);

		DistributedObjects<ChunkDistribution<String>, String> distributedObjects = synSource
				.getNotInDistributionMessages(myDist);
//		if (distributedObjects.getObjects() == null && distributedObjects.getObjects().length == 0) {
//			done = true;
//			return;
//		}

		// handle the batch of Messges
//		MessagePersistence messagePersistence = new MessagePersistence(new MessagePersistenceDependent() {
//
//			@Override
//			public BlockingQueue<JSONObject> getConsMessageSaveQueue() {
//				StagingArea stagingArea = new StagingArea();
//				stagingArea.createQueue(EventBody.class, StagingArea.ConsMessageSaveQueueName, 10000000, null);
//
//				BlockingQueue<JSONObject> q = stagingArea.getQueue(JSONObject.class,
//						StagingArea.ConsMessageSaveQueueName);
//				for (String json : distributedObjects.getObjects()) {
//					q.add(jsonizedMessage);
//				}
//
//				return q;
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
//				return null;
//			}
//
//		});
//		messagePersistence.persisMessages();

		if (distributedObjects.getObjects() != null && distributedObjects.getObjects().length >= 0) {
			RocksJavaUtil rocksJavaUtil = new RocksJavaUtil(context.getProfile().getDBId());
			for (String messageJson : distributedObjects.getObjects()) {
				Message message = JSON.parseObject(messageJson, Message.class);

				rocksJavaUtil.put(message.getHash(), JSON.toJSONString(messageJson));
				rocksJavaUtil.put(MessageIndexes.getMessageHashKey(message.getHash()).getBytes(), new byte[0]);

				logger.info("persistence keys : {}", message.getHash());
			}
		}

		if (!context.joinMessageDistribution(distributedObjects.getDist())) {
			done = true;
		}
	}

}
