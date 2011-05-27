package ch.systemsx.sybit.crkwebui.client.gui.renderers;

import ch.systemsx.sybit.crkwebui.client.controllers.MainController;

import com.extjs.gxt.ui.client.data.BeanModel;
import com.extjs.gxt.ui.client.widget.grid.GridCellRenderer;

/**
 * This factory is used to select correct renderer for each of the column in the grid
 * @author srebniak_a
 *
 */
public class GridCellRendererFactory {
	public static GridCellRenderer<BeanModel> createGridCellRenderer(
			String rendererName, MainController mainController) {
		GridCellRenderer<BeanModel> renderer = new DefaultCellRenderer();

		if (rendererName != null) {
			if (rendererName.equals("details")) {
				renderer = new DetailsButtonCellRenderer(mainController);
			} else if (rendererName.equals("methods")) {
				renderer = new MethodCellRenderer(mainController);
			} else if (rendererName.equals("number")) {
				 renderer = new NumberRenderer();
			} else if (rendererName.equals("viewer")) {
				 renderer = new ViewerButtonCellRenderer(mainController);
			} else if (rendererName.equals("thumbnail")) {
				 renderer = new ThumbnailCellRenderer(mainController);
			} else if (rendererName.equals("warnings")) {
				 renderer = new WarningsCellRenderer(mainController);
			} else if (rendererName.equals("size")) {
				renderer = new SizeCellRenderer(mainController);
			}
		}

		return renderer;
	}
}
