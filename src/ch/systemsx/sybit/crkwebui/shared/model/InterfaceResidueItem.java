package ch.systemsx.sybit.crkwebui.shared.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import model.InterfaceResidueItemDB;
import model.InterfaceResidueMethodItemDB;

public class InterfaceResidueItem implements Serializable
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
	private float asa;
	private float bsa;
	private float bsaPercentage;
	private int assignment; // one of the constants above: SURFACE, RIM, CORE
	
	private List<InterfaceResidueMethodItem> interfaceResidueMethodItems;

	// residue number
	// residue type
	// ASA
	// BSA
	// % BSA
	//entropy
	//KaKs
	
	public InterfaceResidueItem(int residueNumber, String pdbResidueNumber, String residueType, float asa, float bsa, float bsaPercentage, int assignment) {
		this.residueNumber = residueNumber;
		this.pdbResidueNumber = pdbResidueNumber;
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
	
	public static InterfaceResidueItem create(InterfaceResidueItemDB interfaceResidueItemDB)
	{
		InterfaceResidueItem interfaceResidueItem = new InterfaceResidueItem();
		interfaceResidueItem.setAsa(interfaceResidueItemDB.getAsa());
		interfaceResidueItem.setAssignment(interfaceResidueItemDB.getAssignment());
		interfaceResidueItem.setBsa(interfaceResidueItemDB.getBsa());
		interfaceResidueItem.setBsaPercentage(interfaceResidueItemDB.getBsaPercentage());
		
		if(interfaceResidueItemDB.getInterfaceResidueMethodItems() != null)
		{
			List<InterfaceResidueMethodItemDB> interfaceResidueMethodItemDBs = interfaceResidueItemDB.getInterfaceResidueMethodItems();
			
			List<InterfaceResidueMethodItem> interfaceResidueMethodItems = new ArrayList<InterfaceResidueMethodItem>();
			
			for(InterfaceResidueMethodItemDB interfaceResidueMethodItemDB : interfaceResidueMethodItemDBs)
			{
				interfaceResidueMethodItems.add(InterfaceResidueMethodItem.create(interfaceResidueMethodItemDB));
			}
			
			interfaceResidueItem.setInterfaceResidueMethodItems(interfaceResidueMethodItems);
		}
		
		interfaceResidueItem.setResidueNumber(interfaceResidueItemDB.getResidueNumber());
		interfaceResidueItem.setPdbResidueNumber(interfaceResidueItemDB.getPdbResidueNumber());
		interfaceResidueItem.setResidueType(interfaceResidueItemDB.getResidueType());
		interfaceResidueItem.setStructure(interfaceResidueItemDB.getStructure());
		interfaceResidueItem.setUid(interfaceResidueItemDB.getUid());
		return interfaceResidueItem;
	}
}
