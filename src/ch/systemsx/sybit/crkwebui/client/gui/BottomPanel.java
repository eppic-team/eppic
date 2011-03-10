package ch.systemsx.sybit.crkwebui.client.gui;

import ch.systemsx.sybit.crkwebui.client.controllers.MainController;

import com.extjs.gxt.ui.client.Style.Orientation;
import com.extjs.gxt.ui.client.Style.Scroll;
import com.extjs.gxt.ui.client.event.Events;
import com.extjs.gxt.ui.client.event.FieldEvent;
import com.extjs.gxt.ui.client.event.Listener;
import com.extjs.gxt.ui.client.util.Margins;
import com.extjs.gxt.ui.client.widget.Label;
import com.extjs.gxt.ui.client.widget.LayoutContainer;
import com.extjs.gxt.ui.client.widget.Status;
import com.extjs.gxt.ui.client.widget.form.FormPanel;
import com.extjs.gxt.ui.client.widget.form.SimpleComboBox;
import com.extjs.gxt.ui.client.widget.form.ComboBox.TriggerAction;
import com.extjs.gxt.ui.client.widget.layout.RowData;
import com.extjs.gxt.ui.client.widget.layout.RowLayout;
import com.extjs.gxt.ui.client.widget.layout.VBoxLayout;
import com.extjs.gxt.ui.client.widget.layout.VBoxLayout.VBoxLayoutAlign;
import com.extjs.gxt.ui.client.widget.toolbar.ToolBar;
import com.google.gwt.user.client.Cookies;
import com.google.gwt.user.client.ui.HTML;

public class BottomPanel extends LayoutContainer 
{
	private MainController mainController;
	
	private HTML status;
	
	private HTML contactLink;

	public BottomPanel(MainController mainController) 
	{
		this.mainController = mainController;
		this.setLayout(new RowLayout(Orientation.HORIZONTAL));
		
	    status = new HTML();  
	    this.add(status, new RowData(0.8, 1, new Margins(0)));
	    
	    LayoutContainer contactContainer = new LayoutContainer();
	    contactContainer.setBorders(false);
		
		VBoxLayout vBoxLayout = new VBoxLayout();
		vBoxLayout.setVBoxLayoutAlign(VBoxLayoutAlign.RIGHT);
		
		contactContainer.setLayout(vBoxLayout);
		
		contactLink = new HTML("<a href=\"http://www.psi.ch\">Contact</a>");
		
		contactContainer.add(contactLink);
		this.add(contactContainer, new RowData(0.2, 1, new Margins(0)));
//	    this.setBorders(true);
//	    mainToolbar.add(new FillToolItem());  
	  
//	    this.setBottomComponent(mainToolbar);  
	}
	
	public void updateStatusMessage(String message, boolean isError)
	{
		String messageText = "<span style=\"color:";
		
		String color = "black";
		
		if(isError)
		{
			color = "red; font-weight: bold";
		}
		
		messageText += color + "\">" + "Status: " + message + "</span>";
		
		status.setHTML(messageText);
	}

}
