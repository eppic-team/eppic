package ch.systemsx.sybit.crkwebui.client.gui;

import ch.systemsx.sybit.crkwebui.client.controllers.MainController;

import com.extjs.gxt.ui.client.Style.Orientation;
import com.extjs.gxt.ui.client.util.Margins;
import com.extjs.gxt.ui.client.widget.LayoutContainer;
import com.extjs.gxt.ui.client.widget.layout.RowData;
import com.extjs.gxt.ui.client.widget.layout.RowLayout;
import com.extjs.gxt.ui.client.widget.layout.VBoxLayout;
import com.extjs.gxt.ui.client.widget.layout.VBoxLayout.VBoxLayoutAlign;
import com.google.gwt.user.client.ui.HTML;

/**
 * This is the bottom panel containing status label and contact information
 * @author srebniak_a
 *
 */
public class BottomPanel extends LayoutContainer 
{
	private HTML status;
	private HTML contactLink;

	public BottomPanel() 
	{
		this.setLayout(new RowLayout(Orientation.HORIZONTAL));
		
	    status = new HTML();  
	    this.add(status, new RowData(0.8, 1, new Margins(0)));
	    
	    LayoutContainer contactContainer = new LayoutContainer();
	    contactContainer.setBorders(false);
		
		VBoxLayout vBoxLayout = new VBoxLayout();
		vBoxLayout.setVBoxLayoutAlign(VBoxLayoutAlign.RIGHT);
		
		contactContainer.setLayout(vBoxLayout);
		
		contactLink = new HTML("<a href=\"" + 
								MainController.CONSTANTS.bottom_panel_contact_link() +
								"\">" + 
								MainController.CONSTANTS.bottom_panel_contact_link_label() + 
								"</a>");
		
		contactContainer.add(contactLink);
		this.add(contactContainer, new RowData(0.2, 1, new Margins(0)));
	}
	
	public void updateStatusMessage(String message, boolean isError)
	{
		String messageText = "<span style=\"color:";
		
		String color = "black";
		
		if(isError)
		{
			color = "red; font-weight: bold";
		}
		
		messageText += color + "\">" + "Status: " + message;
		
		if(isError)
		{
			messageText += " - " + MainController.CONSTANTS.bottom_panel_status_error_refresh_page();
		}
		
		messageText += "</span>";
		
		status.setHTML(messageText);
	}

}
