package ch.systemsx.sybit.crkwebui.client.commons.gui.labels;

import com.sencha.gxt.widget.core.client.tips.ToolTip;
import com.sencha.gxt.widget.core.client.tips.ToolTipConfig;

/**
 * Label with assigned tooltip.
 * @author nikhil
 *
 */
public class LabelWithTooltip extends EppicLabel
{	
	public LabelWithTooltip(String labelText, String tooltipText){
		setLabelText(labelText);
		
		//Set the tooltip with mouse track only if text of the tooltip is not empty
		if(!tooltipText.equals("")){
			ToolTipConfig ttConfig = new ToolTipConfig(tooltipText);
			ttConfig.setTrackMouse(true);
			new ToolTip(this, ttConfig);
		}
	}

}
