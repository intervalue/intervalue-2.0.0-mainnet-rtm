package one.inve.localfullnode2.dep.items;

import one.inve.localfullnode2.dep.DependentItem;

public class NValue extends DependentItem {
	private int nValue;

	public int get() {
		return nValue;
	}

	public void set(int nValue) {
		this.nValue = nValue;
		nodifyAll();
	}

}
