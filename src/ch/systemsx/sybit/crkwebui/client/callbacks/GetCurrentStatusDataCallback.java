package ch.systemsx.sybit.crkwebui.client.callbacks;

import model.PDBScoreItem;
import ch.systemsx.sybit.crkwebui.client.controllers.MainController;
import ch.systemsx.sybit.crkwebui.shared.model.ProcessingInProgressData;

import com.google.gwt.user.client.rpc.AsyncCallback;

/**
 * This is the callback used to handle the response from the server when trying to retrieve the status of currently selected job
 * @author srebniak_a
 *
 */
public class GetCurrentStatusDataCallback implements AsyncCallback 
{
	private MainController mainController;

	public GetCurrentStatusDataCallback(MainController mainController,
			String selectedId) 
	{
		this.mainController = mainController;
	}

	@Override
	public void onFailure(Throwable caught) 
	{
		String errorText = "Error during getting results of processing from server ";// + caught.getMessage();
		
//		mainController.showError(errorText);
		mainController.updateStatusLabel(errorText, true);
	}

	@Override
	public void onSuccess(Object result) 
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
				resultsData.setJobId(mainController.getSelectedJobId());
				mainController.setPDBScoreItem(resultsData);
				mainController.displayResultView(resultsData);
			}
			else
			{
				mainController.updateStatusLabel("Error during getting results of processing from server " + result.getClass(), true);
//				mainController.showError("Error during getting results of processing from server" + result.getClass());
				mainController.getMainViewPort().getCenterPanel().removeAll();
				mainController.getMainViewPort().getCenterPanel().setDisplayPanel(null);
			}
		}
		else
		{
			mainController.showMessage("Info", "id=" + mainController.getSelectedJobId() + " not found on the server");
			mainController.getMainViewPort().getCenterPanel().removeAll();
			mainController.getMainViewPort().getCenterPanel().setDisplayPanel(null);
		}
	}

}
