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

	/**
	 * Tests {@link PXML#combine(String...)}.
	 */
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

	/**
	 * Tests {@link PXML#consistent(String)}.
	 */
	@Test
	public void testConsistent() {
		// a consistent set of descriptors without duplicates
		String set1 = "arg1=1 arg2=2 arg3=0 arg4=1";
		// a consistent set of descriptors with duplicates
		String set2 = "arg1=1 arg2=2 arg2=2 arg1=1 arg3=0 arg4=1";
		// an inconsistent set of descriptors without duplicates
		String set3 = "arg1=1 arg2=2 arg3=0 arg4=1 arg2=1";
		// an inconsistent set of descriptors with duplicates
		String set4 = "arg1=1 arg2=2 arg2=2 arg1=1 arg1=2 arg3=0 arg4=1 arg2=4";

		// expect set1 to be consistent
		Assert.assertTrue(subject.consistent(set1));
		// expect set2 to be consistent
		Assert.assertTrue(subject.consistent(set2));
		// expect set3 to be inconsistent
		Assert.assertFalse(subject.consistent(set3));
		// expect set4 to be inconsistent
		Assert.assertFalse(subject.consistent(set4));
	}

	/**
	 * Tests {@link PXML#mutuallyExclusive(String, String)} (does *not* test
	 * consistency in this context).
	 */
	@Test
	public void testMutuallyExclusive() {
		// consistent twin sets that can both be true (without overlap)
		String set11 = "arg1=1 arg5=2 arg6=0 arg4=1";
		String set12 = "arg2=1 arg3=2 arg7=0 arg8=1";
		// consistent twin sets that can both be true (with overlap)
		String set21 = "arg1=1 arg5=2 arg6=0 arg4=1";
		String set22 = "arg1=1 arg2=2 arg6=0 arg3=1";
		// twin sets that are mutually exclusive
		String set31 = "arg1=1 arg5=2 arg6=0 arg4=1";
		String set32 = "arg1=1 arg5=2 arg3=0 arg4=2";

		// expect set1 to not be mutually exclusive (both ways)
		Assert.assertFalse(subject.mutuallyExclusive(set11, set12));
		Assert.assertFalse(subject.mutuallyExclusive(set12, set11));
		// expect set2 to not be mutually exclusive (both ways)
		Assert.assertFalse(subject.mutuallyExclusive(set21, set22));
		Assert.assertFalse(subject.mutuallyExclusive(set22, set21));
		// expect set3 to be mutually exclusive (both ways)
		Assert.assertTrue(subject.mutuallyExclusive(set31, set32));
		Assert.assertTrue(subject.mutuallyExclusive(set32, set31));
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
