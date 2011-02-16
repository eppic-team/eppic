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
	
	private int residueNumber;
	private String residueType;
	private float asa;
	private float bsa;
	private float bsaPercentage;
	
	private Map<String,InterfaceResidueMethodItem> interfaceResidueMethodItems;
	
	// residue number
	// residue type
	// ASA
	// BSA
	// % BSA
	//entropy
	//KaKs
	
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

	public Map<String,InterfaceResidueMethodItem> getInterfaceResidueMethodItems() {
		return interfaceResidueMethodItems;
	}

	public void setInterfaceResidueMethodItems(
			Map<String,InterfaceResidueMethodItem> interfaceResidueMethodItems) {
		this.interfaceResidueMethodItems = interfaceResidueMethodItems;
	}

	public static long getSerialversionuid() {
		return serialVersionUID;
	}

}
