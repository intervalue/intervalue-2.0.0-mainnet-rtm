package one.inve.localfullnode2.lc;

import java.util.concurrent.locks.ReentrantReadWriteLock.WriteLock;

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
 * @Description: exclusive writing event
 * @author: Francis.Deng
 * @date: Jun 17, 2019 8:04:29 PM
 * @version: V1.0
 */
public class WriteEventExclusiveEventMessageLoop extends LazyLifecycle implements ILifecycle {
	private static final Logger logger = LoggerFactory.getLogger(WriteEventExclusiveEventMessageLoop.class);

	private volatile boolean stopMe = true;// control the loop

	private final WriteLock writeLock;

	public WriteEventExclusiveEventMessageLoop(WriteLock writeLock) {
		super();
		this.writeLock = writeLock;
	}

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
				writeLock.lock();
				try {
					logger.info("event gossip is running(mutually exclusive with gossipMyMaxSeqList4Consensus )");

					// first,gossip communication
					gossipDep = DepItemsManager.getInstance().getItemConcerned(GossipDependency.class);
					g.talkGossip(gossipDep);

					// second,save new event
					newGossipEventsPersistenceDep = DepItemsManager.getInstance()
							.getItemConcerned(NewGossipEventsPersistenceDependency.class);
					newGossipEventsPersistence.persistNewEvents(newGossipEventsPersistenceDep);
				} finally {
					writeLock.unlock();
				}

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

				long milliSeconds = (long) (lostMotionModel.getYVar(g.getLostMotionRound()) * 1000);
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
