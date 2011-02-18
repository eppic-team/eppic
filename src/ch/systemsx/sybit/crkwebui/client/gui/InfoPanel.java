package ch.systemsx.sybit.crkwebui.client.gui;

import model.PDBScoreItem;
import ch.systemsx.sybit.crkwebui.client.controllers.MainController;

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
	public InfoPanel(PDBScoreItem resultsData) 
	{
		this.getHeader().setVisible(false);
		this.setBodyBorder(false);
		this.setBorders(false);

		LayoutContainer main = new LayoutContainer();
		main.setLayout(new ColumnLayout());
		main.setBorders(false);

		VBoxLayout vBoxLayout = new VBoxLayout();
		vBoxLayout.setPadding(new Padding(5));
		vBoxLayout.setVBoxLayoutAlign(VBoxLayoutAlign.LEFT);
		VBoxLayoutData vBoxLayoutData = new VBoxLayoutData(new Margins(0));
		vBoxLayoutData.setFlex(1);

		LayoutContainer left = new LayoutContainer();
		left.setHeight(65);
		left.setStyleAttribute("paddingRight", "10px");

		left.setLayout(vBoxLayout);

		HTML html = new HTML();
		html.setHTML(MainController.CONSTANTS.info_panel_pdb_identifier() + ": <b>" + resultsData.getPdbName() + "</b>");
		left.add(html, vBoxLayoutData);
		
		Label label = new Label(MainController.CONSTANTS.info_panel_total_core_size_xtal_call_cutoff() + ": " + resultsData.getMinCoreSize());
		left.add(label, vBoxLayoutData);
		
		label = new Label(MainController.CONSTANTS.info_panel_min_number_homologs_required() + ": " + resultsData.getHomologsCutoff());
		left.add(label, vBoxLayoutData);

		vBoxLayout = new VBoxLayout();
		vBoxLayout.setPadding(new Padding(5));
		vBoxLayout.setVBoxLayoutAlign(VBoxLayoutAlign.LEFT);
		vBoxLayoutData = new VBoxLayoutData(new Margins(0));
		vBoxLayoutData.setFlex(1);

		LayoutContainer center = new LayoutContainer();
		center.setStyleAttribute("paddingRight", "10px");
		center.setStyleAttribute("paddingLeft", "10px");
		center.setHeight(65);
		center.setLayout(vBoxLayout);

		label = new Label(MainController.CONSTANTS.info_panel_sequence_identity_cutoff() + ": " + resultsData.getIdCutoff());
		center.add(label, vBoxLayoutData);
		
		label = new Label(MainController.CONSTANTS.info_panel_query_coverage_cutoff() + ": " + resultsData.getQueryCovCutoff());
		center.add(label, vBoxLayoutData);

		label = new Label(MainController.CONSTANTS.info_panel_per_member_core_size_xtal_call_cutoff() + ": " + resultsData.getMinMemberCoreSize());
		center.add(label, vBoxLayoutData);
		
		vBoxLayout = new VBoxLayout();
		vBoxLayout.setPadding(new Padding(5));
		vBoxLayout.setVBoxLayoutAlign(VBoxLayoutAlign.LEFT);
		vBoxLayoutData = new VBoxLayoutData(new Margins(0));
		vBoxLayoutData.setFlex(1);

		LayoutContainer right = new LayoutContainer();
		right.setStyleAttribute("paddingLeft", "10px");
		right.setHeight(80);
		right.setLayout(vBoxLayout);
		
		label = new Label(MainController.CONSTANTS.info_panel_max_num_sequences_used() + ": " + resultsData.getMaxNumSeqsCutoff());
		right.add(label, vBoxLayoutData);

		label = new Label(MainController.CONSTANTS.info_panel_bio_call_cutoff() + ": " + resultsData.getBioCutoff());
		right.add(label, vBoxLayoutData);

		label = new Label(MainController.CONSTANTS.info_panel_xtal_call_cutoff() + ": " + resultsData.getXtalCutoff());
		right.add(label, vBoxLayoutData);
		
		HTML downloadResultsLink = new HTML();
		downloadResultsLink.setHTML("<a href=" + GWT.getModuleBaseURL()
				+ "fileDownload?type=zip&id=" + resultsData.getJobId()
				+ ">" + MainController.CONSTANTS.info_panel_download_results_link() + "</a>");
		right.add(downloadResultsLink);
		
		main.add(left, new ColumnData(.33));
		main.add(center, new ColumnData(.33));
		main.add(right, new ColumnData(.33));

		this.add(main, new FormData("100%"));
	}
}
