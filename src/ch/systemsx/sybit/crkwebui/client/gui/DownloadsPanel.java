package ch.systemsx.sybit.crkwebui.client.gui;

import ch.systemsx.sybit.crkwebui.client.controllers.AppPropertiesManager;
import ch.systemsx.sybit.crkwebui.client.controllers.ApplicationContext;

import com.extjs.gxt.ui.client.Style.Scroll;
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
		this.setScrollMode(Scroll.AUTO);
		
		LinkWithTooltip downloadCrkCommandLine = new LinkWithTooltip(AppPropertiesManager.CONSTANTS.downloads_panel_download_crk_link(), 
																	 AppPropertiesManager.CONSTANTS.downloads_panel_download_crk_link_hint(), 
																	 ApplicationContext.getWindowData(), 
																	 0, 
																	 "http://crkfe.psi.ch/downloads/eppic.zip");
		this.add(downloadCrkCommandLine, new FormData("-20 100%"));
	}
}

