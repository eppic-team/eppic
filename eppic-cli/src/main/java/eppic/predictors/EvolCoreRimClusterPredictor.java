package eppic.predictors;

import java.util.List;

import eppic.commons.util.CallType;

public class EvolCoreRimClusterPredictor implements InterfaceTypePredictor {

	private List<EvolCoreRimPredictor> list;
	
	private CallType call;
	private String callReason;
	
	double score;
	double score1;
	double score2;

	private double callCutoff; 

	
	public EvolCoreRimClusterPredictor(List<EvolCoreRimPredictor> list) {
		this.list = list;
	}
	
	@Override
	public void computeScores() {

		for (EvolCoreRimPredictor ecrp:list) {
			score += ecrp.getScore();
			
			// TODO can't do this yet because we need to know which side belongs to same chain cluster
			//score1 += ecrp.getScore1();
			//score2 += ecrp.getScore2();
		}

		score = score/(double)list.size();
		
		// TODO can't do this yet because we need to know which side belongs to same chain cluster
		//score1 = score1/(double)list.size();
		//score2 = score2/(double)list.size();
		score1 = Double.NaN;
		score2 = Double.NaN;
		
		
		if (score<=callCutoff) {
			call = CallType.BIO;
		} else if (score>callCutoff) {
			call = CallType.CRYSTAL;
		} else if (Double.isNaN(score)) {
			call = CallType.NO_PREDICTION;
		} 		


		if (call == CallType.BIO || call == CallType.CRYSTAL) {
			String belowAboveStr;
			if (call==CallType.BIO) belowAboveStr = "below";
			else belowAboveStr = "above";
			callReason = "Average score "+String.format("%4.2f", score)+" is "+belowAboveStr+" cutoff ("+String.format("%4.2f", callCutoff)+")";			
		}

		if (call == CallType.NO_PREDICTION) {
			// TODO what to say in nopred case? 
			callReason = "No prediction"; 
		}

	}

	@Override
	public CallType getCall() {
		
		return call;
	}

	@Override
	public String getCallReason() {

		return callReason;
	}

	@Override
	public List<String> getWarnings() {
		return null;
	}

	@Override
	public double getScore() {
		return score;
	}

	@Override
	public double getScore1() {
		return score1;
	}

	@Override
	public double getScore2() {
		return score2;
	}
	
	@Override
	public double getConfidence() {
		return CONFIDENCE_UNASSIGNED;
	}

	public void setCallCutoff(double callCutoff) {
		this.callCutoff = callCutoff;
	}
}
