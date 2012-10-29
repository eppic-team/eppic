package ch.systemsx.sybit.crkwebui.client.gui;

import ch.systemsx.sybit.crkwebui.shared.model.WindowData;

import com.extjs.gxt.ui.client.event.BaseEvent;
import com.extjs.gxt.ui.client.event.Events;
import com.extjs.gxt.ui.client.event.Listener;
import com.extjs.gxt.ui.client.widget.Label;
import com.extjs.gxt.ui.client.widget.tips.ToolTip;
import com.extjs.gxt.ui.client.widget.tips.ToolTipConfig;

/**
 * Label with assigned tooltip.
 * @author AS
 *
 */
public class LabelWithTooltip extends Label
{
	protected ToolTip toolTip;
	protected boolean refreshTooltip = true;
	
	/**
	 * Creates instance of label with assigned tooltip.
	 * @param labelText text of the label
	 * @param tooltipText text of the tooltip
	 * @param windowData general window data
	 * @param delay delay after which tooltip is displayed
	 */
	public LabelWithTooltip(String labelText,
							final String tooltipText,
							final WindowData windowData,
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
					if(width > windowData.getWindowWidth())
					{
						width = windowData.getWindowWidth() - 10;
					}
					
					int toolTipXPosition = LabelWithTooltip.this.getAbsoluteLeft() + LabelWithTooltip.this.getWidth() + 5;
					
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
	
	protected void onUnload()
	{
		if(toolTip != null)
		{
			toolTip.disable();
		}
		
		refreshTooltip = true;
	}
}
