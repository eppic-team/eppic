package ch.systemsx.sybit.crkwebui.client.commons.gui.cell;

import com.google.gwt.cell.client.AbstractCell;
import com.google.gwt.i18n.client.NumberFormat;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;

/**
 * Cell used to display float values with two decimal digits
 * @author nikhil
 *
 */
public class TwoDecimalFloatCell extends AbstractCell<Float>  
{
	private NumberFormat number = NumberFormat.getFormat("0.00");

	@Override
	public void render(com.google.gwt.cell.client.Cell.Context context,
			Float value, SafeHtmlBuilder sb) {
		String v = number.format(value);
		sb.appendHtmlConstant("<span>"+ v +"</span>");
	}  
	
}
