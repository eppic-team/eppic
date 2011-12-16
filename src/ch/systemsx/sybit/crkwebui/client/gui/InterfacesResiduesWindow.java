package ch.systemsx.sybit.crkwebui.client.gui;

import ch.systemsx.sybit.crkwebui.client.controllers.MainController;

import com.extjs.gxt.ui.client.GXT;
import com.extjs.gxt.ui.client.event.ComponentEvent;
import com.extjs.gxt.ui.client.event.Events;
import com.extjs.gxt.ui.client.event.Listener;
import com.extjs.gxt.ui.client.event.WindowEvent;
import com.extjs.gxt.ui.client.event.WindowListener;
import com.extjs.gxt.ui.client.util.KeyNav;
import com.extjs.gxt.ui.client.util.Margins;
import com.extjs.gxt.ui.client.widget.layout.RowData;
import com.extjs.gxt.ui.client.widget.layout.RowLayout;
import com.google.gwt.i18n.client.NumberFormat;

public class InterfacesResiduesWindow extends ResizableWindow 
{
	private static int INTERFACE_RESIDUES_WINDOW_DEFAULT_WIDTH = 1200;
	private static int INTERFACE_RESIDUES_WINDOW_DEFAULT_HEIGHT = 665;
	
	private InterfacesResiduesPanel interfacesResiduesPanel;
	
	private int selectedInterface;
	
	public InterfacesResiduesWindow(final MainController mainController,
									int selectedInterface) 
	{
		super(mainController,
			  INTERFACE_RESIDUES_WINDOW_DEFAULT_WIDTH,
			  INTERFACE_RESIDUES_WINDOW_DEFAULT_HEIGHT);
		
		this.setPlain(true);
		this.setModal(false);
		this.setBlinkModal(true);
		this.setLayout(new RowLayout());
		this.setHideOnButtonClick(true);
		this.setSelectedInterface(selectedInterface);
		this.setWindowHeader();

		// adjust to 22 height rows
		windowHeight = (windowHeight - 322) / 22;
		windowHeight = windowHeight * 22 + 322;
		this.setSize(windowWidth, windowHeight);
		
		interfacesResiduesPanel = new InterfacesResiduesPanel(mainController,
															  windowWidth,
															  windowHeight - 100);
		
		this.add(interfacesResiduesPanel, new RowData(1, 1, new Margins(0)));
		
		this.add(new LegendPanel(), new RowData(1, 30, new Margins(0)));
		
		Listener<WindowEvent> resizeWindowListener = new Listener<WindowEvent>() {

			@Override
			public void handleEvent(WindowEvent be) 
			{
				windowHeight = be.getHeight();
				windowWidth = be.getWidth();
				interfacesResiduesPanel.resizeResiduesPanels(windowWidth, windowHeight);
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
		
		if(GXT.isIE8)
		{
			this.setResizable(false);
		}
	}

	public InterfacesResiduesPanel getInterfacesResiduesPanel() {
		return interfacesResiduesPanel;
	}
	
	public void setSelectedInterface(int selectedInterface)
	{
		this.selectedInterface = selectedInterface;
	}
	
	public int getSelectedInterface()
	{
		return selectedInterface;
	}
	
	public void setWindowHeader()
	{
		double area = mainController.getPdbScoreItem().getInterfaceItem(selectedInterface - 1).getArea();
		NumberFormat number = NumberFormat.getFormat("0.00");
		String formattedArea = number.format(area);
		this.setHeading(MainController.CONSTANTS.interfaces_residues_window_title() + " " + selectedInterface + " (" + formattedArea + " A<sup>2</sup>)");
	}
}
