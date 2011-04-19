package ch.systemsx.sybit.crkwebui.client.gui;

import ch.systemsx.sybit.crkwebui.client.controllers.MainController;

import com.extjs.gxt.ui.client.Style.LayoutRegion;
import com.extjs.gxt.ui.client.util.Margins;
import com.extjs.gxt.ui.client.widget.MessageBox;
import com.extjs.gxt.ui.client.widget.Viewport;
import com.extjs.gxt.ui.client.widget.layout.BorderLayout;
import com.extjs.gxt.ui.client.widget.layout.BorderLayoutData;

/**
 * Main view of the application
 * @author srebniak_a
 *
 */
public class MainViewPort extends Viewport 
{
	private MyJobsPanel myJobsPanel;

	private CenterPanel centerPanel;

//	private TopPanel topPanel;
	private BottomPanel bottomPanel;
	
	private InterfacesResiduesWindow interfacesResiduesWindow;
	private MessageBox waitingMessageBox;
	private MessageBox errorMessageBox;

	private MainController mainController;

	public MainViewPort(MainController mainController) 
	{
		this.mainController = mainController;

		BorderLayout layout = new BorderLayout();
		this.setLayout(layout);
		this.setStyleAttribute("padding", "10px");

		BorderLayoutData westData = new BorderLayoutData(LayoutRegion.WEST, 180);
		westData.setCollapsible(true);
		westData.setFloatable(true);
		westData.setSplit(true);
		westData.setMargins(new Margins(0, 5, 0, 0));

		myJobsPanel = new MyJobsPanel(mainController);
		this.add(myJobsPanel, westData);

		BorderLayoutData centerData = new BorderLayoutData(LayoutRegion.CENTER,
				200);
		centerData.setCollapsible(true);
		centerData.setFloatable(true);
		centerData.setSplit(true);
		centerData.setMargins(new Margins(0));

		centerPanel = new CenterPanel(mainController);
		this.add(centerPanel, centerData);

		BorderLayoutData northData = new BorderLayoutData(LayoutRegion.NORTH,
				10);
		northData.setMargins(new Margins(0, 0, 5, 0));

//		topPanel = new TopPanel(mainController);
//		topPanel.getHeader().setVisible(false);
//		this.add(topPanel, northData);
		
		BorderLayoutData southData = new BorderLayoutData(LayoutRegion.SOUTH,
				20);
		southData.setMargins(new Margins(5, 0, 0, 0));
		
		bottomPanel = new BottomPanel();
		this.add(bottomPanel, southData);
	}

	public MyJobsPanel getMyJobsPanel() {
		return myJobsPanel;
	}

	public CenterPanel getCenterPanel() {
		return centerPanel;
	}
	
	public BottomPanel getBottomPanel()
	{
		return bottomPanel;
	}
	
	public InterfacesResiduesWindow getInterfacesResiduesWindow() {
		return interfacesResiduesWindow;
	}

	public void setInterfacesResiduesWindow(
			InterfacesResiduesWindow interfacesResiduesWindow) {
		this.interfacesResiduesWindow = interfacesResiduesWindow;
	}
	
	public void displayInterfacesWindow() 
	{
		if((interfacesResiduesWindow == null) ||
		   (mainController.isResizeInterfacesWindow()))
		{
			interfacesResiduesWindow = new InterfacesResiduesWindow(mainController);
			mainController.setResizeInterfacesWindow(false);
		}
		else
		{
			interfacesResiduesWindow.getInterfacesResiduesPanel().cleanData();
			interfacesResiduesWindow.getInterfacesResiduesPanel().getFirstStructurePanel().cleanResiduesGrid();
			interfacesResiduesWindow.getInterfacesResiduesPanel().getSecondStructurePanel().cleanResiduesGrid();
		}
		
		interfacesResiduesWindow.setVisible(true);
	}
	
	public void showWaiting(String text)
	{
		waitingMessageBox = MessageBox.wait(MainController.CONSTANTS.waiting_message_box_header(),  
											text + ", " + MainController.CONSTANTS.waiting_message_box_info() + "...", 
											text + "...");  
		waitingMessageBox.show();
	}
	
	public void hideWaiting()
	{
		waitingMessageBox.close(); 
	}
	
	public void showError(String message)
	{
		if((errorMessageBox == null) ||
		   (!errorMessageBox.isVisible()))
		{
			errorMessageBox = MessageBox.alert(MainController.CONSTANTS.error_message_box_header(), message, null);
			errorMessageBox.setMinWidth(300);
			errorMessageBox.setMaxWidth(mainController.getWindowWidth());
			errorMessageBox.show();
		}
	}
}
