package eppic.predictors;

import java.util.List;

import eppic.CallType;

public class GeometryClusterPredictor implements InterfaceTypePredictor {

	private List<GeometryPredictor> gmps;
	
	private CallType call;
	private String callReason;
	
	double score;
	double score1;
	double score2;
	private int minCoreSizeForBio;
	
	public GeometryClusterPredictor(List<GeometryPredictor> gmps) {
		this.gmps = gmps;
	}
	
	@Override
	public void computeScores() {
		
		for (GeometryPredictor gp:gmps) {
			score += gp.getScore();
			
			// TODO can't do this yet because we need to know which side belongs to same chain cluster
			//score1 += gp.getScore1();
			//score2 += gp.getScore2();
		}

		score = score/(double)gmps.size();
		
		// TODO can't do this yet because we need to know which side belongs to same chain cluster
		//score1 = score1/(double)gmps.size();
		//score2 = score2/(double)gmps.size();
		score1 = Double.NaN;
		score2 = Double.NaN;
		
		if (score<minCoreSizeForBio) {
			callReason = "Average total core size "+score+" below cutoff ("+minCoreSizeForBio+")";
			call = CallType.CRYSTAL;
		} else {
			callReason = "Average total core size "+score+" above cutoff ("+minCoreSizeForBio+")";
			call = CallType.BIO;
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

	public void setMinCoreSizeForBio(int minCoreSizeForBio) {
		this.minCoreSizeForBio = minCoreSizeForBio;
	}

}
