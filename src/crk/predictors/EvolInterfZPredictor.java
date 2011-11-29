package crk.predictors;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;

//import org.apache.commons.logging.Log;
//import org.apache.commons.logging.LogFactory;

import crk.CallType;
import crk.InterfaceEvolContext;
import crk.ScoringType;

public class EvolInterfZPredictor implements InterfaceTypePredictor {

	protected static final int FIRST  = 0;
	protected static final int SECOND = 1;	
	
	//private static final Log LOGGER = LogFactory.getLog(EvolInterfZPredictor.class);

	private CallType call;
	private String callReason;
	private List<String> warnings;

	private double score; // last run cached averaged score
	private ScoringType scoringType; // the type of the last scoring run (either kaks or entropy)

	private EvolInterfZMemberPredictor member1Pred;
	private EvolInterfZMemberPredictor member2Pred;
	
	private InterfaceEvolContext iec;
	
	private double zScoreCutoff;
	
	// cached values of scoring (filled upon call of score methods and getCall)
	private ArrayList<Integer> bioCalls; // cache of the votes (lists of interface member indices)
	private ArrayList<Integer> xtalCalls;
	private ArrayList<Integer> grayCalls;
	private ArrayList<Integer> noPredictCalls;
	

	
	public EvolInterfZPredictor(InterfaceEvolContext iec) {
		this.iec = iec;
		this.warnings = new ArrayList<String>();
		this.member1Pred = new EvolInterfZMemberPredictor(iec,FIRST);
		this.member2Pred = new EvolInterfZMemberPredictor(iec, SECOND);
	}
	
	public EvolInterfZMemberPredictor getMember1Predictor() {
		return member1Pred;
	}
	
	public EvolInterfZMemberPredictor getMember2Predictor() {
		return member2Pred;
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
		else if (member2Call == CallType.NO_PREDICTION) {
			noPredictCalls.add(SECOND);
		}
		

		// decision time!
		int countBio = bioCalls.size();
		int countXtal = xtalCalls.size();
		int countNoPredict = noPredictCalls.size();

		if (countNoPredict==2) {
			call = CallType.NO_PREDICTION;
			callReason = member1Pred.getCallReason()+"\n"+member2Pred.getCallReason();
		} else if (countBio>countXtal) {
			call = CallType.BIO;
			callReason = member1Pred.getCallReason()+"\n"+member2Pred.getCallReason();
		} else if (countXtal>countBio) {
			call = CallType.CRYSTAL;
			callReason = member1Pred.getCallReason()+"\n"+member2Pred.getCallReason();
		} else if (countBio==countXtal) {
			callReason = member1Pred.getCallReason()+"\n"+member2Pred.getCallReason();
			if (score<zScoreCutoff) {
				call = CallType.BIO;
				callReason += "\nAverage score "+String.format("%4.2f", score)+" is below BIO cutoff ("+String.format("%4.2f", zScoreCutoff)+")";
			} else if (score>zScoreCutoff) {
				call = CallType.CRYSTAL;
				callReason += "\nAverage score "+String.format("%4.2f", score)+" is above XTAL cutoff ("+String.format("%4.2f", zScoreCutoff)+")";
			} else if (Double.isNaN(score)) {
				call = CallType.NO_PREDICTION;
				callReason += "\nAverage score is NaN";
			} else {
				// note this is useless, keeping it here only as place holder in case we want to introduce a gray zone
				call = CallType.GRAY;
				callReason += "\nAverage score "+String.format("%4.2f", score)+" falls in gray area ("+
						String.format("%4.2f", zScoreCutoff)+" - "+String.format("%4.2f", zScoreCutoff)+")";
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
		return warnings;
	}

	/**
	 * Calculates the entropy scores for this interface.
	 * Subsequently use {@link #getCall()} and {@link #getScore()} to get call and average score
	 */
	public void scoreEntropy() {
		member1Pred.scoreEntropy();
		member2Pred.scoreEntropy();
		
		if (canDoFirstEntropyScoring() && canDoSecondEntropyScoring()) {
			score = (member1Pred.getScore()+member2Pred.getScore())/2;
		} else if (!canDoFirstEntropyScoring() && !canDoSecondEntropyScoring()) {
			score = Double.NaN;
		} else if (canDoFirstEntropyScoring()) {
			score = member1Pred.getScore();
		} else if (canDoSecondEntropyScoring()) {
			score = member2Pred.getScore();
		}
		
		score = (member1Pred.getScore()+member2Pred.getScore())/2;
		scoringType = ScoringType.ENTROPY;
	}
	
	/**
	 * Calculates the ka/ks scores for this interface.
	 * Subsequently use {@link #getCall()} and {@link #getScore()} to get call and average score 
	 */
	public void scoreKaKs() {
		member1Pred.scoreKaKs();
		member2Pred.scoreKaKs();
		score = (member1Pred.getScore()+member2Pred.getScore())/2;
		scoringType = ScoringType.KAKS;
	}

	public ScoringType getScoringType() {
		return this.scoringType;
	}
	
	public double getScore() {
		return score;
	}
	
	public void setZscoreCutoff(double zScoreCutoff) {
		this.zScoreCutoff = zScoreCutoff;
		this.member1Pred.setZscoreCutoff(zScoreCutoff);
		this.member2Pred.setZscoreCutoff(zScoreCutoff);
	}
	
	public void setBsaToAsaCutoff(double bsaToAsaCutoff) {
		this.member1Pred.setBsaToAsaCutoff(bsaToAsaCutoff);
		this.member2Pred.setBsaToAsaCutoff(bsaToAsaCutoff);		
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
	
	private void printScores(PrintStream ps, CallType call) {
		
		ps.printf("%5.2f\t%5.2f\t%5.2f\t%5.2f\t%6s\t",
				member1Pred.getCoreScore(), member1Pred.getMean(), member1Pred.getSd(), member1Pred.getScore(), member1Pred.getCall().getName());
		ps.printf("%5.2f\t%5.2f\t%5.2f\t%5.2f\t%6s\t",
				member2Pred.getCoreScore(), member2Pred.getMean(), member2Pred.getSd(), member2Pred.getScore(), member2Pred.getCall().getName());
		// call type, score, voters
		ps.printf("%6s\t%5.2f", call.getName(),	this.getScore());
		//ps.print(this.getVotersString());
		String callReasonForPlainFileOutput = callReason.replace("\n", ", ");
		ps.print("\t"+callReasonForPlainFileOutput);
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
	
	public void resetCall() {
		this.call = null;
		this.scoringType = null;
		this.warnings = new ArrayList<String>();
		this.callReason = null;
		this.bioCalls = new ArrayList<Integer>();
		this.xtalCalls = new ArrayList<Integer>();
		this.noPredictCalls = new ArrayList<Integer>();
		this.score = -1;
		this.member1Pred.resetCall();
		this.member2Pred.resetCall();
	}
	
}
