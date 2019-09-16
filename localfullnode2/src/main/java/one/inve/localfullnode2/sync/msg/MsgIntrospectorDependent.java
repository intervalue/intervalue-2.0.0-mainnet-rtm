package one.inve.localfullnode2.sync.msg;

import one.inve.localfullnode2.store.rocks.INosql;

public interface MsgIntrospectorDependent {
	String getDbId();

	INosql getNosql();
}
