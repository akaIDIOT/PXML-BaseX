package nl.utwente.cs.pxml;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import nl.utwente.cs.pxml.util.CollectionUtils;

public abstract class PXML {

	public static String combine(String... conditions) {
		// TODO: would the uniqueness of the strings in the argument not be enough?
		
		// create a condition 'container'
		Set<Condition> result = new HashSet<Condition>(conditions.length);
		for (String condition : conditions) {
			// rely on Set to enforce uniqueness
			result.add(new Condition(condition));
		}
		
		// join the resulting set on a space
		return CollectionUtils.join(result, " ");
	}
	
	public static boolean consistent(String descriptor) {
		ConditionGenerator generator = new ConditionGenerator(descriptor);
		Map<String, Integer> conditions = new HashMap<String, Integer>();
		
		for (Condition condition : generator) {
			// rely on Map to enforce uniqueness of name, use Integer as it can be null
			Integer value = conditions.put(condition.name, condition.value);
			if (value != null && value != condition.value) {
				// immediately return false if the name was already encountered with a different value 
				return false;
			}
		}
		
		// no inconsistencies found, return true 
		return true;
	}
	
	public static boolean mutuallyExclusive(String a, String b) {
		ConditionGenerator condA = new ConditionGenerator(a);
		ConditionGenerator condB = new ConditionGenerator(b);
		
		return false;
	}
	
	public static double probability(String... conditions) {
		return 0.0;
	}
	
	public static void main(String... args) {
		for (String arg : args) {
			System.out.println(consistent(arg));
		}
	}
	
}
