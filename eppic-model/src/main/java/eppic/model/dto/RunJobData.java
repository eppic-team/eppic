package eppic.model.dto;

import java.io.Serializable;

/**
 * This class is used to transfer information necessary to run the job.
 * @author srebniak_a
 *
 */
public class RunJobData implements Serializable {

	private static final long serialVersionUID = 1L;

	/**
	 * User email adress.
	 */
	private String emailAddress;
	
	/**
	 * Selected input data - pdb code or name of the file.
	 */
	private String input;
	
	/**
	 * Identifier of the job if previously known(null when using pdb code, set when uploading the file)
	 */
	private String jobId;

	/**
	 * Retrieves email address of the user.
	 * @return email address of the user
	 */
	public String getEmailAddress() {
		return emailAddress;
	}

	/**
	 * Sets email address of the user.
	 * @param emailAddress email address of the user
	 */
	public void setEmailAddress(String emailAddress) {
		this.emailAddress = emailAddress;
	}

	/**
	 * Retrieves pdb code or name of submitted file.
	 * @return pdb code or name of submitted file
	 */
	public String getInput() {
		return input;
	}

	/**
	 * Sets pdb code or name of submitted file.
	 * @param input pdb code or name of submitted file
	 */
	public void setInput(String input) {
		this.input = input;
	}

	/**
	 * Sets identifier of the job.
	 * @param jobId identifier of the job
	 */
	public void setJobId(String jobId) {
		this.jobId = jobId;
	}

	/**
	 * Retrieves identifier of the job.
	 * @return identifier of the job
	 */
	public String getJobId() {
		return jobId;
	}
}
