package ch.systemsx.sybit.crkwebui.client;

import java.util.HashMap;
import java.util.List;

import ch.systemsx.sybit.crkwebui.shared.CrkWebException;
import ch.systemsx.sybit.crkwebui.shared.model.ApplicationSettings;
import ch.systemsx.sybit.crkwebui.shared.model.InterfaceResidueItem;
import ch.systemsx.sybit.crkwebui.shared.model.InterfaceResiduesItemsList;
import ch.systemsx.sybit.crkwebui.shared.model.ProcessingData;
import ch.systemsx.sybit.crkwebui.shared.model.ProcessingInProgressData;
import ch.systemsx.sybit.crkwebui.shared.model.RunJobData;

import com.google.gwt.user.client.rpc.RemoteService;
import com.google.gwt.user.client.rpc.RemoteServiceRelativePath;

/**
 * The client side stub for the RPC service.
 * 
 * @author srebniak_a
 */
@RemoteServiceRelativePath("crk")
public interface CrkWebService extends RemoteService
{
	/**
	 * Load initial setttings
	 * @return initial setttings
	 * @throws CrkWebException
	 */
	public ApplicationSettings loadSettings() throws CrkWebException;

	/**
	 * Submit the job to the server
	 * @param runJobData input parameters
	 * @return results of job submission
	 * @throws CrkWebException
	 */
	public String runJob(RunJobData runJobData) throws CrkWebException;
	
	/**
	 * Retrieve results of processing for selected job id - the results type depends on the status of the job on the server
	 * @param jobId identitifier of the job
	 * @return status data for the selected job
	 * @throws CrkWebException
	 */
	public ProcessingData getResultsOfProcessing(String jobId, boolean debug) throws CrkWebException;
	
	/**
	 * Retrieve list of all jobs for current session id
	 * @return list of jobs attached to the current session
	 * @throws CrkWebException
	 */
	public List<ProcessingInProgressData> getJobsForCurrentSession() throws CrkWebException;

	/**
	 * Retrieve residues information for selected interface
	 * @param interfaceUid selected interface uid
	 * @return residues information
	 * @throws CrkWebException
	 */
	public HashMap<Integer, List<InterfaceResidueItem>> getInterfaceResidues(int interfaceUid) throws CrkWebException;
	
	/**
	 * Killing selected job
	 * @param jobsToStop id of the job which is going to be removed
	 * @return result of stopping
	 * @throws CrkWebException
	 */
	public String stopJob(String jobToStop) throws CrkWebException;
	
	/**
	 * Untie specified job id with the session of the current user
	 * @param jobsToDelete job for which session id is to be untied
	 * @return result of deleting
	 * @throws CrkWebException
	 */
	public String deleteJob(String jobToDelete) throws CrkWebException;
	
	/**
	 * Untie all the jobs which are attached to the current session
	 * @throws CrkWebException
	 */
	public void untieJobsFromSession() throws CrkWebException;

	/**
	 * Retrieve all the residues for all interfaces for specified job
	 * @param pdbScoreUid unique id of selected results item
	 * @return all residues for job
	 * @throws CrkWebException
	 */
	public InterfaceResiduesItemsList getAllResidues(int pdbScoreUid) throws CrkWebException;
}
