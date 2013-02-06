package ch.systemsx.sybit.crkwebui.client.main.gui.panels;

import ch.systemsx.sybit.crkwebui.client.commons.appdata.AppPropertiesManager;

import com.extjs.gxt.ui.client.widget.Label;
import com.extjs.gxt.ui.client.widget.form.FormPanel;
import com.extjs.gxt.ui.client.widget.layout.CenterLayout;

/**
 * Top logo panel.
 * @author AS
 */
public class TopPanel extends FormPanel
{
	public TopPanel() 
	{
		this.setBodyBorder(false);
		this.setBorders(false);
		this.getHeader().setVisible(false);
		this.setLayout(new CenterLayout());
		Label title = new Label();
		title.setText(AppPropertiesManager.CONSTANTS.top_panel_title());
		this.add(title);
	}

}
