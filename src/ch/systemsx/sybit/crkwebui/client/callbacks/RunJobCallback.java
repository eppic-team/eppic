package ch.systemsx.sybit.crkwebui.client.callbacks;

import ch.systemsx.sybit.crkwebui.client.controllers.MainController;

import com.google.gwt.user.client.History;
import com.google.gwt.user.client.rpc.AsyncCallback;

public class RunJobCallback implements AsyncCallback 
{
	private MainController mainController;
	private String jobId;

	public RunJobCallback(MainController mainController,
						  String jobId) 
	{
		this.mainController = mainController;
		this.jobId = jobId;
	}

	@Override
	public void onFailure(Throwable caught) 
	{
		mainController.showError("Error during getting data from server");
	}

	@Override
	public void onSuccess(Object result) 
	{
		mainController.getJobsForCurrentSession();
		mainController.setSelectedJobId(jobId);
		mainController.displayResults();
		History.newItem("id/" + jobId);
	}
}
