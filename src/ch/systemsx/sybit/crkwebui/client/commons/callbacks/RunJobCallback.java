package ch.systemsx.sybit.crkwebui.client.commons.callbacks;

import ch.systemsx.sybit.crkwebui.client.commons.appdata.AppPropertiesManager;
import ch.systemsx.sybit.crkwebui.client.commons.appdata.ApplicationContext;
import ch.systemsx.sybit.crkwebui.client.commons.events.ShowErrorEvent;
import ch.systemsx.sybit.crkwebui.client.commons.events.UpdateStatusLabelEvent;
import ch.systemsx.sybit.crkwebui.client.commons.gui.data.StatusMessageType;
import ch.systemsx.sybit.crkwebui.client.commons.managers.EventBusManager;

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
			EventBusManager.EVENT_BUS.fireEvent(new UpdateStatusLabelEvent(AppPropertiesManager.CONSTANTS.callback_run_job_error() + " - incorrect type", 
																		   StatusMessageType.INTERNAL_ERROR));
		}
	}
}
