package eppic.db;

import java.util.Map;

import owl.core.sequence.alignment.PairwiseSequenceAlignment;
import edu.uci.ics.jung.graph.util.Pair;
import eppic.model.InterfaceDB;

public class Interface {

	public static final int FIRST = 0;
	public static final int SECOND = 1;
	
	private InterfaceDB interf;
	private PdbInfo pdb;
	
	public Interface(InterfaceDB interf, PdbInfo pdb) {
		this.interf = interf;
		this.pdb = pdb;
				
	}
	
	public InterfaceDB getInterface() {
		return interf;
	}
	
	public PdbInfo getPdbInfo() {
		return pdb;
	}
	
	/**
	 * Tells whether this interface has the same chain content as the given one. Chain "content"
	 * is defined from the given sequence cluster level. 
	 * @param other
	 * @param seqClusterLevel
	 * @return true if and only if both chains match either in direct or reverse order, e.g.
	 * matches are A->B;B->A , A->A;A->A , A->B;A->B, non-matches are A->B;A->C , A->A;A->B, A->B;C->A
	 */
	public boolean isSameContent(Interface other, SeqClusterLevel seqClusterLevel) {
		
		if ( this.getChainCluster(FIRST).isSameSeqCluster(other.getChainCluster(FIRST), seqClusterLevel) &&
			 this.getChainCluster(SECOND).isSameSeqCluster(other.getChainCluster(SECOND), seqClusterLevel)  ) {
			
			return true;
		}
		
		if ( this.getChainCluster(FIRST).isSameSeqCluster(other.getChainCluster(SECOND), seqClusterLevel) &&
			 this.getChainCluster(SECOND).isSameSeqCluster(other.getChainCluster(FIRST), seqClusterLevel)  ) {
				
			return true;
		}
		
		return false;
	}
	
	public boolean isInvertedContent(Interface other, SeqClusterLevel seqClusterLevel) {
		if (!isSameContent(other,seqClusterLevel)) 
			throw new IllegalArgumentException("Interfaces to be checked for inversion don't have the same content");
		
		if (this.getChainCluster(FIRST).isSameSeqCluster(other.getChainCluster(FIRST), seqClusterLevel) ) {
			
			// both first chains are same, since it is guaranteed that both interfaces 
			// have the same content, then second chains will also coincide, no need for further checks
			// --> Order is not inverted:
			return false;
		}
		
		return true;
	}
	
	/**
	 * Tells whether interface is a homo interface (both chains same within seqCluterLevel given) 
	 * or hetero interface (both chains different within seqClusterLevel given)
	 * @param seqClusterLevel
	 * @return
	 */
	public boolean isHomoContent(SeqClusterLevel seqClusterLevel) {
		return (this.getChainCluster(FIRST).isSameSeqCluster(this.getChainCluster(SECOND), seqClusterLevel));
	}
	
	public ChainCluster getChainCluster(int molecId) {
		
		if (molecId==FIRST) 
			return this.getPdbInfo().getChainCluster(this.getInterface().getChain1());
		
		else if (molecId==SECOND) 
			return this.getPdbInfo().getChainCluster(this.getInterface().getChain2());
		
		return null;
	}
	
	public double calcInterfaceOverlap(Interface other, Map<Pair<String>,PairwiseSequenceAlignment> alnPool, SeqClusterLevel seqClusterLevel, boolean debug) {
		
		InterfaceComparator csComp = new InterfaceComparator(this,other,alnPool,seqClusterLevel);
		csComp.setDebug(debug);
		
		return csComp.calcOverlap();
		
	}
	
}
