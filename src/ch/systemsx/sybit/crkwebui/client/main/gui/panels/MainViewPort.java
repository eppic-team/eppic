package ch.systemsx.sybit.crkwebui.client.main.gui.panels;

import java.util.HashMap;
import java.util.List;

import ch.systemsx.sybit.crkwebui.client.about.gui.windows.AboutWindow;
import ch.systemsx.sybit.crkwebui.client.alignment.gui.windows.AlignmentsWindow;
import ch.systemsx.sybit.crkwebui.client.commons.appdata.AppPropertiesManager;
import ch.systemsx.sybit.crkwebui.client.commons.appdata.ApplicationContext;
import ch.systemsx.sybit.crkwebui.client.commons.events.ApplicationWindowResizeEvent;
import ch.systemsx.sybit.crkwebui.client.commons.events.UnmaskMainViewEvent;
import ch.systemsx.sybit.crkwebui.client.commons.handlers.ApplicationWindowResizeHandler;
import ch.systemsx.sybit.crkwebui.client.commons.handlers.UnmaskMainViewHandler;
import ch.systemsx.sybit.crkwebui.client.commons.managers.EventBusManager;
import ch.systemsx.sybit.crkwebui.client.commons.util.EscapedStringGenerator;
import ch.systemsx.sybit.crkwebui.client.input.gui.panels.InputDataPanel;
import ch.systemsx.sybit.crkwebui.client.jobs.gui.panels.MyJobsPanel;
import ch.systemsx.sybit.crkwebui.client.main.controllers.MainController;
import ch.systemsx.sybit.crkwebui.client.residues.gui.windows.InterfacesResiduesWindow;
import ch.systemsx.sybit.crkwebui.client.results.gui.panels.ResultsPanel;
import ch.systemsx.sybit.crkwebui.client.results.gui.panels.StatusPanel;
import ch.systemsx.sybit.crkwebui.shared.model.HomologsInfoItem;
import ch.systemsx.sybit.crkwebui.shared.model.InterfaceItem;
import ch.systemsx.sybit.crkwebui.shared.model.InterfaceResidueItem;
import ch.systemsx.sybit.crkwebui.shared.model.PDBScoreItem;

import com.extjs.gxt.ui.client.Style.LayoutRegion;
import com.extjs.gxt.ui.client.event.BorderLayoutEvent;
import com.extjs.gxt.ui.client.event.Events;
import com.extjs.gxt.ui.client.event.Listener;
import com.extjs.gxt.ui.client.util.Margins;
import com.extjs.gxt.ui.client.widget.LayoutContainer;
import com.extjs.gxt.ui.client.widget.MessageBox;
import com.extjs.gxt.ui.client.widget.layout.BorderLayout;
import com.extjs.gxt.ui.client.widget.layout.BorderLayoutData;
import com.google.gwt.core.client.Scheduler;
import com.google.gwt.core.client.Scheduler.ScheduledCommand;

/**
 * Main view of the application.
 * @author srebniak_a
 *
 */
public class MainViewPort extends LayoutContainer
{
	private MyJobsPanel myJobsPanel;

	private CenterPanel centerPanel;

//	private TopPanel topPanel;
	private BottomPanel bottomPanel;

	private InterfacesResiduesWindow interfacesResiduesWindow;
	private AlignmentsWindow alignmentsWindow;
	private AboutWindow aboutWindow;

	private MessageBox waitingMessageBox;
	private MessageBox errorMessageBox;
	
	private ResultsPanel resultsPanel;
	private StatusPanel statusPanel;
	private InputDataPanel inputDataPanel;
	
	public MainViewPort(final MainController mainController)
	{
		BorderLayout layout = new BorderLayout();
		this.setLayout(layout);
		this.addStyleName("eppic-default-padding");
		
		layout.addListener(Events.Collapse, new Listener<BorderLayoutEvent>()
		{
			public void handleEvent(BorderLayoutEvent be)
			{
				if(be.getPanel() instanceof MyJobsPanel)
				{
					EventBusManager.EVENT_BUS.fireEvent(new ApplicationWindowResizeEvent());
				}
			}
		});

		layout.addListener(Events.Expand, new Listener<BorderLayoutEvent>()
		{
			public void handleEvent(BorderLayoutEvent be)
			{
				if(be.getPanel() instanceof MyJobsPanel)
				{
					EventBusManager.EVENT_BUS.fireEvent(new ApplicationWindowResizeEvent());
				}
			}
		});

		BorderLayoutData westData = new BorderLayoutData(LayoutRegion.WEST, 220);
		westData.setCollapsible(true);
		westData.setFloatable(true);
		westData.setSplit(true);
		westData.setMargins(new Margins(0, 5, 0, 0));

		myJobsPanel = new MyJobsPanel();
		this.add(myJobsPanel, westData);

		BorderLayoutData centerData = new BorderLayoutData(LayoutRegion.CENTER,
				200);
		centerData.setCollapsible(true);
		centerData.setFloatable(true);
		centerData.setSplit(true);
		centerData.setMargins(new Margins(0));

		centerPanel = new CenterPanel();
		this.add(centerPanel, centerData);

		BorderLayoutData northData = new BorderLayoutData(LayoutRegion.NORTH,
				10);
		northData.setMargins(new Margins(0, 0, 10, 0));

//		topPanel = new TopPanel(mainController);
//		this.add(topPanel, northData);

		BorderLayoutData southData = new BorderLayoutData(LayoutRegion.SOUTH,
				20);
		southData.setMargins(new Margins(5, 0, 0, 0));

		bottomPanel = new BottomPanel();
//		navigationPanel.add(new ThemeSelector());
		this.add(bottomPanel, southData);
		
		initializeEventsListeners();
	}

	/**
	 * Retrieves panel containing list of jobs for the session.
	 * @return panel containing list of jobs for the session
	 */
	public MyJobsPanel getMyJobsPanel() {
		return myJobsPanel;
	}

	/**
	 * Retrieves main central panel.
	 * @return main central panel
	 */
	public CenterPanel getCenterPanel() {
		return centerPanel;
	}

	/**
	 * Retrieves bottom panel.
	 * @return bottom panel
	 */
	public BottomPanel getBottomPanel()
	{
		return bottomPanel;
	}

	/**
	 * Displays window containing interface residues.
	 * @param selectedInterface selected interface identifier
	 */
	public void displayInterfacesWindow(int selectedInterface)
	{
		ApplicationContext.setSelectedInterface(selectedInterface);
		
		if((interfacesResiduesWindow == null) ||
		   (interfacesResiduesWindow.isResizeWindow()))
		{
			interfacesResiduesWindow = new InterfacesResiduesWindow(ApplicationContext.getWindowData());
			interfacesResiduesWindow.setResizeWindow(false);
			interfacesResiduesWindow.setVisible(true);
			Scheduler.get().scheduleDeferred(new ScheduledCommand() {
				@Override
				public void execute() {
					interfacesResiduesWindow.getInterfacesResiduesPanel().resizeResiduesPanels();
				}
			});
		}
		else
		{
			interfacesResiduesWindow.getInterfacesResiduesPanel().cleanData();
			interfacesResiduesWindow.setVisible(true);
		}
		
		InterfaceItem interfaceItem = ApplicationContext.getPdbScoreItem().getInterfaceItem(selectedInterface - 1);
		interfacesResiduesWindow.setWindowHeaders(interfaceItem.getArea(),
												  interfaceItem.getChain1(),
												  interfaceItem.getChain2(),
												  selectedInterface);

		//called beacuse of the bug in GXT 2.2.3
		// http://www.sencha.com/forum/showthread.php?126888-Problems-with-RowLayout
		interfacesResiduesWindow.layout(true);
	}

	/**
	 * Hides window containing interface residues.
	 */
	public void hideInterfacesWindow()
	{
		if(interfacesResiduesWindow != null)
		{
			interfacesResiduesWindow.setVisible(false);
		}
	}
	
	/**
	 * Fills content of interface residues window.
	 * @param interfaceResidues interface residues
	 */
	public void fillInterfacesWindow(HashMap<Integer, List<InterfaceResidueItem>> interfaceResidues,
									 PDBScoreItem pdbScoreItem,
									 int selectedInterface)
	{
		if(interfaceResidues.containsKey(1))
		{
			interfacesResiduesWindow.getInterfacesResiduesPanel().fillStructurePanel(1, 
																					 pdbScoreItem, 
																					 selectedInterface, 
																					 interfaceResidues.get(1));
		}

		if(interfaceResidues.containsKey(2))
		{
			interfacesResiduesWindow.getInterfacesResiduesPanel().fillStructurePanel(2, 
																					 pdbScoreItem, 
																					 selectedInterface, 
																					 interfaceResidues.get(2));
		}
	}

	/**
	 * Shows waiting messagebox with provided text.
	 * @param text information displayed in messagebox
	 */
	public void displayWaiting(final String text)
	{
//		Scheduler.get().scheduleDeferred(new ScheduledCommand() {
//			@Override
//			public void execute() {
				waitingMessageBox = MessageBox.wait(AppPropertiesManager.CONSTANTS.waiting_message_box_header(),
						EscapedStringGenerator.generateEscapedString(text) + ", " + AppPropertiesManager.CONSTANTS.waiting_message_box_info() + "...",
						EscapedStringGenerator.generateEscapedString(text) + "...");
				
				waitingMessageBox.getDialog().setResizable(true);
				if(waitingMessageBox.getDialog().getInitialWidth() > ApplicationContext.getWindowData().getWindowWidth() - 20)
				{
					waitingMessageBox.getDialog().setWidth(ApplicationContext.getWindowData().getWindowWidth() - 20);
				}
				
				
				waitingMessageBox.show();
//			}
//		});
	}

	/**
	 * Hides waiting messagebox.
	 */
	public void hideWaiting()
	{
		if(waitingMessageBox != null)
		{
			waitingMessageBox.close();
		}
	}

	/**
	 * Shows error messagebox with provided error message.
	 * @param message error message to display
	 */
	public void displayError(String message)
	{
		if((errorMessageBox == null) ||
		   (!errorMessageBox.isVisible()))
		{
			errorMessageBox = MessageBox.alert(AppPropertiesManager.CONSTANTS.error_message_box_header(),
											   EscapedStringGenerator.generateEscapedString(message), 
											   null);

			errorMessageBox.getDialog().setResizable(true);
			if(errorMessageBox.getDialog().getInitialWidth() > ApplicationContext.getWindowData().getWindowWidth() - 20)
			{
				errorMessageBox.getDialog().setWidth(ApplicationContext.getWindowData().getWindowWidth() - 20);
			}
			
			errorMessageBox.show();
		}
	}

	/**
	 * Displays alignments window.
	 * @param homologsInfoItem homologs info item
	 * @param pdbName pdb name
	 * @param xPosition left corner
	 * @param yPosition top corner
	 */
	public void displayAlignmentsWindow(HomologsInfoItem homologsInfoItem,
										String pdbName,
										int xPosition,
										int yPosition)
	{
		alignmentsWindow = new AlignmentsWindow(ApplicationContext.getWindowData(), homologsInfoItem, pdbName);
		alignmentsWindow.setResizeWindow(false);
		alignmentsWindow.updateWindowContent();
		alignmentsWindow.setPagePosition(xPosition, yPosition);

		String alignmentWindowTitle = AppPropertiesManager.CONSTANTS.alignment_window_title();
		alignmentWindowTitle = alignmentWindowTitle.replaceFirst("%s", homologsInfoItem.getChains().substring(0, 1));
		alignmentWindowTitle = alignmentWindowTitle.replaceFirst("%s", homologsInfoItem.getUniprotId());
		alignmentsWindow.setHeading(EscapedStringGenerator.generateEscapedString(alignmentWindowTitle));
		alignmentsWindow.setVisible(true);

		//called beacuse of the bug in GXT 2.2.3
		// http://www.sencha.com/forum/showthread.php?126888-Problems-with-RowLayout
		alignmentsWindow.layout(true);
	}

	/**
	 * Shows window containing general information about the application.
	 */
	public void displayAboutWindow()
	{
		if((aboutWindow == null) ||
		   (aboutWindow.isResizeWindow()))
		{
			aboutWindow = new AboutWindow(ApplicationContext.getWindowData());
			aboutWindow.setResizeWindow(false);
		}

		aboutWindow.setVisible(true);
	}
	

	/**
	 * Hides all internal windows.
	 */
	public void hideAllWindows() 
	{
		if(aboutWindow != null)
		{
			aboutWindow.setVisible(false);
		}
		
		if(alignmentsWindow != null)
		{
			alignmentsWindow.setVisible(false);
		}
		
		if(interfacesResiduesWindow != null)
		{
			interfacesResiduesWindow.setVisible(false);
		}
	}	
	
	/**
	 * Sets flag of all internal windows to resize.
	 */
	public void setAllWindowsToResize()
	{
		if(aboutWindow != null)
		{
			aboutWindow.setResizeWindow(true);
		}
		
		if(alignmentsWindow != null)
		{
			alignmentsWindow.setResizeWindow(true);
		}
		
		if(interfacesResiduesWindow != null)
		{
			interfacesResiduesWindow.setResizeWindow(true);
		}
	}
	
	public ResultsPanel getResultsPanel() {
		return resultsPanel;
	}

	public StatusPanel getStatusPanel() {
		return statusPanel;
	}

	public InputDataPanel getInputDataPanel() {
		return inputDataPanel;
	}

	public void setResultsPanel(ResultsPanel resultsPanel) {
		this.resultsPanel = resultsPanel;
	}

	public void setStatusPanel(StatusPanel statusPanel) {
		this.statusPanel = statusPanel;
	}

	public void setInputDataPanel(InputDataPanel inputDataPanel) {
		this.inputDataPanel = inputDataPanel;
	}

	/**
	 * Initializes events listeners.
	 */
	private void initializeEventsListeners()
	{
		EventBusManager.EVENT_BUS.addHandler(UnmaskMainViewEvent.TYPE, new UnmaskMainViewHandler() {
			
			@Override
			public void onUnmaskMainView(UnmaskMainViewEvent event) {
				unmask();
			}
		});
		
		EventBusManager.EVENT_BUS.addHandler(ApplicationWindowResizeEvent.TYPE, new ApplicationWindowResizeHandler() {
			
			@Override
			public void onResizeApplicationWindow(ApplicationWindowResizeEvent event) {
				setSize(ApplicationContext.getAdjustedWindowData().getWindowWidth(), 
						ApplicationContext.getAdjustedWindowData().getWindowHeight());
			}
		});
	}
	
	public void onAttach() 
	{
		setSize(ApplicationContext.getAdjustedWindowData().getWindowWidth(), 
				ApplicationContext.getAdjustedWindowData().getWindowHeight());
		super.onAttach();
	}
}
