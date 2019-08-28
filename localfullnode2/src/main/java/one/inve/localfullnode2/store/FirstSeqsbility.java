package one.inve.localfullnode2.store;

import java.math.BigInteger;
import java.util.concurrent.atomic.AtomicLongArray;

import one.inve.localfullnode2.store.rocks.key.FirstSeqKey;

/**
 * 
 * Copyright © INVE FOUNDATION. All rights reserved.
 * 
 * @ClassName: FirstSeqsbility
 * @Description: first seqs is a variable level due to snapshot mechanism.
 * @author Francis.Deng [francis_xiiiv@163.com]
 * @date Aug 21, 2018
 *
 */
public class FirstSeqsbility {
	private final FirstSeqsHolder firstSeqs = new FirstSeqsHolder();

	private static BigInteger Two = new BigInteger("2");

//	public FirstSeqs(int shardCount, int nValue) {
//		for (int i = 0; i < shardCount; i++) {
//			AtomicLongArray firstSeqsInShard = new AtomicLongArray(nValue);
//			for (int j = 0; j < nValue; j++) {
//				firstSeqsInShard.set(j, -1);
//			}
//			firstSeqs.put(i, firstSeqsInShard);
//		}
//	}

	// via binary search algorithm
	public void search(int shardCount, int nValue, EventStore eventStore) {

		for (int i = 0; i < shardCount; i++) {
			AtomicLongArray firstSeqsInShard = new AtomicLongArray(nValue);

			for (int j = 0; j < nValue; j++) {
				BigInteger seq = binarySearch(i, j, BigInteger.ZERO, eventStore.getLastSeq(i, j), eventStore);
				firstSeqsInShard.set(j, seq.longValue());
			}

			firstSeqs.put(i, firstSeqsInShard);
		}
	}

	// key function to find the bottom of event sequence
	protected BigInteger binarySearch(int shardId, int idInShard, BigInteger fromSeq, BigInteger toSeq,
			EventStore eventStore) {

		if (isFirstSeq(shardId, idInShard, toSeq, eventStore))
			return toSeq;

		BigInteger seq = toSeq.subtract(fromSeq).divide(Two);
		EventKeyPair p = new EventKeyPair(shardId, idInShard, seq.longValue());

		if (isFirstSeq(shardId, idInShard, seq, eventStore))
			return seq;

		if (eventStore.exist(p)) {
			return binarySearch(shardId, idInShard, fromSeq, seq, eventStore);
		} else {
			return binarySearch(shardId, idInShard, seq, toSeq, eventStore);
		}
	}

	protected boolean isFirstSeq(int shardId, int idInShard, BigInteger seq, EventStore eventStore) {
		EventKeyPair p = new EventKeyPair(shardId, idInShard, seq.longValue());
		EventKeyPair pPlusOne = new EventKeyPair(shardId, idInShard, seq.longValue() + 1);
		EventKeyPair pMinusOne = new EventKeyPair(shardId, idInShard, seq.longValue() - 1);
		boolean isFirstSeq = false;

		if (eventStore.exist(p) && eventStore.exist(pPlusOne) && !eventStore.exist(pMinusOne))
			isFirstSeq = true;

		return isFirstSeq;
	}

	public static interface EventStore {
		boolean exist(EventKeyPair eventKey);

		BigInteger getFirstSeq(int shardId, int idInShard);

		BigInteger getLastSeq(int shardId, int idInShard);

		void put(FirstSeqKey firstSeqKey);
	}
}
