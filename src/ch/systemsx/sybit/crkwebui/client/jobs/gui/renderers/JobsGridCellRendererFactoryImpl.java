package ch.systemsx.sybit.crkwebui.client.jobs.gui.renderers;

import ch.systemsx.sybit.crkwebui.client.commons.gui.renderers.GridCellRendererFactory;
import ch.systemsx.sybit.crkwebui.client.commons.gui.renderers.GridCellRendererFactoryImpl;

import com.extjs.gxt.ui.client.data.BaseModel;
import com.extjs.gxt.ui.client.widget.grid.GridCellRenderer;

/**
 * Factory used to select correct renderer for each of the column in the grid.
 * @author srebniak_a
 *
 */
public class JobsGridCellRendererFactoryImpl implements GridCellRendererFactory
{
	private static JobsGridCellRendererFactoryImpl INSTANCE = new JobsGridCellRendererFactoryImpl();
	
	private JobsGridCellRendererFactoryImpl()
	{
		
	}
	
	public static JobsGridCellRendererFactoryImpl getInstance()
	{
		return INSTANCE;
	}
	
	public GridCellRenderer<BaseModel> createGridCellRenderer(
			String rendererName) {

		GridCellRenderer<BaseModel> renderer = null;
		
		if (rendererName != null) {
			if (rendererName.equals("jobinput")) {
				renderer = new InputCellRenderer();
			} else if (rendererName.equals("jobstatus")) {
				renderer = new JobStatusCellRenderer();
			} else if (rendererName.equals("deletejob")) {
				renderer = new DeleteJobCellRenderer();
			}
		}
		
		if(renderer == null)
		{
			renderer = GridCellRendererFactoryImpl.getInstance().createGridCellRenderer(rendererName);
		}

		return renderer;
	}
}
