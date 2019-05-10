package one.inve.localfullnode2.dep;

import java.lang.reflect.Field;

import one.inve.localfullnode2.utilities.ReflectionUtil;

/**
 * 
 * 
 * Copyright © CHXX Co.,Ltd. All rights reserved.
 * 
 * @Description: {@code DependentItem} and {@code DependentItemConcerned} models
 *               provide a convenient interaction among Dep object.
 * @author: Francis.Deng
 * @date: May 8, 2019 1:16:07 AM
 * @version: V1.0
 */
public interface DependentItemConcerned {
	void update(DependentItem item);

	/**
	 * inject item into subclass of {@link DependentItemConcerned} if the type is
	 * matched.
	 * 
	 * @param itemWatcher
	 * @param item
	 */
	default void set(DependentItemConcerned itemWatcher, DependentItem item) {
		Field f = ReflectionUtil.findField(itemWatcher.getClass(), item.getClass());

		if (f != null) {
			try {
				ReflectionUtil.setField(f, itemWatcher, item);
			} catch (Exception e) {
				e.printStackTrace();
			}

		}

	}
}
