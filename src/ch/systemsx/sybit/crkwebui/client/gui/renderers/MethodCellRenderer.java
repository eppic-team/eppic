package ch.systemsx.sybit.crkwebui.client.gui.renderers;

import java.util.List;

import model.InterfaceItem;
import model.InterfaceScoreItem;
import ch.systemsx.sybit.crkwebui.client.controllers.MainController;

import com.extjs.gxt.ui.client.data.BaseModel;
import com.extjs.gxt.ui.client.event.BaseEvent;
import com.extjs.gxt.ui.client.event.Events;
import com.extjs.gxt.ui.client.event.Listener;
import com.extjs.gxt.ui.client.store.ListStore;
import com.extjs.gxt.ui.client.widget.Label;
import com.extjs.gxt.ui.client.widget.grid.ColumnData;
import com.extjs.gxt.ui.client.widget.grid.Grid;
import com.extjs.gxt.ui.client.widget.grid.GridCellRenderer;
import com.extjs.gxt.ui.client.widget.tips.ToolTip;
import com.extjs.gxt.ui.client.widget.tips.ToolTipConfig;

/**
 * This model is used to style the results of calculations for each of the method
 * @author srebniak_a
 *
 */
public class MethodCellRenderer implements GridCellRenderer<BaseModel> 
{
	private MainController mainController;
	
	private ToolTip toolTip;
	private boolean refreshTooltip = true;
	
	public MethodCellRenderer(MainController mainController) 
	{
		this.mainController = mainController;
	}

	public Object render(final BaseModel model, final String property,
			ColumnData config, final int rowIndex, final int colIndex,
			ListStore<BaseModel> store, final Grid<BaseModel> grid) {
		
		String value = (String) model.get(property);

		if (value != null) 
		{
			String color = "black";

			if (value.equals("bio")) 
			{
				color = "green";
			}
			else if (value.equals("xtal")) 
			{
				color = "red";
			}
			
			final Label callReasonLabel = new Label(value);
			String text = null;
			
			callReasonLabel.setStyleAttribute("color", color);

			int interfaceId = model.get("id");
			
			InterfaceItem interfaceItem = mainController.getPdbScoreItem().getInterfaceItem(interfaceId - 1);
			
			if(interfaceItem != null)
			{
				List<InterfaceScoreItem> interfaceScoreItemList = interfaceItem.getInterfaceScores();
				
				if(interfaceScoreItemList != null)
				{
					for(InterfaceScoreItem interfaceScoreItem : interfaceScoreItemList)
					{
						if(interfaceScoreItem.getMethod().equals(property))
						{
							text = interfaceScoreItem.getCallReason();
							
							if(text != null)
							{
								text = text.replaceAll("\n", "<br/>");
							}
						}
					}
				}
			}
			
			final String callReasonText = text;
			
			callReasonLabel.addListener(Events.OnMouseOver, new Listener<BaseEvent>()
			{
				@Override
				public void handleEvent(BaseEvent be) 
				{
					if((toolTip != null) && (refreshTooltip))
					{
						toolTip.disable();
					}
					
					if(refreshTooltip)
					{
						ToolTipConfig toolTipConfig = new ToolTipConfig();  
						toolTipConfig.setMouseOffset(new int[] {0, 0});  
						
						toolTipConfig.setText(callReasonText);  
						
						int width = 500;
						if(width > mainController.getWindowWidth())
						{
							width = mainController.getWindowWidth() - 10;
						}
						
						int toolTipXPosition = callReasonLabel.getAbsoluteLeft() + callReasonLabel.getWidth() + 5;
						
//							toolTipConfig.setMinWidth(width);
						toolTipConfig.setMaxWidth(width);
						toolTipConfig.setShowDelay(100);
						toolTipConfig.setDismissDelay(0);
						
						toolTip = new ToolTip(null, toolTipConfig);
						
						toolTip.showAt(toolTipXPosition, 
									   callReasonLabel.getAbsoluteTop() + callReasonLabel.getHeight() + 5);
						refreshTooltip = false;
					}
				}
			});
		
			callReasonLabel.addListener(Events.OnMouseOut, new Listener<BaseEvent>()
			{
				@Override
				public void handleEvent(BaseEvent be) 
				{
					if(toolTip != null)
					{
						toolTip.disable();
					}
					
					refreshTooltip = true;
				}
			});
			
			return callReasonLabel;
		}

		return value;
	}
}
