package ch.systemsx.sybit.crkwebui.client.releases.gui.panels;

import ch.systemsx.sybit.crkwebui.client.commons.gui.panels.DisplayPanel;
import ch.systemsx.sybit.crkwebui.client.commons.gui.windows.Iframe;

import com.extjs.gxt.ui.client.Style.Scroll;
import com.google.gwt.core.client.GWT;


/**
 * Panel used to display releases.
 * @author srebniak_a
 *
 */
public class ReleasesPanel extends DisplayPanel
{
	public ReleasesPanel() 
	{
		this.addStyleName("eppic-text-panel");
		this.setScrollMode(Scroll.NONE);

		Iframe iframe = new Iframe(GWT.getHostPageBaseURL() + "releases.html");
		this.add(iframe);
	}
}
