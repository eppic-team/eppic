package ch.systemsx.sybit.crkwebui.client.gui;

import ch.systemsx.sybit.crkwebui.client.controllers.MainController;

import com.extjs.gxt.ui.client.Style.Orientation;
import com.extjs.gxt.ui.client.event.BaseEvent;
import com.extjs.gxt.ui.client.event.Events;
import com.extjs.gxt.ui.client.event.Listener;
import com.extjs.gxt.ui.client.util.Margins;
import com.extjs.gxt.ui.client.widget.Label;
import com.extjs.gxt.ui.client.widget.LayoutContainer;
import com.extjs.gxt.ui.client.widget.layout.RowData;
import com.extjs.gxt.ui.client.widget.layout.RowLayout;
import com.extjs.gxt.ui.client.widget.layout.VBoxLayout;
import com.extjs.gxt.ui.client.widget.layout.VBoxLayout.VBoxLayoutAlign;
import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.ui.HTML;

/**
 * Bottom panel containing status label and contact information.
 * @author srebniak_a
 *
 */
public class BottomPanel extends LayoutContainer 
{
	private HTML status;
	private Label helpLink;
	private Label contactLink;
	private Label aboutLink;

	public BottomPanel(final MainController mainController) 
	{
		this.setLayout(new RowLayout(Orientation.HORIZONTAL));
		
	    status = new HTML();  
	    this.add(status, new RowData(1, 1, new Margins(0)));

	    LayoutContainer linksContainerWrapper = new LayoutContainer();
	    linksContainerWrapper.setBorders(false);
	    
	    VBoxLayout vBoxLayout = new VBoxLayout();
	    vBoxLayout.setVBoxLayoutAlign(VBoxLayoutAlign.RIGHT);
	    linksContainerWrapper.setLayout(vBoxLayout);
	    
	    LayoutContainer linksContainer = new LayoutContainer();
	    linksContainer.setBorders(false);
		
	    aboutLink = new Label("<a href=\"\" onClick=\"return false;\">" +
								MainController.CONSTANTS.bottom_panel_about_link() +
								"</a>");
	    aboutLink.addStyleName("eppic-internal-link");
	    
	    aboutLink.addListener(Events.OnClick, new Listener<BaseEvent>() {

			@Override
			public void handleEvent(BaseEvent be) {
				mainController.showAbout();
			}
		});
	    
		contactLink = new Label("<a href=\"" + 
								MainController.CONSTANTS.bottom_panel_contact_link() +
								"\" target=\"_blank\">" + 
								MainController.CONSTANTS.bottom_panel_contact_link_label() + 
								"</a>");
		contactLink.setStyleAttribute("margin-left", "10px");
		contactLink.addStyleName("eppic-internal-link");
		
		helpLink = new Label("<a href=\"" +
							GWT.getHostPageBaseURL() + "Help.html" +
							"\" target=\"_blank\">" +
							MainController.CONSTANTS.bottom_panel_help_link() +
							"</a>");
		helpLink.setStyleAttribute("margin-left", "10px");
		helpLink.addStyleName("eppic-internal-link");
		
		linksContainer.add(aboutLink);
		linksContainer.add(helpLink);
		linksContainer.add(contactLink);
		
		linksContainerWrapper.add(linksContainer);
		this.add(linksContainerWrapper, new RowData(150, 1, new Margins(0)));
	}
	
	/**
	 * Updates text of the status message label.
	 * @param message text to display.
	 * @param isError flag specifying whether message is the error.
	 */
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
