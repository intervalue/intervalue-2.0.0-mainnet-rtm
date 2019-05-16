package one.inve.localfullnode2.dep.items;

import one.inve.localfullnode2.dep.DependentItem;

public class LastSeqs extends DependentItem {
	private long[][] lastSeqs;

	public long[][] get() {
		return lastSeqs;
	}

	public void set(long[][] lastSeqs) {
		this.lastSeqs = lastSeqs;
		nodifyAll();
	}

}
