package ch.systemsx.sybit.crkwebui.client.results.gui.renderers;

import ch.systemsx.sybit.crkwebui.client.commons.appdata.AppPropertiesManager;
import ch.systemsx.sybit.crkwebui.client.commons.events.SelectResultsRowEvent;
import ch.systemsx.sybit.crkwebui.client.commons.events.ShowDetailsEvent;
import ch.systemsx.sybit.crkwebui.client.commons.gui.renderers.DefaultCellRenderer;
import ch.systemsx.sybit.crkwebui.client.commons.managers.EventBusManager;

import com.extjs.gxt.ui.client.data.BaseModel;
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

/**
 * Renderer used to display details button which shows the residues.
 * @author srebniak_a
 *
 */
public class DetailsButtonCellRenderer extends DefaultCellRenderer 
{
	private boolean init;

	@Override
	public Object render(final BaseModel model, String property,
			ColumnData config, final int rowIndex, final int colIndex,
			ListStore<BaseModel> store, Grid<BaseModel> grid) 
	{
		if (!init) {
			init = true;
			grid.addListener(Events.ColumnResize,
					new Listener<GridEvent<BaseModel>>() {

						public void handleEvent(GridEvent<BaseModel> be) {
							for (int i = 0; i < be.getGrid().getStore()
									.getCount(); i++) {
								if (be.getGrid().getView()
										.getWidget(i, be.getColIndex()) != null
										&& be.getGrid().getView()
												.getWidget(i, be.getColIndex()) instanceof BoxComponent) {
									((BoxComponent) be.getGrid().getView()
											.getWidget(i, be.getColIndex()))
											.setWidth(be.getWidth() - 15);
								}
							}
						}
					});
		}

		Button detailsButton = new Button(AppPropertiesManager.CONSTANTS.results_grid_details_button(),
				new SelectionListener<ButtonEvent>() {

					@Override
					public void componentSelected(ButtonEvent ce) 
					{
						EventBusManager.EVENT_BUS.fireEvent(new SelectResultsRowEvent(rowIndex));
						EventBusManager.EVENT_BUS.fireEvent(new ShowDetailsEvent());
					}
				});

		detailsButton
				.setWidth(grid.getColumnModel().getColumnWidth(colIndex) - 15);
		detailsButton.setToolTip(AppPropertiesManager.CONSTANTS.results_grid_details_button_tooltip());

		return detailsButton;
	}

}
