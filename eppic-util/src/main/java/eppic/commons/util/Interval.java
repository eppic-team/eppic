package eppic.commons.util;
import java.io.Serializable;

/**
 * Class representing an integer interval with a beginning 
 * and an end integer
 */
public class Interval implements Comparable<Interval>, Serializable {

	private static final long serialVersionUID = -9176989820409250996L;

	public static final Interval INFINITE_INTERVAL = new Interval(Integer.MIN_VALUE,Integer.MAX_VALUE);

	public final int beg;
	public final int end;

	/**
	 * Create an interval (beg,end) inclusive.
	 * @param beg
	 * @param end
	 */
	public Interval(int beg,int end){
		if( beg <= end) {
			this.beg=beg;
			this.end=end;
		} else {
			this.beg=end;
			this.end=beg;
		}
	}
	/**
	 * Construct an interval consisting of a single position
	 * @param single
	 */
	public Interval(int single) {
		this.beg = this.end = single;
	}
	
	/**
	 * @return the beginning
	 */
	public int getBeginning() {
		return beg;
	}

	/**
	 * @return the end
	 */
	public int getEnd() {
		return end;
	}

	/**
	 * Sort intervals based on beginning and end coordinates
	 */
	@Override
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
		if (this.end>=other.beg && this.beg<=other.end) {
			return true;
		}
		return false;
	}
	/**
	 * Test if a point falls within this interval
	 * @param i
	 * @return
	 */
	public boolean contains(int i) {
		return this.beg <= i && i <= this.end;
	}

	@Override
	public boolean equals(Object o){
		Interval other = (Interval) o;
		if (this.beg==other.beg && this.end==other.end){
			return true;
		}
		return false;
	}

	@Override
	public String toString() {
		return this.beg+" "+this.end;
	}
	
	public String toSelectionString() {
		if( this.isInfinite() ) {
			return "*";
		} else if( beg != end ) {
			return String.format("%d-%d", beg,end);
		} else {
			return Integer.toString(beg);
		}
	}
	
	/**
	 * Test if this interval contains all possible integers.
	 * @return
	 */
	public boolean isInfinite() {
		return this.equals(INFINITE_INTERVAL);
	}
}

