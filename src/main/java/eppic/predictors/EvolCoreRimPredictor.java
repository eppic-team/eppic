package eppic.predictors;

//import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import owl.core.structure.InterfaceRimCore;

//import org.apache.commons.logging.Log;
//import org.apache.commons.logging.LogFactory;

import eppic.EppicParams;
import eppic.CallType;
import eppic.InterfaceEvolContext;
import eppic.ScoringType;


public class EvolCoreRimPredictor implements InterfaceTypePredictor {

	protected static final int FIRST  = 0;
	protected static final int SECOND = 1;
	
	//private static final Log LOGGER = LogFactory.getLog(EvolRimCorePredictor.class);
	
	private InterfaceEvolContext iec;
	
	private String callReason;
	private List<String> warnings;

	private CallType call; // cached result of the last call to getCall(bioCutoff, xtalCutoff, homologsCutoff, minCoreSize, minMemberCoreSize)
	
	private double score; // cache of the last run final score (average of ratios of both sides) (filled by getCall)
	
	private double callCutoff;
	
	private ScoringType scoringType; // the type of the last scoring run (either kaks or entropy)
	
	private double bsaToAsaCutoff;
	private double minAsaForSurface;

	private EvolCoreRimMemberPredictor member1Pred;
	private EvolCoreRimMemberPredictor member2Pred;
	
	public EvolCoreRimPredictor(InterfaceEvolContext iec) {
		this.iec = iec;
		this.warnings = new ArrayList<String>();
		this.member1Pred = new EvolCoreRimMemberPredictor(this, FIRST);
		this.member2Pred = new EvolCoreRimMemberPredictor(this, SECOND);
	}
	
	public EvolCoreRimMemberPredictor getMember1Predictor() {
		return member1Pred;
	}
	
	public EvolCoreRimMemberPredictor getMember2Predictor() {
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
		
		int countBio = 0;
		int countXtal = 0;
		int countNoPredict = 0;
		
		CallType member1Call = member1Pred.getCall();
		CallType member2Call = member2Pred.getCall();
		warnings.addAll(member1Pred.getWarnings());
		warnings.addAll(member2Pred.getWarnings());
		
		// member1
		if (member1Call == CallType.BIO) countBio++;
		else if (member1Call == CallType.CRYSTAL) countXtal++;
		else if (member1Call == CallType.NO_PREDICTION) countNoPredict++;
		//member2
		if (member2Call == CallType.BIO) countBio++;
		else if (member2Call == CallType.CRYSTAL) countXtal++;
		else if (member2Call == CallType.NO_PREDICTION) countNoPredict++;
		

		iec.getInterface().calcRimAndCore(bsaToAsaCutoff, minAsaForSurface);
		InterfaceRimCore rimCore1 = iec.getInterface().getRimCore(0);
		InterfaceRimCore rimCore2 = iec.getInterface().getRimCore(1);

		// a special condition for core size, we don't want that if one side says NOPREDICT because of size, 
		// then the prediction is based only on the other side
		if (rimCore1.getCoreSize()<EppicParams.MIN_NUMBER_CORE_RESIDUES_EVOL_SCORE ||
			rimCore2.getCoreSize()<EppicParams.MIN_NUMBER_CORE_RESIDUES_EVOL_SCORE) {
			call = CallType.NO_PREDICTION;
			callReason ="Not enough core residues to calculate evolutionary score ("+
			rimCore1.getCoreSize()+"+"+rimCore2.getCoreSize()+"), at least "+EppicParams.MIN_NUMBER_CORE_RESIDUES_EVOL_SCORE+" per side required";
			
		// then the rest of the decision is based solely on what members call
		} else if (countNoPredict==2) {
			// NOTE nopred because of too few residues was caught already in previous condition
			// here we catch any other kind of nopreds
			call = CallType.NO_PREDICTION;
			callReason = member1Pred.getCallReason()+"\n"+member2Pred.getCallReason();
		} else if (countBio>countXtal) {
			call = CallType.BIO;
			callReason = member1Pred.getCallReason()+"\n"+member2Pred.getCallReason();
		} else if (countXtal>countBio) {
			call = CallType.CRYSTAL;
			callReason = member1Pred.getCallReason()+"\n"+member2Pred.getCallReason(); 
		} else if (countBio==countXtal) {
			//TODO we are taking simply the average, is this the best solution?
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
	 */
	public void computeScores() {
		
		member1Pred.computeScores();
		member2Pred.computeScores();
		
		if (canDoFirstEntropyScoring() && canDoSecondEntropyScoring()) {
			score = (member1Pred.getScore()+member2Pred.getScore())/2.0;
		} else if (!canDoFirstEntropyScoring() && !canDoSecondEntropyScoring()) {
			score = Double.NaN;
		} else if (canDoFirstEntropyScoring()) {
			score = member1Pred.getScore();
		} else if (canDoSecondEntropyScoring()) {
			score = member2Pred.getScore();
		}
		scoringType = ScoringType.CORERIM;

	}
	
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
	
	@Override
	public Map<String,Double> getScoreDetails() {
		// no score details for this method (doesn't make so much sense to average core and rim scores)
		return null;
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
	
//	private void printHomologsInfo(PrintStream ps) {
//		int numHoms1 = -1;
//		int numHoms2 = -1;
//		if (scoringType==ScoringType.ENTROPY) {
//			if (iec.isProtein(FIRST)) numHoms1 = iec.getFirstChainEvolContext().getNumHomologs();
//			if (iec.isProtein(SECOND)) numHoms2 = iec.getSecondChainEvolContext().getNumHomologs();
//		} 
//		ps.printf("%2d\t%2d\t",numHoms1,numHoms2);
//	}
	
//	private void printScores(PrintStream ps, CallType call) {
//		ps.printf("%5.2f\t%5.2f\t%5.2f\t%6s\t",
//				member1Pred.getCoreScore(), member1Pred.getRimScore(), member1Pred.getScore(),member1Pred.getCall().getName());
//		ps.printf("%5.2f\t%5.2f\t%5.2f\t%6s\t",
//				member2Pred.getCoreScore(), member2Pred.getRimScore(), member2Pred.getScore(),member2Pred.getCall().getName());
//		// call type, score, voters
//		ps.printf("%6s\t%5.2f", call.getName(),	this.getScore());
//		//ps.print(this.getVotersString());
//		String callReasonForPlainFileOutput = callReason.replace("\n", ", ");
//		ps.print("\t"+callReasonForPlainFileOutput);
//	}
	
//	public void printScoresLine(PrintStream ps) {
//		CallType call = getCall();
//		iec.getInterface().printRimCoreInfo(ps);
//		printHomologsInfo(ps);
//		printScores(ps, call);
//		ps.println();
//		if (!warnings.isEmpty()){
//			ps.println("  Warnings: ");
//			for (String warning:getWarnings()) {
//				ps.println("     "+warning);
//			}
//		}
//	}
	
	public void resetCall() {
		this.call = null;
		this.scoringType = null;
		this.warnings = new ArrayList<String>();
		this.callReason = null;
		this.score = -1;
		this.member1Pred.resetCall();
		this.member2Pred.resetCall();
	}
	
}
