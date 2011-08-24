package ch.systemsx.sybit.crkwebui.client.model;

import com.extjs.gxt.ui.client.data.BaseModel;

public class InterfaceResidueItemModel extends BaseModel
{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public InterfaceResidueItemModel()
	{
		
	}

	public InterfaceResidueItemModel( int structure,
									  int residueNumber,
									  String residueType,
									  float asa,
									  float bsa,
									  float bsaPercentage,
									  int assignment) 
	{
		set("structure", structure);
		set("residueNumber", residueNumber);
		set("residueType", residueType);
		set("asa", asa);
		set("bsa", bsa);
		set("bsaPercentage", bsaPercentage);
		set("assignment", assignment);
		set("METHODS", "");
	}

	public int getStructure() {
		return (Integer) get("structure");
	}
	
	public void setStructure(int structure) {
		set("structure", structure);
	}
	
	public int getResidueNumber() {
		return (Integer) get("residueNumber");
	}
	
	public void setResidueNumber(int residueNumber) {
		set("residueNumber", residueNumber);
	}
	
	public String getResidueType() {
		return (String) get("residueType");
	}
	
	public void setResidueType(String residueType) {
		set("residueType", residueType);
	}

	public float getAsa() {
		return (Float) get("asa");
	}
	
	public void setAsa(float asa) {
		set("asa", asa);
	}
	
	public float getBsa() {
		return (Float) get("bsa");
	}
	
	public void setBsa(float bsa) {
		set("bsa", bsa);
	}
	
	public float getBsaPercentage() {
		return (Float) get("bsaPercentage");
	}
	
	public void setBsaPercentage(float bsaPercentage) {
		set("bsaPercentage", bsaPercentage);
	}
	
	public int getAssignment() {
		return (Integer) get("assignment");
	}
	
	public void setAssignment(int assignment) {
		set("assignment", assignment);
	}
}
