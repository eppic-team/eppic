package eppic.db.tools.helpers;

import eppic.model.db.ChainClusterDB;
import eppic.model.db.SeqClusterDB;

public class ChainCluster {

	private ChainClusterDB chainCluster;
	
	public ChainCluster(ChainClusterDB chainCluster) {
		this.chainCluster = chainCluster;
	}
	
	public ChainClusterDB getChainCluster() {
		return chainCluster;
	}
	
	/**
	 * Tells whether this and other ChainClusters are in same sequence cluster for the given seqClusterLevel
	 * @param other
	 * @param seqClusterLevel true if in same cluster, false if not or if no UniProt reference available for either chain
	 * @return
	 */
	public boolean isSameSeqCluster(ChainCluster other, SeqClusterLevel seqClusterLevel) {
		int thisSeqCluster = this.getSeqClusterId(seqClusterLevel);
		int otherSeqCluster = other.getSeqClusterId(seqClusterLevel);
		
		if (thisSeqCluster==-1 || otherSeqCluster==-1) return false;
		
		return (thisSeqCluster==otherSeqCluster);
	}
	
	public int getSeqClusterId(SeqClusterLevel seqClusterLevel) {
		SeqClusterDB seqCluster = chainCluster.getSeqCluster();
		
		if (seqCluster==null) return -1;
		
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
		case C40:
			seqClusterId = seqCluster.getC40();
			break;
		case C30:
			seqClusterId = seqCluster.getC30();
			break;			

		}
		return seqClusterId;
	}
}
