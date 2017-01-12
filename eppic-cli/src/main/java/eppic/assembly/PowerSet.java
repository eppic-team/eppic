package eppic.assembly;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;



/**
 * Class to encapsulate a power set representation as a boolean array
 * 
 * See:
 * http://en.wikipedia.org/wiki/Combination
 * http://en.wikipedia.org/wiki/Power_set
 * 
 * @author duarte_j
 *
 */
public class PowerSet {

	private boolean[] set;

	public PowerSet(int size) {
		this.set = new boolean[size];
	}
	
	public PowerSet(boolean[] set) {
		this.set = set;
	}
	
	/**
	 * Copy constructor
	 * @param powerSet
	 */
	public PowerSet(PowerSet powerSet) {
		this.set = powerSet.set.clone();
	}
	
	public void switchOn(int i) {
		this.set[i] = true;
	}
	
	public void switchOff(int i) {
		this.set[i] = false;
	}
	
	public boolean isOn(int i) {
		return set[i];
	}
	
	public boolean isOff(int i) {
		return !set[i];
	}
	
	/**
	 * Returns true if this set is a child of any of the given parents, false otherwise
	 * @param parents
	 * @return
	 */
	public boolean isChild(List<PowerSet> parents) {

		for (PowerSet invalidGroup:parents) {
			if (this.isChild(invalidGroup)) return true;
		}
		return false;
	}
	
	/**
	 * Returns true if this set is child of the given potentialParent
	 * 
	 * @param potentialParent
	 * @return true if is a child false if not
	 */
	public boolean isChild(PowerSet potentialParent) {
		
		for (int i=0;i<set.length;i++) {
			if (potentialParent.set[i]) {
				if (!set[i]) return false;
			}
		}
		return true;
	}

	/**
	 * Gets all sets that are children of this set, not adding those that are also
	 * children of other invalidParents
	 * @param invalidParents
	 * @return
	 */
	public List<PowerSet> getChildren(List<PowerSet> invalidParents) {
		
		List<PowerSet> children = new ArrayList<PowerSet>();

		for (int i=0;i<this.set.length;i++) {

			if (!this.set[i]) {
				PowerSet a = new PowerSet(this);
				a.switchOn(i);
				// first we need to check that this is not a child of one of the invalidParents
				if (a.isChild(invalidParents)) continue;
				
				children.add(a);
			}
		}

		return children;
	}
	
	public int size() {
		return this.set.length;
	}
	
	@Override
	public boolean equals(Object other) {
		if (! (other instanceof PowerSet)) return false;
		
		PowerSet o = (PowerSet) other;
		
		return Arrays.equals(this.set, o.set);
		
	}
	
	@Override
	public int hashCode() {
		return Arrays.hashCode(this.set);
	}
	
	@Override
	public String toString() {
		return Arrays.toString(this.set);
	}
	
	/**
	 * Obtain the set of all PowerSets with a subset of the true positions of this PowerSet,
	 * with a minimum number of true values.
	 * Equivalent to generating the parents? - Aleix
	 * @param minOn minimum number of On values in the set
	 * @return Set of PowerSet
	 */
	public Set<PowerSet> getOnPowerSet(int minOn) {
		
		Set<PowerSet> sets = new HashSet<PowerSet>();
        if (sizeOn() <= minOn) {
            return sets;
        }
        
        // Generate all sets at distance 1 and continue recursively
        for (int p=0; p<set.length; p++){
        	if (set[p]){
        		PowerSet cs = new PowerSet(this);
        		cs.switchOff(p);
        		sets.add(cs);
        		sets.addAll(cs.getOnPowerSet(minOn));
        	}
        }
        return sets;
	}
	
	/**
	 * Number of true values in this PowerSet.
	 * @return number of {@link #isOn(int)} positions.
	 */
	public int sizeOn() {
		int count = 0;
		for (int p=0; p<set.length; p++){
			if (set[p])
				count++;
		}
		return count;
	}
}
