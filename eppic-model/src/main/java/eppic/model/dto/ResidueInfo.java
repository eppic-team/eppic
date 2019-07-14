package eppic.model.dto;

import java.io.Serializable;

import eppic.model.adapters.DoubleNaNXmlAdapter;
import eppic.model.db.ResidueInfoDB;

import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

/**
 * DTO class for ResidueInfo
 * @author duarte_j
 *
 */
public class ResidueInfo implements Serializable {

	private static final long serialVersionUID = 1L;
	
	
	
	private int uid;

	private int residueNumber;
	private String pdbResidueNumber;
	private String residueType;
	
	private int uniProtNumber;
	
	private boolean mismatchToRef;

	@XmlJavaTypeAdapter(type=Double.class, value= DoubleNaNXmlAdapter.class)
	private double entropyScore;
	
	
	
	public ResidueInfo() {
		
	}


	public int getUid() {
		return uid;
	}


	public void setUid(int uid) {
		this.uid = uid;
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


	public int getUniProtNumber() {
		return uniProtNumber;
	}


	public void setUniProtNumber(int uniProtNumber) {
		this.uniProtNumber = uniProtNumber;
	}


	public boolean isMismatchToRef() {
		return mismatchToRef;
	}


	public void setMismatchToRef(boolean mismatchToRef) {
		this.mismatchToRef = mismatchToRef;
	}


	public double getEntropyScore() {
		return entropyScore;
	}


	public void setEntropyScore(double entropyScore) {
		this.entropyScore = entropyScore;
	}


	/**
	 * Converts DB model item into DTO one.
	 * @param residueInfoDB model item to convert
	 * @return DTO representation of model item
	 */
	public static ResidueInfo create(ResidueInfoDB residueInfoDB)
	{
		ResidueInfo residue = new ResidueInfo();
		
		residue.setResidueNumber(residueInfoDB.getResidueNumber());
		residue.setPdbResidueNumber(residueInfoDB.getPdbResidueNumber());
		residue.setResidueType(residueInfoDB.getResidueType());
		residue.setUniProtNumber(residueInfoDB.getUniProtNumber());
		residue.setMismatchToRef(residueInfoDB.isMismatchToRef());
		residue.setEntropyScore(residueInfoDB.getEntropyScore()); 
		
		residue.setUid(residueInfoDB.getUid());
		
		return residue;
	}
	
	
	

}
