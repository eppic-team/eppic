package ch.systemsx.sybit.crkwebui.client.results.gui.panels;

import java.util.ArrayList;
import java.util.List;

import ch.systemsx.sybit.crkwebui.client.commons.appdata.AppPropertiesManager;
import ch.systemsx.sybit.crkwebui.client.commons.appdata.ApplicationContext;
import ch.systemsx.sybit.crkwebui.client.commons.events.ApplicationWindowResizeEvent;
import ch.systemsx.sybit.crkwebui.client.commons.events.HideAllWindowsEvent;
import ch.systemsx.sybit.crkwebui.client.commons.events.ShowAlignmentsEvent;
import ch.systemsx.sybit.crkwebui.client.commons.events.ShowQueryWarningsEvent;
import ch.systemsx.sybit.crkwebui.client.commons.gui.data.TooltipXPositionType;
import ch.systemsx.sybit.crkwebui.client.commons.gui.data.TooltipYPositionType;
import ch.systemsx.sybit.crkwebui.client.commons.gui.images.ImageWithTooltip;
import ch.systemsx.sybit.crkwebui.client.commons.gui.links.EmptyLinkWithTooltip;
import ch.systemsx.sybit.crkwebui.client.commons.gui.links.ImageLinkWithTooltip;
import ch.systemsx.sybit.crkwebui.client.commons.gui.links.LinkWithTooltip;
import ch.systemsx.sybit.crkwebui.client.commons.handlers.ApplicationWindowResizeHandler;
import ch.systemsx.sybit.crkwebui.client.commons.handlers.HideAllWindowsHandler;
import ch.systemsx.sybit.crkwebui.client.commons.handlers.ShowQueryWarningsHandler;
import ch.systemsx.sybit.crkwebui.client.commons.managers.EventBusManager;
import ch.systemsx.sybit.crkwebui.client.commons.util.EscapedStringGenerator;
import ch.systemsx.sybit.crkwebui.shared.model.HomologsInfoItem;
import ch.systemsx.sybit.crkwebui.shared.model.PDBScoreItem;
import ch.systemsx.sybit.crkwebui.shared.model.QueryWarningItem;

import com.extjs.gxt.ui.client.Style.Scroll;
import com.extjs.gxt.ui.client.core.Template;
import com.extjs.gxt.ui.client.event.BaseEvent;
import com.extjs.gxt.ui.client.event.Events;
import com.extjs.gxt.ui.client.event.Listener;
import com.extjs.gxt.ui.client.widget.Label;
import com.extjs.gxt.ui.client.widget.form.FieldSet;
import com.extjs.gxt.ui.client.widget.layout.ColumnLayout;
import com.extjs.gxt.ui.client.widget.tips.ToolTip;
import com.extjs.gxt.ui.client.widget.tips.ToolTipConfig;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.Widget;

/**
 * Panel containing information about the sequences and their homologs.
 * @author srebniak_a
 *
 */
public class SequenceInfoPanel extends FieldSet 
{
    private final static int ROWS_PER_PAGE = 3;

    PDBScoreItem pdbScoreItem;
    private int homologsStartIndex;
    private FlexTable homologsTable;
    private ToolTip queryWarningsTooltip;

    public SequenceInfoPanel(PDBScoreItem pdbScoreItem) 
    {
    
	this.setBorders(true);
	this.setLayout(new ColumnLayout());
	this.setScrollMode(Scroll.AUTO);

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
	toolTipConfig.setTitle(AppPropertiesManager.CONSTANTS.homologs_panel_query_warnings_title());
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
    	
    	this.setHeading(fullHeading);
    }

    /**
     * Creates info panel containing information about the sequence.
     */
    public void generateSequenceInfoPanel(PDBScoreItem pdbScoreItem)
    {
	this.removeAll();
	
	this.fillHeading(pdbScoreItem.getRunParameters().getUniprotVer());

	FlexTable flexTable = new FlexTable();
	flexTable.addStyleName("eppic-homologs-infopanel");

	this.pdbScoreItem = pdbScoreItem; 

	homologsTable = new FlexTable();
	homologsTable.addStyleName("eppic-homologstable");
	homologsStartIndex = 0;
	fillePagedHomologsInfoTable();

	this.add(homologsTable);
	this.add(flexTable);
    }

    private void fillePagedHomologsInfoTable() {
	List<HomologsInfoItem> homologsStrings = pdbScoreItem.getHomologsInfoItems();
	if(homologsStrings == null)
	{
		Label nothingFound = new Label(AppPropertiesManager.CONSTANTS.info_panel_nothing_found());
		nothingFound.addStyleName("eppic-general-info-label");
		this.add(nothingFound);
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
		    AppPropertiesManager.CONSTANTS.homologs_panel_next_homologs_button(), 
		    -1, true, 0, 0,TooltipXPositionType.RIGHT, TooltipYPositionType.TOP);
	    nextButton.addClickHandler(new ClickHandler() {
		@Override
		public void onClick(ClickEvent event) {
		    homologsStartIndex += ROWS_PER_PAGE;
		    fillePagedHomologsInfoTable();
		}
	    });
	    homologsTable.setWidget(2, 0, nextButton);
	}
	if(homologsStartIndex > 0) {
	    String downIcon = "resources/icons/up-arrow.png";
	    ImageWithTooltip prevButton = new ImageWithTooltip(downIcon, null,
		    AppPropertiesManager.CONSTANTS.homologs_panel_prev_homologs_button(), 
		    -1, true, 0, 0,TooltipXPositionType.RIGHT, TooltipYPositionType.TOP);
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
	String chainHintStr = AppPropertiesManager.CONSTANTS.homologs_panel_uniprot_no_query_match_hint();
	if(chainStr.length() > 13){
		chainStr = chainStr.substring(0,12)+",..)";
		chainHintStr = "<b>Chain " + EscapedStringGenerator.generateEscapedString(homologsInfoItem.getChains())+
				"</b><br>"+AppPropertiesManager.CONSTANTS.homologs_panel_chains_hint();;
	}
	
	final EmptyLinkWithTooltip chainsLink = new EmptyLinkWithTooltip("Chain " + EscapedStringGenerator.generateEscapedString(chainStr), 
		chainHintStr,
		ApplicationContext.getWindowData(), 
		0);
	
	chainsLink.addStyleName("eppic-action");

	chainsLink.addListener(Events.OnClick, new Listener<BaseEvent>() {

	    @Override
	    public void handleEvent(BaseEvent be) {

		EventBusManager.EVENT_BUS.fireEvent(new ShowQueryWarningsEvent(generateHomologsNoQueryMatchTemplate(homologsInfoItem.getQueryWarnings()),
			chainsLink.getAbsoluteLeft() + chainsLink.getWidth(),
			chainsLink.getAbsoluteTop() + chainsLink.getHeight() + 10));
	    }

	});

	items.add(chainsLink);
	return items;
    }

    private static List<Widget> createHomologsPanelItemsIfQueryMatch(final String selectedJobId,
	    final HomologsInfoItem homologsInfoItem,
	    final String pdbName) {
	ArrayList<Widget> items = new ArrayList<Widget>();
	
	String chainStr = EscapedStringGenerator.generateEscapedString(homologsInfoItem.getChains());
	String chainHintStr = AppPropertiesManager.CONSTANTS.homologs_panel_chains_hint();
	if(chainStr.length() > 13){
		chainStr = chainStr.substring(0,12)+",..)";
		chainHintStr = "<b>Chain " + EscapedStringGenerator.generateEscapedString(homologsInfoItem.getChains())+
				"</b><br>"+AppPropertiesManager.CONSTANTS.homologs_panel_chains_hint();;
	}
	
	final EmptyLinkWithTooltip chainsLink = new EmptyLinkWithTooltip("Chain " + EscapedStringGenerator.generateEscapedString(chainStr), 
		chainHintStr,
		ApplicationContext.getWindowData(), 
		0);

	chainsLink.addStyleName("eppic-action");

	chainsLink.addListener(Events.OnClick, new Listener<BaseEvent>() {

	    @Override
	    public void handleEvent(BaseEvent be) {
		EventBusManager.EVENT_BUS.fireEvent(new ShowAlignmentsEvent(
			homologsInfoItem, 
			pdbName,
			chainsLink.getAbsoluteLeft() + chainsLink.getWidth(),
			chainsLink.getAbsoluteTop() + chainsLink.getHeight() + 10));
	    }

	});

	items.add(chainsLink);

	LinkWithTooltip uniprotIdLabel = new LinkWithTooltip(" (" + EscapedStringGenerator.generateEscapedString(homologsInfoItem.getUniprotId()) + ") ", 
		AppPropertiesManager.CONSTANTS.homologs_panel_uniprot_hint(),
		ApplicationContext.getWindowData(), 
		0, 
		ApplicationContext.getSettings().getUniprotLinkUrl() + homologsInfoItem.getUniprotId());
	uniprotIdLabel.addStyleName("eppic-external-link");
	items.add(uniprotIdLabel);

	int nrOfHomologs = homologsInfoItem.getNumHomologs();
	String nrOfHomologsText = String.valueOf(nrOfHomologs) + " homolog";

	if(nrOfHomologs > 1)
	{
	    nrOfHomologsText += "s";
	}

	String alignmentId = homologsInfoItem.getChains().substring(0, 1);
	String downloadLink = GWT.getModuleBaseURL() + "fileDownload?type=fasta&id=" + selectedJobId + "&alignment=" + alignmentId; 

	LinkWithTooltip nrHomologsLabel = new LinkWithTooltip(nrOfHomologsText, 
		AppPropertiesManager.CONSTANTS.homologs_panel_nrhomologs_hint(),
		ApplicationContext.getWindowData(), 
		0, 
		downloadLink);

	nrHomologsLabel.addStyleName("eppic-internal-link");
	items.add(nrHomologsLabel);

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
			ApplicationContext.getWindowData(), 
			0, 
			downloadPseLink);
	items.add(colorPseImg);
	return items;
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
		queryWarningsTooltip.getToolTipConfig().setTemplate(new Template(event.getTooltipTemplate()));

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
