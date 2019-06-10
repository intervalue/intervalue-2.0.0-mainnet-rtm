package one.inve.localfullnode2.dep.items;

import one.inve.bean.wallet.Wallet;
import one.inve.localfullnode2.dep.DependentItem;

/**
 * 
 * 
 * Copyright © CHXX Co.,Ltd. All rights reserved.
 * 
 * @Description: Stand for {@link Wallet} item.
 * @author: Francis.Deng
 * @date: Jun 9, 2019 11:43:43 PM
 * @version: V1.0
 */
public class Wal extends DependentItem {
	private Wallet wal;

	public Wallet get() {
		return wal;
	}

	public void set(Wallet wal) {
		this.wal = wal;
		nodifyAll();
	}

}
