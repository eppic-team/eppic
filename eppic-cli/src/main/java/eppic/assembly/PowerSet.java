package eppic.assembly;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

/**
 * Class containing methods to compute the power set
 * See:
 * http://en.wikipedia.org/wiki/Combination
 * http://en.wikipedia.org/wiki/Power_set
 * 
 * @author duarte_j
 *
 */
public class PowerSet {

	/**
	 * Finds the power set (all combinations) of a set of integers {0,...,n-1},
	 * e.g. for n=3 (set {0,1,2}), the result is {}, {0}, {1}, {2}, {0,1}, {0,2}, {1,2}, {0,1,2}
	 * The results are sorted in group sizes (the key of the map)
	 * See 
	 * http://en.wikipedia.org/wiki/Combination
	 * http://en.wikipedia.org/wiki/Power_set
	 * @param n an integer with max value 31
	 * @return
	 */
	public static Map<Integer,List<int[]>> powerSet(int n) {
		
		
		if (n>=32) throw new IllegalArgumentException("Can't enumerate all combinations for more than 32 elements!");
		
		Map<Integer,List<int[]>> groups = new TreeMap<Integer,List<int[]>>();
		
		for (int k=0;k<=n;k++) {
			groups.put(k, new ArrayList<int[]>());
		}
		
		// the set of all combinations corresponds to a boolean array of size n with number of nodes 2^n (including the empty set)
		// thus a full enumeration is simply counting from 0 to 2^n and getting the boolean array corresponding to it 

		// see for instance: 
		// http://jvalentino.blogspot.ch/2007/02/shortcut-to-calculating-power-set-using.html
		// http://en.wikipedia.org/wiki/Power_set

		
		// note this only works for numInterfClusters<=32 ! (anyway it scales like 2^n so there's no way that we could go beyond that)
		for (int i=0;i< (int) Math.pow(2,n);i++) {
			
			boolean[] v = intToBooleanArray(i, n);

			int k = countGroupSize(v);
						
			List<int[]> sizeKGroups = groups.get(k);
			int[] g = new int[k];
			sizeKGroups.add(g);
			int l = 0;
			for (int j=0;j<v.length;j++) {
				if (v[j]) {
					g[l++] = j; 
				}
			}
			
			
		}
		return groups;
	}

	private static boolean[] intToBooleanArray(int input, int n) {
		
		// see http://stackoverflow.com/questions/8151435/integer-to-binary-array
		
		if (n>=32) throw new IllegalArgumentException("Can't do int to bin array for more than 32!");
	    boolean[] bits = new boolean[n];
	    for (int i = n-1; i >= 0; i--) {
	        bits[i] = (input & (1 << i)) != 0;
	    }
	    return bits;
	}
	
	private static int countGroupSize(boolean[] v) {
		int count=0;
		for (int i=0;i<v.length;i++) {
			if (v[i]) count++;
		}
		return count;
	}
	
	public static void printCombinations(Map<Integer,List<int[]>> combinations, int n) {
		
		for (int k = 0; k<=n;k++) {
			
			List<int[]> kSizeGroups = combinations.get(k);

			System.out.printf("Groups of size %d (%2d): ", k, kSizeGroups.size());
			for (int[] g:kSizeGroups) {
				System.out.print(Arrays.toString(g)+" "); 
			}
			System.out.println();
		}
	}

	/**
	 * Finds the power set (all combinations) of a set of integers {0,...,n-1} representing the result as a boolean array,
	 * e.g. for n=3 (set {0,1,2}), the result is (in binary):
	 * [0,0,0] [1,0,0] [0,1,0] [0,0,1] [1,1,0] [1,0,1] [0,1,1] [1,1,1]
	 * The results are sorted in group sizes (the key of the map)
	 * See 
	 * http://en.wikipedia.org/wiki/Combination
	 * http://en.wikipedia.org/wiki/Power_set
	 * @param n an integer with max value 31
	 * @return
	 */
	public static Map<Integer,List<boolean[]>> powerSetBinary(int n) {
		
		
		if (n>=32) throw new IllegalArgumentException("Can't enumerate all combinations for more than 32 elements!");
		
		int numGroups = (int) Math.pow(2,n);
		
		Map<Integer,List<boolean[]>> groups = new TreeMap<Integer, List<boolean[]>>();
		for (int k=0;k<=n;k++) {
			groups.put(k, new ArrayList<boolean[]>());
		}
		
		// the set of all combinations corresponds to a boolean array of size n with number of nodes 2^n (including the empty set)
		// thus a full enumeration is simply counting from 0 to 2^n and getting the boolean array corresponding to it 

		// see for instance: 
		// http://jvalentino.blogspot.ch/2007/02/shortcut-to-calculating-power-set-using.html
		// http://en.wikipedia.org/wiki/Power_set

		
		// note this only works for numInterfClusters<=32 ! (anyway it scales like 2^n so there's no way that we could go beyond that)
		for (int i=0;i< numGroups; i++) {
			
			boolean[] v = intToBooleanArray(i, n);
			
			int k = countGroupSize(v);
			
			List<boolean[]> sizeKGroups = groups.get(k);
			
			sizeKGroups.add(v);
			
		}
		return groups;
	}

	/**
	 * A recursive approach to finding the power set as explained here:
	 * http://stackoverflow.com/questions/1670862/obtaining-a-powerset-of-a-set-in-java
	 * @param originalSet
	 * @return
	 */
	public static <T> Set<Set<T>> powerSet(Set<T> originalSet) {
		Set<Set<T>> sets = new HashSet<Set<T>>();
		if (originalSet.isEmpty()) {
			sets.add(new HashSet<T>());
			return sets;
		}
		List<T> list = new ArrayList<T>(originalSet);
		T head = list.get(0);
		Set<T> rest = new HashSet<T>(list.subList(1, list.size())); 
		for (Set<T> set : powerSet(rest)) {
			Set<T> newSet = new HashSet<T>();
			newSet.add(head);
			newSet.addAll(set);
			sets.add(newSet);
			sets.add(set);
		}		
		return sets;
	}


	/**
	 * A recursive approach to getting the power set as explained here:
	 * http://stackoverflow.com/questions/20551862/find-the-powerest-of-a-set-recursively
	 * @param originalSet
	 * @return
	 */
	public static <T> List<List<T>> powerSet(final LinkedList<T> originalSet) {
	    final List<List<T>> powerset = new LinkedList<List<T>>();
	    //Base case: empty set
	    if (originalSet.isEmpty()) {
	        final List<T> set = new ArrayList<T>();
	        //System.out.println(set);
	        powerset.add(set);
	    } else { 
	        //Recursive case:
	        final T firstElement = originalSet.removeFirst();
	        final List<List<T>> prevPowerset = powerSet(originalSet);
	        powerset.addAll(prevPowerset);
	        //Add firstElement to each of the set of the previuos powerset
	        for (final List<T> prevSet : prevPowerset) {
	            final List<T> newSet = new ArrayList<T>(prevSet);
	            newSet.add(firstElement);
	            //System.out.println(newSet); 
	            powerset.add(newSet);
	        }
	    }
	    return powerset;
	}
	
	public static void main (String[] args) {
		System.out.println("Power set with recursive approach: ");
		LinkedList<Integer> list = new LinkedList<Integer>();
		list.add(0);
		list.add(1);
		list.add(2);
		list.add(3);
		List<List<Integer>> pw = powerSet(list);
		System.out.println("The final power set has size "+pw.size()+":");
		for (List<Integer> l:pw) {
			System.out.println(l);
		}
		
		System.out.println("Power set with binary approach: ");
		Map<Integer,List<int[]>> power = powerSet(4);
		printCombinations(power, 4);
	}

}
