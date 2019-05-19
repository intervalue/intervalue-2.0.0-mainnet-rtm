package one.inve.localfullnode2.dep;

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
import one.inve.localfullnode2.dep.items.ShardCount;
import one.inve.localfullnode2.dep.items.ShardId;
import one.inve.localfullnode2.dep.items.Stat;
import one.inve.localfullnode2.dep.items.UpdatedSnapshotMessage;

/**
 * 
 * 
 * Copyright © CHXX Co.,Ltd. All rights reserved.
 * 
 * @Description: item/item concerned model interface
 * @author: Francis.Deng
 * @date: May 8, 2019 2:44:15 AM
 * @version: V1.0
 */
public interface DepItemsManagerial {
	<T> T getItemConcerned(Class<T> itemConcernedClass);

	ShardId attachShardId(DependentItemConcerned... dependentItemConcerneds);

	ShardCount attachShardCount(DependentItemConcerned... dependentItemConcerneds);

	NValue attachNValue(DependentItemConcerned... dependentItemConcerneds);

	LocalFullNodes attachLocalFullNodes(DependentItemConcerned... dependentItemConcerneds);

	DBId attachDBId(DependentItemConcerned... dependentItemConcerneds);

	Mnemonic attachMnemonic(DependentItemConcerned... dependentItemConcerneds);

	PublicKey attachPublicKey(DependentItemConcerned... dependentItemConcerneds);

	Members attachMembers(DependentItemConcerned... dependentItemConcerneds);

	CreatorId attachCreatorId(DependentItemConcerned... dependentItemConcerneds);

	LastSeqs attachLastSeqs(DependentItemConcerned... dependentItemConcerneds);

	CurrSnapshotVersion attachCurrSnapshotVersion(DependentItemConcerned... dependentItemConcerneds);

	EventFlow attachEventFlow(DependentItemConcerned... dependentItemConcerneds);

	BlackList4PubKey attachBlackList4PubKey(DependentItemConcerned... dependentItemConcerneds);

	PrivateKey attachPrivateKey(DependentItemConcerned... dependentItemConcerneds);

	AllQueues attachAllQueues(DependentItemConcerned... dependentItemConcerneds);

	DirectCommunicator attachDirectCommunicator(DependentItemConcerned... dependentItemConcerneds);

	UpdatedSnapshotMessage attachUpdatedSnapshotMessage(DependentItemConcerned... dependentItemConcerneds);

	Stat attachStat(DependentItemConcerned... dependentItemConcerneds);
}
