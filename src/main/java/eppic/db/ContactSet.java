package eppic.db;

import java.util.HashSet;

import owl.core.structure.AminoAcid;
import edu.uci.ics.jung.graph.util.Pair;

public class ContactSet {
	
	private HashSet<Pair<SimpleResidue>> directSet;
	private HashSet<Pair<SimpleResidue>> inverseSet;

	public ContactSet() {
		directSet = new HashSet<Pair<SimpleResidue>>();
		inverseSet = new HashSet<Pair<SimpleResidue>>();
		
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
	
	public void addContact(Pair<SimpleResidue> pair) {
		directSet.add(pair);
		inverseSet.add(new Pair<SimpleResidue>(pair.getSecond(),pair.getFirst()));
	}
	
	public static void main(String[] args) {
		Pair<SimpleResidue> pair1 = new Pair<SimpleResidue>(new SimpleResidue(AminoAcid.ALA,1), new SimpleResidue(AminoAcid.ALA,2));
		Pair<SimpleResidue> pair2 = new Pair<SimpleResidue>(new SimpleResidue(AminoAcid.ALA,1), new SimpleResidue(AminoAcid.ALA,2));
		if (pair1.equals(pair2)) System.out.println("equals");
		else System.out.println("not equals");
	}
}
