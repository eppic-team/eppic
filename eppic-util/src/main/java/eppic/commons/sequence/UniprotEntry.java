package eppic.commons.sequence;

import java.io.Serializable;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * A uniprot entry.
 * The class stores the sequence and taxonomy data 
 *  
 * @author duarte_j
 *
 */
public class UniprotEntry implements Serializable {
	
	private static final long serialVersionUID = 1L;

	private static final Logger LOGGER = LoggerFactory.getLogger(UniprotEntry.class);
		
	private String uniId;
	private Sequence uniprotSeq;
	private int taxId;
	private List<String> taxons; // the taxonomy as returned by UniprotEntry.getTaxonomy(). First value is the most general (kingdom), last is the most specific (species).
	private String geneEncodingOrganelle; // the organelle where this gene is encoded (important for genetic code): if gene encoded in nucleus this is null
	

	
	public UniprotEntry(String uniprotId){
		this.uniId = uniprotId;
	}
	
	/**
	 * @return the uniId
	 */
	public String getUniId() {
		return uniId;
	}
	
	/**
	 * @param uniId the uniId to set
	 */
	public void setUniId(String uniId) {
		this.uniId = uniId;
	}

	/**
	 * @return the uniprotSeq
	 */
	public Sequence getUniprotSeq() {
		return uniprotSeq;
	}

	/**
	 * @param uniprotSeq the uniprotSeq to set
	 */
	public void setUniprotSeq(Sequence uniprotSeq) {
		this.uniprotSeq = uniprotSeq;
	}

	/**
	 * @return the taxId
	 */
	public int getTaxId() {
		return taxId;
	}

	/**
	 * @param taxId the taxId to set
	 */
	public void setTaxId(int taxId) {
		this.taxId = taxId;
	}
	
	/**
	 * The taxonomy as returned by UniprotEntry.getTaxonomy(). 
	 * First value is the most general (domain of life), last is the most specific (species).
	 * @return
	 */
	public List<String> getTaxons() {
		return taxons;
	}
	
	/**
	 * Returns the domain of life (what used to be kingdom) for this entry 
	 * @return
	 */
	public String getFirstTaxon() {
		return this.taxons.get(0);
	}
	
	/**
	 * Returns the most specific taxonomy annotation (species) for this entry
	 * @return
	 */
	public String getLastTaxon() {
		return this.taxons.get(this.taxons.size()-1);
	}
	
	public void setTaxons(List<String> taxons) {
		this.taxons = taxons;
	}

	/**
	 * @return the geneEncodingOrganelle
	 */
	public String getGeneEncodingOrganelle() {
		return geneEncodingOrganelle;
	}

	/**
	 * @param geneEncodingOrganelle the geneEncodingOrganelle to set
	 */
	public void setGeneEncodingOrganelle(String geneEncodingOrganelle) {
		this.geneEncodingOrganelle = geneEncodingOrganelle;
	}

	/**
	 * Return the sequence length of this uniprot entry.
	 * @return
	 */
	public int getLength() {
		return this.getUniprotSeq().getLength();
	}

	/**
	 * Returns true if this UniprotEntry belongs to same domain of life (Bacteria, Archaea, Eukaryota) 
	 * as the one given
	 * @param other
	 * @return
	 */
	public boolean isInSameDomainOfLife(UniprotEntry other) {
		return this.getFirstTaxon().equals(other.getFirstTaxon());
	}


}
