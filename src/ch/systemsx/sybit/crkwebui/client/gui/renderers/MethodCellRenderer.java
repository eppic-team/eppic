package ch.systemsx.sybit.crkwebui.client.gui.renderers;

import java.util.List;

import ch.systemsx.sybit.crkwebui.client.controllers.ApplicationContext;
import ch.systemsx.sybit.crkwebui.client.gui.LabelWithTooltip;
import ch.systemsx.sybit.crkwebui.client.gui.util.EscapedStringGenerator;
import ch.systemsx.sybit.crkwebui.shared.model.InterfaceItem;
import ch.systemsx.sybit.crkwebui.shared.model.InterfaceScoreItem;

import com.extjs.gxt.ui.client.data.BaseModel;
import com.extjs.gxt.ui.client.store.ListStore;
import com.extjs.gxt.ui.client.widget.grid.ColumnData;
import com.extjs.gxt.ui.client.widget.grid.Grid;
import com.extjs.gxt.ui.client.widget.grid.GridCellRenderer;

/**
 * Renderer used to style the results of calculations for each of the method.
 * @author srebniak_a
 *
 */
public class MethodCellRenderer implements GridCellRenderer<BaseModel> 
{
	public MethodCellRenderer() 
	{
		
	}

	@Override
	public Object render(final BaseModel model, final String property,
			ColumnData config, final int rowIndex, final int colIndex,
			ListStore<BaseModel> store, final Grid<BaseModel> grid) {
		
		String value = EscapedStringGenerator.generateEscapedString((String) model.get(property));

		if (value != null) 
		{
			String tooltipText = null;
			
			int interfaceId = (Integer)model.get("id");
			
			InterfaceItem interfaceItem = ApplicationContext.getPdbScoreItem().getInterfaceItem(interfaceId - 1);
			
			if(interfaceItem != null)
			{
				List<InterfaceScoreItem> interfaceScoreItemList = interfaceItem.getInterfaceScores();
				
				if(interfaceScoreItemList != null)
				{
					for(InterfaceScoreItem interfaceScoreItem : interfaceScoreItemList)
					{
						if(interfaceScoreItem.getMethod().equals(property))
						{
							tooltipText = interfaceScoreItem.getCallReason();
							
							if(tooltipText != null)
							{
								tooltipText = tooltipText.replaceAll("\n", "<br/>");
							}
						}
					}
				}
			}
			
			LabelWithTooltip callReasonLabel = new LabelWithTooltip(value, 
																	EscapedStringGenerator.generateSanitizedString(tooltipText), 
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
			
			return callReasonLabel;
		}

		return value;
	}
}
