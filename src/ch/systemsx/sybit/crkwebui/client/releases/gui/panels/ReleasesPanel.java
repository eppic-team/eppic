package ch.systemsx.sybit.crkwebui.client.releases.gui.panels;

import ch.systemsx.sybit.crkwebui.client.commons.appdata.ApplicationContext;
import ch.systemsx.sybit.crkwebui.client.commons.gui.panels.DisplayPanel;

import com.extjs.gxt.ui.client.Style.Scroll;
import com.extjs.gxt.ui.client.widget.Label;
import com.extjs.gxt.ui.client.widget.layout.FormData;


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
		this.setScrollMode(Scroll.AUTO);
		
		Label releasesText = new Label();
		releasesText.setText(ApplicationContext.getSettings().getReleasesPageContent());
		this.add(releasesText, new FormData("-20 100%"));
	}
}
