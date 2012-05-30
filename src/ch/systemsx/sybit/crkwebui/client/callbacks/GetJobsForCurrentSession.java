package ch.systemsx.sybit.crkwebui.client.callbacks;

import java.util.List;

import ch.systemsx.sybit.crkwebui.client.controllers.AppPropertiesManager;
import ch.systemsx.sybit.crkwebui.client.controllers.MainController;
import ch.systemsx.sybit.crkwebui.shared.model.ProcessingInProgressData;

import com.google.gwt.user.client.rpc.AsyncCallback;

/**
 * Callback used to handle the response from the server when trying to retrieve the list of jobs for the current session.
 * @author srebniak_a
 *
 */
public class GetJobsForCurrentSession implements AsyncCallback<List<ProcessingInProgressData>> 
{
	private MainController mainController;
	
	public GetJobsForCurrentSession(MainController mainController)
	{
		this.mainController = mainController;
	}
	
	@Override
	public void onFailure(Throwable caught) 
	{
		mainController.updateStatusLabel(AppPropertiesManager.CONSTANTS.callback_get_jobs_for_current_session_error(), true);
		mainController.stopMyJobsAutoRefresh();
	}

	@Override
	public void onSuccess(List<ProcessingInProgressData> result) 
	{
		if(result != null)
		{
			mainController.setJobs(result);
		}
		else
		{
			mainController.updateStatusLabel(AppPropertiesManager.CONSTANTS.callback_get_jobs_for_current_session_error() + " - incorrect type", true);
		}
		
		mainController.setCanRefreshMyJobs();
	}
}
