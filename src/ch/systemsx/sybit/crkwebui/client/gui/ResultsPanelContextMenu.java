package ch.systemsx.sybit.crkwebui.client.gui;

import ch.systemsx.sybit.crkwebui.client.controllers.AppPropertiesManager;
import ch.systemsx.sybit.crkwebui.client.controllers.EventBusManager;
import ch.systemsx.sybit.crkwebui.client.events.ShowDetailsEvent;
import ch.systemsx.sybit.crkwebui.client.events.ShowViewerEvent;

import com.extjs.gxt.ui.client.event.MenuEvent;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.widget.menu.Menu;
import com.extjs.gxt.ui.client.widget.menu.MenuItem;

/**
 * Context menu for results panel. It contains viewer and details items.
 * @author AS
 *
 */
public class ResultsPanelContextMenu extends Menu 
{
	public ResultsPanelContextMenu()
	{
		this.setWidth(140);  
		  
		MenuItem detailsItem = new MenuItem();  
		detailsItem.setText(AppPropertiesManager.CONSTANTS.results_grid_details_button());
		detailsItem.addSelectionListener(new SelectionListener<MenuEvent>() 
		{  
			public void componentSelected(MenuEvent ce) 
			{  
				EventBusManager.EVENT_BUS.fireEvent(new ShowDetailsEvent());
			}  
		});  
		this.add(detailsItem);
		
		MenuItem viewerItem = new MenuItem();  
		viewerItem.setText(AppPropertiesManager.CONSTANTS.results_grid_viewer_button()); 
		viewerItem.addSelectionListener(new SelectionListener<MenuEvent>() 
		{  
			public void componentSelected(MenuEvent ce) 
			{  
				EventBusManager.EVENT_BUS.fireEvent(new ShowViewerEvent());
			}  
		});  
		
		this.add(viewerItem);  
	}
}
