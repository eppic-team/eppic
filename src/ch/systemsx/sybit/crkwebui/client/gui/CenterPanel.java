package ch.systemsx.sybit.crkwebui.client.gui;

import ch.systemsx.sybit.crkwebui.client.controllers.MainController;

import com.extjs.gxt.ui.client.widget.LayoutContainer;
import com.extjs.gxt.ui.client.widget.layout.FitLayout;

/**
 * This is the main center panel
 * @author srebniak_a
 *
 */
public class CenterPanel extends LayoutContainer 
{
	protected MainController mainController;
	
	private DisplayPanel displayPanel;
	
	public CenterPanel(MainController mainController)
	{
		this.mainController = mainController;
//		this.getHeader().setVisible(false);
		this.setBorders(false);
//		this.setBodyBorder(false);
//		this.setPadding(0);
		this.setLayout(new FitLayout());
		
		displayPanel = new DisplayPanel(mainController);
		this.add(displayPanel);
	}
	
	public DisplayPanel getDisplayPanel()
	{
		return displayPanel;
	}
	
	public void setDisplayPanel(DisplayPanel displayPanel)
	{
		this.removeAll();
		this.displayPanel = displayPanel;
		
		if(displayPanel != null)
		{
			this.add(displayPanel);
		}
		
		this.layout();
	}
}
