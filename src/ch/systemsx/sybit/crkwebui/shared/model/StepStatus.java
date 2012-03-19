package ch.systemsx.sybit.crkwebui.shared.model;

import java.io.Serializable;

/**
 * This class represents each of the steps of the processing.
 * @author AS
 */
public class StepStatus implements Serializable
{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	public StepStatus()
	{
		
	}

	/**
	 * Step description.
	 */
	private String currentStep;
	
	/**
	 * Id of the current step.
	 */
	private int currentStepNumber;
	
	/**
	 * Total number of steps for processing.
	 */
	private int totalNumberOfSteps;

	/**
	 * Retrieves current step description.
	 * @return current step description
	 */
	public String getCurrentStep() {
		return currentStep;
	}
	
	/**
	 * Sets current step description.
	 * @param currentStep current step description
	 */
	public void setCurrentStep(String currentStep) {
		this.currentStep = currentStep;
	}
	
	/**
	 * Retrieves current step number.
	 * @return current step number
	 */
	public int getCurrentStepNumber() {
		return currentStepNumber;
	}
	
	/**
	 * Sets current step number.
	 * @param currentStepNumber current step number
	 */
	public void setCurrentStepNumber(int currentStepNumber) {
		this.currentStepNumber = currentStepNumber;
	}
	
	/**
	 * Retrieves total number of steps of processing.
	 * @return total number of steps of processing
	 */
	public int getTotalNumberOfSteps() {
		return totalNumberOfSteps;
	}
	
	/**
	 * Sets total number of steps of processing.
	 * @param totalNumberOfSteps total number of steps of processing
	 */
	public void setTotalNumberOfSteps(int totalNumberOfSteps) {
		this.totalNumberOfSteps = totalNumberOfSteps;
	}
}
