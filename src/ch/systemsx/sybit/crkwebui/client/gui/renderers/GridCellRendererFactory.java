package ch.systemsx.sybit.crkwebui.client.gui.renderers;

import com.extjs.gxt.ui.client.data.BaseModel;
import com.extjs.gxt.ui.client.widget.grid.GridCellRenderer;

/**
 * Factory used to select correct renderer for each of the column in the grid.
 * @author srebniak_a
 *
 */
public class GridCellRendererFactory {
	public static GridCellRenderer<BaseModel> createGridCellRenderer(
			String rendererName) {
		GridCellRenderer<BaseModel> renderer = new DefaultCellRenderer();

		if (rendererName != null) {
			if (rendererName.equals("details")) {
				renderer = new DetailsButtonCellRenderer();
			} else if (rendererName.equals("methods")) {
				renderer = new MethodCellRenderer();
			} else if (rendererName.equals("number")) {
				 renderer = new NumberRenderer();
			} else if (rendererName.equals("thumbnail")) {
				 renderer = new ThumbnailCellRenderer();
			} else if (rendererName.equals("warnings")) {
				 renderer = new WarningsCellRenderer();
			} else if (rendererName.equals("jobinput")) {
				renderer = new InputCellRenderer();
			} else if (rendererName.equals("jobstatus")) {
				renderer = new JobStatusCellRenderer();
			} else if (rendererName.equals("deletejob")) {
				renderer = new DeleteJobCellRenderer();
			} else if (rendererName.equals("finalcall")) {
				renderer = new FinalCallCellRenderer();
			}
		}

		return renderer;
	}
}
