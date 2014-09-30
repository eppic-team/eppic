package ch.systemsx.sybit.crkwebui.client.jobs.gui.cells;

import com.google.gwt.cell.client.AbstractCell;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;

/**
 * Cell used to display input value (pdb code or filename without extension).
 * @author nikhil
 */
public class InputCell extends AbstractCell<String>
{

	@Override
	public void render(com.google.gwt.cell.client.Cell.Context context,
			String value, SafeHtmlBuilder sb) {
		if(value.contains("."))
		{
			value = value.substring(0, value.indexOf("."));
		}
		
		sb.appendHtmlConstant("<span qtip='"+value+"'>"+value+"</span>");
		
	}
	
}
