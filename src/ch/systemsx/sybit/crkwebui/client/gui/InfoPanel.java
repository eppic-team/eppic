package ch.systemsx.sybit.crkwebui.client.gui;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import ch.systemsx.sybit.crkwebui.client.controllers.MainController;
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
	
	private MainController mainController;
	
	public InfoPanel(MainController mainController) 
	{
		this.mainController = mainController;
		this.getHeader().setVisible(false);
		this.setBodyBorder(false);
		this.setBorders(true);
		this.setLayout(new FormLayout());
		this.setScrollMode(Scroll.AUTO);
		this.setPadding(10);

		generateHomologsInfoTooltip(mainController);
		generateInfoPanel(mainController);
	}

	/**
	 * Creates tooltip displayed over homologs query warnings.
	 * @param mainController main application controller
	 */
	private void generateHomologsInfoTooltip(MainController mainController) 
	{
		ToolTipConfig toolTipConfig = new ToolTipConfig();  
		toolTipConfig.setTitle(MainController.CONSTANTS.homologs_panel_query_warnings_title());
		toolTipConfig.setMouseOffset(new int[] {0, 0});  
		toolTipConfig.setCloseable(true); 
		toolTipConfig.setDismissDelay(0);
		toolTipConfig.setShowDelay(100);
		queryWarningsTooltip = new ToolTip(null, toolTipConfig);
	}

	/**
	 * Creates info panel containing general information about the job.
	 * @param mainController main application controller
	 */
	public void generateInfoPanel(MainController mainController)
	{
		this.removeAll();
		
		FlexTable flexTable = new FlexTable();
		
		int nrOfRows = 3;
		int nrOfColumns = 5;
		
		List<HomologsInfoItem> homologsStrings = mainController.getPdbScoreItem().getHomologsInfoItems();
		
		int limit = 50;
		
		if(mainController.getMainViewPort().getMyJobsPanel().isExpanded())
		{
			limit += mainController.getMainViewPort().getMyJobsPanel().getWidth();
		}
		else
		{
			limit += 25;
		}
		
		// last 2 columns are half width of the others
		int columnWidth = (mainController.getWindowWidth() - limit - 20) / (nrOfColumns-1);
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
				if (i<nrOfColumns-2) {
					flexTable.getCellFormatter().setWidth(j, i, String.valueOf(columnWidth));
					flexTable.getCellFormatter().setAlignment(j, i, HasHorizontalAlignment.ALIGN_LEFT, HasVerticalAlignment.ALIGN_TOP);
				} else {
					flexTable.getCellFormatter().setWidth(j, i, String.valueOf(last2columnsWidth));
					flexTable.getCellFormatter().setAlignment(j, i, HasHorizontalAlignment.ALIGN_LEFT, HasVerticalAlignment.ALIGN_TOP);
				}
			}
		}
		
		ToolTipConfig toolTipConfig = new ToolTipConfig();  
		toolTipConfig.setTitle(MainController.CONSTANTS.info_panel_input_parameters());
		toolTipConfig.setMouseOffset(new int[] {0, 0});  
		toolTipConfig.setTemplate(new Template(generateInputParametersTemplate(mainController.getPdbScoreItem())));  
		toolTipConfig.setCloseable(true); 
		toolTipConfig.setDismissDelay(0);
		toolTipConfig.setShowDelay(100);
		toolTipConfig.setMaxWidth(mainController.getWindowWidth());
		inputParametersTooltip = new ToolTip(inputParametersLabel, toolTipConfig);
		
		inputParametersLabel = new EmptyLinkWithTooltip(MainController.CONSTANTS.info_panel_input_parameters(),
														MainController.CONSTANTS.info_panel_input_parameters_hint(),
														mainController,
														0);
		inputParametersLabel.addListener(Events.OnClick, new Listener<BaseEvent>() {

			@Override
			public void handleEvent(BaseEvent be) {
				inputParametersTooltip.showAt(inputParametersLabel.getAbsoluteLeft() + inputParametersLabel.getWidth() + 10, 
						   					  inputParametersLabel.getAbsoluteTop());
			}
			
		});
		
		inputParametersLabel.addStyleName("crk-default-label");
		
		flexTable.setWidget(0, 1, inputParametersLabel);
		
		downloadResultsLink = new LinkWithTooltip(MainController.CONSTANTS.info_panel_download_results_link(), 
												  MainController.CONSTANTS.info_panel_download_results_link_hint(), 
												  mainController, 
												  0, 
												  GWT.getModuleBaseURL() + "fileDownload?type=zip&id=" + mainController.getSelectedJobId());
		downloadResultsLink.addStyleName("crk-default-label");
		flexTable.setWidget(2, 4, downloadResultsLink);
		
		Label uniprotVersionlabel = new Label(MainController.CONSTANTS.info_panel_uniprot() + ": " +
				mainController.getPdbScoreItem().getRunParameters().getUniprotVer());
		uniprotVersionlabel.addStyleName("crk-default-label");
		flexTable.setWidget(0, 4, uniprotVersionlabel);
		
		Label crkVersionLabel = new Label(MainController.CONSTANTS.info_panel_crk() + ": " +
				mainController.getPdbScoreItem().getRunParameters().getCrkVersion());
		crkVersionLabel.addStyleName("crk-default-label");
		flexTable.setWidget(1, 4, crkVersionLabel);
		
		if(homologsStrings != null)
		{
			for(int i=0; i<homologsStrings.size(); i++)
			{
				LayoutContainer homologsContainer = new HomologsInfoPanel(mainController, homologsStrings.get(i), this);
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
		
		if(mainController.getSettings().getRunParametersNames() != null)
		{
			for(String parameter : parametersList)
			{
				if((mainController.getSettings().getRunParametersNames().get(parameter) != null))
				{
					result += "<tr>";
					result += "<td>";
					result += mainController.getSettings().getRunParametersNames().get(parameter);
					result += "</td>";
					result += "<td></td>";
					result += "<td>";
					result += runParametersModel.get(parameter);
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
}
