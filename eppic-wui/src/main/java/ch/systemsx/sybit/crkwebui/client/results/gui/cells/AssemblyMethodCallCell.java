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
import com.sencha.gxt.data.shared.ListStore;

import eppic.model.ScoringMethod;

/**
 * Cell used to used to style the results of calculations for each of the method call (xtal/bio).
 * @author nikhil
 *
 */
public class AssemblyMethodCallCell extends AbstractCell<String> {

	private ListStore<AssemblyItemModel> itemsStore;
	private String type;

	public AssemblyMethodCallCell(ListStore<AssemblyItemModel> itemsStore, String type){
		this.itemsStore = itemsStore;
		this.type = type;
	}

	@Override
	public void render(Context context, String value, SafeHtmlBuilder sb) {
		String tooltipText = null;

		int row = context.getIndex();
		AssemblyItemModel item = itemsStore.get(row);

		/*int interfaceId = item.getInterfaceId();
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
		}*/


		String color = "black";

		if (value.equals("bio")) 
		{
			color = "green";
		}
		else if (value.equals("xtal")) 
		{
			color = "red";
		}

		if(type.equals(ScoringMethod.EPPIC_FINAL)) {
		    value = value.toUpperCase();
		    // disabling this for 2.1.0 release, put back when confidence calculation implementation is better - JD 07.07.2014
		    //value += addIcon(item.getConfidence());
		}

		tooltipText = EscapedStringGenerator.generateEscapedString(tooltipText);
		tooltipText = "<div class=\"eppic-default-font eppic-results-grid-tooltip\">" + tooltipText + "</div>";
		sb.appendHtmlConstant("<span style='color:" + color + ";' qtip='" + tooltipText + "'>"+ value +"</span>");
	}

	@SuppressWarnings("unused")
	private String addIcon(double d) {
	    if(d > .66)
		return "<img src=\"resources/icons/excellent.png\" width=\"16\">";
	    if(d > .33)
		return "</img><img src=\"resources/icons/good.png\" width=\"16\"></img>";
	    return "";
	}

}