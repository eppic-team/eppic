package eppic.commons.sequence;

import eppic.commons.util.Interval;

import java.util.*;

/**
 * A PDB chain as a subfeature of a Uniprot entry. The mapping is taken from the SIFTS resource.
 * See http://www.ebi.ac.uk/msd/sifts
 */
public class SiftsFeature {

	/*--------------------------- member variables --------------------------*/
	
	private String pdbCode;						// pdb id
	private String pdbChainCode;				// chain id: Chain#getName() in BioJava
	private List<Interval> cifPosition;			// position in mmCIF sequence (SEQRES)
	private List<Interval> uniPosition;			// position in uniprot sequence
	private List<String> uniprotIds;			// the uniprot ids for each of the intervals

	/*----------------------------- constructors ----------------------------*/
	
	public SiftsFeature(String pdbCode, String pdbChainCode) {
		this.cifPosition = new ArrayList<>();
		this.uniPosition = new ArrayList<>();
		this.uniprotIds = new ArrayList<>();
		this.pdbCode = pdbCode;
		this.pdbChainCode = pdbChainCode;
	}

	public void addSegment(String uniprotId, int cifBeg, int cifEnd, int uniBeg, int uniEnd) {
		this.cifPosition.add(new Interval(cifBeg, cifEnd));
		this.uniPosition.add(new Interval(uniBeg, uniEnd));
		this.uniprotIds.add(uniprotId);
	}

	/**
	 * @return the Uniprot IDs
	 */
	public List<String> getUniprotIds() {
		return uniprotIds;
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
	public List<Interval> getUniprotIntervalSet() {
		return uniPosition;
	}
	
	/**
	 * @return the position in the mmCIF sequence
	 */
	public List<Interval> getCifIntervalSet() {
		return cifPosition;
	}

	/**
	 * Get the total length that the given UniProt id (possibly in multiple segments)
	 * covers from this SiftsFeature,
	 * i.e. the total UniProt length that is aligned to the PDB chain sequence
	 * @param uniProtId the uniprot id
	 * @return the length of the coverage or 0 if no such uniprot id exist in this feature
	 */
	public int getUniProtLengthCoverage(String uniProtId) {
		int count = 0;
		for (int i = 0; i<uniprotIds.size(); i++) {
			if (uniprotIds.get(i).equals(uniProtId)) {
				count += uniPosition.get(i).getLength();
			}
		}
		return count;
	}

	/**
	 * Get the total length that the UniProt mapping covers from the PDB chain sequence side
	 * (possibly in multiple segments and possibly from multiple UniProt ids, in fusion cases).
	 * @return the total length of coverage onf the PDB chain sequence
	 */
	public int getPdbLengthCoverage() {
		int count = 0;
		for (Interval interval : cifPosition) {
			count += interval.getLength();
		}
		return count;
	}

	/**
	 * Returns true if any of the UniProt intervals contains a negative or 0 coordinate (invalid
	 * mapping that would be a bug in SIFTS).
	 * @return true if this feature contain some negative or 0 uniprot coordinates, false otherwise
	 */
	public boolean hasNegatives() {
		for (Interval interv : uniPosition) {
			if (interv.beg<=0 || interv.end<=0)
				return true;
		}
		return false;
	}

	/**
	 * Returns true if at least one of the UniProt intervals has 0 or negative length (end before begin).
	 * @return true if at least one interval has 0 or negative length
	 */
	public boolean hasNegativeLength() {
		for (Interval interv : uniPosition) {
			if (interv.getLength()<=0)
				return true;
		}
		return false;
	}

	/**
	 * Returns true if there is more than 1 unique UniProt ids in this feature, i.e.
	 * if the PDB chain is chimeric.
	 * @return
	 */
	public boolean isChimeric() {
		return getUniqueUniprotIds().size()>1;
	}

	/**
	 * Get the UniProt id that has the longest total coverage (summing up coverage over all segments) value.
	 * @return
	 */
	public String getLargestTotalCoverageUniProtId() {
		List<UniProtCoverage> coverages = new ArrayList<>();
		for (String uniProtId : getUniqueUniprotIds()) {
			UniProtCoverage cov = new UniProtCoverage(uniProtId, getUniProtLengthCoverage(uniProtId));
			coverages.add(cov);
		}
		coverages.sort(Comparator.comparingInt(o -> o.coverage));
		return coverages.get(coverages.size()-1).uniProtId;
	}

	/**
	 * Get the index corresponding to the UniProt id that has the longest coverage value for 1 segment.
	 * @return
	 */
	public int getLargestSegmentCoverageIndex() {
		List<UniProtCoverage> coverages = new ArrayList<>();
		for (int i=0; i<uniprotIds.size(); i++) {
			UniProtCoverage cov = new UniProtCoverage(uniprotIds.get(i), uniPosition.get(i).getLength());
			coverages.add(cov);
		}
		coverages.sort(Comparator.comparingInt(o -> o.coverage));
		return uniprotIds.indexOf(coverages.get(coverages.size()-1).uniProtId);
	}

	public Set<String> getUniqueUniprotIds() {
		return new HashSet<>(uniprotIds);
	}

	/**
	 * @return a string representation of this feature (position, pdb code, pdb positions)
	 */
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append(pdbCode).append(pdbChainCode).append(" ");
		for (int i =0 ; i<uniPosition.size(); i++) {
			Interval uniInterv = uniPosition.get(i);
			Interval pdbInterv = cifPosition.get(i);
			sb.append(uniprotIds.get(i)).append(": ");
			sb.append("up ").append(uniInterv.beg).append("-").append(uniInterv.end);
			sb.append(", pdb ").append(pdbInterv.beg).append("-").append(pdbInterv.end);
		}
		return sb.toString();
	}

	private class UniProtCoverage {
		String uniProtId;
		int coverage;
		UniProtCoverage(String uniProtId, int coverage) {
			this.uniProtId = uniProtId;
			this.coverage = coverage;
		}
	}
}
