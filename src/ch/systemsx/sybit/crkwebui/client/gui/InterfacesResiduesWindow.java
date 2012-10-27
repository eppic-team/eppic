package ch.systemsx.sybit.crkwebui.client.gui;

import ch.systemsx.sybit.crkwebui.client.controllers.AppPropertiesManager;
import ch.systemsx.sybit.crkwebui.client.controllers.EventBusManager;
import ch.systemsx.sybit.crkwebui.client.data.WindowData;
import ch.systemsx.sybit.crkwebui.client.events.WindowHideEvent;

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

/**
 * Window containing interface residues.
 * @author AS
 *
 */
public class InterfacesResiduesWindow extends ResizableWindow 
{
	private static int INTERFACE_RESIDUES_WINDOW_DEFAULT_WIDTH = 1200;
	private static int INTERFACE_RESIDUES_WINDOW_DEFAULT_HEIGHT = 665;
	
	private InterfacesResiduesPanel interfacesResiduesPanel;
	
	public InterfacesResiduesWindow(WindowData windowData) 
	{
		super(INTERFACE_RESIDUES_WINDOW_DEFAULT_WIDTH,
			  INTERFACE_RESIDUES_WINDOW_DEFAULT_HEIGHT,
			  windowData);
		
		this.setPlain(true);
		this.setModal(false);
		this.setBlinkModal(true);
		this.setLayout(new RowLayout());
		this.setHideOnButtonClick(true);

		// adjust to 22 height rows
		windowHeight = (windowHeight - 322) / 22;
		windowHeight = windowHeight * 22 + 322;
		this.setSize(windowWidth, windowHeight);
		
		interfacesResiduesPanel = new InterfacesResiduesPanel(windowWidth,
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
				EventBusManager.EVENT_BUS.fireEvent(new WindowHideEvent());
			}
		}
		
		);
		
		if(GXT.isIE8)
		{
			this.setResizable(false);
		}
	}

	/**
	 * Retrieves panel containing interface residues.
	 * @return panel containing interface residues
	 */
	public InterfacesResiduesPanel getInterfacesResiduesPanel() {
		return interfacesResiduesPanel;
	}
	
	/**
	 * Sets headers of interface residues window and structures panels.
	 * @param area area
	 * @firstChainName name of first structure
	 * @secondChainName name of second structure
	 * @param selectedInterface chosen interface
	 */
	public void setWindowHeaders(double area,
								 String firstChainName,
								 String secondChainName,
								 int selectedInterface)
	{
//		double area = mainController.getPdbScoreItem().getInterfaceItem(selectedInterface - 1).getArea();
		NumberFormat number = NumberFormat.getFormat("0.00");
		String formattedArea = number.format(area);
		this.setHeading(AppPropertiesManager.CONSTANTS.interfaces_residues_window_title() + " " + selectedInterface + " (" + formattedArea + " A<sup>2</sup>)");
		
		interfacesResiduesPanel.fillHeaders(firstChainName,
											secondChainName);
	}
}
