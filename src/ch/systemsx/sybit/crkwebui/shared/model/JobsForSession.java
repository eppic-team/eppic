package ch.systemsx.sybit.crkwebui.shared.model;

import java.io.Serializable;
import java.util.List;

public class JobsForSession implements Serializable 
{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * Identifier of the session.
	 */
	private String sessionId;
	
	/**
	 * List of jobs for session.
	 */
	private List<ProcessingInProgressData> jobs;
	
	public JobsForSession()
	{
		
	}
	
	public JobsForSession(String sessionId,
						  List<ProcessingInProgressData> jobs)
	{
		this.sessionId = sessionId;
		this.jobs = jobs;
	}

	/**
	 * Retrieves identifier of the session.
	 * @return identifier of the session
	 */
	public String getSessionId() {
		return sessionId;
	}

	/**
	 * Sets identifier of the session
	 * @param sessionId identifier of the session
	 */
	public void setSessionId(String sessionId) {
		this.sessionId = sessionId;
	}

	/**
	 * Retrieves list of jobs for session.
	 * @return list of jobs for session
	 */
	public List<ProcessingInProgressData> getJobs() {
		return jobs;
	}

	/**
	 * Sets list of jobs for session.
	 * @param jobs list of jobs for session
	 */
	public void setJobs(List<ProcessingInProgressData> jobs) {
		this.jobs = jobs;
	}
	
	
}
