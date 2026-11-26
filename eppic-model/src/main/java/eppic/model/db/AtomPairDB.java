package eppic.model.db;

import java.io.Serializable;

/**
 * A pair of atoms across an interface, used in structured warnings (clashes, close interactions, disulfides).
 *
 * @since 3.5.7
 */
public class AtomPairDB implements Serializable {

	private static final long serialVersionUID = 1L;

	/**
	 * The residue of the first atom, belonging to the first side of the interface
	 */
	private ResidueRefDB firstResidue;
	/**
	 * The residue of the second atom, belonging to the second side of the interface
	 */
	private ResidueRefDB secondResidue;

	private String firstAtom;
	private String secondAtom;

	/**
	 * Distance between the atoms in Angstroms
	 */
	private double distance;

	public AtomPairDB() {

	}

	public ResidueRefDB getFirstResidue() {
		return firstResidue;
	}

	public void setFirstResidue(ResidueRefDB firstResidue) {
		this.firstResidue = firstResidue;
	}

	public ResidueRefDB getSecondResidue() {
		return secondResidue;
	}

	public void setSecondResidue(ResidueRefDB secondResidue) {
		this.secondResidue = secondResidue;
	}

	public String getFirstAtom() {
		return firstAtom;
	}

	public void setFirstAtom(String firstAtom) {
		this.firstAtom = firstAtom;
	}

	public String getSecondAtom() {
		return secondAtom;
	}

	public void setSecondAtom(String secondAtom) {
		this.secondAtom = secondAtom;
	}

	public double getDistance() {
		return distance;
	}

	public void setDistance(double distance) {
		this.distance = distance;
	}
}
