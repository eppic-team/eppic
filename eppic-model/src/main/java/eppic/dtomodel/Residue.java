package eppic.dtomodel;

import java.io.Serializable;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;


import eppic.adapters.DoubleNaNXmlAdapter;
import eppic.model.ResidueBurialDB;

/**
 * DTO class for ResidueBurial.
 * Note this class doesn't correspond one-to-one to ResidueBurialDB, but rather
 * pulls data from both ResidueBurialDB and ResidueInfoDB
 * @author AS
 */
@XmlAccessorType(XmlAccessType.FIELD)
public class Residue implements Serializable
{

	private static final long serialVersionUID = 1L;
	
	private int uid;
	
	private boolean side;
	private double asa;
	private double bsa;
	@XmlJavaTypeAdapter(type=Double.class, value=DoubleNaNXmlAdapter.class)
	private double bsaPercentage;
	private short region; // one of the constants above: SURFACE, RIM, CORE

	private int residueNumber;	  
	private String pdbResidueNumber;
	private String residueType;
	private double entropyScore;
	

	public Residue(double asa, double bsa, short region) { 
					
		this.asa = asa;
		this.bsa = bsa;
		this.bsaPercentage = bsa/asa;
		this.region = region;
	}
	
	public Residue()
	{
		
	}

	

	public double getAsa() {
		return asa;
	}

	public void setAsa(double asa) {
		this.asa = asa;
	}

	public double getBsa() {
		return bsa;
	}

	public void setBsa(double bsa) {
		this.bsa = bsa;
	}

	public double getBsaPercentage() {
		return bsaPercentage;
	}

	public void setBsaPercentage(double bsaPercentage) {
		this.bsaPercentage = bsaPercentage;
	}

	public short getRegion() {
		return this.region;
	}
	
	public void setRegion(short region) {
		this.region = region;
	}
	
	public void setSide(boolean side) {
		this.side = side;
	}

	public boolean getSide() {
		return side;
	}

	public void setUid(int uid) {
		this.uid = uid;
	}

	public int getUid() {
		return uid;
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

	public double getEntropyScore() {
		return entropyScore;
	}

	public void setEntropyScore(double entropyScore) {
		this.entropyScore = entropyScore;
	}

	/**
	 * Converts DB model item into DTO one.
	 * @param residueBurialDB model item to convert
	 * @return DTO representation of model item
	 */
	public static Residue create(ResidueBurialDB residueBurialDB)
	{
		Residue residue = new Residue();
		residue.setAsa(residueBurialDB.getAsa());
		residue.setRegion(residueBurialDB.getRegion());
		residue.setBsa(residueBurialDB.getBsa());
		
		residue.setBsaPercentage(residueBurialDB.getBsa() / residueBurialDB.getAsa());

		residue.setSide(residueBurialDB.getSide());
		residue.setUid(residueBurialDB.getUid());
		
		if (residueBurialDB.getResidueInfo()!=null) {
			residue.setResidueNumber(residueBurialDB.getResidueInfo().getResidueNumber());
			residue.setPdbResidueNumber(residueBurialDB.getResidueInfo().getPdbResidueNumber());
			residue.setResidueType(residueBurialDB.getResidueInfo().getResidueType());
			residue.setEntropyScore(residueBurialDB.getResidueInfo().getEntropyScore());
		}
		
		return residue;
	}
}
