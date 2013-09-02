package ch.systemsx.sybit.crkwebui.client.residues.data;

import com.extjs.gxt.ui.client.data.BaseModel;

/**
 * Data model for interface residues grid.
 * @author AS
 */
public class InterfaceResidueItemModel extends BaseModel
{
	public static final String PDB_RESIDUE_NUMBER_PROPERTY_NAME = "pdbResidueNumber";
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public InterfaceResidueItemModel()
	{
		set("structure", "");
		set("residueNumber", "");
		set(PDB_RESIDUE_NUMBER_PROPERTY_NAME, "");
		set("residueType", "");
		set("asa", "");
		set("bsa", "");
		set("bsaPercentage", "");
		set("assignment", "");
		set("entropyScore", "");
	}

	public InterfaceResidueItemModel( int structure,
									  int residueNumber,
									  String pdbResidueNumber,
									  String residueType,
									  float asa,
									  float bsa,
									  float bsaPercentage,
									  int assignment,
									  float entropyScore) 
	{
		set("structure", structure);
		set("residueNumber", residueNumber);
		set(PDB_RESIDUE_NUMBER_PROPERTY_NAME, pdbResidueNumber);
		set("residueType", residueType);
		set("asa", asa);
		set("bsa", bsa);
		set("bsaPercentage", bsaPercentage);
		set("assignment", assignment);
		set("entropyScore", entropyScore);
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
	
	public String getPdbResidueNumber() {
		return (String) get(PDB_RESIDUE_NUMBER_PROPERTY_NAME);
	}
	
	public void setPdbResidueNumber(String pdbResidueNumber) {
		set(PDB_RESIDUE_NUMBER_PROPERTY_NAME, pdbResidueNumber);
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
	
	public void setEntropyScore(float entropyScore) {
		set("entropyScore", entropyScore);
	}
	
	public float getEntropyScore() {
		return (Float) get("entropyScore");
	}
}
