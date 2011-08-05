package model;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

public class InterfaceResidueItem implements Serializable
{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	public static final int SURFACE = 0;
	public static final int RIM = 1;
	public static final int CORE = 2;
	
	private int structure;
	private int residueNumber;
	private String residueType;
	private float asa;
	private float bsa;
	private float bsaPercentage;
	private int assignment; // one of the constants above: SURFACE, RIM, CORE
	
	private List<InterfaceResidueMethodItem> interfaceResidueMethodItems;

	private InterfaceItem interfaceItem;
	// residue number
	// residue type
	// ASA
	// BSA
	// % BSA
	//entropy
	//KaKs
	
	public InterfaceResidueItem(int residueNumber, String residueType, float asa, float bsa, float bsaPercentage, int assignment) {
		this.residueNumber = residueNumber;
		this.residueType = residueType;
		this.asa = asa;
		this.bsa = bsa;
		this.bsaPercentage = bsaPercentage;
		this.assignment = assignment;
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
	
	public List<InterfaceResidueMethodItem> getInterfaceResidueMethodItems() {
		return interfaceResidueMethodItems;
	}

	public void setInterfaceResidueMethodItems(
			List<InterfaceResidueMethodItem> interfaceResidueMethodItems) {
		this.interfaceResidueMethodItems = interfaceResidueMethodItems;
	}

	public void setStructure(int structure) {
		this.structure = structure;
	}

	public int getStructure() {
		return structure;
	}

	public void setInterfaceItem(InterfaceItem interfaceItem) {
		this.interfaceItem = interfaceItem;
	}

	public InterfaceItem getInterfaceItem() {
		return interfaceItem;
	}

}
