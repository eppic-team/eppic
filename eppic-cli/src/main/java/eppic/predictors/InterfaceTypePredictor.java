package eppic.predictors;

import java.util.List;

import eppic.commons.util.CallType;


public interface InterfaceTypePredictor {

	public static final double SCORE_UNASSIGNED = -1.0;
	
	public static final double CONFIDENCE_HIGH = 0.8;
	public static final double CONFIDENCE_MEDIUM = 0.5;
	public static final double CONFIDENCE_LOW = 0.2;
	public static final double CONFIDENCE_UNASSIGNED = -1.0;
	
	/**
	 * Triggers the calculation of scores, subsequently they can be 
	 * retrieved through {@link #getScore()}, {@link #getScore1()} and {@link #getScore2()}
	 * and the corresponding call through {@link #getCall()}
	 */
	public void computeScores();
	
	/**
	 * Gets a prediction call for the interface. Will be null if {@link #computeScores()} was not called.
	 * The reason for prediction can subsequently be retrieved through {@link #getCallReason()} and 
	 * warnings for it through {@link #getWarnings()}
	 * @return
	 */
	public CallType getCall();
	
	/**
	 * 
	 * @return
	 */
	public String getCallReason();
	
	/**
	 * 
	 * @return
	 */
	public List<String> getWarnings();
	
	/**
	 * Returns the final score calculated for the method, the call from 
	 * {@link #getCall()} is based on this score
	 * @return
	 */
	public double getScore();
	
	/**
	 * Returns the final score calculated for the method for side 1 of interface
	 * @return
	 */
	public double getScore1();
	
	/**
	 * Returns the final score calculated for the method for side 2 of interface
	 * @return
	 */
	public double getScore2();
	
	/**
	 * Returns the confidence level for the given call. Ranging from 0 to 1, i.e.
	 * this value represents a probability that the call is right or not.
	 * @return
	 */
	public double getConfidence();
}
