package ch.systemsx.sybit.crkwebui.client.navigation.gui.panels;

import ch.systemsx.sybit.crkwebui.client.commons.appdata.AppPropertiesManager;
import ch.systemsx.sybit.crkwebui.client.commons.events.ShowAboutEvent;
import ch.systemsx.sybit.crkwebui.client.commons.gui.links.EmptyLink;
import ch.systemsx.sybit.crkwebui.client.commons.managers.EventBusManager;

import com.extjs.gxt.ui.client.event.BaseEvent;
import com.extjs.gxt.ui.client.event.Events;
import com.extjs.gxt.ui.client.event.Listener;
import com.extjs.gxt.ui.client.widget.Label;
import com.extjs.gxt.ui.client.widget.LayoutContainer;
import com.extjs.gxt.ui.client.widget.layout.VBoxLayout;
import com.extjs.gxt.ui.client.widget.layout.VBoxLayout.VBoxLayoutAlign;
import com.google.gwt.user.client.History;

/**
 * Panel containing navigation links.
 * @author srebniak_a
 *
 */
public class NavigationPanel extends LayoutContainer
{
	public NavigationPanel()
	{
	    this.setBorders(false);

	    VBoxLayout vBoxLayout = new VBoxLayout();
	    vBoxLayout.setVBoxLayoutAlign(VBoxLayoutAlign.RIGHT);
	    this.setLayout(vBoxLayout);

	    LayoutContainer linksContainer = createLinksPanel();
		this.add(linksContainer);
	}
	
	/**
	 * Creates panel containing navigation links.
	 * @return panel with links
	 */
	private LayoutContainer createLinksPanel()
    {
        LayoutContainer linksContainer = new LayoutContainer();
        linksContainer.setBorders(false);

        Label homeLink = createHomeLink();
        Label aboutLink = createAboutLink();
        Label helpLink = createHelpLink();
        Label downloadsLink = createDownloadsLink();
        Label releasesLink = createReleasesLink();
        Label contactLink = createContactLink();

        linksContainer.add(homeLink);
        linksContainer.add(createBreakLabel());
        linksContainer.add(downloadsLink);
        linksContainer.add(createBreakLabel());
        linksContainer.add(helpLink);
        linksContainer.add(createBreakLabel());
        linksContainer.add(releasesLink);
        linksContainer.add(createBreakLabel());        
        linksContainer.add(aboutLink);
        linksContainer.add(createBreakLabel());
        linksContainer.add(contactLink);

        return linksContainer;
    }

    private Label createBreakLabel()
    {
        Label breakLabel = new Label("|");
        breakLabel.addStyleName("eppic-default-left-margin");
        return breakLabel;
    }

	
	/**
	 * Creates link to the home page.
	 * @return link to the home page
	 */
	private Label createHomeLink()
	{
		Label homeLink = new EmptyLink(AppPropertiesManager.CONSTANTS.bottom_panel_home_link_label());
		homeLink.addStyleName("eppic-horizontal-nav");
		homeLink.addListener(Events.OnClick, new Listener<BaseEvent>() {

			@Override
			public void handleEvent(BaseEvent be) {
				History.newItem("");
			}
		});
	    
	    return homeLink;
	}
	
	/**
	 * Creates link to open about window.
	 * @return link to about window
	 */
	private Label createAboutLink()
	{
		Label aboutLink = new EmptyLink(AppPropertiesManager.CONSTANTS.bottom_panel_about_link_label());
	    aboutLink.addStyleName("eppic-horizontal-nav");
	    aboutLink.addStyleName("eppic-default-left-margin");
	    aboutLink.addListener(Events.OnClick, new Listener<BaseEvent>() {

			@Override
			public void handleEvent(BaseEvent be) {
				EventBusManager.EVENT_BUS.fireEvent(new ShowAboutEvent());
			}
		});
	    
	    return aboutLink;
	}
	
	/**
	 * Creates link to help page.
	 * @return link to help page
	 */
	private Label createHelpLink()
	{
		Label helpLink = new EmptyLink(AppPropertiesManager.CONSTANTS.bottom_panel_help_link_label());
		helpLink.addStyleName("eppic-default-left-margin");
		helpLink.addStyleName("eppic-horizontal-nav");
		helpLink.addListener(Events.OnClick, new Listener<BaseEvent>() {

			@Override
			public void handleEvent(BaseEvent be) {
				History.newItem("help");
			}
		});
		
		return helpLink;
	}
	
	/**
	 * Creates link to the view containing downloads.
	 * @return link to downloads view
	 */
	private Label createDownloadsLink()
	{
		Label downloadsLink = new EmptyLink(AppPropertiesManager.CONSTANTS.bottom_panel_downloads_link_label());
		downloadsLink.addStyleName("eppic-horizontal-nav");
		downloadsLink.addStyleName("eppic-default-left-margin");
		downloadsLink.addListener(Events.OnClick, new Listener<BaseEvent>() {

			@Override
			public void handleEvent(BaseEvent be) {
				History.newItem("downloads");
			}
		});
	    
	    return downloadsLink;
	}
	
	/**
	 * Creates link to the view containing the releases.
	 * @return link to releases view
	 */
	private Label createReleasesLink()
	{
		Label releasesLink = new EmptyLink(AppPropertiesManager.CONSTANTS.bottom_panel_releases_link_label());
		releasesLink.addStyleName("eppic-horizontal-nav");
		releasesLink.addStyleName("eppic-default-left-margin");
		releasesLink.addListener(Events.OnClick, new Listener<BaseEvent>() {

			@Override
			public void handleEvent(BaseEvent be) {
				History.newItem("releases");
			}
		});
	    
	    return releasesLink;
	}
	
	/**
	 * Creates contact link.
	 * @return contact link
	 */
	private Label createContactLink()
	{
		Label contactLink = new Label("<a href=\"" +
				AppPropertiesManager.CONSTANTS.bottom_panel_contact_link() +
				"\" target=\"_blank\">" +
				AppPropertiesManager.CONSTANTS.bottom_panel_contact_link_label() +
				"</a>");
		contactLink.addStyleName("eppic-default-left-margin");
		contactLink.addStyleName("eppic-horizontal-nav");

		return contactLink;
	}
}
