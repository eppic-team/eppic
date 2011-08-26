package crk;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import owl.core.structure.InterfaceRimCore;
import owl.core.structure.Residue;

public class EvolRimCorePredictor implements InterfaceTypePredictor {

	private static final double MAX_ALLOWED_UNREL_RES = 0.05; // 5% maximum allowed unreliable residues for core or rim
	private static final int MIN_NUMBER_CORE_RESIDUES_EVOL_SCORE = 6;

	private static final Log LOGGER = LogFactory.getLog(EvolRimCorePredictor.class);
	
	private InterfaceEvolContext iec;
	
	private String callReason;
	private List<String> warnings;

	private CallType call; // cached result of the last call to getCall(bioCutoff, xtalCutoff, homologsCutoff, minCoreSize, minMemberCoreSize)
	
	// cached values of scoring (filled upon call of score methods and getCall)
	private ArrayList<Integer> bioCalls; // cache of the votes (lists of interface member indices)
	private ArrayList<Integer> xtalCalls;
	private ArrayList<Integer> grayCalls;
	private ArrayList<Integer> noPredictCalls;

	// the cache scoring values, these are filled upon call of scoreInterface
	private double[] rimScores;	// cache of the last run scoreEntropy/scoreKaKs (strictly 2 members, 0 FIRST, 1 SECOND, use constants above)
	private double[] coreScores;
	private double finalScore; // cache of the last run final score (average of ratios of both sides) (filled by getCall)
	
	private double bioCutoff;
	private double xtalCutoff;
	
	private ScoringType scoringType; // the type of the last scoring run (either kaks or entropy)
	private boolean isScoreWeighted; // whether last scoring run was weighted/unweighted


	
	public EvolRimCorePredictor(InterfaceEvolContext iec) {
		this.iec = iec;
		this.warnings = new ArrayList<String>();
	}
	
	@Override
	public CallType getCall() {

		if (call!=null) return call;
		
		// the votes with voters (no anonymous vote here!)
		bioCalls = new ArrayList<Integer>();
		xtalCalls = new ArrayList<Integer>();
		grayCalls = new ArrayList<Integer>();
		noPredictCalls = new ArrayList<Integer>();
		
		if ((iec.getFirstRimCore().getCoreSize()+iec.getSecondRimCore().getCoreSize())<MIN_NUMBER_CORE_RESIDUES_EVOL_SCORE) {
			finalScore = Double.NaN;
			call = CallType.NO_PREDICTION;
			callReason = "Not enough core residues to calculate evolutionary score";
			return call;
		}

		for (int k=0;k<2;k++) {
			CallType memberCall = getMemberCall(k);
			// cast your votes!
			if (memberCall == CallType.BIO) {
				bioCalls.add(k);
			}
			else if (memberCall == CallType.CRYSTAL) {
				xtalCalls.add(k);
			}
			else if (memberCall == CallType.GRAY) {
				grayCalls.add(k);
			}
			else if (memberCall == CallType.NO_PREDICTION) {
				noPredictCalls.add(k);
			}
		}
		
		int countBio = bioCalls.size();
		int countXtal = xtalCalls.size();
		int countGray = grayCalls.size();
		int countNoPredict = noPredictCalls.size();

		// decision time!
		if (countNoPredict==2) {
			finalScore = getAvrgRatio(noPredictCalls);
			call = CallType.NO_PREDICTION;
			callReason = "Both interface members called NOPRED";
		} else if (countBio>countXtal) {
			//TODO check the discrepancies among the different voters. The variance could be a measure of the confidence of the call
			//TODO need to do a study about the correlation of scores in members of the same interface
			//TODO it might be the case that there is good agreement and bad agreement would indicate things like a bio-mimicking crystal interface
			finalScore = getAvrgRatio(bioCalls);
			call = CallType.BIO;
			callReason = "Majority BIO votes";
		} else if (countXtal>countBio) {
			finalScore = getAvrgRatio(xtalCalls);
			call = CallType.CRYSTAL;
			callReason = "Majority XTAL votes";
		} else if (countGray>countBio+countXtal) {
			// we use as final score the average of all gray member scores
			finalScore = getAvrgRatio(grayCalls);
			call = CallType.GRAY;
			callReason = "Majority GRAY votes";
		} else if (countBio==countXtal) {
			//TODO we are taking simply the average, is this the best solution?
			// weighting is not done here, scores are calculated either weighted/non-weighted before
			List<Integer> indices = new ArrayList<Integer>();
			indices.addAll(bioCalls);
			indices.addAll(xtalCalls);
			finalScore = getAvrgRatio(indices);
			callReason = "Interface member "+(bioCalls.get(0)+1)+" called BIO and member "+(xtalCalls.get(0)+1)+" called XTAL. ";
			if (finalScore<bioCutoff) {
				call = CallType.BIO;
				callReason += "Average score is below BIO cutoff ("+String.format("%4.2f", bioCutoff)+")";
			} else if (finalScore>xtalCutoff) {
				call = CallType.CRYSTAL;
				callReason += "Average score is above XTAL cutoff ("+String.format("%4.2f", xtalCutoff)+")";
			} else if (Double.isNaN(finalScore)) {
				call = CallType.NO_PREDICTION;
				callReason += "Average score is NaN";
			} else {
				call = CallType.GRAY;
				callReason += "Average score is in gray area (between BIO cutoff "+String.format("%4.2f", bioCutoff)+" and XTAL cutoff "+String.format("%4.2f", xtalCutoff)+")";
			}
		}
		return call;
	}	

	@Override
	public String getCallReason() {
		return callReason;
	}
	
	@Override
	public List<String> getWarnings() {
		return this.warnings;
	}
	
	/**
	 * Gets the interface partners prediction calls.
	 * @param molecId
	 * @return
	 */
	protected CallType getMemberCall(int molecId) {
		
		int memberSerial = molecId+1;
		InterfaceRimCore rimCore = iec.getRimCore(molecId);
		
		int countsUnrelCoreRes = getUnreliableCoreRes(molecId, scoringType).size();
		int countsUnrelRimRes = getUnreliableRimRes(molecId, scoringType).size();
		
		
		double ratio = this.getScoreRatio(molecId);
		
		CallType call = null;

		if (!iec.isProtein(molecId)) {
			LOGGER.info("Interface "+iec.getInterface().getId()+", member "+memberSerial+" calls NOPRED because it is not a protein");
			warnings.add("Interface member "+memberSerial+" calls NOPRED because it is not a protein");
			call = CallType.NO_PREDICTION;
		}
		else if (!iec.hasEnoughHomologs(molecId)) {
			LOGGER.info("Interface "+iec.getInterface().getId()+", member "+memberSerial+" calls NOPRED because there are not enough homologs to calculate evolutionary scores");
			warnings.add("Interface member "+memberSerial+" calls NOPRED because there are not enough homologs to calculate evolutionary scores");
			call = CallType.NO_PREDICTION;
		}
		else if (((double)countsUnrelCoreRes/(double)rimCore.getCoreSize())>MAX_ALLOWED_UNREL_RES) {
			LOGGER.info("Interface "+iec.getInterface().getId()+", member "+memberSerial+" calls NOPRED because there are not enough reliable core residues ("+
					countsUnrelCoreRes+" unreliable residues out of "+rimCore.getCoreSize()+" residues in core)");
			warnings.add("Interface member "+memberSerial+" calls NOPRED because there are not enough reliable core residues: "+
					countsUnrelCoreRes+" unreliable out of "+rimCore.getCoreSize()+" in core");

			call = CallType.NO_PREDICTION;
		}
		else if (((double)countsUnrelRimRes/(double)rimCore.getRimSize())>MAX_ALLOWED_UNREL_RES) {
			LOGGER.info("Interface "+iec.getInterface().getId()+", member "+memberSerial+" calls NOPRED because there are not enough reliable rim residues ("+
					countsUnrelRimRes+" unreliable residues out of "+rimCore.getRimSize()+" residues in rim)");
			warnings.add("Interface member "+memberSerial+" calls NOPRED because there are not enough reliable rim residues: "+
					countsUnrelRimRes+" unreliable out of "+rimCore.getRimSize()+" in rim");
			call = CallType.NO_PREDICTION;
		}
		else {
			if (ratio<bioCutoff) {
				call = CallType.BIO;
			} else if (ratio>xtalCutoff) {
				call = CallType.CRYSTAL;
			} else if (Double.isNaN(ratio)) {
				warnings.add("Interface member "+memberSerial+" calls NOPRED because score is NaN");
				call = CallType.NO_PREDICTION;
			} else {
				call = CallType.GRAY;
			}
		}

		
		return call;
	}

	/**
	 * Calculates the entropy scores for this interface.
	 * Subsequently use {@link #getCalls(double, double, int, int, int)} and {@link #getFinalScores()}
	 * to get the calls and final scores.
	 * @param weighted
	 */
	public void scoreEntropy(boolean weighted) {
		scoreInterface(weighted, ScoringType.ENTROPY);
		scoringType = ScoringType.ENTROPY;
		isScoreWeighted = weighted;
	}
	
	/**
	 * Calculates the ka/ks scores for this interface.
	 * Subsequently use {@link #getCalls(double, double, int, int, int)} and {@link #getFinalScores()}
	 * to get the calls and final scores. 
	 * @param weighted
	 */
	public void scoreKaKs(boolean weighted) {
		scoreInterface(weighted, ScoringType.KAKS);
		scoringType = ScoringType.KAKS;
		isScoreWeighted = weighted;
	}

	private void scoreInterface(boolean weighted, ScoringType scoType) {
		
		rimScores = new double[2];
		coreScores = new double[2];

		if (iec.getInterface().isFirstProtein()) {
			InterfaceRimCore rimCore = iec.getFirstRimCore();
			rimScores[InterfaceEvolContext.FIRST]  = iec.calcScore(rimCore.getRimResidues(), InterfaceEvolContext.FIRST, scoType, weighted);
			coreScores[InterfaceEvolContext.FIRST] = iec.calcScore(rimCore.getCoreResidues(),InterfaceEvolContext.FIRST, scoType, weighted);
		} else {
			rimScores[InterfaceEvolContext.FIRST] = Double.NaN;
			coreScores[InterfaceEvolContext.FIRST] = Double.NaN;
		}

		if (iec.getInterface().isSecondProtein()) {
			InterfaceRimCore rimCore = iec.getSecondRimCore();
			rimScores[InterfaceEvolContext.SECOND]  = iec.calcScore(rimCore.getRimResidues(), InterfaceEvolContext.SECOND, scoType, weighted);
			coreScores[InterfaceEvolContext.SECOND] = iec.calcScore(rimCore.getCoreResidues(),InterfaceEvolContext.SECOND, scoType, weighted);
		} else {
			rimScores[InterfaceEvolContext.SECOND] = Double.NaN;
			coreScores[InterfaceEvolContext.SECOND] = Double.NaN;
		}

	}

	public ScoringType getScoringType() {
		return this.scoringType;
	}
	
	/**
	 * Gets the average core to rim ratio for the list of given indices of members.
	 * @param a list of indices of the memberScores List
	 * @return
	 */
	private double getAvrgRatio(List<Integer> indices) {
		double sum = 0.0;
		for (int ind:indices) {
			sum+=coreScores[ind]/rimScores[ind];				
		}
		return sum/(double)indices.size();
	}
	
	private double getScoreRatio(int molecId) {
		double ratio = coreScores[molecId]/rimScores[molecId];

		return ratio;
	}

	/**
	 * Gets the final score computed upon last 
	 * call of {@link #getCall(double, double, int, int, int)}
	 * Final score is the score computed from both members of the interface.
	 * @return
	 */
	public double getFinalScore() {
		return finalScore;
	}
	
	public double getRimScore(int molecId) {
		return rimScores[molecId];
	}
	
	public double getCoreScore(int molecId) {
		return coreScores[molecId];
	}
	
	public boolean isScoreWeighted() {
		return this.isScoreWeighted;
	}
	
	public void setBioCutoff(double bioCutoff) {
		this.bioCutoff = bioCutoff;
	}
	
	public void setXtalCutoff(double xtalCutoff) {
		this.xtalCutoff = xtalCutoff;
	}
	
	/**
	 * Finds all unreliable core residues and returns them in a list.
	 * Unreliable are all residues that:
	 * - if scoringType entropy: the alignment from Uniprot to PDB doesn't match
	 * - if scoringType ka/ks: as entropy + the alignment of CDS translation to protein doesn't match
	 * @param molecId
	 * @param scoringType
	 * @return
	 */
	protected List<Residue> getUnreliableCoreRes(int molecId, ScoringType scoringType) {
		List<Residue> coreResidues = iec.getRimCore(molecId).getCoreResidues();

		List<Residue> unreliableCoreResidues = new ArrayList<Residue>();
		List<Residue> unreliableForPdb = iec.getUnreliableResiduesForPDB(coreResidues, molecId);
		String msg = iec.getUnreliableForPdbWarningMsg(unreliableForPdb);
		if (msg!=null) {
			LOGGER.warn(msg);
			warnings.add(msg);
		}	
		unreliableCoreResidues.addAll(unreliableForPdb);
		if (scoringType==ScoringType.KAKS) {
			List<Residue> unreliableForCDS = iec.getUnreliableResiduesForCDS(coreResidues, molecId);
			msg = iec.getUnreliableForCDSWarningMsg(unreliableForCDS);
			if (msg!=null) {
				LOGGER.warn(msg);
				warnings.add(msg);				
			}
			unreliableCoreResidues.addAll(unreliableForCDS);
		}

		return unreliableCoreResidues;
	}

	/**
	 * Finds all unreliable rim residues and returns them in a list.
	 * Unreliable are all residues that:
	 * - if scoringType entropy: the alignment from Uniprot to PDB doesn't match
	 * - if scoringType ka/ks: as entropy + the alignment of CDS translation to protein doesn't match
	 * @param molecId
	 * @param scoringType
	 * @return
	 */
	protected List<Residue> getUnreliableRimRes(int molecId, ScoringType scoringType) {
		List<Residue> rimResidues = iec.getRimCore(molecId).getRimResidues();

		List<Residue> unreliableRimResidues = new ArrayList<Residue>();
		List<Residue> unreliableForPdb = iec.getUnreliableResiduesForPDB(rimResidues, molecId);
		String msg = iec.getUnreliableForPdbWarningMsg(unreliableForPdb);
		if (msg!=null) {
			LOGGER.warn(msg);
			warnings.add(msg);
		}
		unreliableRimResidues.addAll(unreliableForPdb);
		if (scoringType==ScoringType.KAKS) {
			List<Residue> unreliableForCDS = iec.getUnreliableResiduesForCDS(rimResidues, molecId);
			msg = iec.getUnreliableForCDSWarningMsg(unreliableForCDS);
			if (msg!=null) {
				LOGGER.warn(msg);
				warnings.add(msg);				
			}
			unreliableRimResidues.addAll(unreliableForCDS);
		}

		return unreliableRimResidues;
	}
	
	private String getVotersString() {
		String finalStr = "";
		for (CallType callType: CallType.values()) {
			List<Integer> calls = null;
			if (callType==CallType.BIO) {
				calls = bioCalls;
			} else if (callType==CallType.CRYSTAL) {
				calls = xtalCalls;
			} else if (callType==CallType.GRAY) {
				calls = grayCalls;
			} else if (callType==CallType.NO_PREDICTION) {
				calls = noPredictCalls;
			}
			String callsStr = "";
			for (int ind=0;ind<calls.size();ind++) {
				callsStr+=(calls.get(ind)+1);
				if (ind!=calls.size()-1) callsStr+=","; // skip the comma for the last member
			}
			finalStr+=String.format("\t%6s",callsStr);
		}
		return finalStr;
	}
	
	private void printHomologsInfo(PrintStream ps) {
		int numHoms1 = -1;
		int numHoms2 = -1;
		if (scoringType==ScoringType.ENTROPY) {
			if (iec.isProtein(InterfaceEvolContext.FIRST)) numHoms1 = iec.getFirstChainEvolContext().getNumHomologs();
			if (iec.isProtein(InterfaceEvolContext.SECOND)) numHoms2 = iec.getSecondChainEvolContext().getNumHomologs();
		} else if (scoringType==ScoringType.KAKS) {
			if (iec.isProtein(InterfaceEvolContext.FIRST)) numHoms1 = iec.getFirstChainEvolContext().getNumHomologsWithValidCDS();
			if (iec.isProtein(InterfaceEvolContext.SECOND)) numHoms2 = iec.getSecondChainEvolContext().getNumHomologsWithValidCDS();
		}
		ps.printf("%2d\t%2d\t",numHoms1,numHoms2);
	}
	
	private void printScores(PrintStream ps, CallType call) {
		ps.printf("%5.2f\t%5.2f\t%5.2f",
				this.getCoreScore(InterfaceEvolContext.FIRST), this.getRimScore(InterfaceEvolContext.FIRST), this.getCoreScore(InterfaceEvolContext.FIRST)/this.getRimScore(InterfaceEvolContext.FIRST));
		ps.print("\t");
		ps.printf("%5.2f\t%5.2f\t%5.2f",
				this.getCoreScore(InterfaceEvolContext.SECOND), this.getRimScore(InterfaceEvolContext.SECOND), this.getCoreScore(InterfaceEvolContext.SECOND)/this.getRimScore(InterfaceEvolContext.SECOND));
		ps.print("\t");
		// call type, score, voters
		ps.printf("%6s\t%5.2f", call.getName(),	this.getFinalScore());
		ps.print(this.getVotersString());
		ps.print("\t"+callReason);
	}
	
	public void printScoresLine(PrintStream ps) {
		CallType call = getCall();
		iec.getInterface().printRimCoreInfo(ps);
		printHomologsInfo(ps);
		printScores(ps, call);
		ps.println();
		if (!warnings.isEmpty()){
			ps.println("  Warnings: ");
			for (String warning:getWarnings()) {
				ps.println("     "+warning);
			}
		}
	}
	
	public void resetCall() {
		this.call = null;
		this.scoringType = null;
		this.warnings = new ArrayList<String>();
		this.callReason = null;
		this.bioCalls = new ArrayList<Integer>();
		this.xtalCalls = new ArrayList<Integer>();
		this.grayCalls = new ArrayList<Integer>();
		this.noPredictCalls = new ArrayList<Integer>();
		this.rimScores = null;
		this.coreScores = null;
		this.finalScore = -1;
		this.isScoreWeighted = false;
	}
	
}
