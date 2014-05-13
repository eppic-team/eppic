package eppic.db;

import eppic.model.ChainClusterDB;
import eppic.model.SeqClusterDB;

public class ChainCluster {

	private ChainClusterDB chainCluster;
	
	public ChainCluster(ChainClusterDB chainCluster) {
		this.chainCluster = chainCluster;
	}
	
	public ChainClusterDB getChainCluster() {
		return chainCluster;
	}
	
	public boolean isSameSeqCluster(ChainCluster other, SeqClusterLevel seqClusterLevel) {
		int thisSeqCluster = this.getSeqClusterId(seqClusterLevel);
		int otherSeqCluster = other.getSeqClusterId(seqClusterLevel);
		
		return (thisSeqCluster==otherSeqCluster);
	}
	
	public int getSeqClusterId(SeqClusterLevel seqClusterLevel) {
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
