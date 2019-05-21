package one.inve.localfullnode2.lc;

import java.util.concurrent.TimeUnit;

import one.inve.localfullnode2.dep.DepItemsManager;
import one.inve.localfullnode2.gossip.GossipDependency;
import one.inve.localfullnode2.gossip.GossipDependent;
import one.inve.localfullnode2.gossip.Gossiper;
import one.inve.localfullnode2.gossip.persistence.NewGossipEventsPersistence;
import one.inve.localfullnode2.gossip.persistence.NewGossipEventsPersistenceDependency;
import one.inve.localfullnode2.hashnet.HashneterUpstream;
import one.inve.localfullnode2.hashnet.HashneterUpstreamDependency;
import one.inve.localfullnode2.message.MessagePersistence;
import one.inve.localfullnode2.message.MessagePersistenceDependency;
import one.inve.localfullnode2.message.MessagesExe;
import one.inve.localfullnode2.message.MessagesExeDependent;
import one.inve.localfullnode2.message.MessagesVerification;
import one.inve.localfullnode2.message.MessagesVerificationDependency;
import one.inve.localfullnode2.postconsensus.exe.EventsExe;
import one.inve.localfullnode2.postconsensus.exe.EventsExeDependency;
import one.inve.localfullnode2.postconsensus.readout.EventsReadout;
import one.inve.localfullnode2.postconsensus.readout.EventsReadoutDependency;
import one.inve.localfullnode2.postconsensus.sorting.EventsSorting;
import one.inve.localfullnode2.postconsensus.sorting.EventsSortingDependency;

/**
 * 
 * 
 * Copyright © CHXX Co.,Ltd. All rights reserved.
 * 
 * @Description: build a loop to execute(nine steps together) over and over
 *               again and take a sleep at the end of tasks.
 * @author: Francis.Deng
 * @date: May 21, 2019 12:17:08 AM
 * @version: V1.0
 */
public class StoppableInfiniteLoop extends LazyLifecycle implements ILifecycle {

	private boolean stopMe = true;// control the loop

	@Override
	public void start() {

		if (!isRunning()) {
			super.start();
			stopMe = false;

			GossipDependent gossipDep = null;
			Gossiper g = new Gossiper();

			NewGossipEventsPersistenceDependency newGossipEventsPersistenceDep = null;
			NewGossipEventsPersistence newGossipEventsPersistence = new NewGossipEventsPersistence();

			HashneterUpstreamDependency hashneterUpstreamDep = null;
			HashneterUpstream hashneterUpstream = new HashneterUpstream();

			EventsReadoutDependency eventsReadoutDependency = null;
			EventsReadout eventsReadout = new EventsReadout();

			EventsSortingDependency eventsSortingDependency = null;
			EventsSorting evetnsSorting = new EventsSorting();

			EventsExeDependency eventsExeDependency = null;

			MessagesVerificationDependency messagesVerificationDependency = null;

			MessagesExeDependent messagesExeDependent = null;

			MessagePersistenceDependency messagePersistenceDependency = null;

			while (!stopMe) {

				// first,gossip communication
				gossipDep = DepItemsManager.getInstance().getItemConcerned(GossipDependency.class);
				g.talkGossip(gossipDep);

				// second,save new event
				newGossipEventsPersistenceDep = DepItemsManager.getInstance()
						.getItemConcerned(NewGossipEventsPersistenceDependency.class);
				newGossipEventsPersistence.persistNewEvents(newGossipEventsPersistenceDep);

				// third,send new event to Hashnet (call it Hashneter Upstream)
				hashneterUpstreamDep = DepItemsManager.getInstance()
						.getItemConcerned(HashneterUpstreamDependency.class);
				hashneterUpstream.pull(hashneterUpstreamDep);

				// fourth,read them from hashnet
				eventsReadoutDependency = DepItemsManager.getInstance().getItemConcerned(EventsReadoutDependency.class);
				eventsReadout.read(eventsReadoutDependency);

				// fifth,sort all events
				eventsSortingDependency = DepItemsManager.getInstance().getItemConcerned(EventsSortingDependency.class);
				evetnsSorting.work(eventsSortingDependency);

				// sixth,execute all events
				eventsExeDependency = DepItemsManager.getInstance().getItemConcerned(EventsExeDependency.class);
				EventsExe eventsExe = new EventsExe(eventsExeDependency);
				eventsExe.run();

				// seventh,verify all messages
				messagesVerificationDependency = DepItemsManager.getInstance()
						.getItemConcerned(MessagesVerificationDependency.class);
				MessagesVerification messagesVerification = new MessagesVerification(messagesVerificationDependency);
				messagesVerification.verifyMessages();

				// eighth,execute all messages
				messagesExeDependent = DepItemsManager.getInstance().getItemConcerned(MessagesExeDependent.class);
				MessagesExe messagesExe = new MessagesExe(messagesExeDependent);
				messagesExe.exe();

				// ninth,save all messages and system's messages
				messagePersistenceDependency = DepItemsManager.getInstance()
						.getItemConcerned(MessagePersistenceDependency.class);
				MessagePersistence messagePersistence = new MessagePersistence(messagePersistenceDependency);
				messagePersistence.persisMessages();
				messagePersistence.persistSystemMessages();

				sleep(5);// take a break
			}

			super.stop();
		}
	}

	@Override
	public void stop() {
		stopMe = true;

	}

	@Override
	public boolean isRunning() {
		return super.isRunning();
	}

	private void sleep(int seconds) {
		try {
			TimeUnit.SECONDS.sleep(seconds);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
