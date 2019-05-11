package one.inve.localfullnode2.dep.items;

import java.math.BigInteger;

import one.inve.localfullnode2.dep.DependentItem;

public class CurrSnapshotVersion extends DependentItem {
	private BigInteger currSnapshotVersion;

	public BigInteger ge() {
		return currSnapshotVersion;
	}

	public void set(BigInteger currSnapshotVersion) {
		this.currSnapshotVersion = currSnapshotVersion;
	}
}
