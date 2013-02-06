package ch.systemsx.sybit.crkwebui.client.jobs.gui.renderers;

import ch.systemsx.sybit.crkwebui.client.commons.gui.renderers.DefaultCellRenderer;
import ch.systemsx.sybit.crkwebui.client.commons.util.EscapedStringGenerator;
import ch.systemsx.sybit.crkwebui.shared.model.StatusOfJob;

import com.extjs.gxt.ui.client.data.BaseModel;
import com.extjs.gxt.ui.client.store.ListStore;
import com.extjs.gxt.ui.client.widget.Label;
import com.extjs.gxt.ui.client.widget.grid.ColumnData;
import com.extjs.gxt.ui.client.widget.grid.Grid;

/**
 * Renderer used to display styled status of the job.
 * @author srebniak_a
 *
 */
public class JobStatusCellRenderer extends DefaultCellRenderer 
{
	@Override
	public Object render(final BaseModel model, 
						 String property,
						 ColumnData config, 
						 final int rowIndex, 
						 final int colIndex,
						 ListStore<BaseModel> store, 
						 final Grid<BaseModel> grid) 
	{
		String value = EscapedStringGenerator.generateEscapedString((String) model.get(property));
		String color = "black";

		if (value == null) {
			return value;
		} else if (value.equals(StatusOfJob.ERROR.getName())) {
			color = "red";
		} else if (value.equals(StatusOfJob.FINISHED.getName())) {
			color = "green";
		} else if (value.equals(StatusOfJob.QUEUING.getName())) {
			color = "orange";
		} else if (value.equals(StatusOfJob.WAITING.getName())) {
			color = "blue";
		} else {
			return value;
		}

		Label jobStatusLabel = new Label(value);
		jobStatusLabel.setStyleAttribute("color", color);
		jobStatusLabel.addStyleName("eppic-my-jobs-list-status");
		return jobStatusLabel;
	}
}
