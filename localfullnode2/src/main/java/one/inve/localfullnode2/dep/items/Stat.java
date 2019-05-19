package one.inve.localfullnode2.dep.items;

import java.math.BigInteger;

import one.inve.localfullnode2.dep.DependentItem;

/**
 * 
 * Copyright © CHXX Co.,Ltd. All rights reserved.
 * 
 * @Description: The statistics is comprised of TotalEventCount
 * @author: Francis.Deng
 * @date: May 19, 2019 12:13:47 AM
 * @version: V1.0
 */
public class Stat extends DependentItem {
	private BigInteger totalEventCount = BigInteger.valueOf(0);

	public BigInteger getTotalEventCount() {
		return totalEventCount;
	}

	/*
	 * first call from {@code DbUtils::initStatistics}
	 */
	public void addTotalEventCount(long delta) {
		totalEventCount.add(BigInteger.valueOf(delta));
		nodifyAll();
	}

}
