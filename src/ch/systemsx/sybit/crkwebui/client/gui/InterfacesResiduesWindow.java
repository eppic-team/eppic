package ch.systemsx.sybit.crkwebui.client.gui;

import ch.systemsx.sybit.crkwebui.client.controllers.MainController;

import com.extjs.gxt.ui.client.event.ButtonEvent;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.widget.Dialog;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.extjs.gxt.ui.client.widget.layout.FitLayout;
import com.google.gwt.user.client.Window;

public class InterfacesResiduesWindow extends Dialog 
{
	private InterfacesResiduesPanel interfacesResiduesPanel;

	public InterfacesResiduesWindow(final MainController mainController) 
	{
		int width = 1000;
		int height = 630;
		
		if(width > mainController.getWindowWidth())
		{
			width = mainController.getWindowWidth();
		}
		
		if(height > mainController.getWindowHeight() - 50)
		{
			height = mainController.getWindowHeight() - 50;
		}
		
		this.setSize(width, height);
		this.setPlain(true);
		this.setModal(true);
		this.setBlinkModal(true);
		this.setHeading(MainController.CONSTANTS.interfaces_residues_window());
		this.setLayout(new FitLayout());
		this.setHideOnButtonClick(true);

		interfacesResiduesPanel = new InterfacesResiduesPanel(mainController,
															  width,
															  height);
		this.add(interfacesResiduesPanel);
	}

	public InterfacesResiduesPanel getInterfacesResiduesPanel() {
		return interfacesResiduesPanel;
	}
}
