package one.inve.localfullnode2.sync.partofwork;

import java.math.BigInteger;
import java.util.concurrent.BlockingQueue;

import com.alibaba.fastjson.JSONObject;

import one.inve.core.EventBody;
import one.inve.localfullnode2.message.MessagePersistence;
import one.inve.localfullnode2.message.MessagePersistenceDependent;
import one.inve.localfullnode2.staging.StagingArea;
import one.inve.localfullnode2.store.rocks.INosql;
import one.inve.localfullnode2.store.rocks.RocksJavaUtil;
import one.inve.localfullnode2.sync.DistributedObjects;
import one.inve.localfullnode2.sync.ISyncContext;
import one.inve.localfullnode2.sync.SynchronizationWork.BasedIterativePart;
import one.inve.localfullnode2.sync.measure.Distribution;
import one.inve.localfullnode2.sync.source.ISyncSource;
import one.inve.localfullnode2.sync.source.ILFN2Profile;

public class SystemMessageIterativePart extends BasedIterativePart {

	@Override
	public void runOnce(ISyncContext context) {
		Distribution myDist = context.getDistribution();
		ISyncSource synSource = context.getSyncSourceProxy();

		ILFN2Profile srcProfile = getSourceProfile(context);

		DistributedObjects<JSONObject> distributedObjects = synSource.getNotInDistributionMessages(myDist);
		if (distributedObjects.getObjects() == null || distributedObjects.getObjects().length == 0) {
			done = true;
			return;
		}
		// int length = distributedObjects.getObjects().length;

		// handle the batch of Messges
		MessagePersistence messagePersistence = new MessagePersistence(new MessagePersistenceDependent() {

			@Override
			public BlockingQueue<JSONObject> getConsMessageSaveQueue() {
				return null;
			}

			@Override
			public void setConsMessageCount(BigInteger consMessageCount) {
				return;
			}

			@Override
			public BigInteger getConsMessageCount() {
				return BigInteger.ZERO;
			}

			@Override
			public String getDbId() {
				return srcProfile.getDBId();
			}

			@Override
			public INosql getNosql() {
				return new RocksJavaUtil(srcProfile.getDBId());
			}

			@Override
			public BlockingQueue<JSONObject> getSystemAutoTxSaveQueue() {
				StagingArea stagingArea = new StagingArea();
				stagingArea.createQueue(EventBody.class, StagingArea.SystemAutoTxSaveQueueName, 10000000, null);

				BlockingQueue<JSONObject> q = stagingArea.getQueue(JSONObject.class,
						StagingArea.SystemAutoTxSaveQueueName);
				for (JSONObject jsonizedMessage : distributedObjects.getObjects()) {
					q.add(jsonizedMessage);
				}

				return q;
			}

		});
		messagePersistence.persisMessages();

		context.joinDistribution(distributedObjects.getDist());

	}

}
