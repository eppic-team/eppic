package ch.systemsx.sybit.crkwebui.client.downloads.gui.panels;

import ch.systemsx.sybit.crkwebui.client.commons.appdata.ApplicationContext;
import ch.systemsx.sybit.crkwebui.client.commons.gui.panels.DisplayPanel;

import com.extjs.gxt.ui.client.Style.Scroll;
import com.extjs.gxt.ui.client.widget.Label;
import com.extjs.gxt.ui.client.widget.layout.FormData;

/**
 * Panel used to display downloads.
 * @author srebniak_a
 *
 */
public class DownloadsPanel extends DisplayPanel
{
	public DownloadsPanel() 
	{
		this.setBorders(true);
		this.addStyleName("eppic-default-padding");
		this.addStyleName("eppic-text-panel");
		this.setScrollMode(Scroll.AUTO);
		
		Label downloadsText = new Label();
		downloadsText.setText(ApplicationContext.getSettings().getDownloadsPageContent());
		this.add(downloadsText, new FormData("-20 100%"));
	}
}

