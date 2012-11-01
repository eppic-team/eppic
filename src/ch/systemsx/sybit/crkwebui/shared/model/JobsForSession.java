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
	 * Flag pointing whether new session has been established.
	 */
	private boolean isSessionNew;
	
	/**
	 * List of jobs for session.
	 */
	private List<ProcessingInProgressData> jobs;
	
	public JobsForSession()
	{
		
	}
	
	public JobsForSession(boolean isSessionNew,
						  List<ProcessingInProgressData> jobs)
	{
		this.setSessionNew(isSessionNew);
		this.jobs = jobs;
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

	/**
	 * Retrieves information whether new session has been established.
	 * @return information whether new session has been established
	 */
	public boolean isSessionNew() {
		return isSessionNew;
	}

	/**
	 * Sets information whether new session has been established.
	 * @param isSessionNew information whether new session has been established
	 */
	public void setSessionNew(boolean isSessionNew) {
		this.isSessionNew = isSessionNew;
	}
	
}
