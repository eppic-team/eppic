package ch.systemsx.sybit.crkwebui.client.gui;

import ch.systemsx.sybit.crkwebui.client.controllers.MainController;

import com.extjs.gxt.ui.client.widget.WidgetComponent;
import com.extjs.gxt.ui.client.widget.tips.ToolTip;
import com.extjs.gxt.ui.client.widget.tips.ToolTipConfig;
import com.google.gwt.event.dom.client.MouseOutEvent;
import com.google.gwt.event.dom.client.MouseOutHandler;
import com.google.gwt.event.dom.client.MouseOverEvent;
import com.google.gwt.event.dom.client.MouseOverHandler;
import com.google.gwt.user.client.ui.Image;

/**
 * Component used for displaying help icon with tooltip.
 * @author srebniak_a
 *
 */
public class HelpPanel 
{
	private WidgetComponent imageComponent;
	private ToolTip toolTip;
	private boolean refreshTooltip = true;

	public HelpPanel(final MainController mainController,
					 final String helpText) 
	{           
		if(helpText != null)
		{
			String source = "resources/icons/help_icon.png";
			
			final Image image  = new Image(source);
			imageComponent = new WidgetComponent(image);
			image.addMouseOverHandler(new MouseOverHandler() {
				
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
						toolTipConfig.setText(helpText);  
						
						int width = 500;
						if(width > mainController.getWindowWidth())
						{
							width = image.getAbsoluteLeft();
						}
						
						toolTipConfig.setMaxWidth(width);
						toolTipConfig.setShowDelay(100);
						toolTipConfig.setDismissDelay(0);
						
						toolTip = new ToolTip(null, toolTipConfig);
						
						toolTip.showAt(image.getAbsoluteLeft() + image.getOffsetWidth() + 5, 
									   image.getAbsoluteTop() + image.getOffsetWidth() + 5);
						refreshTooltip = false;
					}
				}
			});
			
			image.addMouseOutHandler(new MouseOutHandler() 
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
	}
	
	/**
	 * Retrieves help image icon component.
	 * @return help image icon component
	 */
	public WidgetComponent getImageComponent()
	{
		return imageComponent;
	}
}
