package crk;

import java.io.PrintStream;

import owl.core.connections.pisa.PisaRimCore;

public class InterfaceMemberScore {
	
	private PisaRimCore rimCore;
	private double scoreCore;
	private double scoreRim;
	private int numHomologs;
	private int homologsCutoff;
	private int minMemberCoreSize;
	private int memberSerial;

	public InterfaceMemberScore(PisaRimCore rimCore, double scoreCore, double scoreRim, int numHomologs, int homologsCutoff, int minMemberCoreSize, int memberSerial) {
		this.rimCore = rimCore;
		this.scoreCore = scoreCore;
		this.scoreRim = scoreRim;
		this.numHomologs = numHomologs;
		this.homologsCutoff = homologsCutoff;
		this.minMemberCoreSize = minMemberCoreSize;
		this.memberSerial = memberSerial;
	}

	public double getRatio() {
		return scoreCore/scoreRim;
	}
	
	public double getBsaToAsaCutoff() {
		return rimCore.getBsaToAsaCutoff();
	}

	public boolean isProtein() {
		return rimCore!=null;
	}
	
	public boolean hasEnoughHomologs(){
		return numHomologs>=homologsCutoff;
	}
	
	public boolean hasEnoughCore() {
		return rimCore.getCoreSize()>=minMemberCoreSize;
	}

//	public InterfaceCall getCall(double bioCutoff, double xtalCutoff) {
//		if (!isProtein()) {
//			return new InterfaceCall(CallType.NO_PREDICTION,Double.NaN,"Not a protein");
//		}
//		if (!hasEnoughHomologs()) {
//			return new InterfaceCall(CallType.NO_PREDICTION,Double.NaN,"Not enough homologs ("+numHomologs+")");
//		}
//		if (!hasEnoughCore()) {
//			return new InterfaceCall(CallType.CRYSTAL,Double.NaN, "Core too small ("+rimCore.getCoreSize()+"). Likely a crystal interface.");
//		}
//		double ratio = this.getRatio();
//		if (ratio<bioCutoff) {
//			return new InterfaceCall(CallType.BIO, ratio, "Putative biological interface");
//		} else if (ratio>xtalCutoff) {
//			return new InterfaceCall(CallType.CRYSTAL, ratio, "Putative crystal interface");
//		} else {
//			return new InterfaceCall(CallType.GRAY, ratio, "Undecided");
//		}
//	}
	
	public CallType getCall(double bioCutoff, double xtalCutoff) {
		if (!isProtein()) {
			return CallType.NO_PREDICTION;
		}
		if (!hasEnoughHomologs()) {
			return CallType.NO_PREDICTION;
		}
		if (!hasEnoughCore()) {
			return CallType.CRYSTAL;
		}
		double ratio = this.getRatio();
		if (ratio<bioCutoff) {
			return CallType.BIO;
		} else if (ratio>xtalCutoff) {
			return CallType.CRYSTAL;
		} else {
			return CallType.GRAY;
		}

	}
	
	public int getMemberSerial() {
		return memberSerial;
	}
	
	/**
	 * @return the rimCore
	 */
	public PisaRimCore getRimCore() {
		return rimCore;
	}
	
	/**
	 * @param rimCore the rimCore to set
	 */
	public void setRimCore(PisaRimCore rimCore) {
		this.rimCore = rimCore;
	}

	/**
	 * @return the scoreCore
	 */
	public double getScoreCore() {
		return scoreCore;
	}

	/**
	 * @param scoreCore the scoreCore to set
	 */
	public void setScoreCore(double scoreCore) {
		this.scoreCore = scoreCore;
	}

	/**
	 * @return the scoreRim
	 */
	public double getScoreRim() {
		return scoreRim;
	}

	/**
	 * @param scoreRim the scoreRim to set
	 */
	public void setScoreRim(double scoreRim) {
		this.scoreRim = scoreRim;
	}

	/**
	 * @return the numHomologs
	 */
	public int getNumHomologs() {
		return numHomologs;
	}

	/**
	 * @param numHomologs the numHomologs to set
	 */
	public void setNumHomologs(int numHomologs) {
		this.numHomologs = numHomologs;
	}

	/**
	 * @return the homologsCutoff
	 */
	public int getHomologsCutoff() {
		return homologsCutoff;
	}

	/**
	 * @param homologsCutoff the homologsCutoff to set
	 */
	public void setHomologsCutoff(int homologsCutoff) {
		this.homologsCutoff = homologsCutoff;
	}
	
	public static void printRimAndCoreHeader(PrintStream ps, int memberSerial) {
		ps.printf("%5s\t%5s","size"+memberSerial, "CA"+memberSerial);
		
	}
	
	public void printRimAndCoreInfo(PrintStream ps) {
		ps.printf("%5d\t%5.2f",
				(!isProtein())?0:this.rimCore.getCoreSize(),(!isProtein())?0:this.rimCore.getBsaToAsaCutoff());
	}
	
	public void printTabular(PrintStream ps) {
		ps.printf("%5.2f\t%5.2f\t%5.2f",
				this.getScoreCore(),this.getScoreRim(),
				this.getRatio());
	}
	
	public static void printHeader(PrintStream ps, int memberSerial) {
		ps.printf("%5s\t%5s\t%5s",
				"core"+memberSerial,"rim"+memberSerial,"rat"+memberSerial);
	}
	
	
}
