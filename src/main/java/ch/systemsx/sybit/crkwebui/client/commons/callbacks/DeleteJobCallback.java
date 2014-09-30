package ch.systemsx.sybit.crkwebui.client.commons.callbacks;

import ch.systemsx.sybit.crkwebui.client.commons.appdata.AppPropertiesManager;
import ch.systemsx.sybit.crkwebui.client.commons.events.UpdateStatusLabelEvent;
import ch.systemsx.sybit.crkwebui.client.commons.gui.data.StatusMessageType;
import ch.systemsx.sybit.crkwebui.client.commons.gui.info.PopUpInfo;
import ch.systemsx.sybit.crkwebui.client.commons.managers.EventBusManager;
import ch.systemsx.sybit.crkwebui.client.commons.services.eppic.CrkWebServiceProvider;

import com.google.gwt.user.client.rpc.AsyncCallback;

/**
 * Callback used to handle the response from the server when trying to remove the job.
 * @author srebniak_a
 *
 */
public class DeleteJobCallback implements AsyncCallback<String> 
{
	private String jobToRemove;
	
	private boolean isAllJobsDelete;

	public DeleteJobCallback(String jobToRemove, boolean isAllJobsDelete)
	{
		this.jobToRemove = jobToRemove;
		this.isAllJobsDelete =isAllJobsDelete;
	}

	@Override
	public void onFailure(Throwable caught) 
	{
		EventBusManager.EVENT_BUS.fireEvent(new UpdateStatusLabelEvent(AppPropertiesManager.CONSTANTS.callback_delete_job_error(), caught));
	}

	@Override
	public void onSuccess(String result) 
	{
		if (result != null)
		{
			if(!isAllJobsDelete) PopUpInfo.show("Job deleted", result);
			CrkWebServiceProvider.getServiceController().getJobsForCurrentSession();
		} 
		else 
		{
			EventBusManager.EVENT_BUS.fireEvent(new UpdateStatusLabelEvent(AppPropertiesManager.CONSTANTS.callback_delete_job_error() + " " + jobToRemove, 
																		   StatusMessageType.INTERNAL_ERROR));
		}
	}

}
