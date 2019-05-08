package one.inve.localfullnode2.dep;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * 
 * 
 * Copyright © CHXX Co.,Ltd. All rights reserved.
 * 
 * @Description: {@code DependentItem} and {@code DependentItemConcerned} models
 *               provide a convenient interaction among Dep object.
 * @author: Francis.Deng
 * @date: May 8, 2019 1:13:42 AM
 * @version: V1.0
 */
public abstract class DependentItem {
	private List<DependentItemConcerned> list = new ArrayList<DependentItemConcerned>();

	public DependentItem attach(DependentItemConcerned... concerneds) {
		list.addAll(Arrays.asList(concerneds));
		return this;
	}

	protected void nodifyAll() {
		for (DependentItemConcerned concerned : list) {
			concerned.update(this);
		}
	}
}
