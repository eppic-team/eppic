package ch.systemsx.sybit.crkwebui.client.gui;

import com.extjs.gxt.ui.client.widget.TabPanel;

import ch.systemsx.sybit.crkwebui.client.controllers.MainController;
import ch.systemsx.sybit.crkwebui.client.data.ResultsData;

public class ResultsPanel extends TabPanel
{
	private MainController mainController;
	
	private OverViewTabItem overViewTabItem;
	
	private ResultsData resultsData;
	
	public ResultsPanel(MainController mainController,
						ResultsData resultsData)
	{
		this.mainController = mainController;
		this.resultsData = resultsData;
		this.setBorders(false);
		
		overViewTabItem = new OverViewTabItem(mainController, resultsData);
		this.add(overViewTabItem);
	}
}
