package ch.systemsx.sybit.crkwebui.client.gui;

import ch.systemsx.sybit.crkwebui.client.controllers.MainController;

import com.extjs.gxt.ui.client.widget.Dialog;
import com.extjs.gxt.ui.client.widget.layout.FitLayout;

public class InterfacesWindow extends Dialog {
	private InterfacesPanel interfacesPanel;

	public InterfacesWindow(MainController mainController) {
		this.setSize(1400, 800);
		this.setPlain(true);
		this.setModal(true);
		this.setBlinkModal(true);
		this.setHeading("Interfaces");
		this.setLayout(new FitLayout());
		this.setHideOnButtonClick(true);

		interfacesPanel = new InterfacesPanel(mainController);
		this.add(interfacesPanel);
	}

	public InterfacesPanel getInterfacesPanel() {
		return interfacesPanel;
	}
}
