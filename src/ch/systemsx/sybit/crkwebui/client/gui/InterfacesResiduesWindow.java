package ch.systemsx.sybit.crkwebui.client.gui;

import ch.systemsx.sybit.crkwebui.client.controllers.MainController;

import com.extjs.gxt.ui.client.event.Events;
import com.extjs.gxt.ui.client.event.Listener;
import com.extjs.gxt.ui.client.event.WindowEvent;
import com.extjs.gxt.ui.client.util.Margins;
import com.extjs.gxt.ui.client.widget.Dialog;
import com.extjs.gxt.ui.client.widget.layout.RowData;
import com.extjs.gxt.ui.client.widget.layout.RowLayout;

public class InterfacesResiduesWindow extends Dialog 
{
	private InterfacesResiduesPanel interfacesResiduesPanel;

	public InterfacesResiduesWindow(final MainController mainController) 
	{
		int width = 1200;
		int height = 660;
		
		if(width > mainController.getWindowWidth())
		{
			width = mainController.getWindowWidth();
		}
		
		if(height > mainController.getWindowHeight() - 50)
		{
			height = mainController.getWindowHeight() - 50;
			
			if(height <= 0)
			{
				height = 1;
			}
		}
		
		this.setSize(width, height);
		this.setPlain(true);
		this.setModal(true);
		this.setBlinkModal(true);
		this.setHeading(MainController.CONSTANTS.interfaces_residues_window());
		this.setLayout(new RowLayout());
		this.setHideOnButtonClick(true);

		// adjust to 22 height rows
		height = (height - 220) / 22;
		height = height * 22 + 220;
		
		interfacesResiduesPanel = new InterfacesResiduesPanel(mainController,
															  width,
															  height - 220);
		this.add(interfacesResiduesPanel, new RowData(1, 1, new Margins(0)));
		
		this.add(new LegendPanel(), new RowData(1, 30, new Margins(0)));
		
		Listener<WindowEvent> resizeWindowListener = new Listener<WindowEvent>() {

			@Override
			public void handleEvent(WindowEvent be) 
			{
				interfacesResiduesPanel.resizeResiduesPanels();
			}
		};
		
		this.addListener(Events.Resize, resizeWindowListener);
		
	}

	public InterfacesResiduesPanel getInterfacesResiduesPanel() {
		return interfacesResiduesPanel;
	}
}
