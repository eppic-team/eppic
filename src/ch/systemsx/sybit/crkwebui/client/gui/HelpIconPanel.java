package ch.systemsx.sybit.crkwebui.client.gui;

import ch.systemsx.sybit.crkwebui.client.data.TooltipXPositionType;
import ch.systemsx.sybit.crkwebui.client.data.TooltipYPositionType;

import com.extjs.gxt.ui.client.widget.WidgetComponent;

/**
 * Component used for displaying help icon with tooltip.
 * @author srebniak_a
 *
 */
public class HelpIconPanel 
{
	private WidgetComponent imageComponent;

	public HelpIconPanel(final String helpText) 
	{           
		if(helpText != null)
		{
			String source = "resources/icons/help_icon.png";
			
			ImageWithTooltip imageWithTooltip = new ImageWithTooltip(source,
					 null,
					 helpText,
					 500,
					 false,
					 100,
					 0,
					 TooltipXPositionType.RIGHT,
					 TooltipYPositionType.BOTTOM);

			imageComponent = new WidgetComponent(imageWithTooltip);
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
