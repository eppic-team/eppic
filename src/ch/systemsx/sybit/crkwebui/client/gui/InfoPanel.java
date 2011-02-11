package ch.systemsx.sybit.crkwebui.client.gui;

import model.PDBScoreItem;

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

public class InfoPanel extends FormPanel {
	public InfoPanel(PDBScoreItem resultsData) {
		FormData formData = new FormData("100%");
		// infoPanel.setFrame(true);
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
		left.setHeight(80);
		left.setStyleAttribute("paddingRight", "10px");

		left.setLayout(vBoxLayout);

		Label label = new Label("PDB identifier: " + resultsData.getPdbName());
		left.add(label, vBoxLayoutData);

		label = new Label("Score method:");
		left.add(label, vBoxLayoutData);

		label = new Label("Score type: ");
		left.add(label, vBoxLayoutData);

		label = new Label("Total core size xtal-call cutoff: ");
		left.add(label, vBoxLayoutData);

		vBoxLayout = new VBoxLayout();
		vBoxLayout.setPadding(new Padding(5));
		vBoxLayout.setVBoxLayoutAlign(VBoxLayoutAlign.LEFT);
		vBoxLayoutData = new VBoxLayoutData(new Margins(0));
		vBoxLayoutData.setFlex(1);

		LayoutContainer center = new LayoutContainer();
		center.setStyleAttribute("paddingRight", "10px");
		center.setStyleAttribute("paddingLeft", "10px");
		center.setHeight(80);
		center.setLayout(vBoxLayout);

		label = new Label("Min number homologs required: ");
		center.add(label, vBoxLayoutData);

		label = new Label("Sequence identity cutoff:");
		center.add(label, vBoxLayoutData);

		label = new Label("Query coverage cutoff: ");
		center.add(label, vBoxLayoutData);

		label = new Label("Per-member core size xtal-call cutoff: ");
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

		label = new Label("Max num sequences used: ");
		right.add(label, vBoxLayoutData);

		label = new Label("Bio-call cutoff: " + resultsData.getBioCutoff());
		right.add(label, vBoxLayoutData);

		HTML html = new HTML();
		html.setHTML("Xtal-call cutoff: <b>" + resultsData.getXtalCutoff()
				+ "</b>");
		right.add(html, vBoxLayoutData);

		HTML downloadResultsLink = new HTML();
		downloadResultsLink.setHTML("<a href=" + GWT.getModuleBaseURL()
				+ "fileDownload?id=" + resultsData.getJobId()
				+ ">Download results</a>");
		right.add(downloadResultsLink);

		main.add(left, new ColumnData(.33));
		main.add(center, new ColumnData(.33));
		main.add(right, new ColumnData(.33));

		this.add(main, new FormData("100%"));
	}
}
