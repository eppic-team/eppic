package ch.systemsx.sybit.crkwebui.client.downloads.gui.panels;

import ch.systemsx.sybit.crkwebui.client.commons.gui.panels.DisplayPanel;
import ch.systemsx.sybit.crkwebui.client.commons.gui.windows.Iframe;

import com.google.gwt.core.client.GWT;

/**
 * Panel used to display downloads.
 * @author srebniak_a
 *
 */
public class DownloadsPanel extends DisplayPanel
{
	public DownloadsPanel() 
	{
		this.addStyleName("eppic-text-panel");
		//this.setScrollMode(Scroll.NONE);

		Iframe iframe = new Iframe(GWT.getHostPageBaseURL() + "downloads.html");
		this.setData(iframe);
	}
}

