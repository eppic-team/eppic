package ch.systemsx.sybit.crkwebui.client.gui;

import com.extjs.gxt.ui.client.widget.Label;

/**
 * This class is used to represent label styled as link.
 * @author AS
 */
public class EmptyLink extends Label
{
	public EmptyLink(String labelText)
	{
		this.setText("<a href=\"\" onClick=\"return false;\">" + labelText + "</a>");
	}
}
