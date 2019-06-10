package one.inve.localfullnode2.dep;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import one.inve.localfullnode2.dep.items.AllQueues;
import one.inve.localfullnode2.dep.items.BlackList4PubKey;
import one.inve.localfullnode2.dep.items.CreatorId;
import one.inve.localfullnode2.dep.items.CurrSnapshotVersion;
import one.inve.localfullnode2.dep.items.DBId;
import one.inve.localfullnode2.dep.items.DirectCommunicator;
import one.inve.localfullnode2.dep.items.EventFlow;
import one.inve.localfullnode2.dep.items.LastSeqs;
import one.inve.localfullnode2.dep.items.LocalFullNodes;
import one.inve.localfullnode2.dep.items.Members;
import one.inve.localfullnode2.dep.items.Mnemonic;
import one.inve.localfullnode2.dep.items.NValue;
import one.inve.localfullnode2.dep.items.PrivateKey;
import one.inve.localfullnode2.dep.items.PublicKey;
import one.inve.localfullnode2.dep.items.SS;
import one.inve.localfullnode2.dep.items.ShardCount;
import one.inve.localfullnode2.dep.items.ShardId;
import one.inve.localfullnode2.dep.items.Stat;
import one.inve.localfullnode2.dep.items.UpdatedSnapshotMessage;
import one.inve.localfullnode2.dep.items.Wal;

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
	private AllQueues allQueues;
	private DirectCommunicator directCommunicator;
	private UpdatedSnapshotMessage updatedSnapshotMessage;
	private Stat stat;
	private SS ss;
	private Wal wal;

	private Map<Class<?>, DependentItemConcerned> allDependentItemConcerned = new HashMap<>();

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
		allQueues = new AllQueues();
		directCommunicator = new DirectCommunicator();
		updatedSnapshotMessage = new UpdatedSnapshotMessage();
		stat = new Stat();
		wal = new Wal();
	}

	private static class SingletonHelper {
		private static final DepItemsManager INSTANCE = new DepItemsManager();
	}

	public static DepItemsManager getInstance() {
		return SingletonHelper.INSTANCE;
	}

	protected void retainItemConcernedsByClass(DependentItemConcerned... dependentItemConcerneds) {
		if (dependentItemConcerneds != null) {
			Arrays.asList(dependentItemConcerneds).stream().forEach((e) -> {
				allDependentItemConcerned.put(e.getClass(), e);
			});
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T> T getItemConcerned(Class<T> itemConcernedClass) {
		return (T) allDependentItemConcerned.get(itemConcernedClass);
	}

	@Override
	public ShardId attachShardId(DependentItemConcerned... dependentItemConcerneds) {
		if (dependentItemConcerneds != null) {
			shardId.attach(dependentItemConcerneds);
			retainItemConcernedsByClass(dependentItemConcerneds);
		}

		return shardId;
	}

	@Override
	public ShardCount attachShardCount(DependentItemConcerned... dependentItemConcerneds) {
		if (dependentItemConcerneds != null) {
			shardCount.attach(dependentItemConcerneds);
			retainItemConcernedsByClass(dependentItemConcerneds);
		}

		return shardCount;
	}

	@Override
	public NValue attachNValue(DependentItemConcerned... dependentItemConcerneds) {
		if (dependentItemConcerneds != null) {
			nValue.attach(dependentItemConcerneds);
			retainItemConcernedsByClass(dependentItemConcerneds);
		}

		return nValue;
	}

	@Override
	public LocalFullNodes attachLocalFullNodes(DependentItemConcerned... dependentItemConcerneds) {
		if (dependentItemConcerneds != null) {
			localFullNodes.attach(dependentItemConcerneds);
			retainItemConcernedsByClass(dependentItemConcerneds);
		}

		return localFullNodes;
	}

	@Override
	public DBId attachDBId(DependentItemConcerned... dependentItemConcerneds) {
		if (dependentItemConcerneds != null) {
			dbId.attach(dependentItemConcerneds);
			retainItemConcernedsByClass(dependentItemConcerneds);
		}

		return dbId;
	}

	@Override
	public Mnemonic attachMnemonic(DependentItemConcerned... dependentItemConcerneds) {
		if (dependentItemConcerneds != null) {
			mnemonic.attach(dependentItemConcerneds);
			retainItemConcernedsByClass(dependentItemConcerneds);
		}

		return mnemonic;
	}

	@Override
	public PublicKey attachPublicKey(DependentItemConcerned... dependentItemConcerneds) {
		if (dependentItemConcerneds != null) {
			publicKey.attach(dependentItemConcerneds);
			retainItemConcernedsByClass(dependentItemConcerneds);
		}

		return publicKey;
	}

	@Override
	public Members attachMembers(DependentItemConcerned... dependentItemConcerneds) {
		if (dependentItemConcerneds != null) {
			members.attach(dependentItemConcerneds);
			retainItemConcernedsByClass(dependentItemConcerneds);
		}

		return members;
	}

	@Override
	public CreatorId attachCreatorId(DependentItemConcerned... dependentItemConcerneds) {
		if (dependentItemConcerneds != null) {
			creatorId.attach(dependentItemConcerneds);
			retainItemConcernedsByClass(dependentItemConcerneds);
		}

		return creatorId;
	}

	@Override
	public LastSeqs attachLastSeqs(DependentItemConcerned... dependentItemConcerneds) {
		if (dependentItemConcerneds != null) {
			lastSeqs.attach(dependentItemConcerneds);
			retainItemConcernedsByClass(dependentItemConcerneds);
		}

		return lastSeqs;
	}

	/**
	 * replaced by {@code attachUpdatedSnapshotMessage}
	 */
	@Override
	@Deprecated
	public CurrSnapshotVersion attachCurrSnapshotVersion(DependentItemConcerned... dependentItemConcerneds) {
		if (dependentItemConcerneds != null) {
			currSnapshotVersion.attach(dependentItemConcerneds);
			retainItemConcernedsByClass(dependentItemConcerneds);
		}

		return currSnapshotVersion;
	}

	@Override
	public EventFlow attachEventFlow(DependentItemConcerned... dependentItemConcerneds) {
		if (dependentItemConcerneds != null) {
			eventFlow.attach(dependentItemConcerneds);
			retainItemConcernedsByClass(dependentItemConcerneds);
		}

		return eventFlow;
	}

	@Override
	public BlackList4PubKey attachBlackList4PubKey(DependentItemConcerned... dependentItemConcerneds) {
		if (dependentItemConcerneds != null) {
			blackList4PubKey.attach(dependentItemConcerneds);
			retainItemConcernedsByClass(dependentItemConcerneds);
		}

		return blackList4PubKey;
	}

	@Override
	public PrivateKey attachPrivateKey(DependentItemConcerned... dependentItemConcerneds) {
		if (dependentItemConcerneds != null) {
			privateKey.attach(dependentItemConcerneds);
			retainItemConcernedsByClass(dependentItemConcerneds);
		}

		return privateKey;
	}

	@Override
	public AllQueues attachAllQueues(DependentItemConcerned... dependentItemConcerneds) {
		if (dependentItemConcerneds != null) {
			allQueues.attach(dependentItemConcerneds);
			retainItemConcernedsByClass(dependentItemConcerneds);
		}

		return allQueues;
	}

	@Override
	public DirectCommunicator attachDirectCommunicator(DependentItemConcerned... dependentItemConcerneds) {
		if (dependentItemConcerneds != null) {
			directCommunicator.attach(dependentItemConcerneds);
			retainItemConcernedsByClass(dependentItemConcerneds);
		}

		return directCommunicator;
	}

	@Override
	public UpdatedSnapshotMessage attachUpdatedSnapshotMessage(DependentItemConcerned... dependentItemConcerneds) {
		if (dependentItemConcerneds != null) {
			updatedSnapshotMessage.attach(dependentItemConcerneds);
			retainItemConcernedsByClass(dependentItemConcerneds);
		}

		return updatedSnapshotMessage;
	}

	@Override
	public Stat attachStat(DependentItemConcerned... dependentItemConcerneds) {
		if (dependentItemConcerneds != null) {
			stat.attach(dependentItemConcerneds);
			retainItemConcernedsByClass(dependentItemConcerneds);
		}

		return stat;
	}

	@Override
	public SS attachSS(DependentItemConcerned... dependentItemConcerneds) {
		if (dependentItemConcerneds != null) {
			ss.attach(dependentItemConcerneds);
			retainItemConcernedsByClass(dependentItemConcerneds);
		}

		return ss;
	}

	@Override
	public Wal attachWal(DependentItemConcerned... dependentItemConcerneds) {
		if (dependentItemConcerneds != null) {
			wal.attach(dependentItemConcerneds);
			retainItemConcernedsByClass(dependentItemConcerneds);
		}

		return wal;
	}

}
