package ch.systemsx.sybit.crkwebui.client.gui;

import ch.systemsx.sybit.crkwebui.client.controllers.MainController;

import com.extjs.gxt.ui.client.event.BaseEvent;
import com.extjs.gxt.ui.client.event.Events;
import com.extjs.gxt.ui.client.event.Listener;
import com.extjs.gxt.ui.client.widget.Label;
import com.extjs.gxt.ui.client.widget.tips.ToolTip;
import com.extjs.gxt.ui.client.widget.tips.ToolTipConfig;

public class LabelWithTooltip extends Label
{
	protected ToolTip toolTip;
	protected boolean refreshTooltip = true;
	
	public LabelWithTooltip(String labelText,
							final String tooltipText,
							final MainController mainController,
							final int delay)
	{
		super(labelText);
		
		this.addListener(Events.OnMouseOver, new Listener<BaseEvent>()
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
					
					toolTipConfig.setText(tooltipText);  
					
					int width = 500;
					if(width > mainController.getWindowWidth())
					{
						width = mainController.getWindowWidth() - 10;
					}
					
					int toolTipXPosition = LabelWithTooltip.this.getAbsoluteLeft() + LabelWithTooltip.this.getWidth() + 5;
					
//						toolTipConfig.setMinWidth(width);
					toolTipConfig.setMaxWidth(width);
					toolTipConfig.setShowDelay(delay);
					toolTipConfig.setDismissDelay(0);
					
					toolTip = new ToolTip(null, toolTipConfig);
					
					toolTip.showAt(toolTipXPosition, 
							LabelWithTooltip.this.getAbsoluteTop() + LabelWithTooltip.this.getHeight() + 5);
					refreshTooltip = false;
				}
			}
		});
	
		this.addListener(Events.OnMouseOut, new Listener<BaseEvent>()
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
	}
}
