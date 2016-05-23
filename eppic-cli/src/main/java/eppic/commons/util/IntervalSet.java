package eppic.commons.util;

import java.util.Collection;
import java.util.Comparator;
import java.util.Iterator;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
		
	public IntervalSet() {
		super();
	}

	public IntervalSet(Collection<? extends Interval> c) {
		super(c);
	}

	public IntervalSet(Comparator<? super Interval> comparator) {
		super(comparator);
	}

	/**
	 * Returns an IntervalSet created from a set of integers.
	 */
	public IntervalSet(SortedSet<Integer> intSet) {
		if(intSet.size() > 0) {
			// intSet is sorted because it's a TreeSet
			int last = intSet.first();	// previous element
			int start = last;			// start if current interval
			for(int n:intSet) {
				int i = n;
				if(i > last+1) {
					// output interval and start new one
					add(new Interval(start, last));
					start = i;
					last = i;
				} else if(i == last+1) {
					last = i;
				}
			}
			// output last interval
			add(new Interval(start, last));
		}
	}

	/**
	 * Create a new TreeSet of integers from a selection string in 'comma-hyphen' nomenclature, e.g. 1-3,5,7-8.
	 * The validity of a selection string can be checked by isValidSelectionString().
	 * @param selStr Comma-separated string of intervals, e.g. "1-5,7,8-10"
	 * @return A TreeSet of integers corresponding to the given selection string or null of string is invalid.
	 */
	public IntervalSet(String selStr) {
		super();
		if(!isValidSelectionString(selStr))
			throw new IllegalArgumentException("Malformed interval string");
		String[] tokens = selStr.trim().split(",");		// allow spaces around whole string
		for(String t:tokens) {
			t = t.trim(); // allow spaces around commas
			if(t.contains("-")) {
				String[] range = t.split("-");
				int from = Integer.parseInt(range[0]);
				int to = Integer.parseInt(range[1]);
				this.add(new Interval(from, to));
			} else if( !t.isEmpty() ){
				int num = Integer.parseInt(t);
				this.add(new Interval(num));
			}
		}
	}

	/**
	 * Returns an ordered set of integers resulting from the union of all Intervals in this set
	 * @return
	 */
	public TreeSet<Integer> getIntegerSet() {
		TreeSet<Integer> set = new TreeSet<Integer>();
		for (Interval interv:this) {
			for (int i=interv.getBeginning();i<=interv.getEnd();i++) {
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
		IntervalSet newSet = new IntervalSet();
		if( !isEmpty() ) {
			Iterator<Interval> it = this.iterator();
			Interval last = it.next();
			while(it.hasNext()) {
				Interval next = it.next();
				// Check for overlap
				if(last.getEnd()+1 >= next.getBeginning()) {
					// merge them
					last = new Interval(last.getBeginning(), Math.max(last.getEnd(),next.getEnd()));
				} else {
					newSet.add(last);
					last = next;
				}
			}
			newSet.add(last);
		}
		return newSet;
	}
	
	/**
	 * Whether this IntervalSet overlaps the given one
	 * @param other
	 * @return
	 */
	public boolean overlaps(IntervalSet other) {
		if( !this.isEmpty() && !other.isEmpty() ) {
			Iterator<Interval> a = this.iterator();
			Iterator<Interval> b = other.iterator();
			Interval nextA = a.next();
			Interval nextB = b.next();
			
			if(nextA.overlaps(nextB)) {
				return true;
			}
			while(a.hasNext() || b.hasNext()) {
				// Advance lower interval
				if( nextA.getEnd() <= nextB.getEnd() ) {
					if( a.hasNext() ) {
						nextA = a.next();
					} else {
						return false;
					}
				} else {
					if( b.hasNext() ) {
						nextB = b.next();
					} else {
						return false;
					}
				}
				if(nextA.overlaps(nextB)) {
					return true;
				}
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
	 * Returns true if selStr is a valid selection string in 'comma-hyphen' syntax, e.g. 1-3,5,7-8.
	 * @return true if selStr is a syntactically correct selection string, false otherwise
	 */
	public static boolean isValidSelectionString(String selStr) {
		Pattern p = Pattern.compile("^ *(\\d+(-\\d+)?( *, *\\d+(-\\d+)?)*)? *$");
		Matcher m = p.matcher(selStr.trim()); // allow spaces around whole string and around commas
		return m.matches();
	}

	/**
	 * Convert to a comma-separated range, e.g. "1-6,8,10-11"
	 * @return
	 */
	public String toSelectionString() {
		if(isEmpty()) {
			return "";
		}
		
		StringBuilder str = null;
		for( Interval range : this) {
			if(str == null) {
				str = new StringBuilder();
			} else {
				str.append(",");
			}
			str.append(range.toSelectionString());
		}
		return str.toString();
	}
}
