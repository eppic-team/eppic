package eppic.db.tools.helpers;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.biojava.nbio.alignment.NeedlemanWunsch;
import org.biojava.nbio.alignment.SimpleGapPenalty;
import org.biojava.nbio.alignment.template.GapPenalty;
import org.biojava.nbio.core.alignment.matrices.SubstitutionMatrixHelper;
import org.biojava.nbio.core.alignment.template.SequencePair;
import org.biojava.nbio.core.alignment.template.SubstitutionMatrix;
import org.biojava.nbio.core.exceptions.CompoundNotFoundException;
import org.biojava.nbio.core.sequence.ProteinSequence;
import org.biojava.nbio.core.sequence.compound.AminoAcidCompound;
import org.biojava.nbio.structure.contact.Pair;

import eppic.model.db.ChainClusterDB;
import eppic.model.db.InterfaceClusterDB;
import eppic.model.db.PdbInfoDB;

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
	
	public int getNumInterfaceClustersAboveArea(double area) {
		int count = 0;
		for (InterfaceClusterDB ic:pdbInfo.getInterfaceClusters()) {			
			if (ic.getAvgArea()>area) count++;							
		}
		return count;
	}
	
	public List<InterfaceCluster> getInterfaceClustersAboveArea(double area) {
		List<InterfaceCluster> list = new ArrayList<InterfaceCluster>();
		for (InterfaceClusterDB ic:pdbInfo.getInterfaceClusters()) {							
			if (ic.getAvgArea()>area) list.add(new InterfaceCluster(ic,this));			
		}
		return list;		
	}
	
	public LatticeMatchMatrix calcLatticeOverlapMatrix(PdbInfo other, SeqClusterLevel seqClusterLevel, double minArea, boolean debug) 
			 {
	
		// Maps from a pair of chain Ids to their alignment
		// Only includes chains from the same sequence cluster
		Map<Pair<String>,SequencePair<ProteinSequence,AminoAcidCompound>> map = getAlignmentsPool(other, seqClusterLevel);
		
		if (debug) {
			for (Pair<String> pair:map.keySet()) {
				System.out.println(pair.getFirst()+"-"+pair.getSecond());
				System.out.println(map.get(pair).toString(100));
			}
		}
		
		int interfCount1 = this.getNumInterfaceClustersAboveArea(minArea);
		int interfCount2 = other.getNumInterfaceClustersAboveArea(minArea);
	
		double[][] matrix = new double[interfCount1][interfCount2];

		for (InterfaceCluster thisInterfCluster : this.getInterfaceClustersAboveArea(minArea)) {
			for (InterfaceCluster otherInterfCluster : other.getInterfaceClustersAboveArea(minArea)) {
								
				double co = thisInterfCluster.getRepresentative().calcInterfaceOverlap(otherInterfCluster.getRepresentative(), map, seqClusterLevel, debug);
				matrix[thisInterfCluster.getInterfaceClusterDB().getClusterId()-1][otherInterfCluster.getInterfaceClusterDB().getClusterId()-1] = co;
			}
		}	
		
		LatticeMatchMatrix lmm = new LatticeMatchMatrix(matrix);
		
		return lmm;
	}
	
	/**
	 * Calculate alignments between all pairs of chains in two structures.
	 * 
	 * All representative chains are found for each structure. Chains from
	 * the same sequence cluster are aligned (those from different clusters
	 * are ignored). Returned is a map from the two chainIds to the aligned sequences.
	 * @param other Structure to compare to
	 * @param seqClusterLevel Minimum sequence identity to compare chains
	 * @return A map from the pair of chains to the alignment
	 */
	private Map<Pair<String>,SequencePair<ProteinSequence,AminoAcidCompound>> getAlignmentsPool(PdbInfo other, SeqClusterLevel seqClusterLevel) 
		 {
		
		Map<Pair<String>,SequencePair<ProteinSequence,AminoAcidCompound>> map = new HashMap<Pair<String>,SequencePair<ProteinSequence,AminoAcidCompound>>();
		for (ChainCluster thisChainCluster:this.getChainClusters()) {
			
			if (thisChainCluster.getChainCluster().getPdbAlignedSeq()==null) continue;
			
			String thisSeq = thisChainCluster.getChainCluster().getPdbAlignedSeq().replace("-", "");
			for (ChainCluster otherChainCluster:other.getChainClusters()) {
				
				if (thisChainCluster.isSameSeqCluster(otherChainCluster, seqClusterLevel)) {
					
					if (otherChainCluster.getChainCluster().getPdbAlignedSeq()==null) continue;
					
					String otherSeq = otherChainCluster.getChainCluster().getPdbAlignedSeq().replace("-", "");
					
					
					SubstitutionMatrix<AminoAcidCompound> matrix = SubstitutionMatrixHelper.getBlosum50();
					GapPenalty penalty = new SimpleGapPenalty((short)8,(short)1);

					// before move to Biojava, we had as tags of the sequences:  "first" and "second"
					ProteinSequence s1 = null; 
					ProteinSequence s2 = null; 
					try {
						s1 = new ProteinSequence(thisSeq);
						s2 = new ProteinSequence(otherSeq);
					} catch (CompoundNotFoundException e) {
						System.err.println("Non-protein characters in one of the sequences! "+e.getMessage());
						System.err.println("Sequences are: ");
						System.err.println(
								thisChainCluster.getChainCluster().getPdbCode() + 
								thisChainCluster.getChainCluster().getRepChain()+ ": "+thisSeq);
						System.err.println(
								otherChainCluster.getChainCluster().getPdbCode() + 
								otherChainCluster.getChainCluster().getRepChain()+ ": "+otherSeq);
						System.err.println("This should not happen. Exiting...");
						System.exit(1);
					}

					NeedlemanWunsch<ProteinSequence,AminoAcidCompound> nw = 
							new NeedlemanWunsch<ProteinSequence,AminoAcidCompound>(s1,s2, penalty, matrix);
					
					SequencePair<ProteinSequence,AminoAcidCompound> aln = nw.getPair(); 

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
