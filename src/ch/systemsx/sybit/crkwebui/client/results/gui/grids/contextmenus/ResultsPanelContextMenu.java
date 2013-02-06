package ch.systemsx.sybit.crkwebui.client.results.gui.grids.contextmenus;

import ch.systemsx.sybit.crkwebui.client.commons.appdata.AppPropertiesManager;
import ch.systemsx.sybit.crkwebui.client.commons.events.ShowDetailsEvent;
import ch.systemsx.sybit.crkwebui.client.commons.events.ShowViewerEvent;
import ch.systemsx.sybit.crkwebui.client.commons.managers.EventBusManager;

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
		  
		MenuItem detailsItem = createDetailsMenuItem();
		this.add(detailsItem);
		
		MenuItem viewerItem = createViewerMenuItem();  
		this.add(viewerItem);  
	}
	
	/**
	 * Creates context menu item used to show details for specified interface.
	 * @return menu item
	 */
	private MenuItem createDetailsMenuItem()
	{
		MenuItem detailsItem = new MenuItem();  
		detailsItem.setText(AppPropertiesManager.CONSTANTS.results_grid_details_button());
		detailsItem.addSelectionListener(new SelectionListener<MenuEvent>() 
		{  
			public void componentSelected(MenuEvent ce) 
			{  
				EventBusManager.EVENT_BUS.fireEvent(new ShowDetailsEvent());
			}  
		});  
		
		return detailsItem;
	}

	/**
	 * Creates context menu item used to open molecular viewer.
	 * @return menu item
	 */
	private MenuItem createViewerMenuItem()
	{
		MenuItem viewerItem = new MenuItem();  
		viewerItem.setText(AppPropertiesManager.CONSTANTS.results_grid_viewer_button()); 
		viewerItem.addSelectionListener(new SelectionListener<MenuEvent>() 
		{  
			public void componentSelected(MenuEvent ce) 
			{  
				EventBusManager.EVENT_BUS.fireEvent(new ShowViewerEvent());
			}  
		});  
		
		return viewerItem;
	}
}
