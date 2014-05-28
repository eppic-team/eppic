package eppic.analysis.compare;

import java.util.Set;
import java.util.TreeSet;

import owl.core.structure.BioUnitAssignmentType;

/**
 * A class to reduce a list of PdbBioUnits to a single one 
 * with unique type, mmSize and list of interface-cluster ids  
 * @author duarte_j
 *
 */
public class BioUnitView {
	
	//private int index;
	private BioUnitAssignmentType type;
	private int mmSize;
	private Set<Integer> clusterIds;
	
	public BioUnitView() {
		clusterIds = new TreeSet<Integer>();
	}
	
	public BioUnitView(BioUnitAssignmentType type, int mmSize, Set<Integer> clusterIds) {
		//this.setIndex(index);
		this.type = type;
		this.mmSize = mmSize;
		this.clusterIds = clusterIds;
	}
	
	public boolean equals(Object o) {
		if (!(o instanceof BioUnitView)) return false;
		BioUnitView other = (BioUnitView) o;
		
		if (other.type!=this.type) return false;
		if (other.mmSize!=this.mmSize) return false;
		if (other.clusterIds.size()!=this.clusterIds.size()) return false;
				
		for (int thisClusterId:this.clusterIds) {
			if (!other.clusterIds.contains(thisClusterId)) return false;
		}
		
		return true;
	}

	public void addInterfClusterId(int interfClusterId) {
		this.clusterIds.add(interfClusterId);
	}
	
//	public int getIndex() {
//		return index;
//	}
//
//	public void setIndex(int index) {
//		this.index = index;
//	}

	public BioUnitAssignmentType getType() {
		return type;
	}

	public void setType(BioUnitAssignmentType type) {
		this.type = type;
	}

	public int getMmSize() {
		return mmSize;
	}

	public void setMmSize(int mmSize) {
		this.mmSize = mmSize;
	}

	public Set<Integer> getClusterIds() {
		return clusterIds;
	}

	public void setClusterIds(Set<Integer> clusterIds) {
		this.clusterIds = clusterIds;
	}
}