package ch.systemsx.sybit.crkwebui.client.gui.renderers;

import com.extjs.gxt.ui.client.data.BaseModel;
import com.extjs.gxt.ui.client.store.ListStore;
import com.extjs.gxt.ui.client.widget.grid.ColumnData;
import com.extjs.gxt.ui.client.widget.grid.Grid;
import com.extjs.gxt.ui.client.widget.grid.GridCellRenderer;
import com.extjs.gxt.ui.client.widget.table.NumberCellRenderer;
import com.google.gwt.i18n.client.NumberFormat;

/**
 * This renderer is used to display formatted numerical values
 * @author srebniak_a
 *
 */
public class NumberRenderer implements GridCellRenderer<BaseModel> 
{
	private NumberFormat number = NumberFormat.getFormat("0.00");  
	private NumberCellRenderer<Grid<BaseModel>> numberRenderer = new NumberCellRenderer<Grid<BaseModel>>(number);  
	
	public Object render(final BaseModel model, 
						 String property,
						 ColumnData config, 
						 final int rowIndex, 
						 final int colIndex,
						 ListStore<BaseModel> store, 
						 final Grid<BaseModel> grid) 
	{
		String renderedNumber = numberRenderer.render(null, property, model.get(property));
		
		if((renderedNumber != null) && (renderedNumber.equals("NaN")))
		{
			renderedNumber = "-";
		}
		
		return renderedNumber;
	}
}
