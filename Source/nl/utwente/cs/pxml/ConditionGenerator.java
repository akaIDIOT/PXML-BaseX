package nl.utwente.cs.pxml;

import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Scanner;
import java.util.regex.Pattern;

/**
 * Class that generates {@link Condition} instances from a string. Meant to be
 * used as
 * 
 * <pre>
 * for (Condition condition : new ConditionGenerator(conditions)) {
 * 	// do something with condition
 * }
 * </pre>
 * 
 * @author Mattijs Ugen
 */
public class ConditionGenerator implements Iterable<Condition>, Iterator<Condition> {

	// pattern separating both conditions and names and values within a
	// condition
	public static final Pattern PATTERN = Pattern.compile("[\\s=]+");

	protected Scanner input;

	/**
	 * Creates a new ConditionGenerator generating Conditions from the provided
	 * 'stream'.
	 * 
	 * @param descriptor
	 *            A string containing conditions in "name=value"-form.
	 */
	public ConditionGenerator(String descriptor) {
		this.input = new Scanner(descriptor).useDelimiter(ConditionGenerator.PATTERN);
	}

	@Override
	public Iterator<Condition> iterator() {
		// class is its own iterator, just return this
		return this;
	}

	@Override
	public boolean hasNext() {
		return this.input.hasNext();
	}

	/**
	 * Provides the next Condition in the 'stream'.
	 * 
	 * @throws NoSuchElementException
	 *             - when the stream is malformed when the end of the stream is
	 *             reached.
	 */
	@Override
	public Condition next() {
		String name = this.input.next();
		int value = this.input.nextInt();

		return new Condition(name, value);
	}

	/**
	 * Unsupported operation.
	 * 
	 * @throws UnsupportedOperationException
	 *             - always
	 */
	@Override
	public void remove() {
		throw new UnsupportedOperationException("cannot remove condition, generated from stream");
	}

	/**
	 * Tests DescriptionParser by feeding it input from the arguments provided
	 * by the user.
	 * 
	 * @param args
	 */
	public static void main(String... args) {
		for (String arg : args) {
			try {
				System.out.println("for input '" + arg + "'");
				for (Condition cond : new ConditionGenerator(arg)) {
					System.out.print(cond + " ");
				}
				System.out.println();
			} catch (NoSuchElementException e) {
				System.err.println("input was malformed");
			}
		}
	}

}
