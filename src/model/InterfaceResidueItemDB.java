package model;

import java.io.Serializable;

public class InterfaceResidueItemDB implements Serializable
{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	public static final int SURFACE = 0;
	public static final int RIM = 1;
	public static final int CORE = 2;
	
	private int uid;
	
	private int structure;
	private int residueNumber;
	private String pdbResidueNumber;
	private String residueType;
	private Float asa;
	private Float bsa;
	private Float bsaPercentage;
	private int assignment; // one of the constants above: SURFACE, RIM, CORE
	private Float entropyScore;

	private InterfaceItemDB interfaceItem;
	// residue number
	// residue type
	// ASA
	// BSA
	// % BSA
	//entropy
	//KaKs
	
	public InterfaceResidueItemDB(int residueNumber, String pdbResidueNumber, String residueType, Float asa, Float bsa, Float bsaPercentage, int assignment, Float entropyScore) {
		this.residueNumber = residueNumber;
		this.pdbResidueNumber = pdbResidueNumber;
		this.residueType = residueType;
		this.asa = asa;
		this.bsa = bsa;
		this.bsaPercentage = bsaPercentage;
		this.assignment = assignment;
		this.setEntropyScore(entropyScore);
	}
	
	public InterfaceResidueItemDB()
	{
		
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

	public Float getAsa() {
		return asa;
	}

	public void setAsa(Float asa) {
		this.asa = asa;
	}

	public Float getBsa() {
		return bsa;
	}

	public void setBsa(Float bsa) {
		this.bsa = bsa;
	}

	public Float getBsaPercentage() {
		return bsaPercentage;
	}

	public void setBsaPercentage(Float bsaPercentage) {
		this.bsaPercentage = bsaPercentage;
	}

	public int getAssignment() {
		return this.assignment;
	}
	
	public void setAssignment(int assignment) {
		this.assignment = assignment;
	}
	
	public void setStructure(int structure) {
		this.structure = structure;
	}

	public int getStructure() {
		return structure;
	}

	public void setInterfaceItem(InterfaceItemDB interfaceItem) {
		this.interfaceItem = interfaceItem;
	}

	public InterfaceItemDB getInterfaceItem() {
		return interfaceItem;
	}

	public void setUid(int uid) {
		this.uid = uid;
	}

	public int getUid() {
		return uid;
	}

	public void setEntropyScore(Float entropyScore) {
		this.entropyScore = entropyScore;
	}

	public Float getEntropyScore() {
		return entropyScore;
	}
}
