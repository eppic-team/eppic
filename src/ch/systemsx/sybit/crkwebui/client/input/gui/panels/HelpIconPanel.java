package ch.systemsx.sybit.crkwebui.client.input.gui.panels;

import ch.systemsx.sybit.crkwebui.client.commons.gui.images.ImageWithTooltip;

import com.sencha.gxt.widget.core.client.WidgetComponent;

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
					 helpText);

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
