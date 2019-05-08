package one.inve.localfullnode2.message;

import java.math.BigInteger;
import java.util.concurrent.BlockingQueue;

import com.alibaba.fastjson.JSONObject;

import one.inve.localfullnode2.store.rocks.INosql;

public interface MessagePersistenceDependent {
	//// the source for messages
	BlockingQueue<JSONObject> getConsMessageSaveQueue();

	void setConsMessageCount(BigInteger consMessageCount);

	BigInteger getConsMessageCount();

	String getDbId();

	INosql getNosql();

	// the source for system messages
	BlockingQueue<JSONObject> getSystemAutoTxSaveQueue();

}
