package one.inve.localfullnode2.sync.msg;

import one.inve.localfullnode2.dep.DependentItem;
import one.inve.localfullnode2.dep.DependentItemConcerned;
import one.inve.localfullnode2.dep.items.DBId;
import one.inve.localfullnode2.store.rocks.INosql;
import one.inve.localfullnode2.store.rocks.RocksJavaUtil;

public class MsgIntrospectorDependency implements MsgIntrospectorDependent, DependentItemConcerned {
	private DBId dbId;

	@Override
	public void update(DependentItem item) {
		set(this, item);
	}

	@Override
	public String getDbId() {
		return dbId.get();
	}

	@Override
	public INosql getNosql() {
		return new RocksJavaUtil(dbId.get());
	}

}
