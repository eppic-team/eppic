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
import ch.systemsx.sybit.crkwebui.shared.model.RunJobData;

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

	private MainController mainController;

	public ServiceControllerImpl(MainController mainController) {
		this.mainController = mainController;
	}

	@Override
	public void loadSettings() {
		crkWebService.loadSettings(new GetSettingsCallback(mainController));
	}

	@Override
	public void getResultsOfProcessing(String jobId) {
		crkWebService.getResultsOfProcessing(jobId, 
				new GetResultsOfProcessingCallback(mainController, jobId));
	}
	
	@Override
	public void getInterfaceResidues(String jobId, int interfaceUid, int interfaceId) {
		crkWebService.getInterfaceResidues(interfaceUid,
				new GetInterfaceResiduesCallback(mainController, jobId, interfaceId));
	}
	
	@Override
	public void getJobsForCurrentSession() {
		crkWebService.getJobsForCurrentSession(new GetJobsForCurrentSession(
				mainController));
	}
	
	@Override
	public void runJob(RunJobData runJobData) {
		crkWebService.runJob(runJobData, new RunJobCallback(mainController));
	}

	@Override
	public void stopJob(String jobToStop) {
		crkWebService.stopJob(jobToStop, new StopJobCallback(mainController, jobToStop));
	}
	
	@Override
	public void deleteJob(String jobToDelete) {
		crkWebService.deleteJob(jobToDelete, new DeleteJobCallback(mainController, jobToDelete));
	}

	@Override
	public void untieJobsFromSession() {
		crkWebService.untieJobsFromSession(new UntieJobsFromSessionCallback(
				mainController));
	}
	
	@Override
	public void getCurrentStatusData(String jobId) 
	{
		crkWebService.getResultsOfProcessing(jobId, new GetCurrentStatusDataCallback(mainController, jobId));
	}

	@Override
	public void getAllResidues(String jobId, int pdbScoreUid) {
		crkWebService.getAllResidues(pdbScoreUid,
				new GetAllResiduesCallback(mainController, jobId));
	}
}
