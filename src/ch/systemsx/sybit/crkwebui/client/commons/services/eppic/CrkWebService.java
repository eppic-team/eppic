package ch.systemsx.sybit.crkwebui.client.commons.services.eppic;

import java.util.HashMap;
import java.util.List;

import ch.systemsx.sybit.crkwebui.shared.model.ApplicationSettings;
import ch.systemsx.sybit.crkwebui.shared.model.InterfaceResidueItem;
import ch.systemsx.sybit.crkwebui.shared.model.InterfaceResiduesItemsList;
import ch.systemsx.sybit.crkwebui.shared.model.JobsForSession;
import ch.systemsx.sybit.crkwebui.shared.model.ProcessingData;
import ch.systemsx.sybit.crkwebui.shared.model.RunJobData;

import com.google.gwt.user.client.rpc.RemoteService;
import com.google.gwt.user.client.rpc.RemoteServiceRelativePath;
import com.google.gwt.user.server.rpc.NoXsrfProtect;
import com.google.gwt.user.server.rpc.XsrfProtect;

/**
 * The client side stub for the RPC service.
 * 
 * @author srebniak_a
 */
@RemoteServiceRelativePath("crk")
public interface CrkWebService extends RemoteService
{
	/**
	 * Loads initial setttings.
	 * @return initial setttings
	 * @throws Exception when an asynchronous call fails to complete normally 
	 */
	@NoXsrfProtect
	public ApplicationSettings loadSettings() throws Exception;

	/**
	 * Submits the job to the server.
	 * @param runJobData input parameters
	 * @return results of job submission
	 * @throws Exception when an asynchronous call fails to complete normally
	 */
	@XsrfProtect
	public String runJob(RunJobData runJobData) throws Exception;
	
	/**
	 * Retrieves results of processing for selected job id - the results type depends on the status of the job on the server.
	 * @param jobId identifier of the job
	 * @return status data for the selected job
	 * @throws Exception when an asynchronous call fails to complete normally
	 */
	@NoXsrfProtect
	public ProcessingData getResultsOfProcessing(String jobId) throws Exception;
	
	/**
	 * Retrieves list of all jobs for current session id.
	 * @return list of jobs attached to the current session
	 * @throws Exception when an asynchronous call fails to complete normally
	 */
	@XsrfProtect
	public JobsForSession getJobsForCurrentSession() throws Exception;

	/**
	 * Retrieves residues information for selected interface.
	 * @param interfaceUid selected interface uid
	 * @return residues information
	 * @throws Exception when an asynchronous call fails to complete normally
	 */
	@NoXsrfProtect
	public HashMap<Integer, List<InterfaceResidueItem>> getInterfaceResidues(int interfaceUid) throws Exception;
	
	/**
	 * Kills selected job.
	 * @param jobToStop id of the job to remove
	 * @return result of stopping
	 * @throws Exception when an asynchronous call fails to complete normally
	 */
	@XsrfProtect
	public String stopJob(String jobToStop) throws Exception;
	
	/**
	 * Unties specified job id from the session of the current user.
	 * @param jobToDelete job for which session id is going to be untied
	 * @return result of deleting
	 * @throws Exception when an asynchronous call fails to complete normally
	 */
	@XsrfProtect
	public String deleteJob(String jobToDelete) throws Exception;
	
	/**
	 * Unties all the jobs which are attached to the current session.
	 * @throws Exception when an asynchronous call fails to complete normally
	 */
	@XsrfProtect
	public void untieJobsFromSession() throws Exception;

	/**
	 * Retrieves all the residues for all interfaces for specified job.
	 * @param jobId identifier of the job
	 * @return all residues for job
	 * @throws Exception when an asynchronous call fails to complete normally
	 */
	@NoXsrfProtect
	public InterfaceResiduesItemsList getAllResidues(String jobId) throws Exception;
}
