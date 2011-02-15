package ch.systemsx.sybit.crkwebui.client.gui;

import ch.systemsx.sybit.crkwebui.client.controllers.MainController;

import com.extjs.gxt.ui.client.Style.Orientation;
import com.extjs.gxt.ui.client.util.Margins;
import com.extjs.gxt.ui.client.widget.form.FormPanel;
import com.extjs.gxt.ui.client.widget.layout.FormData;
import com.extjs.gxt.ui.client.widget.layout.RowData;
import com.extjs.gxt.ui.client.widget.layout.RowLayout;

public class InterfacesResiduesPanel extends FormPanel 
{
	private ResiduesPanel firstStructure;
	private ResiduesPanel secondStructure;

	public InterfacesResiduesPanel(MainController mainController)
	{
		// infoPanel.setFrame(true);
		this.getHeader().setVisible(false);
		this.setBodyBorder(false);
		this.setBorders(false);
		this.setLayout(new RowLayout(Orientation.HORIZONTAL));

		firstStructure = new ResiduesPanel(MainController.CONSTANTS.interfaces_residues_panel_first_structure(), mainController);
		secondStructure = new ResiduesPanel(MainController.CONSTANTS.interfaces_residues_panel_second_structure(), mainController);

		this.add(firstStructure, new RowData(0.45, 1, new Margins(0)));

		FormPanel breakPanel = new FormPanel();
		breakPanel.setBodyBorder(false);
		breakPanel.setBorders(false);
		breakPanel.getHeader().setVisible(false);
		this.add(breakPanel, new RowData(0.1, 1, new Margins(0)));

		this.add(secondStructure, new RowData(0.45, 1, new Margins(0)));
	}

	public ResiduesPanel getFirstStructurePanel() {
		return firstStructure;
	}

	public ResiduesPanel getSecondStructurePanel() {
		return secondStructure;
	}
}
