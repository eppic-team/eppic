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
	private InputParameters inputParameters;
	private String emailAddress;
	private String fileName;
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

	public String getFileName() {
		return fileName;
	}

	public void setFileName(String fileName) {
		this.fileName = fileName;
	}

	public void setJobId(String jobId) {
		this.jobId = jobId;
	}

	public String getJobId() {
		return jobId;
	}
}
