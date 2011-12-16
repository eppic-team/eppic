package ch.systemsx.sybit.crkwebui.client.gui;

import ch.systemsx.sybit.crkwebui.client.controllers.MainController;

public class LinkWithTooltip extends LabelWithTooltip
{
	public LinkWithTooltip(String labelText,
							final String tooltipText,
							final MainController mainController,
							final int delay,
							String linkUrl)
	{
		super(labelText, tooltipText, mainController, delay);
		this.setText("<a href=\"" + linkUrl + "\" target=\"_blank\">" + labelText + "</a>");
	}
}
