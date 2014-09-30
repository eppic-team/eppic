package ch.systemsx.sybit.crkwebui.client.jobs.gui.cells;

import ch.systemsx.sybit.crkwebui.client.commons.appdata.AppPropertiesManager;
import ch.systemsx.sybit.crkwebui.shared.model.StatusOfJob;

import com.google.gwt.cell.client.AbstractCell;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;


/**
 * Cell used to display styled status of the job.
 * @author nikhil
 *
 */
public class JobStatusCell extends AbstractCell<String>
{

	@Override
	public void render(com.google.gwt.cell.client.Cell.Context context,
			String value, SafeHtmlBuilder sb) {
//		String color = "black";
//
//		if (value.equals(StatusOfJob.ERROR.getName())) {
//			color = "red";
//		} else if (value.equals(StatusOfJob.FINISHED.getName())) {
//			color = "green";
//		} else if (value.equals(StatusOfJob.QUEUING.getName())) {
//			color = "orange";
//		} else if (value.equals(StatusOfJob.WAITING.getName())) {
//			color = "blue";
//		}
//		
//		String style = "style='color: " + color + "'";
		
		String imgName = "";
		
		if(value.equals(StatusOfJob.ERROR.getName()))
			imgName = AppPropertiesManager.CONSTANTS.myjobs_ERROR();
		else if(value.equals(StatusOfJob.FINISHED.getName()))
			imgName = AppPropertiesManager.CONSTANTS.myjobs_FINISHED();
		else if(value.equals(StatusOfJob.NONEXISTING.getName()))
			imgName = AppPropertiesManager.CONSTANTS.myjobs_NONEXISTING();
		else if(value.equals(StatusOfJob.QUEUING.getName()))
			imgName = AppPropertiesManager.CONSTANTS.myjobs_QUEUING();
		else if(value.equals(StatusOfJob.RUNNING.getName()))
			imgName = AppPropertiesManager.CONSTANTS.myjobs_RUNNING();
		else if(value.equals(StatusOfJob.STOPPED.getName()))
			imgName = AppPropertiesManager.CONSTANTS.myjobs_STOPPED();
		else if(value.equals(StatusOfJob.WAITING.getName()))
			imgName = AppPropertiesManager.CONSTANTS.myjobs_WAITING();
		
		if(imgName != ""){
			String source = "resources/icons/status/" + imgName;
			sb.appendHtmlConstant("<img src='" + source + "' qtip='" + value + "' alt='"+ value + "'/>");
		}
		
	}
	
}
