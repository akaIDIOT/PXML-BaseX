package nl.utwente.cs.pxml;

public enum ProbabilityNodeType {

	// "each child is chosen with probability 1"
	DETERMINISTIC("det"),
	// "the choices of distinct children are independent"
	INDEPENDENT("ind"),
	// "at most one child node can be chosen"
	MUTEX("mux"),
	// "the probability of choosing each subset of children is explicitly given unless it is zero"
	EXPLICIT("exp"),
	// "each child is chosen according to a conjunction of probabilistically independent events, which can be used globally throughout the p-document"
	EVENTS("cie");

	// the node name associated with the probability node type
	public final String nodeName;

	private ProbabilityNodeType(String nodeName) {
		this.nodeName = nodeName;
	}

}
