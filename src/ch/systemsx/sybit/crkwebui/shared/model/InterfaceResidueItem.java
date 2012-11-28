package ch.systemsx.sybit.crkwebui.shared.model;

import java.io.Serializable;

import model.InterfaceResidueItemDB;

/**
 * DTO class for InterfaceResidue item.
 * @author AS
 */
public class InterfaceResidueItem implements Serializable
{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	private int uid;
	
	private int structure;
	private int residueNumber;
	private String pdbResidueNumber;
	private String residueType;
	private float asa;
	private float bsa;
	private float bsaPercentage;
	private int assignment; // one of the constants above: SURFACE, RIM, CORE
	private float entropyScore;

	public InterfaceResidueItem(int residueNumber, String pdbResidueNumber, String residueType, float asa, float bsa, float bsaPercentage, int assignment, float entropyScore) {
		this.residueNumber = residueNumber;
		this.pdbResidueNumber = pdbResidueNumber;
		this.residueType = residueType;
		this.asa = asa;
		this.bsa = bsa;
		this.bsaPercentage = bsaPercentage;
		this.assignment = assignment;
		this.entropyScore = entropyScore;
	}
	
	public InterfaceResidueItem()
	{
		
	}

	public int getResidueNumber() {
		return residueNumber;
	}

	public void setResidueNumber(int residueNumber) {
		this.residueNumber = residueNumber;
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

	public void setUid(int uid) {
		this.uid = uid;
	}

	public int getUid() {
		return uid;
	}
	
	public String getPdbResidueNumber() {
		return pdbResidueNumber;
	}
	
	public void setPdbResidueNumber(String pdbResidueNumber) {
		this.pdbResidueNumber = pdbResidueNumber;
	}
	
	/**
	 * Converts DB model item into DTO one.
	 * @param interfaceResidueItemDB model item to convert
	 * @return DTO representation of model item
	 */
	public static InterfaceResidueItem create(InterfaceResidueItemDB interfaceResidueItemDB)
	{
		InterfaceResidueItem interfaceResidueItem = new InterfaceResidueItem();
		interfaceResidueItem.setAsa(interfaceResidueItemDB.getAsa());
		interfaceResidueItem.setAssignment(interfaceResidueItemDB.getAssignment());
		interfaceResidueItem.setBsa(interfaceResidueItemDB.getBsa());
		interfaceResidueItem.setBsaPercentage(interfaceResidueItemDB.getBsa() / 
											  interfaceResidueItemDB.getAsa());
		interfaceResidueItem.setEntropyScore(interfaceResidueItemDB.getEntropyScore());
		interfaceResidueItem.setResidueNumber(interfaceResidueItemDB.getResidueNumber());
		interfaceResidueItem.setPdbResidueNumber(interfaceResidueItemDB.getPdbResidueNumber());
		interfaceResidueItem.setResidueType(interfaceResidueItemDB.getResidueType());
		interfaceResidueItem.setStructure(interfaceResidueItemDB.getStructure());
		interfaceResidueItem.setUid(interfaceResidueItemDB.getUid());
		return interfaceResidueItem;
	}

	public float getEntropyScore() {
		return entropyScore;
	}

	public void setEntropyScore(float entropyScore) {
		this.entropyScore = entropyScore;
	}
}
