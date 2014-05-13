package eppic.db;

import java.util.HashSet;
import java.util.List;

import owl.core.structure.AminoAcid;
import edu.uci.ics.jung.graph.util.Pair;
import eppic.model.ContactDB;

public class ContactSet {
	
	private HashSet<Pair<SimpleResidue>> directSet;
	private HashSet<Pair<SimpleResidue>> inverseSet;

	public ContactSet(List<ContactDB> contacts) {
		directSet = new HashSet<Pair<SimpleResidue>>();
		inverseSet = new HashSet<Pair<SimpleResidue>>();
		for (ContactDB contact:contacts) {
			directSet.add(pairFromContact(contact,false));
			inverseSet.add(pairFromContact(contact,true));
		}
	}
	
	public boolean contains(Pair<SimpleResidue> contact, boolean inverse) {
		if (!inverse)
			return directSet.contains(contact);
		else
			return inverseSet.contains(contact);
	}
	
	private static Pair<SimpleResidue> pairFromContact(ContactDB contact, boolean inverse) {
		if (!inverse)
			return new Pair<SimpleResidue>(
				new SimpleResidue(AminoAcid.getByThreeLetterCode(contact.getFirstResType()),contact.getFirstResNumber()),
				new SimpleResidue(AminoAcid.getByThreeLetterCode(contact.getSecondResType()),contact.getSecondResNumber()));
		
		else 
			return new Pair<SimpleResidue>(
				new SimpleResidue(AminoAcid.getByThreeLetterCode(contact.getSecondResType()),contact.getSecondResNumber()),
				new SimpleResidue(AminoAcid.getByThreeLetterCode(contact.getFirstResType()),contact.getFirstResNumber()));
			
	}
	
	public double calcOverlap(ContactSet o, boolean inverse) {
		ContactSet large = null;
		ContactSet small = null;
		if (this.directSet.size()>=o.directSet.size()) {
			large = this;
			small = o;
		} else {
			large = o;
			small = this;
		}
		int common = 0;
		for (Pair<SimpleResidue> contact:large.directSet) {
			if (small.contains(contact,inverse)) {
				common++;
			} 
		}
		return (2.0*common)/(this.directSet.size()+o.directSet.size());
	}
	
	public static void main(String[] args) {
		Pair<SimpleResidue> pair1 = new Pair<SimpleResidue>(new SimpleResidue(AminoAcid.ALA,1), new SimpleResidue(AminoAcid.ALA,2));
		Pair<SimpleResidue> pair2 = new Pair<SimpleResidue>(new SimpleResidue(AminoAcid.ALA,1), new SimpleResidue(AminoAcid.ALA,2));
		if (pair1.equals(pair2)) System.out.println("equals");
		else System.out.println("not equals");
	}
}
