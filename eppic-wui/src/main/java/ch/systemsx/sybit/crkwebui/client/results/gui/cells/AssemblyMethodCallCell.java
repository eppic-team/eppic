/**
 * 
 */
package ch.systemsx.sybit.crkwebui.client.results.gui.cells;

import ch.systemsx.sybit.crkwebui.client.results.data.AssemblyItemModel;

import com.google.gwt.cell.client.AbstractCell;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.sencha.gxt.data.shared.ListStore;

import eppic.EppicParams;


/**
 * Cell used to used to style the results of calculations for each of the method call (xtal/bio).
 * @author Althea Parker
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

		//get the callReason here! and put it into the tooltip.
		tooltipText = item.getCallReason();
		
		String color = "black";

		if (value.equalsIgnoreCase("bio")) 
		{
			color = "green";
		}
		else if (value.equalsIgnoreCase("xtal")) 
		{
			color = "red";
		}

		value = value.toUpperCase();
		
		value += addIcon(item.isPdb1Assembly());
		
		value += addConfidenceIcon(item.getConfidence());

		sb.appendHtmlConstant("<span style='color:" + color + ";' qtip='" + tooltipText + "'>"+ value +"</span>");
	}

	private String addIcon(boolean pdb1Assembly) {
	    if(pdb1Assembly)
	    	return "<img src=\"resources/icons/pdb_biounit_annotation.png\" width=\"18\">";
	    else 
	    	return "";
	}
	
	private String addConfidenceIcon(double confidence) {
	    if(confidence > EppicParams.HIGH_PROB_CONFIDENCE)
	    	return "<img src=\"resources/icons/excellent.png\" width=\"16\">";
	    if(confidence > EppicParams.LOW_PROB_CONFIDENCE)
	    	return "</img><img src=\"resources/icons/good.png\" width=\"16\"></img>";
	    return "";
	}

}
