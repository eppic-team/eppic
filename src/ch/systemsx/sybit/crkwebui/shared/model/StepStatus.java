package ch.systemsx.sybit.crkwebui.shared.model;

import java.io.Serializable;

public class StepStatus implements Serializable
{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	public StepStatus()
	{
		
	}
	
	private String currentStep;
	private int currentStepNumber;
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
