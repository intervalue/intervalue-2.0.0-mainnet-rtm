package one.inve.localfullnode2.firstseq;

import java.util.concurrent.ConcurrentHashMap;

import one.inve.localfullnode2.dep.DepItemsManager;
import one.inve.localfullnode2.dep.items.FirstSeqs;
import one.inve.localfullnode2.store.AtomicLongArrayWrapper;
import one.inve.localfullnode2.store.SeqsHolder;

/**
 * 
 * Copyright © INVE FOUNDATION. All rights reserved.
 * 
 * @ClassName: FirstSeqsHolder
 * @Description: override parent's {@code notifyDeps}
 * @author Francis.Deng [francis_xiiiv@163.com]
 * @date Aug 28, 2019
 *
 */
public class FirstSeqsHolder extends SeqsHolder {

	@Override
	protected void notifyDeps(ConcurrentHashMap<Integer, AtomicLongArrayWrapper> firstSeq) {
		FirstSeqs firstSeqs = DepItemsManager.getInstance().attachFirstSeqs(null);
		firstSeqs.set(firstSeq);
	}

}
