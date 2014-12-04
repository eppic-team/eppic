package eppic.commons.sequence;

import eppic.commons.util.Interval;
import eppic.commons.util.IntervalSet;

/**
 * A PDB chain as a subfeature of a Uniprot entry. The mapping is taken from the SIFTS resource.
 * See {@link http://www.ebi.ac.uk/msd/sifts}
 */
public class SiftsFeature {

	/*--------------------------- member variables --------------------------*/
	
	private String pdbCode;						// pdb chain which is mapped to this Uniprot entry
	private String pdbChainCode;				
	private IntervalSet cifPosition;			// position in mmCIF sequence
	private IntervalSet uniPosition;			// position in uniprot sequence
	private String pdbBeg;						// beginning residue in pdb-file numbering (may contain insertion codes)
	private String pdbEnd;						// ending residue in pdb-file numbering (may contain insertion codes)
	private String uniprotId;					// this entry's Uniprot ID (from SIFTS mapping)
	private String description;					// pdb+chain code

	/*----------------------------- constructors ----------------------------*/
	
	public SiftsFeature(String pdbCode, String pdbChainCode, String uniprotId, int cifBeg, int cifEnd, String pdbBeg, String pdbEnd, int uniBeg, int uniEnd) {
		this.cifPosition = new IntervalSet();
		this.cifPosition.add(new Interval(cifBeg, cifEnd));
		this.pdbBeg = pdbBeg;
		this.pdbEnd = pdbEnd;
		this.uniPosition = new IntervalSet();
		this.uniPosition.add(new Interval(uniBeg, uniEnd));
		this.pdbCode = pdbCode;
		this.pdbChainCode = pdbChainCode;
		this.uniprotId = uniprotId;
		this.description = pdbCode+pdbChainCode;
	}

	/*-------------------------- implemented methods ------------------------*/
	
	public String getDescription() {
		return this.description;	// pdb code + chain code
	}

	public IntervalSet getIntervalSet() {
		return uniPosition;
	}



	/*---------------------------- public methods ---------------------------*/
	
	/**
	 * @return the Uniprot ID
	 */
	public String getUniprotId() {
		return uniprotId;
	}

	/**
	 * @return the pdb code
	 */
	public String getPdbCode() {
		return pdbCode;
	}
	
	/**
	 * @return the pdb chain code
	 */
	public String getPdbChainCode() {
		return pdbChainCode;
	}
	
	/**
	 * @return the position in the Uniprot sequence
	 */
	public IntervalSet getUniprotIntervalSet() {
		return uniPosition;
	}
	
	/**
	 * @return the position in the mmCIF sequence
	 */
	public IntervalSet getCifIntervalSet() {
		return cifPosition;
	}

	/**
	 * @return the beginning residue in pdb-file numbering (may contain insertion codes)
	 */
	public String getPdbBeg() {
		return pdbBeg;
	}

	/**
	 * @return the ending residue in pdb-file numbering (may contain insertion codes)
	 */
	public String getPdbEnd() {
		return pdbEnd;
	}
	
	/**
	 * @return a string representation of this feature (position, pdb code, pdb positions)
	 */
	public String toString() {
		return uniPosition.first().beg + "-" + uniPosition.first().end + " " + this.description + "(cif:" + cifPosition + ", pdb:" + pdbBeg + "-" + pdbEnd + ")";
	}
}
