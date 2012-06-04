package nl.utwente.cs.pxml.util;

import java.util.Iterator;
import java.util.NoSuchElementException;

/**
 * Provides an Iterator implementation fed from an Iterator and additional elements.
 * 
 * @author Mattijs Ugen
 * 
 * @param <E>
 *            The type of elements in the Iterator.
 */
public class CompoundIterator<E> implements Iterator<E> {

	protected Iterator<E> iterator;
	protected E[] elements;
	protected int current = 0;

	public CompoundIterator(Iterator<E> iterator, E... extraElements) {
		this.iterator = iterator;
		this.elements = extraElements;
	}

	@Override
	public boolean hasNext() {
		return iterator.hasNext() || this.current < this.elements.length;
	}

	@Override
	public E next() {
		if (iterator.hasNext()) {
			// use the original iterator while it has elements ...
			return iterator.next();
		} else if (this.current < this.elements.length) {
			// ... otherwise use elements from the array
			return this.elements[this.current++];
		} else {
			throw new NoSuchElementException();
		}
	}

	/**
	 * Operation not supported for ArrayIterator.
	 * 
	 * @throws UnsupportedOperationException
	 *             always.
	 */
	@Override
	public void remove() {
		throw new UnsupportedOperationException("operation remove() not supported for ArrayIterator");
	}

}
