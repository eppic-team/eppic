package ch.systemsx.sybit.crkwebui.client.callbacks;

import java.util.HashMap;
import java.util.List;

import model.InterfaceResidueItem;
import ch.systemsx.sybit.crkwebui.client.controllers.MainController;
import ch.systemsx.sybit.crkwebui.client.gui.InterfacesWindow;

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

			InterfacesWindow interfacesWindow = new InterfacesWindow(
					mainController);
			
			if(structures.containsKey(1))
			{
				interfacesWindow.getInterfacesPanel().getFirstStructurePanel()
						.fillResiduesGrid(structures.get(1));
			}
			
			if(structures.containsKey(2))
			{
				interfacesWindow.getInterfacesPanel().getSecondStructurePanel()
						.fillResiduesGrid(structures.get(2));
			}
			
			interfacesWindow.setVisible(true);
		}
		else 
		{
			mainController.showError("Error during getting residues from server");
		}
	}
}
