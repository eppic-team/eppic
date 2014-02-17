package eppic.model;

import java.io.Serializable;

public class ResidueDB implements Serializable {


	private static final long serialVersionUID = 1L;
	
	public static final int OTHER = -1;
	public static final int SURFACE = 0;
	public static final int RIM = 1;
	public static final int CORE_EVOLUTIONARY = 2;
	public static final int CORE_GEOMETRY = 3;
	
	private int uid;
	
	private int side; // 1 or 2 for 1st or 2nd partner of interface
	private int residueNumber;
	private String pdbResidueNumber;
	private String residueType;
	private double asa;
	private double bsa;
	private int region; // one of the constants above
	private double entropyScore;

	private InterfaceDB interfaceItem;
	
	public ResidueDB(int residueNumber, String pdbResidueNumber, String residueType, 
			double asa, double bsa, int assignment, double entropyScore) {
		
		this.residueNumber = residueNumber;
		this.pdbResidueNumber = pdbResidueNumber;
		this.residueType = residueType;
		this.asa = asa;
		this.bsa = bsa;
		this.region = assignment;
		this.setEntropyScore(entropyScore);
	}
	
	public ResidueDB() {
		
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

	public void setInterface (InterfaceDB interfaceItem) {
		this.interfaceItem = interfaceItem;
	}

	public InterfaceDB getInterface() {
		return interfaceItem;
	}

	public void setUid(int uid) {
		this.uid = uid;
	}

	public int getUid() {
		return uid;
	}

	public void setEntropyScore(double entropyScore) {
		this.entropyScore = entropyScore;
	}

	public double getEntropyScore() {
		return entropyScore;
	}
}
