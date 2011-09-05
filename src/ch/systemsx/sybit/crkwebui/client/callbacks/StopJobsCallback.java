package ch.systemsx.sybit.crkwebui.client.callbacks;

import ch.systemsx.sybit.crkwebui.client.controllers.MainController;

import com.google.gwt.user.client.rpc.AsyncCallback;

/**
 * This is the callback used to handle the response from the server when trying to stop the jobs
 * @author srebniak_a
 *
 */
public class StopJobsCallback implements AsyncCallback<String> 
{
	private MainController mainController;
	private String jobToStop;

	public StopJobsCallback(MainController mainController,
							String jobToStop) {
		this.mainController = mainController;
		this.jobToStop = jobToStop;
	}

	@Override
	public void onFailure(Throwable caught) 
	{
		mainController.updateStatusLabel(MainController.CONSTANTS.callback_kill_job_error() + " " + jobToStop, true);
	}

	@Override
	public void onSuccess(String result) 
	{
		if (result != null)
		{
			mainController.showMessage("Job stopping", result);
			mainController.getJobsForCurrentSession();
			
			if(jobToStop.equals(mainController.getSelectedJobId()))
			{
				mainController.getCurrentStatusData();
			}
		} 
		else 
		{
			mainController.updateStatusLabel(MainController.CONSTANTS.callback_kill_job_error() + " " + jobToStop, true);
		}
	}

}
