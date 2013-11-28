package ch.systemsx.sybit.crkwebui.client.commons.gui.labels;

import com.google.gwt.user.client.ui.HTML;
import com.sencha.gxt.widget.core.client.tips.ToolTip;
import com.sencha.gxt.widget.core.client.tips.ToolTipConfig;

/**
 * Label with assigned tooltip.
 * @author AS
 *
 */
public class LabelWithTooltip extends HTML
{
	/**
	 * Creates instance of label with assigned tooltip.
	 * @param labelText text of the label
	 * @param template text of the tooltip
	 */
	public LabelWithTooltip(String labelText,
							final String template)
	{
		super(labelText);
		ToolTipConfig config = new ToolTipConfig();
	    config.setBodyHtml(template);
	    config.setTrackMouse(true);
		
	    new ToolTip(this, config);
	}

}
