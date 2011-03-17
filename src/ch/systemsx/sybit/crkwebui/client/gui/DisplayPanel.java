package ch.systemsx.sybit.crkwebui.client.gui;

import ch.systemsx.sybit.crkwebui.client.controllers.MainController;

import com.extjs.gxt.ui.client.widget.LayoutContainer;
import com.extjs.gxt.ui.client.widget.layout.FitLayout;

/**
 * Base class for different views visible in center panel
 * @author srebniak_a
 *
 */
public class DisplayPanel extends LayoutContainer 
{
	protected MainController mainController;
	
	public DisplayPanel(MainController mainController)
	{
		this.mainController = mainController;
//		this.getHeader().setVisible(false);
		this.setBorders(false);
//		this.setBodyBorder(false);
//		this.setPadding(0);
		
//		VBoxLayout vBoxLayout = new VBoxLayout();
//		vBoxLayout.setVBoxLayoutAlign(VBoxLayoutAlign.CENTER);
		
		this.setLayout(new FitLayout());
	}
}
