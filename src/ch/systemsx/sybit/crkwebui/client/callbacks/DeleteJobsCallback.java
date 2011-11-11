package ch.systemsx.sybit.crkwebui.client.callbacks;

import ch.systemsx.sybit.crkwebui.client.controllers.MainController;

import com.google.gwt.user.client.History;
import com.google.gwt.user.client.rpc.AsyncCallback;

/**
 * This is the callback used to handle the response from the server when trying to stop the jobs
 * @author srebniak_a
 *
 */
public class DeleteJobsCallback implements AsyncCallback<String> 
{
	private MainController mainController;
	private String jobToRemove;

	public DeleteJobsCallback(MainController mainController,
							  String jobToRemove)
	{
		this.mainController = mainController;
		this.jobToRemove = jobToRemove;
	}

	@Override
	public void onFailure(Throwable caught) 
	{
		mainController.updateStatusLabel(MainController.CONSTANTS.callback_kill_job_error(), true);
//		mainController.showError("Error during getting data from server: " + caught.getMessage());
	}

	@Override
	public void onSuccess(String result) 
	{
		if (result != null)
		{
			mainController.showMessage("Job deleting", result);
			mainController.getJobsForCurrentSession();
			
//			if(jobToRemove.equals(mainController.getSelectedJobId()))
//			{
//				History.newItem("");
//			}
		} 
		else 
		{
			mainController.updateStatusLabel(MainController.CONSTANTS.callback_kill_job_error() + " " + jobToRemove, true);
		}
	}

}
