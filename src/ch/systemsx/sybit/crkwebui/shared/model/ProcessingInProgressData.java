package ch.systemsx.sybit.crkwebui.shared.model;

import java.io.Serializable;

/**
 * This class is used to transfer information necessary to display the status of submitted job
 * @author srebniak_a
 *
 */
public class ProcessingInProgressData implements Serializable, ProcessingData 
{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * Identitifier of the job.
	 */
	private String jobId;
	
	/**
	 * Current status of the job.
	 */
	private String status;
	
	/**
	 * Output of the job processing.
	 */
	private String log;
	
	/**
	 * Pdb code or name of the file submitted.
	 */
	private String input;
	
	/**
	 * Type of the input - pdb code or file.
	 */
	private int inputType;
	
	/**
	 * Current step.
	 */
	private StepStatus step;

	public String getJobId() {
		return jobId;
	}

	public void setJobId(String jobId) {
		this.jobId = jobId;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public String getLog() {
		return log;
	}

	public void setLog(String log) {
		this.log = log;
	}

	public void setInput(String input) {
		this.input = input;
	}

	public String getInput() {
		return input;
	}

	public void setStep(StepStatus step) {
		this.step = step;
	}

	public StepStatus getStep() {
		return step;
	}

	public void setInputType(int inputType) {
		this.inputType = inputType;
	}

	public int getInputType() {
		return inputType;
	}

}
