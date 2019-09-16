package one.inve.localfullnode2.dep.items;

import java.util.concurrent.ConcurrentHashMap;

import one.inve.localfullnode2.dep.DependentItem;
import one.inve.localfullnode2.store.AtomicLongArrayWrapper;

public class FirstSeqs extends DependentItem {
	private ConcurrentHashMap<Integer, AtomicLongArrayWrapper> firstSeqs;

	public long[] get(int shardId) {
		AtomicLongArrayWrapper longArray = firstSeqs.get(new Integer(shardId));

		long[] fs = new long[longArray.length()];
		for (int index = 0; index < longArray.length(); index++) {
			fs[index] = longArray.get(index);
		}

		return fs;
	}

	public void set(ConcurrentHashMap<Integer, AtomicLongArrayWrapper> firstSeqs) {
		this.firstSeqs = firstSeqs;
		nodifyAll();
	}

}
