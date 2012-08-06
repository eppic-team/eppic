package ch.systemsx.sybit.crkwebui.client.callbacks;

import ch.systemsx.sybit.crkwebui.client.controllers.AppPropertiesManager;
import ch.systemsx.sybit.crkwebui.client.controllers.CrkWebServiceProvider;
import ch.systemsx.sybit.crkwebui.client.controllers.EventBusManager;
import ch.systemsx.sybit.crkwebui.client.events.ShowMessageEvent;
import ch.systemsx.sybit.crkwebui.client.events.UpdateStatusLabelEvent;

import com.google.gwt.user.client.rpc.AsyncCallback;

/**
 * Callback used to handle the response from the server when trying to remove the job.
 * @author srebniak_a
 *
 */
public class DeleteJobCallback implements AsyncCallback<String> 
{
	private String jobToRemove;

	public DeleteJobCallback(String jobToRemove)
	{
		this.jobToRemove = jobToRemove;
	}

	@Override
	public void onFailure(Throwable caught) 
	{
		EventBusManager.EVENT_BUS.fireEvent(new UpdateStatusLabelEvent(AppPropertiesManager.CONSTANTS.callback_delete_job_error(), true));
	}

	@Override
	public void onSuccess(String result) 
	{
		if (result != null)
		{
			EventBusManager.EVENT_BUS.fireEvent(new ShowMessageEvent("Job deleting", result));
			CrkWebServiceProvider.getServiceController().getJobsForCurrentSession();
		} 
		else 
		{
			EventBusManager.EVENT_BUS.fireEvent(new UpdateStatusLabelEvent(AppPropertiesManager.CONSTANTS.callback_delete_job_error() + " " + jobToRemove, true));
		}
	}

}
