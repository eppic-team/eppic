package ch.systemsx.sybit.crkwebui.client.commons.services.eppic;

import java.util.List;

import ch.systemsx.sybit.crkwebui.client.commons.callbacks.DeleteJobCallbackXsrf;
import ch.systemsx.sybit.crkwebui.client.commons.callbacks.GetAllResiduesCallback;
import ch.systemsx.sybit.crkwebui.client.commons.callbacks.GetCurrentStatusDataCallback;
import ch.systemsx.sybit.crkwebui.client.commons.callbacks.GetInterfaceResiduesCallback;
import ch.systemsx.sybit.crkwebui.client.commons.callbacks.GetJobsForCurrentSessionCallbackXsrf;
import ch.systemsx.sybit.crkwebui.client.commons.callbacks.GetResultsOfProcessingCallback;
import ch.systemsx.sybit.crkwebui.client.commons.callbacks.GetSettingsCallback;
import ch.systemsx.sybit.crkwebui.client.commons.callbacks.RunJobCallbackXsrf;
import ch.systemsx.sybit.crkwebui.client.commons.callbacks.StopJobCallbackXsrf;
import ch.systemsx.sybit.crkwebui.client.commons.callbacks.UntieJobsFromSessionCallbackXsrf;
import ch.systemsx.sybit.crkwebui.client.commons.events.BeforeJobDeletedEvent;
import ch.systemsx.sybit.crkwebui.client.commons.managers.EventBusManager;
import ch.systemsx.sybit.crkwebui.client.commons.services.xsrf.XsrfTokenServiceProvider;
import ch.systemsx.sybit.crkwebui.shared.model.RunJobData;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.History;
import com.google.gwt.user.client.rpc.XsrfTokenServiceAsync;
import com.sencha.gxt.core.client.GXT;

/**
 * Implementation of service manager.
 * @author srebniak_a
 *
 */
public class CrkWebServiceControllerImpl implements CrkWebServiceController 
{
	/**
	 * Create a remote service proxy to talk to the server-side service.
	 */
	private final CrkWebServiceAsync crkWebService = GWT
			.create(CrkWebService.class);
	
	private final XsrfTokenServiceAsync xsrfTokenService = XsrfTokenServiceProvider.getXsrfTokenService();

	public CrkWebServiceControllerImpl() 
	{
		
	}

	@Override
	public void loadSettings() {
		crkWebService.loadSettings(new GetSettingsCallback());
	}

	@Override
	public void getResultsOfProcessing(String jobId) {
		crkWebService.getResultsOfProcessing(jobId, 
				new GetResultsOfProcessingCallback(jobId));
	}
	
	@Override
	public void getInterfaceResidues(String jobId, int interfaceUid, int interfaceId) {
		crkWebService.getInterfaceResidues(interfaceUid,
				new GetInterfaceResiduesCallback(jobId, interfaceId));
	}
	
	@Override
	public void getJobsForCurrentSession() {
		xsrfTokenService.getNewXsrfToken(new GetJobsForCurrentSessionCallbackXsrf(crkWebService));
	}
	
	@Override
	public void runJob(RunJobData runJobData) {
		xsrfTokenService.getNewXsrfToken(new RunJobCallbackXsrf(crkWebService, runJobData));
	}

	@Override
	public void stopJob(String jobToStop) {
		xsrfTokenService.getNewXsrfToken(new StopJobCallbackXsrf(crkWebService, jobToStop));
	}
	
	@Override
	public void deleteAllJobs(List<String> jobsToDelete) 
	{
		History.newItem("");
		for(String jobToDelete : jobsToDelete)
			xsrfTokenService.getNewXsrfToken(new DeleteJobCallbackXsrf(crkWebService, jobToDelete, true));
	}
	
	@Override
	public void deleteJob(String jobToDelete) 
	{
		EventBusManager.EVENT_BUS.fireEvent(new BeforeJobDeletedEvent(jobToDelete));
		xsrfTokenService.getNewXsrfToken(new DeleteJobCallbackXsrf(crkWebService, jobToDelete, false));
	}

	@Override
	public void untieJobsFromSession() {
		xsrfTokenService.getNewXsrfToken(new UntieJobsFromSessionCallbackXsrf(crkWebService));
	}
	
	@Override
	public void getCurrentStatusData(String jobId) {
		crkWebService.getResultsOfProcessing(jobId, new GetCurrentStatusDataCallback(jobId));
	}

	@Override
	public void getAllResidues(String jobId) {
		if(!GXT.isIE8())
		{
			crkWebService.getAllResidues(jobId,new GetAllResiduesCallback(jobId));
		}
	}
}
