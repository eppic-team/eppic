package ch.systemsx.sybit.crkwebui.client.commons.gui.images;

import ch.systemsx.sybit.crkwebui.client.commons.appdata.ApplicationContext;
import ch.systemsx.sybit.crkwebui.client.commons.gui.data.TooltipXPositionType;
import ch.systemsx.sybit.crkwebui.client.commons.gui.data.TooltipYPositionType;

import com.extjs.gxt.ui.client.core.Template;
import com.extjs.gxt.ui.client.widget.tips.ToolTip;
import com.extjs.gxt.ui.client.widget.tips.ToolTipConfig;
import com.google.gwt.event.dom.client.MouseOutEvent;
import com.google.gwt.event.dom.client.MouseOutHandler;
import com.google.gwt.event.dom.client.MouseOverEvent;
import com.google.gwt.event.dom.client.MouseOverHandler;
import com.google.gwt.user.client.ui.Image;

/**
 * Image with tooltip component.
 */
public class ImageWithTooltip extends Image
{
	private ToolTip toolTip;
	private boolean refreshTooltip = true;

	/**
	 * Creates instance of image with tooltip with set input parameters.
	 * @param source url to the image
	 * @param title title of the tooltip
	 * @param template content of the tooltip
	 * @param maxWidth max width of the tooltip
	 * @param isAdjustable flag pointing whether minwidth and maxwidth are the same
	 * @param showDelay delay in milliseconds before tooltip shows on mouse over
	 * @param dismissDelay delay in milliseconds before tooltip hides on mouse out
	 * @param tooltipXPositionType x position of the tooltip to show
	 * @param tooltipYPositionType y position of the tooltip to show
	 */
	public ImageWithTooltip(String source,
							final String title,
							final String template,
							final int maxWidth,
							final boolean isAdjustable,
							final int showDelay,
							final int dismissDelay,
							final TooltipXPositionType tooltipXPositionType,
							final TooltipYPositionType tooltipYPositionType) 
	{
		super(source);
		
		this.addMouseOverHandler(new MouseOverHandler() {
			
			@Override
			public void onMouseOver(MouseOverEvent event)
			{
				if((toolTip != null) && (refreshTooltip))
				{
					toolTip.disable();
				}
				
				if(refreshTooltip)
				{
					ToolTipConfig toolTipConfig = new ToolTipConfig();  
					toolTipConfig.setMouseOffset(new int[] {0, 0});
					toolTipConfig.setTitle(title);
					toolTipConfig.setTemplate(new Template(template));  

					int width = maxWidth;
					
					if(maxWidth > 0)
					{
						if(width > ApplicationContext.getWindowData().getWindowWidth())
						{
							width = ImageWithTooltip.this.getAbsoluteLeft();
						}
						
						if(!isAdjustable)
						{
							toolTipConfig.setMinWidth(width);
						}
						
						toolTipConfig.setMaxWidth(width);
					}
					
					toolTipConfig.setShowDelay(showDelay);
					toolTipConfig.setDismissDelay(dismissDelay);
					
					toolTip = new ToolTip(null, toolTipConfig);
					
					toolTip.showAt(getXPosition(tooltipXPositionType, width), 
								   getYPosition(tooltipYPositionType));
					refreshTooltip = false;
				}
			}
		});
		
		this.addMouseOutHandler(new MouseOutHandler() 
		{
			@Override
			public void onMouseOut(MouseOutEvent event) 
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
	
	/**
	 * Generates x position of the tooltip.
	 * @param tooltipXPositionType type of the x position
	 * @param max width of the tooltip
	 * @return x position of the tooltip
	 */
	private int getXPosition(TooltipXPositionType tooltipXPositionType,
							 int tooltipWidth)
	{
		int tooltipXPosition = this.getAbsoluteLeft() + this.getOffsetWidth() + 5;
		
		if(tooltipXPositionType == TooltipXPositionType.LEFT)
		{
			tooltipXPosition = this.getAbsoluteLeft() - tooltipWidth;
		}
		
		return tooltipXPosition;
	}

	/**
	 * Generates y position of the tooltip.
	 * @param tooltipYPositionType type of the y position
	 * @return y position of the tooltip
	 */
	private int getYPosition(TooltipYPositionType tooltipYPositionType)
	{
		int tooltipYPosition = this.getAbsoluteTop() + this.getOffsetHeight() + 5;
	
		if(tooltipYPositionType == TooltipYPositionType.TOP)
		{
			tooltipYPosition = this.getAbsoluteTop();
		}
	
		return tooltipYPosition;
	}
}
