package ch.systemsx.sybit.crkwebui.client.residues.data;

import java.io.Serializable;

/**
 * Data model for interface residues grid.
 * @author AS
 */
public class InterfaceResidueItemModel implements Serializable
{
	public static final String PDB_RESIDUE_NUMBER_PROPERTY_NAME = "pdbResidueNumber";
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	
	private int structure;
	private int residueNumber;
	private String pdbResidueNumber;
	private String residueType;
	private float asa;
	private float bsa;
	private float bsaPercentage;
	private int assignment;
	private float entropyScore;

	public InterfaceResidueItemModel()
	{
		this.structure = 0;
		this.residueNumber = 0;
		this.pdbResidueNumber = "";
		this.residueType = "";
		this.asa = 0;
		this.bsa = 0;
		this.bsaPercentage = 0;
		this.assignment = 0;
		this.entropyScore = 0;
	}

	public int getStructure() {
		return structure;
	}

	public void setStructure(int structure) {
		this.structure = structure;
	}

	public int getResidueNumber() {
		return residueNumber;
	}

	public void setResidueNumber(int residueNumber) {
		this.residueNumber = residueNumber;
	}

	public String getPdbResidueNumber() {
		return pdbResidueNumber;
	}

	public void setPdbResidueNumber(String pdbResidueNumber) {
		this.pdbResidueNumber = pdbResidueNumber;
	}

	public String getResidueType() {
		return residueType;
	}

	public void setResidueType(String residueType) {
		this.residueType = residueType;
	}

	public float getAsa() {
		return asa;
	}

	public void setAsa(float asa) {
		this.asa = asa;
	}

	public float getBsa() {
		return bsa;
	}

	public void setBsa(float bsa) {
		this.bsa = bsa;
	}

	public float getBsaPercentage() {
		return bsaPercentage;
	}

	public void setBsaPercentage(float bsaPercentage) {
		this.bsaPercentage = bsaPercentage;
	}

	public int getAssignment() {
		return assignment;
	}

	public void setAssignment(int assignment) {
		this.assignment = assignment;
	}

	public float getEntropyScore() {
		return entropyScore;
	}

	public void setEntropyScore(float entropyScore) {
		this.entropyScore = entropyScore;
	}
	
	
}
