package eppic.commons.util;

import static org.junit.Assert.*;

import java.util.Arrays;
import java.util.TreeSet;

import org.junit.Test;

public class TestIntervalSet {

	@Test
	public void testIntervalStrings() {
		IntervalSet set,merged;
		String str;
		TreeSet<Integer> expected;

		str = "1-4 , 7, 9-9 ";
		expected = new TreeSet<Integer>(Arrays.asList(1,2,3,4,7,9));
		assertTrue(str,IntervalSet.isValidSelectionString(str));
		set = new IntervalSet(str);
		assertEquals(str, 3,set.size());
		assertEquals(str,expected,set.getIntegerSet());
		assertEquals(str,"1-4,7,9",set.toSelectionString());


		str = "1-4 , 2-5 ";
		expected = new TreeSet<Integer>(Arrays.asList(1,2,3,4,5));
		assertTrue(str,IntervalSet.isValidSelectionString(str));
		set = new IntervalSet(str);
		assertEquals(str, 2,set.size());
		assertEquals(str,expected,set.getIntegerSet());
		assertEquals(str,"1-4,2-5",set.toSelectionString());
		merged = set.getMergedIntervalSet();
		assertEquals(str, 1,merged.size());
		assertEquals(str, new Interval(1,5),merged.first());
		assertEquals(str,"1-5",merged.toSelectionString());
		
		str = "1-4 , 5-6 ";
		expected = new TreeSet<Integer>(Arrays.asList(1,2,3,4,5,6));
		assertTrue(str,IntervalSet.isValidSelectionString(str));
		set = new IntervalSet(str);
		assertEquals(str, 2,set.size());
		assertEquals(str,expected,set.getIntegerSet());
		assertEquals(str,"1-4,5-6",set.toSelectionString());
		merged = set.getMergedIntervalSet();
		assertEquals(str, 1,merged.size());
		assertEquals(str, new Interval(1,6),merged.first());
		assertEquals(str,"1-6",merged.toSelectionString());
	}
	

	@Test
	public void testParseInterfaceList() {
		IntervalSet result;

		result = new IntervalSet("*");
		assertEquals(1,result.size());
		assertTrue(result.first().isInfinite());

		result = new IntervalSet("");
		assertEquals(0,result.size());
		
		result = new IntervalSet("1");
		assertEquals(1,result.size());
		assertEquals(new Interval(1),result.first());
		
		result = new IntervalSet("2,3");
		assertEquals(2,result.size());
		assertEquals(new Interval(2),result.first());
		assertEquals(new Interval(3),result.last());

		result = new IntervalSet("3-2,3");
		assertEquals(2,result.size());
		assertEquals(new Interval(2,3),result.first());
		assertEquals(new Interval(3),result.last());

		result = new IntervalSet("4-6");
		assertEquals(1,result.size());
		assertEquals(new Interval(4,6),result.first());

		result = new IntervalSet(" 2,\n  3\t");
		assertEquals(2,result.size());
		assertEquals(new Interval(2),result.first());
		assertEquals(new Interval(3),result.last());
	}
	
	@Test
	public void testOverlap() {
		
		IntervalSet a,b;
		
		a = new IntervalSet("1-4");
		b = new IntervalSet("5-6");
		assertFalse(a.overlaps(b));
		assertFalse(b.overlaps(a));
		
		a = new IntervalSet("1-4");
		b = new IntervalSet("4");
		assertTrue(a.overlaps(b));
		assertTrue(b.overlaps(a));
		
		a = new IntervalSet("1-4");
		b = new IntervalSet("2-3");
		assertTrue(a.overlaps(b));
		assertTrue(b.overlaps(a));
		
		a = new IntervalSet("1-4");
		b = new IntervalSet("");
		assertFalse(a.overlaps(b));
		assertFalse(b.overlaps(a));
	}
}
