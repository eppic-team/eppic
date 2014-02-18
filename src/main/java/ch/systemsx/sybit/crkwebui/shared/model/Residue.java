package ch.systemsx.sybit.crkwebui.shared.model;

import java.io.Serializable;

import eppic.model.ResidueDB;

/**
 * DTO class for Residue.
 * @author AS
 */
public class Residue implements Serializable
{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	private int uid;
	
	private int side;
	private int residueNumber;
	private String pdbResidueNumber;
	private String residueType;
	private double asa;
	private double bsa;
	private double bsaPercentage;
	private int region; // one of the constants above: SURFACE, RIM, CORE
	private double entropyScore;

	public Residue(int residueNumber, 
					String pdbResidueNumber, 
					String residueType, 
					double asa, 
					double bsa, 
					double bsaPercentage, 
					int region, 
					double entropyScore) {
		this.residueNumber = residueNumber;
		this.pdbResidueNumber = pdbResidueNumber;
		this.residueType = residueType;
		this.asa = asa;
		this.bsa = bsa;
		this.bsaPercentage = bsaPercentage;
		this.region = region;
		this.entropyScore = entropyScore;
	}
	
	public Residue()
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

	public int getRegion() {
		return this.region;
	}
	
	public void setRegion(int region) {
		this.region = region;
	}
	
	public void setSide(int side) {
		this.side = side;
	}

	public int getSide() {
		return side;
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
	
	public double getEntropyScore() {
		return entropyScore;
	}

	public void setEntropyScore(double entropyScore) {
		this.entropyScore = entropyScore;
	}
	
	/**
	 * Converts DB model item into DTO one.
	 * @param residueDB model item to convert
	 * @return DTO representation of model item
	 */
	public static Residue create(ResidueDB residueDB)
	{
		Residue residue = new Residue();
		residue.setAsa(residueDB.getAsa());
		residue.setRegion(residueDB.getRegion());
		residue.setBsa(residueDB.getBsa());
		residue.setBsaPercentage(residueDB.getBsa() / 
											  residueDB.getAsa());
		residue.setEntropyScore(residueDB.getEntropyScore());
		residue.setResidueNumber(residueDB.getResidueNumber());
		residue.setPdbResidueNumber(residueDB.getPdbResidueNumber());
		residue.setResidueType(residueDB.getResidueType());
		residue.setSide(residueDB.getSide());
		residue.setUid(residueDB.getUid());
		return residue;
	}
}
