package one.inve.localfullnode2.nodes;

import java.math.BigInteger;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alibaba.fastjson.JSONObject;
import com.zeroc.Ice.Communicator;

import one.inve.bean.message.SnapshotMessage;
import one.inve.bean.node.LocalFullNode;
import one.inve.cluster.Member;
import one.inve.core.EventBody;
import one.inve.localfullnode2.dep.DepItemsManager;
import one.inve.localfullnode2.dep.DepItemsManagerial;
import one.inve.localfullnode2.dep.items.AllQueues;
import one.inve.localfullnode2.gossip.GossipDependency;
import one.inve.localfullnode2.gossip.persistence.NewGossipEventsPersistenceDependency;
import one.inve.localfullnode2.hashnet.HashneterDependency;
import one.inve.localfullnode2.hashnet.HashneterUpstreamDependency;
import one.inve.localfullnode2.message.MessagePersistenceDependency;
import one.inve.localfullnode2.message.MessagesExeDependency;
import one.inve.localfullnode2.message.MessagesVerificationDependency;
import one.inve.localfullnode2.postconsensus.exe.EventsExeDependency;
import one.inve.localfullnode2.postconsensus.readout.EventsReadoutDependency;
import one.inve.localfullnode2.postconsensus.sorting.EventsSortingDependency;
import one.inve.localfullnode2.snapshot.CreateSnapshotPointDependency;
import one.inve.localfullnode2.snapshot.DetectAndRepairSnapshotDataDependency;
import one.inve.localfullnode2.snapshot.HandleConsensusSnapshotMessageDependency;
import one.inve.localfullnode2.snapshot.HandleSnapshotPointDependency;
import one.inve.localfullnode2.snapshot.RepairCurrSnapshotPointInfoDependency;
import one.inve.localfullnode2.snapshot.SnapshotSyncConsumer;
import one.inve.localfullnode2.snapshot.SnapshotSynchronizerDependency;
import one.inve.localfullnode2.staging.StagingArea;
import one.inve.localfullnode2.store.EventStoreDependency;

/**
 * event listener registration.
 * 
 * Copyright © CHXX Co.,Ltd. All rights reserved.
 * 
 * @Description: maintain data convey via {@link DepItemsManager}
 * @author: Francis.Deng
 * @date: May 15, 2019 12:12:19 AM
 * @version: V1.0
 */
public abstract class DepsPointcut extends LocalFullNode1GeneralNode {
	private static final Logger logger = LoggerFactory.getLogger(DepsPointcut.class);

	private DepItemsManagerial depItemsManager = DepItemsManager.getInstance();

	// making ensure that event being triggered happened after registration.
	protected void loadDeps() {
		// new instance of listeners and of them
		EventStoreDependency eventStoreDependency = new EventStoreDependency();
		of(eventStoreDependency);

		GossipDependency gossipDependency = new GossipDependency();
		of(gossipDependency);

		NewGossipEventsPersistenceDependency newGossipEventsPersistenceDependency = new NewGossipEventsPersistenceDependency();
		of(newGossipEventsPersistenceDependency);

		HashneterDependency hashneterDependency = new HashneterDependency();
		of(hashneterDependency);
		hashneterDependency.setEventStoreDependent(eventStoreDependency);// depending on {@code eventStoreDependency}

		HashneterUpstreamDependency hashneterUpstreamDependency = new HashneterUpstreamDependency();
		of(hashneterUpstreamDependency);

		EventsReadoutDependency eventsReadoutDependency = new EventsReadoutDependency();
		of(eventsReadoutDependency);

		EventsSortingDependency eventsSortingDependency = new EventsSortingDependency();
		of(eventsSortingDependency);

		EventsExeDependency eventsExeDependency = new EventsExeDependency();
		of(eventsExeDependency);

		MessagesVerificationDependency messagesVerificationDependency = new MessagesVerificationDependency();
		of(messagesVerificationDependency);

		MessagesExeDependency messagesExeDependency = new MessagesExeDependency();
		of(messagesExeDependency);

		MessagePersistenceDependency messagePersistenceDependency = new MessagePersistenceDependency();
		of(messagePersistenceDependency);

		CreateSnapshotPointDependency createSnapshotPointDependency = new CreateSnapshotPointDependency();
		of(createSnapshotPointDependency);

		DetectAndRepairSnapshotDataDependency detectAndRepairSnapshotDataDependency = new DetectAndRepairSnapshotDataDependency();
		of(detectAndRepairSnapshotDataDependency);

		HandleConsensusSnapshotMessageDependency handleConsensusSnapshotMessageDependency = new HandleConsensusSnapshotMessageDependency();
		of(handleConsensusSnapshotMessageDependency);

		HandleSnapshotPointDependency handleSnapshotPointDependency = new HandleSnapshotPointDependency();
		of(handleSnapshotPointDependency);

		RepairCurrSnapshotPointInfoDependency repairCurrSnapshotPointInfoDependency = new RepairCurrSnapshotPointInfoDependency();
		of(repairCurrSnapshotPointInfoDependency);

		SnapshotSynchronizerDependency snapshotSynchronizerDependency = new SnapshotSynchronizerDependency();
		of(snapshotSynchronizerDependency);

		SnapshotSyncConsumer snapshotSyncConsumer = new SnapshotSyncConsumer();
		of(snapshotSyncConsumer);

		buildStagingArea();
	}

	/**
	 * {@code SnapshotSync(SnapshotSyncConsumable dep)}
	 */
	private void of(SnapshotSyncConsumer snapshotSyncConsumer) {
		DepItemsManager.getInstance().attachDirectCommunicator(snapshotSyncConsumer);
	}

	/**
	 * {@code SnapshotSynchronizer(SnapshotSynchronizerDependent dep)}
	 */
	protected void of(SnapshotSynchronizerDependency snapshotSynchronizerDependency) {
		DepItemsManager.getInstance().attachSS(snapshotSynchronizerDependency);
		DepItemsManager.getInstance().attachStat(snapshotSynchronizerDependency);
		DepItemsManager.getInstance().attachShardCount(snapshotSynchronizerDependency);
		DepItemsManager.getInstance().attachNValue(snapshotSynchronizerDependency);
		DepItemsManager.getInstance().attachPublicKey(snapshotSynchronizerDependency);
		DepItemsManager.getInstance().attachAllQueues(snapshotSynchronizerDependency);
	}

	/**
	 * {@code RepairCurrSnapshotPointInfo(RepairCurrSnapshotPointInfoDependent dep)}
	 */
	protected void of(RepairCurrSnapshotPointInfoDependency repairCurrSnapshotPointInfoDependency) {
		DepItemsManager.getInstance().attachSS(repairCurrSnapshotPointInfoDependency);
		DepItemsManager.getInstance().attachStat(repairCurrSnapshotPointInfoDependency);
		DepItemsManager.getInstance().attachShardCount(repairCurrSnapshotPointInfoDependency);
		DepItemsManager.getInstance().attachNValue(repairCurrSnapshotPointInfoDependency);
		DepItemsManager.getInstance().attachLocalFullNodes(repairCurrSnapshotPointInfoDependency);
		DepItemsManager.getInstance().attachShardId(repairCurrSnapshotPointInfoDependency);
		DepItemsManager.getInstance().attachCreatorId(repairCurrSnapshotPointInfoDependency);
		DepItemsManager.getInstance().attachDBId(repairCurrSnapshotPointInfoDependency);
		DepItemsManager.getInstance().attachAllQueues(repairCurrSnapshotPointInfoDependency);
	}

	/**
	 * {@code HandleSnapshotPoint(HandleSnapshotPointDependent dep)}
	 */
	protected void of(HandleSnapshotPointDependency handleSnapshotPointDependency) {
		DepItemsManager.getInstance().attachSS(handleSnapshotPointDependency);
		DepItemsManager.getInstance().attachAllQueues(handleSnapshotPointDependency);
		DepItemsManager.getInstance().attachWal(handleSnapshotPointDependency);
	}

	/**
	 * {@code HandleConsensusSnapshotMessage(HandleConsensusSnapshotMessageDependent dep)}
	 */
	protected void of(HandleConsensusSnapshotMessageDependency handleConsensusSnapshotMessageDependency) {
		DepItemsManager.getInstance().attachSS(handleConsensusSnapshotMessageDependency);
		DepItemsManager.getInstance().attachStat(handleConsensusSnapshotMessageDependency);
		DepItemsManager.getInstance().attachShardCount(handleConsensusSnapshotMessageDependency);
		DepItemsManager.getInstance().attachNValue(handleConsensusSnapshotMessageDependency);
		DepItemsManager.getInstance().attachDBId(handleConsensusSnapshotMessageDependency);
		DepItemsManager.getInstance().attachAllQueues(handleConsensusSnapshotMessageDependency);

	}

	/**
	 * {@code DetectAndRepairSnapshotData(DetectAndRepairSnapshotDataDependent dep)}
	 */
	protected void of(DetectAndRepairSnapshotDataDependency detectAndRepairSnapshotDataDependency) {
		DepItemsManager.getInstance().attachSS(detectAndRepairSnapshotDataDependency);
		DepItemsManager.getInstance().attachNValue(detectAndRepairSnapshotDataDependency);
		DepItemsManager.getInstance().attachShardId(detectAndRepairSnapshotDataDependency);
		DepItemsManager.getInstance().attachCreatorId(detectAndRepairSnapshotDataDependency);
		DepItemsManager.getInstance().attachDBId(detectAndRepairSnapshotDataDependency);
	}

	/**
	 * {@code CreateSnapshotPoint(CreateSnapshotPointDependent dep)}
	 */
	protected void of(CreateSnapshotPointDependency createSnapshotPointDependency) {
		DepItemsManager.getInstance().attachSS(createSnapshotPointDependency);
		DepItemsManager.getInstance().attachStat(createSnapshotPointDependency);
		DepItemsManager.getInstance().attachShardCount(createSnapshotPointDependency);
		DepItemsManager.getInstance().attachNValue(createSnapshotPointDependency);
		DepItemsManager.getInstance().attachLocalFullNodes(createSnapshotPointDependency);
		DepItemsManager.getInstance().attachShardId(createSnapshotPointDependency);
		DepItemsManager.getInstance().attachCreatorId(createSnapshotPointDependency);
		DepItemsManager.getInstance().attachAllQueues(createSnapshotPointDependency);

	}

	/**
	 * {@code MessagePersistence(MessagePersistenceDependent dep)}
	 */
	protected void of(MessagePersistenceDependency messagePersistenceDependency) {
		DepItemsManager.getInstance().attachAllQueues(messagePersistenceDependency);
		DepItemsManager.getInstance().attachStat(messagePersistenceDependency);
		DepItemsManager.getInstance().attachDBId(messagePersistenceDependency);
	}

	/**
	 * {@code MessagesExe(MessagesExeDependent dep)}
	 */
	protected void of(MessagesExeDependency messagesExeDependency) {
		DepItemsManager.getInstance().attachAllQueues(messagesExeDependency);
		DepItemsManager.getInstance().attachShardCount(messagesExeDependency);
		DepItemsManager.getInstance().attachDBId(messagesExeDependency);
		DepItemsManager.getInstance().attachStat(messagesExeDependency);
		DepItemsManager.getInstance().attachSS(messagesExeDependency);
	}

	/**
	 * {@code MessagesVerification(MessagesVerificationDependent dep)}
	 */
	protected void of(MessagesVerificationDependency messagesVerificationDependency) {
		DepItemsManager.getInstance().attachAllQueues(messagesVerificationDependency);
	}

	/**
	 * {@code EventsExe(EventsExeDependent dep)}
	 */
	protected void of(EventsExeDependency eventsExeDependency) {
		DepItemsManager.getInstance().attachCreatorId(eventsExeDependency);
		DepItemsManager.getInstance().attachStat(eventsExeDependency);
		DepItemsManager.getInstance().attachDBId(eventsExeDependency);
		DepItemsManager.getInstance().attachAllQueues(eventsExeDependency);
		DepItemsManager.getInstance().attachSS(eventsExeDependency);
	}

	/**
	 * {@code work(EventsSortingDependent dep)}
	 */
	protected void of(EventsSortingDependency eventsSortingDependency) {
		DepItemsManager.getInstance().attachShardCount(eventsSortingDependency);
		DepItemsManager.getInstance().attachAllQueues(eventsSortingDependency);
	}

	/**
	 * {@code void read(EventsReadoutDependent dep)}
	 */
	protected void of(EventsReadoutDependency eventsReadoutDependency) {
		DepItemsManager.getInstance().attachShardCount(eventsReadoutDependency);
		DepItemsManager.getInstance().attachAllQueues(eventsReadoutDependency);
	}

	/**
	 * {@code pull(HashneterUpstreamDependent dep)}
	 */
	protected void of(HashneterUpstreamDependency hashneterUpstreamDependency) {
		DepItemsManager.getInstance().attachShardCount(hashneterUpstreamDependency);
	}

	/**
	 * {@code initHashnet(HashneterDependent dep)}
	 */
	protected void of(HashneterDependency hashneterDependency) {
		DepItemsManager.getInstance().attachShardCount(hashneterDependency);
		DepItemsManager.getInstance().attachShardId(hashneterDependency);
		DepItemsManager.getInstance().attachEventFlow(hashneterDependency);
		DepItemsManager.getInstance().attachStat(hashneterDependency);
		DepItemsManager.getInstance().attachNValue(hashneterDependency);
		DepItemsManager.getInstance().attachCreatorId(hashneterDependency);
		DepItemsManager.getInstance().attachAllQueues(hashneterDependency);
		DepItemsManager.getInstance().attachLocalFullNodes(hashneterDependency);
		DepItemsManager.getInstance().attachPrivateKey(hashneterDependency);
	}

	/**
	 * {@code persistNewEvents(NewGossipEventsPersistenceDependent dep)}
	 */
	protected void of(NewGossipEventsPersistenceDependency newGossipEventsPersistenceDependency) {
		DepItemsManager.getInstance().attachAllQueues(newGossipEventsPersistenceDependency);
		DepItemsManager.getInstance().attachDBId(newGossipEventsPersistenceDependency);
		DepItemsManager.getInstance().attachStat(newGossipEventsPersistenceDependency);
	}

	/**
	 * {@code EventStoreImpl(EventStoreDependent dep)}
	 */
	protected void of(EventStoreDependency eventStoreDependency) {
		DepItemsManager.getInstance().attachDBId(eventStoreDependency);
		DepItemsManager.getInstance().attachNValue(eventStoreDependency);
		DepItemsManager.getInstance().attachShardCount(eventStoreDependency);
		DepItemsManager.getInstance().attachCreatorId(eventStoreDependency);
		DepItemsManager.getInstance().attachAllQueues(eventStoreDependency);

	}

	/**
	 * {@code talkGossip(GossipDependent dep)}
	 */
	protected void of(GossipDependency gossipDependency) {
		DepItemsManager.getInstance().attachMembers(gossipDependency);
		DepItemsManager.getInstance().attachShardCount(gossipDependency);
		DepItemsManager.getInstance().attachShardId(gossipDependency);
		DepItemsManager.getInstance().attachCreatorId(gossipDependency);
		DepItemsManager.getInstance().attachLastSeqs(gossipDependency);
		DepItemsManager.getInstance().attachPublicKey(gossipDependency);
//		DepItemsManager.getInstance().attachCurrSnapshotVersion(gossipDependency);
		DepItemsManager.getInstance().attachEventFlow(gossipDependency);
		DepItemsManager.getInstance().attachBlackList4PubKey(gossipDependency);
		DepItemsManager.getInstance().attachPrivateKey(gossipDependency);
		DepItemsManager.getInstance().attachDirectCommunicator(gossipDependency);
		DepItemsManager.getInstance().attachAllQueues(gossipDependency);
		DepItemsManager.getInstance().attachLastSeqs(gossipDependency);
//		DepItemsManager.getInstance().attachUpdatedSnapshotMessage(gossipDependency);
		DepItemsManager.getInstance().attachSS(gossipDependency);
		DepItemsManager.getInstance().attachNValue(gossipDependency);

	}

	protected void info(String id, String e, String op) {
		logger.info("DSPTVM - MessageTracker     - queue   : {}", id);
		logger.info("DSPTVM -   MessageTracker   - op      : {}", op);
		logger.info("DSPTVM -     MessageTracker - element : {}", e);

		int i = 1;
	}

	// replace queues in {@link GeneralNode}
	protected void buildStagingArea() {
		AllQueues allQueues = DepItemsManager.getInstance().attachAllQueues(null);
		StagingArea stagingArea = new StagingArea();

		stagingArea.createQueue(byte[].class, StagingArea.MessageQueueName, 10000000,
				(e, id, op) -> info(id, new String((byte[]) e), op));
		stagingArea.createQueue(EventBody.class, StagingArea.EventSaveQueueName, 10000000, null);
		stagingArea.createQueue(EventBody.class, StagingArea.ConsEventHandleQueueName, 10000000, null);
		stagingArea.createQueue(JSONObject.class, StagingArea.ConsMessageVerifyQueueName, 10000000,
				(e, id, op) -> info(id, ((JSONObject) e).toJSONString(), op));
		stagingArea.createQueue(JSONObject.class, StagingArea.ConsMessageHandleQueueName, 10000000,
				(e, id, op) -> info(id, ((JSONObject) e).toJSONString(), op));
		stagingArea.createQueue(JSONObject.class, StagingArea.ConsMessageSaveQueueName, 10000000,
				(e, id, op) -> info(id, ((JSONObject) e).toJSONString(), op));
		stagingArea.createQueue(JSONObject.class, StagingArea.SystemAutoTxSaveQueueName, 10000000,
				(e, id, op) -> info(id, ((JSONObject) e).toJSONString(), op));

		allQueues.set(stagingArea);
	}

	@Override
	public void setShardId(int shardId) {
		depItemsManager.attachShardId(null).set(shardId);
		super.setShardId(shardId);
	}

	@Override
	public void setCreatorId(long creatorId) {
		depItemsManager.attachCreatorId(null).set(creatorId);
		super.setCreatorId(creatorId);
	}

	@Override
	public void setnValue(int nValue) {
		depItemsManager.attachNValue(null).set(nValue);
		super.setnValue(nValue);
	}

	@Override
	public void setShardCount(int shardCount) {
		depItemsManager.attachShardCount(null).set(shardCount);
		super.setShardCount(shardCount);
	}

	@Override
	public void setCommunicator(Communicator communicator) {
		depItemsManager.attachDirectCommunicator(null).set(communicator);
		super.setCommunicator(communicator);
	}

	@Override
	public void publicKey(PublicKey publicKey) {
		depItemsManager.attachPublicKey(null).set(publicKey);
		super.publicKey(publicKey);
	}

	@Override
	public void privateKey(PrivateKey privateKey) {
		depItemsManager.attachPrivateKey(null).set(privateKey);
		super.privateKey(privateKey);
	}

	public void dbId(String dbId) {
		depItemsManager.attachDBId(null).set(dbId);
	}

	@Override
	public void setLocalFullNodes(List<LocalFullNode> localFullNodes) {
		depItemsManager.attachLocalFullNodes(null).set(localFullNodes);
		super.setLocalFullNodes(localFullNodes);
	}

	@Override
	public void setBlackList4PubKey(List<String> blackList4PubKey) {
		depItemsManager.attachBlackList4PubKey(null).set(blackList4PubKey);
		super.setBlackList4PubKey(blackList4PubKey);
	}

	@Override
	public void addConsMessageMaxId(long delta) {
		depItemsManager.attachStat(null).addConsMessageMaxId(delta);
		super.addConsMessageMaxId(delta);
	}

	@Override
	public void setConsMessageCount(BigInteger consMessageCount) {
		depItemsManager.attachStat(null).setConsMessageCount(consMessageCount);
		super.setConsMessageCount(consMessageCount);
	}

	@Override
	public void addSystemAutoTxMaxId(long delta) {
		depItemsManager.attachStat(null).addSystemAutoTxMaxId(delta);
		super.addSystemAutoTxMaxId(delta);
	}

	@Override
	public void inshardNeighborPools(List<Member> members) {
		depItemsManager.attachMembers(null).setInSharding(members);
		super.inshardNeighborPools(members);
	}

	@Override
	public void globalNeighborPools(List<Member> members) {
		depItemsManager.attachMembers(null).setGlobally(members);
		super.globalNeighborPools(members);
	}

//	@Override
//	public void setLastSeqs(long[][] lastSeqs) {
//		depItemsManager.attachLastSeqs(null).set(lastSeqs);
//		super.setLastSeqs(lastSeqs);
//	}

	@Override
	public void setSnapshotMessage(SnapshotMessage snapshotMessage) {
		depItemsManager.attachSS(null).setSnapshotMessage(snapshotMessage);
		super.setSnapshotMessage(snapshotMessage);
	}

	@Override
	public void setTotalConsEventCount(BigInteger totalConsEventCount) {
		depItemsManager.attachStat(null).setTotalConsEventCount(totalConsEventCount);
		super.setTotalConsEventCount(totalConsEventCount);
	}

}
