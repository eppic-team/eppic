package ch.systemsx.sybit.crkwebui.client.gui.renderers;

import ch.systemsx.sybit.crkwebui.client.controllers.ApplicationContext;
import ch.systemsx.sybit.crkwebui.client.gui.LabelWithTooltip;
import ch.systemsx.sybit.crkwebui.shared.model.InterfaceItem;

import com.extjs.gxt.ui.client.data.BaseModel;
import com.extjs.gxt.ui.client.store.ListStore;
import com.extjs.gxt.ui.client.widget.grid.ColumnData;
import com.extjs.gxt.ui.client.widget.grid.Grid;
import com.extjs.gxt.ui.client.widget.grid.GridCellRenderer;

/**
 * Renderer used to style final result.
 * @author srebniak_a
 *
 */
public class FinalCallCellRenderer implements GridCellRenderer<BaseModel> 
{
	@Override
	public Object render(final BaseModel model, final String property,
			ColumnData config, final int rowIndex, final int colIndex,
			ListStore<BaseModel> store, final Grid<BaseModel> grid) {
		
		String value = (String) model.get(property);

		if (value != null) 
		{
			String tooltipText = null;
			
			int interfaceId = (Integer)model.get("id");
			
			InterfaceItem interfaceItem = ApplicationContext.getPdbScoreItem().getInterfaceItem(interfaceId - 1);
			
			if(interfaceItem != null)
			{
				tooltipText = interfaceItem.getFinalCallReason();
				
				if(tooltipText != null)
				{
					tooltipText = tooltipText.replaceAll("\n", "<br/>");
				}
			}
			
			LabelWithTooltip callReasonLabel = new LabelWithTooltip(value, 
																	tooltipText, 
																	ApplicationContext.getWindowData(), 
																	100);
			
			String color = "black";

			if (value.equals("bio")) 
			{
				color = "green";
			}
			else if (value.equals("xtal")) 
			{
				color = "red";
			}
			
			callReasonLabel.setStyleAttribute("color", color);
			callReasonLabel.addStyleName("eppic-results-final-call");
			
			return callReasonLabel;
		}

		return value;
	}
}
