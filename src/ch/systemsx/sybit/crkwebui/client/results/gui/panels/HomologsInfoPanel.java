package ch.systemsx.sybit.crkwebui.client.results.gui.panels;

import java.util.List;

import ch.systemsx.sybit.crkwebui.client.commons.appdata.AppPropertiesManager;
import ch.systemsx.sybit.crkwebui.client.commons.appdata.ApplicationContext;
import ch.systemsx.sybit.crkwebui.client.commons.events.ShowAlignmentsEvent;
import ch.systemsx.sybit.crkwebui.client.commons.events.ShowQueryWarningsEvent;
import ch.systemsx.sybit.crkwebui.client.commons.gui.links.EmptyLinkWithTooltip;
import ch.systemsx.sybit.crkwebui.client.commons.gui.links.ImageLinkWithTooltip;
import ch.systemsx.sybit.crkwebui.client.commons.gui.links.LinkWithTooltip;
import ch.systemsx.sybit.crkwebui.client.commons.managers.EventBusManager;
import ch.systemsx.sybit.crkwebui.client.commons.util.EscapedStringGenerator;
import ch.systemsx.sybit.crkwebui.shared.model.HomologsInfoItem;
import ch.systemsx.sybit.crkwebui.shared.model.QueryWarningItem;

import com.extjs.gxt.ui.client.event.BaseEvent;
import com.extjs.gxt.ui.client.event.Events;
import com.extjs.gxt.ui.client.event.Listener;
import com.extjs.gxt.ui.client.widget.Label;
import com.extjs.gxt.ui.client.widget.LayoutContainer;
import com.google.gwt.core.client.GWT;

/**
 * Panel used to store information about homologs.
 * @author AS
 *
 */
public class HomologsInfoPanel extends LayoutContainer
{
	public HomologsInfoPanel(final String selectedJobId,
							 final HomologsInfoItem homologsInfoItem,
							 final String pdbName)
	{
		
		if(homologsInfoItem.isHasQueryMatch())
		{
			createHomologsPanelQueryMatch(selectedJobId, homologsInfoItem, pdbName);
		}
		else
		{
			createHomologsPanelNoQueryMatch(homologsInfoItem);
		}
	}

	/**
	 * Creates homologs panel when there is query match.
	 * @param selectedJobId job id
	 * @param homologsInfoItem homologs info
	 * @param pdbName name of the pdb
	 */
	private void createHomologsPanelQueryMatch(final String selectedJobId,
											   final HomologsInfoItem homologsInfoItem,
											   final String pdbName)
	{
		final EmptyLinkWithTooltip chainsLink = new EmptyLinkWithTooltip("Chain " + EscapedStringGenerator.generateEscapedString(homologsInfoItem.getChains()), 
				 AppPropertiesManager.CONSTANTS.homologs_panel_chains_hint(),
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
		
		this.add(chainsLink);
		
		Label startUniprotLabel = new Label(" (");
		this.add(startUniprotLabel);
		
		LinkWithTooltip uniprotIdLabel = new LinkWithTooltip(EscapedStringGenerator.generateEscapedString(homologsInfoItem.getUniprotId()), 
			 AppPropertiesManager.CONSTANTS.homologs_panel_uniprot_hint(),
			 ApplicationContext.getWindowData(), 
			 0, 
			 ApplicationContext.getSettings().getUniprotLinkUrl() + homologsInfoItem.getUniprotId());
		uniprotIdLabel.addStyleName("eppic-external-link");
		this.add(uniprotIdLabel);
		
		Label endUniprotLabel = new Label(") ");
		this.add(endUniprotLabel);
		
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
		this.add(nrHomologsLabel);
		
		Label separatorLabel = new Label(" ");
		this.add(separatorLabel);
		
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
		this.add(colorPseImg);
		
	}
	
	private void createHomologsPanelNoQueryMatch(final HomologsInfoItem homologsInfoItem)
	{
		final EmptyLinkWithTooltip chainsLink = new EmptyLinkWithTooltip("Chain " + EscapedStringGenerator.generateEscapedString(homologsInfoItem.getChains()),
			 	 AppPropertiesManager.CONSTANTS.homologs_panel_uniprot_no_query_match_hint(),
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
		
		this.add(chainsLink);
	}
	
	/**
	 * Creates template for query warnings tooltip.
	 * @param warnings list of warnings
	 * @return template for query warnings
	 */
	private String generateHomologsNoQueryMatchTemplate(List<QueryWarningItem> warnings)
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
	
}
