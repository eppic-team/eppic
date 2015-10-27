package eppic.commons.util;

import java.util.Iterator;
import java.util.TreeSet;

/**
 * Class representing a unique, ordered (according to 
 * ordering defined in Interval.compareTo) set of intervals
 */
public class IntervalSet extends TreeSet<Interval> implements Comparable<IntervalSet> {

	private static final long serialVersionUID = 1L;
		
	/*---------------------------- public methods ---------------------------*/
	
	/**
	 * Convenience method to add an interval to this set without having to explicitly instantiate an interval object
	 */
	public void addInterval(int start, int end) {
		Interval newInterval = new Interval(start, end);
		this.add(newInterval);
	}
		
	/**
	 * Returns an ordered set of integers resulting from the intersection of all Intervals in this set
	 * @return
	 */
	public TreeSet<Integer> getIntegerSet() {
		TreeSet<Integer> set = new TreeSet<Integer>();
		for (Interval interv:this) {
			for (int i=interv.beg;i<=interv.end;i++) {
				set.add(i);
			}
		}
		return set; 
	}
	
	/**
	 * Intersect the intervals in this set such that non-consecutive, non-overlapping intervals remain.
	 * @return 
	 */
	public IntervalSet getMergedIntervalSet() {
		return IntervalSet.createFromIntSet(this.getIntegerSet());
	}
	
	/**
	 * Whether this IntervalSet overlaps the given one
	 * @param other
	 * @return
	 */
	public boolean overlaps(IntervalSet other) {
		// this can probably be implemented a lot more efficiently, now o(n2)
		for (Interval thisInter:this) {
			for (Interval otherInter:other) {
				if (thisInter.overlaps(otherInter)) return true;
			}
		}
		return false;
	}
	
	/*-------------------------- implemented methods ------------------------*/
	
	/**
	 * Compares two IntevalSets according to a lexicographical-like ordering.
	 */
	@Override
	public int compareTo(IntervalSet other) {
		// whichever set has a smaller first element is smaller
		int compare = this.first().compareTo(other.first());
		if(compare != 0) return compare;
		// otherwise, we have to go through all elements until one is missing
		TreeSet<Integer> thisElements = this.getIntegerSet();
		TreeSet<Integer> otherElements = other.getIntegerSet();
		Iterator<Integer> thisIt = thisElements.iterator();
		Iterator<Integer> otherIt = otherElements.iterator();
		while(thisIt.hasNext() && otherIt.hasNext()) {
			int thisNext = thisIt.next();
			int otherNext = otherIt.next();
			if(thisNext != otherNext) {
				return new Integer(thisNext).compareTo(new Integer(otherNext));
			}
		}
		if(thisIt.hasNext() && !otherIt.hasNext()) return 1;	// this is longer, other ends
		if(!thisIt.hasNext() && otherIt.hasNext()) return -1;	// this ends, other is longer
		// if none of the above checks could break the tie, then we are equal
		return 0;
	}
	
	@Override
	public String toString() {
		String str = "[";

		for (Interval interv:this) {
			str+=interv.toString();
			if (interv!=this.last()) {
				str+=",";
			}
		}
		return str+"]";
	}
	
	/*---------------------------- static methods ---------------------------*/
	
	/**
	 * Returns an IntervalSet created from a set of integers.
	 */
	public static IntervalSet createFromIntSet(TreeSet<Integer> intSet) {
		return Interval.getIntervals(intSet);
	}
	
	/**
	 * Convenience method to create a new interval set containing a single interval.
	 * @param intv the initial interval
	 * @return a new interval set containing a single interval
	 */
	public static IntervalSet createFromInterval(Interval intv) {
		IntervalSet newSet = new IntervalSet();
		newSet.add(intv);
		return newSet;
	}
	
}
