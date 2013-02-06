package ch.systemsx.sybit.crkwebui.client.jobs.gui.renderers;

import ch.systemsx.sybit.crkwebui.client.commons.appdata.AppPropertiesManager;
import ch.systemsx.sybit.crkwebui.client.commons.gui.data.TooltipXPositionType;
import ch.systemsx.sybit.crkwebui.client.commons.gui.data.TooltipYPositionType;
import ch.systemsx.sybit.crkwebui.client.commons.gui.images.ImageWithTooltip;
import ch.systemsx.sybit.crkwebui.client.commons.services.eppic.CrkWebServiceProvider;

import com.extjs.gxt.ui.client.data.BaseModel;
import com.extjs.gxt.ui.client.store.ListStore;
import com.extjs.gxt.ui.client.widget.grid.ColumnData;
import com.extjs.gxt.ui.client.widget.grid.Grid;
import com.extjs.gxt.ui.client.widget.grid.GridCellRenderer;
import com.google.gwt.event.dom.client.DoubleClickEvent;
import com.google.gwt.event.dom.client.DoubleClickHandler;

/**
 * Renderer used to display icon used to delete job.
 * @author AS
 */
public class DeleteJobCellRenderer implements GridCellRenderer<BaseModel> 
{
	@Override
	public Object render(final BaseModel model, String property,
			ColumnData config, final int rowIndex, final int colIndex,
			ListStore<BaseModel> store, final Grid<BaseModel> grid) 
	{
		String source = "resources/icons/delete_icon.png";
		
		ImageWithTooltip imageWithTooltip = new ImageWithTooltip(source,
																 null,
																 AppPropertiesManager.CONSTANTS.myjobs_grid_delete_tooltip(), 
																 -1, 
																 true, 
																 0, 
																 0, 
																 TooltipXPositionType.RIGHT, 
																 TooltipYPositionType.TOP);
		
		imageWithTooltip.addDoubleClickHandler(new DoubleClickHandler() 
		{
			@Override
			public void onDoubleClick(DoubleClickEvent event)
			{
				CrkWebServiceProvider.getServiceController().deleteJob(String.valueOf(model.get("jobid")));
			}
		});
		
		return imageWithTooltip;
	}
}
