package eppic.db;

import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;


/**
 * A class to contain a matrix of lattice overlap scores for a set of PDBs,
 * from which the Crystal Form clusters can be obtained.
 * 
 * 
 * @author duarte_j
 *
 */
public class CFCompareMatrix {
	
	
	private PdbInfoList pdbInfoList;
	
	private LatticeOverlapScore[][] matrix;

	public CFCompareMatrix(PdbInfoList pdbInfoList, LatticeOverlapScore[][] matrix) {
		this.pdbInfoList = pdbInfoList;
		this.matrix = matrix;
	}
	
	public LatticeOverlapScore[][] getMatrix() {
		return matrix;
	}
	
	public Collection<PdbInfoCluster> getClusters(double losCutoff) {
		
		Map<Integer, PdbInfoCluster> clusters = new TreeMap<Integer, PdbInfoCluster>();
		
		int clusterId = 1;
		
		for (int i=0;i<matrix.length;i++) {
			for (int j=i+1;j<matrix[i].length;j++) {
				if (matrix[i][j].getAvgScore() > losCutoff) {
					if (clusters.containsKey(i)) {
						PdbInfoCluster pdbInfoCluster = clusters.get(i);
						pdbInfoCluster.addMember(pdbInfoList.get(j));
						
						clusters.put(j, pdbInfoCluster);
					} else {
						PdbInfoCluster pdbInfoCluster = new PdbInfoCluster(clusterId);
						pdbInfoCluster.addMember(pdbInfoList.get(i));
						pdbInfoCluster.addMember(pdbInfoList.get(j));
						clusters.put(i, pdbInfoCluster);
						clusters.put(j, pdbInfoCluster);
						clusterId++;
					}
				} 
			}
		}
		
		// anything not clustered is assigned to a singleton cluster (cluster with one member)
		for (int i=0;i<pdbInfoList.size();i++) {
			if (!clusters.containsKey(i)) {
				PdbInfoCluster pdbInfoCluster = new PdbInfoCluster(clusterId);
				pdbInfoCluster.addMember(pdbInfoList.get(i));
				clusters.put(i,pdbInfoCluster);
				clusterId++;
			}
		}
		
		// return the unique list sorted by ids (thanks to equals, hashCode and compareTo)
		Set<PdbInfoCluster> set = new TreeSet<PdbInfoCluster>();
		set.addAll(clusters.values());
		
		return set;
	}
}
