package one.inve.localfullnode2.dep;

import one.inve.localfullnode2.dep.items.DBId;
import one.inve.localfullnode2.dep.items.LocalFullNodes;
import one.inve.localfullnode2.dep.items.Mnemonic;
import one.inve.localfullnode2.dep.items.NValue;
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

	private DepItemsManager() {
		shardId = new ShardId();
		shardCount = new ShardCount();
		nValue = new NValue();
		localFullNodes = new LocalFullNodes();
		dbId = new DBId();
		mnemonic = new Mnemonic();
		publicKey = new PublicKey();
	}

	private static class SingletonHelper {
		private static final DepItemsManager INSTANCE = new DepItemsManager();
	}

	public static DepItemsManager getInstance() {
		return SingletonHelper.INSTANCE;
	}

	public ShardId attachShardId(DependentItemConcerned... dependentItemConcerneds) {
		if (dependentItemConcerneds != null) {
			shardId.attach(dependentItemConcerneds);
		}

		return shardId;
	}

	public ShardCount attachShardCount(DependentItemConcerned... dependentItemConcerneds) {
		if (dependentItemConcerneds != null) {
			shardCount.attach(dependentItemConcerneds);
		}

		return shardCount;
	}

	public NValue attachNValue(DependentItemConcerned... dependentItemConcerneds) {
		if (dependentItemConcerneds != null) {
			nValue.attach(dependentItemConcerneds);
		}

		return nValue;
	}

	public LocalFullNodes attachLocalFullNodes(DependentItemConcerned... dependentItemConcerneds) {
		if (dependentItemConcerneds != null) {
			localFullNodes.attach(dependentItemConcerneds);
		}

		return localFullNodes;
	}

	public DBId attachDBId(DependentItemConcerned... dependentItemConcerneds) {
		if (dependentItemConcerneds != null) {
			dbId.attach(dependentItemConcerneds);
		}

		return dbId;
	}

	public Mnemonic attachMnemonic(DependentItemConcerned... dependentItemConcerneds) {
		if (dependentItemConcerneds != null) {
			mnemonic.attach(dependentItemConcerneds);
		}

		return mnemonic;
	}

	public PublicKey attachPublicKey(DependentItemConcerned... dependentItemConcerneds) {
		if (dependentItemConcerneds != null) {
			publicKey.attach(dependentItemConcerneds);
		}

		return publicKey;
	}
}
