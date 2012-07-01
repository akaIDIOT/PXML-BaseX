package nl.utwente.cs.pxml;

import java.util.Arrays;
import java.util.Collection;

import nl.utwente.cs.pxml.util.CollectionUtils;

import org.basex.query.value.Value;
import org.basex.query.value.item.Str;
import org.basex.query.value.seq.Empty;
import org.basex.query.value.seq.ItemSeq;
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
		String[] set1 = {
				"arg1=1", "arg2=2", "arg1=2", "arg2=1"
		};
		Str set1str = Str.get(CollectionUtils.join(Arrays.asList(set1), " "));
		
		// set of descriptors with a duplicate
		String[] set2 = {
				"arg1=1", "arg2=2", "arg2=1", "arg1=2", "arg2=1", "arg1=1"
		};
		Str set2str = Str.get(CollectionUtils.join(Arrays.asList(set2), " "));

		// simple test for determinism
		Assert.assertEquals(subject.combine(set1str, Empty.SEQ), subject.combine(set1str, Empty.SEQ));

		// test to see if all descriptors are still present (evil string length hack :()
		Assert.assertEquals(subject.combine(set1str, Empty.SEQ).length(), set1.length * (descriptorLength + 1) - 1);

		// test to see if combine removes duplicates
		Assert.assertEquals(subject.combine(set1str, Empty.SEQ).length(), subject.combine(set2str, Empty.SEQ).length());
		
		// test to see if combining sets 1 and 2 will yield the length of set 2
		Str[] strs = new Str[set2.length];
		for (int i = 0; i < set2.length; i++) {
			strs[i] = Str.get(set2[i]);
		}
		Value sequence = ItemSeq.get(strs, set2.length);
		// set 2 has two duplicates, subtract 2 from its length
		Assert.assertEquals(subject.combine(set1str, sequence).length(), (set2.length - 2) * (descriptorLength + 1) - 1);

		// test for existence of all values in both computed combinations
		String one = subject.combine(set1str, Empty.SEQ);
		String two = subject.combine(set2str, Empty.SEQ);
		for (String descriptor : set1) {
			TestPXML.assertContains(one, descriptor);
		}
		for (String descriptor : set2) {
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
		Assert.assertTrue(subject.consistent(Str.get(set1)));
		// expect set2 to be consistent
		Assert.assertTrue(subject.consistent(Str.get(set2)));
		// expect set3 to be inconsistent
		Assert.assertFalse(subject.consistent(Str.get(set3)));
		// expect set4 to be inconsistent
		Assert.assertFalse(subject.consistent(Str.get(set4)));
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
	 * Asserts element being a member of collection. Calls {@link Assert#fail(String)} otherwise.
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
	 * Asserts sub being a substring of string. Calls {@link Assert#fail(String)} otherwise.
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
