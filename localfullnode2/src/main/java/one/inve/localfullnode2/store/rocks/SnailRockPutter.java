package one.inve.localfullnode2.store.rocks;

import java.time.Instant;
import java.util.HashSet;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 
 * 
 * Copyright © INVE FOUNDATION. All rights reserved.
 * 
 * @Description: An phenomenon was found that a half of invoking methods access
 *               to rocksdb was writing-type,which is possible to cost a lot.In
 *               this solution,we push writing-operation back after a interval
 *               or reaching a valve value(container size in this case).
 * 
 *               What if the system loss data because of power outage or
 *               something else.In my philosophy,I would recommend that you must
 *               have a repair mechanism in place if bad thing happens in the
 *               future.
 * @author: Francis.Deng
 * @date: 2019年4月16日 下午5:52:17
 * @version: V1.0
 */
public abstract class SnailRockPutter extends RocksJavaUtil {
	private static final Logger logger = LoggerFactory.getLogger(SnailRockPutter.class);

	private final long _intervalInMilli;
	private final long _capacity;

	private Instant timeOfStartingRound;

	private Set<RockPutEntry> noDuplicateElementsContainer = new HashSet<>();

	public SnailRockPutter(String dbId, long capacity, long intervalInMilli) {
		super(dbId);

		this._capacity = capacity;
		this._intervalInMilli = intervalInMilli;
	}

	public void put(final byte[] k, final byte[] v) {
		RockPutEntry putEntry = new RockPutEntry(k, v);

		if (noDuplicateElementsContainer.isEmpty())
			timeOfStartingRound = Instant.now();

		// replace outdated RockPutEntry
		if (!noDuplicateElementsContainer.add(putEntry)) {
			noDuplicateElementsContainer.remove(putEntry);
			noDuplicateElementsContainer.add(putEntry);
		}

		// combine time,capacity together to do calculation
		if ((normalize(_capacity, 0, noDuplicateElementsContainer.size())
				+ normalize((timeOfStartingRound.toEpochMilli() + _intervalInMilli), timeOfStartingRound.toEpochMilli(),
						Instant.now().toEpochMilli())) > 1) {

			noDuplicateElementsContainer.parallelStream().forEach((e) -> {
				try {
					rocksDB.put(e.key(), e.value());

				} catch (Exception ex) {
					logger.error("rocksDB.put error[in SnailRockPutter]", ex);
				}
			});

			noDuplicateElementsContainer.clear();
		}
	}

	private double normalize(long max, long min, long cur) {
		return ((double) cur - min) / (max - min);
	}

}
