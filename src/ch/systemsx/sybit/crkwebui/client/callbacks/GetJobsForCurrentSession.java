package ch.systemsx.sybit.crkwebui.client.callbacks;

import java.util.List;

import ch.systemsx.sybit.crkwebui.client.controllers.MainController;
import ch.systemsx.sybit.crkwebui.shared.model.ProcessingInProgressData;

import com.google.gwt.user.client.rpc.AsyncCallback;

/**
 * This is the callback used to handle the response from the server when trying to retrieve the list of jobs for the current session
 * @author srebniak_a
 *
 */
public class GetJobsForCurrentSession implements AsyncCallback 
{
	private MainController mainController;
	
	public GetJobsForCurrentSession(MainController mainController)
	{
		this.mainController = mainController;
	}
	
	@Override
	public void onFailure(Throwable caught) 
	{
		mainController.updateStatusLabel(MainController.CONSTANTS.callback_get_jobs_for_current_session_error(), true);
//		mainController.showError("Error during getting current jobs from server. " + caught.getMessage());
		mainController.stopMyJobsAutoRefresh();
	}

	@Override
	public void onSuccess(Object result) 
	{
		if(result != null)
		{
			List<ProcessingInProgressData> myJobs = (List<ProcessingInProgressData>)result;
			mainController.setJobs(myJobs);
		}
		else
		{
			mainController.updateStatusLabel(MainController.CONSTANTS.callback_get_jobs_for_current_session_error() + " - incorrect type", true);
//			mainController.showError("Error during getting current jobs for current session");
		}
	}
}
