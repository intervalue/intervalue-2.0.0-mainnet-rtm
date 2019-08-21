package one.inve.localfullnode2.utilities;

/**
 * 
 * Copyright © INVE FOUNDATION. All rights reserved.
 * 
 * @ClassName: GenericArray
 * @Description: the object represents basic type array because of generic type
 *               array initialization
 * @author Francis.Deng
 * @date Aug 20, 2019
 * 
 * @param <E>
 */
public class GenericArray<E> {
	private Object[] elements;

	public void append(E... es) {
		elements = ArraysUtil.append(elements, es);
	}

	public int length() {
		return elements.length;
	}

	public E get(int pos) {
		return (E) elements[pos];
	}

	public E last() {
		return get(this.length() - 1);
	}
}
