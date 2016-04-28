package eppic.assembly;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;

/**
 * A representation of the stoichiometry of a set of entities.
 * The stoichiometry is just the occurance counts of a fixed set of possible
 * values.
 * @author duarte_j
 *
 */
public class Stoichiometry<T> {
	//private static final Logger logger = LoggerFactory.getLogger(Stoichiometry.class);

	private final List<T> values;
	//Stoichiometry for each entity
	private final int[] sto;

	/**
	 * Counts the occurrence of each element of `possibleValues` in `instances`.
	 * 
	 * The order of possibleValues is preserved, but duplicates will be removed.
	 * @param instances Collection
	 * @param possibleValues Set of all possible values. If null, the union of all
	 *  elements in instances will be used.
	 * @throws IndexOutOfBoundsException if instances contains values not in allValues
	 */
	public Stoichiometry(Collection<T> instances, List<T> possibleValues) {
		// Ensure unique
		if(possibleValues == null) {
			values = instances.stream().distinct().collect(Collectors.toList());
		} else {
			values = possibleValues.stream().distinct().collect(Collectors.toList());
		}

		// count instances
		sto = new int[values.size()];
		for(T val : instances) {
			sto[values.indexOf(val)]++;
		}
	}
	
	
	public List<T> getValues() {
		return values;
	}

	/**
	 * @return The number of non-zero values
	 */
	public int getNumPresent() {
		int numPresentEntities = 0;
		for (int i=0;i<sto.length;i++) {
			if (sto[i]>0) {
				numPresentEntities ++;
			}
		}
		return numPresentEntities;
	}
	
	/**
	 * Returns the stoichiometry represented as a vector of positive integers with counts per entity
	 * @return
	 */
	public int[] getStoichiometry() {
		return sto;
	}
	
	public int getCount(T val) {
		return sto[values.indexOf(val)];
	}
	
	public int getCountForIndex(int i) {
		return sto[i];
	}
	
	/**
	 * Sum of the stoichiometry vector
	 * @return
	 */
	public int getTotalSize() {
		return IntStream.of(sto).sum();
	}
	
	/**
	 * Tells whether 2 stoichiometries are overlapping in any of their entities,
	 * this is equivalent to !{@link #isOrthogonal(Stoichiometry)}
	 * @param other
	 * @return
	 */
	public boolean isOverlapping(Stoichiometry<T> other) {
		return !isOrthogonal(other);
	}
	
	/**
	 * Tells whether 2 stoichiometries are orthogonal: i.e. they don't share any of their entities
	 * @param other
	 * @return
	 */
	public boolean isOrthogonal(Stoichiometry<T> other) {
		if (this.values.equals(other.values) )
			throw new IllegalArgumentException("Vectors have different values");
		if (this.values.size() ==0) 
			throw new IllegalArgumentException("Vectors have size 0"); 
		
		int scalarProduct = 0;
		for (int i=0;i<sto.length;i++) {
			scalarProduct += sto[i]*other.sto[i];
		}
		return scalarProduct == 0;
	}
	
	/**
	 * Tells whether this stoichiometry is even: a stoichiometry is even if and
	 * only if all of its non-zero members have the same count
	 * @return true if even, false otherwise, if all members have 0 count it also returns false
	 */
	public boolean isEven() {
		int nonzero = getFirstNonZero();
		// all are zero
		if (nonzero == -1) return false;
		
		for (int i=0;i<this.sto.length;i++) {
			if (sto[i]>0 && sto[i]!=nonzero) return false;
		}
		
		return true;
	}
	
	/**
	 * Return the first non-zero count or -1 if all are 0
	 * @return
	 */
	protected int getFirstNonZero() {
		for (int i=0;i<this.sto.length;i++) {
			if (sto[i]>0) {
				return sto[i];
			}
		}
		return -1;
	}
	
	/**
	 * Return a string representation of the Stoichiometry where each entity is represented as 
	 * sequential letters: A, ... Z, AA, AB, ... AZ, BA, BB, ... ZZ
	 * Thus the letters have no relation to the actual values, and are
	 * merely sorted in descending order of the number of copies
	 * @return
	 */
	public String toFormattedStringRelettered() {
		// Sorted list of stoichiometries in decreasing order
		List<Integer> sortedSto = IntStream.of(sto)
			.filter(i -> i>0)
			.boxed()
			.sorted(  (i,j) -> j.compareTo(i))
			.collect(Collectors.toList());
		// Convert to string like "A(2) B"
		return IntStream.range(0, sortedSto.size())
			.mapToObj(i -> {int s = sortedSto.get(i); return indexToLetters(i) + ( s>1 ? "("+s+")" : ""); } )
			.collect(Collectors.joining(" "));
	}
	
	private static String indexToLetters(int i) {
		int quot = i/26;
		int rem = i%26;
		char letter = (char) ('A' + rem);
		if(quot == 0) {
			return String.valueOf(letter);
		} else {
			return indexToLetters(quot-1) + letter;
		}
	}


	/**
	 * Return a string representation of the Stoichiometry in the form of
	 * "Value1(copies) Value2(copies) ...". The toString() method is used for
	 * each value.
	 * @return
	 */
	public String toFormattedString() {
		return toFormattedString(t -> t.toString());
	}
	/**
	 * Return a string representation of the Stoichiometry in the form of
	 * "Value1(copies) Value2(copies) ...". A custom function is used to convert
	 * values to strings.
	 * @param getValueString Function mapping from a value to a string representation
	 * @return
	 */
	public String toFormattedString(Function<T,String> getValueString) {
		return IntStream.range(0, sto.length)
				.filter(i -> sto[i] > 0)
				.mapToObj(i -> {int s = sto[i]; return getValueString.apply(values.get(i)) + ( s>1 ? "("+s+")" : ""); } )
				.collect(Collectors.joining(" "));
	}



	/**
	 * Returns true if this stoichiometry covers all entities, i.e. it has 
	 * a count >0 for all of its member molecules. False otherwise
	 * @return
	 */
	public boolean isFullyCovering() {
		for (int i=0;i<sto.length;i++) {
			if (sto[i]==0) return false;
		}
		return true;
	}
	
	@Override
	public String toString() {
		return Arrays.toString(sto);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + Arrays.hashCode(sto);
		result = prime * result + ((values == null) ? 0 : values.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Stoichiometry<?> other = (Stoichiometry<?>) obj;
		if (!Arrays.equals(sto, other.sto))
			return false;
		if (values == null) {
			if (other.values != null)
				return false;
		} else if (!values.equals(other.values))
			return false;
		return true;
	}


//	/**
//	 * Return the greatest common divisor (GCD, aka greatest common factor GCF) for the given ints
//	 * This is an implementation of the Euclidean algorithm:
//	 * http://en.wikipedia.org/wiki/Euclidean_algorithm
//	 * Thanks to this SO question: 
//	 * http://stackoverflow.com/questions/4009198/java-get-greatest-common-divisor
//	 * @param a
//	 * @param b
//	 * @return
//	 */
//	private static int gcd(int a, int b) {		
//		if (b==0) return a;
//		return gcd(b,a%b);
//	}
//
//	/**
//	 * Return the greatest common divisor (GCD) of the given array of ints
//	 * @param values
//	 * @return
//	 */
//	public static int gcd(int[] values) {
//		if (values.length==0) throw new IllegalArgumentException("Can't calculate GCD for an empty array");
//		
//		// we go recursively		
//		int result = values[0];
//		for(int i = 1; i < values.length; i++){
//		    result = gcd(result, values[i]);
//		}
//		return result;
//		
//	}
	
	public static void main (String[] args) {
		for (int i=0;i<800;i++) {
			System.out.print(indexToLetters(i)+",");
			if(i%80 == 0)
				System.out.println();
		}
	}
}
