package eppic.commons.sequence;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;


/**
 * A minimal Uniref entry representation  
 * 
 * @see UniProtConnection
 * @see UniprotLocalConnection
 * @see UnirefXMLParser
 * 
 * @author duarte_j
 *
 */
public class UnirefEntry implements Serializable {

	private static final long serialVersionUID = 1L;
	
	private String id;
	private String uniprotId;
	private String uniparcId;
	private int ncbiTaxId;
	private String sequence;
	
	private List<String> inactiveUniprotIds;
	private List<UnirefEntryClusterMember> clusterMembers;
	
	private List<String> taxons;
	
	public UnirefEntry() {
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getUniprotId() {
		return uniprotId;
	}

	public void setUniprotId(String uniprotId) {
		this.uniprotId = uniprotId;
	}

	public String getUniparcId() {
		return uniparcId;
	}

	public void setUniparcId(String uniparcId) {
		this.uniparcId = uniparcId;
	}

	public int getNcbiTaxId() {
		return ncbiTaxId;
	}

	public void setNcbiTaxId(int ncbiTaxId) {
		this.ncbiTaxId = ncbiTaxId;
	}

	public String getSequence() {
		return sequence;
	}
	
	/**
	 * Gets the sequence as a Sequence object using as name for it either 
	 * the uniprot id if the entry is UniProt or uniparc id if the entry is not uniprot 
	 * @return
	 */
	public Sequence getSeq() {
		return new Sequence(isUniprot()?uniprotId:uniparcId,sequence);
	}

	public void setSequence(String sequence) {
		this.sequence = sequence;
	}
	
	public int getLength() {
		return this.sequence.length();
	}
	
	/**
	 * The taxonomy as returned by UniProt JAPI's UniprotEntry.getTaxonomy(). 
	 * First value is the most general (domain of life), last is the most specific (species).
	 * @return
	 */
	public List<String> getTaxons() {
		return taxons;
	}	
	
	public void setTaxons(List<String> taxons) {
		this.taxons = taxons;
	}
	
	/**
	 * Returns either the uniprot or the uniparc id depending of the kind of this uniref entry
	 * @return
	 */
	public String getUniId() {
		return isUniprot()?uniprotId:uniparcId;
	}
	
	/**
	 * Adds an inactive uniprot id to this uniref entry.
	 * Uniprot ids get obsoleted (inactive) when they are merged, demerged, deleted, etc
	 * The ids are then never reused and kept in an inactive ids database.
	 * e.g. Q31NP8 is an obsoleted entry replaced by P30340
	 * See http://www.uniprot.org/help/query-fields and 
	 * http://www.uniprot.org/uniprot/?query=active:no 
	 * @param uniprotId
	 */
	public void addInactiveUniprotId(String uniprotId) {
		if (inactiveUniprotIds==null) {
			inactiveUniprotIds = new ArrayList<String>();
		}
		this.inactiveUniprotIds.add(uniprotId);
	}
	
	/**
	 * Adds a cluster member to this UniRef entry.
	 * Uniref entries are clustering several sequences that share 100%, 90% or 50% identity,
	 * thus they are actually clusters for which a representative has been chosen.
	 * e.g. UniRef100_P30340 contains 4 members, the representative P30340 plus Q2EFX9, Q5N5G6, Q6PQB8
	 * We only keep UniProt cluster members and not UniParc cluster members
	 * @param member
	 */
	public void addClusterMember(UnirefEntryClusterMember member) {
		if (clusterMembers==null) {
			clusterMembers = new ArrayList<UnirefEntryClusterMember>();
		}
		this.clusterMembers.add(member);
	}
	
	public List<UnirefEntryClusterMember> getClusterMembers() {
		return this.clusterMembers;
	}
	
	public boolean hasClusterMembers() {
		return clusterMembers!=null;
	}
	
	public boolean hasTaxons() {
		return taxons!=null;
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
	
//	/**
//	 * Returns a UniprotEntry with uniprot id, sequence, taxId and taxons as they are in this UnirefEntry
//	 * Does not set any information of EMBL CDS or related
//	 * @return
//	 */
//	public UniprotEntry getUniprotEntry() {
//		UniprotEntry uniprotEntry = new UniprotEntry(uniprotId);
//		uniprotEntry.setUniprotSeq(getSeq());
//		uniprotEntry.setTaxId(ncbiTaxId);
//		uniprotEntry.setTaxons(taxons);
//		return uniprotEntry;
//	}
	
	/**
	 * Returns true if this UniRef entry is a UniProt entry, else (i.e. if a UniParc entry) it returns false
	 * @return
	 */
	public boolean isUniprot() {
		return uniprotId!=null;
	}
	
	/**
	 * Returns true if this UniRef corresponds to an isoform UniProt entry (ids are like Q12345-1 or Q12345-2)
	 * @return
	 */
	public boolean isUniprotIsoform() {
		if (!isUniprot()) return false;
		return uniprotId.contains("-");
	}
	
	/**
	 * Returns true if this UnirefEntry belongs to same domain of life (Bacteria, Archaea, Eukaryota) 
	 * as the one given
	 * @param other
	 * @return
	 */
	public boolean isInSameDomainOfLife(UnirefEntry other) {
		return this.getFirstTaxon().equals(other.getFirstTaxon());
	}
	
	/**
	 * Returns true if this UnirefEntry belongs to same domain of life (Bacteria, Archaea, Eukaryota) 
	 * as the given UniprotEntry
	 * @param uniprotEntry
	 * @return
	 */
	public boolean isInSameDomainOfLife(UniprotEntry uniprotEntry) {
		return this.getFirstTaxon().equals(uniprotEntry.getFirstTaxon());
	}	
	
	/**
	 * If the entry contains null except for case uniprotId==null with a uniparcId!=null
	 * or case where uniprotId==null then ncbiTaxId can be ==0
	 * @return
	 */
	public boolean hasNulls() {
		return (id==null || (uniprotId==null && uniparcId==null) || uniparcId==null || (uniprotId!=null && ncbiTaxId==0) || sequence==null);
	}
	
	@Override
	public String toString() {
		return id+"\t"+uniprotId+"\t"+uniparcId+"\t"+ncbiTaxId+"\t"+sequence;
	}
	
	public String getTabDelimitedMySQLString() {
		return id+"\t"+(uniprotId==null?"\\N":uniprotId)+"\t"+uniparcId+"\t"+ncbiTaxId+"\t"+sequence;
	}
	
	/**
	 * Replace non-standard amino-acid character 'O' (Pyrrolysine) by 'X' 
	 * in the sequence of this UnirefEntry
	 * 
	 * @return true if 'O' was present and replaced, false otherwise
	 */
	public boolean replaceNonStandardByX() {
		if (this.sequence == null) return false;
		
		if (this.sequence.contains("O")) {
			this.sequence = this.sequence.replaceAll("O", "X");
			return true;
		}
		
		return false;
	}

}
