package one.inve.localfullnode2.dep;

import java.util.concurrent.BlockingQueue;

import org.junit.Test;

import junit.framework.Assert;
import one.inve.localfullnode2.dep.items.AllQueues;
import one.inve.localfullnode2.staging.StagingArea;
import one.inve.localfullnode2.store.EventBody;

public class AllQueuesTest {
	// demonstrate how to pass a AllQueues item between producer and consumer
	@Test
	public void testAllQueues() {
		AllQueuesProducer producer = new AllQueuesProducer();
		AllQueuesConsumer consumer = new AllQueuesConsumer();

		DepItemsManager.getInstance().attachAllQueues(consumer);

		producer.produce();// see all things
	}

	public static class AllQueuesProducer {

		public void produce() {
			AllQueues allQueues = DepItemsManager.getInstance().attachAllQueues(null);
			StagingArea stagingArea = new StagingArea();

			// ElementModifiable m = (e) -> allQueues.set(stagingArea);

			BlockingQueue<byte[]> messageQueue = stagingArea.createQueue(byte[].class, StagingArea.MessageQueueName,
					10000000, null);
			BlockingQueue<EventBody> eventSaveQueue = stagingArea.createQueue(EventBody.class,
					StagingArea.EventSaveQueueName, 10000000, null);

			messageQueue.offer("hello".getBytes());
			messageQueue.offer("world".getBytes());
			messageQueue.offer("shao".getBytes());

			allQueues.set(stagingArea);

			directOperation();

		}

		private void directOperation() {
			AllQueues allQueues = DepItemsManager.getInstance().attachAllQueues(null);

			StagingArea stagingArea = allQueues.get();
			BlockingQueue<byte[]> messageQueue = stagingArea.getQueue(byte[].class, StagingArea.MessageQueueName);

			byte[] shao = messageQueue.poll();
			System.out.println(new String(shao));
			Assert.assertEquals(new String(shao), "shao");
		}

	}

	public static class AllQueuesConsumer implements DependentItemConcerned {

		public void consume(AllQueues allQueues) {
			StagingArea stagingArea = allQueues.get();
			BlockingQueue<byte[]> messageQueue = stagingArea.getQueue(byte[].class, StagingArea.MessageQueueName);

			byte[] hello = messageQueue.poll();
			System.out.println(new String(hello));
			Assert.assertEquals(new String(hello), "hello");

			byte[] world = messageQueue.poll();
			System.out.println(new String(world));
			Assert.assertEquals(new String(world), "world");

		}

		@Override
		public void update(DependentItem item) {
			if (item instanceof AllQueues) {
				consume(((AllQueues) item));
			}

		}

	}

}
