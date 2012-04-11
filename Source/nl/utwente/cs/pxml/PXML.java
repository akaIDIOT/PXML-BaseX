package nl.utwente.cs.pxml;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.basex.query.QueryContext;
import org.basex.query.QueryModule;
import org.basex.query.item.Str;
import org.basex.util.InputInfo;

import nl.utwente.cs.pxml.util.CollectionUtils;

public class PXML extends QueryModule {

	protected Map<String, Double> probabilityCache;

	/**
	 * Acts as a constructor, presumably called for every query to be evaluated
	 * by the class / an instance of this class.
	 * 
	 * @see {@link QueryModule}
	 */
	@Override
	public void init(QueryContext ctx, InputInfo ii) {
		// do whatever super does in this case
		super.init(ctx, ii);

		// purge the probability cache by simply creating a new object (also
		// resetting the size of the cache)
		this.probabilityCache = new HashMap<String, Double>();
	}

	/**
	 * Combines all conditions into a single string keeping only the unique
	 * conditions. The result need not be consistent.
	 * 
	 * @param conditions
	 *            The conditions to be combined.
	 * @return A single string containing all conditions in the parameters.
	 */
	public String combine(String... conditions) {
		// create a condition 'container'
		Set<String> result = new HashSet<String>(conditions.length);
		for (String condition : conditions) {
			// rely on Set to enforce uniqueness
			result.add(condition);
		}

		// join the resulting set on the separator
		return CollectionUtils.join(result, " ");
	}

	/**
	 * Checks whether the provided descriptor is consistent. Consistency means
	 * no variables occurring twice with different values.
	 * 
	 * @param descriptor
	 *            The condition descriptor string to be tested.
	 * @return Whether the conditions in the provided condition string are
	 *         consistent.
	 */
	public boolean consistent(String descriptor) {
		ConditionGenerator generator = new ConditionGenerator(descriptor);
		Map<String, Integer> conditions = new HashMap<String, Integer>();

		for (Condition condition : generator) {
			// rely on Map to enforce uniqueness of name, use Integer as it can
			// be null
			Integer value = conditions.put(condition.name, condition.value);
			if (value != null && value != condition.value) {
				// immediately return false if the name was already encountered
				// with a different value
				return false;
			}
		}

		// no inconsistencies found, return true
		return true;
	}

	/**
	 * Tests whether condition descriptor a is mutually exclusive with condition
	 * descriptor b by checking for variables in a having different values in b.
	 * 
	 * @param a
	 *            The one condition descriptor.
	 * @param b
	 *            The other condition descriptor.
	 * @return Whether the two condition descriptors are mutually exclusive.
	 */
	public boolean mutuallyExclusive(String a, String b) {
		ConditionGenerator condA = new ConditionGenerator(a);
		ConditionGenerator condB = new ConditionGenerator(b);
		// TODO: is using a map a problem? (possibly breaks comparison
		// inconsistent strings)
		Map<String, Integer> conditions = new HashMap<String, Integer>();

		// put all conditions a into the map ...
		for (Condition condition : condA) {
			conditions.put(condition.name, condition.value);
		}

		// ... check if there is a condition in b that has the same name but a
		// different value
		for (Condition condition : condB) {
			Integer value = conditions.get(condition.name);
			if (value != null && value != condition.value) {
				// immediately return false if this such a condition is found
				return true;
			}
		}

		return false;
	}

	/**
	 * Calculates the probability of all conditions by simply multiplying them
	 * together. Does *not* check for consistency (which would make the
	 * probability 0).
	 * 
	 * @param docName
	 *            The document name of the place where the probabilities are
	 *            stored.
	 * @param conditions
	 *            The conditions to look up.
	 * @return The probability of all conditions being true.
	 */
	public double probability(String docName, String... conditions) {
		// TODO: cache probabilities with (docName, condition) as keys? (static
		// mapping in PXML?)

		// find the wsd-list in the document

		double probability = 1.0;
		// find probabilities for all conditions, multiply them
		for (String condition : conditions) {

		}

		// return the result
		return probability;
	}

}
