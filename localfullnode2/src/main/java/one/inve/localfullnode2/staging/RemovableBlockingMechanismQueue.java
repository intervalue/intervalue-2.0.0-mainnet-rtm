package one.inve.localfullnode2.staging;

import java.util.AbstractQueue;
import java.util.Collection;
import java.util.Iterator;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 
 * 
 * Copyright © CHXX Co.,Ltd. All rights reserved.
 * 
 * @Description: blocking mechanism is useless under some circumstances.
 * @author: Francis.Deng
 * @date: May 10, 2019 11:22:53 PM
 * @version: V1.0
 */
public class RemovableBlockingMechanismQueue<E> extends AbstractQueue<E> implements BlockingQueue<E> {

	public static interface ElementModifiable<F> {
		void notify(F e);
	}

	/**
	 * Linked list node class
	 */
	static class Node<E> {
		E item;

		/**
		 * One of: - the real successor Node - this Node, meaning the successor is
		 * head.next - null, meaning there is no successor (this is the last node)
		 */
		Node<E> next;

		Node(E x) {
			item = x;
		}
	}

	/** The capacity bound, or Integer.MAX_VALUE if none */
	private final int capacity;

	/** Current number of elements */
	private final AtomicInteger count = new AtomicInteger();

	/**
	 * Head of linked list. Invariant: head.item == null
	 */
	transient Node<E> head;

	/**
	 * Tail of linked list. Invariant: last.next == null
	 */
	private transient Node<E> last;

	/**
	 * notify external module as long as changing element.
	 */
	private ElementModifiable<E> modifier;

	public RemovableBlockingMechanismQueue(int capacity, ElementModifiable<E> modifier) {
		if (capacity <= 0)
			throw new IllegalArgumentException();
		this.capacity = capacity;
		this.modifier = modifier;
	}

	public RemovableBlockingMechanismQueue(int capacity) {
		this(capacity, null);
	}

	/**
	 * Links node at end of queue.
	 *
	 * @param node the node
	 */
	private void enqueue(Node<E> node) {
		// assert putLock.isHeldByCurrentThread();
		// assert last.next == null;
		last = last.next = node;
	}

	/**
	 * Removes a node from head of queue.
	 *
	 * @return the node
	 */
	private E dequeue() {
		// assert takeLock.isHeldByCurrentThread();
		// assert head.item == null;
		Node<E> h = head;
		Node<E> first = h.next;
		h.next = h; // help GC
		head = first;
		E x = first.item;
		first.item = null;
		return x;
	}

	@Override
	public boolean offer(E e) {
		if (e == null)
			throw new NullPointerException();
		final AtomicInteger count = this.count;
		if (count.get() == capacity)
			return false;
		int c = -1;
		Node<E> node = new Node<E>(e);

		if (count.get() < capacity) {
			enqueue(node);
			c = count.getAndIncrement();

		}

		if (c >= 0)
			notify(e);

		return c >= 0;
	}

	@Override
	public boolean add(E e) {
		// TODO Auto-generated method stub
		boolean b = super.add(e);
		if (b)
			notify(e);

		return b;
	}

	@Override
	public void put(E e) throws InterruptedException {
		if (!offer(e)) {
			throw new RuntimeException("not enough capacity");
		}
	}

	@Override
	public boolean offer(E e, long timeout, TimeUnit unit) throws InterruptedException {
		return offer(e);
	}

	@Override
	public E take() throws InterruptedException {
		return poll();
	}

	@Override
	public E poll(long timeout, TimeUnit unit) throws InterruptedException {
		return poll();
	}

	@Override
	public int remainingCapacity() {
		return capacity - count.get();
	}

	@Override
	public boolean remove(Object o) {
		if (o == null)
			return false;

		for (Node<E> trail = head, p = trail.next; p != null; trail = p, p = p.next) {
			if (o.equals(p.item)) {
				unlink(p, trail);

				notify(null);
				return true;
			}
		}
		return false;

	}

	/**
	 * Unlinks interior Node p with predecessor trail.
	 */
	void unlink(Node<E> p, Node<E> trail) {
		// assert isFullyLocked();
		// p.next is not changed, to allow iterators that are
		// traversing p to maintain their weak-consistency guarantee.
		p.item = null;
		trail.next = p.next;
		if (last == p)
			last = trail;
	}

	@Override
	public boolean contains(Object o) {
		if (o == null)
			return false;

		for (Node<E> p = head.next; p != null; p = p.next)
			if (o.equals(p.item))
				return true;
		return false;

	}

	@Override
	public int drainTo(Collection<? super E> c) {
		return drainTo(c, Integer.MAX_VALUE);
	}

	@Override
	public int drainTo(Collection<? super E> c, int maxElements) {
		if (c == null)
			throw new NullPointerException();
		if (c == this)
			throw new IllegalArgumentException();
		if (maxElements <= 0)
			return 0;
		boolean signalNotFull = false;

		int n = Math.min(maxElements, count.get());
		// count.get provides visibility to first n Nodes
		Node<E> h = head;
		int i = 0;
		try {
			while (i < n) {
				Node<E> p = h.next;
				c.add(p.item);
				p.item = null;
				h.next = h;
				h = p;
				++i;
			}
			return n;
		} finally {
			// Restore invariants even if c.add() threw
			if (i > 0) {
				// assert h.item == null;
				head = h;
				signalNotFull = (count.getAndAdd(-i) == capacity);
			}
		}

	}

	@Override
	public E poll() {
		final AtomicInteger count = this.count;
		if (count.get() == 0)
			return null;
		E x = null;
		int c = -1;

		if (count.get() > 0) {
			x = dequeue();
			c = count.getAndDecrement();
		}

		if (x != null)
			notify(x);

		return x;
	}

	@Override
	public E peek() {
		if (count.get() == 0)
			return null;

		Node<E> first = head.next;
		if (first == null)
			return null;
		else
			return first.item;

	}

	@Override
	public Iterator<E> iterator() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public int size() {
		return count.get();
	}

	protected void notify(E e) {
		if (modifier != null)
			modifier.notify(e);
	}

}
