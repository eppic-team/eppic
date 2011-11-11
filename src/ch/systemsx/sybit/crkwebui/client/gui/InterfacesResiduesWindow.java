package ch.systemsx.sybit.crkwebui.client.gui;

import ch.systemsx.sybit.crkwebui.client.controllers.MainController;

import com.extjs.gxt.ui.client.event.ComponentEvent;
import com.extjs.gxt.ui.client.event.Events;
import com.extjs.gxt.ui.client.event.Listener;
import com.extjs.gxt.ui.client.event.WindowEvent;
import com.extjs.gxt.ui.client.event.WindowListener;
import com.extjs.gxt.ui.client.util.KeyNav;
import com.extjs.gxt.ui.client.util.Margins;
import com.extjs.gxt.ui.client.widget.Dialog;
import com.extjs.gxt.ui.client.widget.layout.RowData;
import com.extjs.gxt.ui.client.widget.layout.RowLayout;
import com.google.gwt.i18n.client.NumberFormat;

public class InterfacesResiduesWindow extends Dialog 
{
	private InterfacesResiduesPanel interfacesResiduesPanel;
	
	private int selectedInterface;
	
	private MainController mainController;

	public InterfacesResiduesWindow(final MainController mainController,
									int selectedInterface) 
	{
		this.mainController = mainController;
		
		int width = 1200;
		int height = 665;
		
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
		
		this.setPlain(true);
		this.setModal(false);
		this.setBlinkModal(true);
		this.setLayout(new RowLayout());
		this.setHideOnButtonClick(true);
		this.setSelectedInterface(selectedInterface);
		this.setWindowHeader();

		// adjust to 22 height rows
		height = (height - 220) / 22;
		height = height * 22 + 220;
		this.setSize(width, height);
		
		interfacesResiduesPanel = new InterfacesResiduesPanel(mainController,
															  width,
															  height - 220,
															  selectedInterface);
		
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
		
		new KeyNav<ComponentEvent>(this)
		{
			@Override
            public void onPageDown(ComponentEvent ce) 
			{
				interfacesResiduesPanel.getFirstStructurePanel().getResiduesGridPagingToolbar().setActivePage(
						interfacesResiduesPanel.getFirstStructurePanel().getResiduesGridPagingToolbar().getActivePage() + 1);
				interfacesResiduesPanel.getSecondStructurePanel().getResiduesGridPagingToolbar().setActivePage(
						interfacesResiduesPanel.getSecondStructurePanel().getResiduesGridPagingToolbar().getActivePage() + 1);
            }
			
			@Override
            public void onPageUp(ComponentEvent ce) 
			{
				interfacesResiduesPanel.getFirstStructurePanel().getResiduesGridPagingToolbar().setActivePage(
						interfacesResiduesPanel.getFirstStructurePanel().getResiduesGridPagingToolbar().getActivePage() - 1);
				interfacesResiduesPanel.getSecondStructurePanel().getResiduesGridPagingToolbar().setActivePage(
						interfacesResiduesPanel.getSecondStructurePanel().getResiduesGridPagingToolbar().getActivePage() - 1);
            }
		};
		
		this.addWindowListener(new WindowListener(){
			
			@Override
			public void windowHide(WindowEvent we)
			{
				MainViewPort mainViewPort = mainController.getMainViewPort();
				
				if((mainViewPort != null) &&
		           (mainViewPort.getCenterPanel() != null) &&
		           (mainViewPort.getCenterPanel().getDisplayPanel() != null) &&
		           (mainViewPort.getCenterPanel().getDisplayPanel() instanceof ResultsPanel))
		           {
				    	((ResultsPanel)mainViewPort.getCenterPanel().getDisplayPanel()).getResultsGrid().focus();
		           }
			}
		}
		
		);
	}

	public InterfacesResiduesPanel getInterfacesResiduesPanel() {
		return interfacesResiduesPanel;
	}
	
	public void setSelectedInterface(int selectedInterface)
	{
		this.selectedInterface = selectedInterface;
	}
	
	public void setWindowHeader()
	{
		double area = mainController.getPdbScoreItem().getInterfaceItem(selectedInterface - 1).getArea();
		NumberFormat number = NumberFormat.getFormat("0.00");
		String formattedArea = number.format(area);
		this.setHeading(MainController.CONSTANTS.interfaces_residues_window_title() + " " + selectedInterface + " (" + formattedArea + " A<sup>2</sup>)");
	}
	
	public int getSelectedInterface()
	{
		return selectedInterface;
	}
}
