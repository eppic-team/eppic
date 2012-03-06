package ch.systemsx.sybit.crkwebui.client.gui;

import ch.systemsx.sybit.crkwebui.client.controllers.MainController;

/**
 * This class is used to represent label styled as link with tooltip. 
 * @author AS
 */
public class EmptyLinkWithTooltip extends LabelWithTooltip
{
	public EmptyLinkWithTooltip(String labelText,
							final String tooltipText,
							final MainController mainController,
							final int delay)
	{
		super(labelText, tooltipText, mainController, delay);
		this.setText("<a href=\"\" onClick=\"return false;\">" + labelText + "</a>");
	}
}
