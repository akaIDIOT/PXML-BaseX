package nl.utwente.cs.xmlp;

import java.util.Map;

public class Condition {

	public final String name;
	public final int value;

	/**
	 * Creates a new Condition stating the value of name being equal to value.
	 * 
	 * @param name
	 *            The name of the variable for this condition.
	 * @param value
	 *            The value of the variable for this condition.
	 */
	public Condition(String name, int value) {
		this.name = name;
		this.value = value;
	}

	/**
	 * Tests whether a condition is consistent with this condition. A condition
	 * is considered consistent with another if it is possible for both to be
	 * true.
	 * 
	 * @param other
	 *            The condition to test consistency with.
	 * @return Whether this condition is consistent with the other.
	 */
	public boolean isConsistentWith(Condition other) {
		return !this.equals(other) || this.value != other.value;
	}

	@Override
	public boolean equals(Object other) {
		// immediately return false if the other object is not a condition
		if (!(other instanceof Condition)) {
			return false;
		}

		// cast to Condition and return equality based on properties
		Condition condition = (Condition) other;
		return this.name.equals(condition.name) && this.value == condition.value;
	}

	@Override
	public String toString() {
		return this.name + "=" + this.value;
	}

}
