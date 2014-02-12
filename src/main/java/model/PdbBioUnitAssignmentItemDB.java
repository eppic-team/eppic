/**
 * 
 */
package model;

import java.io.Serializable;

/**
 * @author biyani_n
 *
 */
public class PdbBioUnitAssignmentItemDB implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	private int uid;
	
	private int size;
	private String type;
	private String assignment;
	
	private InterfaceItemDB interfaceItem;
	
	public PdbBioUnitAssignmentItemDB(){
		this.size=0;
		this.type="none";
		this.assignment="none";
	}

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

	public InterfaceItemDB getInterfaceItem() {
		return interfaceItem;
	}

	public void setInterfaceItem(InterfaceItemDB interfaceItem) {
		this.interfaceItem = interfaceItem;
	}
	
	

}
