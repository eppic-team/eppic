package eppic.predictors;

import java.util.List;

import eppic.CallType;


public interface InterfaceTypePredictor {

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
	
}
