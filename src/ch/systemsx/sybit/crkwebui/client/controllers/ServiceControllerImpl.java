package ch.systemsx.sybit.crkwebui.client.controllers;

import ch.systemsx.sybit.crkwebui.client.CrkWebService;
import ch.systemsx.sybit.crkwebui.client.CrkWebServiceAsync;
import ch.systemsx.sybit.crkwebui.client.callbacks.DeleteJobCallbackXsrf;
import ch.systemsx.sybit.crkwebui.client.callbacks.GetAllResiduesCallback;
import ch.systemsx.sybit.crkwebui.client.callbacks.GetCurrentStatusDataCallback;
import ch.systemsx.sybit.crkwebui.client.callbacks.GetInterfaceResiduesCallback;
import ch.systemsx.sybit.crkwebui.client.callbacks.GetResultsOfProcessingCallback;
import ch.systemsx.sybit.crkwebui.client.callbacks.GetSettingsCallback;
import ch.systemsx.sybit.crkwebui.client.callbacks.GetJobsForCurrentSessionCallbackXsrf;
import ch.systemsx.sybit.crkwebui.client.callbacks.RunJobCallbackXsrf;
import ch.systemsx.sybit.crkwebui.client.callbacks.StopJobCallbackXsrf;
import ch.systemsx.sybit.crkwebui.client.callbacks.UntieJobsFromSessionCallbackXsrf;
import ch.systemsx.sybit.crkwebui.client.events.BeforeJobDeletedEvent;
import ch.systemsx.sybit.crkwebui.shared.model.RunJobData;

import com.extjs.gxt.ui.client.GXT;
import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.rpc.XsrfTokenServiceAsync;

/**
 * Implementation of service manager.
 * @author srebniak_a
 *
 */
public class ServiceControllerImpl implements ServiceController 
{
	/**
	 * Create a remote service proxy to talk to the server-side service.
	 */
	private final CrkWebServiceAsync crkWebService = GWT
			.create(CrkWebService.class);
	
	private final XsrfTokenServiceAsync xsrfTokenService = XsrfTokenServiceProvider.getXsrfTokenService();

	public ServiceControllerImpl() 
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
	public void deleteJob(String jobToDelete) 
	{
		EventBusManager.EVENT_BUS.fireEvent(new BeforeJobDeletedEvent(jobToDelete));
		xsrfTokenService.getNewXsrfToken(new DeleteJobCallbackXsrf(crkWebService, jobToDelete));
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
	public void getAllResidues(String jobId, int pdbScoreUid) {
		if(!GXT.isIE8)
		{
			crkWebService.getAllResidues(pdbScoreUid,new GetAllResiduesCallback(jobId));
		}
	}
}
