package eppic.db;

import eppic.model.InterfaceDB;

public class Interface {

	public static final int FIRST = 0;
	public static final int SECOND = 1;
	
	private InterfaceDB interf;
	private PdbInfo pdb;
	
	private ContactSet contactSet;
	
	
	public Interface(InterfaceDB interf, PdbInfo pdb) {
		this.interf = interf;
		this.pdb = pdb;
		
		this.contactSet = new ContactSet(this.interf.getContacts());
	}
	
	public InterfaceDB getInterface() {
		return interf;
	}
	
	public PdbInfo getPdbInfo() {
		return pdb;
	}
	
	public ContactSet getContactSet() {
		return contactSet;
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
	
	public double calcInterfaceOverlap(Interface other, SeqClusterLevel seqClusterLevel) {
		
		if (! this.isSameContent(other, seqClusterLevel)) {
			// not same content: overlap value is 0 
			System.out.println("different content: "+this.getInterface().getInterfaceId()+"-"+other.getInterface().getInterfaceId());
			return 0.0;
		}

		// now we know we have same content in both interfaces
		
		// we then branch here on the 2 cases: homo-interface or hetero-interface
		
		boolean homoInterface = this.isHomoContent(seqClusterLevel);		
		
		if (homoInterface) {

			// in this case we need to check both direct and inverse, because the contacts might be stored in opposite order
			double coDirect = this.contactSet.calcOverlap(other.getContactSet(), false);
			double coInverse = this.contactSet.calcOverlap(other.getContactSet(), true);

			if (coInverse>coDirect) 
				System.out.println("inverse homo-interface: "+this.getInterface().getInterfaceId()+"-"+other.getInterface().getInterfaceId()+" direct value: "+String.format("%5.3f",coDirect));
			
			return Math.max(coDirect, coInverse);
			
		} else {		
			// we check if we need to reverse the order of chains in the comparison
			boolean inverse = this.isInvertedContent(other, seqClusterLevel);				

			if (inverse) 
				System.out.println("inverse hetero-interface: "+this.getInterface().getInterfaceId()+"-"+other.getInterface().getInterfaceId());
			
			return this.contactSet.calcOverlap(other.getContactSet(), inverse);
		}
	}
	
}
