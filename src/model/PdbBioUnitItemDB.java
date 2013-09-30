package model;

import java.io.Serializable;

public class PdbBioUnitItemDB implements Serializable {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private int uid;
	
	private String type;
	private int size;
	
	private PDBScoreItemDB pdbScoreItem;

	public PdbBioUnitItemDB() {
		this.size=0;
		this.type="none";
	}

	public int getUid() {
		return uid;
	}

	public void setUid(int uid) {
		this.uid = uid;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public int getSize() {
		return size;
	}

	public void setSize(int size) {
		this.size = size;
	}

	public PDBScoreItemDB getPdbScoreItem() {
		return pdbScoreItem;
	}

	public void setPdbScoreItem(PDBScoreItemDB pdbScoreItem) {
		this.pdbScoreItem = pdbScoreItem;
	}
	
	

}
