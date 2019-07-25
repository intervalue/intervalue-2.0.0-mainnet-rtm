package one.inve.localfullnode2.dep.items;

import java.util.List;

import one.inve.localfullnode2.dep.DependentItem;

public class BlackList4PubKey extends DependentItem {
	private List<String> blackList4PubKey;

	public List<String> get() {
		return blackList4PubKey;
	}

	public void set(List<String> blackList4PubKey) {
		this.blackList4PubKey = blackList4PubKey;
		nodifyAll();
	}
}
