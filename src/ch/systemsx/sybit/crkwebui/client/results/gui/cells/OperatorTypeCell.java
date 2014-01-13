package ch.systemsx.sybit.crkwebui.client.results.gui.cells;

import ch.systemsx.sybit.crkwebui.client.commons.appdata.ApplicationContext;
import ch.systemsx.sybit.crkwebui.client.results.data.InterfaceItemModel;
import ch.systemsx.sybit.crkwebui.shared.model.InterfaceItem;

import com.google.gwt.cell.client.AbstractCell;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.sencha.gxt.data.shared.ListStore;

/**
 * Cell used to style operator type.
 * @author nikhil
 *
 */
public class OperatorTypeCell extends AbstractCell<String> {
	
	private ListStore<InterfaceItemModel> itemsStore;
	
	public OperatorTypeCell(ListStore<InterfaceItemModel> itemsStore){
		this.itemsStore = itemsStore;
	}

	@Override
	public void render(com.google.gwt.cell.client.Cell.Context context,
			String value, SafeHtmlBuilder sb) {
		String iconsDir = "resources/icons/";

		if (value != null) {
			String tooltipText = null;
			String icon = "optype_" + value ;
			
			int row = context.getIndex();
			InterfaceItemModel item = itemsStore.get(row);
			int interfaceId = item.getId();
		
			InterfaceItem interfaceItem = ApplicationContext.getPdbScoreItem().getInterfaceItem(interfaceId - 1);
			if(interfaceItem != null)
			{
				tooltipText = interfaceItem.getOperator();
				if (interfaceItem.getIsInfinite()) icon += "_inf";
			}
			
			icon+=".png"; // either optype_2S.png or optype_2S_red.png
			
			String source = iconsDir+icon;
			
			tooltipText = "<div class=\"eppic-default-font eppic-results-grid-tooltip\">" + tooltipText + "</div>";
			sb.appendHtmlConstant("<img src='"+ source + 
					"' qtip='" + tooltipText + "' />");
		
		// to make it compatible with older versions without operatorType in the model, 
		// we return the operator string if no operator type is present			
		} else { 
			int row = context.getIndex();
			InterfaceItemModel item = itemsStore.get(row);
			int interfaceId = item.getId();
			
			InterfaceItem interfaceItem = ApplicationContext.getPdbScoreItem().getInterfaceItem(interfaceId - 1);
			if(interfaceItem != null)
			{
				String operator = interfaceItem.getOperator();
				value = operator;
			}
		}	

	}
}
