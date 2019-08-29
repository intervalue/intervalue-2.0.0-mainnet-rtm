package one.inve.localfullnode2.store;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLongArray;

import one.inve.localfullnode2.dep.DepItemsManager;
import one.inve.localfullnode2.dep.items.LastSeqs;

/**
 * 
 * Copyright © INVE FOUNDATION. All rights reserved.
 * 
 * @ClassName: SeqsHolder
 * @Description: work as LastSeqsHolder as opposed to {@code FirstSeqsHolder}
 * @author Francis.Deng [francis_xiiiv@163.com]
 * @date Aug 27, 2019
 *
 */
public class SeqsHolder implements AtomicLongArrayWrapper.WriteNotifiable {
	private final ConcurrentHashMap<Integer, AtomicLongArrayWrapper> seq = new ConcurrentHashMap<>();

	public void put(int shardId, AtomicLongArray seqs) {
		AtomicLongArrayWrapper atomicLongArrayWrapper = AtomicLongArrayWrapper.of(seqs);
		atomicLongArrayWrapper.setNotifier(this);
		seq.put(shardId, AtomicLongArrayWrapper.of(seqs));

		notifyDeps(seq);
	}

	public AtomicLongArrayWrapper get(int shardId) {
		return seq.get(shardId);
	}

	@Override
	public void notify(AtomicLongArrayWrapper atomicLongArrayWrapper) {
		notifyDeps(seq);
	}

	protected void notifyDeps(ConcurrentHashMap<Integer, AtomicLongArrayWrapper> lastSeq) {
		LastSeqs lastSeqs = DepItemsManager.getInstance().attachLastSeqs(null);
		lastSeqs.set(lastSeq);
	}

}
