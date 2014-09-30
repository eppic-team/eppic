package ch.systemsx.sybit.crkwebui.client.commons.callbacks;

import ch.systemsx.sybit.crkwebui.client.commons.appdata.AppPropertiesManager;
import ch.systemsx.sybit.crkwebui.client.commons.appdata.ApplicationContext;
import ch.systemsx.sybit.crkwebui.client.commons.events.ShowMessageEvent;
import ch.systemsx.sybit.crkwebui.client.commons.events.UpdateStatusLabelEvent;
import ch.systemsx.sybit.crkwebui.client.commons.gui.data.StatusMessageType;
import ch.systemsx.sybit.crkwebui.client.commons.managers.EventBusManager;
import ch.systemsx.sybit.crkwebui.client.commons.services.eppic.CrkWebServiceProvider;

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
		EventBusManager.EVENT_BUS.fireEvent(new UpdateStatusLabelEvent(AppPropertiesManager.CONSTANTS.callback_stop_job_error() + " " + jobToStop, caught));
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
			EventBusManager.EVENT_BUS.fireEvent(new UpdateStatusLabelEvent(AppPropertiesManager.CONSTANTS.callback_stop_job_error() + " " + jobToStop, 
																		   StatusMessageType.INTERNAL_ERROR));
		}
	}

}
