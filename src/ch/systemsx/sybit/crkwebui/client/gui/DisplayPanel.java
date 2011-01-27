package ch.systemsx.sybit.crkwebui.client.gui;

import ch.systemsx.sybit.crkwebui.client.controllers.MainController;

import com.extjs.gxt.ui.client.Style.HorizontalAlignment;
import com.extjs.gxt.ui.client.widget.ContentPanel;
import com.extjs.gxt.ui.client.widget.VerticalPanel;
import com.extjs.gxt.ui.client.widget.form.FormPanel;
import com.extjs.gxt.ui.client.widget.layout.CenterLayout;
import com.extjs.gxt.ui.client.widget.layout.FillLayout;
import com.extjs.gxt.ui.client.widget.layout.FitLayout;
import com.extjs.gxt.ui.client.widget.layout.HBoxLayout;
import com.extjs.gxt.ui.client.widget.layout.RowLayout;
import com.extjs.gxt.ui.client.widget.layout.HBoxLayout.HBoxLayoutAlign;
import com.extjs.gxt.ui.client.widget.layout.VBoxLayout.VBoxLayoutAlign;
import com.extjs.gxt.ui.client.widget.layout.VBoxLayout;

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
