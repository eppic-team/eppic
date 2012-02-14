package ch.systemsx.sybit.crkwebui.client.callbacks;

import ch.systemsx.sybit.crkwebui.client.controllers.MainController;

import com.google.gwt.user.client.rpc.AsyncCallback;

/**
 * This is the callback used to handle the response from the server when trying to stop the job
 * @author srebniak_a
 *
 */
public class StopJobCallback implements AsyncCallback<String> 
{
	private MainController mainController;
	private String jobToStop;
	private boolean debug;

	public StopJobCallback(MainController mainController,
							String jobToStop,
							boolean debug) 
	{
		this.mainController = mainController;
		this.jobToStop = jobToStop;
		this.debug = debug;
	}

	@Override
	public void onFailure(Throwable caught) 
	{
		mainController.updateStatusLabel(MainController.CONSTANTS.callback_stop_job_error() + " " + jobToStop, true);
	}

	@Override
	public void onSuccess(String result) 
	{
		if (result != null)
		{
			mainController.showMessage(MainController.CONSTANTS.callback_stop_job_message(), result);
			mainController.getJobsForCurrentSession();
			
			if(jobToStop.equals(mainController.getSelectedJobId()))
			{
				mainController.getCurrentStatusData(debug);
			}
		} 
		else 
		{
			mainController.updateStatusLabel(MainController.CONSTANTS.callback_stop_job_error() + " " + jobToStop, true);
		}
	}

}
