package one.inve.localfullnode2.lc;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import one.inve.localfullnode2.conf.Config;
import one.inve.localfullnode2.dep.DepItemsManager;
import one.inve.localfullnode2.gossip.GossipDependency;
import one.inve.localfullnode2.gossip.GossipDependent;
import one.inve.localfullnode2.gossip.Gossiper;
import one.inve.localfullnode2.gossip.LostMotionModel;
import one.inve.localfullnode2.gossip.persistence.NewGossipEventsPersistence;
import one.inve.localfullnode2.gossip.persistence.NewGossipEventsPersistenceDependency;
import one.inve.localfullnode2.hashnet.HashneterUpstream;
import one.inve.localfullnode2.hashnet.HashneterUpstreamDependency;
import one.inve.localfullnode2.message.MessagePersistence;
import one.inve.localfullnode2.message.MessagePersistenceDependency;
import one.inve.localfullnode2.message.MessagesExe;
import one.inve.localfullnode2.message.MessagesExeDependency;
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
 *               again and take a sleep every round.
 * @author: Francis.Deng
 * @date: May 21, 2019 12:17:08 AM
 * @version: V1.0
 */
public class FormalEventMessageLoop extends LazyLifecycle implements ILifecycle {
	private static final Logger logger = LoggerFactory.getLogger(FormalEventMessageLoop.class);

	private volatile boolean stopMe = true;// control the loop

	@Override
	public void start() {
		new Thread(() -> startCore()).start();
	}

	public void startCore() {

		if (!isRunning()) {
			super.start();
			stopMe = false;

			LostMotionModel lostMotionModel = new LostMotionModel(Config.LostMotionModel_EXPONENT);

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

			MessagesExeDependency messagesExeDependency = null;

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
				messagesExeDependency = DepItemsManager.getInstance().getItemConcerned(MessagesExeDependency.class);
				MessagesExe messagesExe = new MessagesExe(messagesExeDependency);
				messagesExe.exe();

				// ninth,save all messages and system's messages
				messagePersistenceDependency = DepItemsManager.getInstance()
						.getItemConcerned(MessagePersistenceDependency.class);
				MessagePersistence messagePersistence = new MessagePersistence(messagePersistenceDependency);
				messagePersistence.persisMessages();
				messagePersistence.persistSystemMessages();

				long milliSeconds = (long) (lostMotionModel.getYVar(g.getLostMotionRound()));
				sleepMilliSeconds(milliSeconds);// take a break
			}

			super.stop();

			logger.info("<<formal event and message loop>> is stopped......");
		}
	}

	@Override
	public void stop() {
		stopMe = true;
//
//		while (isRunning()) {
//
//		}

	}

	@Override
	public boolean isRunning() {
		return super.isRunning();
	}

}
