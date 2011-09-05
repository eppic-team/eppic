package ch.systemsx.sybit.crkwebui.client.gui;

import ch.systemsx.sybit.crkwebui.client.controllers.MainController;

import com.extjs.gxt.ui.client.event.MenuEvent;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.extjs.gxt.ui.client.widget.menu.Menu;
import com.extjs.gxt.ui.client.widget.menu.MenuItem;

public class ResultsPanelContextMenu extends Menu 
{
	public ResultsPanelContextMenu(final MainController mainController)
	{
		this.setWidth(140);  
		  
		MenuItem detailsItem = new MenuItem();  
		detailsItem.setText(MainController.CONSTANTS.results_grid_details_button());
		detailsItem.addSelectionListener(new SelectionListener<MenuEvent>() 
		{  
			public void componentSelected(MenuEvent ce) 
			{  
				if((mainController.getMainViewPort().getCenterPanel().getDisplayPanel() != null) &&
					(mainController.getMainViewPort().getCenterPanel().getDisplayPanel() instanceof ResultsPanel))
				{
					ResultsPanel resultsPanel = (ResultsPanel)mainController.getMainViewPort().getCenterPanel().getDisplayPanel();
					mainController.getInterfaceResidues((Integer) resultsPanel.getResultsGrid().getSelectionModel().getSelectedItem().get("id"));
				}
			}  
		});  
		this.add(detailsItem);
		
		MenuItem viewerItem = new MenuItem();  
		viewerItem.setText(MainController.CONSTANTS.results_grid_viewer_button()); 
		viewerItem.addSelectionListener(new SelectionListener<MenuEvent>() 
		{  
			public void componentSelected(MenuEvent ce) 
			{  
				if((mainController.getMainViewPort().getCenterPanel().getDisplayPanel() != null) &&
					(mainController.getMainViewPort().getCenterPanel().getDisplayPanel() instanceof ResultsPanel))
				{
					ResultsPanel resultsPanel = (ResultsPanel)mainController.getMainViewPort().getCenterPanel().getDisplayPanel();
					mainController.runViewer(String.valueOf(resultsPanel.getResultsGrid().getSelectionModel().getSelectedItem().get("id")));
				}
			}  
		});  
		this.add(viewerItem);  
	}
}
