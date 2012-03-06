package ch.systemsx.sybit.crkwebui.client.gui.renderers;

import com.extjs.gxt.ui.client.data.BaseModel;
import com.extjs.gxt.ui.client.store.ListStore;
import com.extjs.gxt.ui.client.widget.grid.ColumnData;
import com.extjs.gxt.ui.client.widget.grid.Grid;
import com.google.gwt.user.client.ui.Hyperlink;

/**
 * This renderer is used to display input value (pdb code or filename without extension)
 * @author srebniak_a
 */
public class InputCellRenderer extends DefaultCellRenderer 
{
	@Override
	public Object render(final BaseModel model, 
						 String property,
						 ColumnData config, 
						 final int rowIndex, 
						 final int colIndex,
						 ListStore<BaseModel> store, 
						 final Grid<BaseModel> grid) 
	{
		String input = (String) model.get("input");
		if(input.contains("."))
		{
			input = input.substring(0, input.indexOf("."));
		}
		
		Hyperlink link = new Hyperlink(input, "id/" + model.get("jobid"));
		return link;
	}
}
