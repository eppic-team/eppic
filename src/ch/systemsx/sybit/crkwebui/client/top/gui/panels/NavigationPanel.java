package ch.systemsx.sybit.crkwebui.client.top.gui.panels;

import ch.systemsx.sybit.crkwebui.client.commons.appdata.AppPropertiesManager;
import ch.systemsx.sybit.crkwebui.client.commons.gui.links.EmptyLink;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.History;
import com.google.gwt.user.client.ui.HTML;
import com.sencha.gxt.widget.core.client.container.HorizontalLayoutContainer;
import com.sencha.gxt.widget.core.client.container.HorizontalLayoutContainer.HorizontalLayoutData;
import com.sencha.gxt.widget.core.client.container.VBoxLayoutContainer;

/**
 * Panel containing navigation links.
 * @author srebniak_a
 *
 */
public class NavigationPanel extends VBoxLayoutContainer
{
	public NavigationPanel()
	{
	    this.setBorders(false);
	    createLinksPanel();
	}
	
	/**
	 * Creates panel containing navigation links.
	 */
	private void createLinksPanel()
    {
		this.setVBoxLayoutAlign(VBoxLayoutAlign.RIGHT);
        this.setBorders(false);

        HorizontalLayoutContainer linksContainer = new HorizontalLayoutContainer();
        linksContainer.setWidth(440);
        
        HTML homeLink = createHomeLink();
        HTML publicationsLink = createPublicationsLink();
        HTML helpLink = createHelpLink();
       // HTML changeViewerLink = createChangeViewerLink();
        HTML downloadsLink = createDownloadsLink();
        HTML releasesLink = createReleasesLink();
        HTML fAQLink = createFAQLink();

        linksContainer.add(homeLink, new HorizontalLayoutData(-1,1));
        linksContainer.add(createBreakLabel(), new HorizontalLayoutData(-1,1));
        linksContainer.add(downloadsLink, new HorizontalLayoutData(-1,1));
        linksContainer.add(createBreakLabel(), new HorizontalLayoutData(-1,1));
       // linksContainer.add(changeViewerLink, new HorizontalLayoutData(-1,1));
       // linksContainer.add(createBreakLabel(), new HorizontalLayoutData(-1,1));
        linksContainer.add(helpLink, new HorizontalLayoutData(-1,1));
        linksContainer.add(createBreakLabel(), new HorizontalLayoutData(-1,1));
        linksContainer.add(fAQLink, new HorizontalLayoutData(-1,1));
        linksContainer.add(createBreakLabel(), new HorizontalLayoutData(-1,1));
        linksContainer.add(releasesLink, new HorizontalLayoutData(-1,1));
        linksContainer.add(createBreakLabel(), new HorizontalLayoutData(-1,1));        
        linksContainer.add(publicationsLink, new HorizontalLayoutData(-1,1));
        
        this.add(linksContainer);
    }
	
    private HTML createBreakLabel()
    {
        HTML breakLabel = new HTML("&nbsp;|&nbsp;");
        breakLabel.addStyleName("eppic-default-left-margin");
        breakLabel.addStyleName("eppic-horizontal-nav");
        return breakLabel;
    }

	
	/**
	 * Creates link to the home page.
	 * @return link to the home page
	 */
	private HTML createHomeLink()
	{
		HTML homeLink = new EmptyLink(AppPropertiesManager.CONSTANTS.navigation_panel_home_link_label());
		homeLink.addStyleName("eppic-horizontal-nav");
		homeLink.addClickHandler(new ClickHandler() {	
			@Override
			public void onClick(ClickEvent event) {
				History.newItem("");
			}
		});
	    
	    return homeLink;
	}
	
	/**
	 * Creates link to publications.
	 * @return link to publications
	 */
	private HTML createPublicationsLink()
	{
		HTML aboutLink = new EmptyLink(AppPropertiesManager.CONSTANTS.navigation_panel_publications_link_label());
	    aboutLink.addStyleName("eppic-horizontal-nav");
	    aboutLink.addStyleName("eppic-default-left-margin");
	    aboutLink.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				History.newItem("publications");
			}
		});
	    
	    return aboutLink;
	}
	
	/**
	 * Creates link to help page.
	 * @return link to help page
	 */
	private HTML createHelpLink()
	{
		HTML helpLink = new EmptyLink(AppPropertiesManager.CONSTANTS.navigation_panel_help_link_label());
		helpLink.addStyleName("eppic-default-left-margin");
		helpLink.addStyleName("eppic-horizontal-nav");
		helpLink.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				History.newItem("help");
			}
		});
		
		return helpLink;
	}
	
	/**
	 * Creates link to faq page.
	 * @return link to faq page
	 */
	private HTML createFAQLink()
	{
		HTML helpLink = new EmptyLink(AppPropertiesManager.CONSTANTS.navigation_panel_faq_link_label());
		helpLink.addStyleName("eppic-default-left-margin");
		helpLink.addStyleName("eppic-horizontal-nav");
		helpLink.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				History.newItem("faq");
			}
		});
		
		return helpLink;
	}
	
//	/**
//	 * Creates viewer change link.
//	 * @return link
//	 */
//	private HTML createChangeViewerLink()
//	{
//		HTML link = new EmptyLink(AppPropertiesManager.CONSTANTS.navigation_panel_change_viewer_link_label());
//		link.addStyleName("eppic-default-left-margin");
//		link.addStyleName("eppic-horizontal-nav");
//		link.addClickHandler(new ClickHandler() {
//			@Override
//			public void onClick(ClickEvent event) {
//				EventBusManager.EVENT_BUS.fireEvent(new ShowViewerSelectorEvent());
//			}
//		});
//		
//		return link;
//	}
	
	/**
	 * Creates link to the view containing downloads.
	 * @return link to downloads view
	 */
	private HTML createDownloadsLink()
	{
		HTML downloadsLink = new EmptyLink(AppPropertiesManager.CONSTANTS.navigation_panel_downloads_link_label());
		downloadsLink.addStyleName("eppic-horizontal-nav");
		downloadsLink.addStyleName("eppic-default-left-margin");
		downloadsLink.addClickHandler(new ClickHandler() {	
			@Override
			public void onClick(ClickEvent event) {
				History.newItem("downloads");	
			}
		});
	    
	    return downloadsLink;
	}
	
	/**
	 * Creates link to the view containing the releases.
	 * @return link to releases view
	 */
	private HTML createReleasesLink()
	{
		HTML releasesLink = new EmptyLink(AppPropertiesManager.CONSTANTS.navigation_panel_releases_link_label());
		releasesLink.addStyleName("eppic-horizontal-nav");
		releasesLink.addStyleName("eppic-default-left-margin");
		releasesLink.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				History.newItem("releases");	
			}
		});
	    
	    return releasesLink;
	}
	
}
