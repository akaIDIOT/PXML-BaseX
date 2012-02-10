package nl.utwente.cs.pxml;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import nl.utwente.cs.pxml.util.CollectionUtils;

public abstract class PXML {

	public static String combine(String... conditions) {
		// TODO: would the uniqueness of the strings in the argument not be
		// enough?

		// create a condition 'container'
		Set<Condition> result = new HashSet<Condition>(conditions.length);
		for (String condition : conditions) {
			// rely on Set to enforce uniqueness
			result.add(new Condition(condition));
		}

		// join the resulting set on the separator 
		return CollectionUtils.join(result, " ");
	}

	public static boolean consistent(String descriptor) {
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

	public static boolean mutuallyExclusive(String a, String b) {
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

	public static double probability(String docName, String... conditions) {
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
