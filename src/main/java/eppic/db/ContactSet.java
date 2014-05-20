package eppic.db;

import java.util.HashSet;
import java.util.List;

import owl.core.sequence.alignment.PairwiseSequenceAlignment;
import owl.core.structure.AminoAcid;
import edu.uci.ics.jung.graph.util.Pair;
import eppic.model.ContactDB;

public class ContactSet {
	
	private HashSet<Pair<SimpleResidue>> directSet;
	private HashSet<Pair<SimpleResidue>> inverseSet;

	private PairwiseSequenceAlignment aln11;
	private PairwiseSequenceAlignment aln22;
	private PairwiseSequenceAlignment aln12;
	private PairwiseSequenceAlignment aln21;
	
	public ContactSet(List<ContactDB> contacts, 
			PairwiseSequenceAlignment aln11, PairwiseSequenceAlignment aln22,
			PairwiseSequenceAlignment aln12, PairwiseSequenceAlignment aln21 ) {
		
		this.directSet = new HashSet<Pair<SimpleResidue>>();
		this.inverseSet = new HashSet<Pair<SimpleResidue>>();
		
		this.aln11 = aln11;
		this.aln22 = aln22;
		this.aln12 = aln12;
		this.aln21 = aln21;
		
		for (ContactDB contact:contacts) {
			addContact(contact);
		}

	}
	
	public boolean contains(Pair<SimpleResidue> contact, boolean inverse) {
		if (!inverse)
			return directSet.contains(contact);
		else
			return inverseSet.contains(contact);
	}
	
	public int size() {
		return directSet.size();
	}
	
	public HashSet<Pair<SimpleResidue>> getDirectSet() {
		return directSet;
	}
	
	public void addContact(ContactDB contact) {
		
		// 2 cases:
		// a) alignments passed are null: that means we are in first contact of the comparison: 
		//    for those we take original coordinates
		// b) alignments passed are not null: only for the second contact in comparison we map back 
		//    to coordinates of chains in first contact via the alignment
		
		if (aln11!=null && aln22!=null && aln12!=null && aln21!=null) {
			
			// second contact in comparison: we need both direct and inverted contact and mapping via alignments 
			// in both direct and inverse cases
			
			// direct
			int resSerial1 = aln11.getMapping2To1(contact.getFirstResNumber()-1) + 1 ;
			int resSerial2 = aln22.getMapping2To1(contact.getSecondResNumber()-1) + 1 ;
			//TODO if they map to -1 that means they map to gap: what to do then?

			
			Pair<SimpleResidue> directPair = new Pair<SimpleResidue>(
					new SimpleResidue(AminoAcid.getByThreeLetterCode(contact.getFirstResType()),resSerial1),
					new SimpleResidue(AminoAcid.getByThreeLetterCode(contact.getSecondResType()),resSerial2));

			directSet.add(directPair);
			
			
			// inverse
			resSerial1 = aln21.getMapping2To1(contact.getFirstResNumber()-1) + 1 ;
			resSerial2 = aln12.getMapping2To1(contact.getSecondResNumber()-1) + 1 ;
			//TODO if they map to -1 that means they map to gap: what to do then?

			
			Pair<SimpleResidue> invertedPair = new Pair<SimpleResidue>(
					new SimpleResidue(AminoAcid.getByThreeLetterCode(contact.getSecondResType()),resSerial2),
					new SimpleResidue(AminoAcid.getByThreeLetterCode(contact.getFirstResType()),resSerial1));
			
			inverseSet.add(invertedPair);
			
		} else {
			
			// first contact in comparison: we need no inverse set, and no mapping through alignment
			
			Pair<SimpleResidue> directPair = new Pair<SimpleResidue>(
					new SimpleResidue(AminoAcid.getByThreeLetterCode(contact.getFirstResType()),contact.getFirstResNumber()),
					new SimpleResidue(AminoAcid.getByThreeLetterCode(contact.getSecondResType()),contact.getSecondResNumber()));
			
			directSet.add(directPair);
		
		}
		
		
		
		
	}
	
	public static void main(String[] args) {
		Pair<SimpleResidue> pair1 = new Pair<SimpleResidue>(new SimpleResidue(AminoAcid.ALA,1), new SimpleResidue(AminoAcid.ALA,2));
		Pair<SimpleResidue> pair2 = new Pair<SimpleResidue>(new SimpleResidue(AminoAcid.ALA,1), new SimpleResidue(AminoAcid.ALA,2));
		if (pair1.equals(pair2)) System.out.println("equals");
		else System.out.println("not equals");
	}
}
