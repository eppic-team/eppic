package ch.systemsx.sybit.crkwebui.client.callbacks;

import java.util.ArrayList;
import java.util.List;

import model.InterfaceItem;
import model.PDBScoreItem;
import model.ProcessingData;
import ch.systemsx.sybit.crkwebui.client.controllers.MainController;
import ch.systemsx.sybit.crkwebui.shared.model.ProcessingInProgressData;

import com.google.gwt.user.client.rpc.AsyncCallback;

/**
 * This is the callback used to handle the response from the server
 * @author srebniak_a
 *
 */
public class GetResultsOfProcessingCallback implements AsyncCallback<ProcessingData>
{
	private MainController mainController;
	private String selectedId;

	public GetResultsOfProcessingCallback(MainController mainController,
			String selectedId) 
	{
		this.mainController = mainController;
		this.selectedId = selectedId;
	}

	@Override
	public void onFailure(Throwable caught) 
	{
		String errorText = MainController.CONSTANTS.callback_get_results_of_processing_error() + " " + caught.getMessage();
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
				mainController.displayStatusView(statusData);
			}
			else if(result instanceof PDBScoreItem)
			{
				PDBScoreItem resultsData = (PDBScoreItem) result;
				mainController.setPDBScoreItem(resultsData);
				
				List<Integer> interfaceIds = new ArrayList<Integer>();
				for(InterfaceItem interfaceItem: resultsData.getInterfaceItems())
				{
					interfaceIds.add(interfaceItem.getId());
				}
				mainController.getAllResidues(resultsData.getJobId(), interfaceIds);
				
				mainController.displayResultView(resultsData);
			}
			else
			{
				mainController.updateStatusLabel(MainController.CONSTANTS.callback_get_results_of_processing_error() + " - incorrect type ", true);
				mainController.cleanCenterPanel();
			}
			
			mainController.getJobsForCurrentSession();
		}
		else
		{
			mainController.showMessage(MainController.CONSTANTS.callback_job_not_found_error(), "Id=" + selectedId + " not found on the server");
			mainController.cleanCenterPanel();
		}
	}

}
