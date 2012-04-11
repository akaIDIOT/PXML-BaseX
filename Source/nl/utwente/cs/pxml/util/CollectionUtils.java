package nl.utwente.cs.pxml.util;

import java.util.Iterator;

/**
 * Utility class capable of doing some cumbersome things with collections. 
 * 
 * @author Mattijs Ugen
 */
public abstract class CollectionUtils {

	/**
	 * Joins string-representations of items in iterable on the separator.
	 * 
	 * @param iterable
	 *            Items to join.
	 * @param separator
	 *            The separator put in between.
	 * @return A string containing all the items in iterable separated by
	 *         separator.
	 */
	public static String join(Iterable<?> iterable, String separator) {
		// TODO: figure out a way to initialize this with a capacity that makes
		// sense
		StringBuilder result = new StringBuilder();

		// use an iterator to loop of the iterable
		for (Iterator<?> iterator = iterable.iterator(); iterator.hasNext();) {
			result.append(String.valueOf(iterator.next()));
			// only append a space if there's another item after this one
			if (iterator.hasNext()) {
				result.append(separator);
			}
		}

		// return the join result
		return result.toString();
	}

}
