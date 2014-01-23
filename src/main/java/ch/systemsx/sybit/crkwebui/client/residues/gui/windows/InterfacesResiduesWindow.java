package ch.systemsx.sybit.crkwebui.client.residues.gui.windows;

import ch.systemsx.sybit.crkwebui.client.commons.appdata.AppPropertiesManager;
import ch.systemsx.sybit.crkwebui.client.commons.events.WindowHideEvent;
import ch.systemsx.sybit.crkwebui.client.commons.gui.windows.ResizableWindow;
import ch.systemsx.sybit.crkwebui.client.commons.managers.EventBusManager;
import ch.systemsx.sybit.crkwebui.client.residues.gui.panels.InterfacesResiduesPanel;
import ch.systemsx.sybit.crkwebui.client.residues.gui.panels.LegendPanel;
import ch.systemsx.sybit.crkwebui.shared.model.WindowData;

import com.google.gwt.dom.client.NativeEvent;
import com.google.gwt.event.logical.shared.ResizeEvent;
import com.google.gwt.event.logical.shared.ResizeHandler;
import com.google.gwt.i18n.client.NumberFormat;
import com.sencha.gxt.core.client.GXT;
import com.sencha.gxt.widget.core.client.container.VerticalLayoutContainer;
import com.sencha.gxt.widget.core.client.container.VerticalLayoutContainer.VerticalLayoutData;
import com.sencha.gxt.widget.core.client.event.HideEvent;
import com.sencha.gxt.widget.core.client.event.HideEvent.HideHandler;
import com.sencha.gxt.core.client.util.KeyNav;

/**
 * Window containing interface residues.
 * @author AS
 *
 */
public class InterfacesResiduesWindow extends ResizableWindow 
{
	private static int INTERFACE_RESIDUES_WINDOW_DEFAULT_WIDTH = 1000;
	private static int INTERFACE_RESIDUES_WINDOW_DEFAULT_HEIGHT = 585;
	
	private InterfacesResiduesPanel interfacesResiduesPanel;
	
	public InterfacesResiduesWindow(WindowData windowData) 
	{
		super(INTERFACE_RESIDUES_WINDOW_DEFAULT_WIDTH,
			  INTERFACE_RESIDUES_WINDOW_DEFAULT_HEIGHT,
			  windowData);
		
		this.setBlinkModal(true);
		this.setHideOnButtonClick(true);
		this.getButtonBar().setVisible(false);

		VerticalLayoutContainer mainContainer = new VerticalLayoutContainer();
		this.setWidget(mainContainer);
		mainContainer.addStyleName("eppic-default-font");
		
		interfacesResiduesPanel = new InterfacesResiduesPanel();
		mainContainer.add(interfacesResiduesPanel, new VerticalLayoutData(1, 1));
		
		mainContainer.add(new LegendPanel(), new VerticalLayoutData(1, 30));
		
		this.addResizeHandler(new ResizeHandler() {
			
			@Override
			public void onResize(ResizeEvent event) {
				interfacesResiduesPanel.resizeResiduesPanels();
				
			}
		});
		
		new KeyNav(this)
		{
			@Override
            public void onPageDown(NativeEvent event) 
			{
				interfacesResiduesPanel.increaseActivePages();
            }
			
			@Override
            public void onPageUp(NativeEvent event) 
			{
				interfacesResiduesPanel.decreaseActivePages();
            }
		};
		
		this.addHideHandler(new HideHandler()
		{
			@Override
			public void onHide(HideEvent event) {
				EventBusManager.EVENT_BUS.fireEvent(new WindowHideEvent());	

			}
		});
		
		if(GXT.isIE8())
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
		NumberFormat number = NumberFormat.getFormat("0.00");
		String formattedArea = number.format(area);
		this.setHeadingHtml(AppPropertiesManager.CONSTANTS.interfaces_residues_window_title() + " " + selectedInterface + " (" + formattedArea + " A<sup>2</sup>)");
		
		interfacesResiduesPanel.fillHeaders(firstChainName,
											secondChainName);
	}
}
