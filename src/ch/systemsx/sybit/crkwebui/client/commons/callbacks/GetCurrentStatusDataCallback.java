package ch.systemsx.sybit.crkwebui.client.commons.callbacks;

import ch.systemsx.sybit.crkwebui.client.commons.appdata.AppPropertiesManager;
import ch.systemsx.sybit.crkwebui.client.commons.appdata.ApplicationContext;
import ch.systemsx.sybit.crkwebui.client.commons.events.RefreshStatusDataEvent;
import ch.systemsx.sybit.crkwebui.client.commons.events.ShowMessageEvent;
import ch.systemsx.sybit.crkwebui.client.commons.events.ShowNoResultsDataEvent;
import ch.systemsx.sybit.crkwebui.client.commons.events.ShowResultsDataEvent;
import ch.systemsx.sybit.crkwebui.client.commons.events.UpdateStatusLabelEvent;
import ch.systemsx.sybit.crkwebui.client.commons.gui.data.StatusMessageType;
import ch.systemsx.sybit.crkwebui.client.commons.managers.EventBusManager;
import ch.systemsx.sybit.crkwebui.client.commons.services.eppic.CrkWebServiceProvider;
import ch.systemsx.sybit.crkwebui.shared.model.PDBScoreItem;
import ch.systemsx.sybit.crkwebui.shared.model.ProcessingData;
import ch.systemsx.sybit.crkwebui.shared.model.ProcessingInProgressData;

import com.google.gwt.user.client.rpc.AsyncCallback;

/**
 * Callback used to handle the response from the server when trying to retrieve the status of currently selected job.
 * @author srebniak_a
 *
 */
public class GetCurrentStatusDataCallback implements AsyncCallback<ProcessingData>
{
	private String jobId;

	public GetCurrentStatusDataCallback(String jobId) 
	{
		this.jobId = jobId;
	}

	@Override
	public void onFailure(Throwable caught) 
	{
		String errorText = AppPropertiesManager.CONSTANTS.callback_get_current_status_data();
		EventBusManager.EVENT_BUS.fireEvent(new UpdateStatusLabelEvent(errorText, caught));
	}

	@Override
	public void onSuccess(ProcessingData result) 
	{
		if(result != null)
		{
			if(ApplicationContext.getSelectedJobId().equals(jobId))
			{
				if(result instanceof ProcessingInProgressData)
				{
					ProcessingInProgressData statusData = (ProcessingInProgressData) result;
					EventBusManager.EVENT_BUS.fireEvent(new RefreshStatusDataEvent(statusData));
				}
				else if(result instanceof PDBScoreItem)
				{
					PDBScoreItem resultsData = (PDBScoreItem) result;
					ApplicationContext.setPdbScoreItem(resultsData);
					ApplicationContext.cleanResiduesForInterface();
					
					CrkWebServiceProvider.getServiceController().getAllResidues(jobId);
					EventBusManager.EVENT_BUS.fireEvent(new ShowResultsDataEvent(resultsData));
				}
				else
				{
					EventBusManager.EVENT_BUS.fireEvent(new UpdateStatusLabelEvent(AppPropertiesManager.CONSTANTS.callback_get_current_status_data() + " " + result.getClass(),
																				   StatusMessageType.INTERNAL_ERROR));
					EventBusManager.EVENT_BUS.fireEvent(new ShowNoResultsDataEvent());
				}
			}
			
			CrkWebServiceProvider.getServiceController().getJobsForCurrentSession();
		}
		else
		{
			EventBusManager.EVENT_BUS.fireEvent(new ShowMessageEvent(AppPropertiesManager.CONSTANTS.callback_job_not_found_error(), "Job with id=" + jobId + " not found on the server"));
			EventBusManager.EVENT_BUS.fireEvent(new ShowNoResultsDataEvent());
		}
	}

}
