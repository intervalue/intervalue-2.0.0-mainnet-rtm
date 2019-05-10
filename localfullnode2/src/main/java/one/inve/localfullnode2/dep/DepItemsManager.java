package one.inve.localfullnode2.dep;

import one.inve.localfullnode2.dep.items.BlackList4PubKey;
import one.inve.localfullnode2.dep.items.CreatorId;
import one.inve.localfullnode2.dep.items.CurrSnapshotVersion;
import one.inve.localfullnode2.dep.items.DBId;
import one.inve.localfullnode2.dep.items.EventFlow;
import one.inve.localfullnode2.dep.items.LastSeqs;
import one.inve.localfullnode2.dep.items.LocalFullNodes;
import one.inve.localfullnode2.dep.items.Members;
import one.inve.localfullnode2.dep.items.Mnemonic;
import one.inve.localfullnode2.dep.items.NValue;
import one.inve.localfullnode2.dep.items.PrivateKey;
import one.inve.localfullnode2.dep.items.PublicKey;
import one.inve.localfullnode2.dep.items.ShardCount;
import one.inve.localfullnode2.dep.items.ShardId;

/**
 * 
 * Copyright © CHXX Co.,Ltd. All rights reserved.
 * 
 * @Description: remain a sole instance and remain all items concerned
 * @see {@code DependentItem}
 * @see {@code DependentItemConcerned}
 * @author: Francis.Deng
 * @date: May 8, 2019 1:10:10 AM
 * @version: V1.0
 */
public final class DepItemsManager implements DepItemsManagerial {
	private ShardId shardId;
	private ShardCount shardCount;
	private NValue nValue;
	private LocalFullNodes localFullNodes;
	private DBId dbId;
	private Mnemonic mnemonic;
	private PublicKey publicKey;
	private Members members;
	private CreatorId creatorId;
	private LastSeqs lastSeqs;
	private CurrSnapshotVersion currSnapshotVersion;
	private EventFlow eventFlow;
	private BlackList4PubKey blackList4PubKey;
	private PrivateKey privateKey;

	private DepItemsManager() {
		shardId = new ShardId();
		shardCount = new ShardCount();
		nValue = new NValue();
		localFullNodes = new LocalFullNodes();
		dbId = new DBId();
		mnemonic = new Mnemonic();
		publicKey = new PublicKey();
		members = new Members();
		creatorId = new CreatorId();
		lastSeqs = new LastSeqs();
		currSnapshotVersion = new CurrSnapshotVersion();
		eventFlow = new EventFlow();
		blackList4PubKey = new BlackList4PubKey();
		privateKey = new PrivateKey();
	}

	private static class SingletonHelper {
		private static final DepItemsManager INSTANCE = new DepItemsManager();
	}

	public static DepItemsManager getInstance() {
		return SingletonHelper.INSTANCE;
	}

	@Override
	public ShardId attachShardId(DependentItemConcerned... dependentItemConcerneds) {
		if (dependentItemConcerneds != null) {
			shardId.attach(dependentItemConcerneds);
		}

		return shardId;
	}

	@Override
	public ShardCount attachShardCount(DependentItemConcerned... dependentItemConcerneds) {
		if (dependentItemConcerneds != null) {
			shardCount.attach(dependentItemConcerneds);
		}

		return shardCount;
	}

	@Override
	public NValue attachNValue(DependentItemConcerned... dependentItemConcerneds) {
		if (dependentItemConcerneds != null) {
			nValue.attach(dependentItemConcerneds);
		}

		return nValue;
	}

	@Override
	public LocalFullNodes attachLocalFullNodes(DependentItemConcerned... dependentItemConcerneds) {
		if (dependentItemConcerneds != null) {
			localFullNodes.attach(dependentItemConcerneds);
		}

		return localFullNodes;
	}

	@Override
	public DBId attachDBId(DependentItemConcerned... dependentItemConcerneds) {
		if (dependentItemConcerneds != null) {
			dbId.attach(dependentItemConcerneds);
		}

		return dbId;
	}

	@Override
	public Mnemonic attachMnemonic(DependentItemConcerned... dependentItemConcerneds) {
		if (dependentItemConcerneds != null) {
			mnemonic.attach(dependentItemConcerneds);
		}

		return mnemonic;
	}

	@Override
	public PublicKey attachPublicKey(DependentItemConcerned... dependentItemConcerneds) {
		if (dependentItemConcerneds != null) {
			publicKey.attach(dependentItemConcerneds);
		}

		return publicKey;
	}

	@Override
	public Members attachMembers(DependentItemConcerned... dependentItemConcerneds) {
		if (dependentItemConcerneds != null) {
			members.attach(dependentItemConcerneds);
		}

		return members;
	}

	@Override
	public CreatorId attachCreatorId(DependentItemConcerned... dependentItemConcerneds) {
		if (dependentItemConcerneds != null) {
			creatorId.attach(dependentItemConcerneds);
		}

		return creatorId;
	}

	@Override
	public LastSeqs attachLastSeqs(DependentItemConcerned... dependentItemConcerneds) {
		if (dependentItemConcerneds != null) {
			lastSeqs.attach(dependentItemConcerneds);
		}

		return lastSeqs;
	}

	@Override
	public CurrSnapshotVersion attachCurrSnapshotVersion(DependentItemConcerned... dependentItemConcerneds) {
		if (dependentItemConcerneds != null) {
			currSnapshotVersion.attach(dependentItemConcerneds);
		}

		return currSnapshotVersion;
	}

	@Override
	public EventFlow attachEventFlow(DependentItemConcerned... dependentItemConcerneds) {
		if (dependentItemConcerneds != null) {
			eventFlow.attach(dependentItemConcerneds);
		}

		return eventFlow;
	}

	@Override
	public BlackList4PubKey attachBlackList4PubKey(DependentItemConcerned... dependentItemConcerneds) {
		if (dependentItemConcerneds != null) {
			blackList4PubKey.attach(dependentItemConcerneds);
		}

		return blackList4PubKey;
	}

	@Override
	public PrivateKey attachPrivateKey(DependentItemConcerned... dependentItemConcerneds) {
		if (dependentItemConcerneds != null) {
			privateKey.attach(dependentItemConcerneds);
		}

		return privateKey;
	}
}
