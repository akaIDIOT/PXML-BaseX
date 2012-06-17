package nl.utwente.cs.pxml;

/**
 * Record used to represent a variable and its value in {@link PXML#consistent(String)}.
 * 
 * @author Mattijs Ugen
 */
public class Condition implements Comparable<Condition> {

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
	 * Create a new Condition from the a string of the form "name=value".
	 * 
	 * @param condition
	 *            The condition as a string.
	 */
	public Condition(String condition) {
		// split name=value into name and value
		String[] parts = condition.split("=");
		// assume this went fine
		this.name = parts[0].trim();
		this.value = Integer.parseInt(parts[1].trim());
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

	@Override
	public int compareTo(Condition other) {
		int value = this.name.compareTo(other.name);
		return value != 0 ? value : other.value - this.value;
	}

}
