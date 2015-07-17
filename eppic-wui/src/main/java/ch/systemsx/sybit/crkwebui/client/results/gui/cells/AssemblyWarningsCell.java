package ch.systemsx.sybit.crkwebui.client.results.gui.cells;

import java.util.List;

import ch.systemsx.sybit.crkwebui.client.commons.appdata.AppPropertiesManager;
import ch.systemsx.sybit.crkwebui.client.commons.util.EscapedStringGenerator;
import ch.systemsx.sybit.crkwebui.client.results.data.AssemblyItemModel;
import ch.systemsx.sybit.crkwebui.client.results.data.InterfaceItemModel;
import ch.systemsx.sybit.crkwebui.shared.model.InterfaceWarning;

import com.google.gwt.cell.client.AbstractCell;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.sencha.gxt.data.shared.ListStore;

/**
 * Cell used to display warning icon.
 * @author nikhil
 *
 */
public class AssemblyWarningsCell extends AbstractCell<String> {

	private ListStore<AssemblyItemModel> itemsStore;
	
	public AssemblyWarningsCell(ListStore<AssemblyItemModel> itemsStore){
		this.itemsStore = itemsStore;
	}
	@Override
	public void render(Context context, String value, SafeHtmlBuilder sb) {
		
		int row = context.getIndex();
		AssemblyItemModel item = itemsStore.get(row);
		
		//final List<InterfaceWarning> warnings = item.getWarnings();
		final List<InterfaceWarning> warnings = null; //????
		
		if((warnings != null) && (warnings.size() > 0))
		{
			sb.appendHtmlConstant("<img src='"+ value + 
					"' qtitle='"+ 
					AppPropertiesManager.CONSTANTS.results_grid_warnings_tooltip_title() +
					"' qtip='" + generateWarningsTemplate(warnings) + "'/>");
		}
		else
		{
			value = "";
		}
	}

	/**
	 * Creates list of interface warnings.
	 * @param warnings list of warnings to display
	 * @return template containing list of warnings
	 */
	private String generateWarningsTemplate(List<InterfaceWarning> warnings)
	{
		String warningsList = "<div><ul class=\"eppic-default-font eppic-results-grid-tooltip eppic-tooltip-list\">";
		
		for(InterfaceWarning warning : warnings)
		{
			if(!warning.getText().equals(""))
			{
				warningsList += "<li>" + EscapedStringGenerator.generateSanitizedString(warning.getText()) + "</li>";
			}
		}
			
		warningsList += "</ul></div>";
		
		return warningsList;
	}
}
