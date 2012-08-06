package ch.systemsx.sybit.crkwebui.client.callbacks;

import ch.systemsx.sybit.crkwebui.client.controllers.AppPropertiesManager;
import ch.systemsx.sybit.crkwebui.client.controllers.ApplicationContext;
import ch.systemsx.sybit.crkwebui.client.controllers.CrkWebServiceProvider;
import ch.systemsx.sybit.crkwebui.client.controllers.EventBusManager;
import ch.systemsx.sybit.crkwebui.client.events.ShowMessageEvent;
import ch.systemsx.sybit.crkwebui.client.events.UpdateStatusLabelEvent;

import com.google.gwt.user.client.rpc.AsyncCallback;

/**
 * Callback used to handle the response from the server when trying to stop the job.
 * @author srebniak_a
 *
 */
public class StopJobCallback implements AsyncCallback<String> 
{
	private String jobToStop;

	public StopJobCallback(String jobToStop) 
	{
		this.jobToStop = jobToStop;
	}

	@Override
	public void onFailure(Throwable caught) 
	{
		EventBusManager.EVENT_BUS.fireEvent(new UpdateStatusLabelEvent(AppPropertiesManager.CONSTANTS.callback_stop_job_error() + " " + jobToStop, true));
	}

	@Override
	public void onSuccess(String result) 
	{
		if (result != null)
		{
			EventBusManager.EVENT_BUS.fireEvent(new ShowMessageEvent(AppPropertiesManager.CONSTANTS.callback_stop_job_message(), result));
			CrkWebServiceProvider.getServiceController().getJobsForCurrentSession();
			
			if(jobToStop.equals(ApplicationContext.getSelectedJobId()))
			{
				CrkWebServiceProvider.getServiceController().getCurrentStatusData(jobToStop);
			}
		} 
		else 
		{
			EventBusManager.EVENT_BUS.fireEvent(new UpdateStatusLabelEvent(AppPropertiesManager.CONSTANTS.callback_stop_job_error() + " " + jobToStop, true));
		}
	}

}
