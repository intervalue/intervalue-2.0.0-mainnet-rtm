package one.inve.localfullnode2.dep.items;

import one.inve.bean.message.SnapshotMessage;
import one.inve.localfullnode2.dep.DependentItem;

@Deprecated
public class UpdatedSnapshotMessage extends DependentItem {
	private SnapshotMessage snapshotMessage;

	public void set(SnapshotMessage snapshotMessage) {
		this.snapshotMessage = snapshotMessage;
		nodifyAll();
	}

	public SnapshotMessage get() {
		return snapshotMessage;
	}

}
