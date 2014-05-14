package eppic.db;

import java.util.Map;

import edu.uci.ics.jung.graph.util.Pair;
import eppic.model.ContactDB;
import owl.core.sequence.alignment.PairwiseSequenceAlignment;
import owl.core.structure.AminoAcid;

public class InterfaceComparator {

	private Interface interf1;
	private Interface interf2;
	private ContactSet cs1;
	private ContactSet cs2;
	
	private boolean sameContent;
	private boolean homoInterface;
	private boolean invertedOrder;
	
	private Map<Pair<String>,PairwiseSequenceAlignment> alignmentsPool;
	
	public InterfaceComparator(Interface interf1, Interface interf2, 
			Map<Pair<String>, PairwiseSequenceAlignment> alignmentsPool, SeqClusterLevel seqClusterLevel) {
		
		this.interf1 = interf1;
		this.interf2 = interf2;
		this.alignmentsPool = alignmentsPool;
		
		this.sameContent = interf1.isSameContent(interf2, seqClusterLevel);

		if (this.sameContent) {

			this.homoInterface = interf1.isHomoContent(seqClusterLevel);
			this.invertedOrder = interf1.isInvertedContent(interf2, seqClusterLevel);

			initialiseContactSets();
		}
		
		// if not same content all the other calculations don't make much sense, in calcOverlap we return directly a 0
		
	}
	
	private void initialiseContactSets() {
		
		String interf1FirstChain = interf1.getChainCluster(Interface.FIRST).getChainCluster().getRepChain();
		String interf1SecondChain = interf1.getChainCluster(Interface.SECOND).getChainCluster().getRepChain();
		String interf2FirstChain = interf2.getChainCluster(Interface.FIRST).getChainCluster().getRepChain();
		String interf2SecondChain = interf2.getChainCluster(Interface.SECOND).getChainCluster().getRepChain();

		PairwiseSequenceAlignment aln1 = null;
		PairwiseSequenceAlignment aln2 = null;
				
		if (homoInterface) {
			aln1 = alignmentsPool.get(new Pair<String>(interf1FirstChain,interf2FirstChain));
			aln2 = aln1;
		} else {
			if (invertedOrder) {
				// remember: the alignments are for chains of interf2 to be mapped onto 1, that's why they have to be inverted
				aln2 = alignmentsPool.get(new Pair<String>(interf1FirstChain,interf2SecondChain));
				aln1 = alignmentsPool.get(new Pair<String>(interf1SecondChain,interf2FirstChain));				
			} else {
				aln1 = alignmentsPool.get(new Pair<String>(interf1FirstChain,interf2FirstChain));
				aln2 = alignmentsPool.get(new Pair<String>(interf1SecondChain,interf2SecondChain));
			}
		}
		
		if (interf1.getInterface().getInterfaceId()==1 && interf2.getInterface().getInterfaceId()==1) {
			if (aln1!=null) aln1.printAlignment();
			if (aln2!=null) aln2.printAlignment();
		}
		
		cs1 = new ContactSet();
		cs2 = new ContactSet();
		
		for (ContactDB contact:interf1.getInterface().getContacts()) {
			cs1.addContact(pairFromContact(contact, null, null));
		}
		for (ContactDB contact:interf2.getInterface().getContacts()) {
			cs2.addContact(pairFromContact(contact, aln1, aln2));
		}
		
	}
	
	private Pair<SimpleResidue> pairFromContact(ContactDB contact, PairwiseSequenceAlignment aln1, PairwiseSequenceAlignment aln2) {
		
		//TODO check whether we need to invert or not, and whether addContact above is correct (it adds both directed and inverted)
		
		int resSerial1 = contact.getFirstResNumber();
		int resSerial2 = contact.getSecondResNumber();
		
		if (aln1!=null && aln2!=null) {
			resSerial1 = aln1.getMapping2To1(resSerial1-1) + 1 ;
			resSerial2 = aln2.getMapping2To1(resSerial2-1) + 1 ;
			//TODO if they map to -1 that means they map to gap: what to do then?
		}
		
		return new Pair<SimpleResidue>(
				new SimpleResidue(AminoAcid.getByThreeLetterCode(contact.getFirstResType()),resSerial1),
				new SimpleResidue(AminoAcid.getByThreeLetterCode(contact.getSecondResType()),resSerial2));
	}
	
	public double calcOverlap() {
		
		if (! sameContent) {
			// not same content: overlap value is 0 
			System.out.println("different content: "+interf1.getInterface().getInterfaceId()+"-"+interf2.getInterface().getInterfaceId());
			return 0.0;
		}

		// At this point we know we have same content in both interfaces that we are comparing, but they 
		// can still be homo-interfaces or hetero-interfaces		
		// We then branch here on the 2 cases: homo-interface or hetero-interface		

		
		if (homoInterface) {
			
			// in this case we need to check both direct and inverse, because the contacts might be stored in opposite order
			double coDirect = calcOverlap(false);
			double coInverse = calcOverlap(true);

			if (coInverse>coDirect) 
				System.out.println("inverse homo-interface: "+interf1.getInterface().getInterfaceId()+"-"+interf2.getInterface().getInterfaceId()+" direct value: "+String.format("%5.3f",coDirect));
			
			return Math.max(coDirect, coInverse);
			
		} else {
			
			// we check if we need to reverse the order of chains in the comparison

			if (invertedOrder) 
				System.out.println("inverse hetero-interface: "+interf1.getInterface().getInterfaceId()+"-"+interf2.getInterface().getInterfaceId());
			else 
				System.out.println("direct hetero-interface: "+interf1.getInterface().getInterfaceId()+"-"+interf2.getInterface().getInterfaceId());
			
			return calcOverlap(invertedOrder);
		}

	}
	
	private double calcOverlap(boolean inverse) {
		ContactSet large = null;
		ContactSet small = null;
		if (cs1.size()>=cs2.size()) {
			large = cs1;
			small = cs2;
		} else {
			large = cs2;
			small = cs1;
		}
		int common = 0;
		for (Pair<SimpleResidue> contact:large.getDirectSet()) {
			if (small.contains(contact,inverse)) {
				common++;
			} 
		}
		return (2.0*common)/(cs1.size()+cs2.size());
	}
}
