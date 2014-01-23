package ch.systemsx.sybit.crkwebui.client.commons.gui.windows;

import com.sencha.gxt.widget.core.client.container.HtmlLayoutContainer;

public class Iframe extends HtmlLayoutContainer
{
	/**
	 * Creates instance of iframe widget with specified source.
	 * @param source url of page to display
	 */
	public Iframe(String source)
	{
		super("<iframe src='" + source + "'" +" frameborder='0' width='100%' height='100%' />");
	}
}
