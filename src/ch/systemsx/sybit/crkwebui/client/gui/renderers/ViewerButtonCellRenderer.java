package ch.systemsx.sybit.crkwebui.client.gui.renderers;

import ch.systemsx.sybit.crkwebui.client.controllers.MainController;

import com.extjs.gxt.ui.client.data.BeanModel;
import com.extjs.gxt.ui.client.event.ButtonEvent;
import com.extjs.gxt.ui.client.event.Events;
import com.extjs.gxt.ui.client.event.GridEvent;
import com.extjs.gxt.ui.client.event.Listener;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.store.ListStore;
import com.extjs.gxt.ui.client.widget.BoxComponent;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.extjs.gxt.ui.client.widget.grid.ColumnData;
import com.extjs.gxt.ui.client.widget.grid.Grid;
import com.extjs.gxt.ui.client.widget.grid.GridCellRenderer;

public class ViewerButtonCellRenderer implements GridCellRenderer<BeanModel> 
{
	private MainController mainController;

	private boolean init;

	public ViewerButtonCellRenderer(MainController mainController) 
	{
		this.mainController = mainController;
	}

	public Object render(final BeanModel model, String property,
			ColumnData config, final int rowIndex, final int colIndex,
			ListStore<BeanModel> store, Grid<BeanModel> grid) {
		if (!init) {
			init = true;
			grid.addListener(Events.ColumnResize,
					new Listener<GridEvent<BeanModel>>() {

						public void handleEvent(GridEvent<BeanModel> be) 
						{
							for (int i = 0; i < be.getGrid().getStore()
									.getCount(); i++) {
								if (be.getGrid().getView()
										.getWidget(i, be.getColIndex()) != null
										&& be.getGrid().getView()
												.getWidget(i, be.getColIndex()) instanceof BoxComponent) {
									((BoxComponent) be.getGrid().getView()
											.getWidget(i, be.getColIndex()))
											.setWidth(be.getWidth() - 10);
								}
							}
						}
					});
		}

		Button viewerButton = new Button(MainController.CONSTANTS.results_grid_viewer_button(),
			new SelectionListener<ButtonEvent>() 
			{
				@Override
				public void componentSelected(ButtonEvent ce) 
				{
					mainController.runViewer(String.valueOf(model.get("id")));
				}
			});

		viewerButton.setWidth(grid.getColumnModel().getColumnWidth(colIndex) - 10);
		viewerButton.setToolTip(MainController.CONSTANTS.results_grid_viewer_button_tooltip());

		return viewerButton;
	}

}
