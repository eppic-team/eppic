package ch.systemsx.sybit.crkwebui.client.callbacks;

import java.util.List;

import ch.systemsx.sybit.crkwebui.client.controllers.AppPropertiesManager;
import ch.systemsx.sybit.crkwebui.client.controllers.ApplicationContext;
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
 * Callback used to handle the response from the server when trying to retrieve the list of jobs for the current session.
 * @author srebniak_a
 *
 */
public class GetJobsForCurrentSession implements AsyncCallback<JobsForSession> 
{
	public GetJobsForCurrentSession()
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
			if(!result.getSessionId().equals(ApplicationContext.getSettings().getSessionId()))
			{
				EventBusManager.EVENT_BUS.fireEvent(new ShowErrorEvent(AppPropertiesManager.CONSTANTS.callback_get_jobs_for_current_session_changed()));
				ApplicationContext.getSettings().setSessionId(result.getSessionId());
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
