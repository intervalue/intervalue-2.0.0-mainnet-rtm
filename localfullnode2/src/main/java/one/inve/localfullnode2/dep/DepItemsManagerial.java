package one.inve.localfullnode2.dep;

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
interface DepItemsManagerial {
	ShardId attachShardId(DependentItemConcerned... dependentItemConcerneds);

	ShardCount attachShardCount(DependentItemConcerned... dependentItemConcerneds);

	NValue attachNValue(DependentItemConcerned... dependentItemConcerneds);

	LocalFullNodes attachLocalFullNodes(DependentItemConcerned... dependentItemConcerneds);

	DBId attachDBId(DependentItemConcerned... dependentItemConcerneds);

	Mnemonic attachMnemonic(DependentItemConcerned... dependentItemConcerneds);

	PublicKey attachPublicKey(DependentItemConcerned... dependentItemConcerneds);
}
