package ch.systemsx.sybit.crkwebui.client.gui;

import java.util.List;

import ch.systemsx.sybit.crkwebui.client.controllers.MainController;

import com.extjs.gxt.ui.client.Style.Scroll;
import com.extjs.gxt.ui.client.widget.Label;
import com.extjs.gxt.ui.client.widget.form.FormPanel;
import com.extjs.gxt.ui.client.widget.layout.FormData;
import com.extjs.gxt.ui.client.widget.layout.FormLayout;
import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.HasVerticalAlignment;

/**
 * The panel containing parameters values and download link
 * @author srebniak_a
 *
 */
public class InfoPanel extends FormPanel 
{
	private Label totalCoreSizeXtalCallCutoff;
	private Label infoPanelMinNumberHomologsRequired;
	private Label infoPanelSequenceIdentityCutoff;
	private Label infoPanelQueryCoverageCutoff;
	private Label infoPanelPerMemberCoreSizeXtalCallCutoff;
	private Label infoPanelMaxNumSequencesUsed;
	private Label infoPanelBioCallCutoff;
	private Label infoPanelXtalCallCutoff;
	private HTML downloadResultsLink;
	
	public InfoPanel(MainController mainController) 
	{
		this.getHeader().setVisible(false);
		this.setBodyBorder(false);
		this.setBorders(true);
		this.setLayout(new FormLayout());
		this.setScrollMode(Scroll.AUTO);
		this.setPadding(10);

		generateInfoPanel(mainController);
	}

	public void generateInfoPanel(MainController mainController)
	{
		this.removeAll();
		
		FlexTable flexTable = new FlexTable();
		
		int nrOfRows = 3;
		int nrOfColumns = 4;
		
		List<String> homologsStrings = mainController.getPdbScoreItem().getNumHomologsStrings();
		
		int limit = 50;
		
		if(mainController.getMainViewPort().getMyJobsPanel().isExpanded())
		{
			limit += mainController.getMainViewPort().getMyJobsPanel().getWidth();
		}
		else
		{
			limit += 25;
		}
		
		int columnWidth = (mainController.getWindowWidth() - limit - 20) / nrOfColumns;
		
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
				flexTable.getCellFormatter().setWidth(j, i, String.valueOf(columnWidth));
				flexTable.getCellFormatter().setAlignment(j, i, HasHorizontalAlignment.ALIGN_LEFT, HasVerticalAlignment.ALIGN_TOP);
			}
		}
		
		totalCoreSizeXtalCallCutoff = new Label(MainController.CONSTANTS.info_panel_total_core_size_xtal_call_cutoff() + ": " + mainController.getPdbScoreItem().getMinCoreSize());
		totalCoreSizeXtalCallCutoff.addStyleName("crk-default-label");
		flexTable.setWidget(0, 0, totalCoreSizeXtalCallCutoff);
		
		infoPanelMinNumberHomologsRequired = new Label(MainController.CONSTANTS.info_panel_min_number_homologs_required() + ": " + mainController.getPdbScoreItem().getHomologsCutoff());
		infoPanelMinNumberHomologsRequired.addStyleName("crk-default-label");
		flexTable.setWidget(1, 0, infoPanelMinNumberHomologsRequired);
		
		infoPanelSequenceIdentityCutoff = new Label(MainController.CONSTANTS.info_panel_sequence_identity_cutoff() + ": " + mainController.getPdbScoreItem().getIdCutoff());
		infoPanelSequenceIdentityCutoff.addStyleName("crk-default-label");
		flexTable.setWidget(2, 0, infoPanelSequenceIdentityCutoff);
		
		infoPanelQueryCoverageCutoff = new Label(MainController.CONSTANTS.info_panel_query_coverage_cutoff() + ": " + mainController.getPdbScoreItem().getQueryCovCutoff());
		infoPanelQueryCoverageCutoff.addStyleName("crk-default-label");
		flexTable.setWidget(0, 1, infoPanelQueryCoverageCutoff);
		
		infoPanelPerMemberCoreSizeXtalCallCutoff = new Label(MainController.CONSTANTS.info_panel_per_member_core_size_xtal_call_cutoff() + ": " + mainController.getPdbScoreItem().getMinMemberCoreSize());
		infoPanelPerMemberCoreSizeXtalCallCutoff.addStyleName("crk-default-label");
		flexTable.setWidget(1, 1, infoPanelPerMemberCoreSizeXtalCallCutoff);
		
		infoPanelMaxNumSequencesUsed = new Label(MainController.CONSTANTS.info_panel_max_num_sequences_used() + ": " + mainController.getPdbScoreItem().getMaxNumSeqsCutoff());
		infoPanelMaxNumSequencesUsed.addStyleName("crk-default-label");
		flexTable.setWidget(2, 1, infoPanelMaxNumSequencesUsed);
		
		infoPanelBioCallCutoff = new Label(MainController.CONSTANTS.info_panel_bio_call_cutoff() + ": " + mainController.getPdbScoreItem().getBioCutoff());
		infoPanelBioCallCutoff.addStyleName("crk-default-label");
		flexTable.setWidget(0, 2, infoPanelBioCallCutoff);
		
		infoPanelXtalCallCutoff = new Label(MainController.CONSTANTS.info_panel_xtal_call_cutoff() + ": " + mainController.getPdbScoreItem().getXtalCutoff());
		infoPanelXtalCallCutoff.addStyleName("crk-default-label");
		flexTable.setWidget(1, 2, infoPanelXtalCallCutoff);
		
		downloadResultsLink = new HTML();
		downloadResultsLink.addStyleName("crk-default-label");
		downloadResultsLink.setHTML("<a href=" + GWT.getModuleBaseURL()
				+ "fileDownload?type=zip&id=" + mainController.getPdbScoreItem().getJobId()
				+ ">" + MainController.CONSTANTS.info_panel_download_results_link() + "</a>");
		flexTable.setWidget(2, 2, downloadResultsLink);
		
		if(homologsStrings != null)
		{
			for(int i=0; i<homologsStrings.size(); i++)
			{
				Label label = new Label(homologsStrings.get(i));
				label.addStyleName("crk-default-label");
				flexTable.setWidget(i, 3, label);
			}
		}

		// formdata -20 - fix for chrome - otherwise unnecessary scroll bar
		this.add(flexTable, new FormData("-20 100%"));
	}
}
