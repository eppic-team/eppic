package ch.systemsx.sybit.crkwebui.client.commons.gui.links;

import ch.systemsx.sybit.crkwebui.client.commons.gui.labels.LabelWithTooltip;

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
	 * @param linkUrl url of the link
	 */
	public LinkWithTooltip(String labelText,
							String tooltipText,
							String linkUrl)
	{
		super(labelText, tooltipText);
		this.setData(labelText, linkUrl);
	}
	
	/**
	 * Resets the link and content
	 * @param labelText
	 * @param tooltipText
	 * @param linkUrl
	 */
	public void setData(String labelText,
					String linkUrl){
		this.setHTML("<a href=\"" + linkUrl + "\" target=\"_blank\" style='vertical-align:top;'>" + labelText + "</a>");
	}
	
	/**
	 * Resets the link and content
	 * @param labelText
	 * @param tooltipText
	 * @param linkUrl
	 */
	public void setData(String labelText,
					String tooltipText,
					String linkUrl){
		this.setToolTipText(tooltipText);
		this.setHTML("<a href=\"" + linkUrl + "\" target=\"_blank\" style='vertical-align:top;'>" + labelText + "</a>");
	}
}
