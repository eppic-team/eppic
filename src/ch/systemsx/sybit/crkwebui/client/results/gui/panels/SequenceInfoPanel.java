package ch.systemsx.sybit.crkwebui.client.results.gui.panels;

import java.util.ArrayList;
import java.util.List;

import ch.systemsx.sybit.crkwebui.client.commons.appdata.AppPropertiesManager;
import ch.systemsx.sybit.crkwebui.client.commons.appdata.ApplicationContext;
import ch.systemsx.sybit.crkwebui.client.commons.events.ApplicationWindowResizeEvent;
import ch.systemsx.sybit.crkwebui.client.commons.events.HideAllWindowsEvent;
import ch.systemsx.sybit.crkwebui.client.commons.events.ShowAlignmentsEvent;
import ch.systemsx.sybit.crkwebui.client.commons.events.ShowHomologsEvent;
import ch.systemsx.sybit.crkwebui.client.commons.events.ShowQueryWarningsEvent;
import ch.systemsx.sybit.crkwebui.client.commons.gui.images.ImageWithTooltip;
import ch.systemsx.sybit.crkwebui.client.commons.gui.labels.LabelWithTooltip;
import ch.systemsx.sybit.crkwebui.client.commons.gui.links.ImageLinkWithTooltip;
import ch.systemsx.sybit.crkwebui.client.commons.gui.links.LinkWithTooltip;
import ch.systemsx.sybit.crkwebui.client.commons.handlers.ApplicationWindowResizeHandler;
import ch.systemsx.sybit.crkwebui.client.commons.handlers.HideAllWindowsHandler;
import ch.systemsx.sybit.crkwebui.client.commons.handlers.ShowQueryWarningsHandler;
import ch.systemsx.sybit.crkwebui.client.commons.managers.EventBusManager;
import ch.systemsx.sybit.crkwebui.client.commons.util.EscapedStringGenerator;
import ch.systemsx.sybit.crkwebui.client.commons.util.StyleGenerator;
import ch.systemsx.sybit.crkwebui.shared.model.HomologsInfoItem;
import ch.systemsx.sybit.crkwebui.shared.model.PDBScoreItem;
import ch.systemsx.sybit.crkwebui.shared.model.QueryWarningItem;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.Widget;
import com.sencha.gxt.core.client.dom.ScrollSupport.ScrollMode;
import com.sencha.gxt.core.client.dom.XElement;
import com.sencha.gxt.core.client.util.Margins;
import com.sencha.gxt.widget.core.client.button.IconButton;
import com.sencha.gxt.widget.core.client.button.IconButton.IconConfig;
import com.sencha.gxt.widget.core.client.container.CssFloatLayoutContainer;
import com.sencha.gxt.widget.core.client.event.SelectEvent;
import com.sencha.gxt.widget.core.client.event.SelectEvent.SelectHandler;
import com.sencha.gxt.widget.core.client.form.FieldSet;
import com.sencha.gxt.widget.core.client.tips.ToolTip;
import com.sencha.gxt.widget.core.client.tips.ToolTipConfig;

/**
 * Panel containing information about the sequences and their homologs.
 * @author srebniak_a
 *
 */
public class SequenceInfoPanel extends FieldSet 
{
    private final static int ROWS_PER_PAGE = 4;

    PDBScoreItem pdbScoreItem;
    private int homologsStartIndex;
    private FlexTable homologsTable;
    private ToolTip queryWarningsTooltip;
    
    public SequenceInfoPanel(PDBScoreItem pdbScoreItem) 
    {
    	this.setBorders(true);

    	this.addStyleName("eppic-rounded-border");
    	this.addStyleName("eppic-info-panel");

    	queryWarningsTooltip = createHomologsInfoTooltip();
    	generateSequenceInfoPanel(pdbScoreItem);

    	initializeEventsListeners();
    }

    /**
     * Creates tooltip displayed over homologs query warnings.
     */
    private ToolTip createHomologsInfoTooltip() 
    {
    	ToolTipConfig toolTipConfig = new ToolTipConfig();  
    	toolTipConfig.setTitleHtml(AppPropertiesManager.CONSTANTS.homologs_panel_query_warnings_title());
    	toolTipConfig.setMouseOffset(new int[] {0, 0});  
    	toolTipConfig.setCloseable(true); 
    	toolTipConfig.setDismissDelay(0);
    	toolTipConfig.setShowDelay(100);
    	toolTipConfig.setMaxWidth(calculateTooltipMaxWidth());
    	return new ToolTip(null, toolTipConfig);
    }
    
    /**
     * Creates tooltip displayed over homologs query warnings.
     */
    private ToolTip createHomologsInfoTooltip(String bodyString) 
    {
    	ToolTipConfig toolTipConfig = new ToolTipConfig(
    			AppPropertiesManager.CONSTANTS.homologs_panel_query_warnings_title(),
    			bodyString);
    	toolTipConfig.setMouseOffset(new int[] {0, 0});  
    	toolTipConfig.setCloseable(true); 
    	toolTipConfig.setDismissDelay(0);
    	toolTipConfig.setShowDelay(100);
    	toolTipConfig.setMaxWidth(calculateTooltipMaxWidth());
    	return new ToolTip(null, toolTipConfig);
    }
    
    /**
     * Sets the heading of the toolbar
     */
    public void fillHeading(String uniprot_version){
    	String fullHeading = AppPropertiesManager.CONSTANTS.info_panel_homologs_info();
    	
    	if(uniprot_version != null)
    		fullHeading = fullHeading+" ("+AppPropertiesManager.CONSTANTS.info_panel_uniprot() +" " +
            		EscapedStringGenerator.generateEscapedString(uniprot_version)+ ")";
    	
    	this.setHeadingHtml(
    			StyleGenerator.defaultFontStyleString(fullHeading));
    }

    /**
     * Creates info panel containing information about the sequence.
     */
    public void generateSequenceInfoPanel(PDBScoreItem pdbScoreItem)
    {
    	this.fillHeading(pdbScoreItem.getRunParameters().getUniprotVer());
    	
    	CssFloatLayoutContainer mainContainer = new CssFloatLayoutContainer();
    	mainContainer.setScrollMode(ScrollMode.AUTO);

    	FlexTable flexTable = new FlexTable();
    	flexTable.addStyleName("eppic-homologs-infopanel");

    	this.pdbScoreItem = pdbScoreItem; 

    	homologsTable = new FlexTable();
    	homologsTable.setCellSpacing(0);
    	homologsTable.setCellPadding(0);
    	homologsTable.addStyleName("eppic-homologstable");
    	homologsStartIndex = 0;
    	fillePagedHomologsInfoTable();

    	mainContainer.add(homologsTable);
    	mainContainer.add(flexTable);
    	
    	this.setWidget(mainContainer);
    }

    private void fillePagedHomologsInfoTable() {
    	List<HomologsInfoItem> homologsStrings = pdbScoreItem.getHomologsInfoItems();
    	if(homologsStrings == null || homologsStrings.isEmpty())
    	{
    		HTML nothingFound = new HTML(AppPropertiesManager.CONSTANTS.info_panel_nothing_found());
    		nothingFound.addStyleName("eppic-general-info-label");
    		homologsTable.clear();
    		homologsTable.setWidget(0,0,nothingFound);
    		return;
    	}
    	
    	List<List<Widget>> homologsInfoPanels = loadHomologPanles(pdbScoreItem, homologsStrings);
    	homologsTable.clear();
    	for(int i = homologsStartIndex; i < Math.min(homologsStartIndex + ROWS_PER_PAGE, homologsInfoPanels.size()); i++) {
    		final List<Widget> homologsContainer = homologsInfoPanels.get(i);
    		for(int j = 0; j < homologsContainer.size(); j++)
    			homologsTable.setWidget(i - homologsStartIndex, j + 1, homologsContainer.get(j));
    	}
    	
    	if(homologsInfoPanels.size() > homologsStartIndex + ROWS_PER_PAGE) {
    		String downIcon = "resources/icons/down-arrow.png";
    		ImageWithTooltip nextButton = new ImageWithTooltip(downIcon, null,
    				AppPropertiesManager.CONSTANTS.homologs_panel_next_homologs_button());
    		nextButton.addStyleName("eppic-homologs-infopanel-buttons");
    		nextButton.addClickHandler(new ClickHandler() {
    			@Override
    			public void onClick(ClickEvent event) {
    				homologsStartIndex += ROWS_PER_PAGE;
    				fillePagedHomologsInfoTable();
    			}
    		});
    		homologsTable.setWidget(3, 0, nextButton);
    	}
    	if(homologsStartIndex > 0) {
    		String downIcon = "resources/icons/up-arrow.png";
    		ImageWithTooltip prevButton = new ImageWithTooltip(downIcon, null,
    				AppPropertiesManager.CONSTANTS.homologs_panel_prev_homologs_button());
    		prevButton.addStyleName("eppic-homologs-infopanel-buttons");
    		prevButton.addClickHandler(new ClickHandler() {
    			@Override
    			public void onClick(ClickEvent event) {
    				homologsStartIndex -= ROWS_PER_PAGE;
    				fillePagedHomologsInfoTable();
    			}
    		});
    		homologsTable.setWidget(0, 0, prevButton);
    	}
    }

    private List<List<Widget>> loadHomologPanles(PDBScoreItem pdbScoreItem, List<HomologsInfoItem> homologsStrings) {
    	ArrayList<List<Widget>> homologsInfoPanels = new ArrayList<List<Widget>>();
    	for(int i=0; i<homologsStrings.size(); i++)
    	{
    		homologsInfoPanels.add(generateHomologsInfoPanelItems(pdbScoreItem.getJobId(),
    				homologsStrings.get(i),
    				pdbScoreItem.getPdbName()));
    	};
    	return homologsInfoPanels;
    }

    /**
     * Gets tooltip displayed over query warnings label.
     * @return tooltip displayed over query warnings label
     */
    public ToolTip getQueryWarningsTooltip() {
    	return queryWarningsTooltip;
    }

    /**
     * Creates template for query warnings tooltip.
     * @param warnings list of warnings
     * @return template for query warnings
     */
    private static String generateHomologsNoQueryMatchTemplate(List<QueryWarningItem> warnings)
    {
    	String warningsList = "<div><ul class=\"eppic-tooltip-list\">";

    	for(QueryWarningItem warning : warnings)
    	{
    		if((warning.getText() != null) &&
    				(!warning.getText().equals("")))
    		{
    			warningsList += "<li>" + EscapedStringGenerator.generateSanitizedString(warning.getText()) + "</li>";
    		}
    	}

    	warningsList += "</ul></div>";

    	return warningsList;
    }

    /**
     * Method to generate homologs in the sequence info panel
     * @param selectedJobId
     * @param homologsInfoItem
     * @param pdbName
     * @return
     */
    public static List<Widget>  generateHomologsInfoPanelItems(final String selectedJobId,
    		final HomologsInfoItem homologsInfoItem,
    		final String pdbName)
    		{

    	if(homologsInfoItem.isHasQueryMatch())
    	{
    		return createHomologsPanelItemsIfQueryMatch(selectedJobId, homologsInfoItem, pdbName);
    	}
    	else
    	{
    		return createHomologsPanelItemsIfNoQueryMatch(homologsInfoItem);
    	}
    		}

    private static List<Widget> createHomologsPanelItemsIfNoQueryMatch(final HomologsInfoItem homologsInfoItem)
    {
    	ArrayList<Widget> items = new ArrayList<Widget>();

    	String chainStr = EscapedStringGenerator.generateEscapedString(homologsInfoItem.getChains());
    	String chainHintStr = "";
    	if(chainStr.length() > 13){
    		chainStr = chainStr.substring(0,12)+",..)";
    		chainHintStr = "Chain " + EscapedStringGenerator.generateEscapedString(homologsInfoItem.getChains());
    	}

    	final LabelWithTooltip chainsLink = new LabelWithTooltip("Chain " + EscapedStringGenerator.generateEscapedString(chainStr), 
    			chainHintStr);

    	chainsLink.addStyleName("eppic-action");
    	items.add(chainsLink);

    	IconButton chainsLinkButton = createWarningButton(AppPropertiesManager.CONSTANTS.homologs_panel_uniprot_no_query_match_hint());		
    	chainsLinkButton.getElement().setMargins(new Margins(0, 10, 0, 0));
    	chainsLinkButton.addSelectHandler(new SelectHandler() {
			
			@Override
			public void onSelect(SelectEvent event) {
				EventBusManager.EVENT_BUS.fireEvent(new ShowQueryWarningsEvent(generateHomologsNoQueryMatchTemplate(homologsInfoItem.getQueryWarnings()),
    					chainsLink.getAbsoluteLeft() + chainsLink.getElement().getClientWidth(),
    					chainsLink.getAbsoluteTop() + chainsLink.getElement().getClientHeight() + 10));
				
			}
		});

    	items.add(chainsLinkButton);
    	return items;
    }

    private static List<Widget> createHomologsPanelItemsIfQueryMatch(final String selectedJobId,
    		final HomologsInfoItem homologsInfoItem,
    		final String pdbName) {
    	ArrayList<Widget> items = new ArrayList<Widget>();

    	String chainStr = EscapedStringGenerator.generateEscapedString(homologsInfoItem.getChains());
    	String chainHintStr = "";
    	if(chainStr.length() > 13){
    		chainStr = chainStr.substring(0,12)+",..)";
    		chainHintStr = "Chain " + EscapedStringGenerator.generateEscapedString(homologsInfoItem.getChains());
    	}

    	final LabelWithTooltip chainsLink = new LabelWithTooltip("Chain " + EscapedStringGenerator.generateEscapedString(chainStr), 
    			chainHintStr);

    	chainsLink.addStyleName("eppic-action");
    	items.add(chainsLink);
    		
    	IconButton chainsLinkButton = createMoreInfoButton(AppPropertiesManager.CONSTANTS.homologs_panel_chains_hint());		
    	chainsLinkButton.getElement().setMargins(new Margins(0, 10, 0, 0));
    	chainsLinkButton.addSelectHandler(new SelectHandler() {
			
			@Override
			public void onSelect(SelectEvent event) {
				EventBusManager.EVENT_BUS.fireEvent(new ShowAlignmentsEvent(
    					homologsInfoItem, 
    					pdbName,
    					chainsLink.getAbsoluteLeft() + chainsLink.getElement().getClientWidth(),
    					chainsLink.getAbsoluteTop() + chainsLink.getElement().getClientHeight() + 10));
    		}

		});

    	items.add(chainsLinkButton);

    	LinkWithTooltip uniprotIdLabel = new LinkWithTooltip(" (" + EscapedStringGenerator.generateEscapedString(homologsInfoItem.getUniprotId()) + ") ", 
    			AppPropertiesManager.CONSTANTS.homologs_panel_uniprot_hint(),
    			ApplicationContext.getSettings().getUniprotLinkUrl() + homologsInfoItem.getUniprotId());
    	uniprotIdLabel.addStyleName("eppic-external-link");
    	uniprotIdLabel.getElement().<XElement>cast().setMargins(new Margins(0, 10, 0, 0));
    	items.add(uniprotIdLabel);

    	int nrOfHomologs = homologsInfoItem.getNumHomologs();
    	String nrOfHomologsText = String.valueOf(nrOfHomologs) + " homolog";

    	if(nrOfHomologs > 1)
    	{
    		nrOfHomologsText += "s";
    	}

    	String alignmentId = homologsInfoItem.getChains().substring(0, 1);

    	final HTML nrHomologsLabel = new HTML(nrOfHomologsText);

    	nrHomologsLabel.addStyleName("eppic-action");
    	items.add(nrHomologsLabel);
    	
    	IconButton nrHoButton = createMoreInfoButton(AppPropertiesManager.CONSTANTS.homologs_panel_nrhomologs_hint());
    	nrHoButton.getElement().setMargins(new Margins(0, 10, 0, 0));
    	nrHoButton.addSelectHandler(new SelectHandler() {
			
			@Override
			public void onSelect(SelectEvent event) {
				EventBusManager.EVENT_BUS.fireEvent(new ShowHomologsEvent(
    					homologsInfoItem, 
    					selectedJobId,
    					nrHomologsLabel.getAbsoluteLeft() + nrHomologsLabel.getElement().getClientWidth(),
    					nrHomologsLabel.getAbsoluteTop() + nrHomologsLabel.getElement().getClientHeight() + 10));
				
			}
		});
    	
    	items.add(nrHoButton);

    	String downloadPseLink = GWT.getModuleBaseURL() + 
    			"fileDownload?type=entropiespse&id=" + selectedJobId + "&alignment=" + alignmentId; 

    	String colorPseIconImgSrc = 
    			ApplicationContext.getSettings().getResultsLocation() + 
    			selectedJobId+"/"+
    			pdbName +"."+alignmentId+".entropies.png";
    	//String colorPseIconImgSrc = "resources/icons/entropies_pse_icon.png";

    	ImageLinkWithTooltip colorPseImg = 
    			new ImageLinkWithTooltip(colorPseIconImgSrc, 14, 14, 
    					AppPropertiesManager.CONSTANTS.homologs_panel_entropiespse_hint(),
    					downloadPseLink);
    	items.add(colorPseImg);
    	return items;
    }
    
    /**
     * Creates a more Icon Button
     */
    private static IconButton createMoreInfoButton(String tooltipText){
    	IconConfig cnfg = new IconConfig("eppic-seq-info-panel-more-button");
    	IconButton button = new IconButton(cnfg);
    	button.setPixelSize(14, 14);
    	button.setBorders(false);
    	
    	new ToolTip(button, new ToolTipConfig(tooltipText));
    	
    	return button; 	
    }
    
    /**
     * Creates a warning Icon Button
     */
    private static IconButton createWarningButton(String tooltipText){
    	IconConfig cnfg = new IconConfig("eppic-seq-info-panel-warning-button");
    	IconButton button = new IconButton(cnfg);
    	button.setPixelSize(14, 14);
    	button.setBorders(false);
    	
    	new ToolTip(button, new ToolTipConfig(tooltipText));
    	
    	return button; 	
    }

    /**
     * Initializes events listeners.
     */
    private void initializeEventsListeners()
    {
    	EventBusManager.EVENT_BUS.addHandler(HideAllWindowsEvent.TYPE, new HideAllWindowsHandler() {

    		@Override
    		public void onHideAllWindows(HideAllWindowsEvent event) 
    		{
    			if(queryWarningsTooltip != null)
    			{
    				queryWarningsTooltip.setVisible(false);
    			}
    		}
    	});

    	EventBusManager.EVENT_BUS.addHandler(ShowQueryWarningsEvent.TYPE, new ShowQueryWarningsHandler() {

    		@Override
    		public void onShowQueryWarnings(ShowQueryWarningsEvent event) {
    			queryWarningsTooltip = createHomologsInfoTooltip(event.getTooltipTemplate());
    			queryWarningsTooltip.showAt(event.getxCoordinate(),
    					event.getyCoordinate());
    		}
    	});

    	EventBusManager.EVENT_BUS.addHandler(ApplicationWindowResizeEvent.TYPE, new ApplicationWindowResizeHandler() {

    		@Override
    		public void onResizeApplicationWindow(ApplicationWindowResizeEvent event) {

    			if(queryWarningsTooltip != null)
    			{
    				queryWarningsTooltip.setMaxWidth(calculateTooltipMaxWidth());
    			}
    		}
    	});
    }

    /**
     * Calculate max width which can be assigned to tooltip.
     * @return max width of the tooltip
     */
    private int calculateTooltipMaxWidth()
    {
    	int width = 500;

    	if(width > ApplicationContext.getWindowData().getWindowWidth() - 20)
    	{
    		width = ApplicationContext.getWindowData().getWindowWidth() - 20;
    	}

    	return width;
    }
}
