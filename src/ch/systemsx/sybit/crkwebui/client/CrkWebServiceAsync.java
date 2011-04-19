package ch.systemsx.sybit.crkwebui.client;

import java.util.HashMap;
import java.util.List;

import model.InterfaceResidueItem;
import model.ProcessingData;
import ch.systemsx.sybit.crkwebui.shared.model.ApplicationSettings;
import ch.systemsx.sybit.crkwebui.shared.model.ProcessingInProgressData;
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
	
	public void getJobsForCurrentSession(AsyncCallback<List<ProcessingInProgressData>> callback);
	
	public void getInterfaceResidues(String jobId, 
									 int interfaceId,
									 AsyncCallback<HashMap<Integer, List<InterfaceResidueItem>>> callback);

	public void killJob(String jobId, AsyncCallback<String> callback);

	public void untieJobsFromSession(AsyncCallback<Void> callback);
}
