package eppic.predictors;

import java.util.List;

import eppic.commons.util.CallType;

public class CombinedClusterPredictor implements InterfaceTypePredictor {

	private List<CombinedPredictor> list;
	
	private CallType call;
	private String callReason;
	
	private double probability = 0.5;
	private double confidence = 0.5;
	
	
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
			callReason = String.format(
					"Probability of the interface being biologically relevant is %.2f.",
					probability);
			call = CallType.BIO;
		}
		// 2) XTAL call
		else if (probability <= 0.5) {
			callReason = String.format(
					"Probability of the interface being biologically relevant is %.2f.",
					probability);
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
			confidence = probability;
			break;
		case CRYSTAL: 
			confidence = 1 - probability;
			break;
		case NO_PREDICTION: 
			confidence = 0.5;
		
		}
		
	}

}
