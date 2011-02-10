package ch.systemsx.sybit.crkwebui.client.controllers;

import ch.systemsx.sybit.crkwebui.client.CrkWebService;
import ch.systemsx.sybit.crkwebui.client.CrkWebServiceAsync;
import ch.systemsx.sybit.crkwebui.client.callbacks.CheckIfDataProcessedCallback;
import ch.systemsx.sybit.crkwebui.client.callbacks.GetJobsForCurrentSession;
import ch.systemsx.sybit.crkwebui.client.callbacks.GetResultsDataCallback;
import ch.systemsx.sybit.crkwebui.client.callbacks.GetSettingsCallback;
import ch.systemsx.sybit.crkwebui.client.callbacks.GetStatusDataCallback;
import ch.systemsx.sybit.crkwebui.client.callbacks.KillJobCallback;
import ch.systemsx.sybit.crkwebui.client.callbacks.RunJobCallback;
import ch.systemsx.sybit.crkwebui.client.callbacks.TestCallback;
import ch.systemsx.sybit.crkwebui.client.callbacks.UntieJobsFromSessionCallback;
import ch.systemsx.sybit.crkwebui.shared.model.RunJobData;

import com.google.gwt.core.client.GWT;

public class ServiceControllerImpl implements ServiceController 
{
	/**
	 * The message displayed to the user when the server cannot be reached or
	 * returns an error.
	 */
	private static final String SERVER_ERROR = "An error occurred while "
			+ "attempting to contact the server. Please check your network "
			+ "connection and try again.";

	/**
	 * Create a remote service proxy to talk to the server-side service.
	 */
	private final CrkWebServiceAsync crkWebService = GWT
			.create(CrkWebService.class);
	
	private MainController mainController;
	
	public ServiceControllerImpl(MainController mainController)
	{
		this.mainController = mainController;
	}
	
	public void test(String testValue) 
	{
		crkWebService.test(testValue, new TestCallback(mainController));
	}
	
	public void checkIfDataProcessed(String selectedId) 
	{
		crkWebService.checkIfDataProcessed(selectedId, new CheckIfDataProcessedCallback(mainController, selectedId));
	}
	
	public void getStatusData(String selectedId)
	{
		crkWebService.getStatusData(selectedId, new GetStatusDataCallback(mainController));
	}
	
	public void getResultData(String selectedId)
	{
		crkWebService.getResultData(selectedId, new GetResultsDataCallback(mainController, selectedId));
	}

	public void killJob(String selectedId)
	{
		crkWebService.killJob(selectedId, new KillJobCallback(mainController, selectedId));
	}
	
	public void getJobsForCurrentSession()
	{
		crkWebService.getJobsForCurrentSession(new GetJobsForCurrentSession(mainController));
	}

	public void untieJobsFromSession()
	{
		crkWebService.untieJobsFromSession(new UntieJobsFromSessionCallback(mainController));
	}
	
	public void loadSettings()
	{
		crkWebService.getSettings(new GetSettingsCallback(mainController));
	}

	public void runJob(RunJobData runJobData) 
	{
		crkWebService.runJob(runJobData, new RunJobCallback(mainController));
	}
}
