package ch.systemsx.sybit.crkwebui.client.commons.callbacks;

import ch.systemsx.sybit.crkwebui.client.commons.appdata.AppPropertiesManager;
import ch.systemsx.sybit.crkwebui.client.commons.appdata.ApplicationContext;
import ch.systemsx.sybit.crkwebui.client.commons.events.ShowMessageEvent;
import ch.systemsx.sybit.crkwebui.client.commons.events.ShowNoResultsDataEvent;
import ch.systemsx.sybit.crkwebui.client.commons.events.ShowResultsDataEvent;
import ch.systemsx.sybit.crkwebui.client.commons.events.ShowStatusDataEvent;
import ch.systemsx.sybit.crkwebui.client.commons.events.UnmaskMainViewEvent;
import ch.systemsx.sybit.crkwebui.client.commons.events.UpdateStatusLabelEvent;
import ch.systemsx.sybit.crkwebui.client.commons.gui.data.StatusMessageType;
import ch.systemsx.sybit.crkwebui.client.commons.managers.EventBusManager;
import ch.systemsx.sybit.crkwebui.client.commons.services.eppic.CrkWebServiceProvider;
import ch.systemsx.sybit.crkwebui.shared.model.PDBScoreItem;
import ch.systemsx.sybit.crkwebui.shared.model.ProcessingData;
import ch.systemsx.sybit.crkwebui.shared.model.ProcessingInProgressData;

import com.google.gwt.user.client.History;
import com.google.gwt.user.client.rpc.AsyncCallback;

/**
 * Callback used to handle the response from the server when retrieving results of processing.
 * @author srebniak_a
 *
 */
public class GetResultsOfProcessingCallback implements AsyncCallback<ProcessingData>
{
	private String jobId;

	public GetResultsOfProcessingCallback(String jobId) 
	{
		this.jobId = jobId;
	}

	@Override
	public void onFailure(Throwable caught) 
	{
		String errorText = AppPropertiesManager.CONSTANTS.callback_get_results_of_processing_error() + " " + caught.getMessage();
		EventBusManager.EVENT_BUS.fireEvent(new UpdateStatusLabelEvent(errorText, caught));
		EventBusManager.EVENT_BUS.fireEvent(new UnmaskMainViewEvent());
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
					EventBusManager.EVENT_BUS.fireEvent(new ShowStatusDataEvent(statusData));
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
					EventBusManager.EVENT_BUS.fireEvent(new UpdateStatusLabelEvent(AppPropertiesManager.CONSTANTS.callback_get_results_of_processing_error() + " - incorrect type ", 
																				   StatusMessageType.INTERNAL_ERROR));
					EventBusManager.EVENT_BUS.fireEvent(new ShowNoResultsDataEvent());
				}
			}
			
			CrkWebServiceProvider.getServiceController().getJobsForCurrentSession();
		}
		else
		{
			History.newItem("");
			String msg = "Id=" + jobId + " not found on the server";
			
			if (jobId!=null && jobId.length()==4 && Character.isDigit(jobId.charAt(0))) {
				msg = "<div> <b>"
						+ "Could not find PDB code '"+jobId+"' on the server!</b></br>"
						+ "This can occur because:"
						+ "<ul><li>PDB code does not exist, or,</li>"
						+ "<li>If the PDB entry was released recently we might not have computed results for it yet. "
						+ "You can always try uploading the corresponding PDB/mmCIF file.</li></ul>"
						+ "</div>";
			}
			
			EventBusManager.EVENT_BUS.fireEvent(new ShowMessageEvent(AppPropertiesManager.CONSTANTS.callback_job_not_found_error(), msg));
		}
		
		EventBusManager.EVENT_BUS.fireEvent(new UnmaskMainViewEvent());
	}

}
