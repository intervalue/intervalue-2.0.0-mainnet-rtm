package one.inve.localfullnode2.dep.items;

import java.math.BigInteger;

import one.inve.localfullnode2.dep.DependentItem;

/**
 * 
 * Copyright © CHXX Co.,Ltd. All rights reserved.
 * 
 * @Description: The statistics is comprised of
 *               TotalEventCount,TotalConsEventCount,ConsMessageMaxId,SystemAutoTxMaxId,ConsMessageCount
 * @author: Francis.Deng
 * @date: May 19, 2019 12:13:47 AM
 * @version: V1.0
 */
public class Stat extends DependentItem {
	private BigInteger totalEventCount = BigInteger.valueOf(0);
	private BigInteger totalConsEventCount = BigInteger.valueOf(0);
	private BigInteger consMessageMaxId = BigInteger.valueOf(0);
	private BigInteger systemAutoTxMaxId = BigInteger.valueOf(0);
	private BigInteger consMessageCount = BigInteger.valueOf(0);

	public BigInteger getTotalEventCount() {
		return totalEventCount;
	}

	/*
	 * first call from {@code DbUtils::initStatistics}
	 */
	public void addTotalEventCount(long delta) {
		totalEventCount = totalEventCount.add(BigInteger.valueOf(delta));
		nodifyAll();
	}

	public BigInteger getTotalConsEventCount() {
		return totalConsEventCount;
	}

	/*
	 * first call from {@code EventsExe::run}
	 */
	public void addTotalConsEventCount(long delta) {
		totalConsEventCount = totalConsEventCount.add(BigInteger.valueOf(delta));
		nodifyAll();
	}

	public BigInteger getConsMessageMaxId() {
		return consMessageMaxId;
	}

	/*
	 * first call from {@code EventsExe::addConsMessage2VerifyQueue}
	 */
	public void addConsMessageMaxId(long delta) {
		consMessageMaxId = consMessageMaxId.add(BigInteger.valueOf(delta));
		nodifyAll();
	}

	public BigInteger getSystemAutoTxMaxId() {
		return systemAutoTxMaxId;
	}

	public void addSystemAutoTxMaxId(long delta) {
		systemAutoTxMaxId = systemAutoTxMaxId.add(BigInteger.valueOf(delta));
		nodifyAll();
	}

	public BigInteger getConsMessageCount() {
		return consMessageCount;
	}

	public void setConsMessageCount(BigInteger consMessageCount) {
		this.consMessageCount = consMessageCount;
		nodifyAll();
	}

	public void setSystemAutoTxMaxId(BigInteger systemAutoTxMaxId) {
		this.systemAutoTxMaxId = systemAutoTxMaxId;
		nodifyAll();
	}

	public void setTotalConsEventCount(BigInteger totalConsEventCount) {
		this.totalConsEventCount = totalConsEventCount;
		nodifyAll();
	}

	public void setConsMessageMaxId(BigInteger consMessageMaxId) {
		this.consMessageMaxId = consMessageMaxId;
		nodifyAll();
	}

	public void setTotalEventCount(BigInteger totalEventCount) {
		this.totalEventCount = totalEventCount;
		nodifyAll();
	}
}
