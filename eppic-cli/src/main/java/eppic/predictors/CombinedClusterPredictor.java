package eppic.predictors;

import java.util.List;

import eppic.CallType;

public class CombinedClusterPredictor implements InterfaceTypePredictor {

	private List<CombinedPredictor> list;
	
	private CallType call;
	private String callReason;
	
	private double probability;
	private double confidence;
	
	
	public CombinedClusterPredictor(List<CombinedPredictor> lcp) {
		this.list = lcp;
	}
	
	private void calcProbability() {
		// Take the average of the probabilities
		probability = 0;
		for (CombinedPredictor cp : list) {
			probability += cp.getScore();
		}
		probability = probability / (double) list.size();
	}

	@Override
	public void computeScores() {
		
		calcProbability();
		
		// 1) BIO call
		if (probability > 0.5) {
			callReason = "P(BIO) = " + probability + " > 0.5";
			call = CallType.BIO;
		} 
		// 2) XTAL call
		else if (probability < 0.5) {
			callReason = "P(BIO) = " + probability + " < 0.5";
			call = CallType.CRYSTAL;
		}
		
		calcConfidence();

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
		return probability;
	}

	@Override
	public double getScore1() {		
		return SCORE_UNASSIGNED;
	}

	@Override
	public double getScore2() {
		return SCORE_UNASSIGNED;
	}

	@Override
	public double getConfidence() {
		return confidence;
	}
	
	private void calcConfidence() {
		
		switch (call) {
		case BIO: 
			confidence = (probability - 0.5) / 0.5;
			break;
		case CRYSTAL: 
			confidence = (0.5 - probability) / 0.5;
			break;
		case NO_PREDICTION: 
			confidence = CONFIDENCE_UNASSIGNED;
		
		}
		
	}

}
