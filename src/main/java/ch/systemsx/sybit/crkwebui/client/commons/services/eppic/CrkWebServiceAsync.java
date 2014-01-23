package ch.systemsx.sybit.crkwebui.client.commons.services.eppic;

import java.util.HashMap;
import java.util.List;

import ch.systemsx.sybit.crkwebui.shared.model.ApplicationSettings;
import ch.systemsx.sybit.crkwebui.shared.model.InterfaceResidueItem;
import ch.systemsx.sybit.crkwebui.shared.model.InterfaceResiduesItemsList;
import ch.systemsx.sybit.crkwebui.shared.model.JobsForSession;
import ch.systemsx.sybit.crkwebui.shared.model.ProcessingData;
import ch.systemsx.sybit.crkwebui.shared.model.RunJobData;

import com.google.gwt.user.client.rpc.AsyncCallback;

/**
 * The async counterpart of <code>CrkWebService</code>.
 * 
 * @author srebniak_a
 */
public interface CrkWebServiceAsync 
{
	public void loadSettings(AsyncCallback<ApplicationSettings> callback);

	public void runJob(RunJobData runJobData, AsyncCallback<String> callback);
	
	public void getResultsOfProcessing(String jobId, AsyncCallback<ProcessingData> callback);
	
	public void getJobsForCurrentSession(AsyncCallback<JobsForSession> callback);
	
	public void getInterfaceResidues(int interfaceUid,
									 AsyncCallback<HashMap<Integer, List<InterfaceResidueItem>>> callback);
	
	public void stopJob(String jobToStop,
			AsyncCallback<String> stopJobsCallback);

	public void deleteJob(String jobToDelete,
						   AsyncCallback<String> deleteJobsCallback);

	public void untieJobsFromSession(AsyncCallback<Void> callback);

	public void getAllResidues(String jobId,
			AsyncCallback<InterfaceResiduesItemsList> getAllResiduesCallback);
}
