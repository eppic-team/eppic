package crk.predictors;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;

import owl.core.structure.InterfaceRimCore;

//import org.apache.commons.logging.Log;
//import org.apache.commons.logging.LogFactory;

import crk.CRKParams;
import crk.CallType;
import crk.InterfaceEvolContext;
import crk.ScoringType;


public class EvolRimCorePredictor implements InterfaceTypePredictor {

	protected static final int FIRST  = 0;
	protected static final int SECOND = 1;
	
	//private static final Log LOGGER = LogFactory.getLog(EvolRimCorePredictor.class);
	
	private InterfaceEvolContext iec;
	
	private String callReason;
	private List<String> warnings;

	private CallType call; // cached result of the last call to getCall(bioCutoff, xtalCutoff, homologsCutoff, minCoreSize, minMemberCoreSize)
	
	private ArrayList<Integer> bioCalls; // cache of the votes (lists of interface member indices)
	private ArrayList<Integer> xtalCalls;
	private ArrayList<Integer> grayCalls;
	private ArrayList<Integer> noPredictCalls;

	private double score; // cache of the last run final score (average of ratios of both sides) (filled by getCall)
	
	private double callCutoff;
	
	private ScoringType scoringType; // the type of the last scoring run (either kaks or entropy)
	private boolean isScoreWeighted; // whether last scoring run was weighted/unweighted
	
	private double bsaToAsaCutoff;
	private double minAsaForSurface;

	private EvolRimCoreMemberPredictor member1Pred;
	private EvolRimCoreMemberPredictor member2Pred;
	
	public EvolRimCorePredictor(InterfaceEvolContext iec) {
		this.iec = iec;
		this.warnings = new ArrayList<String>();
		this.member1Pred = new EvolRimCoreMemberPredictor(this, FIRST);
		this.member2Pred = new EvolRimCoreMemberPredictor(this, SECOND);
	}
	
	public EvolRimCoreMemberPredictor getMember1Predictor() {
		return member1Pred;
	}
	
	public EvolRimCoreMemberPredictor getMember2Predictor() {
		return member2Pred;
	}
	
	protected boolean canDoEntropyScoring(int molecId) {
		return iec.getChainEvolContext(molecId).hasQueryMatch();
	}
	
	protected InterfaceEvolContext getInterfaceEvolContext() {
		return iec;
	}
	
	private boolean canDoFirstEntropyScoring() {
		return iec.getChainEvolContext(FIRST).hasQueryMatch();
	}

	private boolean canDoSecondEntropyScoring() {
		return iec.getChainEvolContext(SECOND).hasQueryMatch();
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
		warnings.addAll(member1Pred.getWarnings());
		warnings.addAll(member2Pred.getWarnings());
		
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
		
		iec.getInterface().calcRimAndCore(bsaToAsaCutoff, minAsaForSurface);
		InterfaceRimCore rimCore1 = iec.getInterface().getRimCore(0);
		InterfaceRimCore rimCore2 = iec.getInterface().getRimCore(1);

		// a special condition for core size, we don't want that if one side says NOPREDICT because of size, 
		// then the prediction is based only on the other side
		if ((rimCore1.getCoreSize()+rimCore2.getCoreSize())<2*CRKParams.MIN_NUMBER_CORE_RESIDUES_EVOL_SCORE) {
			call = CallType.NO_PREDICTION;
			callReason ="Not enough core residues to calculate evolutionary score (at least "+2*CRKParams.MIN_NUMBER_CORE_RESIDUES_EVOL_SCORE+" needed)";
			
		// then the rest of the decision is based solely on what members call
		} else if (countNoPredict==2) {
			call = CallType.NO_PREDICTION;
			callReason = member1Pred.getCallReason()+"\n"+member2Pred.getCallReason();
		} else if (countBio>countXtal) {
			//TODO check the discrepancies among the 2 voters. The variance could be a measure of the confidence of the call
			//TODO need to do a study about the correlation of scores in members of the same interface
			//TODO it might be the case that there is good agreement and bad agreement would indicate things like a bio-mimicking crystal interface
			call = CallType.BIO;
			callReason = member1Pred.getCallReason()+"\n"+member2Pred.getCallReason();
		} else if (countXtal>countBio) {
			call = CallType.CRYSTAL;
			callReason = member1Pred.getCallReason()+"\n"+member2Pred.getCallReason();
		} else if (countGray>countBio+countXtal) {
			// we use as final score the average of all gray member scores
			call = CallType.GRAY;
			callReason = member1Pred.getCallReason()+"\n"+member2Pred.getCallReason();
		} else if (countBio==countXtal) {
			//TODO we are taking simply the average, is this the best solution?
			// weighting is not done here, scores are calculated either weighted/non-weighted before
			callReason = member1Pred.getCallReason()+"\n"+member2Pred.getCallReason();
			if (score<=callCutoff) {
				call = CallType.BIO;
				callReason += "\nAverage score "+String.format("%4.2f", score)+" is below cutoff ("+String.format("%4.2f", callCutoff)+")";
			} else if (score>callCutoff) {
				call = CallType.CRYSTAL;
				callReason += "\nAverage score "+String.format("%4.2f", score)+" is above cutoff ("+String.format("%4.2f", callCutoff)+")";
			} else if (Double.isNaN(score)) {
				call = CallType.NO_PREDICTION;
				callReason += "\nAverage score is NaN";
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
	 * Subsequently use {@link #getCall()} and {@link #getScore()} to get the call and final score
	 * @param weighted
	 */
	public void scoreEntropy(boolean weighted) {
		
		member1Pred.scoreEntropy(weighted);
		member2Pred.scoreEntropy(weighted);
		
		if (canDoFirstEntropyScoring() && canDoSecondEntropyScoring()) {
			score = (member1Pred.getScore()+member2Pred.getScore())/2;
		} else if (!canDoFirstEntropyScoring() && !canDoSecondEntropyScoring()) {
			score = Double.NaN;
		} else if (canDoFirstEntropyScoring()) {
			score = member1Pred.getScore();
		} else if (canDoSecondEntropyScoring()) {
			score = member2Pred.getScore();
		}
		scoringType = ScoringType.ENTROPY;
		isScoreWeighted = weighted;
	}
	
//	/**
//	 * Calculates the ka/ks scores for this interface.
//	 * Subsequently use {@link #getCall()} and {@link #getScore()} to get the call and final score 
//	 * @param weighted
//	 */
//	public void scoreKaKs(boolean weighted) {
//
//		member1Pred.scoreKaKs(weighted);
//		member2Pred.scoreKaKs(weighted);
//
//		if (canDoFirstEntropyScoring() && canDoSecondEntropyScoring()) {
//			score = (member1Pred.getScore()+member2Pred.getScore())/2;
//		} else if (!canDoFirstEntropyScoring() && !canDoSecondEntropyScoring()) {
//			score = Double.NaN;
//		} else if (canDoFirstEntropyScoring()) {
//			score = member1Pred.getScore();
//		} else if (canDoSecondEntropyScoring()) {
//			score = member2Pred.getScore();
//		}
//		scoringType = ScoringType.KAKS;
//		isScoreWeighted = weighted;
//	}

	public ScoringType getScoringType() {
		return this.scoringType;
	}

	/**
	 * Gets the final score computed from both members of the interface.
	 * @return
	 */
	@Override
	public double getScore() {
		return score;
	}
	
	public boolean isScoreWeighted() {
		return this.isScoreWeighted;
	}

	protected double getCallCutoff() {
		return callCutoff;
	}
	
	public void setCallCutoff(double callCutoff) {
		this.callCutoff = callCutoff;
	}
	
	public void setBsaToAsaCutoff(double bsaToAsaCutoff, double minAsaForSurface) {
		this.bsaToAsaCutoff = bsaToAsaCutoff;
		this.minAsaForSurface = minAsaForSurface;
	}
	
	protected double getBsaToAsaCutoff() {
		return bsaToAsaCutoff;
	}
	
	protected double getMinAsaForSurface() {
		return minAsaForSurface;
	}
	
	@SuppressWarnings("unused")
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
		} 
//		else if (scoringType==ScoringType.KAKS) {
//			if (iec.isProtein(FIRST)) numHoms1 = iec.getFirstChainEvolContext().getNumHomologsWithValidCDS();
//			if (iec.isProtein(SECOND)) numHoms2 = iec.getSecondChainEvolContext().getNumHomologsWithValidCDS();
//		}
		ps.printf("%2d\t%2d\t",numHoms1,numHoms2);
	}
	
	private void printScores(PrintStream ps, CallType call) {
		ps.printf("%5.2f\t%5.2f\t%5.2f\t%6s\t",
				member1Pred.getCoreScore(), member1Pred.getRimScore(), member1Pred.getScore(),member1Pred.getCall().getName());
		ps.printf("%5.2f\t%5.2f\t%5.2f\t%6s\t",
				member2Pred.getCoreScore(), member2Pred.getRimScore(), member2Pred.getScore(),member2Pred.getCall().getName());
		// call type, score, voters
		ps.printf("%6s\t%5.2f", call.getName(),	this.getScore());
		//ps.print(this.getVotersString());
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
		this.score = -1;
		this.isScoreWeighted = false;
		this.member1Pred.resetCall();
		this.member2Pred.resetCall();
	}
	
}
