package eppic.commons.pisa;

import java.io.PrintStream;
import java.io.Serializable;


public class PisaResidue implements Serializable{
	
	private static final long serialVersionUID = -6171417132434795731L;
	
	private int resSerial;
	private String pdbResSer; // a String because it can have insertion codes
	private String resType;
	private double asa;
	private double bsa;
	private double solvEnergy;
	
	public PisaResidue() {
		
	}

	public void printTabular(PrintStream ps) {
		ps.printf("%d\t%s\t%s\t%6.2f\t%6.2f\t%6.2f\n",resSerial,pdbResSer,resType,asa,bsa,solvEnergy);
	}
	
	/**
	 * Returns bsa/asa
	 * @return
	 */
	public double getBsaToAsaRatio() {
		return (double)this.bsa/ (double)this.asa;
	}
	
	/**
	 * @return the resSerial
	 */
	public int getResSerial() {
		return resSerial;
	}

	/**
	 * @param resSerial the resSerial to set
	 */
	public void setResSerial(int resSerial) {
		this.resSerial = resSerial;
	}

	/**
	 * @return the pdbResSer
	 */
	public String getPdbResSer() {
		return pdbResSer;
	}

	/**
	 * @param pdbResSer the pdbResSer to set
	 */
	public void setPdbResSer(String pdbResSer) {
		this.pdbResSer = pdbResSer;
	}

	/**
	 * @return the resType
	 */
	public String getResType() {
		return resType;
	}

	/**
	 * @param resType the resType to set
	 */
	public void setResType(String resType) {
		this.resType = resType;
	}

	/**
	 * @return the asa
	 */
	public double getAsa() {
		return asa;
	}

	/**
	 * @param asa the asa to set
	 */
	public void setAsa(double asa) {
		this.asa = asa;
	}

	/**
	 * @return the bsa
	 */
	public double getBsa() {
		return bsa;
	}

	/**
	 * @param bsa the bsa to set
	 */
	public void setBsa(double bsa) {
		this.bsa = bsa;
	}

	/**
	 * @return the solvEnergy
	 */
	public double getSolvEnergy() {
		return solvEnergy;
	}

	/**
	 * @param solvEnergy the solvEnergy to set
	 */
	public void setSolvEnergy(double solvEnergy) {
		this.solvEnergy = solvEnergy;
	}
	
	public String toString() {
		return resType+resSerial;
	}
}
