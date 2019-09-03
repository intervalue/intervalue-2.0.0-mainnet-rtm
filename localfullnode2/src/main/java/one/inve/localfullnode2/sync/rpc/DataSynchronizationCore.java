package one.inve.localfullnode2.sync.rpc;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alibaba.fastjson.JSONObject;
import com.google.gson.Gson;

import one.inve.core.EventBody;
import one.inve.localfullnode2.dep.DepItemsManager;
import one.inve.localfullnode2.nodes.LocalFullNode1GeneralNode;
import one.inve.localfullnode2.store.EventKeyPair;
import one.inve.localfullnode2.store.rocks.RocksJavaUtil;
import one.inve.localfullnode2.sync.Mapper;
import one.inve.localfullnode2.sync.measure.Column;
import one.inve.localfullnode2.sync.measure.Distribution;
import one.inve.localfullnode2.sync.measure.Range;
import one.inve.localfullnode2.sync.rpc.DataSynchronizationZerocImpl.IDataSynchronization;
import one.inve.localfullnode2.sync.rpc.gen.DistributedEventObjects;
import one.inve.localfullnode2.sync.rpc.gen.Localfullnode2InstanceProfile;
import one.inve.localfullnode2.sync.rpc.gen.MerkleTreeizedSyncEvent;
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

	// ensuring that {@code probeFirstSeqs} is executed before.
	public DataSynchronizationCore(LocalFullNode1GeneralNode node) {
		this.node = node;

		lastSeqs = node.getLastSeqs();
		nValue = node.getnValue();
		dbId = node.nodeParameters().dbId;
		shardId = node.getShardId();
		creatorId = node.getCreatorId();

		firstSeqsInThisShard = DepItemsManager.getInstance().attachFirstSeqs(null).get(shardId);
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

	public EventBody getEventBody(int shardId, long creatorId, long seq) {
		EventBody eb = null;
		RocksJavaUtil rocksJavaUtil = new RocksJavaUtil(dbId);
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
