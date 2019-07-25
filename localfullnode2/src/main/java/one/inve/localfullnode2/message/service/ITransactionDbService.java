package one.inve.localfullnode2.message.service;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

import com.alibaba.fastjson.JSONObject;

import one.inve.bean.node.LocalFullNode;
import one.inve.localfullnode2.store.rocks.Message;

public interface ITransactionDbService {
	boolean saveLocalFullNodes2Database(List<LocalFullNode> localFullNodes, String dbId);

	byte[][][] queryPubkeysFromDatabase(String dbId);

	ArrayList<Message> queryTransactionHistory(String address, String dbId);

	List<JSONObject> queryMissingTransactionsBeforeSnapshotPoint(String message, BigInteger requestConsMessageMaxId,
			String dbId);

	List<JSONObject> queryTransactionsByRange(BigInteger firstTranId, BigInteger lastTranId, String dbId);

	List<String> queryMessageHashByRange(BigInteger firstTranId, BigInteger lastTranId, String dbId);
}
