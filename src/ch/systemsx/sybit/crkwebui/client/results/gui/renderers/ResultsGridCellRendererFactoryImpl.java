package ch.systemsx.sybit.crkwebui.client.results.gui.renderers;

import ch.systemsx.sybit.crkwebui.client.commons.gui.renderers.GridCellRendererFactory;
import ch.systemsx.sybit.crkwebui.client.commons.gui.renderers.GridCellRendererFactoryImpl;

import com.extjs.gxt.ui.client.data.BaseModel;
import com.extjs.gxt.ui.client.widget.grid.GridCellRenderer;

/**
 * Factory used to select correct renderer for each of the column in the grid.
 * @author srebniak_a
 *
 */
public class ResultsGridCellRendererFactoryImpl implements GridCellRendererFactory
{
	private static ResultsGridCellRendererFactoryImpl INSTANCE = new ResultsGridCellRendererFactoryImpl();
	
	private ResultsGridCellRendererFactoryImpl()
	{
		
	}
	
	public static ResultsGridCellRendererFactoryImpl getInstance()
	{
		return INSTANCE;
	}
	
	public GridCellRenderer<BaseModel> createGridCellRenderer(String rendererName) 
	{
		GridCellRenderer<BaseModel> renderer = null;

		if (rendererName != null) {
			if (rendererName.equals("details")) {
				renderer = new DetailsButtonCellRenderer();
			} else if (rendererName.equals("methods")) {
				renderer = new MethodCellRenderer();
			} else if (rendererName.equals("thumbnail")) {
				 renderer = new ThumbnailCellRenderer();
			} else if (rendererName.equals("warnings")) {
				 renderer = new WarningsCellRenderer();
			} else if (rendererName.equals("finalcall")) {
				renderer = new FinalCallCellRenderer();
			} else if (rendererName.equals("operatorType")) {
				renderer = new OperatorTypeCellRenderer();
			}
		}
		
		if(renderer == null)
		{
			renderer = GridCellRendererFactoryImpl.getInstance().createGridCellRenderer(rendererName);
		}

		return renderer;
	}
}
