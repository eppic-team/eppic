package eppic.db;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import eppic.model.PdbInfoDB;

public class PdbInfoList {
	
	private List<PdbInfo> pdbList;
	private boolean debug;

	private HashMap<Integer, Interface> interfaceLookup;
	private HashMap<Integer, Integer> offsets;
	
	public PdbInfoList(List<PdbInfoDB> pdbInfoList) {
		pdbList = new ArrayList<PdbInfo>();
		for (PdbInfoDB pdbInfo:pdbInfoList) {
			pdbList.add(new PdbInfo(pdbInfo)); 
		}		
	}
	
	public LatticeComparisonGroup calcLatticeOverlapMatrix(SeqClusterLevel seqClusterLevel, double coCutoff, double minArea) {
		
		LatticeComparisonGroup cfCompare = new LatticeComparisonGroup(this, minArea);
		
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
	
	public int getNumInterfaces(double minArea) {
		interfaceLookup = new HashMap<Integer, Interface>();
		offsets = new HashMap<Integer, Integer>();
		
		int count = 0;
		int i = 0;
		for (PdbInfo pdb:this.pdbList) {
			offsets.put(i,count);
			for (Interface interf: pdb.getInterfacesAboveArea(minArea)) {
				interfaceLookup.put(count, interf);
				count++;
			}			
			i++;
		}
		return count;
	}
	
	public Interface getInterface (double minArea, int i) {
		
		if (interfaceLookup==null) getNumInterfaces(minArea);
		
		return interfaceLookup.get(i);
	}
	
	public int getOffset(double minArea, int i) {
		
		if (interfaceLookup==null) getNumInterfaces(minArea);
		
		return offsets.get(i);
	}
		
}
