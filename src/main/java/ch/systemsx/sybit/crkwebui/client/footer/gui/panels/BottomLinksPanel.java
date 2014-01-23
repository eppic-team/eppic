package ch.systemsx.sybit.crkwebui.client.footer.gui.panels;

import ch.systemsx.sybit.crkwebui.client.commons.appdata.AppPropertiesManager;
import ch.systemsx.sybit.crkwebui.client.commons.events.ShowAboutEvent;
import ch.systemsx.sybit.crkwebui.client.commons.gui.links.EmptyLink;
import ch.systemsx.sybit.crkwebui.client.commons.managers.EventBusManager;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.HTML;
import com.sencha.gxt.core.client.util.Margins;
import com.sencha.gxt.widget.core.client.container.HorizontalLayoutContainer;
import com.sencha.gxt.widget.core.client.container.SimpleContainer;

/**
 * Panel containing navigation links in the footer.
 * @author nikhil
 */
public class BottomLinksPanel extends HorizontalLayoutContainer
{
	public BottomLinksPanel()
	{
	    this.setBorders(false);
	    createLinksPanel();
	}
	
	/**
	 * Creates panel containing navigation links.
	 */
	private void createLinksPanel()
    {
        this.setBorders(false);

        HorizontalLayoutContainer linksContainer = new HorizontalLayoutContainer();
        linksContainer.setWidth(170);
        
        HTML disclaimerLink = createDisclaimerLink();
        HTML aboutLink = createAboutLink();
        HTML contactLink = createContactLink();
   
        linksContainer.add(disclaimerLink, new HorizontalLayoutData(-1,1, new Margins(0,10,0,5)));
        linksContainer.add(createBreakLabel(), new HorizontalLayoutData(-1,1));
        linksContainer.add(aboutLink, new HorizontalLayoutData(-1,1, new Margins(0,5,0,5)));
        linksContainer.add(createBreakLabel(), new HorizontalLayoutData(-1,1));
        linksContainer.add(contactLink, new HorizontalLayoutData(-1,1, new Margins(0,5,0,5)));
        
        this.add(new SimpleContainer(), new HorizontalLayoutData(0.5,-1));
        this.add(linksContainer);
    }
	
    private HTML createBreakLabel()
    {
        HTML breakLabel = new HTML("&nbsp;&nbsp;");
        breakLabel.addStyleName("eppic-default-left-margin");
        breakLabel.addStyleName("eppic-footer-link");
        return breakLabel;
    }
	
	/**
	 * Creates link to open disclaimer window.
	 * @return link to disclaimer window
	 */
	private HTML createDisclaimerLink()
	{
		HTML aboutLink = new EmptyLink(AppPropertiesManager.CONSTANTS.bottom_panel_disclaimer_link_label());
	    aboutLink.addStyleName("eppic-footer-link");
	    aboutLink.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				EventBusManager.EVENT_BUS.fireEvent(new ShowAboutEvent());
			}
		});
	    
	    return aboutLink;
	}
	
	/**
	 * Creates about link.
	 * @return about link
	 */
	private HTML createAboutLink()
	{
		HTML contactLink = new HTML("<a href=\"" +
				AppPropertiesManager.CONSTANTS.bottom_panel_about_link() +
				"\" target=\"_blank\">" +
				AppPropertiesManager.CONSTANTS.bottom_panel_about_link_label() +
				"</a>");
		contactLink.addStyleName("eppic-footer-link");

		return contactLink;
	}
	
	/**
	 * Creates contact link.
	 * @return contact link
	 */
	private HTML createContactLink()
	{
		HTML contactLink = new HTML("<a href=\"mailto:"+AppPropertiesManager.CONSTANTS.bottom_panel_contact_email() +"\"" +
				"\">" +
				AppPropertiesManager.CONSTANTS.bottom_panel_contact_link_label() +
				"</a>");
		contactLink.addStyleName("eppic-footer-link");

		return contactLink;
	}
}
