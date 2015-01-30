package eppic.db;

import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import org.biojava.nbio.core.util.SingleLinkageClusterer;



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
		
		
		// first we convert the latticeOverlapMatrix into a double matrix
		double[][] matrix = new double[pdbInfoList.size()][pdbInfoList.size()];
		for (int i=0;i<latticeOverlapMatrix.length;i++) {
			for (int j=i+1;j<latticeOverlapMatrix[i].length;j++) {
				matrix[i][j] = latticeOverlapMatrix[i][j].getAvgScore();
			}
		}
		
		// note that the clusterer alters the matrix, keep that in mind if we wanted to use the matrix down the line
		SingleLinkageClusterer cl = new SingleLinkageClusterer(matrix, true);
		//cl.setDebug();
		Map<Integer,Set<Integer>> cls = cl.getClusters(losCutoff);
		
		// return the unique list sorted by ids (thanks to equals, hashCode and compareTo)
		Set<PdbInfoCluster> set = new TreeSet<PdbInfoCluster>();
		
		for (int clusterId:cls.keySet()) {
			PdbInfoCluster pdbInfoCluster = new PdbInfoCluster(clusterId);
			for (int member:cls.get(clusterId)) {
				
				pdbInfoCluster.addMember(pdbInfoList.get(member));
				
			}
			set.add(pdbInfoCluster);
			
		}
		
		return set;
	}
	
	public Collection<InterfaceCluster> getInterfClusters(double coCutoff) {
		
		// note that the clusterer alters the matrix, keep that in mind if we wanted to use the matrix down the line
		SingleLinkageClusterer cl = new SingleLinkageClusterer(interfCompMatrix, true);
		
		Map<Integer, Set<Integer>> cls = cl.getClusters(coCutoff);
		
		
		// return the unique list sorted by ids (thanks to equals, hashCode and compareTo)
		Set<InterfaceCluster> set = new TreeSet<InterfaceCluster>();
		
		for (int clusterId:cls.keySet()) {
			InterfaceCluster interfCluster = new InterfaceCluster(clusterId);
			for (int member:cls.get(clusterId)) {
				
				interfCluster.addMember(pdbInfoList.getInterface(this.minArea, member)); 
				
			}
			set.add(interfCluster);
			
		}
		
		return set;
	}
		
}
