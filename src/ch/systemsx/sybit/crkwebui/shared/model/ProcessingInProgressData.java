package ch.systemsx.sybit.crkwebui.shared.model;

import java.io.Serializable;

import model.ProcessingData;

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

	// identitifier of the job
	private String jobId;
	
	// current status of the job
	private String status;
	
	// output of the job processing
	private String log;
	
	// pdb code or name of the file submitted
	private String input;
	
	private String step;

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

	public void setStep(String step) {
		this.step = step;
	}

	public String getStep() {
		return step;
	}

}
