package ch.systemsx.sybit.crkwebui.server.db.data;


/**
 * Job status details.
 * @author root
 */
public class JobStatusDetails
{
	/**
	 * Identifier of the job.
	 */
	private String jobId;
	
	/**
	 * Status of the job.
	 */
	private String status;
	
	/**
	 * Job input.
	 */
	private String input;
	
	/**
	 * Email address.
	 */
	private String emailAddress;
	
	/**
	 * Submission of the identifier as returned by the submitter.
	 */
	private String submissionId;

	/**
	 * Creates empty instance of job status details.
	 */
	public JobStatusDetails()
	{

	}

	public JobStatusDetails(String jobId,
							String status,
							String input,
							String emailAddress,
							String submissionId)
	{
		this.jobId = jobId;
		this.status = status;
		this.input = input;
		this.emailAddress = emailAddress;
		this.setSubmissionId(submissionId);
	}

	public void setStatus(String status) {
		this.status = status;
	}
	public String getStatus() {
		return status;
	}
	public void setJobId(String jobId) {
		this.jobId = jobId;
	}
	public String getJobId() {
		return jobId;
	}

	public String getInput() {
		return input;
	}

	public void setInput(String input) {
		this.input = input;
	}

	public String getEmailAddress() {
		return emailAddress;
	}

	public void setEmailAddress(String emailAddress) {
		this.emailAddress = emailAddress;
	}

	public void setSubmissionId(String submissionId) {
		this.submissionId = submissionId;
	}

	public String getSubmissionId() {
		return submissionId;
	}
}
