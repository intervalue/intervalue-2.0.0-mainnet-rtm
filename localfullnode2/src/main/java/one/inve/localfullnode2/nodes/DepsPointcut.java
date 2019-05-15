package one.inve.localfullnode2.nodes;

import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.List;

import com.alibaba.fastjson.JSONObject;
import com.zeroc.Ice.Communicator;

import one.inve.bean.node.LocalFullNode;
import one.inve.localfullnode2.dep.DepItemsManager;
import one.inve.localfullnode2.dep.DepItemsManagerial;
import one.inve.localfullnode2.dep.items.AllQueues;
import one.inve.localfullnode2.gossip.GossipDependency;
import one.inve.localfullnode2.staging.StagingArea;
import one.inve.localfullnode2.store.EventBody;
import one.inve.localfullnode2.store.EventStoreDependency;

/**
 * event listener registration.
 * 
 * Copyright © CHXX Co.,Ltd. All rights reserved.
 * 
 * @Description: TODO
 * @author: Francis.Deng
 * @date: May 15, 2019 12:12:19 AM
 * @version: V1.0
 */
public abstract class DepsPointcut extends LocalFullNode1GeneralNode {
	private DepItemsManagerial depItemsManager = DepItemsManager.getInstance();

	// making ensure that event being triggered happened after registration.
	protected void registerDeps() {
		// new instance of listeners and register them
		EventStoreDependency eventStoreDependency = new EventStoreDependency();
		registerEventStoreDependency(eventStoreDependency);

		GossipDependency gossipDependency = new GossipDependency();
		registerGossipDependency(gossipDependency);

		buildStagingArea();
	}

	/**
	 * for {@code EventStoreImpl(EventStoreDependent dep)}
	 */
	protected void registerEventStoreDependency(EventStoreDependency eventStoreDependency) {
		DepItemsManager.getInstance().attachDBId(eventStoreDependency);
		DepItemsManager.getInstance().attachNValue(eventStoreDependency);
		DepItemsManager.getInstance().attachShardCount(eventStoreDependency);
		DepItemsManager.getInstance().attachCreatorId(eventStoreDependency);
		DepItemsManager.getInstance().attachAllQueues(eventStoreDependency);

	}

	/**
	 * for {@code talkGossip(GossipDependent dep)}
	 */
	protected void registerGossipDependency(GossipDependency gossipDependency) {
		DepItemsManager.getInstance().attachMembers(gossipDependency);
		DepItemsManager.getInstance().attachShardCount(gossipDependency);
		DepItemsManager.getInstance().attachShardId(gossipDependency);
		DepItemsManager.getInstance().attachCreatorId(gossipDependency);
		DepItemsManager.getInstance().attachLastSeqs(gossipDependency);
		DepItemsManager.getInstance().attachPublicKey(gossipDependency);
		DepItemsManager.getInstance().attachCurrSnapshotVersion(gossipDependency);
		DepItemsManager.getInstance().attachEventFlow(gossipDependency);
		DepItemsManager.getInstance().attachBlackList4PubKey(gossipDependency);
		DepItemsManager.getInstance().attachPrivateKey(gossipDependency);
		DepItemsManager.getInstance().attachDirectCommunicator(gossipDependency);
		DepItemsManager.getInstance().attachAllQueues(gossipDependency);
	}

	// replace queues in {@link GeneralNode}
	protected void buildStagingArea() {
		AllQueues allQueues = DepItemsManager.getInstance().attachAllQueues(null);
		StagingArea stagingArea = new StagingArea();

		stagingArea.createQueue(byte[].class, StagingArea.MessageQueueName, 10000000, null);
		stagingArea.createQueue(EventBody.class, StagingArea.EventSaveQueueName, 10000000, null);
		stagingArea.createQueue(EventBody.class, StagingArea.ConsEventHandleQueueName, 10000000, null);
		stagingArea.createQueue(JSONObject.class, StagingArea.ConsMessageVerifyQueueName, 10000000, null);
		stagingArea.createQueue(JSONObject.class, StagingArea.ConsMessageHandleQueueName, 10000000, null);
		stagingArea.createQueue(JSONObject.class, StagingArea.ConsMessageSaveQueueName, 10000000, null);
		stagingArea.createQueue(JSONObject.class, StagingArea.SystemAutoTxSaveQueueName, 10000000, null);

		allQueues.set(stagingArea);
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

}
