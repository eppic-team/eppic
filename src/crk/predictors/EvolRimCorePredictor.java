package crk.predictors;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import crk.CallType;
import crk.InterfaceEvolContext;
import crk.ScoringType;

import owl.core.structure.InterfaceRimCore;
import owl.core.structure.Residue;

public class EvolRimCorePredictor implements InterfaceTypePredictor {

	protected static final int FIRST  = 0;
	protected static final int SECOND = 1;
	
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

	private EvolRimCoreMemberPredictor member1Pred;
	private EvolRimCoreMemberPredictor member2Pred;
	
	public EvolRimCorePredictor(InterfaceEvolContext iec) {
		this.iec = iec;
		this.warnings = new ArrayList<String>();
		this.member1Pred = new EvolRimCoreMemberPredictor(this, FIRST);
		this.member2Pred = new EvolRimCoreMemberPredictor(this, SECOND);
	}
	
	protected InterfaceEvolContext getInterfaceEvolContext() {
		return iec;
	}
	
	protected double getBioCutoff() {
		return bioCutoff;
	}
	
	protected double getXtalCutoff() {
		return xtalCutoff;
	}
	
	@Override
	public CallType getCall() {

		if (call!=null) return call;
		
		// the votes with voters (no anonymous vote here!)
		bioCalls = new ArrayList<Integer>();
		xtalCalls = new ArrayList<Integer>();
		grayCalls = new ArrayList<Integer>();
		noPredictCalls = new ArrayList<Integer>();

		// cast your votes!
		CallType member1Call = member1Pred.getCall();
		CallType member2Call = member2Pred.getCall();
		
		// member1
		if (member1Call == CallType.BIO) {
			bioCalls.add(FIRST);
		}
		else if (member1Call == CallType.CRYSTAL) {
			xtalCalls.add(FIRST);
		}
		else if (member1Call == CallType.GRAY) {
			grayCalls.add(FIRST);
		}
		else if (member1Call == CallType.NO_PREDICTION) {
			noPredictCalls.add(FIRST);
		}
		//member2
		if (member2Call == CallType.BIO) {
			bioCalls.add(SECOND);
		}
		else if (member2Call == CallType.CRYSTAL) {
			xtalCalls.add(SECOND);
		}
		else if (member2Call == CallType.GRAY) {
			grayCalls.add(SECOND);
		}
		else if (member2Call == CallType.NO_PREDICTION) {
			noPredictCalls.add(SECOND);
		}
		

		// decision time!
		int countBio = bioCalls.size();
		int countXtal = xtalCalls.size();
		int countGray = grayCalls.size();
		int countNoPredict = noPredictCalls.size();

		if ((iec.getFirstRimCore().getCoreSize()+iec.getSecondRimCore().getCoreSize())<MIN_NUMBER_CORE_RESIDUES_EVOL_SCORE) {
			finalScore = Double.NaN;
			call = CallType.NO_PREDICTION;
			callReason = "Not enough core residues to calculate evolutionary score (at least "+MIN_NUMBER_CORE_RESIDUES_EVOL_SCORE+" needed)";
		} else if (countNoPredict==2) {
			finalScore = getAvrgRatio(noPredictCalls);
			call = CallType.NO_PREDICTION;
			callReason = member1Pred.getCallReason()+"\n"+member2Pred.getCallReason();
		} else if (countBio>countXtal) {
			//TODO check the discrepancies among the different voters. The variance could be a measure of the confidence of the call
			//TODO need to do a study about the correlation of scores in members of the same interface
			//TODO it might be the case that there is good agreement and bad agreement would indicate things like a bio-mimicking crystal interface
			finalScore = getAvrgRatio(bioCalls);
			call = CallType.BIO;
			callReason = member1Pred.getCallReason()+"\n"+member2Pred.getCallReason();
		} else if (countXtal>countBio) {
			finalScore = getAvrgRatio(xtalCalls);
			call = CallType.CRYSTAL;
			callReason = member1Pred.getCallReason()+"\n"+member2Pred.getCallReason();
		} else if (countGray>countBio+countXtal) {
			// we use as final score the average of all gray member scores
			finalScore = getAvrgRatio(grayCalls);
			call = CallType.GRAY;
			callReason = member1Pred.getCallReason()+"\n"+member2Pred.getCallReason();
		} else if (countBio==countXtal) {
			//TODO we are taking simply the average, is this the best solution?
			// weighting is not done here, scores are calculated either weighted/non-weighted before
			List<Integer> indices = new ArrayList<Integer>();
			indices.addAll(bioCalls);
			indices.addAll(xtalCalls);
			finalScore = getAvrgRatio(indices);
			callReason = member1Pred.getCallReason()+"\n"+member2Pred.getCallReason();
			if (finalScore<bioCutoff) {
				call = CallType.BIO;
				callReason += "\nAverage score "+String.format("%4.2f", finalScore)+" is below BIO cutoff ("+String.format("%4.2f", bioCutoff)+")";
			} else if (finalScore>xtalCutoff) {
				call = CallType.CRYSTAL;
				callReason += "\nAverage score "+String.format("%4.2f", finalScore)+" is above XTAL cutoff ("+String.format("%4.2f", xtalCutoff)+")";
			} else if (Double.isNaN(finalScore)) {
				call = CallType.NO_PREDICTION;
				callReason += "\nAverage score is NaN";
			} else {
				call = CallType.GRAY;
				callReason += "\nAverage score "+String.format("%4.2f", finalScore)+" falls in gray area ("+
						String.format("%4.2f", bioCutoff)+" - "+String.format("%4.2f", xtalCutoff)+")";
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
			rimScores[FIRST]  = iec.calcScore(rimCore.getRimResidues(), FIRST, scoType, weighted);
			coreScores[FIRST] = iec.calcScore(rimCore.getCoreResidues(),FIRST, scoType, weighted);
		} else {
			rimScores[FIRST] = Double.NaN;
			coreScores[FIRST] = Double.NaN;
		}

		if (iec.getInterface().isSecondProtein()) {
			InterfaceRimCore rimCore = iec.getSecondRimCore();
			rimScores[SECOND]  = iec.calcScore(rimCore.getRimResidues(), SECOND, scoType, weighted);
			coreScores[SECOND] = iec.calcScore(rimCore.getCoreResidues(),SECOND, scoType, weighted);
		} else {
			rimScores[SECOND] = Double.NaN;
			coreScores[SECOND] = Double.NaN;
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
	
	protected double getScoreRatio(int molecId) {
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
			if (iec.isProtein(FIRST)) numHoms1 = iec.getFirstChainEvolContext().getNumHomologs();
			if (iec.isProtein(SECOND)) numHoms2 = iec.getSecondChainEvolContext().getNumHomologs();
		} else if (scoringType==ScoringType.KAKS) {
			if (iec.isProtein(FIRST)) numHoms1 = iec.getFirstChainEvolContext().getNumHomologsWithValidCDS();
			if (iec.isProtein(SECOND)) numHoms2 = iec.getSecondChainEvolContext().getNumHomologsWithValidCDS();
		}
		ps.printf("%2d\t%2d\t",numHoms1,numHoms2);
	}
	
	private void printScores(PrintStream ps, CallType call) {
		ps.printf("%5.2f\t%5.2f\t%5.2f",
				this.getCoreScore(FIRST), this.getRimScore(FIRST), this.getCoreScore(FIRST)/this.getRimScore(FIRST));
		ps.print("\t");
		ps.printf("%5.2f\t%5.2f\t%5.2f",
				this.getCoreScore(SECOND), this.getRimScore(SECOND), this.getCoreScore(SECOND)/this.getRimScore(SECOND));
		ps.print("\t");
		// call type, score, voters
		ps.printf("%6s\t%5.2f", call.getName(),	this.getFinalScore());
		ps.print(this.getVotersString());
		String callReasonForPlainFileOutput = callReason.replace("\n", ", ");
		ps.print("\t"+callReasonForPlainFileOutput);
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
