package one.inve.localfullnode2.dep.items;

import one.inve.localfullnode2.dep.DependentItem;
import one.inve.localfullnode2.store.IEventFlow;

public class EventFlow extends DependentItem {
	private IEventFlow eventFlow;

	public IEventFlow get() {
		return eventFlow;
	}

	public void set(IEventFlow eventFlow) {
		this.eventFlow = eventFlow;
		nodifyAll();
	}

}
