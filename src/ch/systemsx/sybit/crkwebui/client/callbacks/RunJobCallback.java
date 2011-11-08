package ch.systemsx.sybit.crkwebui.client.callbacks;

import ch.systemsx.sybit.crkwebui.client.controllers.MainController;

import com.google.gwt.user.client.History;
import com.google.gwt.user.client.rpc.AsyncCallback;

/**
 * This is the callback to handle response when trying to run new job
 * @author srebniak_a
 *
 */
public class RunJobCallback implements AsyncCallback<String> 
{
	private MainController mainController;

	public RunJobCallback(MainController mainController) 
	{
		this.mainController = mainController;
	}

	@Override
	public void onFailure(Throwable caught) 
	{
		mainController.showError("Error during running job: " + caught.getMessage());
	}

	@Override
	public void onSuccess(String result) 
	{
		if(result != null) 
		{
			String jobId = result;
			mainController.setSelectedJobId(jobId);
//			mainController.getJobsForCurrentSession();
//			mainController.displayResults();
			History.newItem("id/" + jobId);
		} 
		else 
		{
			mainController.updateStatusLabel(MainController.CONSTANTS.callback_run_job_error() + " - incorrect type" , true);
		}
	}
}
