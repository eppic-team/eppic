package eppic.db.tools.helpers;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import eppic.model.db.PdbInfoDB;

/**
 * A container for a list of PDB structures coming from database.
 * Used to hold a set of PDB structures coming from the same sequence cluster in 
 * order to compare their lattices.
 * 
 * @author Jose Duarte
 *
 */
public class PdbInfoList {
	
	private List<PdbInfo> pdbList;
	private boolean debug;
	
	/**
	 * The minimum area to consider an interface for comparisons. Interfaces below this area
	 * will not be used for comparing lattices.
	 */
	private double minArea;

	private HashMap<Integer, InterfaceCluster> interfaceLookup;
	private HashMap<Integer, Integer> offsets;
	
	public PdbInfoList(List<PdbInfoDB> pdbInfoList) {
		pdbList = new ArrayList<PdbInfo>();
		for (PdbInfoDB pdbInfo:pdbInfoList) {
			pdbList.add(new PdbInfo(pdbInfo)); 
		}		
	}
	
	public void setMinArea(double minArea) {
		this.minArea = minArea;
	}
	
	public LatticeComparisonGroup calcLatticeOverlapMatrix(SeqClusterLevel seqClusterLevel, double coCutoff) { 
		
		LatticeComparisonGroup cfCompare = new LatticeComparisonGroup(this);
		
		for (int i=0;i<pdbList.size();i++) {
			for (int j=0;j<pdbList.size();j++) {
				if (j<i) continue; // note we also do j==i in order to cluster interfaces within a PDB
				PdbInfo ipdb = pdbList.get(i);
				PdbInfo jpdb = pdbList.get(j);
								
				LatticeMatchMatrix lmm = ipdb.calcLatticeOverlapMatrix(jpdb, seqClusterLevel, minArea, debug);
				cfCompare.setElement(i, j, lmm.getLatticeOverlapScore(coCutoff), lmm);
				 
			}

		}
		return cfCompare;
	}
	
	public int size() {
		return pdbList.size();
	}
	
	public PdbInfo get(int i) {
		return pdbList.get(i);
	}
	
	public int getNumInterfaceClusters() {
		interfaceLookup = new HashMap<Integer, InterfaceCluster>();
		offsets = new HashMap<Integer, Integer>();
		
		int count = 0;
		int i = 0;
		for (PdbInfo pdb:this.pdbList) {
			offsets.put(i,count);
			for (InterfaceCluster interfCluster: pdb.getInterfaceClustersAboveArea(minArea)) {
				interfaceLookup.put(count, interfCluster);
				count++;
			}			
			i++;
		}
		return count;
	}
	
	/**
	 * Given an index in the all-interfaceClusters list returns the corresponding interfaceCluster
	 * @param i
	 * @return
	 */
	public InterfaceCluster getInterfaceCluster (int i) {
		 
		if (interfaceLookup==null) getNumInterfaceClusters();
		
		return interfaceLookup.get(i);
	}
	
	/**
	 * Given an index in this PdbInfoList corresponding to a PdbInfo, returns the index of first interfaceCluster for that PdbInfo in the all-interfaceClusters list
	 * @param i
	 * @return
	 */
	public int getOffset(int i) {
		
		if (interfaceLookup==null) getNumInterfaceClusters();
		
		return offsets.get(i);
	}
		
}
