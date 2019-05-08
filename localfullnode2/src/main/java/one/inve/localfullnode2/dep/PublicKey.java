package one.inve.localfullnode2.dep;

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
