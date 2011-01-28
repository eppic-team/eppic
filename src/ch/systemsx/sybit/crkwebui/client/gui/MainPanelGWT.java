package ch.systemsx.sybit.crkwebui.client.gui;

import ch.systemsx.sybit.crkwebui.client.controllers.MainController;

import com.google.gwt.user.client.ui.SplitLayoutPanel;

public class MainPanelGWT extends SplitLayoutPanel
{
	private MainController mainController;
	
	private MyJobsPanelGWT myJobsPanel;
	
	private DisplayPanelGWT  displayPanel;
	
	public MainPanelGWT(MainController mainController)
	{
		this.mainController = mainController;
		
		myJobsPanel = new MyJobsPanelGWT(mainController);
		this.addWest(myJobsPanel, 150);
		
		displayPanel = new DisplayPanelGWT(mainController);
		this.add(displayPanel);
	}
	
	public DisplayPanelGWT getDisplayPanel()
	{
		return displayPanel;
	}
	
	public MyJobsPanelGWT getMyJobsPanel()
	{
		return myJobsPanel;
	}
}
