package ch.systemsx.sybit.crkwebui.client.commons.gui.links;

import ch.systemsx.sybit.crkwebui.client.commons.gui.labels.LabelWithTooltip;
import ch.systemsx.sybit.crkwebui.shared.model.WindowData;

public class ImageLinkWithTooltip extends LabelWithTooltip
{
	/**
	 * Creates instance of image link with assigned tooltip.
	 * @param imgsrc url of the image
	 * @param width width of the image
	 * @param height height of the image
	 * @param tooltipText text of the tooltip
	 * @param windowData general window data
	 * @param delay delay after which tooltip is displayed
	 * @param linkUrl url of the link
	 */
	public ImageLinkWithTooltip(String imgsrc,
							int width, int height,
							String tooltipText,
							WindowData windowData,
							int delay,
							String linkUrl)
	{
		super("", tooltipText, windowData, delay);
		this.setText("<a href=\"" + linkUrl + "\" target=\"_blank\">" +
				"<img border=\"0\" src=\""+imgsrc+"\" width=\""+width+"\" height=\""+height+"\">"+ 
						"</a>");
	}
}
