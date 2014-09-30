package ch.systemsx.sybit.crkwebui.client.commons.gui.links;

import ch.systemsx.sybit.crkwebui.client.commons.gui.labels.LabelWithTooltip;

/**
 * Label styled as empty link with tooltip. 
 * @author AS
 */
public class EmptyLinkWithTooltip extends LabelWithTooltip
{
	public EmptyLinkWithTooltip(String labelText,
								String tooltipText)
	{
		super(labelText, tooltipText);
		this.setHTML("<a href=\"\" onClick=\"return false;\">" + labelText + "</a>");
	}
}
