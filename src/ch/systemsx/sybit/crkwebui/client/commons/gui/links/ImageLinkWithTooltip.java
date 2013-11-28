package ch.systemsx.sybit.crkwebui.client.commons.gui.links;

import ch.systemsx.sybit.crkwebui.client.commons.gui.labels.LabelWithTooltip;

public class ImageLinkWithTooltip extends LabelWithTooltip
{
	/**
	 * Creates instance of image link with assigned tooltip.
	 * @param imgsrc url of the image
	 * @param width width of the image
	 * @param height height of the image
	 * @param tooltipText text of the tooltip
	 * @param linkUrl url of the link
	 */
	public ImageLinkWithTooltip(String imgsrc,
			int width, int height,
			String tooltipText,
			String linkUrl)
	{
		super("", tooltipText);
		this.setHTML("<a href=\"" + linkUrl + "\">" +
				"<img border=\"0\" src=\""+imgsrc+"\" width=\""+width+"\" height=\""+height+"\">"+ 
				"</a>");
	}

	/**
	 * Resets the link and content
	 * @param imgsrc
	 * @param width
	 * @param height
	 * @param tooltipText
	 * @param linkUrl
	 */
	public void setData(String imgsrc,
			int width, int height,
			String tooltipText,
			String linkUrl)
	{
		this.setHTML("<a href=\"" + linkUrl + "\">" +
				"<img border=\"0\" src=\""+imgsrc+"\" width=\""+width+"\" height=\""+height+"\">"+ 
				"</a>");
	}

}
