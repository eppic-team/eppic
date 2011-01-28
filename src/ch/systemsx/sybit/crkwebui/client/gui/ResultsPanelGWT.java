package ch.systemsx.sybit.crkwebui.client.gui;

import ch.systemsx.sybit.crkwebui.client.controllers.MainController;

import com.google.gwt.user.client.ui.TabPanel;

public class ResultsPanelGWT extends TabPanel
{
	private MainController mainController;
	
	public ResultsPanelGWT(MainController mainController)
	{
		this.mainController = mainController;
	}
}
