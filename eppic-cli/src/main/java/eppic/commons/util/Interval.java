package eppic.commons.util;
import java.io.Serializable;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Class representing an integer interval with a beginning 
 * and an end integer
 */
public class Interval implements Comparable<Interval>, Serializable {
	
	private static final long serialVersionUID = 1L;

	public int beg;
	public int end;

	public Interval(int beg,int end){
		this.beg=beg;
		this.end=end;
	}
	
	public int compareTo(Interval other) {
		if (this.beg>other.beg){
			return 1;
		} 
		else if (this.beg<other.beg){
			return -1;
		}
		else {
			if (this.end>other.end){
				return 1;
			}
			else if (this.end<other.end){
				return -1;
			}
		}				
		return 0; // if none of the conditions before returned, then both beg and end are equal
	}
	
	public int getLength(){
		return (end - beg + 1);
	}
	
	/**
	 * Whether this Interval overlaps the given interval
	 * @param other
	 * @return
	 */
	public boolean overlaps(Interval other) {
		if (this.end>other.beg && this.beg<other.end) {
			return true;
		}
		return false;
	}

	public boolean equals(Object o){
		Interval other = (Interval) o;
		if (this.beg==other.beg && this.end==other.end){
			return true;
		}
		return false;
	}

	public String toString() {
		return this.beg+" "+this.end;
	}
	
	
	/*----------------------------- static methods --------------------------------*/
	
	/**
	 * Returns true if selStr is a valid selection string in 'comma-hyphen' syntax, e.g. 1-3,5,7-8.
	 * @return true if selStr is a syntactically correct selection string, false otherwise
	 */
	public static boolean isValidSelectionString(String selStr) {
		Pattern p = Pattern.compile("\\d+(-\\d+)?( *, *\\d+(-\\d+)?)*");
		Matcher m = p.matcher(selStr.trim()); // allow spaces around whole string and around commas
		return m.matches();
	}
	
	/**
	 * Create a new TreeSet of integers from a selection string in 'comma-hyphen' nomenclature, e.g. 1-3,5,7-8.
	 * The validity of a selection string can be checked by isValidSelectionString().
	 * @return A TreeSet of integers corresponding to the given selection string or null of string is invalid.
	 */
	public static TreeSet<Integer> parseSelectionString(String selStr) {
		if(!isValidSelectionString(selStr)) return null;
		TreeSet<Integer> newSet = new TreeSet<Integer>();
		String[] tokens = selStr.trim().split(",");		// allow spaces around whole string
		for(String t:tokens) {
			if(t.contains("-")) {
				String[] range = t.trim().split("-");	// allow spaces around commas
				int from = Integer.parseInt(range[0]);
				int to = Integer.parseInt(range[1]);
				for(int i=from; i <= to; i++) {
					newSet.add(i);
				}
				
			} else {
				int num = Integer.parseInt(t.trim());	// allow spaces around commas
				newSet.add(num);
			}
		}
		return newSet;
	}
	
	/**
	 * Creates selection string from the given set of integers.
	 * @param intSet  set of integers
	 * @return  selection string for PyMol, e.g. 
	 *  <code>intSet=[1 2 4 5 6 9]</code> yields <code>1-2,4-6,9,</code>
	 */
	public static String createSelectionString(TreeSet<Integer> intSet) {		
		switch(intSet.size()) {
		case 0:
			return "";
		case 1:
			return intSet.first().toString();
		}		
		int last = intSet.first();
		int start = last;
		String resString = "";
		
		for(int i:intSet) {
			if(i > last+1) {
				resString += (last-start == 0?last:(start + "-" + last)) + ",";
				start = i;
				last = i;
			} else
				if(i == last) {
					// skip
				} else
					if(i == last+1) {
						last = i;
					}
		}
		resString += (last-start == 0?last:(start + "-" + last));		
		return resString;
	}
	
	/**
	 * Returns the set as a vector of intervals of consecutive elements.
	 * @return
	 */
	public static IntervalSet getIntervals(TreeSet<Integer> intSet) {
		IntervalSet intervals = new IntervalSet();
		if(intSet.size() == 0) return intervals;
		// intSet is sorted becaus it's a TreeSet
		int last = intSet.first();	// previous element
		int start = last;			// start if current interval
		for(int n:intSet) {
			int i = n;
			if(i > last+1) {
				// output interval and start new one
				intervals.add(new Interval(start, last));
				start = i;
				last = i;
			} else
			if(i == last) {
				// can that even happen?
			} else
			if(i == last+1) {
				last = i;
			}
		}
		// output last interval
		intervals.add(new Interval(start, last));
		return intervals;
	}
}

