package ch.systemsx.sybit.crkwebui.client.gui;

import model.PDBScoreItem;
import ch.systemsx.sybit.crkwebui.client.controllers.MainController;

import com.extjs.gxt.ui.client.widget.TabPanel;

public class ResultsPanel extends TabPanel {
	private MainController mainController;

	private OverViewTabItem overViewTabItem;

	public ResultsPanel(MainController mainController, PDBScoreItem resultsData) {
		this.mainController = mainController;
		this.setBorders(false);

		overViewTabItem = new OverViewTabItem(mainController, resultsData);
		this.add(overViewTabItem);
	}

	public OverViewTabItem getOverViewTabItem() {
		return overViewTabItem;
	}
}
