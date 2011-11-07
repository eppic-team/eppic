package ch.systemsx.sybit.crkwebui.client.gui;

import ch.systemsx.sybit.crkwebui.client.controllers.MainController;

import com.extjs.gxt.ui.client.widget.Label;
import com.extjs.gxt.ui.client.widget.form.FormPanel;
import com.extjs.gxt.ui.client.widget.layout.CenterLayout;

public class TopPanel extends FormPanel
{
	private MainController mainController;
	
	public TopPanel(MainController mainController) 
	{
		this.mainController = mainController;
		this.setBodyBorder(false);
		this.setBorders(false);
		this.getHeader().setVisible(false);
		this.setLayout(new CenterLayout());
		this.addStyleName("top-panel-label");
		Label title = new Label();
		title.setText(MainController.CONSTANTS.top_panel_title());
		this.add(title);
	}

}
