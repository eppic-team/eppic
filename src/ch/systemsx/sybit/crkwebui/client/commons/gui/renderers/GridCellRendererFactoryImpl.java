package ch.systemsx.sybit.crkwebui.client.commons.gui.renderers;

import com.extjs.gxt.ui.client.data.BaseModel;
import com.extjs.gxt.ui.client.widget.grid.GridCellRenderer;

/**
 * Factory used to select correct renderer for each of the column in the grid.
 * @author srebniak_a
 *
 */
public class GridCellRendererFactoryImpl implements GridCellRendererFactory
{
	private static GridCellRendererFactoryImpl INSTANCE = new GridCellRendererFactoryImpl();
	
	private GridCellRendererFactoryImpl()
	{
		
	}
	
	public static GridCellRendererFactoryImpl getInstance()
	{
		return INSTANCE;
	}
	
	public GridCellRenderer<BaseModel> createGridCellRenderer(String rendererName) 
	{
		GridCellRenderer<BaseModel> renderer = new DefaultCellRenderer();

		if (rendererName != null) {
			if (rendererName.equals("number")) {
				 renderer = new NumberRenderer();
			}
		}

		return renderer;
	}
}
