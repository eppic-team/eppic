package ch.systemsx.sybit.crkwebui.client.callbacks;

import ch.systemsx.sybit.crkwebui.client.controllers.AppPropertiesManager;
import ch.systemsx.sybit.crkwebui.client.controllers.ApplicationContext;
import ch.systemsx.sybit.crkwebui.client.controllers.EventBusManager;
import ch.systemsx.sybit.crkwebui.client.events.ShowErrorEvent;
import ch.systemsx.sybit.crkwebui.client.events.UpdateStatusLabelEvent;

import com.google.gwt.user.client.History;
import com.google.gwt.user.client.rpc.AsyncCallback;

/**
 * Callback used to handle response when trying to run new job.
 * @author srebniak_a
 *
 */
public class RunJobCallback implements AsyncCallback<String> 
{
	public RunJobCallback() 
	{
		
	}

	@Override
	public void onFailure(Throwable caught) 
	{
		EventBusManager.EVENT_BUS.fireEvent(new ShowErrorEvent("Error during running job: " + caught.getMessage()));
	}

	@Override
	public void onSuccess(String result) 
	{
		if(result != null) 
		{
			String jobId = result;
			ApplicationContext.setSelectedJobId(jobId);
			History.newItem("id/" + jobId);
		} 
		else 
		{
			EventBusManager.EVENT_BUS.fireEvent(new UpdateStatusLabelEvent(AppPropertiesManager.CONSTANTS.callback_run_job_error() + " - incorrect type", true));
		}
	}
}
