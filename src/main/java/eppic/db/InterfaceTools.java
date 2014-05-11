package eppic.db;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import edu.uci.ics.jung.graph.util.Pair;
import eppic.model.ChainClusterDB;
import eppic.model.InterfaceClusterDB;
import eppic.model.InterfaceDB;
import eppic.model.PdbInfoDB;
import eppic.model.SeqClusterDB;

public class InterfaceTools {
	
	public static final double MIN_AREA_LATTICE_COMPARISON = 100;

	public static double calcInterfaceOverlap(InterfaceDB interf1, InterfaceDB interf2) {
		ContactSet contacts1 = new ContactSet(interf1.getContacts());
		ContactSet contacts2 = new ContactSet(interf2.getContacts());
		
		return contacts1.calcOverlap(contacts2);
		
	}
	
	/**
	 * Returns all InterfaceDB for this PdbInfoDB by looping over all interface clusters
	 * @return
	 */
	public List<InterfaceDB> getInterfaces(PdbInfoDB pdb) {
		List<InterfaceDB> list = new ArrayList<InterfaceDB>();
		for (InterfaceClusterDB ic:pdb.getInterfaceClusters()) {
			for (InterfaceDB interf:ic.getInterfaces()) {
				list.add(interf);
			}
		}
		return list;
	}
	
	public static int getNumInterfacesAboveArea(PdbInfoDB pdb, double area) {
		int count = 0;
		for (InterfaceClusterDB ic:pdb.getInterfaceClusters()) {
			for (InterfaceDB interf:ic.getInterfaces()) {
				if (interf.getArea()>area) count++;	
			}			
		}
		return count;
	}
	
	public static List<InterfaceDB> getInterfacesAboveArea(PdbInfoDB pdbInfo, double area) {
		List<InterfaceDB> list = new ArrayList<InterfaceDB>();
		for (InterfaceClusterDB ic:pdbInfo.getInterfaceClusters()) {
			for (InterfaceDB interf:ic.getInterfaces()) {				
				if (interf.getArea()>area) list.add(interf);
			}
		}
		return list;
	}
	
	public static boolean haveSameContent(PdbInfoDB pdb1, PdbInfoDB pdb2, SeqClusterLevel seqClusterLevel) {
	
		int numChainClusters1 = pdb1.getChainClusters().size();
		int numChainClusters2 = pdb2.getChainClusters().size();
		
		if (numChainClusters1!=numChainClusters2) return false;
		
		
		for (ChainClusterDB chainCluster1:pdb1.getChainClusters()) {
			
			boolean match = false; 
			
			int seqClusterId1 = getSeqClusterId(chainCluster1, seqClusterLevel);
			
			for (ChainClusterDB chainCluster2:pdb2.getChainClusters()) {
				int seqClusterId2 = getSeqClusterId(chainCluster2, seqClusterLevel);
				if (seqClusterId1==seqClusterId2) {
					match = true;
					break;
				}
			}
			// if seqClusterId1 didn't match something in 2 then that's about it: there's different content
			if (!match) return false;
		}
		// if all chains have matches, then we have same content
		return true;
	}
	
	public static int getSeqClusterId(ChainClusterDB chainCluster, SeqClusterLevel seqClusterLevel) {
		SeqClusterDB seqCluster = chainCluster.getSeqCluster();
		int seqClusterId = -1;
		switch (seqClusterLevel) {
		case C100:
			seqClusterId = seqCluster.getC100(); 
			break;
		case C95:
			seqClusterId = seqCluster.getC95(); 
			break;
		case C90:
			seqClusterId = seqCluster.getC90(); 
			break;
		case C80:
			seqClusterId = seqCluster.getC80(); 
			break;
		case C70:
			seqClusterId = seqCluster.getC70(); 
			break;
		case C60:
			seqClusterId = seqCluster.getC60(); 
			break;
		case C50:
			seqClusterId = seqCluster.getC50(); 
			break;

		}
		return seqClusterId;
	}
	
	public static double[][] calcLatticeOverlapMatrix(PdbInfoDB pdb1, PdbInfoDB pdb2) {
		
		HashMap<String,ChainClusterDB> lookup1 = getChainIdLookup(pdb1);
		HashMap<String,ChainClusterDB> lookup2 = getChainIdLookup(pdb2);
		
		int interfCount1 = getNumInterfacesAboveArea(pdb1, InterfaceTools.MIN_AREA_LATTICE_COMPARISON);
		int interfCount2 = getNumInterfacesAboveArea(pdb2, InterfaceTools.MIN_AREA_LATTICE_COMPARISON);
	
		double[][] matrix = new double[interfCount1][interfCount2];

		for (InterfaceDB interf1 : getInterfacesAboveArea(pdb1, InterfaceTools.MIN_AREA_LATTICE_COMPARISON)) {
			for (InterfaceDB interf2 : getInterfacesAboveArea(pdb2, InterfaceTools.MIN_AREA_LATTICE_COMPARISON)) {
				
				if (! isSameContent(interf1, interf2, lookup1, lookup2)) {
					continue; // the matrix value will remain 0
				}
				double co = calcInterfaceOverlap(interf1, interf2);
				matrix[interf1.getInterfaceId()-1][interf2.getInterfaceId()-1] = co;
			}
		}	
		
		return matrix;
	}
	
	private static boolean isSameContent(InterfaceDB interf1, InterfaceDB interf2, 
			HashMap<String,ChainClusterDB> lookup1, HashMap<String,ChainClusterDB> lookup2) {
		
		String chain1i = interf1.getChain1();
		String chain1j = interf1.getChain2();
		String chain2i = interf2.getChain1();
		String chain2j = interf2.getChain2();

		ChainClusterDB c1i = lookup1.get(chain1i);
		ChainClusterDB c1j = lookup1.get(chain1j);
		ChainClusterDB c2i = lookup2.get(chain2i);
		ChainClusterDB c2j = lookup2.get(chain2j);
		
		int sc1i = getSeqClusterId(c1i, SeqClusterLevel.C80);
		int sc1j = getSeqClusterId(c1j, SeqClusterLevel.C80);
		int sc2i = getSeqClusterId(c2i, SeqClusterLevel.C80);
		int sc2j = getSeqClusterId(c2j, SeqClusterLevel.C80);
		
		if (sc1i==sc2i && sc1j==sc2j) {
			return true;
		}
		if (sc1i==sc2j && sc1j==sc2i) {
			return true;
		}
		
		return false;
	}
	
	
	public static HashMap<String,ChainClusterDB> getChainIdLookup(PdbInfoDB pdb) {
		HashMap<String,ChainClusterDB> lookup = new HashMap<String,ChainClusterDB>();
		for (ChainClusterDB cc:pdb.getChainClusters()) {
			lookup.put(cc.getRepChain(), cc);
			if (cc.getMemberChains()!=null) {
				for (String chain:cc.getMemberChains().split(",")) {
					lookup.put(chain,cc);
				}
			}
		}
		return lookup;
	}
	
	public static Pair<ChainClusterDB> getChainClusterPair(InterfaceDB interf) {
		
		ChainClusterDB cc1 = null;
		ChainClusterDB cc2 = null;
		
		return new Pair<ChainClusterDB>(cc1,cc2);
	}
}
