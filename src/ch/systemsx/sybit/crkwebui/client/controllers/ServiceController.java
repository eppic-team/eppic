package ch.systemsx.sybit.crkwebui.client.controllers;

import ch.systemsx.sybit.crkwebui.shared.model.RunJobData;

/**
 * This is the controller used to contact with the server
 * @author srebniak_a
 *
 */
public interface ServiceController
{
	/**
	 * Load initial setttings
	 */
	public abstract void loadSettings();
	
	/**
	 * Submit the job to the server
	 * @param runJobData input parameters
	 */
	public abstract void runJob(RunJobData runJobData);
	
	/**
	 * Retrieve results of processing for selected job id - the results type depends on the status of the job on the server
	 * @param jobId job id
	 */
	public abstract void getResultsOfProcessing(String jobId, boolean debug);
	
	/**
	 * Retrieve the status data for the current jobs
	 * @param jobId job id
	 */
	public abstract void getCurrentStatusData(String jobId, boolean debug);
	
	/**
	 * Retrieve list of all jobs for current session id
	 */
	public abstract void getJobsForCurrentSession();

	/**
	 * Retrieve residues information for selected interface
	 * @param jobId selected job id
	 * @param interfaceId selected interface uid
	 */
	public abstract void getInterfaceResidues(String jobId, int interfaceUid, int interfaceId);
	
	/**
	 * Stop the execution of the specified running job
	 * @param jobsToStop list of jobs to stop
	 */
	public abstract void stopJob(String jobToStop, boolean debug);
	
	/**
	 * Untie specified job with the current session id
	 * @param jobsToStop list of jobs to remove
	 */
	public abstract void deleteJob(String jobToDelete);

	/**
	 * Untie all the jobs which are attached to the current session
	 */
	public abstract void untieJobsFromSession();

	public abstract void getAllResidues(String jobId, int pdbScoreUid);
}
