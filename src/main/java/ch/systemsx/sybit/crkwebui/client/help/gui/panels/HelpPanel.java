package ch.systemsx.sybit.crkwebui.client.help.gui.panels;

import ch.systemsx.sybit.crkwebui.client.commons.gui.panels.DisplayPanel;
import ch.systemsx.sybit.crkwebui.client.commons.gui.windows.Iframe;

import com.google.gwt.core.client.GWT;
import com.sencha.gxt.core.client.dom.ScrollSupport.ScrollMode;

/**
 * Panel used to display help.
 * @author srebniak_a
 *
 */
public class HelpPanel extends DisplayPanel
{
	public HelpPanel() 
	{
		this.addStyleName("eppic-text-panel");
		this.setScrollMode(ScrollMode.NONE);

		Iframe iframe = new Iframe(GWT.getHostPageBaseURL() + "help.html");
		this.setData(iframe);
	}
}

