package eppic.predictors;

import java.util.List;

import eppic.CallType;

public class EvolCoreSurfaceClusterPredictor implements InterfaceTypePredictor {

	private List<EvolCoreSurfacePredictor> list;
	
	private CallType call;
	private String callReason;
	
	double score;
	double score1;
	double score2;

	private double callCutoff; 

	
	public EvolCoreSurfaceClusterPredictor(List<EvolCoreSurfacePredictor> list) {
		this.list = list;
	}
	
	@Override
	public void computeScores() {
		
		for (EvolCoreSurfacePredictor ecsp:list) {
			score += ecsp.getScore();
			
			// TODO can't do this yet because we need to know which side belongs to same chain cluster
			//score1 += ecsp.getScore1();
			//score2 += ecsp.getScore2();
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

		// TODO handle callReason for NOPRED cases
		if (call == CallType.BIO || call == CallType.CRYSTAL) {
			String belowAboveStr;
			if (call==CallType.BIO) belowAboveStr = "below";
			else belowAboveStr = "above";
			callReason = "Average score "+String.format("%4.2f", score)+" is "+belowAboveStr+" cutoff ("+String.format("%4.2f", callCutoff)+")";			
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

	public void setCallCutoff(double callCutoff) {
		this.callCutoff = callCutoff;
	}
}
