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
	public abstract void getResultsOfProcessing(String jobId);
	
	/**
	 * Retrieve list of all jobs for current session id
	 * @param jobId job id
	 */
	public abstract void getCurrentStatusData(String jobId);
	
	/**
	 * Retrieve list of all jobs for current session id
	 */
	public abstract void getJobsForCurrentSession();

	/**
	 * Retrieve residues information for selected interface
	 * @param jobId selected job id
	 * @param interfaceId selected interface id
	 */
	public abstract void getInterfaceResidues(String jobId, int interfaceId);
	
	/**
	 * Stop the processing of the selected job
	 * @param id selected job id which is supposed to be stopped on the server
	 */
	public abstract void killJob(String jobId);

	/**
	 * Untie all the jobs which are attached to the current session
	 */
	public abstract void untieJobsFromSession();

	

	

}
