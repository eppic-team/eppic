package ch.systemsx.sybit.crkwebui.client.callbacks;

import ch.systemsx.sybit.crkwebui.client.controllers.MainController;

import com.google.gwt.user.client.History;
import com.google.gwt.user.client.rpc.AsyncCallback;

/**
 * This is the callback to handle response when trying to run new job
 * @author srebniak_a
 *
 */
public class RunJobCallback implements AsyncCallback 
{
	private MainController mainController;

	public RunJobCallback(MainController mainController) 
	{
		this.mainController = mainController;
	}

	@Override
	public void onFailure(Throwable caught) 
	{
		mainController.updateStatusLabel("Error during getting run job data from server", true);
//		mainController.showError("Error during getting run job data from server");
	}

	@Override
	public void onSuccess(Object result) 
	{
		if ((result != null) && (result instanceof String)) 
		{
			String jobId = (String) result;
			mainController.setSelectedJobId(jobId);
			mainController.getJobsForCurrentSession();
//			mainController.displayResults();
			History.newItem("id/" + jobId);
		} 
		else 
		{
			mainController.updateStatusLabel("Error during running the job" , true);
//			mainController.showError("Error during stopping the job " + mainController.getSelectedJobId());
		}
	}
}
