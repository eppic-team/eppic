package ch.systemsx.sybit.crkwebui.client.callbacks;

import ch.systemsx.sybit.crkwebui.client.controllers.MainController;

import com.google.gwt.user.client.rpc.AsyncCallback;

/**
 * This is the callback used to handle the response from the server when trying to stop the job
 * @author srebniak_a
 *
 */
public class KillJobCallback implements AsyncCallback<String> 
{
	private MainController mainController;

	public KillJobCallback(MainController mainController) {
		this.mainController = mainController;
	}

	@Override
	public void onFailure(Throwable caught) 
	{
		mainController.updateStatusLabel(MainController.CONSTANTS.callback_kill_job_error() + " " + mainController.getSelectedJobId(), true);
//		mainController.showError("Error during getting data from server: " + caught.getMessage());
	}

	@Override
	public void onSuccess(String result) 
	{
		if (result != null)
		{
			mainController.showMessage("Job stopping", result);
			mainController.getJobsForCurrentSession();
			mainController.getCurrentStatusData();
		} 
		else 
		{
			mainController.updateStatusLabel(MainController.CONSTANTS.callback_kill_job_error() + " " + mainController.getSelectedJobId(), true);
		}
	}

}
