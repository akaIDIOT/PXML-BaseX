package nl.utwente.cs.pxml;

import java.util.Collection;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class TestPXML {

	protected PXML subject;

	@Before
	public void setUp() throws Exception {
		this.subject = new PXML();
	}

	@Test
	public void testCombine() {
		// hard code the descriptor length for testing purposes
		int descriptorLength = 6;
		// simple set of descriptors
		String[] set1 = { "arg1=1", "arg2=2", "arg1=2", "arg2=1" };
		// set of descriptors with a duplicates
		String[] set2 = { "arg1=1", "arg2=2", "arg2=1", "arg1=2", "arg2=1", "arg1=1" };

		// simple test for determinism
		Assert.assertEquals(subject.combine(set1), subject.combine(set1));

		// test to see if all descriptors are still present (evil string length
		// hack :()
		Assert.assertEquals(subject.combine(set1).length(), set1.length * (descriptorLength + 1) - 1);

		// test to see if combine removes duplicates
		Assert.assertEquals(subject.combine(set1).length(), subject.combine(set2).length());

		// test for existence of all values in both computed combinations
		String one = subject.combine(set1);
		String two = subject.combine(set2);
		for (String descriptor : set1) {
			TestPXML.assertContains(one, descriptor);
			TestPXML.assertContains(two, descriptor);
		}
		for (String descriptor : set2) {
			TestPXML.assertContains(one, descriptor);
			TestPXML.assertContains(two, descriptor);
		}
	}

	@Test
	public void testConsistent() {
		Assert.fail("Not yet implemented");
	}

	@Test
	public void testMutuallyExclusive() {
		Assert.fail("Not yet implemented");
	}

	/**
	 * Asserts element being a member of collection. Calls
	 * {@link Assert#fail(String)} otherwise.
	 * 
	 * @param collection
	 *            The collection element should be a member of.
	 * @param element
	 *            The element to check.
	 */
	public static void assertContains(Collection<?> collection, Object element) {
		if (!collection.contains(element)) {
			Assert.fail("" + collection + " does not contain " + element);
		}
	}

	/**
	 * Asserts sub being a substring of string. Calls
	 * {@link Assert#fail(String)} otherwise.
	 * 
	 * @param string
	 *            The string sub should be a substring of.
	 * @param sub
	 *            The substring to check.
	 */
	public static void assertContains(String string, String sub) {
		if (!string.contains(sub)) {
			Assert.fail(string + " does not contain " + sub);
		}
	}

}
