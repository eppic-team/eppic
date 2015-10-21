package eppic.assembly;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;



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
}
