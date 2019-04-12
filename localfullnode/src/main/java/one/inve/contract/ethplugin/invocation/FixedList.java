package one.inve.contract.ethplugin.invocation;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * 
 * 
 * Copyright © CHXX Co.,Ltd. All rights reserved.
 * 
 * @Description: with a maximum number of elements, beyond which, if you add
 *               other elements, the class behaves like a queue with a FIFO
 *               logic (first-in first-out)
 * @author: Francis.Deng
 * @date: 2018年11月22日 下午2:14:17
 * @version: V1.0
 */
public class FixedList<T> {

	private final int size;
	private List<T> list;
	private Lock lock;

	public FixedList(int size) {
		list = new ArrayList<T>();
		this.size = size;
		lock = new ReentrantLock();
	}

	public T[] all(T[] a) {
		return (T[]) Arrays.copyOf(list.toArray(a), size, a.getClass());
	}

	public T get(int index) {
        lock.lock();
        try {
            return list.get(index);
        } finally {
            lock.unlock();
        }
    }

	public void add(T element) {
        lock.lock();
        try {
            while (list.size() >= size) {
                list.remove(list.size() - 1);
            }
            list.add(0, element);
        } finally {
			lock.unlock();
		}
	}

}