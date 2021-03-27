package eppic;

import java.io.Serializable;

import eppic.commons.blast.BlastHsp;
import eppic.commons.sequence.UnirefEntry;


/**
 * Class to encapsulate a blast hit representing a homolog to a certain
 * sequence. 
 * It stores data from the blast hit and the UniRef entry (can be UniProt or UniParc)
 * <p>
 * Note: the {@link HomologList} class removes elements from its Homolog lists by using references,
 * that relies on having no equals() implemented here. If we implement an equals() method we should
 * double check that things still work properly in {@link HomologList}.
 *  
 * @see HomologList
 * 
 * @author Jose Duarte
 *
 */
public class Homolog implements Serializable {
	
	
	private static final long serialVersionUID = 1L;

	private BlastHsp blastHsp;
	private UnirefEntry unirefEntry;
	
	/**
	 * Constructs a new UniRef Homolog
	 * @param blastHsp
	 * @param unirefEntry
	 */
	public Homolog(BlastHsp blastHsp, UnirefEntry unirefEntry) {
		this.blastHsp = blastHsp;
		this.unirefEntry = unirefEntry;
	}
	
	public BlastHsp getBlastHsp() {
		return blastHsp;
	}

	public void setBlastHsp(BlastHsp blastHsp) {
		this.blastHsp = blastHsp;
	}

	public UnirefEntry getUnirefEntry() {
		return this.unirefEntry;
	}
	
	/**
	 * Returns the UniProt identifier if the UniRef entry corresponds to a UniProt or
	 * else it returns the UniParc identifier 
	 * @return
	 */
	public String getUniId() {
		return unirefEntry.getUniId();
	}

	public double getPercentIdentity() {
		return this.blastHsp.getPercentIdentity();
	}
	
	public double getQueryCoverage() {
		return this.blastHsp.getQueryCoverage();
	}
	
	/**
	 * Returns the long sequence tag i.e. the original Uniprot long identifier (e.g. tr|Q6V461|Q6V461_AERHY)
	 * (without the comment field)
	 * @return
	 */
	public String getLongSequenceTag() {
		return this.blastHsp.getParent().getSubjectId();
	}
	
	public String getSequence() {
		return unirefEntry.getSequence();
	}
	
	/**
	 * Returns true if this Homolog is a Uniprot entry, false if a Uniparc
	 * @return
	 */
	public boolean isUniprot() {
		return unirefEntry.isUniprot();
	}

	/**
	 * Returns an identifier that uniquely identifies the homolog, composed of 2 parts:
	 * the UniProt/UniParc identifier and the match (HSP) start and end, e.g. P12345_20-30
	 * @return
	 */
	public String getIdentifier() {
		return getUnirefEntry().getUniId()+"_"+
				getBlastHsp().getSubjectStart()+"-"+getBlastHsp().getSubjectEnd();
	}
	
	@Override
	public String toString() {
		return getUniId();
	}
}
