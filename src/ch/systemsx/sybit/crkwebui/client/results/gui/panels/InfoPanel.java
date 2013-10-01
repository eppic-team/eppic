package ch.systemsx.sybit.crkwebui.client.results.gui.panels;

import java.util.ArrayList;
import java.util.List;

import ch.systemsx.sybit.crkwebui.client.commons.appdata.AppPropertiesManager;
import ch.systemsx.sybit.crkwebui.client.commons.appdata.ApplicationContext;
import ch.systemsx.sybit.crkwebui.client.commons.events.ApplicationWindowResizeEvent;
import ch.systemsx.sybit.crkwebui.client.commons.events.HideAllWindowsEvent;
import ch.systemsx.sybit.crkwebui.client.commons.events.ShowQueryWarningsEvent;
import ch.systemsx.sybit.crkwebui.client.commons.gui.data.TooltipXPositionType;
import ch.systemsx.sybit.crkwebui.client.commons.gui.data.TooltipYPositionType;
import ch.systemsx.sybit.crkwebui.client.commons.gui.images.ImageWithTooltip;
import ch.systemsx.sybit.crkwebui.client.commons.gui.links.LinkWithTooltip;
import ch.systemsx.sybit.crkwebui.client.commons.handlers.ApplicationWindowResizeHandler;
import ch.systemsx.sybit.crkwebui.client.commons.handlers.HideAllWindowsHandler;
import ch.systemsx.sybit.crkwebui.client.commons.handlers.ShowQueryWarningsHandler;
import ch.systemsx.sybit.crkwebui.client.commons.managers.EventBusManager;
import ch.systemsx.sybit.crkwebui.client.commons.util.EscapedStringGenerator;
import ch.systemsx.sybit.crkwebui.shared.model.HomologsInfoItem;
import ch.systemsx.sybit.crkwebui.shared.model.PDBScoreItem;

import com.extjs.gxt.ui.client.Style.Scroll;
import com.extjs.gxt.ui.client.core.Template;
import com.extjs.gxt.ui.client.widget.Label;
import com.extjs.gxt.ui.client.widget.form.FormPanel;
import com.extjs.gxt.ui.client.widget.layout.ColumnLayout;
import com.extjs.gxt.ui.client.widget.tips.ToolTip;
import com.extjs.gxt.ui.client.widget.tips.ToolTipConfig;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.Widget;

/**
 * Panel containing general information about results.
 * @author srebniak_a
 *
 */
public class InfoPanel extends FormPanel 
{
    private final static int ROWS_PER_PAGE = 3;

    PDBScoreItem pdbScoreItem;
    private int homologsStartIndex;
    private FlexTable homologsTable;
    private ToolTip inputParametersTooltip;
    private ToolTip queryWarningsTooltip;

    public InfoPanel(PDBScoreItem pdbScoreItem) 
    {
	this.getHeader().setVisible(false);
	this.setBodyBorder(false);
	this.setBorders(true);
	this.setLayout(new ColumnLayout());
	this.setScrollMode(Scroll.NONE);

	this.addStyleName("eppic-rounded-border");

	queryWarningsTooltip = createHomologsInfoTooltip();
	generateInfoPanel(pdbScoreItem);

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
     * Creates info panel containing general information about the job.
     */
    public void generateInfoPanel(PDBScoreItem pdbScoreItem)
    {
	this.removeAll();

	FlexTable flexTable = new FlexTable();
	flexTable.addStyleName("eppic-homologs-infopanel");

	LinkWithTooltip downloadResultsLink = createDownloadsLink(pdbScoreItem.getJobId());
	flexTable.setWidget(2, 0, downloadResultsLink);

	Label uniprotVersionlabel = createUniprotVersionlabel(pdbScoreItem.getRunParameters().getUniprotVer());
	flexTable.setWidget(0, 0, uniprotVersionlabel);

	Label eppicVersionLabel = createEppicVersionLabel(pdbScoreItem.getRunParameters().getCrkVersion());
	flexTable.setWidget(1, 0, eppicVersionLabel);
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
	    homologsInfoPanels.add(HomologsInfoPanel.generateHomologsInfoPanelItems(pdbScoreItem.getJobId(),
		    homologsStrings.get(i),
		    pdbScoreItem.getPdbName()));
	};
	return homologsInfoPanels;
    }

    /**
     * Creates link to download compressed results of processing.
     * @param jobId identifier of the job
     * @return link to compressed results
     */
    private LinkWithTooltip createDownloadsLink(String jobId)
    {
	LinkWithTooltip downloadResultsLink = new LinkWithTooltip(AppPropertiesManager.CONSTANTS.info_panel_download_results_link(), 
		AppPropertiesManager.CONSTANTS.info_panel_download_results_link_hint(), 
		ApplicationContext.getWindowData(), 
		0, 
		GWT.getModuleBaseURL() + "fileDownload?type=zip&id=" + jobId);
	downloadResultsLink.addStyleName("eppic-internal-link");
	return downloadResultsLink;
    }

    /**
     * Creates label containing version of the uniprot used for processing.
     * @param uniprotVersion version of uniprot used during processing
     * @return label with uniprot version
     */
    private Label createUniprotVersionlabel(String uniprotVersion)
    {
	Label uniprotVersionlabel = new Label(AppPropertiesManager.CONSTANTS.info_panel_uniprot() + ": " +
		EscapedStringGenerator.generateEscapedString(uniprotVersion));
	uniprotVersionlabel.addStyleName("eppic-default-label");
	return uniprotVersionlabel;
    }

    /**
     * Creates label containing version of the eppic application used for processing.
     * @param eppicVersion version of eppic application used during processing
     * @return label with eppic version
     */
    private Label createEppicVersionLabel(String eppicVersion)
    {
	Label eppicVersionLabel = new Label(AppPropertiesManager.CONSTANTS.info_panel_crk() + ": " +
		EscapedStringGenerator.generateEscapedString(eppicVersion));
	eppicVersionLabel.addStyleName("eppic-default-label");
	return eppicVersionLabel;
    }

    /**
     * Gets tooltip displayed over query warnings label.
     * @return tooltip displayed over query warnings label
     */
    public ToolTip getQueryWarningsTooltip() {
	return queryWarningsTooltip;
    }

    /**
     * Gets tooltip displayed over input parameters label.
     * @return tooltip displayed over input parameters label
     */
    public ToolTip getInputParametersTooltip() {
	return inputParametersTooltip;
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

		if(inputParametersTooltip != null)
		{
		    inputParametersTooltip.setVisible(false);
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

		if(inputParametersTooltip != null)
		{
		    inputParametersTooltip.setMaxWidth(calculateTooltipMaxWidth());
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
