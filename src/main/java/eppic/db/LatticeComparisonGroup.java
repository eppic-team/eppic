package eppic.db;

import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;


/**
 * A class to contain the comparison of lattices for a set of PDBs,
 * from which the Crystal Form clusters and interface clusters can be obtained.
 * 
 * 
 * @author duarte_j
 *
 */
public class LatticeComparisonGroup {
	
	
	private PdbInfoList pdbInfoList;
	
	private double minArea;
	
	private LatticeOverlapScore[][] latticeOverlapMatrix;
	private double[][] interfCompMatrix;

	public LatticeComparisonGroup(PdbInfoList pdbInfoList, double minArea) {
		this.pdbInfoList = pdbInfoList;
		
		this.latticeOverlapMatrix = new LatticeOverlapScore[pdbInfoList.size()][pdbInfoList.size()];
		
		this.minArea = minArea;
		
		int numInterfaces = pdbInfoList.getNumInterfaces(minArea);
		
		this.interfCompMatrix = new double[numInterfaces][numInterfaces];
	}
	
	public void setElement(int i, int j, LatticeOverlapScore los, LatticeMatchMatrix lmm) {
		
		latticeOverlapMatrix[i][j] = los;
		
		double[][] coMatrix = lmm.getCoMatrix();
		
		//System.out.println("offset "+i+","+j+": "+pdbInfoList.getOffset(minArea, i)+" "+pdbInfoList.getOffset(minArea, j));
		
		for (int k=0;k<coMatrix.length;k++) {
			for (int l=0;l<coMatrix[k].length;l++){
				interfCompMatrix [k + pdbInfoList.getOffset(minArea, i)]
								 [l + pdbInfoList.getOffset(minArea, j)] = coMatrix[k][l];
			}
		}
	}
	
	public LatticeOverlapScore[][] getLatticeComparisonMatrix() {
		return latticeOverlapMatrix;
	}
	
	public double[][] getInterfaceComparisonMatrix() {
		return interfCompMatrix;
	}
	
	public Collection<PdbInfoCluster> getCFClusters(double losCutoff) {
		
		Map<Integer, PdbInfoCluster> clusters = new TreeMap<Integer, PdbInfoCluster>();
		
		int clusterId = 1;
		
		for (int i=0;i<latticeOverlapMatrix.length;i++) {
			for (int j=i+1;j<latticeOverlapMatrix[i].length;j++) {
				if (latticeOverlapMatrix[i][j].getAvgScore() > losCutoff) {
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
	
	public Collection<InterfaceCluster> getInterfClusters(double coCutoff) {
		
		Map<Integer, InterfaceCluster> clusters = new TreeMap<Integer, InterfaceCluster>();
		
		int clusterId = 1;
		
		for (int i=0;i<interfCompMatrix.length;i++) {
			for (int j=i+1;j<interfCompMatrix[i].length;j++) {
				if (interfCompMatrix[i][j] > coCutoff) {
					if (clusters.containsKey(i)) {
						InterfaceCluster interfCluster = clusters.get(i);
						interfCluster.addMember(pdbInfoList.getInterface(this.minArea,j));
						
						clusters.put(j, interfCluster);
					} else {
						InterfaceCluster interfCluster = new InterfaceCluster(clusterId);
						interfCluster.addMember(pdbInfoList.getInterface(this.minArea,i));
						interfCluster.addMember(pdbInfoList.getInterface(this.minArea,j));
						clusters.put(i, interfCluster);
						clusters.put(j, interfCluster);
						clusterId++;
					}
				} 
			}
		}
		
		// anything not clustered is assigned to a singleton cluster (cluster with one member)
		for (int i=0;i<pdbInfoList.size();i++) {
			if (!clusters.containsKey(i)) {
				InterfaceCluster interfCluster = new InterfaceCluster(clusterId);
				interfCluster.addMember(pdbInfoList.getInterface(this.minArea,i));
				clusters.put(i,interfCluster);
				clusterId++;
			}
		}
		
		// return the unique list sorted by ids (thanks to equals, hashCode and compareTo)
		Set<InterfaceCluster> set = new TreeSet<InterfaceCluster>();
		set.addAll(clusters.values());
		
		return set;
	}
		
}
