package one.inve.localfullnode2.sync.rpc;

import java.util.ArrayList;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alibaba.fastjson.JSONObject;
import com.google.gson.Gson;

import one.inve.core.EventBody;
import one.inve.localfullnode2.dep.DepItemsManager;
import one.inve.localfullnode2.nodes.LocalFullNode1GeneralNode;
import one.inve.localfullnode2.store.EventKeyPair;
import one.inve.localfullnode2.store.rocks.RocksJavaUtil;
import one.inve.localfullnode2.store.rocks.key.MessageIndexes;
import one.inve.localfullnode2.sync.Mapper;
import one.inve.localfullnode2.sync.measure.ChunkDistribution;
import one.inve.localfullnode2.sync.measure.Column;
import one.inve.localfullnode2.sync.measure.Distribution;
import one.inve.localfullnode2.sync.measure.Range;
import one.inve.localfullnode2.sync.rpc.DataSynchronizationZerocImpl.IDataSynchronization;
import one.inve.localfullnode2.sync.rpc.gen.DistributedEventObjects;
import one.inve.localfullnode2.sync.rpc.gen.DistributedMessageObjects;
import one.inve.localfullnode2.sync.rpc.gen.DistributedSysMessageObjects;
import one.inve.localfullnode2.sync.rpc.gen.Localfullnode2InstanceProfile;
import one.inve.localfullnode2.sync.rpc.gen.MerkleTreeizedSyncEvent;
import one.inve.localfullnode2.sync.rpc.gen.MerkleTreeizedSyncMessage;
import one.inve.localfullnode2.sync.rpc.gen.MerkleTreeizedSyncSysMessage;
import one.inve.localfullnode2.sync.rpc.gen.SyncEvent;
import one.inve.localfullnode2.utilities.GenericArray;
import one.inve.localfullnode2.utilities.merkle.INodeContent;
import one.inve.localfullnode2.utilities.merkle.MerklePath;
import one.inve.localfullnode2.utilities.merkle.MerkleTree;
import one.inve.localfullnode2.utilities.merkle.Node;

/**
 * 
 * 
 * Copyright © INVE FOUNDATION. All rights reserved.
 * 
 * @Description: follow the principle of "Dependence Inversion" by the move
 * @author: Francis.Deng [francis_xiiiv@163.com]
 * @date: Aug 29, 2019 8:53:04 PM
 * @version: V1.0
 */
public class DataSynchronizationCore implements IDataSynchronization {
	private static final Logger logger = LoggerFactory.getLogger(DataSynchronizationCore.class);
	private Gson gson = new Gson();

	private volatile LocalFullNode1GeneralNode node;
	private long[][] lastSeqs;
	private int nValue;
	private String dbId;
	private int shardId;
	private long creatorId;

	private long[] firstSeqsInThisShard;

	private RocksJavaUtil rocksJavaUtil;

	// ensuring that {@code probeFirstSeqs} is executed before.
	public DataSynchronizationCore(LocalFullNode1GeneralNode node) {
		this.node = node;

		lastSeqs = node.getLastSeqs();
		nValue = node.getnValue();
		dbId = node.nodeParameters().dbId;
		shardId = node.getShardId();
		creatorId = node.getCreatorId();

		firstSeqsInThisShard = DepItemsManager.getInstance().attachFirstSeqs(null).get(shardId);

		rocksJavaUtil = new RocksJavaUtil(dbId);
	}

	protected String[] getAllMessageIds() {
		Map<byte[], byte[]> m = rocksJavaUtil.startWith(MessageIndexes.getMessageHashPrefix().getBytes());

		GenericArray<String> array = new GenericArray<>();
		for (byte[] k : m.keySet()) {
			String h = MessageIndexes.getMessageHash(new String(k));
			array.append(h);
		}

		return array.toArray(new String[array.length()]);
	}

	protected GenericArray<String> getEntities(String[] entityIds) {
		GenericArray<String> array = new GenericArray<>();
		for (String entityId : entityIds) {
			byte[] bytes = rocksJavaUtil.get(entityId);
			String json = new String(bytes);

			array.append(json);
		}

		return array;
	}

	protected String[] getSysMessageIds() {
		Map<byte[], byte[]> m = rocksJavaUtil.startWith(MessageIndexes.getSysMessageTypeIdPrefix().getBytes());

		GenericArray<String> array = new GenericArray<>();
		for (byte[] k : m.keySet()) {
			String t = MessageIndexes.getSysMessageTypeId(new String(k));
			array.append(t);
		}

		return array.toArray(new String[array.length()]);
	}

	@Override
	public DistributedMessageObjects getNotInDistributionMessages(String distJson) {
		DistributedMessageObjects result = new DistributedMessageObjects();
		ChunkDistribution<String> nextDist;

		ChunkDistribution<String> requestSideDist = gson.fromJson(distJson, ChunkDistribution.class);

		if (requestSideDist.isNull()) {
			String[] msgIds = getAllMessageIds();
			nextDist = new ChunkDistribution<String>(msgIds);

			return new DistributedMessageObjects(gson.toJson(nextDist), null, null);
		} else {
			nextDist = requestSideDist.next();
			ArrayList<String> ids = nextDist.getNextPartOfElements();

			// retrieving specified messages by ids
			GenericArray<String> messagesInJson = getEntities(ids.toArray(new String[ids.size()]));

			// compute rootHash and INodeContent array
			INodeContent[] nodeContents = Mapper.transformFromStringArray(messagesInJson);
			MerkleTree mt = MerkleTree.create(nodeContents);
			Node root = mt.getRoot();
			byte[] rootHash = root.getHash();

			// compute MerkleTreeizedSyncMessage array
			MerkleTreeizedSyncMessage[] merkleTreeizedSyncMessages = buildMerkleTreeizedSyncMessages(mt, nodeContents,
					messagesInJson.toArray(new String[messagesInJson.length()]));

			return new DistributedMessageObjects(gson.toJson(nextDist), merkleTreeizedSyncMessages, rootHash);
		}
	}

	@Override
	public DistributedSysMessageObjects getNotInDistributionSysMessages(String distJson) {
		DistributedSysMessageObjects result = new DistributedSysMessageObjects();
		ChunkDistribution<String> nextDist;

		ChunkDistribution<String> requestSideDist = gson.fromJson(distJson, ChunkDistribution.class);

		if (requestSideDist.isNull()) {
			String[] sysMsgIds = getSysMessageIds();
			nextDist = new ChunkDistribution<String>(sysMsgIds);

			return new DistributedSysMessageObjects(gson.toJson(nextDist), null, null);
		} else {
			nextDist = requestSideDist.next();
			ArrayList<String> ids = nextDist.getNextPartOfElements();

			// retrieving specified messages by ids
			GenericArray<String> sysMessagesInJson = getEntities(ids.toArray(new String[ids.size()]));

			// compute rootHash and INodeContent array
			INodeContent[] nodeContents = Mapper.transformFromStringArray(sysMessagesInJson);
			MerkleTree mt = MerkleTree.create(nodeContents);
			Node root = mt.getRoot();
			byte[] rootHash = root.getHash();

			// compute MerkleTreeizedSyncMessage array
			MerkleTreeizedSyncSysMessage[] merkleTreeizedSyncSysMessages = buildMerkleTreeizedSyncSysMessages(mt,
					nodeContents, sysMessagesInJson.toArray(new String[sysMessagesInJson.length()]));

			return new DistributedSysMessageObjects(gson.toJson(nextDist), merkleTreeizedSyncSysMessages, rootHash);
		}
	}

	@Override
	public DistributedEventObjects getNotInDistributionEvents(String distJson) {
		int _eventSize = 100;// hard-code it,better solution is to take a threshold like 10k.
		Distribution nextDist;

		// Distribution requestSideDist = JSON.parseObject(distJson,
		// Distribution.class);
		Distribution requestSideDist = gson.fromJson(distJson, Distribution.class);

		if (requestSideDist.isNull()) {
			nextDist = Distribution.build(nValue, firstSeqsInThisShard, _eventSize);
		} else {
			nextDist = requestSideDist.next(_eventSize);
		}

		GenericArray<EventBody> eventBodyArray = new GenericArray<>();

		// extract events from next distribution
		for (long creator = 0; creator < nValue; creator++) {
			Column c = nextDist.getColumns()[(int) creator];
			Range r = c.getRanges().get(0);
			for (Long l : r) {
				EventBody eb = getEventBody(shardId, creator, l.longValue());
				if (eb != null) {
					eventBodyArray.append(eb);
				} else {
					r.setStop(l);
					break;
				}

			}
		}

		if (eventBodyArray.length() != 0) {
			// compute rootHash and INodeContent array
			INodeContent[] nodeContents = Mapper.transformFromArray(eventBodyArray);
			MerkleTree mt = MerkleTree.create(nodeContents);
			Node root = mt.getRoot();
			byte[] rootHash = root.getHash();

			// compute MerkleTreeizedSyncEvent array
			MerkleTreeizedSyncEvent[] merkleTreeizedSyncEvents = buildMerkleTreeizedSyncEvents(mt, nodeContents,
					eventBodyArray.toArray(new EventBody[eventBodyArray.length()]));

			// return new DistributedEventObjects(JSON.toJSONString(nextDist),
			// merkleTreeizedSyncEvents, rootHash);
			return new DistributedEventObjects(gson.toJson(nextDist), merkleTreeizedSyncEvents, rootHash);
		} else {
			return new DistributedEventObjects(gson.toJson(new Distribution(nValue)), null, null);
		}

	}

	public MerkleTreeizedSyncEvent[] buildMerkleTreeizedSyncEvents(MerkleTree mt, INodeContent[] nodeContents,
			EventBody[] eventBodies) {
		GenericArray<MerkleTreeizedSyncEvent> merkleTreeizedSyncEvents = new GenericArray<>();

		for (int index = 0; index < nodeContents.length; index++) {
			MerklePath mp = mt.getMerklePath(nodeContents[index]);

			SyncEvent target = new SyncEvent();
			Mapper.copyProperties(eventBodies[index], target, false);

			// due to interface specification change
			// merkleTreeizedSyncEvents.append(new MerkleTreeizedSyncEvent(target,
			// gson.toJson(mp)));
			merkleTreeizedSyncEvents.append(new MerkleTreeizedSyncEvent(target, mp.path(), mp.index()));
		}

		return merkleTreeizedSyncEvents.toArray(new MerkleTreeizedSyncEvent[merkleTreeizedSyncEvents.length()]);

	}

	public MerkleTreeizedSyncMessage[] buildMerkleTreeizedSyncMessages(MerkleTree mt, INodeContent[] nodeContents,
			String[] messagesInJson) {
		GenericArray<MerkleTreeizedSyncMessage> merkleTreeizedSyncMessages = new GenericArray<>();

		for (int index = 0; index < nodeContents.length; index++) {
			MerklePath mp = mt.getMerklePath(nodeContents[index]);

			merkleTreeizedSyncMessages
					.append(new MerkleTreeizedSyncMessage(messagesInJson[index], mp.path(), mp.index()));
		}

		return merkleTreeizedSyncMessages.toArray(new MerkleTreeizedSyncMessage[merkleTreeizedSyncMessages.length()]);

	}

	public MerkleTreeizedSyncSysMessage[] buildMerkleTreeizedSyncSysMessages(MerkleTree mt, INodeContent[] nodeContents,
			String[] sysMessagesInJson) {
		GenericArray<MerkleTreeizedSyncSysMessage> merkleTreeizedSyncSysMessages = new GenericArray<>();

		for (int index = 0; index < nodeContents.length; index++) {
			MerklePath mp = mt.getMerklePath(nodeContents[index]);

			merkleTreeizedSyncSysMessages
					.append(new MerkleTreeizedSyncSysMessage(sysMessagesInJson[index], mp.path(), mp.index()));
		}

		return merkleTreeizedSyncSysMessages
				.toArray(new MerkleTreeizedSyncSysMessage[merkleTreeizedSyncSysMessages.length()]);

	}

	public EventBody getEventBody(int shardId, long creatorId, long seq) {
		EventBody eb = null;
		// RocksJavaUtil rocksJavaUtil = new RocksJavaUtil(dbId);
		EventKeyPair pair = new EventKeyPair(shardId, creatorId, seq);
		byte[] evt = rocksJavaUtil.get(pair.toString());

		if (null != evt && evt.length > 0) {
			eb = JSONObject.parseObject(new String(evt), EventBody.class);
		}

		return eb;
	}

	@Override
	public Localfullnode2InstanceProfile getLocalfullnode2InstanceProfile() {
		return new Localfullnode2InstanceProfile(shardId, (int) creatorId, nValue, dbId);
	}

}
