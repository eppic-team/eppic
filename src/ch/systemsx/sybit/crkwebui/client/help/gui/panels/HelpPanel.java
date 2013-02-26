package ch.systemsx.sybit.crkwebui.client.help.gui.panels;

import ch.systemsx.sybit.crkwebui.client.commons.gui.panels.DisplayPanel;
import ch.systemsx.sybit.crkwebui.client.commons.gui.windows.Iframe;

import com.extjs.gxt.ui.client.Style.Scroll;
import com.google.gwt.core.client.GWT;

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
		this.setScrollMode(Scroll.NONE);

		Iframe iframe = new Iframe(GWT.getHostPageBaseURL() + "Help.html");
		this.add(iframe);
	}
}

