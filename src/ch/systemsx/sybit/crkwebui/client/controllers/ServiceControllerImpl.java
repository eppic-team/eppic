package ch.systemsx.sybit.crkwebui.client.controllers;

import ch.systemsx.sybit.crkwebui.client.CrkWebService;
import ch.systemsx.sybit.crkwebui.client.CrkWebServiceAsync;
import ch.systemsx.sybit.crkwebui.client.callbacks.DeleteJobCallback;
import ch.systemsx.sybit.crkwebui.client.callbacks.GetAllResiduesCallback;
import ch.systemsx.sybit.crkwebui.client.callbacks.GetCurrentStatusDataCallback;
import ch.systemsx.sybit.crkwebui.client.callbacks.GetInterfaceResiduesCallback;
import ch.systemsx.sybit.crkwebui.client.callbacks.GetJobsForCurrentSession;
import ch.systemsx.sybit.crkwebui.client.callbacks.GetResultsOfProcessingCallback;
import ch.systemsx.sybit.crkwebui.client.callbacks.GetSettingsCallback;
import ch.systemsx.sybit.crkwebui.client.callbacks.RunJobCallback;
import ch.systemsx.sybit.crkwebui.client.callbacks.StopJobCallback;
import ch.systemsx.sybit.crkwebui.client.callbacks.UntieJobsFromSessionCallback;
import ch.systemsx.sybit.crkwebui.client.events.BeforeJobDeletedEvent;
import ch.systemsx.sybit.crkwebui.shared.model.RunJobData;

import com.extjs.gxt.ui.client.GXT;
import com.google.gwt.core.client.GWT;

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

	public ServiceControllerImpl() {
		
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
		crkWebService.getJobsForCurrentSession(new GetJobsForCurrentSession(
				));
	}
	
	@Override
	public void runJob(RunJobData runJobData) {
		crkWebService.runJob(runJobData, new RunJobCallback());
	}

	@Override
	public void stopJob(String jobToStop) {
		crkWebService.stopJob(jobToStop, new StopJobCallback(jobToStop));
	}
	
	@Override
	public void deleteJob(String jobToDelete) 
	{
		EventBusManager.EVENT_BUS.fireEvent(new BeforeJobDeletedEvent(jobToDelete));
		crkWebService.deleteJob(jobToDelete, new DeleteJobCallback(jobToDelete));
	}

	@Override
	public void untieJobsFromSession() {
		crkWebService.untieJobsFromSession(new UntieJobsFromSessionCallback(
				));
	}
	
	@Override
	public void getCurrentStatusData(String jobId) 
	{
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
