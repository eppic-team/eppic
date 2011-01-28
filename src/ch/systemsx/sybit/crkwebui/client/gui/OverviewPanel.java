package ch.systemsx.sybit.crkwebui.client.gui;

import ch.systemsx.sybit.crkwebui.client.controllers.MainController;
import ch.systemsx.sybit.crkwebui.client.data.ResultsData;

import com.extjs.gxt.ui.client.widget.ContentPanel;
import com.extjs.gxt.ui.client.widget.Label;
import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.ui.HTML;

public class OverviewPanel extends ContentPanel 
{
	private MainController mainController;
	
	private HTML downloadResultsLink;
	
	public OverviewPanel(MainController mainController,
						 ResultsData resultsData)
	{
		this.mainController = mainController;
		this.setBorders(false);
		this.setBodyBorder(false);
		this.getHeader().setVisible(false);
		
		this.add(new Label("This panel contains results"));
		
		downloadResultsLink = new HTML();
		downloadResultsLink.setHTML("<a href=" + GWT.getModuleBaseURL() + "fileDownload?id=" + resultsData.getJobId() + ">Results</a>");
		this.add(downloadResultsLink);
		
	}
}
