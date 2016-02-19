package eppic.db;

import java.util.HashSet;
import java.util.List;

import org.biojava.nbio.core.alignment.template.SequencePair;
import org.biojava.nbio.core.sequence.ProteinSequence;
import org.biojava.nbio.core.sequence.compound.AminoAcidCompound;
import org.biojava.nbio.structure.contact.Pair;

import eppic.model.ContactDB;

/**
 * Represents a set of contacting residues for interface alignment. Contacts
 * are stored as a set of pairs of residue indices.
 */
public class ContactSet {
	
	private HashSet<Pair<SimpleResidue>> directSet;
	private HashSet<Pair<SimpleResidue>> inverseSet;

	private SequencePair<ProteinSequence,AminoAcidCompound> aln11;
	private SequencePair<ProteinSequence,AminoAcidCompound> aln22;
	private SequencePair<ProteinSequence,AminoAcidCompound> aln12;
	private SequencePair<ProteinSequence,AminoAcidCompound> aln21;
	
	/**
	 * Create a new contact set from a list of contacts.
	 * 
	 * If the alignments are null, the paired residue numbers are simply
	 * extracted from the input contacts.
	 * 
	 * If the alignments are not null, they should specify an alignment between
	 * some reference interface (the query in each alignment) and the interface
	 * from the contacts. Contacts are then mapped back to the residue indices
	 * of the query sequence.
	 * @param contacts List of contacting residues from the second interface
	 * @param aln11 Alignment between first chains of each interface (direct), or null
	 * @param aln22 Alignment between second chains of each interface (direct), or null
	 * @param aln12 Alignment between first chain of first interface and second of second interface (indirect), or null
	 * @param aln21 Alignment between second chain of first interface and first of second interface (indirect), or null
	 */
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
	
	/**
	 * Add a contact to the set. If alignments were specified upon creation,
	 * assumes the contact is from the second sequence in the alignment and maps
	 * the contact back onto indices in the first sequence. If the contacting
	 * residues are unaligned, adds a contact to or from index -1.
	 * @param contact
	 */
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
		boolean directalignment = aln11!=null && aln22!=null;
		boolean indirectalignment = aln12!=null && aln21!=null;
		if (directalignment) {
			
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
		}
		if( indirectalignment) {
			
			// inverse
			int resSerial1 = getMapping2To1(aln21, contact.getFirstResNumber());
			int resSerial2 = getMapping2To1(aln12, contact.getSecondResNumber());
			//TODO if they map to -1 that means they map to gap: what to do then?

			
			Pair<SimpleResidue> invertedPair = new Pair<SimpleResidue>(
					new SimpleResidue(resSerial2),
					new SimpleResidue(resSerial1));
			
			inverseSet.add(invertedPair);
			
		}
		if( directalignment || indirectalignment) {
			
			// first contact in comparison: we need no inverse set, and no mapping through alignment
			
			Pair<SimpleResidue> directPair = new Pair<SimpleResidue>(
					new SimpleResidue(contact.getFirstResNumber()),
					new SimpleResidue(contact.getSecondResNumber()));
			
			directSet.add(directPair);
		
		}
		
		
		
		
	}
	
	/**
	 * Given an alignment, convert an index in the second sequence to the
	 * equivalent position in the first sequence
	 * @param aln alignment
	 * @param resNumber 1-based index in the second sequence
	 * @return 1-based index of the equivalent position in the first sequence
	 */
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
