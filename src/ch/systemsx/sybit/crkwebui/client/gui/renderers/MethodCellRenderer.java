package ch.systemsx.sybit.crkwebui.client.gui.renderers;

import java.util.ArrayList;
import java.util.List;

import ch.systemsx.sybit.crkwebui.client.controllers.MainController;

import com.extjs.gxt.ui.client.core.Template;
import com.extjs.gxt.ui.client.data.BeanModel;
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
public class MethodCellRenderer implements GridCellRenderer<BeanModel> 
{
	private MainController mainController;
	
	private ToolTip toolTip;
	private boolean refreshTooltip = true;
	
	public MethodCellRenderer(MainController mainController) 
	{
		this.mainController = mainController;
	}

	public Object render(final BeanModel model, final String property,
			ColumnData config, final int rowIndex, final int colIndex,
			ListStore<BeanModel> store, final Grid<BeanModel> grid) {
		
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
			else
			{
				final List<String> warnings = new ArrayList<String>();
				warnings.add("This is the reason why nopred");
				
				final Label nopredLabel = new Label(value);
				
				nopredLabel.addListener(Events.OnMouseOver, new Listener<BaseEvent>() {

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
							toolTipConfig.setTemplate(new Template(generateNopredTemplate(warnings)));  
							
							int width = 500;
							if(width > mainController.getWindowWidth())
							{
								width = nopredLabel.getAbsoluteLeft();
							}
							
							int toolTipXPosition = nopredLabel.getAbsoluteLeft() - width;
							
							toolTipConfig.setMinWidth(width);
							toolTipConfig.setMaxWidth(width);
							toolTipConfig.setShowDelay(100);
							toolTipConfig.setDismissDelay(0);
							
							toolTip = new ToolTip(null, toolTipConfig);
							
							toolTip.showAt(toolTipXPosition, 
										   nopredLabel.getAbsoluteTop() + nopredLabel.getOffsetWidth() + 5);
							refreshTooltip = false;
						}
					}
					
				});
				
				nopredLabel.addListener(Events.OnMouseOut, new Listener<BaseEvent>() {
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
				
				
				return nopredLabel;
			}

			return "<span style='font-weight: bold;color:" + color + "'>"
					+ value + "</span>";
		}

		return value;
	}
	
	private String generateNopredTemplate(List<String> warnings)
	{
		String warningsList = "<div><ul style=\"list-style: disc; margin: 0px 0px 0px 15px;\">";
		
		for(String warning : warnings)
		{
			if(!warning.equals(""))
			{
				warningsList += "<li>" + warning + "</li>";
			}
		}
			
		warningsList += "</ul></div>";
		
		return warningsList;
	}

}
