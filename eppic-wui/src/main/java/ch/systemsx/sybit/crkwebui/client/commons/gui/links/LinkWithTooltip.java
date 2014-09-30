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
		super(getLinkString(labelText, linkUrl),tooltipText);
	}
	
	/**
	 * Changes the data of the link
	 * @param labelText
	 * @param linkUrl
	 */
	public void changeData(String labelText, String linkUrl){
		setLabelText(getLinkString(labelText, linkUrl));
	}
	
	/**
	 * Returns the html string combining text and url
	 * @param labelText
	 * @param linkUrl
	 * @return html string
	 */
	private static String getLinkString(String labelText, String linkUrl){
		return "<a href=\"" + linkUrl + "\" target=\"_blank\" style='vertical-align:top;'>" + labelText + "</a>";
	}
}
