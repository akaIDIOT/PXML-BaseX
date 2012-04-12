package nl.utwente.cs.pxml;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import org.basex.query.QueryModule;
import org.basex.query.item.ANode;

import nl.utwente.cs.pxml.util.CollectionUtils;

/**
 * Importable query module containing functions for use with probablistic XML.
 * 
 * @author Mattijs Ugen
 */
public class PXML extends QueryModule {

	protected Map<ProbabilityCacheKey, Double> probabilityCache;
	
	/**
	 * Creates a new PXML instance, with a newly created probability cache. 
	 */
	public PXML() {
		this.probabilityCache = new TreeMap<ProbabilityCacheKey, Double>();
	}

	/**
	 * Combines all conditions into a single string keeping only the unique
	 * conditions. The result need not be consistent.
	 * 
	 * @param conditions
	 *            The conditions to be combined.
	 * @return A single string containing all conditions in the parameters.
	 */
	@Requires(Permission.NONE)
	@Deterministic
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
	@Requires(Permission.NONE)
	@Deterministic
	public boolean consistent(String descriptor) {
		ConditionGenerator generator = new ConditionGenerator(descriptor);
		Map<String, Integer> conditions = new HashMap<String, Integer>();

		for (Condition condition : generator) {
			// rely on Map to enforce uniqueness of name, use Integer as it can
			// be null (TODO: test this)
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
	@Requires(Permission.NONE)
	@Deterministic
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
	@Requires(Permission.NONE)
	@ContextDependent
	public double probability(String docName, String... conditions) {
		// find the wsd-list in the document (TODO)
		ANode wsdList = null;

		double probability = 1.0;
		// find probabilities for all conditions, multiply them
		for (String condition : conditions) {
			// create the key (equals() will make sure to match it)
			ProbabilityCacheKey key = new ProbabilityCacheKey(docName, condition);
			// use Double to allow null when key is not present (TODO: test this)
			Double value = this.probabilityCache.get(key);
			if (value == null) {
				// find it in the wsd-list ...
				value = this.probability(wsdList, condition);
				// ... and cache it for future use
				this.probabilityCache.put(key, value);
			}

			// do the probability multiplication
			probability *= value;
		}

		// return the result
		return probability;
	}

	/**
	 * Finds a probability for the variable encoded by condition in the wsdList.
	 * 
	 * @param wsdList
	 *            The node containing the probabilities.
	 * @param condition
	 *            The condition to match.
	 * @return The probability of the condition being true.
	 */
	protected Double probability(ANode wsdList, String condition) {
		// find node matching condition (TODO)

		// return its probability
		return 0.0;
	}

	/**
	 * Record used as a key in the probability cache of {@link PXML}.
	 * 
	 * @author Mattijs Ugen
	 */
	public static class ProbabilityCacheKey implements Comparable<ProbabilityCacheKey> {

		// the document this instance belongs to
		public final String doc;
		// the variable and its value this instance represents
		public final String var;

		/**
		 * Creates a new {@link ProbabilityCacheKey} with the provided document
		 * and variable keys.
		 * 
		 * @param doc
		 *            The document this instance belongs to.
		 * @param var
		 *            The variable and value this instance represents.
		 */
		public ProbabilityCacheKey(String doc, String var) {
			this.doc = doc;
			this.var = var;
		}

		/**
		 * Provides a means of sorting {@link ProbabilityCacheKey}
		 * alphabetically.
		 * 
		 * @see {@link Comparable#compareTo(Object)}
		 */
		@Override
		public int compareTo(ProbabilityCacheKey other) {
			// make doc more significant for sorting purposes
			int distance = this.doc.compareTo(other.doc);
			// only check var distance when docs are equal
			return distance != 0 ? distance : this.var.compareTo(other.var);
		}

		/**
		 * Tests if another object is equal to this one. Specifically whether
		 * they point to the same document and variable+value, in the case of a
		 * {@link ProbabilityCacheKey}.
		 * 
		 * @see {@link Object#equals(Object)}
		 */
		@Override
		public boolean equals(Object obj) {
			if (obj instanceof ProbabilityCacheKey) {
				ProbabilityCacheKey key = (ProbabilityCacheKey) obj;
				// var assumed to be unequal more often, checked first
				return this.var.equals(key.var) && this.doc.equals(key.doc);
			} else {
				return false;
			}
		}

	}

}
