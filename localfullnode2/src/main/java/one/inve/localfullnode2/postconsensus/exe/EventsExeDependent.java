package one.inve.localfullnode2.postconsensus.exe;

import java.math.BigInteger;
import java.util.HashSet;
import java.util.concurrent.LinkedBlockingQueue;

import com.alibaba.fastjson.JSONObject;

import one.inve.bean.message.Contribution;
import one.inve.core.EventBody;
import one.inve.localfullnode2.store.rocks.INosql;

public interface EventsExeDependent {
	long getCreatorId();

	String msgHashTreeRoot();

	void setTotalConsEventCount(BigInteger totalConsEventCount);

	BigInteger getTotalConsEventCount();

	HashSet<Contribution> getContributions();

	void setConsMessageMaxId(BigInteger consMessageMaxId);

	BigInteger getConsMessageMaxId();

	String dbId();

	INosql getNosql(String dbId);

	// the source
	LinkedBlockingQueue<EventBody> getConsEventHandleQueue();

	// the destination
	LinkedBlockingQueue<JSONObject> getConsMessageVerifyQueue();

}
