package ch.systemsx.sybit.crkwebui.client.commons.gui.images;

import com.google.gwt.user.client.ui.Image;
import com.sencha.gxt.widget.core.client.tips.ToolTip;
import com.sencha.gxt.widget.core.client.tips.ToolTipConfig;

/**
 * Image with tooltip component.
 */
public class ImageWithTooltip extends Image
{
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
							final String template) 
	{
		super(source);
		ToolTipConfig config = new ToolTipConfig();
		config.setTitleHtml(title);
	    config.setBodyHtml(template);
	    config.setTrackMouse(true);
		
	    new ToolTip(this, config);
		
	}
}
