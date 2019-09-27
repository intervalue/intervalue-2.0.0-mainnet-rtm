package one.inve.localfullnode2.dep.items;

import one.inve.localfullnode2.dep.DependentItem;
import one.inve.localfullnode2.store.IEventStore;

public class EventStore extends DependentItem {
	private IEventStore eventStore;

	public IEventStore get() {
		return eventStore;
	}

	public void set(IEventStore eventStore) {
		this.eventStore = eventStore;
		nodifyAll();
	}
}
