/**
 * 
 */
package ch.systemsx.sybit.crkwebui.client.results.gui.cells;



import com.google.gwt.cell.client.AbstractCell;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;

/**
 * Cell used to used to style the results of calculations for each of the method call (xtal/bio).
 * @author nikhil
 *
 */
public class SubscriptTypeCell extends AbstractCell<String> {

	public SubscriptTypeCell(){
	}

	@Override
	public void render(Context context, String value, SafeHtmlBuilder sb) {
		
		if (value.indexOf("(") != -1)
			value = value.replace("(", "<sub>");
		if (value.indexOf(")") != -1)
			value = value.replace(")", "</sub>");		

		value = value.toUpperCase();
		sb.appendHtmlConstant(value);
	}

}
