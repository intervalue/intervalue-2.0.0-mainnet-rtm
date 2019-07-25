package one.inve.localfullnode2.dep.items;

import one.inve.localfullnode2.dep.DependentItem;

public class PublicKey extends DependentItem {
	private java.security.PublicKey publicKey;

	public void set(java.security.PublicKey publicKey) {
		this.publicKey = publicKey;
		nodifyAll();
	}

	public java.security.PublicKey get() {
		return publicKey;
	}
}
