package ch.systemsx.sybit.crkwebui.client.commons.gui.links;

import com.google.gwt.user.client.ui.HTML;

/**
 * Label styled as link.
 * @author AS
 */
public class EmptyLink extends HTML
{
	public EmptyLink(String labelText)
	{
		this.setHTML("<a href=\"\" onClick=\"return false;\">" + labelText + "</a>");
	}
}
