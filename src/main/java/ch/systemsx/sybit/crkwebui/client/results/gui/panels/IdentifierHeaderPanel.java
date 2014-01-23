package ch.systemsx.sybit.crkwebui.client.results.gui.panels;

import ch.systemsx.sybit.crkwebui.client.commons.appdata.AppPropertiesManager;
import ch.systemsx.sybit.crkwebui.client.commons.gui.links.LinkWithTooltip;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.Image;
import com.sencha.gxt.widget.core.client.container.HorizontalLayoutContainer;
import com.sencha.gxt.widget.core.client.container.SimpleContainer;
import com.sencha.gxt.widget.core.client.container.VerticalLayoutContainer;
import com.sencha.gxt.widget.core.client.container.VerticalLayoutContainer.VerticalLayoutData;
import com.sencha.gxt.core.client.dom.ScrollSupport.ScrollMode;
import com.sencha.gxt.core.client.util.Margins;

public class IdentifierHeaderPanel extends HorizontalLayoutContainer
{
    private PDBIdentifierPanel pdbIdentifierPanel;
    private PDBIdentifierSubtitlePanel pdbIdentifierSubtitlePanel;
    private HorizontalLayoutContainer downloadResultsPanel;
    
    private LinkWithTooltip downloadResultsLink;
    
    private VerticalLayoutContainer pdbInfo;
    
    private VerticalLayoutContainer eppicLogoPanel;
    
    private HTML eppicVersionLabel;

    public IdentifierHeaderPanel(int width){
    	
    	this.setWidth(width);
    	this.addStyleName("eppic-results-header-panel");
    	this.setScrollMode(ScrollMode.AUTOY);
    	
    	pdbInfo = new VerticalLayoutContainer();
    	pdbIdentifierPanel = new PDBIdentifierPanel();
    	pdbInfo.add(pdbIdentifierPanel, new VerticalLayoutData(1, 30, new Margins(0, 0, 0, 0)));

    	pdbIdentifierSubtitlePanel = new PDBIdentifierSubtitlePanel();
    	pdbInfo.add(pdbIdentifierSubtitlePanel, new VerticalLayoutData(-1, -1, new Margins(0, 0, 0, 0)));
    	
    	downloadResultsPanel = new HorizontalLayoutContainer();
    	downloadResultsPanel.setBorders(false);
		
		downloadResultsLink = new LinkWithTooltip(AppPropertiesManager.CONSTANTS.info_panel_download_results_link(),
				AppPropertiesManager.CONSTANTS.info_panel_download_results_link_hint(), "");
		downloadResultsLink.addStyleName("eppic-download-link");
		
		downloadResultsPanel.add(downloadResultsLink);
    	pdbInfo.add(downloadResultsPanel, new VerticalLayoutData(-1, -1, new Margins(0, 0, 10, 0)));

    	this.add(pdbInfo,  new HorizontalLayoutData(1,-1));

    	eppicLogoPanel = new VerticalLayoutContainer();
    	eppicLogoPanel.setWidth(120);

    	HorizontalLayoutContainer logo = getLogoContainer();
    	eppicLogoPanel.add(logo, new VerticalLayoutData(1, -1, new Margins(0, 0, 0, 0)));

    	eppicVersionLabel = new HTML("");
    	eppicVersionLabel.addStyleName("eppic-version");
    	eppicLogoPanel.add(eppicVersionLabel, new VerticalLayoutData(-1, -1, new Margins(0)));
    	
    	this.add(eppicLogoPanel, new HorizontalLayoutData(-1,1));
    }

    /**
	 * sets the link in the Download Results 
	 * @param PDBScoreItem
	 */
	public void setDownloadResultsLink(String jobId){

		downloadResultsLink .changeData(AppPropertiesManager.CONSTANTS.info_panel_download_results_link(),
				GWT.getModuleBaseURL() + "fileDownload?type=zip&id=" + jobId);

	}
	
	/**
	 * sets the link in the Download Results to empty value
	 * @param PDBScoreItem
	 */
	public void setEmptyDownloadResultsLink(){

		downloadResultsLink.changeData("","");

	}

	private HorizontalLayoutContainer getLogoContainer() {
    	String logoIconSource = "resources/images/eppic-logo.png";
    	Image logo = new Image(logoIconSource);
    	logo.setWidth("100px");
    	logo.setHeight("40px");
    	
    	HorizontalLayoutContainer con = new HorizontalLayoutContainer();
    	con.add(logo);
    	con.setHeight(40);
    	con.add(new SimpleContainer(), new HorizontalLayoutData(20,-1));
    	
    	return con;
    	
    }
    
    public void setEppicLogoPanel(String eppicVersion){
    	eppicVersionLabel.setHTML(eppicVersion);
    	
    }
    
    public void setPDBText(String pdbName, String spaceGroup, String expMethod, double resolution, double rfreeValue, int inputType)
    {
    	pdbIdentifierPanel.setPDBText(pdbName, spaceGroup, expMethod, resolution, rfreeValue, inputType);
    }

    public void setPDBIdentifierSubtitle(String subtitle)
    {
    	pdbIdentifierSubtitlePanel.setPDBIdentifierSubtitle(subtitle);
    }

    public void resizePanel(int width) {
    	this.setWidth(width);
    }
}
