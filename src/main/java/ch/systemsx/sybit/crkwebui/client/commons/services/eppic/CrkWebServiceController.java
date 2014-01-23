package ch.systemsx.sybit.crkwebui.client.commons.services.eppic;

import java.util.List;

import ch.systemsx.sybit.crkwebui.shared.model.RunJobData;

/**
 * This is the service manager used to contact with the server
 * @author srebniak_a
 *
 */
public interface CrkWebServiceController
{
	/**
	 * Loads initial setttings.
	 */
	public abstract void loadSettings();
	
	/**
	 * Submits the job to the server.
	 * @param runJobData input parameters
	 */
	public abstract void runJob(RunJobData runJobData);
	
	/**
	 * Retrieves results of processing for selected job id - the results type depends on the status of the job on the server.
	 * @param jobId job id
	 */
	public abstract void getResultsOfProcessing(String jobId);
	
	/**
	 * Retrieves the status data for the current job.
	 * @param jobId job identifier
	 */
	public abstract void getCurrentStatusData(String jobId);
	
	/**
	 * Retrieves list of all jobs for current session id.
	 */
	public abstract void getJobsForCurrentSession();

	/**
	 * Retrieves residues information for selected interface.
	 * @param jobId selected job id
	 * @param interfaceUid selected interface uid
	 * @param interfaceId selected interface id
	 */
	public abstract void getInterfaceResidues(String jobId, int interfaceUid, int interfaceId);

	/**
	 * Retrieves all the residues for all interfaces for specified job.
	 * @param jobId selected job identifier
	 */
	public abstract void getAllResidues(String jobId);
	
	/**
	 * Stops the execution of specified running job.
	 * @param jobToStop job to stop
	 */
	public abstract void stopJob(String jobToStop);
	
	/**
	 * Deletes all jobs in the current my jobs panel
	 * @param List of jobsToDelete identifiers of the jobs to remove
	 */
	public abstract void deleteAllJobs(List<String> jobsToDelete);
	
	/**
	 * Unties specified job from the current session id.
	 * @param jobToDelete identifier of the job to remove
	 */
	public abstract void deleteJob(String jobToDelete);

	/**
	 * Unties all the jobs which are attached to the current session.
	 */
	public abstract void untieJobsFromSession();
}
