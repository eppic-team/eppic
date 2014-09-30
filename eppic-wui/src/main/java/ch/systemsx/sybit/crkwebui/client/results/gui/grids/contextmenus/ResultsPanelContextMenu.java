package ch.systemsx.sybit.crkwebui.client.results.gui.grids.contextmenus;

import ch.systemsx.sybit.crkwebui.client.commons.appdata.AppPropertiesManager;
import ch.systemsx.sybit.crkwebui.client.commons.events.ShowDetailsEvent;
import ch.systemsx.sybit.crkwebui.client.commons.events.ShowViewerEvent;
import ch.systemsx.sybit.crkwebui.client.commons.events.ShowViewerSelectorEvent;
import ch.systemsx.sybit.crkwebui.client.commons.managers.EventBusManager;

import com.google.gwt.event.logical.shared.SelectionEvent;
import com.google.gwt.event.logical.shared.SelectionHandler;
import com.sencha.gxt.widget.core.client.menu.Item;
import com.sencha.gxt.widget.core.client.menu.Menu;
import com.sencha.gxt.widget.core.client.menu.MenuItem;

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
		
		MenuItem selectorItem = createSelectorMenuItem();
		this.add(selectorItem);
		
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
		detailsItem.addSelectionHandler(new SelectionHandler<Item>() {
			@Override
			public void onSelection(SelectionEvent<Item> event) {
				EventBusManager.EVENT_BUS.fireEvent(new ShowDetailsEvent());
				
			}
		});
		
		return detailsItem;
	}
	
	/**
	 * Creates context menu item used to select 3D viewer.
	 * @return menu item
	 */
	private MenuItem createSelectorMenuItem()
	{
		MenuItem selectorItem = new MenuItem();  
		selectorItem.setText(AppPropertiesManager.CONSTANTS.results_grid_selector_button());
		selectorItem.addSelectionHandler(new SelectionHandler<Item>() {
			@Override
			public void onSelection(SelectionEvent<Item> event) {
				EventBusManager.EVENT_BUS.fireEvent(new ShowViewerSelectorEvent());
				
			}
		});
		
		return selectorItem;
	}

	/**
	 * Creates context menu item used to open molecular viewer.
	 * @return menu item
	 */
	private MenuItem createViewerMenuItem()
	{
		MenuItem viewerItem = new MenuItem();  
		viewerItem.setText(AppPropertiesManager.CONSTANTS.results_grid_viewer_button()); 
		viewerItem.addSelectionHandler(new SelectionHandler<Item>() {
			
			@Override
			public void onSelection(SelectionEvent<Item> event) {
				EventBusManager.EVENT_BUS.fireEvent(new ShowViewerEvent());
				
			}
		});
		
		return viewerItem;
	}
}
