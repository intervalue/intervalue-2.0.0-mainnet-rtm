package one.inve.localfullnode2.utilities;

import java.util.Iterator;

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
public class GenericArray<E> implements Iterable<E> {
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
		return (E) get(this.length() - 1);
	}

	// the ease of using for.each statement
	@Override
	public Iterator<E> iterator() {
		return new Iterator<E>() {
			private int position = 0;
			private final int limit = length();

			@Override
			public boolean hasNext() {
				return position < limit;
			}

			@Override
			public E next() {
				return (E) get(position++);
			}

		};
	}
}
