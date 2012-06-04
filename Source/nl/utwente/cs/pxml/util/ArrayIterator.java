package nl.utwente.cs.pxml.util;

import java.util.Iterator;
import java.util.NoSuchElementException;

/**
 * Provides an Iterator implementation fed from an array.
 * 
 * @author Mattijs Ugen
 * 
 * @param <E>
 *            The type of elements in the array.
 */
public class ArrayIterator<E> implements Iterator<E> {

	protected E[] elements;
	protected int current = 0;

	/**
	 * Creates a new ArrayIterator using elements as the source of the elements.
	 * 
	 * @param elements
	 *            The array to use as source.
	 */
	public ArrayIterator(E[] elements) {
		this.elements = elements;
	}

	@Override
	public boolean hasNext() {
		return this.current < this.elements.length;
	}

	@Override
	public E next() {
		if (this.hasNext()) {
			// return the current element and *post*-increment current for the next call
			return this.elements[this.current++];
		} else {
			throw new NoSuchElementException("no such element (index " + this.current + " >= size "
					+ this.elements.length + ")");
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
