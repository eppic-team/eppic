package ch.systemsx.sybit.crkwebui.client.gui.renderers;

import ch.systemsx.sybit.crkwebui.client.controllers.AppPropertiesManager;
import ch.systemsx.sybit.crkwebui.client.controllers.ApplicationContext;
import ch.systemsx.sybit.crkwebui.client.controllers.EventBusManager;
import ch.systemsx.sybit.crkwebui.client.data.TooltipXPositionType;
import ch.systemsx.sybit.crkwebui.client.data.TooltipYPositionType;
import ch.systemsx.sybit.crkwebui.client.events.SelectResultsRowEvent;
import ch.systemsx.sybit.crkwebui.client.events.ShowViewerEvent;
import ch.systemsx.sybit.crkwebui.client.gui.ImageWithTooltip;

import com.extjs.gxt.ui.client.data.BaseModel;
import com.extjs.gxt.ui.client.store.ListStore;
import com.extjs.gxt.ui.client.widget.grid.ColumnData;
import com.extjs.gxt.ui.client.widget.grid.Grid;
import com.extjs.gxt.ui.client.widget.grid.GridCellRenderer;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;

/**
 * Renderer used to display interfaces thumbnails.
 * @author srebniak_a
 *
 */
public class ThumbnailCellRenderer implements GridCellRenderer<BaseModel> 
{
	@Override
	public Object render(final BaseModel model, String property,
			ColumnData config, final int rowIndex, int colIndex,
			ListStore<BaseModel> store, final Grid<BaseModel> grid) 
	{
		String url = ApplicationContext.getSettings().getResultsLocation();
		
		String source = url + 
						ApplicationContext.getPdbScoreItem().getJobId() + 
						"/" +
						ApplicationContext.getPdbScoreItem().getPdbName() +
						"." +
						model.get("id") +
						".75x75.png";
		
		ImageWithTooltip imageWithTooltip = new ImageWithTooltip(source, 
																 null,
																 AppPropertiesManager.CONSTANTS.results_grid_thumbnail_tooltip_text(),
																 -1, 
																 true, 
																 1000, 
																 0, 
																 TooltipXPositionType.RIGHT, 
																 TooltipYPositionType.TOP);
		
		imageWithTooltip.addClickHandler(new ClickHandler() 
		{
			@Override
			public void onClick(ClickEvent event)
			{
				EventBusManager.EVENT_BUS.fireEvent(new SelectResultsRowEvent(rowIndex));
				EventBusManager.EVENT_BUS.fireEvent(new ShowViewerEvent());
			}
		});
		
		return imageWithTooltip;
	}
}