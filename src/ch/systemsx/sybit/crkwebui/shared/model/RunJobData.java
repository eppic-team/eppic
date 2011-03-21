package ch.systemsx.sybit.crkwebui.shared.model;

import java.io.Serializable;

/**
 * This class is used to transfer information necessary to run the job
 * @author srebniak_a
 *
 */
public class RunJobData implements Serializable 
{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	// parameters which will be used for calculations
	private InputParameters inputParameters;

	// user email adress
	private String emailAddress;
	
	// selected input data - pdb code or name of the file
	private String input;
	
	// identifier of the job if previously known(null when using pdb code, set when uploading the file)
	private String jobId;

	public InputParameters getInputParameters() {
		return inputParameters;
	}

	public void setInputParameters(InputParameters inputParameters) {
		this.inputParameters = inputParameters;
	}

	public String getEmailAddress() {
		return emailAddress;
	}

	public void setEmailAddress(String emailAddress) {
		this.emailAddress = emailAddress;
	}

	public String getInput() {
		return input;
	}

	public void setInput(String input) {
		this.input = input;
	}

	public void setJobId(String jobId) {
		this.jobId = jobId;
	}

	public String getJobId() {
		return jobId;
	}
}
