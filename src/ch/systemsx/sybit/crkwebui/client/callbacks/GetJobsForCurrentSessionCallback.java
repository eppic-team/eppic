package ch.systemsx.sybit.crkwebui.client.callbacks;

import ch.systemsx.sybit.crkwebui.client.controllers.AppPropertiesManager;
import ch.systemsx.sybit.crkwebui.client.controllers.EventBusManager;
import ch.systemsx.sybit.crkwebui.client.data.StatusMessageType;
import ch.systemsx.sybit.crkwebui.client.events.JobListRetrievedEvent;
import ch.systemsx.sybit.crkwebui.client.events.ShowErrorEvent;
import ch.systemsx.sybit.crkwebui.client.events.StopJobsListAutoRefreshEvent;
import ch.systemsx.sybit.crkwebui.client.events.UpdateStatusLabelEvent;
import ch.systemsx.sybit.crkwebui.shared.model.JobsForSession;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.rpc.IncompatibleRemoteServiceException;

/**
 * Callback used to handle the response from the server when trying to retrieve list of jobs for current session.
 * @author srebniak_a
 *
 */
public class GetJobsForCurrentSessionCallback implements AsyncCallback<JobsForSession> 
{
	public GetJobsForCurrentSessionCallback()
	{
		
	}
	
	@Override
	public void onFailure(Throwable caught) 
	{
		EventBusManager.EVENT_BUS.fireEvent(new UpdateStatusLabelEvent(AppPropertiesManager.CONSTANTS.callback_get_jobs_for_current_session_error(), caught));
		
		if(caught instanceof IncompatibleRemoteServiceException)
		{
			EventBusManager.EVENT_BUS.fireEvent(new StopJobsListAutoRefreshEvent());
		}
	}

	@Override
	public void onSuccess(JobsForSession result) 
	{
		if(result != null)
		{
			if(result.isSessionNew())
			{
				EventBusManager.EVENT_BUS.fireEvent(new ShowErrorEvent(AppPropertiesManager.CONSTANTS.callback_get_jobs_for_current_session_changed()));
			}
			else
			{
				EventBusManager.EVENT_BUS.fireEvent(new UpdateStatusLabelEvent(AppPropertiesManager.CONSTANTS.callback_get_jobs_for_current_session_ok(), 
																			   StatusMessageType.NO_ERROR));
			}
			
			EventBusManager.EVENT_BUS.fireEvent(new JobListRetrievedEvent(result.getJobs()));
		}
		else
		{
			EventBusManager.EVENT_BUS.fireEvent(new UpdateStatusLabelEvent(AppPropertiesManager.CONSTANTS.callback_get_jobs_for_current_session_error() + " - incorrect type", 
																		   StatusMessageType.INTERNAL_ERROR));
		}
	}
}
