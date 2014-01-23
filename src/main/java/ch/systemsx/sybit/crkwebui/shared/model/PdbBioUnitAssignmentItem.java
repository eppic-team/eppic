/**
 * 
 */
package ch.systemsx.sybit.crkwebui.shared.model;

import java.io.Serializable;

import model.PdbBioUnitAssignmentItemDB;

/**
 * @author biyani_n
 *
 */
public class PdbBioUnitAssignmentItem implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	private int uid;
	
	private int size;
	private String type;
	private String assignment;
	
	public int getUid() {
		return uid;
	}

	public void setUid(int uid) {
		this.uid = uid;
	}

	public int getSize() {
		return size;
	}

	public void setSize(int size) {
		this.size = size;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getAssignment() {
		return assignment;
	}

	public void setAssignment(String assignment) {
		this.assignment = assignment;
	}

	public PdbBioUnitAssignmentItem(){
		this.size=0;
		this.type="none";
		this.assignment="none";
	}

	public static PdbBioUnitAssignmentItem create(PdbBioUnitAssignmentItemDB bioUnitAssignmentItemDB) {
		PdbBioUnitAssignmentItem bioUnitAssignmentItem = new PdbBioUnitAssignmentItem();
		
		bioUnitAssignmentItem.setSize(bioUnitAssignmentItemDB.getSize());
		bioUnitAssignmentItem.setAssignment(bioUnitAssignmentItemDB.getAssignment());
		bioUnitAssignmentItem.setType(bioUnitAssignmentItemDB.getType());
		
		bioUnitAssignmentItem.setUid(bioUnitAssignmentItemDB.getUid());
		
		return bioUnitAssignmentItem;
	}

}
