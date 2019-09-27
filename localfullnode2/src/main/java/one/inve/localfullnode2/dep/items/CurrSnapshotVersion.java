package one.inve.localfullnode2.dep.items;

import java.math.BigInteger;

import one.inve.localfullnode2.dep.DependentItem;

@Deprecated
public class CurrSnapshotVersion extends DependentItem {
	private BigInteger currSnapshotVersion;

	public BigInteger get() {
		return currSnapshotVersion;
	}

	public void set(BigInteger currSnapshotVersion) {
		this.currSnapshotVersion = currSnapshotVersion;
		nodifyAll();
	}
}
