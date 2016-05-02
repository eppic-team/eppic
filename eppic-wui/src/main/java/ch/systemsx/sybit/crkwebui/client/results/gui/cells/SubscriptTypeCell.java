/**
 * 
 */
package ch.systemsx.sybit.crkwebui.client.results.gui.cells;

import java.util.List;

import ch.systemsx.sybit.crkwebui.client.commons.appdata.ApplicationContext;
import ch.systemsx.sybit.crkwebui.client.commons.util.EscapedStringGenerator;
import ch.systemsx.sybit.crkwebui.client.results.data.AssemblyItemModel;
import ch.systemsx.sybit.crkwebui.client.results.data.InterfaceItemModel;
import ch.systemsx.sybit.crkwebui.shared.model.Interface;
import ch.systemsx.sybit.crkwebui.shared.model.InterfaceScore;

import com.google.gwt.cell.client.AbstractCell;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.user.client.Window;
import com.sencha.gxt.data.shared.ListStore;

import eppic.model.ScoringMethod;

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
