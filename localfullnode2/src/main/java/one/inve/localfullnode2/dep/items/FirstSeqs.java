package one.inve.localfullnode2.dep.items;

import java.util.concurrent.ConcurrentHashMap;

import one.inve.localfullnode2.dep.DependentItem;
import one.inve.localfullnode2.store.AtomicLongArrayWrapper;

public class FirstSeqs extends DependentItem {
	private ConcurrentHashMap<Integer, AtomicLongArrayWrapper> firstSeqs;

	public ConcurrentHashMap<Integer, AtomicLongArrayWrapper> get() {
		return firstSeqs;
	}

	public void set(ConcurrentHashMap<Integer, AtomicLongArrayWrapper> firstSeqs) {
		this.firstSeqs = firstSeqs;
		nodifyAll();
	}

}
