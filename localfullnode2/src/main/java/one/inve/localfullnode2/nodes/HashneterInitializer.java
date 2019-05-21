package one.inve.localfullnode2.nodes;

import one.inve.localfullnode2.dep.DepItemsManager;
import one.inve.localfullnode2.gossip.GossipDependency;
import one.inve.localfullnode2.hashnet.Hashneter;
import one.inve.localfullnode2.hashnet.HashneterDependency;
import one.inve.localfullnode2.hashnet.HashneterUpstreamDependency;
import one.inve.localfullnode2.postconsensus.readout.EventsReadoutDependency;
import one.inve.localfullnode2.store.IEventFlow;
import one.inve.localfullnode2.store.IEventStore;

/**
 * Copyright © CHXX Co.,Ltd. All rights reserved.
 * 
 * @Description: initialize hashneter
 * @author: Francis.Deng
 * @date: May 19, 2019 11:53:44 PM
 * @version: V1.0
 */
public abstract class HashneterInitializer extends LocalFullNodeSkeleton {

	private IEventFlow eventFlow;
	private IEventStore eventStore;

	protected Hashneter initHashneter() {
		Hashneter hashneter = new Hashneter();

		HashneterDependency hashneterDep = DepItemsManager.getInstance().getItemConcerned(HashneterDependency.class);
		HashneterUpstreamDependency hashneterUpstreamDep = DepItemsManager.getInstance()
				.getItemConcerned(HashneterUpstreamDependency.class);
		EventsReadoutDependency eventsReadoutDep = DepItemsManager.getInstance()
				.getItemConcerned(EventsReadoutDependency.class);
		GossipDependency gossipDependency = DepItemsManager.getInstance().getItemConcerned(GossipDependency.class);

		try {
			hashneter.initHashnet(hashneterDep);

			hashneterUpstreamDep.set(hashneter, hashneterDep);// which indicats that {@code hashneterUpstreamDep} is
																// based on {@code hashneter},{@code hashneterDep}
			eventsReadoutDep.setHashneter(hashneter);

			set(hashneter.getEventFlow(), hashneter.getEventStore());
			gossipDependency.setEventFlow(hashneter.getEventFlow());

			return hashneter;
		} catch (InterruptedException e) {
			e.printStackTrace();
		}

		return null;
	}

	private void set(IEventFlow eventFlow, IEventStore eventStore) {
		this.eventFlow = eventFlow;
		this.eventStore = eventStore;
	}

	public IEventFlow getEventFlow() {
		return eventFlow;
	}

	public IEventStore getEventStore() {
		return eventStore;
	}
}
