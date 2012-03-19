package ch.systemsx.sybit.crkwebui.client.gui.renderers;

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
		String value = (String) model.get(property);
		String color = "black";

		if (value == null) {
			return value;
		} else if (value.equals(StatusOfJob.ERROR.getName())) {
			color = "red";
		} else if (value.equals(StatusOfJob.FINISHED.getName())) {
			color = "green";
		} else {
			return value;
		}

		Label jobStatusLabel = new Label(value);
		jobStatusLabel.setStyleAttribute("color", color);
		jobStatusLabel.setStyleAttribute("font-weight", "bold");
		return jobStatusLabel;
	}
}
