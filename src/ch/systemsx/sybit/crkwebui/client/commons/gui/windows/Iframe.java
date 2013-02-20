package ch.systemsx.sybit.crkwebui.client.commons.gui.windows;

import com.extjs.gxt.ui.client.widget.Html;

public class Iframe extends Html
{
	/**
	 * Creates instance of iframe widget with specified source.
	 * @param source url of page to display
	 */
	public Iframe(String source)
	{
		this.setHtml("<iframe src=\"" + source + "\"" +
					 " frameborder=\"0\" width=\"100%\" height=\"100%\" />");
	}
}
