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
		super();
		this.setLinkData(labelText, tooltipText, linkUrl);
	}
	
	/**
	 * Creates a blank link
	 */
	public LinkWithTooltip(){
		super();
	}
	
	/**
	 * Resets the link and content
	 * @param labelText
	 * @param tooltipText
	 * @param linkUrl
	 */
	public void setLinkData(String labelText,
					String tooltipText,
					String linkUrl){
		super.setData("<a href=\"" + linkUrl + "\" target=\"_blank\" style='vertical-align:top;'>" + labelText + "</a>",
						tooltipText);
	}
	
	/**
	 * Sets a text with no link
	 */
	public void setNoLinkData(String labelText,
					String tooltipText){
		super.setData(labelText, tooltipText);
	}
}
