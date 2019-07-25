package one.inve.localfullnode2.hashnet;

import one.inve.localfullnode2.dep.DependentItem;
import one.inve.localfullnode2.dep.DependentItemConcerned;
import one.inve.localfullnode2.dep.items.ShardCount;

/**
 * 
 * Copyright © INVE FOUNDATION. All rights reserved.
 * 
 * @Description: The class is depending on {@link IHashneter} and
 *               {@link HashneterDependent},all of which were pushed before.
 * @author: Francis.Deng
 * @date: May 19, 2019 6:41:36 PM
 * @version: V1.0
 */
public class HashneterUpstreamDependency implements HashneterUpstreamDependent, DependentItemConcerned {

	private ShardCount shardCount;

	private IHashneter hashneter;
	private HashneterDependent hashneterDep;

	@Override
	public int getShardCount() {
		return shardCount.get();
	}

	@Override
	public void addToHashnet(int shardId) {
		hashneter.addToHashnet(hashneterDep, shardId);

	}

	@Override
	public void update(DependentItem item) {
		set(this, item);

	}

	public void set(IHashneter hashneter, HashneterDependent hashneterDep) {
		this.hashneter = hashneter;
		this.hashneterDep = hashneterDep;
	}

}
