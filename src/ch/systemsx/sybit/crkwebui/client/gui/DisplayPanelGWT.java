package ch.systemsx.sybit.crkwebui.client.gui;

import ch.systemsx.sybit.crkwebui.client.controllers.MainController;

import com.google.gwt.user.client.ui.VerticalPanel;

public class DisplayPanelGWT extends VerticalPanel
{
	private MainController mainController;
	
	public DisplayPanelGWT(MainController mainController)
	{
		this.mainController = mainController;
	}
	
}
