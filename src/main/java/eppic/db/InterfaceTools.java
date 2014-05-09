package eppic.db;

import java.util.ArrayList;
import java.util.List;

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
			
			int seqClusterId1 = getChainClusterId(chainCluster1, seqClusterLevel);
			
			for (ChainClusterDB chainCluster2:pdb2.getChainClusters()) {
				int seqClusterId2 = getChainClusterId(chainCluster2, seqClusterLevel);
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
	
	public static int getChainClusterId(ChainClusterDB chainCluster, SeqClusterLevel seqClusterLevel) {
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
}
