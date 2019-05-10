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
 * 
 * Copyright © CHXX Co.,Ltd. All rights reserved.
 * 
 * @Description: item/item concerned model interface
 * @author: Francis.Deng
 * @date: May 8, 2019 2:44:15 AM
 * @version: V1.0
 */
public interface DepItemsManagerial {
	ShardId attachShardId(DependentItemConcerned... dependentItemConcerneds);

	ShardCount attachShardCount(DependentItemConcerned... dependentItemConcerneds);

	NValue attachNValue(DependentItemConcerned... dependentItemConcerneds);

	LocalFullNodes attachLocalFullNodes(DependentItemConcerned... dependentItemConcerneds);

	DBId attachDBId(DependentItemConcerned... dependentItemConcerneds);

	Mnemonic attachMnemonic(DependentItemConcerned... dependentItemConcerneds);

	PublicKey attachPublicKey(DependentItemConcerned... dependentItemConcerneds);
}
