/**
 * 
 */
package ch.systemsx.sybit.crkwebui.client.jobs.gui.grids.contextmenus;

import ch.systemsx.sybit.crkwebui.client.commons.appdata.AppPropertiesManager;
import ch.systemsx.sybit.crkwebui.client.commons.services.eppic.CrkWebServiceProvider;
import ch.systemsx.sybit.crkwebui.client.jobs.data.MyJobsModel;

import com.google.gwt.event.logical.shared.SelectionEvent;
import com.google.gwt.event.logical.shared.SelectionHandler;
import com.sencha.gxt.widget.core.client.grid.Grid;
import com.sencha.gxt.widget.core.client.menu.Item;
import com.sencha.gxt.widget.core.client.menu.Menu;
import com.sencha.gxt.widget.core.client.menu.MenuItem;

/**
 * Context menu for jobs grid
 * @author biyani_n
 *
 */
public class JobsPanelContextMenu extends Menu{

	private Grid<MyJobsModel> grid;
	
	public JobsPanelContextMenu(Grid<MyJobsModel> grid){
		this.grid = grid;
		
		this.setWidth(100);
		
		this.add(createDeleteMenuItem());
		
	}
	
	/**
	 * Creates context menu item used to show delete button for specified job.
	 * @return menu item
	 */
	private MenuItem createDeleteMenuItem()
	{
		MenuItem deleteItem = new MenuItem();  
		deleteItem.setText(AppPropertiesManager.CONSTANTS.myjobs_grid_delete_button());
		deleteItem.addSelectionHandler(new SelectionHandler<Item>() {
			@Override
			public void onSelection(SelectionEvent<Item> event) {
				MyJobsModel selectedItem = grid.getSelectionModel().getSelectedItem();
				if(selectedItem != null)
				{
					CrkWebServiceProvider.getServiceController().deleteJob(selectedItem.getJobid());
				}	
			}
		});
		
		return deleteItem;
	}
}
