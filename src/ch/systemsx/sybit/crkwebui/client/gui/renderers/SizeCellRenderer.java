package ch.systemsx.sybit.crkwebui.client.gui.renderers;

import ch.systemsx.sybit.crkwebui.client.controllers.MainController;

import com.extjs.gxt.ui.client.data.BeanModel;
import com.extjs.gxt.ui.client.store.ListStore;
import com.extjs.gxt.ui.client.widget.grid.ColumnData;
import com.extjs.gxt.ui.client.widget.grid.Grid;
import com.extjs.gxt.ui.client.widget.grid.GridCellRenderer;

/**
 * This model is used to style the results of calculations for each of the method
 * @author srebniak_a
 *
 */
public class SizeCellRenderer implements GridCellRenderer<BeanModel> 
{
	private MainController mainController;
	
	public SizeCellRenderer(MainController mainController) 
	{
		this.mainController = mainController;
	}

	public Object render(final BeanModel model, String property,
			ColumnData config, final int rowIndex, final int colIndex,
			ListStore<BeanModel> store, Grid<BeanModel> grid) {
		
		int value = model.get(property);
		
		int size1 = model.get("size1");
		int size2 = model.get("size2");
		
		int sizeSum = size1 + size2;

		if(sizeSum < mainController.getPdbScoreItem().getMinCoreSize())
		{
			String color = "red";
			return "<span style='font-weight: bold;color:" + color + "'>"
					+ value + "</span>";
		}

		return value;
	}

}
