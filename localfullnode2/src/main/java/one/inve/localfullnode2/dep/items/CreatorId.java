package one.inve.localfullnode2.dep.items;

import one.inve.localfullnode2.dep.DependentItem;

public class CreatorId extends DependentItem {
	private long creatorId;

	public long get() {
		return creatorId;
	}

	public void set(long creatorId) {
		this.creatorId = creatorId;
	}

}
