package one.inve.localfullnode2.sync.partofwork;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alibaba.fastjson.JSONObject;

import one.inve.core.EventBody;
import one.inve.localfullnode2.dep.DepItemsManager;
import one.inve.localfullnode2.dep.DepItemsManagerial;
import one.inve.localfullnode2.dep.items.AllQueues;
import one.inve.localfullnode2.message.MessagesExe;
import one.inve.localfullnode2.message.MessagesExeDependency;
import one.inve.localfullnode2.message.MessagesVerification;
import one.inve.localfullnode2.message.MessagesVerificationDependency;
import one.inve.localfullnode2.postconsensus.exe.EventsExe;
import one.inve.localfullnode2.postconsensus.exe.EventsExeDependency;
import one.inve.localfullnode2.staging.StagingArea;
import one.inve.localfullnode2.sync.ISyncContext;
import one.inve.localfullnode2.sync.source.ILFN2Profile;

/**
 * 
 * Copyright © INVE FOUNDATION. All rights reserved.
 * 
 * @ClassName: NativeEventsLoaderConsumer
 * @Description: mainly handle the events process without message part.If there
 *               are 30 seconds left to do nothing,the process is finished.
 * @author Francis.Deng [francis_xiiiv@163.com]
 * @date Sep 9, 2019
 *
 */
public class NativeEventsLoaderConsumer extends NativeEventsLoader {
	private static final Logger logger = LoggerFactory.getLogger(NativeEventsLoaderConsumer.class);

	private DepItemsManagerial depItemsManager = DepItemsManager.getInstance();

	public void consumeEvents(ISyncContext context, BlockingQueue<EventBody> queue) {
		loadDeps();

		try {
			EventBody eb = null;
			while ((eb = queue.poll(2, TimeUnit.MINUTES)) != null) {
				logger.info("executing event in shard,seq : {},{}", eb.getCreatorId(), eb.getCreatorSeq());

				executeEvent(eb);
			}
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	protected void executeEvent(EventBody eb) {
		AllQueues allQueues = DepItemsManager.getInstance().attachAllQueues(null);
		StagingArea stagingArea = allQueues.get();

		BlockingQueue<EventBody> q0 = stagingArea.getQueue(EventBody.class, StagingArea.ConsEventHandleQueueName);
		q0.offer(eb);

		EventsExeDependency eventsExeDependency = depItemsManager.getItemConcerned(EventsExeDependency.class);
		EventsExe eventsExe = new EventsExe(eventsExeDependency);
		eventsExe.run();

		// seventh,verify all messages
		MessagesVerificationDependency messagesVerificationDependency = depItemsManager
				.getItemConcerned(MessagesVerificationDependency.class);
		MessagesVerification messagesVerification = new MessagesVerification(messagesVerificationDependency);
		messagesVerification.verifyMessages();

		// eighth,execute all messages
		MessagesExeDependency messagesExeDependency = depItemsManager.getItemConcerned(MessagesExeDependency.class);
		MessagesExe messagesExe = new MessagesExe(messagesExeDependency);
		messagesExe.exe();

		BlockingQueue<JSONObject> q1 = stagingArea.getQueue(JSONObject.class, StagingArea.ConsMessageSaveQueueName);
		q1.poll();

	}

	protected void initValues(ISyncContext context) {
		ILFN2Profile profile = context.getProfile();
		depItemsManager.attachCreatorId(null).set(profile.getCreatorId());
		depItemsManager.attachDBId(null).set(profile.getDBId());
		depItemsManager.attachShardCount(null).set(profile.getShardCount());
	}

	protected void loadDeps() {
		EventsExeDependency eventsExeDependency = new EventsExeDependency();
		of(eventsExeDependency);

		MessagesVerificationDependency messagesVerificationDependency = new MessagesVerificationDependency();
		of(messagesVerificationDependency);

		MessagesExeDependency messagesExeDependency = new MessagesExeDependency();
		of(messagesExeDependency);

		buildStagingArea();
	}

	protected void of(EventsExeDependency eventsExeDependency) {
		depItemsManager.attachCreatorId(eventsExeDependency);
		depItemsManager.attachStat(eventsExeDependency);
		depItemsManager.attachDBId(eventsExeDependency);
		depItemsManager.attachAllQueues(eventsExeDependency);
		depItemsManager.attachSS(eventsExeDependency);
	}

	protected void of(MessagesVerificationDependency messagesVerificationDependency) {
		DepItemsManager.getInstance().attachAllQueues(messagesVerificationDependency);
	}

	protected void of(MessagesExeDependency messagesExeDependency) {
		DepItemsManager.getInstance().attachAllQueues(messagesExeDependency);
		DepItemsManager.getInstance().attachShardCount(messagesExeDependency);
		DepItemsManager.getInstance().attachDBId(messagesExeDependency);
		DepItemsManager.getInstance().attachStat(messagesExeDependency);
		DepItemsManager.getInstance().attachSS(messagesExeDependency);
	}

	protected void buildStagingArea() {
		AllQueues allQueues = DepItemsManager.getInstance().attachAllQueues(null);
		StagingArea stagingArea = new StagingArea();

		// stagingArea.createQueue(byte[].class, StagingArea.MessageQueueName, 10000000,
		// (e, id, op) -> info(id, new String((byte[]) e), op));
		// stagingArea.createQueue(EventBody.class, StagingArea.EventSaveQueueName,
		// 10000000, null);
		stagingArea.createQueue(EventBody.class, StagingArea.ConsEventHandleQueueName, 1000, null);
		stagingArea.createQueue(JSONObject.class, StagingArea.ConsMessageVerifyQueueName, 1000,
				(e, id, op) -> info(id, ((JSONObject) e).toJSONString(), op));
		stagingArea.createQueue(JSONObject.class, StagingArea.ConsMessageHandleQueueName, 1000,
				(e, id, op) -> info(id, ((JSONObject) e).toJSONString(), op));
		stagingArea.createQueue(JSONObject.class, StagingArea.ConsMessageSaveQueueName, 1000,
				(e, id, op) -> info(id, ((JSONObject) e).toJSONString(), op));
//		stagingArea.createQueue(JSONObject.class, StagingArea.SystemAutoTxSaveQueueName, 10000000,
//				(e, id, op) -> info(id, ((JSONObject) e).toJSONString(), op));

		allQueues.set(stagingArea);
	}

	protected void info(String id, String e, String op) {
		logger.info("DSPTVM - MessageTracker     - queue   : {}", id);
		logger.info("DSPTVM -   MessageTracker   - op      : {}", op);
		logger.info("DSPTVM -     MessageTracker - element : {}", e);

		int i = 1;
	}
}
