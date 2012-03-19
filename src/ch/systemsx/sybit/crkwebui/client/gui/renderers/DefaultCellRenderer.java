package ch.systemsx.sybit.crkwebui.client.gui.renderers;

import com.extjs.gxt.ui.client.data.BaseModel;
import com.extjs.gxt.ui.client.store.ListStore;
import com.extjs.gxt.ui.client.widget.grid.ColumnData;
import com.extjs.gxt.ui.client.widget.grid.Grid;
import com.extjs.gxt.ui.client.widget.grid.GridCellRenderer;

/**
 * Default cell renderer.
 * @author srebniak_a
 */
public class DefaultCellRenderer implements GridCellRenderer<BaseModel> {

	@Override
	public Object render(BaseModel model, String property, ColumnData config,
			int rowIndex, int colIndex, ListStore<BaseModel> store,
			Grid<BaseModel> grid) {
		return model.get(property);
	}

}
