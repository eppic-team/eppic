package ch.systemsx.sybit.crkwebui.client.controllers;

import java.util.List;

import ch.systemsx.sybit.crkwebui.client.CrkWebService;
import ch.systemsx.sybit.crkwebui.client.CrkWebServiceAsync;
import ch.systemsx.sybit.crkwebui.client.callbacks.DeleteJobsCallback;
import ch.systemsx.sybit.crkwebui.client.callbacks.GetAllResiduesCallback;
import ch.systemsx.sybit.crkwebui.client.callbacks.GetCurrentStatusDataCallback;
import ch.systemsx.sybit.crkwebui.client.callbacks.GetInterfaceResiduesCallback;
import ch.systemsx.sybit.crkwebui.client.callbacks.GetJobsForCurrentSession;
import ch.systemsx.sybit.crkwebui.client.callbacks.GetResultsOfProcessingCallback;
import ch.systemsx.sybit.crkwebui.client.callbacks.GetSettingsCallback;
import ch.systemsx.sybit.crkwebui.client.callbacks.RunJobCallback;
import ch.systemsx.sybit.crkwebui.client.callbacks.StopJobsCallback;
import ch.systemsx.sybit.crkwebui.client.callbacks.UntieJobsFromSessionCallback;
import ch.systemsx.sybit.crkwebui.shared.model.RunJobData;

import com.google.gwt.core.client.GWT;

/**
 * 
 * @author srebniak_a
 *
 */
public class ServiceControllerImpl implements ServiceController 
{
//	/**
//	 * The message displayed to the user when the server cannot be reached or
//	 * returns an error.
//	 */
//	private static final String SERVER_ERROR = "An error occurred while "
//			+ "attempting to contact the server. Please check your network "
//			+ "connection and try again.";

	/**
	 * Create a remote service proxy to talk to the server-side service.
	 */
	private final CrkWebServiceAsync crkWebService = GWT
			.create(CrkWebService.class);

	private MainController mainController;

	public ServiceControllerImpl(MainController mainController) {
		this.mainController = mainController;
	}

	public void loadSettings() {
		crkWebService.loadSettings(new GetSettingsCallback(mainController));
	}

	public void getResultsOfProcessing(String jobId) {
		crkWebService.getResultsOfProcessing(jobId,
				new GetResultsOfProcessingCallback(mainController, jobId));
	}
	
	
	public void getInterfaceResidues(String jobId, int interfaceId) {
		crkWebService.getInterfaceResidues(jobId, interfaceId,
				new GetInterfaceResiduesCallback(mainController));
	}
	
	public void getJobsForCurrentSession() {
		crkWebService.getJobsForCurrentSession(new GetJobsForCurrentSession(
				mainController));
	}
	
	public void runJob(RunJobData runJobData) {
		crkWebService.runJob(runJobData, new RunJobCallback(mainController));
	}

	public void stopJob(String jobToStop) {
		crkWebService.stopJob(jobToStop, new StopJobsCallback(mainController, jobToStop));
	}
	
	public void deleteJob(String jobToDelete) {
		crkWebService.deleteJob(jobToDelete, new DeleteJobsCallback(mainController, jobToDelete));
	}

	public void untieJobsFromSession() {
		crkWebService.untieJobsFromSession(new UntieJobsFromSessionCallback(
				mainController));
	}
	
	public void getCurrentStatusData(String jobId) 
	{
		crkWebService.getResultsOfProcessing(jobId,
				new GetCurrentStatusDataCallback(mainController, jobId));
	}

	@Override
	public void getAllResidues(String jobId, List<Integer> interfaceIds) {
		crkWebService.getAllResidues(jobId, interfaceIds,
				new GetAllResiduesCallback(mainController, jobId));
	}
}
