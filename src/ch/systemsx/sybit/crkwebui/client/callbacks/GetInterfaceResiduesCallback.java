package ch.systemsx.sybit.crkwebui.client.callbacks;

import java.util.HashMap;
import java.util.List;

import model.InterfaceResidueItem;
import ch.systemsx.sybit.crkwebui.client.controllers.MainController;

import com.google.gwt.user.client.rpc.AsyncCallback;

/**
 * This is the callback used to handle the response from the server when trying to retrieve residues data
 * @author srebniak_a
 *
 */
public class GetInterfaceResiduesCallback implements AsyncCallback<HashMap<Integer, List<InterfaceResidueItem>>>
{
	private MainController mainController;

	public GetInterfaceResiduesCallback(MainController mainController) 
	{
		this.mainController = mainController;
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
			mainController.setInterfacesResiduesWindowData(result);
		}
		else 
		{
			mainController.updateStatusLabel(MainController.CONSTANTS.callback_get_interface_residues_error() + " - incorrect result type", true);
		}
		
		mainController.hideWaiting();
	}
}
