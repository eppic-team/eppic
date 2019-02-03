package eppic.commons.sequence;

import java.io.Serializable;

public class UnirefEntryClusterMember implements Serializable {

	private static final long serialVersionUID = 1L;

	private String uniprotId;
	private int ncbiTaxId;

	public UnirefEntryClusterMember() {
		
	}
	
	public UnirefEntryClusterMember(String uniprotId, int ncbiTaxId) {
		this.uniprotId = uniprotId;
		this.ncbiTaxId = ncbiTaxId;
	}

	public String getUniprotId() {
		return uniprotId;
	}

	public void setUniprotId(String uniprotId) {
		this.uniprotId = uniprotId;
	}

	public int getNcbiTaxId() {
		return ncbiTaxId;
	}

	public void setNcbiTaxId(int ncbiTaxId) {
		this.ncbiTaxId = ncbiTaxId;
	}
	
}
