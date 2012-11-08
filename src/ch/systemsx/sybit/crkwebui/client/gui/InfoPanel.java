package ch.systemsx.sybit.crkwebui.client.gui;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import ch.systemsx.sybit.crkwebui.client.controllers.AppPropertiesManager;
import ch.systemsx.sybit.crkwebui.client.controllers.ApplicationContext;
import ch.systemsx.sybit.crkwebui.client.controllers.EventBusManager;
import ch.systemsx.sybit.crkwebui.client.events.ApplicationWindowResizeEvent;
import ch.systemsx.sybit.crkwebui.client.events.HideAllWindowsEvent;
import ch.systemsx.sybit.crkwebui.client.events.ShowQueryWarningsEvent;
import ch.systemsx.sybit.crkwebui.client.gui.util.EscapedStringGenerator;
import ch.systemsx.sybit.crkwebui.client.handlers.ApplicationWindowResizeHandler;
import ch.systemsx.sybit.crkwebui.client.handlers.HideAllWindowsHandler;
import ch.systemsx.sybit.crkwebui.client.handlers.ShowQueryWarningsHandler;
import ch.systemsx.sybit.crkwebui.shared.model.HomologsInfoItem;
import ch.systemsx.sybit.crkwebui.shared.model.PDBScoreItem;
import ch.systemsx.sybit.crkwebui.shared.model.RunParametersItem;

import com.extjs.gxt.ui.client.Style.Scroll;
import com.extjs.gxt.ui.client.core.Template;
import com.extjs.gxt.ui.client.data.BeanModel;
import com.extjs.gxt.ui.client.data.BeanModelFactory;
import com.extjs.gxt.ui.client.data.BeanModelLookup;
import com.extjs.gxt.ui.client.event.BaseEvent;
import com.extjs.gxt.ui.client.event.Events;
import com.extjs.gxt.ui.client.event.Listener;
import com.extjs.gxt.ui.client.widget.Label;
import com.extjs.gxt.ui.client.widget.LayoutContainer;
import com.extjs.gxt.ui.client.widget.form.FormPanel;
import com.extjs.gxt.ui.client.widget.layout.FormData;
import com.extjs.gxt.ui.client.widget.layout.FormLayout;
import com.extjs.gxt.ui.client.widget.tips.ToolTip;
import com.extjs.gxt.ui.client.widget.tips.ToolTipConfig;
import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.HasVerticalAlignment;

/**
 * Panel containing general information about results.
 * @author srebniak_a
 *
 */
public class InfoPanel extends FormPanel 
{
	private EmptyLinkWithTooltip inputParametersLabel;
	private ToolTip inputParametersTooltip;
	private LinkWithTooltip downloadResultsLink;
	
	private ToolTip queryWarningsTooltip;
	
	public InfoPanel(PDBScoreItem pdbScoreItem) 
	{
		this.getHeader().setVisible(false);
		this.setBodyBorder(false);
		this.setBorders(true);
		this.setLayout(new FormLayout());
		this.setScrollMode(Scroll.AUTO);
//		this.addStyleName("eppic-default-padding");

		generateHomologsInfoTooltip();
		generateInfoPanel(pdbScoreItem);
		
		initializeEventsListeners();
	}

	/**
	 * Creates tooltip displayed over homologs query warnings.
	 */
	private void generateHomologsInfoTooltip() 
	{
		ToolTipConfig toolTipConfig = new ToolTipConfig();  
		toolTipConfig.setTitle(AppPropertiesManager.CONSTANTS.homologs_panel_query_warnings_title());
		toolTipConfig.setMouseOffset(new int[] {0, 0});  
		toolTipConfig.setCloseable(true); 
		toolTipConfig.setDismissDelay(0);
		toolTipConfig.setShowDelay(100);
		toolTipConfig.setMaxWidth(ApplicationContext.getWindowData().getWindowWidth() - 20);
		queryWarningsTooltip = new ToolTip(null, toolTipConfig);
	}

	/**
	 * Creates info panel containing general information about the job.
	 */
	public void generateInfoPanel(PDBScoreItem pdbScoreItem)
	{
		this.removeAll();
		
		FlexTable flexTable = new FlexTable();
		
		int nrOfRows = 3;
		int nrOfColumns = 4;
		
		List<HomologsInfoItem> homologsStrings = pdbScoreItem.getHomologsInfoItems();
		
		int limit = 50;
		
		if(ApplicationContext.isMyJobsListVisible())
		{
			limit += ApplicationContext.getMyJobsPanelWidth();
		}
		else
		{
			limit += 25;
		}
		
		// we divide the window width by desired number of columns (4) and use double width for first and half width for last 2
		// 1st column is double width
		// last 2 columns are half width
		int columnWidth = (ApplicationContext.getAdjustedWindowData().getWindowWidth() - limit - 20) / (nrOfColumns);
		int firstcolumnWidth = columnWidth * 2;
		int last2columnsWidth = columnWidth / 2;
		
		if(homologsStrings != null)
		{
			if(homologsStrings.size() > nrOfRows)
			{
				nrOfRows = homologsStrings.size();
			}
		}
			
		for(int i=0; i < nrOfColumns; i++)
		{
			for(int j=0; j<nrOfRows; j++)
			{
				if (i==0) {
					flexTable.getCellFormatter().setWidth(j, i, String.valueOf(firstcolumnWidth));
					flexTable.getCellFormatter().setAlignment(j, i, HasHorizontalAlignment.ALIGN_LEFT, HasVerticalAlignment.ALIGN_TOP);					
				} else if (i>0 && i<nrOfColumns-2) {
					flexTable.getCellFormatter().setWidth(j, i, String.valueOf(columnWidth));
					flexTable.getCellFormatter().setAlignment(j, i, HasHorizontalAlignment.ALIGN_LEFT, HasVerticalAlignment.ALIGN_TOP);
				} else {
					flexTable.getCellFormatter().setWidth(j, i, String.valueOf(last2columnsWidth));
					flexTable.getCellFormatter().setAlignment(j, i, HasHorizontalAlignment.ALIGN_LEFT, HasVerticalAlignment.ALIGN_TOP);
				}
			}
		}
		
		ToolTipConfig toolTipConfig = new ToolTipConfig();  
		toolTipConfig.setTitle(AppPropertiesManager.CONSTANTS.info_panel_input_parameters());
		toolTipConfig.setMouseOffset(new int[] {0, 0});  
		toolTipConfig.setTemplate(new Template(generateInputParametersTemplate(pdbScoreItem)));  
		toolTipConfig.setCloseable(true); 
		toolTipConfig.setDismissDelay(0);
		toolTipConfig.setShowDelay(100);
		toolTipConfig.setMaxWidth(ApplicationContext.getWindowData().getWindowWidth() - 20);
		inputParametersTooltip = new ToolTip(null, toolTipConfig);
		
		inputParametersLabel = new EmptyLinkWithTooltip(AppPropertiesManager.CONSTANTS.info_panel_input_parameters(),
														AppPropertiesManager.CONSTANTS.info_panel_input_parameters_hint(),
														ApplicationContext.getWindowData(),
														0);
		inputParametersLabel.addListener(Events.OnClick, new Listener<BaseEvent>() {

			@Override
			public void handleEvent(BaseEvent be) {
				inputParametersTooltip.showAt(inputParametersLabel.getAbsoluteLeft() + inputParametersLabel.getWidth() + 10, 
						   					  inputParametersLabel.getAbsoluteTop());
			}
			
		});
		
		inputParametersLabel.addStyleName("eppic-action");
		
		flexTable.setWidget(0, 1, inputParametersLabel);
		
		downloadResultsLink = new LinkWithTooltip(AppPropertiesManager.CONSTANTS.info_panel_download_results_link(), 
												  AppPropertiesManager.CONSTANTS.info_panel_download_results_link_hint(), 
												  ApplicationContext.getWindowData(), 
												  0, 
												  GWT.getModuleBaseURL() + "fileDownload?type=zip&id=" + pdbScoreItem.getJobId());
		downloadResultsLink.addStyleName("eppic-internal-link");
		flexTable.setWidget(2, 3, downloadResultsLink);
		
		Label uniprotVersionlabel = new Label(AppPropertiesManager.CONSTANTS.info_panel_uniprot() + ": " +
				EscapedStringGenerator.generateEscapedString(pdbScoreItem.getRunParameters().getUniprotVer()));
		uniprotVersionlabel.addStyleName("eppic-default-label");
		flexTable.setWidget(0, 3, uniprotVersionlabel);
		
		Label crkVersionLabel = new Label(AppPropertiesManager.CONSTANTS.info_panel_crk() + ": " +
				EscapedStringGenerator.generateEscapedString(pdbScoreItem.getRunParameters().getCrkVersion()));
		crkVersionLabel.addStyleName("eppic-default-label");
		flexTable.setWidget(1, 3, crkVersionLabel);
		
		if(homologsStrings != null)
		{
			for(int i=0; i<homologsStrings.size(); i++)
			{
				LayoutContainer homologsContainer = new HomologsInfoPanel(pdbScoreItem.getJobId(),
																		  homologsStrings.get(i),
																		  pdbScoreItem.getPdbName());
				flexTable.setWidget(i, 0, homologsContainer);
			};
		}
		
		// formdata -20 - fix for chrome - otherwise unnecessary scroll bar
		this.add(flexTable, new FormData("-20 100%"));
	}
	
	/**
	 * Creates content of the tooltip displayed over input parameters label and containing list of the parameters with values.
	 * @param pdbScoreItem result item containing values of input parameters
	 * @return template containing list of parameters with values
	 */
	private String generateInputParametersTemplate(PDBScoreItem pdbScoreItem)
	{
		String result = "<table>";
		
		BeanModelFactory factory = BeanModelLookup.get().getFactory(RunParametersItem.class);
		BeanModel runParametersModel = factory.createModel(pdbScoreItem.getRunParameters());
		 
		List<String> parametersList = new ArrayList<String>(runParametersModel.getPropertyNames());
		Collections.sort(parametersList);
		
		if(ApplicationContext.getSettings().getRunParametersNames() != null)
		{
			for(String parameter : parametersList)
			{
				if((ApplicationContext.getSettings().getRunParametersNames().get(parameter) != null))
				{
					result += "<tr>";
					result += "<td>";
					result += EscapedStringGenerator.generateEscapedString(
							ApplicationContext.getSettings().getRunParametersNames().get(parameter));
					result += "</td>";
					result += "<td></td>";
					result += "<td>";
					result += EscapedStringGenerator.generateEscapedString((String)runParametersModel.get(parameter));
					result += "</td>";
					result += "</tr>";
				}
			}
		}
		
		result += "</table>";
		return result;
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
					queryWarningsTooltip.setMaxWidth(ApplicationContext.getWindowData().getWindowWidth() - 20);
				}
				
				if(inputParametersTooltip != null)
				{
					inputParametersTooltip.setMaxWidth(ApplicationContext.getWindowData().getWindowWidth() - 20);
				}
			}
		});
	}
}
