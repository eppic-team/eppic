package crk.predictors;

import java.util.List;

import crk.CallType;


public interface InterfaceTypePredictor {

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
}
