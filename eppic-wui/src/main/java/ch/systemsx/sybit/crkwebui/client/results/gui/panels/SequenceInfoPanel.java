package ch.systemsx.sybit.crkwebui.client.results.gui.panels;

import java.util.ArrayList;
import java.util.List;

//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;





import ch.systemsx.sybit.crkwebui.client.commons.appdata.AppPropertiesManager;
import ch.systemsx.sybit.crkwebui.client.commons.appdata.ApplicationContext;
import ch.systemsx.sybit.crkwebui.client.commons.events.ApplicationWindowResizeEvent;
import ch.systemsx.sybit.crkwebui.client.commons.events.HideAllWindowsEvent;
import ch.systemsx.sybit.crkwebui.client.commons.events.ShowAlignmentsEvent;
import ch.systemsx.sybit.crkwebui.client.commons.events.ShowHomologsEvent;
import ch.systemsx.sybit.crkwebui.client.commons.events.ShowQueryWarningsEvent;
import ch.systemsx.sybit.crkwebui.client.commons.gui.images.ImageWithTooltip;
import ch.systemsx.sybit.crkwebui.client.commons.gui.labels.LabelWithTooltip;
import ch.systemsx.sybit.crkwebui.client.commons.gui.links.EmptyLinkWithTooltip;
import ch.systemsx.sybit.crkwebui.client.commons.gui.links.ImageLinkWithTooltip;
//import ch.systemsx.sybit.crkwebui.client.commons.gui.links.ImageLinkWithTooltip;
import ch.systemsx.sybit.crkwebui.client.commons.handlers.ApplicationWindowResizeHandler;
import ch.systemsx.sybit.crkwebui.client.commons.handlers.HideAllWindowsHandler;
import ch.systemsx.sybit.crkwebui.client.commons.handlers.ShowQueryWarningsHandler;
import ch.systemsx.sybit.crkwebui.client.commons.managers.EventBusManager;
import ch.systemsx.sybit.crkwebui.client.commons.util.EscapedStringGenerator;
import ch.systemsx.sybit.crkwebui.client.commons.util.StyleGenerator;
import ch.systemsx.sybit.crkwebui.server.files.downloader.servlets.FileDownloadServlet;
import ch.systemsx.sybit.crkwebui.shared.model.ChainCluster;
import ch.systemsx.sybit.crkwebui.shared.model.PdbInfo;
import ch.systemsx.sybit.crkwebui.shared.model.UniProtRefWarning;






import com.google.gwt.core.client.GWT;
//import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.History;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Widget;
import com.sencha.gxt.core.client.dom.ScrollSupport.ScrollMode;
import com.sencha.gxt.core.client.dom.XElement;
import com.sencha.gxt.core.client.util.Margins;
import com.sencha.gxt.widget.core.client.button.IconButton;
import com.sencha.gxt.widget.core.client.button.IconButton.IconConfig;
import com.sencha.gxt.widget.core.client.container.CssFloatLayoutContainer;
import com.sencha.gxt.widget.core.client.container.HorizontalLayoutContainer.HorizontalLayoutData;
import com.sencha.gxt.widget.core.client.container.SimpleContainer;
import com.sencha.gxt.widget.core.client.event.SelectEvent;
import com.sencha.gxt.widget.core.client.event.SelectEvent.SelectHandler;
import com.sencha.gxt.widget.core.client.form.FieldSet;
import com.sencha.gxt.widget.core.client.tips.ToolTip;
import com.sencha.gxt.widget.core.client.tips.ToolTipConfig;

import eppic.EppicParams;

/**
 * Panel containing information about the sequences and their homologs.
 * @author srebniak_a
 *
 */
public class SequenceInfoPanel extends FieldSet 
{
	private final static int ROWS_PER_PAGE = 4;
	
	//private static final Logger logger = LoggerFactory.getLogger(SequenceInfoPanel.class);

	private PdbInfo pdbScoreItem;
	private int homologsStartIndex;
	private FlexTable homologsTable;
	private ToolTip queryWarningsTooltip;

	public SequenceInfoPanel(PdbInfo pdbScoreItem) 
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
		toolTipConfig.setMouseOffsetX(0);
		toolTipConfig.setMouseOffsetY(0);
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
		toolTipConfig.setMouseOffsetX(0);
		toolTipConfig.setMouseOffsetY(0);
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
	public void generateSequenceInfoPanel(PdbInfo pdbScoreItem)
	{
		this.fillHeading(pdbScoreItem.getRunParameters().getUniprotVersion());

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
		List<ChainCluster> homologsStrings = pdbScoreItem.getChainClusters();
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

	private List<List<Widget>> loadHomologPanles(PdbInfo pdbInfoItem, List<ChainCluster> homologsStrings) {
		ArrayList<List<Widget>> homologsInfoPanels = new ArrayList<List<Widget>>();
		for(int i=0; i<homologsStrings.size(); i++)
		{
			homologsInfoPanels.add(generateHomologsInfoPanelItems(pdbInfoItem.getJobId(),
					homologsStrings.get(i),
					pdbInfoItem.getTruncatedInputName(), pdbInfoItem.getInputType() == 0));
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
	private String generateHomologsNoQueryMatchTemplate(List<UniProtRefWarning> warnings)
	{
		String warningsList = "<div><ul class=\"eppic-tooltip-list\">";

		for(UniProtRefWarning warning : warnings)
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
	 * @param chainCluster
	 * @param pdbName
	 * @param precomputed 
	 * @return
	 */
	public List<Widget>  generateHomologsInfoPanelItems(final String selectedJobId,
			final ChainCluster chainCluster,
			final String pdbName, boolean precomputed) {

		if(chainCluster.isHasUniProtRef())
		{
			return createHomologsPanelItemsIfQueryMatch(selectedJobId, chainCluster, pdbName, precomputed);
		}
		else
		{
			return createHomologsPanelItemsIfNoQueryMatch(chainCluster);
		}
	}

	private List<Widget> createHomologsPanelItemsIfNoQueryMatch(final ChainCluster chainCluster) {
		ArrayList<Widget> items = new ArrayList<Widget>();

		final LabelWithTooltip chainsLink = createChainLink(chainCluster);
		items.add(chainsLink);

		Image chainsLinkButton = createWarningButton(AppPropertiesManager.CONSTANTS.homologs_panel_uniprot_no_query_match_hint());		
		chainsLinkButton.getElement().<XElement>cast().setMargins(new Margins(0, 10, 0, 0));
		ToolTipConfig ttConfig = new ToolTipConfig(AppPropertiesManager.CONSTANTS.homologs_panel_query_warnings_title(),
				generateHomologsNoQueryMatchTemplate(chainCluster.getUniProtRefWarnings()));
		new ToolTip(chainsLinkButton, ttConfig);

		items.add(chainsLinkButton);
		return items;
	}

	private List<Widget> createHomologsPanelItemsIfQueryMatch(final String selectedJobId,
			final ChainCluster chainCluster,
			final String pdbName, boolean precomputed) {
		ArrayList<Widget> items = new ArrayList<Widget>();

		final LabelWithTooltip chainsLink = createChainLink(chainCluster);
		items.add(chainsLink);
		
		int nrOfHomologs = chainCluster.getNumHomologs();
		String nrOfHomologsText = String.valueOf(nrOfHomologs) + " homolog";

		if(nrOfHomologs > 1)
		{
			nrOfHomologsText += "s";
		}

		final HTML nrHomologsLabel = new HTML(nrOfHomologsText);
		nrHomologsLabel.addStyleName("eppic-action");
		items.add(nrHomologsLabel);
		
		ImageWithTooltip sequenceIcon = new ImageWithTooltip("resources/icons/sequence_14.png", null, AppPropertiesManager.CONSTANTS.homologs_panel_chains_hint());
		sequenceIcon.addClickHandler(new ClickHandler() {		
			@Override
			public void onClick(ClickEvent event) {
				EventBusManager.EVENT_BUS.fireEvent(new ShowAlignmentsEvent(
						chainCluster, 
						pdbName,
						chainsLink.getAbsoluteLeft() + chainsLink.getElement().getClientWidth(),
						chainsLink.getAbsoluteTop() + chainsLink.getElement().getClientHeight() + 10));
			}
			
		});
		sequenceIcon.getElement().<XElement>cast().applyStyles("verticalAlign:bottom;");
		items.add(sequenceIcon);	
		
		ImageWithTooltip homologsIcon = new ImageWithTooltip("resources/icons/homologs_14.png", null, AppPropertiesManager.CONSTANTS.homologs_panel_nrhomologs_hint());
		homologsIcon.addClickHandler(new ClickHandler() {	
			@Override
			public void onClick(ClickEvent event) {
				EventBusManager.EVENT_BUS.fireEvent(new ShowHomologsEvent(
						chainCluster, 
						selectedJobId,
						pdbScoreItem,
						nrHomologsLabel.getAbsoluteLeft() + nrHomologsLabel.getElement().getClientWidth(),
						nrHomologsLabel.getAbsoluteTop() + nrHomologsLabel.getElement().getClientHeight() + 10));
			}
		});
		homologsIcon.getElement().<XElement>cast().applyStyles("verticalAlign:bottom;");
		items.add(homologsIcon);		
		
		// we only add the search link if a precomputed entry & not null chain cluster & we have sequence clusters for it
		if (precomputed && chainCluster!=null && chainCluster.getSeqCluster()!=null) {
			ImageLinkWithTooltip similarStructuresImage = createSearchSimilarStructuresIcon(chainCluster);
			similarStructuresImage.getElement().<XElement>cast().applyStyles("verticalAlign:bottom;");
			items.add(similarStructuresImage);
		}
		items.add(createPotatoImageLink(chainCluster, ApplicationContext.getPdbInfo().getJobId(), ApplicationContext.getPdbInfo()));

		return items;
	}
	
	private ImageLinkWithTooltip createPotatoImageLink(ChainCluster chainCluster, String jobId, PdbInfo pdbInfo) {
		
		String repChainId = chainCluster.getRepChain();
		//String pdbName = pdbInfo.getTruncatedInputName();
		String downloadPseLink = GWT.getModuleBaseURL() + 
				FileDownloadServlet.SERVLET_NAME + "?" +
				FileDownloadServlet.PARAM_TYPE+"=" + FileDownloadServlet.TYPE_VALUE_ENTROPIESPSE+
				"&"+FileDownloadServlet.PARAM_ID+"=" + jobId + 
				"&"+FileDownloadServlet.PARAM_REP_CHAIN_ID+"=" + repChainId;
		
		String colorPseIconImgSrc = "resources/icons/potato_14.png";

		ImageLinkWithTooltip potatoImage = new ImageLinkWithTooltip(colorPseIconImgSrc, 
						14, 14, 
						AppPropertiesManager.CONSTANTS.homologs_panel_entropiespse_hint(), 
						downloadPseLink);
		String html = potatoImage.getHTML();
		int insertat = html.indexOf("></a>");
		if (insertat != -1)
			html = html.substring(0,insertat) + " style='position:relative;top:3px'" + html.substring(insertat,html.length());
		potatoImage.setHTML(html);
		return potatoImage;
		
	}

	private ImageLinkWithTooltip createSearchSimilarStructuresIcon(final ChainCluster chainCluster) {
		String url = "searchPdb/"+chainCluster.getPdbCode() + "/" + chainCluster.getRepChain();
		ImageLinkWithTooltip imagelink = new ImageLinkWithTooltip("resources/icons/related_14.png", 
				14, 14, 
				AppPropertiesManager.CONSTANTS.homologs_panel_entropiespse_hint(), 
				url);
		imagelink.getElement().<XElement>cast().applyStyles("verticalAlign:bottom;");
		return imagelink;
	}
	
	//alternative implementation
	/*private Image createSearchSimilarStructuresIcon(final ChainCluster chainCluster) {
		Image similarStructuresIcon = new Image("resources/icons/related_14.png");
		similarStructuresIcon.getElement().<XElement>cast().applyStyles("verticalAlign:bottom;");
		similarStructuresIcon.addClickHandler(new ClickHandler() {	
			@Override
			public void onClick(ClickEvent event) {
				History.newItem("searchPdb/"+chainCluster.getPdbCode() + "/" + chainCluster.getRepChain());
			}
		});
		return similarStructuresIcon;
	}*/

	private LabelWithTooltip createChainLink(final ChainCluster chainCluster) {
		String chainStr = chainCluster.getRepChain();
		if(chainCluster.getMemberChainsNoRepresentative() != null){
			chainStr += "(" + EscapedStringGenerator.generateEscapedString(chainCluster.getMemberChainsNoRepresentative()) + ")";
		}
		String chainHintStr = "";
		if(chainStr.length() > 13){
			chainHintStr = "Chain " + chainStr;
			chainStr = chainStr.substring(0,12)+",..)";
		}

		final LabelWithTooltip chainsLink = new LabelWithTooltip("Chain " + EscapedStringGenerator.generateEscapedString(chainStr), 
				chainHintStr);

		chainsLink.addStyleName("eppic-action");
		return chainsLink;
	}

	/**
	 * Creates a more Icon Button
	 */
	private IconButton createMoreInfoButton(String tooltipText){
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
	private Image createWarningButton(String tooltipText){

		Image warningImage = new Image("resources/icons/warning_icon_14x14.png");
		warningImage.getElement().<XElement>cast().applyStyles("verticalAlign:bottom;");

		return warningImage; 	
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
