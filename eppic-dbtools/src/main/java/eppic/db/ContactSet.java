package eppic.db;

import java.util.HashSet;
import java.util.List;

import org.biojava.nbio.core.alignment.template.SequencePair;
import org.biojava.nbio.core.sequence.ProteinSequence;
import org.biojava.nbio.core.sequence.compound.AminoAcidCompound;
import org.biojava.nbio.structure.contact.Pair;

import eppic.model.ContactDB;

public class ContactSet {
	
	private HashSet<Pair<SimpleResidue>> directSet;
	private HashSet<Pair<SimpleResidue>> inverseSet;

	private SequencePair<ProteinSequence,AminoAcidCompound> aln11;
	private SequencePair<ProteinSequence,AminoAcidCompound> aln22;
	private SequencePair<ProteinSequence,AminoAcidCompound> aln12;
	private SequencePair<ProteinSequence,AminoAcidCompound> aln21;
	
	public ContactSet(List<ContactDB> contacts, 
			SequencePair<ProteinSequence,AminoAcidCompound> aln11, SequencePair<ProteinSequence,AminoAcidCompound> aln22,
			SequencePair<ProteinSequence,AminoAcidCompound> aln12, SequencePair<ProteinSequence,AminoAcidCompound> aln21 ) {
		
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
		
		// first we check if the contact is with a hetatom residue not part of the polymer (they have -1 as res number)
		// we don't want those at all in our contact set
		// e.g. 2f9q (interface 6, B+D), contact between SO4(-1) and GLU(74)
		if (contact.getFirstResNumber()==-1 || contact.getSecondResNumber()==-1) 
			return;
		
		// 2 cases:
		// a) alignments passed are null: that means we are in first contact of the comparison: 
		//    for those we take original coordinates
		// b) alignments passed are not null: only for the second contact in comparison we map back 
		//    to coordinates of chains in first contact via the alignment
		
		if (aln11!=null && aln22!=null && aln12!=null && aln21!=null) {
			
			// second contact in comparison: we need both direct and inverted contact and mapping via alignments 
			// in both direct and inverse cases
			
			// direct			
			int resSerial1 = getMapping2To1(aln11, contact.getFirstResNumber());
			int resSerial2 = getMapping2To1(aln22, contact.getSecondResNumber());
			//TODO if they map to -1 that means they map to gap: what to do then?

			
			Pair<SimpleResidue> directPair = new Pair<SimpleResidue>(
					new SimpleResidue(resSerial1),
					new SimpleResidue(resSerial2));

			directSet.add(directPair);
			
			
			// inverse
			resSerial1 = getMapping2To1(aln21, contact.getFirstResNumber());
			resSerial2 = getMapping2To1(aln12, contact.getSecondResNumber());
			//TODO if they map to -1 that means they map to gap: what to do then?

			
			Pair<SimpleResidue> invertedPair = new Pair<SimpleResidue>(
					new SimpleResidue(resSerial2),
					new SimpleResidue(resSerial1));
			
			inverseSet.add(invertedPair);
			
		} else {
			
			// first contact in comparison: we need no inverse set, and no mapping through alignment
			
			Pair<SimpleResidue> directPair = new Pair<SimpleResidue>(
					new SimpleResidue(contact.getFirstResNumber()),
					new SimpleResidue(contact.getSecondResNumber()));
			
			directSet.add(directPair);
		
		}
		
		
		
		
	}
	
	private static int getMapping2To1(SequencePair<ProteinSequence, AminoAcidCompound> aln, int resNumber) {
		
		int alnIdx = aln.getTarget().getAlignmentIndexAt(resNumber);
		if (aln.hasGap(alnIdx)) {
			return -1;
		} else {
			return aln.getIndexInQueryAt(alnIdx);
		}

	}
	
	public static void main(String[] args) {
		Pair<SimpleResidue> pair1 = new Pair<SimpleResidue>(new SimpleResidue(1), new SimpleResidue(2));
		Pair<SimpleResidue> pair2 = new Pair<SimpleResidue>(new SimpleResidue(1), new SimpleResidue(2));
		if (pair1.equals(pair2)) System.out.println("equals");
		else System.out.println("not equals");
	}
}
