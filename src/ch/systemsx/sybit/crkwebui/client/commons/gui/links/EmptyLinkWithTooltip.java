package ch.systemsx.sybit.crkwebui.client.commons.gui.links;

import ch.systemsx.sybit.crkwebui.client.commons.gui.labels.LabelWithTooltip;
import ch.systemsx.sybit.crkwebui.shared.model.WindowData;

/**
 * Label styled as empty link with tooltip. 
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
		this.setHtml("<a href=\"\" onClick=\"return false;\">" + labelText + "</a>");
	}
}
