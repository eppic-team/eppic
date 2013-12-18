package ch.systemsx.sybit.crkwebui.client.commons.gui.links;

import com.google.gwt.user.client.ui.HTML;

public class ImageEmptyLink extends HTML
{
	public ImageEmptyLink(String imgsrc, int width, int height)
	{
		this.setHTML("<a href=\"\" onClick=\"return false;\">" + 
				"<img border=\"0\" src=\""+imgsrc+"\" width=\""+width+"\" height=\""+height+"\">"
				+ "</a>");
	}
}
