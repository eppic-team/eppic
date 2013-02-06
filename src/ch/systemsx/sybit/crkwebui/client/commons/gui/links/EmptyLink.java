package ch.systemsx.sybit.crkwebui.client.commons.gui.links;

import com.extjs.gxt.ui.client.widget.Label;

/**
 * Label styled as link.
 * @author AS
 */
public class EmptyLink extends Label
{
	public EmptyLink(String labelText)
	{
		this.setText("<a href=\"\" onClick=\"return false;\">" + labelText + "</a>");
	}
}
