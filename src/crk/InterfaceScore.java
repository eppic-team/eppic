package crk;

import java.io.PrintStream;

import owl.core.connections.pisa.PisaRimCore;

public class InterfaceScore {

	private PisaRimCore rimCore1;
	private PisaRimCore rimCore2;
	private double scoreCore1;
	private double scoreRim1;
	private double scoreCore2;
	private double scoreRim2;
	private int minNumCoreResidues;
	
	public InterfaceScore(PisaRimCore rimCore1, PisaRimCore rimCore2, double scoreCore1, double scoreRim1, double scoreCore2, double scoreRim2, int minNumCoreResidues) {
		this.rimCore1 = rimCore1;
		this.rimCore2 = rimCore2;
		this.scoreCore1 = scoreCore1;
		this.scoreRim1 = scoreRim1;
		this.scoreCore2 = scoreCore2;
		this.scoreRim2 = scoreRim2;
		this.minNumCoreResidues = minNumCoreResidues;
	}

	public double getRatio1() {
		return scoreCore1/scoreRim1;
	}
	
	public double getRatio2() {
		return scoreCore2/scoreRim2;
	}
	
	public double getAvrgRatio() {
		return (getRatio1()+getRatio2())/2.0;
	}

	public double getBsaToAsaCutoff1() {
		return rimCore1.getBsaToAsaCutoff();
	}
	
	public double getBsaToAsaCutoff2() {
		return rimCore2.getBsaToAsaCutoff();
	}
	
	/**
	 * Gets the prediction call for this InterfaceScore. See the {@link CRKCall} enum for 
	 * the possible prediction calls.
	 * @param bioCutoff
	 * @param xtalCutoff
	 * @param minNumCoreResidues
	 * @return
	 */
	public CRKCall getCall(double bioCutoff, double xtalCutoff) {
		int totalCoreResidues = 0;
		if (rimCore1!=null) totalCoreResidues+=rimCore1.getCoreSize();
		if (rimCore2!=null) totalCoreResidues+=rimCore2.getCoreSize();
		if (totalCoreResidues<minNumCoreResidues) {
			return CRKCall.SMALL_CORE;
		}
		
		double avrgRatio = -1;
		if (rimCore1==null || rimCore2==null) {
			if (rimCore1==null) {
				avrgRatio = getRatio2();
			} 
			if (rimCore2==null) {
				avrgRatio = getRatio1();
			}
		} else {
			avrgRatio = getAvrgRatio();
		}
		if (avrgRatio<bioCutoff) {
			return CRKCall.BIO;
		} else if (avrgRatio>xtalCutoff) {
			return CRKCall.CRYSTAL;
		} else {
			return CRKCall.GRAY;
		}
	}
	
	/**
	 * @return the scoreCore1
	 */
	public double getScoreCore1() {
		return scoreCore1;
	}

	/**
	 * @param scoreCore1 the scoreCore1 to set
	 */
	public void setScoreCore1(double scoreCore1) {
		this.scoreCore1 = scoreCore1;
	}

	/**
	 * @return the scoreRim1
	 */
	public double getScoreRim1() {
		return scoreRim1;
	}

	/**
	 * @param scoreRim1 the scoreRim1 to set
	 */
	public void setScoreRim1(double scoreRim1) {
		this.scoreRim1 = scoreRim1;
	}

	/**
	 * @return the scoreCore2
	 */
	public double getScoreCore2() {
		return scoreCore2;
	}

	/**
	 * @param scoreCore2 the scoreCore2 to set
	 */
	public void setScoreCore2(double scoreCore2) {
		this.scoreCore2 = scoreCore2;
	}

	/**
	 * @return the scoreRim2
	 */
	public double getScoreRim2() {
		return scoreRim2;
	}

	/**
	 * @param scoreRim2 the scoreRim2 to set
	 */
	public void setScoreRim2(double scoreRim2) {
		this.scoreRim2 = scoreRim2;
	}
	
	/**
	 * 
	 * @return the minimum number of core residues used to calculate this score
	 */
	public int getMinNumCoreResidues() {
		return minNumCoreResidues;
	}
	
	public static void printRimAndCoreHeader(PrintStream ps) {
		ps.printf("%5s\t%5s\t%5s\t%5s","size1", "CA1","size2","CA2");
		
	}
	
	public void printRimAndCoreInfo(PrintStream ps) {
		ps.printf("%5d\t%5.2f\t%5d\t%5.2f",
				(rimCore1==null)?0:this.rimCore1.getCoreSize(),(rimCore1==null)?0:this.rimCore1.getBsaToAsaCutoff(),
				(rimCore2==null)?0:this.rimCore2.getCoreSize(),(rimCore2==null)?0:this.rimCore2.getBsaToAsaCutoff());
	}
	
	public void printTabular(PrintStream ps, double bioCutoff, double xtalCutoff) {
		ps.printf("%5.2f\t%5.2f\t%5.2f\t%5.2f\t%5.2f\t%5.2f\t%5.2f\t%7s",
				this.getScoreCore1(),this.getScoreRim1(),
				this.getRatio1(),
				this.getScoreCore2(),this.getScoreRim2(),
				this.getRatio2(),
				this.getAvrgRatio(),
				this.getCall(bioCutoff, xtalCutoff).getName());
	}

	public static void printHeader(PrintStream ps) {
		ps.printf("%5s\t%5s\t%5s\t%5s\t%5s\t%5s\t%5s\t%7s",
				"core1","rim1","rat1","core2","rim2","rat2","avrg","call");

	}
	
}
