package ch.systemsx.sybit.crkwebui.client.callbacks;

import java.util.HashMap;
import java.util.List;

import model.InterfaceResidueItem;
import ch.systemsx.sybit.crkwebui.client.controllers.MainController;
import ch.systemsx.sybit.crkwebui.client.gui.InterfacesResiduesWindow;

import com.google.gwt.user.client.rpc.AsyncCallback;

public class GetInterfaceResiduesCallback implements AsyncCallback 
{
	private MainController mainController;

	public GetInterfaceResiduesCallback(MainController mainController) 
	{
		this.mainController = mainController;
	}

	@Override
	public void onFailure(Throwable caught) 
	{
		mainController.showError("Error during getting residues data from server" + caught.getMessage());
	}

	@Override
	public void onSuccess(Object result) 
	{
		mainController.showWaiting("Loading");
		
		if ((result != null) && (result instanceof HashMap)) 
		{
			HashMap<String, List<InterfaceResidueItem>> structures = (HashMap<String, List<InterfaceResidueItem>>) result;

			if(structures.containsKey(1))
			{
				mainController.getInterfacesResiduesWindow().getInterfacesResiduesPanel().getFirstStructurePanel()
						.fillResiduesGrid(structures.get(1));
				mainController.getInterfacesResiduesWindow().getInterfacesResiduesPanel().getFirstStructurePanel().applyFilter(false);
			}
			
			if(structures.containsKey(2))
			{
				mainController.getInterfacesResiduesWindow().getInterfacesResiduesPanel().getSecondStructurePanel()
						.fillResiduesGrid(structures.get(2));
				mainController.getInterfacesResiduesWindow().getInterfacesResiduesPanel().getSecondStructurePanel().applyFilter(false);
			}
		}
		else 
		{
			mainController.showError("Error during getting residues from server");
		}
		
		mainController.hideWaiting();
	}
}
