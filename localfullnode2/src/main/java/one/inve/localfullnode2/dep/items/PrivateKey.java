package one.inve.localfullnode2.dep.items;

import one.inve.localfullnode2.dep.DependentItem;

public class PrivateKey extends DependentItem {
	private java.security.PrivateKey privateKey;

	public java.security.PrivateKey get() {
		return privateKey;
	}

	public void set(java.security.PrivateKey privateKey) {
		this.privateKey = privateKey;
	}

}
