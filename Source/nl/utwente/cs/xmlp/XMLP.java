package nl.utwente.cs.xmlp;

import java.util.HashSet;
import java.util.Set;

import nl.utwente.cs.xmlp.util.CollectionUtils;

public abstract class XMLP {

	public static String combine(String... conditions) {
		// create a condition 'container'
		Set<Condition> result = new HashSet<Condition>(conditions.length);
		for (String condition : conditions) {
			// rely on Set to enforce uniqueness
			result.add(new Condition(condition));
		}
		
		// join the resulting set on a space
		return CollectionUtils.join(result, " ");
	}
	
	public static boolean consistent(String conditions) {
		ConditionGenerator generator = new ConditionGenerator(conditions);
		
		return false;
	}
	
	public static boolean mutuallyExclusive(String a, String b) {
		ConditionGenerator condA = new ConditionGenerator(a);
		ConditionGenerator condB = new ConditionGenerator(b);
		
		return false;
	}
	
	public static double probability(String... conditions) {
		return 0.0;
	}
	
}
