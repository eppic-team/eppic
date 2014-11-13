package eppic.db;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import owl.core.sequence.alignment.PairwiseSequenceAlignment;
import owl.core.sequence.alignment.PairwiseSequenceAlignment.PairwiseSequenceAlignmentException;
import edu.uci.ics.jung.graph.util.Pair;
import eppic.model.ChainClusterDB;
import eppic.model.InterfaceClusterDB;
import eppic.model.InterfaceDB;
import eppic.model.PdbInfoDB;

/**
 * A class to wrap a PDB entry as extracted from the EPPIC database.
 * It essentially maps 1:1 to a PdbInfoDB, but we need to keep it separate of the model
 * because otherwise GWT and/or JPA would complain.
 * @author duarte_j
 *
 */
public class PdbInfo {

	//public static final double MIN_AREA_LATTICE_COMPARISON = 10;
	
	private PdbInfoDB pdbInfo;
	
	private HashMap<String,ChainCluster> chainIdLookup;
	
	public PdbInfo(PdbInfoDB pdbInfo) {
		this.pdbInfo = pdbInfo;
		this.chainIdLookup = getChainIdLookup();
		
	}
	
	public PdbInfoDB getPdbInfo() {
		return pdbInfo;
	}
	
	public ChainCluster getChainCluster(String pdbChainCode) {
		return chainIdLookup.get(pdbChainCode);
	}
	
	private HashMap<String,ChainCluster> getChainIdLookup() {
		HashMap<String,ChainCluster> lookup = new HashMap<String,ChainCluster>();
		for (ChainCluster cc:getChainClusters()) {
			// NOTE: this is not needed anymore since now the members also include the representative, leaving it here for backwards db compatibility
			lookup.put(cc.getChainCluster().getRepChain(), cc); 
			if (cc.getChainCluster().getMemberChains()!=null) {
				for (String chain:cc.getChainCluster().getMemberChains().split(",")) {
					lookup.put(chain,cc);
				}
			}
		}
		return lookup;
	}
	
	public List<ChainCluster> getChainClusters() {
		List<ChainCluster> list = new ArrayList<ChainCluster>();
		for (ChainClusterDB cc:pdbInfo.getChainClusters()) {
			list.add(new ChainCluster(cc));
		}
		return list;
	}
	
	/**
	 * Returns all Interface's for this PdbInfo by looping over all interface clusters
	 * @return
	 */
	public List<Interface> getInterfaces() {
		List<Interface> list = new ArrayList<Interface>();
		for (InterfaceClusterDB ic:pdbInfo.getInterfaceClusters()) {
			for (InterfaceDB interf:ic.getInterfaces()) {
				list.add(new Interface(interf, this));
			}
		}
		return list;
	}
	
	public int getNumInterfacesAboveArea(double area) {
		int count = 0;
		for (InterfaceClusterDB ic:pdbInfo.getInterfaceClusters()) {
			for (InterfaceDB interf:ic.getInterfaces()) {
				if (interf.getArea()>area) count++;	
			}			
		}
		return count;
	}
	
	public List<Interface> getInterfacesAboveArea(double area) {
		List<Interface> list = new ArrayList<Interface>();
		for (InterfaceClusterDB ic:pdbInfo.getInterfaceClusters()) {
			for (InterfaceDB interf:ic.getInterfaces()) {				
				if (interf.getArea()>area) list.add(new Interface(interf,this));
			}
		}
		return list;
	}
	
	public LatticeMatchMatrix calcLatticeOverlapMatrix(PdbInfo other, SeqClusterLevel seqClusterLevel, double minArea, boolean debug) 
			throws PairwiseSequenceAlignmentException {
	
		Map<Pair<String>,PairwiseSequenceAlignment> map = getAlignmentsPool(other, seqClusterLevel);
		
		if (debug) {
			for (Pair<String> pair:map.keySet()) {
				System.out.println(pair.getFirst()+"-"+pair.getSecond());
				map.get(pair).printAlignment();
			}
		}
		
		int interfCount1 = this.getNumInterfacesAboveArea(minArea);
		int interfCount2 = other.getNumInterfacesAboveArea(minArea);
	
		double[][] matrix = new double[interfCount1][interfCount2];

		for (Interface thisInterf : this.getInterfacesAboveArea(minArea)) {
			for (Interface otherInterf : other.getInterfacesAboveArea(minArea)) {
								
				double co = thisInterf.calcInterfaceOverlap(otherInterf, map, seqClusterLevel, debug);
				matrix[thisInterf.getInterface().getInterfaceId()-1][otherInterf.getInterface().getInterfaceId()-1] = co;
			}
		}	
		
		LatticeMatchMatrix lmm = new LatticeMatchMatrix(matrix);
		
		return lmm;
	}
	
	/**
	 * 
	 * @param other
	 * @param seqClusterLevel
	 * @return
	 * @throws PairwiseSequenceAlignmentException
	 */
	private Map<Pair<String>,PairwiseSequenceAlignment> getAlignmentsPool(PdbInfo other, SeqClusterLevel seqClusterLevel) 
			throws PairwiseSequenceAlignmentException {
		
		Map<Pair<String>,PairwiseSequenceAlignment> map = new HashMap<Pair<String>,PairwiseSequenceAlignment>();
		for (ChainCluster thisChainCluster:this.getChainClusters()) {
			
			if (thisChainCluster.getChainCluster().getPdbAlignedSeq()==null) continue;
			
			String thisSeq = thisChainCluster.getChainCluster().getPdbAlignedSeq().replace("-", "");
			for (ChainCluster otherChainCluster:other.getChainClusters()) {
				
				if (thisChainCluster.isSameSeqCluster(otherChainCluster, seqClusterLevel)) {
					
					if (otherChainCluster.getChainCluster().getPdbAlignedSeq()==null) continue;
					
					String otherSeq = otherChainCluster.getChainCluster().getPdbAlignedSeq().replace("-", "");
					PairwiseSequenceAlignment aln = new PairwiseSequenceAlignment(thisSeq, otherSeq, "first" , "second");
					Pair<String> pair = new Pair<String>(thisChainCluster.getChainCluster().getRepChain(),
														 otherChainCluster.getChainCluster().getRepChain());
					map.put(pair, aln);
				}
			}
		}
		return map;
	}
	
	/**
	 * Tells whether this and other PdbInfo have the same content at the given seqClusterLevel.
	 * Same content means: same number of chain clusters, each of this' chains having a 
	 * match (i.e. belonging to same sequence cluster within seqClusterLevel) in other's chains
	 * @param other
	 * @param seqClusterLevel true if same content, false if different content or if any of the chains
	 * in either entry is an engineered chain without a UniProt reference
	 * @return
	 */
	public boolean haveSameContent(PdbInfo other, SeqClusterLevel seqClusterLevel) {
		
		int numChainClusters1 = this.getPdbInfo().getChainClusters().size();
		int numChainClusters2 = other.getPdbInfo().getChainClusters().size();
		
		if (numChainClusters1!=numChainClusters2) return false;
		
		
		for (ChainCluster chainCluster1:this.getChainClusters()) {
			
			boolean match = false; 
			
			int seqClusterId1 = chainCluster1.getSeqClusterId(seqClusterLevel);
			
			if (seqClusterId1==-1) return false;
			
			for (ChainCluster chainCluster2:other.getChainClusters()) {
				int seqClusterId2 = chainCluster2.getSeqClusterId(seqClusterLevel);
				
				if (seqClusterId2 == -1) return false;
				
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
	
	public boolean equals(Object o) {
		if (!(o instanceof PdbInfo)) return false;
		
		PdbInfo other = (PdbInfo)o;
		return this.getPdbInfo().getPdbCode().equals(other.getPdbInfo().getPdbCode());
	}
	
	public int hashCode() {
		return this.getPdbInfo().getPdbCode().hashCode();
	}
}