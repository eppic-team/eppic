package ch.systemsx.sybit.crkwebui.client.callbacks;

import model.PDBScoreItem;
import ch.systemsx.sybit.crkwebui.client.controllers.MainController;
import ch.systemsx.sybit.crkwebui.shared.model.ProcessingInProgressData;

import com.google.gwt.user.client.rpc.AsyncCallback;

public class GetResultsOfProcessingCallback implements AsyncCallback {
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
		String errorText = "Error during getting results of processing from server\n";
		errorText += caught.getMessage() + "\n";
		
		mainController.showError(errorText);
	}

	@Override
	public void onSuccess(Object result) 
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
				resultsData.setJobId(selectedId);
				mainController.setPDBScoreItem(resultsData);
				mainController.displayResultView(resultsData);
			}
			else
			{
				mainController.showError("Error during getting results of processing from server" + result.getClass());
				mainController.getMainViewPort().getDisplayPanel().removeAll();
			}
		}
		else
		{
			mainController.showMessage("Info", "id=" + selectedId + " not found on the server");
			mainController.getMainViewPort().getDisplayPanel().removeAll();
		}
	}

}
