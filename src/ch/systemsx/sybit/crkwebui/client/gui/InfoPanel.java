package ch.systemsx.sybit.crkwebui.client.gui;

import model.PDBScoreItem;
import ch.systemsx.sybit.crkwebui.client.controllers.MainController;

import com.extjs.gxt.ui.client.Style.Scroll;
import com.extjs.gxt.ui.client.util.Margins;
import com.extjs.gxt.ui.client.util.Padding;
import com.extjs.gxt.ui.client.widget.Label;
import com.extjs.gxt.ui.client.widget.LayoutContainer;
import com.extjs.gxt.ui.client.widget.form.FormPanel;
import com.extjs.gxt.ui.client.widget.layout.ColumnData;
import com.extjs.gxt.ui.client.widget.layout.ColumnLayout;
import com.extjs.gxt.ui.client.widget.layout.FormData;
import com.extjs.gxt.ui.client.widget.layout.VBoxLayout;
import com.extjs.gxt.ui.client.widget.layout.VBoxLayout.VBoxLayoutAlign;
import com.extjs.gxt.ui.client.widget.layout.VBoxLayoutData;
import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.ui.HTML;

public class InfoPanel extends FormPanel 
{
	private HTML pdbIdentifier;
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
		this.getHeader().setVisible(false);
		this.setBodyBorder(false);
		this.setBorders(false);
		this.setLayout(new ColumnLayout());

		VBoxLayout vBoxLayout = new VBoxLayout();
		vBoxLayout.setPadding(new Padding(5));
		vBoxLayout.setVBoxLayoutAlign(VBoxLayoutAlign.LEFT);
//		VBoxLayoutData vBoxLayoutData = new VBoxLayoutData(new Margins(0));
//		vBoxLayoutData.setFlex(1);

		LayoutContainer left = new LayoutContainer();
		left.setHeight(65);
		left.setStyleAttribute("paddingRight", "10px");
		left.setLayout(vBoxLayout);
		left.setScrollMode(Scroll.AUTO);

		pdbIdentifier = new HTML();
		pdbIdentifier.setHTML(MainController.CONSTANTS.info_panel_pdb_identifier() + ": <b>" + resultsData.getPdbName() + "</b>");
		left.add(pdbIdentifier);
		
		totalCoreSizeXtalCallCutoff = new Label(MainController.CONSTANTS.info_panel_total_core_size_xtal_call_cutoff() + ": " + resultsData.getMinCoreSize());
		left.add(totalCoreSizeXtalCallCutoff);
		
		infoPanelMinNumberHomologsRequired = new Label(MainController.CONSTANTS.info_panel_min_number_homologs_required() + ": " + resultsData.getHomologsCutoff());
		left.add(infoPanelMinNumberHomologsRequired);

		vBoxLayout = new VBoxLayout();
		vBoxLayout.setPadding(new Padding(5));
		vBoxLayout.setVBoxLayoutAlign(VBoxLayoutAlign.LEFT);
//		vBoxLayoutData = new VBoxLayoutData(new Margins(0));
//		vBoxLayoutData.setFlex(1);

		LayoutContainer center = new LayoutContainer();
		center.setStyleAttribute("paddingRight", "10px");
		center.setStyleAttribute("paddingLeft", "10px");
		center.setHeight(65);
		center.setLayout(vBoxLayout);
		center.setScrollMode(Scroll.AUTO);

		infoPanelSequenceIdentityCutoff = new Label(MainController.CONSTANTS.info_panel_sequence_identity_cutoff() + ": " + resultsData.getIdCutoff());
		center.add(infoPanelSequenceIdentityCutoff);
		
		infoPanelQueryCoverageCutoff = new Label(MainController.CONSTANTS.info_panel_query_coverage_cutoff() + ": " + resultsData.getQueryCovCutoff());
		center.add(infoPanelQueryCoverageCutoff);

		infoPanelPerMemberCoreSizeXtalCallCutoff = new Label(MainController.CONSTANTS.info_panel_per_member_core_size_xtal_call_cutoff() + ": " + resultsData.getMinMemberCoreSize());
		center.add(infoPanelPerMemberCoreSizeXtalCallCutoff);
		
		vBoxLayout = new VBoxLayout();
		vBoxLayout.setPadding(new Padding(5));
		vBoxLayout.setVBoxLayoutAlign(VBoxLayoutAlign.LEFT);
//		vBoxLayoutData = new VBoxLayoutData(new Margins(0));
//		vBoxLayoutData.setFlex(1);

		LayoutContainer right = new LayoutContainer();
		right.setStyleAttribute("paddingLeft", "10px");
		right.setHeight(80);
		right.setLayout(vBoxLayout);
		right.setScrollMode(Scroll.AUTO);
		
		infoPanelMaxNumSequencesUsed = new Label(MainController.CONSTANTS.info_panel_max_num_sequences_used() + ": " + resultsData.getMaxNumSeqsCutoff());
		right.add(infoPanelMaxNumSequencesUsed);

		infoPanelBioCallCutoff = new Label(MainController.CONSTANTS.info_panel_bio_call_cutoff() + ": " + resultsData.getBioCutoff());
		right.add(infoPanelBioCallCutoff);

		infoPanelXtalCallCutoff = new Label(MainController.CONSTANTS.info_panel_xtal_call_cutoff() + ": " + resultsData.getXtalCutoff());
		right.add(infoPanelXtalCallCutoff);
		
		downloadResultsLink = new HTML();
		downloadResultsLink.setHTML("<a href=" + GWT.getModuleBaseURL()
				+ "fileDownload?type=zip&id=" + resultsData.getJobId()
				+ ">" + MainController.CONSTANTS.info_panel_download_results_link() + "</a>");
		right.add(downloadResultsLink);
		
		this.add(left, new ColumnData(.33));
		this.add(center, new ColumnData(.33));
		this.add(right, new ColumnData(.33));

//		this.add(main);
	}

	public void fillInfoPanel(PDBScoreItem resultsData) 
	{
		pdbIdentifier.setHTML(MainController.CONSTANTS.info_panel_pdb_identifier() + ": <b>" + resultsData.getPdbName() + "</b>");
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
