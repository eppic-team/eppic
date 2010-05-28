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
	
	public InterfaceScore(PisaRimCore rimCore1, PisaRimCore rimCore2, double scoreCore1, double scoreRim1, double scoreCore2, double scoreRim2) {
		this.rimCore1 = rimCore1;
		this.rimCore2 = rimCore2;
		this.scoreCore1 = scoreCore1;
		this.scoreRim1 = scoreRim1;
		this.scoreCore2 = scoreCore2;
		this.scoreRim2 = scoreRim2;
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
	
	public void printTabular(PrintStream ps) {
		ps.printf("%5d\t%5.2f\t%5.2f\t%5.2f\t%5.2f\t%5d\t%5.2f\t%5.2f\t%5.2f\t%5.2f\t%5.2f",
				this.rimCore1.getCoreSize(),this.rimCore1.getBsaToAsaCutoff(),
				this.getScoreCore1(),this.getScoreRim1(),
				this.getRatio1(),
				this.rimCore2.getCoreSize(),this.rimCore2.getBsaToAsaCutoff(),
				this.getScoreCore2(),this.getScoreRim2(),
				this.getRatio2(),
				this.getAvrgRatio());
	}

	public void printHeader(PrintStream ps) {
		ps.printf("%5s\t%5s\t%5s\t%5s\t%5s\t%5s\t%5s\t%5s\t%5s\t%5s\t%5s",
				"size1","CA1","core1","rim1","rat1","size2","CA2","core2","rim2","rat2","avrg");

	}
	
}
