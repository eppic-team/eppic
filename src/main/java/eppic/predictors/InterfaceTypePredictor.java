package eppic.predictors;

import java.util.List;
import java.util.Map;

import eppic.CallType;


public interface InterfaceTypePredictor {

	/**
	 * Triggers the calculation of scores, subsequently they can be 
	 * retrieved through {@link #getScore()} and the corresponding call through {@link #getCall()}
	 */
	public void computeScores();
	
	/**
	 * Gets a prediction call for the interface.
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
	 * Returns a Map containing score details in a key-to-value format.
	 * e.g. "average_core_entropy" -> 0.74
	 *      "average_rim_entropy" -> 0.88
	 * @return the map of keys to value details or null if no details are available for the method
	 */
	public Map<String,Double> getScoreDetails();
}
