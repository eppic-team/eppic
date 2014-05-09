package eppic.db;

import java.util.HashSet;
import java.util.List;

import owl.core.structure.AminoAcid;
import edu.uci.ics.jung.graph.util.Pair;
import eppic.model.ContactDB;

public class ContactSet {
	
	private HashSet<Pair<SimpleResidue>> set;  

	public ContactSet(List<ContactDB> contacts) {
		set = new HashSet<Pair<SimpleResidue>>();
		for (ContactDB contact:contacts) {
			set.add(pairFromContact(contact));
		}
	}
	
	public boolean contains(Pair<SimpleResidue> contact) {
		return set.contains(contact);
	}
	
	public static Pair<SimpleResidue> pairFromContact(ContactDB contact) {
		return new Pair<SimpleResidue>(
				new SimpleResidue(AminoAcid.getByThreeLetterCode(contact.getFirstResType()),contact.getFirstResNumber()),
				new SimpleResidue(AminoAcid.getByThreeLetterCode(contact.getSecondResType()),contact.getSecondResNumber()));
	}
	
	public double calcOverlap(ContactSet o) {
		ContactSet large = null;
		ContactSet small = null;
		if (this.set.size()>=o.set.size()) {
			large = this;
			small = o;
		} else {
			large = o;
			small = this;
		}
		int common = 0;
		for (Pair<SimpleResidue> contact:large.set) {
			if (small.contains(contact)) {
				common++;
			} 
		}
		return (2.0*common)/(this.set.size()+o.set.size());
	}
	
	public static void main(String[] args) {
		Pair<SimpleResidue> pair1 = new Pair<SimpleResidue>(new SimpleResidue(AminoAcid.ALA,1), new SimpleResidue(AminoAcid.ALA,2));
		Pair<SimpleResidue> pair2 = new Pair<SimpleResidue>(new SimpleResidue(AminoAcid.ALA,1), new SimpleResidue(AminoAcid.ALA,2));
		if (pair1.equals(pair2)) System.out.println("equals");
		else System.out.println("not equals");
	}
}
