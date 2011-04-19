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
//		mainController.showError("Error during getting residues data from server" + caught.getMessage());
	}

	@Override
	public void onSuccess(HashMap<Integer, List<InterfaceResidueItem>> result) 
	{
		mainController.showWaiting("Loading");
		
		if (result != null)
		{
			if(result.containsKey(1))
			{
				mainController.getMainViewPort().getInterfacesResiduesWindow().getInterfacesResiduesPanel().getFirstStructurePanel()
						.fillResiduesGrid(result.get(1));
				mainController.getMainViewPort().getInterfacesResiduesWindow().getInterfacesResiduesPanel().getFirstStructurePanel().applyFilter(false);
			}
			
			if(result.containsKey(2))
			{
				mainController.getMainViewPort().getInterfacesResiduesWindow().getInterfacesResiduesPanel().getSecondStructurePanel()
						.fillResiduesGrid(result.get(2));
				mainController.getMainViewPort().getInterfacesResiduesWindow().getInterfacesResiduesPanel().getSecondStructurePanel().applyFilter(false);
			}
			
			mainController.getMainViewPort().getInterfacesResiduesWindow().getInterfacesResiduesPanel().resizeResiduesPanels();
		}
		else 
		{
			mainController.updateStatusLabel(MainController.CONSTANTS.callback_get_interface_residues_error() + " - incorrect result type", true);
//			mainController.showError("Error during getting residues from server");
		}
		
		mainController.hideWaiting();
	}
}
