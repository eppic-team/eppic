package eppic.db;

import java.util.Map;

import org.biojava.nbio.core.alignment.template.SequencePair;
import org.biojava.nbio.core.sequence.ProteinSequence;
import org.biojava.nbio.core.sequence.compound.AminoAcidCompound;
import org.biojava.nbio.structure.contact.Pair;


public class InterfaceComparator {

	private boolean debug;
	
	private Interface interf1;
	private Interface interf2;
	private ContactSet cs1;
	private ContactSet cs2;
	
	private boolean sameContent;
	//private boolean homoInterface;
	//private boolean invertedOrder;
	
	private Map<Pair<String>,SequencePair<ProteinSequence,AminoAcidCompound>> alignmentsPool;
	
	public InterfaceComparator(Interface interf1, Interface interf2, 
			Map<Pair<String>, SequencePair<ProteinSequence,AminoAcidCompound>> alignmentsPool, SeqClusterLevel seqClusterLevel) {
		
		this.interf1 = interf1;
		this.interf2 = interf2;
		this.alignmentsPool = alignmentsPool;
		
		this.sameContent = interf1.isSameContent(interf2, seqClusterLevel);

		if (this.sameContent) {

			//this.homoInterface = interf1.isHomoContent(seqClusterLevel);
			//this.invertedOrder = interf1.isInvertedContent(interf2, seqClusterLevel);

			initialiseContactSets();
		}
		
		// if not same content all the other calculations don't make much sense, in calcOverlap we return directly a 0
		
	}
	
	public void setDebug(boolean debug) {
		this.debug = debug;
	}
	
	private void initialiseContactSets() {
		
		String interf1FirstChain = interf1.getChainCluster(Interface.FIRST).getChainCluster().getRepChain();
		String interf1SecondChain = interf1.getChainCluster(Interface.SECOND).getChainCluster().getRepChain();
		String interf2FirstChain = interf2.getChainCluster(Interface.FIRST).getChainCluster().getRepChain();
		String interf2SecondChain = interf2.getChainCluster(Interface.SECOND).getChainCluster().getRepChain();

		// we get all the 4 possible alignments
		// 1st chain to 1st chain
		SequencePair<ProteinSequence,AminoAcidCompound> aln11 = alignmentsPool.get(new Pair<String>(interf1FirstChain,interf2FirstChain));
		// 2nd chain to 2nd chain
		SequencePair<ProteinSequence,AminoAcidCompound> aln22 = alignmentsPool.get(new Pair<String>(interf1SecondChain,interf2SecondChain));
		// 1st chain to 2nd chain
		SequencePair<ProteinSequence,AminoAcidCompound> aln12 = alignmentsPool.get(new Pair<String>(interf1FirstChain,interf2SecondChain));
		// 2nd chain to 1st chain
		SequencePair<ProteinSequence,AminoAcidCompound> aln21 = alignmentsPool.get(new Pair<String>(interf1SecondChain,interf2FirstChain));
		
				
		cs1 = new ContactSet(interf1.getInterface().getContacts(),null,null,null,null);
		cs2 = new ContactSet(interf2.getInterface().getContacts(),aln11, aln22, aln12, aln21);
		
		
	}
	
	public double calcOverlap() {
		
		if (! sameContent) {
			// not same content: overlap value is 0 
			if (debug) 
				System.out.println("different content: "+interf1.getInterface().getInterfaceId()+"-"+interf2.getInterface().getInterfaceId());
			
			return 0.0;
		}

		// At this point we know we have same content in both interfaces that we are comparing, but they 
		// can still be homo-interfaces or hetero-interfaces		
		// We simply calculate both direct and inverse overlaps to be sure that we get it right
		
		// we need to check both direct and inverse, because the contacts might be stored in opposite order
		double coDirect = calcOverlap(false);
		double coInverse = calcOverlap(true);

		if (debug && coInverse>coDirect) 
			System.out.println("inverted interface: "+interf1.getInterface().getInterfaceId()+"-"+interf2.getInterface().getInterfaceId()+" direct value: "+String.format("%5.3f",coDirect));

		return Math.max(coDirect, coInverse);

		

	}
	
	private double calcOverlap(boolean inverse) {
		
		int common = 0;
		for (Pair<SimpleResidue> contact:cs1.getDirectSet()) {
			if (cs2.contains(contact,inverse)) {
				common++;
			} 
		}
		return (2.0*common)/(cs1.size()+cs2.size());
	}
}
