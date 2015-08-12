package ch.systemsx.sybit.crkwebui.client.results.gui.grid.util;

import java.util.Map;

import ch.systemsx.sybit.crkwebui.client.results.data.AssemblyItemModel;
import ch.systemsx.sybit.crkwebui.client.results.data.InterfaceItemModel;

import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.user.client.Window;
import com.sencha.gxt.core.client.ValueProvider;
import com.sencha.gxt.widget.core.client.grid.SummaryRenderer;

/**
 * Renders the int returned from Summary Type to final bio/xtal/nopred
 * @author biyani_n
 *
 */
public class AssemblyMethodsSummaryRenderer implements SummaryRenderer<AssemblyItemModel>{

	@Override
	public SafeHtml render(Number value,
			Map<ValueProvider<? super AssemblyItemModel, ?>, Number> data) {
		return SafeHtmlUtils.fromSafeConstant(convertIntToCall((Integer)value));
	}
	
	protected String convertIntToCall(int callInt){
		if(callInt == 0){
			return "nopred";
		} else if(callInt == 1){
			return "bio";
		} else if(callInt == 2){
			return "xtal";
		} else{
			return "-";
		}
	}

}
