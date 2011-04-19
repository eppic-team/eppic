package ch.systemsx.sybit.crkwebui.client.gui.renderers;

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
public class MethodCellRenderer implements GridCellRenderer<BeanModel> 
{
	public MethodCellRenderer() 
	{
		
	}

	public Object render(final BeanModel model, String property,
			ColumnData config, final int rowIndex, final int colIndex,
			ListStore<BeanModel> store, Grid<BeanModel> grid) {
		String value = (String) model.get(property);

		if (value != null) 
		{
			String color = "black";

			if (value.equals("bio")) {
				color = "green";
			} else if (value.equals("xtal")) {
				color = "red";
			} else {
				return value;
			}

			return "<span style='font-weight: bold;color:" + color + "'>"
					+ value + "</span>";
		}

		return value;
	}

}
