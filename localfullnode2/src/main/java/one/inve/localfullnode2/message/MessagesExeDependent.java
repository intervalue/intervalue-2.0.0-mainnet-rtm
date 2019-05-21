package one.inve.localfullnode2.message;

import java.math.BigInteger;
import java.util.concurrent.BlockingQueue;

import com.alibaba.fastjson.JSONObject;

import one.inve.localfullnode2.store.rocks.INosql;

public interface MessagesExeDependent {
	// the source
	BlockingQueue<JSONObject> getConsMessageHandleQueue();

	// the destination
	BlockingQueue<JSONObject> getConsMessageSaveQueue();

	// the destination
	BlockingQueue<JSONObject> getSystemAutoTxSaveQueue();

	int getMultiple();

	int getShardCount();

	// int getShardId();

	INosql getNosql();

	String getDbId();

	// void setSystemAutoTxMaxId(BigInteger systemAutoTxMaxId);
	void addSystemAutoTxMaxId(long delta);

	BigInteger getSystemAutoTxMaxId();

	void setTotalFeeBetween2Snapshots(BigInteger totalFeeBetween2Snapshots);

	BigInteger getTotalFeeBetween2Snapshots();

	IWorldStateService getWorldStateService();
}
