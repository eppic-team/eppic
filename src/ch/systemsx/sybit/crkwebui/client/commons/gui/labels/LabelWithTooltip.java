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
	private ToolTip tooltip;
	
	/**
	 * Creates instance of label with assigned tooltip.
	 * @param labelText text of the label
	 * @param template text of the tooltip
	 */
	public LabelWithTooltip(String labelText,
							final String template)
	{
		super();
		this.setData(labelText, template);
	}

	/**
	 * Creates a Label with no text and no tooltip
	 */
	public LabelWithTooltip(){
		super();
	}
	
	/**
	 * Sets the text and the tool tip
	 * @param text
	 * @param tool tip text  
	 */
	public void setData(String labelText, String template) {
		this.setLabelText(labelText);
		this.setToolTipText(template);
	}
	
	/**
	 * Sets the text of the Label
	 * @param text to be set
	 */
	public void setLabelText(String text){
		this.setHTML(text);
	}
	
	/**
	 * Sets the text of the tool tip
	 * @param tooltip text
	 */
	public void setToolTipText(String text){
		if(!text.equals("")){
			ToolTipConfig config = new ToolTipConfig();
			config.setBodyHtml(text);
			config.setTrackMouse(true);

			tooltip = new ToolTip(this, config);
		}
		else{
			if(tooltip != null)
				tooltip.removeFromParent();
		}
	}

}
