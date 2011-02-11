package ch.systemsx.sybit.crkwebui.client.gui.renderers;

import com.extjs.gxt.ui.client.data.BeanModel;
import com.extjs.gxt.ui.client.store.ListStore;
import com.extjs.gxt.ui.client.widget.grid.ColumnData;
import com.extjs.gxt.ui.client.widget.grid.Grid;
import com.extjs.gxt.ui.client.widget.grid.GridCellRenderer;

public class DefaultCellRenderer implements GridCellRenderer<BeanModel> {

	@Override
	public Object render(BeanModel model, String property, ColumnData config,
			int rowIndex, int colIndex, ListStore<BeanModel> store,
			Grid<BeanModel> grid) {
		return model.get(property);
	}

}
