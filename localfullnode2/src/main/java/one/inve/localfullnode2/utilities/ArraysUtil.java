package one.inve.localfullnode2.utilities;

/**
 * 
 * Copyright © INVE FOUNDATION. All rights reserved.
 * 
 * @ClassName: ArraysUtil
 * @Description: TODO
 * @author Francis.Deng
 * @date Aug 20, 2019
 *
 */
public class ArraysUtil {

	/**
	 * a array helper works likes golang append function
	 * 
	 * @param       <T>
	 * @param array
	 * @param ts
	 * @return
	 */
	public static Object[] append(Object[] array, Object... ts) {
		int fromIndex = 0;
		Object[] newArray;
		if (array == null) {
			array = new Object[ts.length];

			for (int i = 0; i < array.length; i++) {
				array[i] = ts[i];
			}

			return array;
		}

		newArray = new Object[ts.length + array.length];
		System.arraycopy(array, 0, newArray, 0, array.length);
		System.arraycopy(ts, 0, newArray, array.length, ts.length);

		return newArray;
	}

}
