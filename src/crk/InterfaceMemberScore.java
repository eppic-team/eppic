package crk;

import java.io.PrintStream;
import java.io.Serializable;
import java.util.List;

import org.apache.log4j.Logger;

import owl.core.connections.pisa.PisaResidue;
import owl.core.connections.pisa.PisaRimCore;

public class InterfaceMemberScore implements Serializable {
	
	private static final long serialVersionUID = -3409108391018870468L;
	
	private static final Logger LOGGER = Logger.getLogger(InterfaceMemberScore.class);
	
	private static final double MAX_ALLOWED_UNREL_RES = 0.05; // 5% maximum allowed unreliable residues for core or rim
	
	private PisaRimCore rimCore;
	private double scoreCore;
	private double scoreRim;
	private int numHomologs;
	private int homologsCutoff;
	private int minMemberCoreSize;
	private List<PisaResidue> unreliableRimResidues;
	private List<PisaResidue> unreliableCoreResidues;
	private int memberSerial;

	public InterfaceMemberScore(PisaRimCore rimCore, double scoreCore, double scoreRim, int numHomologs, int homologsCutoff, 
			int minMemberCoreSize,
			List<PisaResidue> unreliableRimResidues,
			List<PisaResidue> unreliableCoreResidues,
			int memberSerial) {
		this.rimCore = rimCore;
		this.scoreCore = scoreCore;
		this.scoreRim = scoreRim;
		this.numHomologs = numHomologs;
		this.homologsCutoff = homologsCutoff;
		this.minMemberCoreSize = minMemberCoreSize;
		this.unreliableRimResidues = unreliableRimResidues;
		this.unreliableCoreResidues = unreliableCoreResidues;
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
	
	public boolean hasEnoughReliableCoreRes() {
		if (((double)this.unreliableCoreResidues.size()/(double)this.rimCore.getCoreSize())>MAX_ALLOWED_UNREL_RES) {
			return false;
		}
		return true;
	}
	
	public boolean hasEnoughReliableRimRes() {
		if (((double)this.unreliableRimResidues.size()/(double)this.rimCore.getRimSize())>MAX_ALLOWED_UNREL_RES) {
			return false;
		}
		return true;		
	}

	public CallType getCall(double bioCutoff, double xtalCutoff) {
		if (!isProtein()) {
			LOGGER.info("Interface member "+memberSerial+" calls NOPRED because it is not a protein");
			return CallType.NO_PREDICTION;
		}
		if (!hasEnoughHomologs()) {
			LOGGER.info("Interface member "+memberSerial+" calls NOPRED because there are not enough homologs to evaluate conservation scores");
			return CallType.NO_PREDICTION;
		}
		if (!hasEnoughCore()) {
			LOGGER.info("Interface member "+memberSerial+" calls NOPRED because core is too small ("+rimCore.getCoreSize()+" residues)");
			return CallType.CRYSTAL;
		}
		if (!hasEnoughReliableCoreRes()) {
			LOGGER.info("Interface member "+memberSerial+" calls NOPRED because there are not enough reliable core residues ("+this.unreliableCoreResidues.size()+" unreliable residues out of "+this.rimCore.getCoreSize()+" residues in core)");
			return CallType.NO_PREDICTION;
		}
		if (!hasEnoughReliableRimRes()) {
			LOGGER.info("Interface member "+memberSerial+" calls NOPRED because there are not enough reliable rim residues ("+this.unreliableRimResidues.size()+" unreliable residues out of "+this.rimCore.getRimSize()+" residues in rim)");
			return CallType.NO_PREDICTION;
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
