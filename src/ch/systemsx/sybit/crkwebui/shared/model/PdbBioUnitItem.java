package ch.systemsx.sybit.crkwebui.shared.model;

import java.io.Serializable;

import model.PdbBioUnitItemDB;


public class PdbBioUnitItem implements Serializable {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private int uid;

	private String type;
	private int size;

	public PdbBioUnitItem() {
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

	/**
	 * Converts DB model item into DTO one.
	 * @param pdbBioUnitItemDB model item to convert
	 * @return DTO representation of model item
	 */
	public static PdbBioUnitItem create(PdbBioUnitItemDB bioUnitItemDB) {
		PdbBioUnitItem bioUnitItem = new PdbBioUnitItem();
		
		bioUnitItem.setUid(bioUnitItemDB.getUid());
		
		bioUnitItem.setSize(bioUnitItemDB.getSize());
		bioUnitItem.setType(bioUnitItemDB.getType());
		
		return bioUnitItem;
	}
	
	
}
