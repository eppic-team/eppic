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
	 * Loads initial setttings.
	 * @return initial setttings
	 * @throws CrkWebException when an asynchronous call fails to complete normally 
	 */
	public ApplicationSettings loadSettings() throws CrkWebException;

	/**
	 * Submits the job to the server.
	 * @param runJobData input parameters
	 * @return results of job submission
	 * @throws CrkWebException when an asynchronous call fails to complete normally
	 */
	public String runJob(RunJobData runJobData) throws CrkWebException;
	
	/**
	 * Retrieves results of processing for selected job id - the results type depends on the status of the job on the server.
	 * @param jobId identifier of the job
	 * @return status data for the selected job
	 * @throws CrkWebException when an asynchronous call fails to complete normally
	 */
	public ProcessingData getResultsOfProcessing(String jobId, boolean debug) throws CrkWebException;
	
	/**
	 * Retrieves list of all jobs for current session id.
	 * @return list of jobs attached to the current session
	 * @throws CrkWebException when an asynchronous call fails to complete normally
	 */
	public List<ProcessingInProgressData> getJobsForCurrentSession() throws CrkWebException;

	/**
	 * Retrieves residues information for selected interface.
	 * @param interfaceUid selected interface uid
	 * @return residues information
	 * @throws CrkWebException when an asynchronous call fails to complete normally
	 */
	public HashMap<Integer, List<InterfaceResidueItem>> getInterfaceResidues(int interfaceUid) throws CrkWebException;
	
	/**
	 * Killing selected job.
	 * @param jobToStop id of the job to remove
	 * @return result of stopping
	 * @throws CrkWebException when an asynchronous call fails to complete normally
	 */
	public String stopJob(String jobToStop) throws CrkWebException;
	
	/**
	 * Unties specified job id from the session of the current user.
	 * @param jobToDelete job for which session id is going to be untied
	 * @return result of deleting
	 * @throws CrkWebException when an asynchronous call fails to complete normally
	 */
	public String deleteJob(String jobToDelete) throws CrkWebException;
	
	/**
	 * Unties all the jobs which are attached to the current session.
	 * @throws CrkWebException when an asynchronous call fails to complete normally
	 */
	public void untieJobsFromSession() throws CrkWebException;

	/**
	 * Retrieves all the residues for all interfaces for specified job.
	 * @param pdbScoreUid unique id of selected results item
	 * @return all residues for job
	 * @throws CrkWebException when an asynchronous call fails to complete normally
	 */
	public InterfaceResiduesItemsList getAllResidues(int pdbScoreUid) throws CrkWebException;
}
