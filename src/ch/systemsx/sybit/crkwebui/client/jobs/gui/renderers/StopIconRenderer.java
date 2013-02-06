package ch.systemsx.sybit.crkwebui.client.jobs.gui.renderers;

import ch.systemsx.sybit.crkwebui.client.commons.appdata.AppPropertiesManager;
import ch.systemsx.sybit.crkwebui.client.commons.gui.data.TooltipXPositionType;
import ch.systemsx.sybit.crkwebui.client.commons.gui.data.TooltipYPositionType;
import ch.systemsx.sybit.crkwebui.client.commons.gui.images.ImageWithTooltip;
import ch.systemsx.sybit.crkwebui.client.commons.services.eppic.CrkWebServiceProvider;
import ch.systemsx.sybit.crkwebui.shared.model.StatusOfJob;

import com.extjs.gxt.ui.client.data.BaseModel;
import com.extjs.gxt.ui.client.store.ListStore;
import com.extjs.gxt.ui.client.widget.grid.ColumnData;
import com.extjs.gxt.ui.client.widget.grid.Grid;
import com.extjs.gxt.ui.client.widget.grid.GridCellRenderer;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;

/**
 * Renderer used to display icon used to stop job.
 * @author AS
 *
 */
public class StopIconRenderer implements GridCellRenderer<BaseModel>
{
	@Override
	public Object render(final BaseModel model, String property,
			ColumnData config, final int rowIndex, final int colIndex,
			ListStore<BaseModel> store, final Grid<BaseModel> grid)
	{
		String status = model.get("status");

		if((status != null) &&
		   ((status.equals(StatusOfJob.RUNNING.getName())) ||
		    (status.equals(StatusOfJob.QUEUING.getName()))))
		{
			String source = "resources/icons/stop_icon.png";
			
			ImageWithTooltip imageWithTooltip = new ImageWithTooltip(source,
					 null,
					 AppPropertiesManager.CONSTANTS.myjobs_grid_stop_tooltip(),
					 -1, 
					 true, 
					 0, 
					 0, 
					 TooltipXPositionType.RIGHT, 
					 TooltipYPositionType.TOP);

			imageWithTooltip.addClickHandler(new ClickHandler()
			{
				@Override
				public void onClick(ClickEvent event)
				{
					BaseModel selectedItem = grid.getSelectionModel().getSelectedItem();
					CrkWebServiceProvider.getServiceController().stopJob(String.valueOf(selectedItem.get("jobid")));
				}
			});

			return imageWithTooltip;
		}
		else
		{
			return "";
		}
	}
}
