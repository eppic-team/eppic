package ch.systemsx.sybit.crkwebui.client.help.gui.panels;

import ch.systemsx.sybit.crkwebui.client.commons.appdata.ApplicationContext;
import ch.systemsx.sybit.crkwebui.client.commons.gui.panels.DisplayPanel;

import com.extjs.gxt.ui.client.Style.Scroll;
import com.extjs.gxt.ui.client.widget.Label;
import com.extjs.gxt.ui.client.widget.layout.FormData;

/**
 * Panel used to display help.
 * @author srebniak_a
 *
 */
public class HelpPanel extends DisplayPanel
{
	public HelpPanel() 
	{
		this.setBorders(true);
		this.addStyleName("eppic-default-padding");
		this.addStyleName("eppic-text-panel");
		this.setScrollMode(Scroll.AUTO);
		
		Label helpText = new Label();
		helpText.setText(ApplicationContext.getSettings().getHelpPageContent());
		this.add(helpText, new FormData("-20 100%"));
	}
}

