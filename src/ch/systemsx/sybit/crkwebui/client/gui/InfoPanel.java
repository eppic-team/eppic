package ch.systemsx.sybit.crkwebui.client.gui;

import model.PDBScoreItem;
import ch.systemsx.sybit.crkwebui.client.controllers.MainController;

import com.extjs.gxt.ui.client.Style.Scroll;
import com.extjs.gxt.ui.client.widget.Label;
import com.extjs.gxt.ui.client.widget.form.FieldSet;
import com.extjs.gxt.ui.client.widget.form.FormPanel;
import com.extjs.gxt.ui.client.widget.layout.FitLayout;
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
	
	public InfoPanel(PDBScoreItem resultsData) 
	{
		this.setHeight(60);
		this.getHeader().setVisible(false);
		this.setBodyBorder(false);
		this.setBorders(true);
//		this.setLayout(new FitLayout());
		this.setLayout(new FormLayout());
		
		FlexTable flexTable = new FlexTable();
		
		int nrOfRows = 3;
		int nrOfColumns = 3;
		
		for(int i=0; i < nrOfColumns; i++)
		{
			for(int j=0; j<nrOfRows; j++)
			{
				flexTable.getCellFormatter().setWidth(j, i, "33%");
				flexTable.getCellFormatter().setAlignment(j, i, HasHorizontalAlignment.ALIGN_LEFT, HasVerticalAlignment.ALIGN_TOP);
			}
		}
		
		totalCoreSizeXtalCallCutoff = new Label(MainController.CONSTANTS.info_panel_total_core_size_xtal_call_cutoff() + ": " + resultsData.getMinCoreSize());
		totalCoreSizeXtalCallCutoff.addStyleName("crk-default-label");
		flexTable.setWidget(0, 0, totalCoreSizeXtalCallCutoff);
		
		infoPanelMinNumberHomologsRequired = new Label(MainController.CONSTANTS.info_panel_min_number_homologs_required() + ": " + resultsData.getHomologsCutoff());
		infoPanelMinNumberHomologsRequired.addStyleName("crk-default-label");
		flexTable.setWidget(1, 0, infoPanelMinNumberHomologsRequired);
		
		infoPanelSequenceIdentityCutoff = new Label(MainController.CONSTANTS.info_panel_sequence_identity_cutoff() + ": " + resultsData.getIdCutoff());
		infoPanelSequenceIdentityCutoff.addStyleName("crk-default-label");
		flexTable.setWidget(2, 0, infoPanelSequenceIdentityCutoff);
		
		infoPanelQueryCoverageCutoff = new Label(MainController.CONSTANTS.info_panel_query_coverage_cutoff() + ": " + resultsData.getQueryCovCutoff());
		infoPanelQueryCoverageCutoff.addStyleName("crk-default-label");
		flexTable.setWidget(0, 1, infoPanelQueryCoverageCutoff);
		
		infoPanelPerMemberCoreSizeXtalCallCutoff = new Label(MainController.CONSTANTS.info_panel_per_member_core_size_xtal_call_cutoff() + ": " + resultsData.getMinMemberCoreSize());
		infoPanelPerMemberCoreSizeXtalCallCutoff.addStyleName("crk-default-label");
		flexTable.setWidget(1, 1, infoPanelPerMemberCoreSizeXtalCallCutoff);
		
		infoPanelMaxNumSequencesUsed = new Label(MainController.CONSTANTS.info_panel_max_num_sequences_used() + ": " + resultsData.getMaxNumSeqsCutoff());
		infoPanelMaxNumSequencesUsed.addStyleName("crk-default-label");
		flexTable.setWidget(2, 1, infoPanelMaxNumSequencesUsed);
		
		infoPanelBioCallCutoff = new Label(MainController.CONSTANTS.info_panel_bio_call_cutoff() + ": " + resultsData.getBioCutoff());
		infoPanelBioCallCutoff.addStyleName("crk-default-label");
		flexTable.setWidget(0, 2, infoPanelBioCallCutoff);
		
		infoPanelXtalCallCutoff = new Label(MainController.CONSTANTS.info_panel_xtal_call_cutoff() + ": " + resultsData.getXtalCutoff());
		infoPanelXtalCallCutoff.addStyleName("crk-default-label");
		flexTable.setWidget(1, 2, infoPanelXtalCallCutoff);
		
		downloadResultsLink = new HTML();
		downloadResultsLink.addStyleName("crk-default-label");
		downloadResultsLink.setHTML("<a href=" + GWT.getModuleBaseURL()
				+ "fileDownload?type=zip&id=" + resultsData.getJobId()
				+ ">" + MainController.CONSTANTS.info_panel_download_results_link() + "</a>");
		flexTable.setWidget(2, 2, downloadResultsLink);

		// formdata -20 - fix for chrome - otherwise unnecessary scroll bar
//		fieldSet.add(flexTable, new FormData("-20 100%"));
//		this.add(fieldSet);
		
		this.add(flexTable, new FormData("-20 100%"));
		
//		this.setScrollMode(Scroll.ALWAYS);
//
//		VBoxLayout vBoxLayout = new VBoxLayout();
//		vBoxLayout.setPadding(new Padding(5));
//		vBoxLayout.setVBoxLayoutAlign(VBoxLayoutAlign.LEFT);
////		VBoxLayoutData vBoxLayoutData = new VBoxLayoutData(new Margins(0));
////		vBoxLayoutData.setFlex(1);
//
//		LayoutContainer left = new LayoutContainer();
//		left.setHeight(65);
//		left.setStyleAttribute("paddingRight", "10px");
//		left.setLayout(vBoxLayout);
////		left.setScrollMode(Scroll.AUTO);
//
//		pdbIdentifier = new HTML();
//		pdbIdentifier.setHTML(MainController.CONSTANTS.info_panel_pdb_identifier() + ": <b>" + resultsData.getPdbName() + "</b>");
//		left.add(pdbIdentifier);
//		
//		totalCoreSizeXtalCallCutoff = new Label(MainController.CONSTANTS.info_panel_total_core_size_xtal_call_cutoff() + ": " + resultsData.getMinCoreSize());
//		left.add(totalCoreSizeXtalCallCutoff);
//		
//		infoPanelMinNumberHomologsRequired = new Label(MainController.CONSTANTS.info_panel_min_number_homologs_required() + ": " + resultsData.getHomologsCutoff());
//		left.add(infoPanelMinNumberHomologsRequired);
//
//		vBoxLayout = new VBoxLayout();
//		vBoxLayout.setPadding(new Padding(5));
//		vBoxLayout.setVBoxLayoutAlign(VBoxLayoutAlign.LEFT);
////		vBoxLayoutData = new VBoxLayoutData(new Margins(0));
////		vBoxLayoutData.setFlex(1);
//
//		LayoutContainer center = new LayoutContainer();
//		center.setStyleAttribute("paddingRight", "10px");
//		center.setStyleAttribute("paddingLeft", "10px");
//		center.setHeight(65);
//		center.setLayout(vBoxLayout);
////		center.setScrollMode(Scroll.AUTO);
//
//		infoPanelSequenceIdentityCutoff = new Label(MainController.CONSTANTS.info_panel_sequence_identity_cutoff() + ": " + resultsData.getIdCutoff());
//		center.add(infoPanelSequenceIdentityCutoff);
//		
//		infoPanelQueryCoverageCutoff = new Label(MainController.CONSTANTS.info_panel_query_coverage_cutoff() + ": " + resultsData.getQueryCovCutoff());
//		center.add(infoPanelQueryCoverageCutoff);
//
//		infoPanelPerMemberCoreSizeXtalCallCutoff = new Label(MainController.CONSTANTS.info_panel_per_member_core_size_xtal_call_cutoff() + ": " + resultsData.getMinMemberCoreSize());
//		center.add(infoPanelPerMemberCoreSizeXtalCallCutoff);
//		
//		vBoxLayout = new VBoxLayout();
//		vBoxLayout.setPadding(new Padding(5));
//		vBoxLayout.setVBoxLayoutAlign(VBoxLayoutAlign.LEFT);
////		vBoxLayoutData = new VBoxLayoutData(new Margins(0));
////		vBoxLayoutData.setFlex(1);
//
//		LayoutContainer right = new LayoutContainer();
//		right.setStyleAttribute("paddingLeft", "10px");
//		right.setHeight(80);
//		right.setLayout(vBoxLayout);
////		right.setScrollMode(Scroll.AUTO);
//		
//		infoPanelMaxNumSequencesUsed = new Label(MainController.CONSTANTS.info_panel_max_num_sequences_used() + ": " + resultsData.getMaxNumSeqsCutoff());
//		right.add(infoPanelMaxNumSequencesUsed);
//
//		infoPanelBioCallCutoff = new Label(MainController.CONSTANTS.info_panel_bio_call_cutoff() + ": " + resultsData.getBioCutoff());
//		right.add(infoPanelBioCallCutoff);
//
//		infoPanelXtalCallCutoff = new Label(MainController.CONSTANTS.info_panel_xtal_call_cutoff() + ": " + resultsData.getXtalCutoff());
//		right.add(infoPanelXtalCallCutoff);
//		
//		downloadResultsLink = new HTML();
//		downloadResultsLink.setHTML("<a href=" + GWT.getModuleBaseURL()
//				+ "fileDownload?type=zip&id=" + resultsData.getJobId()
//				+ ">" + MainController.CONSTANTS.info_panel_download_results_link() + "</a>");
//		right.add(downloadResultsLink);
//		
//		this.add(left, new RowData(0.33, 1, new Margins(0)));
//		this.add(center, new RowData(0.33, 1, new Margins(0)));
//		this.add(right, new RowData(0.33, 1, new Margins(0)));
//
////		this.add(main);
	}

	public void fillInfoPanel(PDBScoreItem resultsData) 
	{
		totalCoreSizeXtalCallCutoff.setText(MainController.CONSTANTS.info_panel_total_core_size_xtal_call_cutoff() + ": " + resultsData.getMinCoreSize());
		infoPanelMinNumberHomologsRequired.setText(MainController.CONSTANTS.info_panel_min_number_homologs_required() + ": " + resultsData.getHomologsCutoff());
		infoPanelSequenceIdentityCutoff.setText(MainController.CONSTANTS.info_panel_sequence_identity_cutoff() + ": " + resultsData.getIdCutoff());
		infoPanelQueryCoverageCutoff.setText(MainController.CONSTANTS.info_panel_query_coverage_cutoff() + ": " + resultsData.getQueryCovCutoff());
		infoPanelPerMemberCoreSizeXtalCallCutoff.setText(MainController.CONSTANTS.info_panel_per_member_core_size_xtal_call_cutoff() + ": " + resultsData.getMinMemberCoreSize());
		infoPanelMaxNumSequencesUsed.setText(MainController.CONSTANTS.info_panel_max_num_sequences_used() + ": " + resultsData.getMaxNumSeqsCutoff());
		infoPanelBioCallCutoff.setText(MainController.CONSTANTS.info_panel_bio_call_cutoff() + ": " + resultsData.getBioCutoff());
		infoPanelXtalCallCutoff.setText(MainController.CONSTANTS.info_panel_xtal_call_cutoff() + ": " + resultsData.getXtalCutoff());
		downloadResultsLink.setHTML("<a href=" + GWT.getModuleBaseURL()
				+ "fileDownload?type=zip&id=" + resultsData.getJobId()
				+ ">" + MainController.CONSTANTS.info_panel_download_results_link() + "</a>");
	}
}
