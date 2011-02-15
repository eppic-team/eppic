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
		if ((result != null) && (result instanceof HashMap)) 
		{
			HashMap<String, List<InterfaceResidueItem>> structures = (HashMap<String, List<InterfaceResidueItem>>) result;

			InterfacesResiduesWindow interfacesResiduesWindow = new InterfacesResiduesWindow(mainController);
			
			if(structures.containsKey(1))
			{
				interfacesResiduesWindow.getInterfacesResiduesPanel().getFirstStructurePanel()
						.fillResiduesGrid(structures.get(1));
			}
			
			if(structures.containsKey(2))
			{
				interfacesResiduesWindow.getInterfacesResiduesPanel().getSecondStructurePanel()
						.fillResiduesGrid(structures.get(2));
			}
			
			interfacesResiduesWindow.setVisible(true);
		}
		else 
		{
			mainController.showError("Error during getting residues from server");
		}
	}
}
