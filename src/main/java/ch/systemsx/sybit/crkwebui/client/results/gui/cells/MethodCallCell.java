/**
 * 
 */
package ch.systemsx.sybit.crkwebui.client.results.gui.cells;

import java.util.List;

import ch.systemsx.sybit.crkwebui.client.commons.appdata.ApplicationContext;
import ch.systemsx.sybit.crkwebui.client.commons.util.EscapedStringGenerator;
import ch.systemsx.sybit.crkwebui.client.results.data.InterfaceItemModel;
import ch.systemsx.sybit.crkwebui.shared.model.Interface;
import ch.systemsx.sybit.crkwebui.shared.model.InterfaceScore;

import com.google.gwt.cell.client.AbstractCell;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.sencha.gxt.data.shared.ListStore;

/**
 * Cell used to used to style the results of calculations for each of the method call (xtal/bio).
 * @author nikhil
 *
 */
public class MethodCallCell extends AbstractCell<String> {

	private ListStore<InterfaceItemModel> itemsStore;
	private String type;

	public MethodCallCell(ListStore<InterfaceItemModel> itemsStore, String type){
		this.itemsStore = itemsStore;
		this.type = type;
	}

	@Override
	public void render(Context context, String value, SafeHtmlBuilder sb) {
		String tooltipText = null;

		int row = context.getIndex();
		InterfaceItemModel item = itemsStore.get(row);

		int interfaceId = item.getInterfaceId();
		Interface interfaceItem = ApplicationContext.getPdbInfo().getInterface(interfaceId);

		if(interfaceItem != null)
		{
			List<InterfaceScore> interfaceScoreItemList = interfaceItem.getInterfaceScores();

			if(interfaceScoreItemList != null)
			{
				for(InterfaceScore interfaceScore : interfaceScoreItemList)
				{
					if(interfaceScore.getMethod().equals(type))
						tooltipText = interfaceScore.getCallReason();

					if(tooltipText != null)
					{
						tooltipText = tooltipText.replaceAll("\n", "<br/>");
					}
				}
			}
		}


		String color = "black";

		if (value.equals("bio")) 
		{
			color = "green";
		}
		else if (value.equals("xtal")) 
		{
			color = "red";
		}

		if(type.equals("finalCallName")) value=value.toUpperCase();

		tooltipText = EscapedStringGenerator.generateEscapedString(tooltipText);
		tooltipText = "<div class=\"eppic-default-font eppic-results-grid-tooltip\">" + tooltipText + "</div>";
		sb.appendHtmlConstant("<span style='color:" + color + ";' qtip='" + tooltipText + "'>"+ value +"</span>");
	}

}
