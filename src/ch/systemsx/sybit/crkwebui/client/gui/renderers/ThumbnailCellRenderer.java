package ch.systemsx.sybit.crkwebui.client.gui.renderers;

import ch.systemsx.sybit.crkwebui.client.controllers.MainController;

import com.extjs.gxt.ui.client.data.BeanModel;
import com.extjs.gxt.ui.client.store.ListStore;
import com.extjs.gxt.ui.client.widget.grid.ColumnData;
import com.extjs.gxt.ui.client.widget.grid.Grid;
import com.extjs.gxt.ui.client.widget.grid.GridCellRenderer;

/**
 * This renderer is used to display interfaces thumbnails
 * @author srebniak_a
 *
 */
public class ThumbnailCellRenderer implements GridCellRenderer<BeanModel> 
{
	private MainController mainController;

	public ThumbnailCellRenderer(MainController mainController) {
		this.mainController = mainController;
	}
	
	public Object render(BeanModel model, String property,
			ColumnData config, int rowIndex, int colIndex,
			ListStore<BeanModel> store, Grid<BeanModel> grid) 
	{
		String url = mainController.getSettings().getResultsLocation();
		
		return "<img src=\"" + 
				url + 
				mainController.getSelectedJobId() + 
				"/" +
				mainController.getPdbScoreItem().getPdbName() +
				"." +
				model.get("id") +
				".75x75.png" +
				"\"/>";
	}
}