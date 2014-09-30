package ch.systemsx.sybit.crkwebui.client.search.gui.cells;

import com.google.gwt.cell.client.AbstractCell;
import com.google.gwt.i18n.client.NumberFormat;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;

/**
 * Cell used to display double values with two decimal digits for pdb data like resolution, rfree
 * @author nikhil
 *
 */
public class PdbDataDoubleCell extends AbstractCell<Double>  
{
	private NumberFormat number = NumberFormat.getFormat("0.00");

	@Override
	public void render(com.google.gwt.cell.client.Cell.Context context,
			Double value, SafeHtmlBuilder sb) {
		
		String v;
		if(value >= 0) v = number.format(value);
		else v = "";
		
		sb.appendHtmlConstant("<span>"+ v +"</span>");
	}  
	
}
