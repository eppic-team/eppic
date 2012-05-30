package ch.systemsx.sybit.crkwebui.client.gui;

import ch.systemsx.sybit.crkwebui.client.controllers.AppPropertiesManager;

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
		this.addStyleName("top-panel-label");
		Label title = new Label();
		title.setText(AppPropertiesManager.CONSTANTS.top_panel_title());
		this.add(title);
	}

}
