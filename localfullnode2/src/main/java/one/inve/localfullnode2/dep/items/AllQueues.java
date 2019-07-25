package one.inve.localfullnode2.dep.items;

import one.inve.localfullnode2.dep.DependentItem;
import one.inve.localfullnode2.staging.StagingArea;

public class AllQueues extends DependentItem {
	private StagingArea area;

	public StagingArea get() {
		return area;
	}

	public void set(StagingArea area) {
		this.area = area;
		nodifyAll();
	}

}
