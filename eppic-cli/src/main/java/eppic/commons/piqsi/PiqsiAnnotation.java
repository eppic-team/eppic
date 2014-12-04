package eppic.commons.piqsi;

public class PiqsiAnnotation {
	
	private String pdbCode;
	private int pdbSubunits;
	private int piqsiSubunits;
	private String pdbSymmetry;
	private String piqsiSymmetry;
	
	public PiqsiAnnotation(String pdbCode, int pdbSubunits, int piqsiSubunits, String pdbSymmetry, String piqsiSymmetry) {
		this.pdbCode = pdbCode;
		this.pdbSubunits = pdbSubunits;
		this.piqsiSubunits = piqsiSubunits;
		this.pdbSymmetry = pdbSymmetry;
		this.piqsiSymmetry = piqsiSymmetry;
	}

	public String getPdbCode() {
		return pdbCode;
	}

	public void setPdbCode(String pdbCode) {
		this.pdbCode = pdbCode;
	}

	public int getPdbSubunits() {
		return pdbSubunits;
	}

	public void setPdbSubunits(int pdbSubunits) {
		this.pdbSubunits = pdbSubunits;
	}

	public int getPiqsiSubunits() {
		return piqsiSubunits;
	}

	public void setPiqsiSubunits(int piqsiSubunits) {
		this.piqsiSubunits = piqsiSubunits;
	}

	public String getPdbSymmetry() {
		return pdbSymmetry;
	}

	public void setPdbSymmetry(String pdbSymmetry) {
		this.pdbSymmetry = pdbSymmetry;
	}

	public String getPiqsiSymmetry() {
		return piqsiSymmetry;
	}

	public void setPiqsiSymmetry(String piqsiSymmetry) {
		this.piqsiSymmetry = piqsiSymmetry;
	}
}
