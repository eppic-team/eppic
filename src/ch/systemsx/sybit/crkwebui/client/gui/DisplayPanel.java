package ch.systemsx.sybit.crkwebui.client.gui;

import ch.systemsx.sybit.crkwebui.client.controllers.MainController;

import com.extjs.gxt.ui.client.widget.form.FormPanel;
import com.extjs.gxt.ui.client.widget.layout.FitLayout;

public class DisplayPanel extends FormPanel 
{
	private MainController mainController;
	
	public DisplayPanel(MainController mainController)
	{
		this.mainController = mainController;
		this.getHeader().setVisible(false);
		this.setBorders(true);
		this.setBodyBorder(false);
		
//		VBoxLayout vBoxLayout = new VBoxLayout();
//		vBoxLayout.setVBoxLayoutAlign(VBoxLayoutAlign.CENTER);
		
		this.setLayout(new FitLayout());
	}
}
