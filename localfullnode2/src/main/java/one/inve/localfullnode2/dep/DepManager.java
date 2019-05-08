package one.inve.localfullnode2.dep;

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
public final class DepManager {
	private ShardId shardId;

	private DepManager() {
		shardId = new ShardId();
	}

	private static class SingletonHelper {
		private static final DepManager INSTANCE = new DepManager();
	}

	public static DepManager getInstance() {
		return SingletonHelper.INSTANCE;
	}

	public ShardId attachShardId(DependentItemConcerned... dependentItemConcerneds) {
		if (dependentItemConcerneds != null) {
			shardId.attach(dependentItemConcerneds);
		}

		return shardId;
	}
}
