package ch.systemsx.sybit.crkwebui.client.gui;

import ch.systemsx.sybit.crkwebui.client.data.WindowData;

/**
 * Link with attached tooltip.
 * @author AS
 *
 */
public class LinkWithTooltip extends LabelWithTooltip
{
	/**
	 * Creates instance of link with assigned tooltip.
	 * @param labelText text of the link
	 * @param tooltipText text of the tooltip
	 * @param windowData general window data
	 * @param delay delay after which tooltip is displayed
	 * @param linkUrl url of the link
	 */
	public LinkWithTooltip(String labelText,
							String tooltipText,
							WindowData windowData,
							int delay,
							String linkUrl)
	{
		super(labelText, tooltipText, windowData, delay);
		this.setText("<a href=\"" + linkUrl + "\" target=\"_blank\">" + labelText + "</a>");
	}
}
