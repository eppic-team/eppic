package ch.systemsx.sybit.crkwebui.client.gui;

import ch.systemsx.sybit.crkwebui.client.controllers.MainController;

import com.extjs.gxt.ui.client.widget.Dialog;
import com.extjs.gxt.ui.client.widget.layout.FitLayout;

public class InterfacesResiduesWindow extends Dialog 
{
	private InterfacesResiduesPanel interfacesResiduesPanel;

	public InterfacesResiduesWindow(MainController mainController) 
	{
		int width = 1000;
		int height = 630;
		this.setSize(width, height);
		this.setPlain(true);
		this.setModal(true);
		this.setBlinkModal(true);
		this.setHeading(MainController.CONSTANTS.interfaces_residues_window());
		this.setLayout(new FitLayout());
		this.setHideOnButtonClick(true);

		interfacesResiduesPanel = new InterfacesResiduesPanel(mainController);
		this.add(interfacesResiduesPanel);
	}

	public InterfacesResiduesPanel getInterfacesResiduesPanel() {
		return interfacesResiduesPanel;
	}
}
