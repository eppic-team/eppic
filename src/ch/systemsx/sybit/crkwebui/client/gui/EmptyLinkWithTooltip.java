package ch.systemsx.sybit.crkwebui.client.gui;

import ch.systemsx.sybit.crkwebui.client.data.WindowData;

/**
 * Label styled as link with tooltip. 
 * @author AS
 */
public class EmptyLinkWithTooltip extends LabelWithTooltip
{
	public EmptyLinkWithTooltip(String labelText,
								String tooltipText,
								WindowData windowData,
								int delay)
	{
		super(labelText, tooltipText, windowData, delay);
		this.setText("<a href=\"\" onClick=\"return false;\">" + labelText + "</a>");
	}
}
