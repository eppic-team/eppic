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
	 * Step description
	 */
	private String currentStep;
	
	/**
	 * Id of the current step
	 */
	private int currentStepNumber;
	
	/**
	 * Total number of steps for processing
	 */
	private int totalNumberOfSteps;

	public String getCurrentStep() {
		return currentStep;
	}
	public void setCurrentStep(String currentStep) {
		this.currentStep = currentStep;
	}
	public int getCurrentStepNumber() {
		return currentStepNumber;
	}
	public void setCurrentStepNumber(int currentStepNumber) {
		this.currentStepNumber = currentStepNumber;
	}
	public int getTotalNumberOfSteps() {
		return totalNumberOfSteps;
	}
	public void setTotalNumberOfSteps(int totalNumberOfSteps) {
		this.totalNumberOfSteps = totalNumberOfSteps;
	}
}
