package eppic.db;

import static eppic.db.Interface.*;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;

import org.biojava.nbio.core.alignment.template.AlignedSequence;
import org.biojava.nbio.core.alignment.template.Profile;
import org.biojava.nbio.core.sequence.ProteinSequence;
import org.biojava.nbio.core.sequence.compound.AminoAcidCompound;

import eppic.model.ContactDB;

/**
 * Compares multiple interfaces based on a shared chain
 * @author Spencer Bliven
 *
 */
public class MultipleInterfaceComparator {

	private List<DirectedInterface> interfaces;
	private Profile<ProteinSequence, AminoAcidCompound> alignment;
	private List<List<Integer>> contactIndices;

	/**
	 * 
	 * @param interfaces
	 * @param alignment
	 * @param contacts
	 */
	public MultipleInterfaceComparator(List<DirectedInterface> interfaces, Profile<ProteinSequence, AminoAcidCompound> alignment, List<List<ContactDB>> contacts ) {
		if(interfaces.size() != alignment.getSize() || interfaces.size() != contacts.size()) {
			throw new IllegalArgumentException("All arguments must be same length");
		}
		this.interfaces = interfaces;
		this.alignment = alignment;

		this.contactIndices = new ArrayList<>(interfaces.size());
		for(int i=0;i<interfaces.size();i++) {
			AlignedSequence<ProteinSequence, AminoAcidCompound> seq = alignment.getAlignedSequence(i+1);
			List<ContactDB> icontacts = contacts.get(i);
			SortedSet<Integer> indices = new TreeSet<>();
			
			int dir = interfaces.get(i).getDirection();
			for(ContactDB contact : contacts.get(i)) {
				int seqIndex;
				switch(dir) {
				case FIRST: seqIndex = contact.getFirstResNumber(); break;
				case SECOND: seqIndex = contact.getSecondResNumber(); break;
				default: throw new IllegalStateException("Unknown direction");
				}
				int alignIndex = seq.getAlignmentIndexAt(seqIndex);
				indices.add(alignIndex);
			}
			this.contactIndices.add(new ArrayList<>(indices));
		}
	}
	/**
	 * Calculates the fraction of contacts shared by the two interfaces, also
	 * known as the Sørensen–Dice index. That is,
	 * (# equivalent contacts)/(average number of contacts)
	 * 
	 * @param i Index of first interface
	 * @param j Index of second interface
	 * @return
	 */
	public double getOverlap(int i, int j) {
		List<Integer> indicesI = contactIndices.get(i);
		List<Integer> indicesJ = contactIndices.get(j);

		int common = getIntersectionSize(indicesI,indicesJ);
		return 2.*common / ( indicesI.size()+indicesJ.size() );
	}
	
	/**
	 * Calculate the Jaccard index for the contact residues of two interfaces.
	 * If the two interfaces have sets of contacting residues A and B, the
	 * Jaccard index is given by
	 * 
	 * |A intersection B|/|A union B|
	 * @param i
	 * @param j
	 * @return
	 */
	public double getJaccard(int i, int j) {
		List<Integer> indicesI = contactIndices.get(i);
		List<Integer> indicesJ = contactIndices.get(j);

		int common = getIntersectionSize(indicesI,indicesJ);
		return (double)common / ( indicesI.size()+indicesJ.size() - common );

	}
	/**
	 * Computes the size of the set intersection between two sorted lists
	 * @param a sorted list
	 * @param b sorted list
	 * @return number of elements in both lists
	 */
	private static <T extends Comparable<T>> int getIntersectionSize(List<T> a, List<T> b) {
		//Note these are sorted

		if(a.isEmpty() || b.isEmpty())
			return 0;

		int common = 0;
		
		Iterator<T> itA = a.iterator();
		Iterator<T> itB = b.iterator();

		
		// Run through both lists, counting matches
		T valA = itA.next();
		T valB = itB.next();
		while(true) {
			int comp = valA.compareTo(valB);
			if(comp == 0) {
				common++;
				if( !itA.hasNext() || !itB.hasNext())
					break;
				valA = itA.next();
				valB = itB.next();
			} else if(comp < 0) {
				// move forward the smaller value
				if(!itA.hasNext())
					break;
				valA = itA.next();
			} else {
				if(!itB.hasNext())
					break;
				valB = itB.next();
			}
		}
		return common;
	}
	
	public int size() {
		return interfaces.size();
	}
}
