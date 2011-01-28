package ch.systemsx.sybit.crkwebui.client.gui;

import ch.systemsx.sybit.crkwebui.client.controllers.MainController;

import com.extjs.gxt.ui.client.Style.LayoutRegion;
import com.extjs.gxt.ui.client.util.Margins;
import com.extjs.gxt.ui.client.widget.Viewport;
import com.extjs.gxt.ui.client.widget.layout.BorderLayout;
import com.extjs.gxt.ui.client.widget.layout.BorderLayoutData;

public class MainViewPort extends Viewport
{
	private MyJobsPanel myJobsPanel;
	
	private DisplayPanel displayPanel;
	
	private TopPanel topPanel;
	
	private MainController mainController;
	
	public MainViewPort(MainController mainController)
	{
		this.mainController = mainController;
		
		BorderLayout layout = new BorderLayout();  
		setLayout(layout);  
		setStyleAttribute("padding", "10px");  
		
		this.setLayout(layout);
		
		BorderLayoutData westData = new BorderLayoutData(LayoutRegion.WEST, 200);  
		westData.setCollapsible(true);  
		westData.setFloatable(true);  
		westData.setSplit(true);  
	    westData.setMargins(new Margins(0, 5, 0, 0));  
	    
		myJobsPanel = new MyJobsPanel(mainController);
		this.add(myJobsPanel, westData);
		
		BorderLayoutData centerData = new BorderLayoutData(LayoutRegion.CENTER, 200);  
		centerData.setCollapsible(true);  
		centerData.setFloatable(true);  
		centerData.setSplit(true);  
		centerData.setMargins(new Margins(0));  
	    
		displayPanel = new DisplayPanel(mainController);
		this.add(displayPanel, centerData);
		
		BorderLayoutData northData = new BorderLayoutData(LayoutRegion.NORTH,10);  
		northData.setMargins(new Margins(0, 0, 5, 0));  
	    
		topPanel = new TopPanel(mainController);
		topPanel.getHeader().setVisible(false);
		this.add(topPanel, northData);
	}
	
	public MyJobsPanel getMyJobsPanel()
	{
		return myJobsPanel;
	}
	
	public DisplayPanel getDisplayPanel()
	{
		return displayPanel;
	}
}
