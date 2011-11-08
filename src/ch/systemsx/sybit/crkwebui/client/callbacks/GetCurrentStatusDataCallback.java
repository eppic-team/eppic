package ch.systemsx.sybit.crkwebui.client.callbacks;

import ch.systemsx.sybit.crkwebui.client.controllers.MainController;
import ch.systemsx.sybit.crkwebui.shared.model.PDBScoreItem;
import ch.systemsx.sybit.crkwebui.shared.model.ProcessingData;
import ch.systemsx.sybit.crkwebui.shared.model.ProcessingInProgressData;

import com.google.gwt.user.client.rpc.AsyncCallback;

/**
 * This is the callback used to handle the response from the server when trying to retrieve the status of currently selected job
 * @author srebniak_a
 *
 */
public class GetCurrentStatusDataCallback implements AsyncCallback<ProcessingData>
{
	private MainController mainController;
	private String selectedId;

	public GetCurrentStatusDataCallback(MainController mainController,
			String selectedId) 
	{
		this.mainController = mainController;
		this.selectedId = selectedId;
	}

	@Override
	public void onFailure(Throwable caught) 
	{
		String errorText = MainController.CONSTANTS.callback_get_current_status_data();// + caught.getMessage();
		mainController.updateStatusLabel(errorText, true);
	}

	@Override
	public void onSuccess(ProcessingData result) 
	{
		if(result != null)
		{
			if(result instanceof ProcessingInProgressData)
			{
				ProcessingInProgressData statusData = (ProcessingInProgressData) result;
				mainController.refreshStatusView(statusData);
			}
			else if(result instanceof PDBScoreItem)
			{
				PDBScoreItem resultsData = (PDBScoreItem) result;
				mainController.setPDBScoreItem(resultsData);
				mainController.cleanResiduesForInterface();
				
				mainController.getAllResidues(selectedId, resultsData.getUid());
				mainController.displayResultView(resultsData);
			}
			else
			{
				mainController.updateStatusLabel(MainController.CONSTANTS.callback_get_current_status_data() + " " + result.getClass(), true);
				mainController.cleanCenterPanel();
			}
			
			mainController.getJobsForCurrentSession();
		}
		else
		{
			mainController.showMessage(MainController.CONSTANTS.callback_job_not_found_error(), "Job with id=" + selectedId + " not found on the server");
			mainController.cleanCenterPanel();
		}
	}

}
