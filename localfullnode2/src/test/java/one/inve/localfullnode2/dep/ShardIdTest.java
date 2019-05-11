package one.inve.localfullnode2.dep;

import org.junit.Test;

import one.inve.localfullnode2.dep.items.ShardId;

/**
 * 
 * 
 * Copyright © CHXX Co.,Ltd. All rights reserved.
 * 
 * @Description: simulate a scenario of producer/consumer.
 * @author: Francis.Deng
 * @date: May 8, 2019 1:52:30 AM
 * @version: V1.0
 */
public class ShardIdTest {
	// demonstrate how to pass a shardId item between producer and consumer
	@Test
	public void testShardId() {
		ShardIdProducer producer = new ShardIdProducer();
		ShardIdConsumer consumer = new ShardIdConsumer();
		SilentConsumer silentConsumer = new SilentConsumer();

		producer.produceOneBit();// see nothing

		DepItemsManager.getInstance().attachShardId(consumer, silentConsumer);

		producer.produceTwoBits();// see all things
	}

	public static class ShardIdProducer {

		public void produceOneBit() {
			ShardId shardId = DepItemsManager.getInstance().attachShardId(null);
			shardId.set(1);
			shardId.set(2);
			shardId.set(3);
			shardId.set(4);
			shardId.set(5);
		}

		public void produceTwoBits() {
			ShardId shardId = DepItemsManager.getInstance().attachShardId(null);
			shardId.set(11);
			shardId.set(22);
			shardId.set(33);
			shardId.set(44);
			shardId.set(55);
		}

	}

	public static class ShardIdConsumer implements DependentItemConcerned {

		public void consume() {
			// ShardId shardId = DepManager.getInstance().attachShardId(null);

		}

		private void consumeOne(ShardId shardId) {
			System.out.println("eat out " + shardId.get());
		}

		@Override
		public void update(DependentItem item) {
			if (item instanceof ShardId) {
				consumeOne(((ShardId) item));
			}

		}

	}

	public static class SilentConsumer implements DependentItemConcerned {

		private ShardId shardId;

		public void consume() {
			// ShardId shardId = DepManager.getInstance().attachShardId(null);

		}

		@Override
		public void update(DependentItem item) {
			set(this, item);

		}

	}
}
