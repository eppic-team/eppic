package ch.systemsx.sybit.crkwebui.client.callbacks;

import java.util.HashMap;
import java.util.List;

import ch.systemsx.sybit.crkwebui.client.controllers.MainController;
import ch.systemsx.sybit.crkwebui.shared.model.InterfaceResidueItem;

import com.google.gwt.user.client.rpc.AsyncCallback;

/**
 * This is the callback used to handle the response from the server when trying to retrieve residues data
 * @author srebniak_a
 *
 */
public class GetInterfaceResiduesCallback implements AsyncCallback<HashMap<Integer, List<InterfaceResidueItem>>>
{
	private MainController mainController;
	private String jobId;

	public GetInterfaceResiduesCallback(MainController mainController,
										String jobId) 
	{
		this.mainController = mainController;
		this.jobId = jobId;
	}

	@Override
	public void onFailure(Throwable caught) 
	{
		mainController.updateStatusLabel(MainController.CONSTANTS.callback_get_interface_residues_error(), true);
	}

	@Override
	public void onSuccess(HashMap<Integer, List<InterfaceResidueItem>> result) 
	{
		mainController.showWaiting("Loading");
		
		if (result != null)
		{
			if(mainController.getSelectedJobId().equals(jobId))
			{
				mainController.setInterfacesResiduesWindowData(result);
			}
		}
		else 
		{
			mainController.updateStatusLabel(MainController.CONSTANTS.callback_get_interface_residues_error() + " - incorrect result type", true);
		}
		
		mainController.hideWaiting();
	}
}
