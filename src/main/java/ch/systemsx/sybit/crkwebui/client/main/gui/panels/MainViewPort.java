package ch.systemsx.sybit.crkwebui.client.main.gui.panels;

import java.util.HashMap;
import java.util.List;

import ch.systemsx.sybit.crkwebui.client.about.gui.windows.AboutWindow;
import ch.systemsx.sybit.crkwebui.client.alignment.gui.windows.AlignmentsWindow;
import ch.systemsx.sybit.crkwebui.client.commons.appdata.AppPropertiesManager;
import ch.systemsx.sybit.crkwebui.client.commons.appdata.ApplicationContext;
import ch.systemsx.sybit.crkwebui.client.commons.events.ApplicationWindowResizeEvent;
import ch.systemsx.sybit.crkwebui.client.commons.events.HideJobsPanelEvent;
import ch.systemsx.sybit.crkwebui.client.commons.events.ShowJobsPanelEvent;
import ch.systemsx.sybit.crkwebui.client.commons.events.UnmaskMainViewEvent;
import ch.systemsx.sybit.crkwebui.client.commons.handlers.ApplicationWindowResizeHandler;
import ch.systemsx.sybit.crkwebui.client.commons.handlers.HideJobsPanelHandler;
import ch.systemsx.sybit.crkwebui.client.commons.handlers.ShowJobsPanelHandler;
import ch.systemsx.sybit.crkwebui.client.commons.handlers.UnmaskMainViewHandler;
import ch.systemsx.sybit.crkwebui.client.commons.managers.EventBusManager;
import ch.systemsx.sybit.crkwebui.client.commons.util.EscapedStringGenerator;
import ch.systemsx.sybit.crkwebui.client.homologs.gui.windows.HomologsWindow;
import ch.systemsx.sybit.crkwebui.client.input.gui.panels.InputDataPanel;
import ch.systemsx.sybit.crkwebui.client.jobs.gui.panels.MyJobsPanel;
import ch.systemsx.sybit.crkwebui.client.main.controllers.MainController;
import ch.systemsx.sybit.crkwebui.client.residues.gui.windows.InterfacesResiduesWindow;
import ch.systemsx.sybit.crkwebui.client.results.gui.panels.ResultsPanel;
import ch.systemsx.sybit.crkwebui.client.results.gui.panels.StatusPanel;
import ch.systemsx.sybit.crkwebui.client.viewer.gui.windows.ViewerSelectorWindow;
import ch.systemsx.sybit.crkwebui.shared.model.HomologsInfoItem;
import ch.systemsx.sybit.crkwebui.shared.model.InterfaceItem;
import ch.systemsx.sybit.crkwebui.shared.model.InterfaceResidueItem;
import ch.systemsx.sybit.crkwebui.shared.model.PDBScoreItem;

import com.google.gwt.core.client.Scheduler;
import com.google.gwt.core.client.Scheduler.ScheduledCommand;
import com.sencha.gxt.widget.core.client.container.BorderLayoutContainer;
import com.sencha.gxt.widget.core.client.event.CollapseItemEvent;
import com.sencha.gxt.widget.core.client.event.CollapseItemEvent.CollapseItemHandler;
import com.sencha.gxt.widget.core.client.event.ExpandItemEvent;
import com.sencha.gxt.widget.core.client.event.ExpandItemEvent.ExpandItemHandler;
import com.sencha.gxt.widget.core.client.ContentPanel;
import com.sencha.gxt.core.client.Style.LayoutRegion;
import com.sencha.gxt.core.client.util.Margins;
import com.sencha.gxt.widget.core.client.box.AlertMessageBox;
import com.sencha.gxt.widget.core.client.box.AutoProgressMessageBox;
import com.sencha.gxt.widget.core.client.box.MessageBox;

/**
 * Main view of the application.
 * @author srebniak_a
 *
 */
public class MainViewPort extends BorderLayoutContainer
{
	private MyJobsPanel myJobsPanel;

	private CenterPanel centerPanel;

	private TopPanel topPanel;
	
	private BottomPanel bottomPanel;

	private InterfacesResiduesWindow interfacesResiduesWindow;
	private AlignmentsWindow alignmentsWindow;
	private AboutWindow aboutWindow;
	private ViewerSelectorWindow viewerWindow;
	private HomologsWindow homologsWindow;

	private MessageBox waitingMessageBox;
	private MessageBox errorMessageBox;
	
	private ResultsPanel resultsPanel;
	private StatusPanel statusPanel;
	private InputDataPanel inputDataPanel;
	
	public MainViewPort(final MainController mainController)
	{
		this.addStyleName("eppic-default-font");
		
		this.addCollapseHandler(new CollapseItemHandler<ContentPanel>() {
			@Override
			public void onCollapse(CollapseItemEvent<ContentPanel> event) {
				if(event.getItem() instanceof MyJobsPanel)
				{
					EventBusManager.EVENT_BUS.fireEvent(new ApplicationWindowResizeEvent());
				}
				
			}
			
		});

		this.addExpandHandler(new ExpandItemHandler<ContentPanel>() {
			@Override
			public void onExpand(ExpandItemEvent<ContentPanel> event) {
				if(event.getItem() instanceof MyJobsPanel)
				{
					EventBusManager.EVENT_BUS.fireEvent(new ApplicationWindowResizeEvent());
				}
			}
	
		});

		BorderLayoutData westData = new BorderLayoutData(120);
		westData.setCollapsible(true);
		westData.setFloatable(true);
		westData.setSplit(true);
		westData.setMargins(new Margins(0, 0, 0, 10));

		myJobsPanel = new MyJobsPanel();
		this.setWestWidget(myJobsPanel, westData);

		BorderLayoutData centerData = new BorderLayoutData(200);
		centerData.setCollapsible(true);
		centerData.setFloatable(true);
		centerData.setSplit(true);
		centerData.setMargins(new Margins(0, 10, 0, 10));

		centerPanel = new CenterPanel();
		this.setCenterWidget(centerPanel, centerData);

		BorderLayoutData northData = new BorderLayoutData(40);
		northData.setMargins(new Margins(0));

		topPanel = new TopPanel();
		this.setNorthWidget(topPanel, northData);
		
		BorderLayoutData southData = new BorderLayoutData(15);
		southData.setMargins(new Margins(10,0,5,0));
		bottomPanel = new BottomPanel();
		this.setSouthWidget(bottomPanel, southData);
		
		this.hide(LayoutRegion.EAST);
		
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
	 * Retrieves top panel.
	 * @return top panel
	 */
	public TopPanel getTopPanel()
	{
		return topPanel;
	}

	/**
	 * Displays window containing interface residues.
	 * @param selectedInterface selected interface identifier
	 */
	public void displayInterfacesWindow(int selectedInterface)
	{
		ApplicationContext.setSelectedInterface(selectedInterface);
		
		if(interfacesResiduesWindow != null && interfacesResiduesWindow.isVisible()){
			interfacesResiduesWindow.getInterfacesResiduesPanel().cleanData();
			interfacesResiduesWindow.setVisible(true);
		}
		else{
			interfacesResiduesWindow = new InterfacesResiduesWindow(ApplicationContext.getWindowData());
			interfacesResiduesWindow.setVisible(true);
			Scheduler.get().scheduleDeferred(new ScheduledCommand() {
				@Override
				public void execute() {
					interfacesResiduesWindow.getInterfacesResiduesPanel().resizeResiduesPanels();
				}
			});
		}
		
		InterfaceItem interfaceItem = ApplicationContext.getPdbScoreItem().getInterfaceItem(selectedInterface - 1);
		interfacesResiduesWindow.setWindowHeaders(interfaceItem.getArea(),
												  interfaceItem.getChain1(),
												  interfaceItem.getChain2(),
												  selectedInterface);

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
		waitingMessageBox = new AutoProgressMessageBox(AppPropertiesManager.CONSTANTS.waiting_message_box_header(),
				EscapedStringGenerator.generateEscapedString(text) + ", " + AppPropertiesManager.CONSTANTS.waiting_message_box_info() + "...");

		waitingMessageBox.setResizable(true);
		if(waitingMessageBox.getMinWidth() > ApplicationContext.getWindowData().getWindowWidth() - 20)
		{
			waitingMessageBox.setWidth(ApplicationContext.getWindowData().getWindowWidth() - 20);
		}


		waitingMessageBox.show();

	}

	/**
	 * Hides waiting messagebox.
	 */
	public void hideWaiting()
	{
		if(waitingMessageBox != null)
		{
			waitingMessageBox.hide();
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
			errorMessageBox = new AlertMessageBox(AppPropertiesManager.CONSTANTS.error_message_box_header(),
											   EscapedStringGenerator.generateEscapedString(message));

			errorMessageBox.setResizable(true);
			if(errorMessageBox.getMinWidth() > ApplicationContext.getWindowData().getWindowWidth() - 20)
			{
				errorMessageBox.setWidth(ApplicationContext.getWindowData().getWindowWidth() - 20);
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
		if(alignmentsWindow != null)
		{
			alignmentsWindow.setVisible(false);
		}
		
		alignmentsWindow = new AlignmentsWindow(ApplicationContext.getWindowData(), homologsInfoItem, pdbName);
		alignmentsWindow.updateWindowContent();
		alignmentsWindow.setPagePosition(xPosition, yPosition);

		String alignmentWindowTitle = AppPropertiesManager.CONSTANTS.alignment_window_title();
		alignmentWindowTitle = alignmentWindowTitle.replaceFirst("%s", homologsInfoItem.getChains().substring(0, 1));
		alignmentWindowTitle = alignmentWindowTitle.replaceFirst("%s", homologsInfoItem.getUniprotId());
		alignmentsWindow.setHeadingHtml(EscapedStringGenerator.generateEscapedString(alignmentWindowTitle));
		alignmentsWindow.setVisible(true);

		//called beacuse of the bug in GXT 2.2.3
		// http://www.sencha.com/forum/showthread.php?126888-Problems-with-RowLayout
		//alignmentsWindow.layout(true);
	}

	/**
	 * Displays homologs window.
	 * @param homologsInfoItem homologs info item
	 * @param pdbName pdb name
	 * @param xPosition left corner
	 * @param yPosition top corner
	 */
	public void displayHomologsWindow(HomologsInfoItem homologsInfoItem,
										String pdbName,
										int xPosition,
										int yPosition)
	{
		if(homologsWindow != null)
		{
			homologsWindow.setVisible(false);
		}
		
		homologsWindow = new HomologsWindow(ApplicationContext.getWindowData(), homologsInfoItem, pdbName);
		homologsWindow.updateWindowContent();
		homologsWindow.setPagePosition(xPosition, yPosition);

		String homologsWindowTitle = AppPropertiesManager.CONSTANTS.homologs_window_title();
		homologsWindow.setHeadingHtml(EscapedStringGenerator.generateEscapedString(homologsWindowTitle));
		homologsWindow.setVisible(true);
	}
	
	/**
	 * Shows window containing general information about the application.
	 */
	public void displayAboutWindow()
	{
		if(aboutWindow != null && aboutWindow.isVisible())
		{
			return;
		}
		aboutWindow = new AboutWindow(ApplicationContext.getWindowData());
		aboutWindow.setVisible(true);
	}
	
	
	/**
	 * Shows window to select the viewer.
	 */
	public void displayViewerSelectorWindow()
	{
		if(viewerWindow != null && viewerWindow.isVisible())
		{
			return;
		}
		viewerWindow = new ViewerSelectorWindow(ApplicationContext.getWindowData());
		viewerWindow.setVisible(true);
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
		
		if(viewerWindow != null)
		{
			viewerWindow.setVisible(false);
		}
		
		if(homologsWindow != null)
		{
			homologsWindow.setVisible(false);
		}
	}	
	
	private void setJobsPanelVisibility(boolean visibility){
		if(visibility) this.show(LayoutRegion.WEST);
		else this.hide(LayoutRegion.WEST);
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
				setPixelSize(ApplicationContext.getAdjustedWindowData().getWindowWidth(), 
						ApplicationContext.getAdjustedWindowData().getWindowHeight());
				resizeContent();
			}
		});
		
		EventBusManager.EVENT_BUS.addHandler(ShowJobsPanelEvent.TYPE, new ShowJobsPanelHandler() {
			
			@Override
			public void onShowJobsPanel(ShowJobsPanelEvent event) {
				setJobsPanelVisibility(true);
				EventBusManager.EVENT_BUS.fireEvent(new ApplicationWindowResizeEvent());
			}
		});
		
		EventBusManager.EVENT_BUS.addHandler(HideJobsPanelEvent.TYPE, new HideJobsPanelHandler() {
			
			@Override
			public void onHideJobsPanel(HideJobsPanelEvent event) {
				setJobsPanelVisibility(false);
				EventBusManager.EVENT_BUS.fireEvent(new ApplicationWindowResizeEvent());
			}
		});
	}
	
	protected void resizeContent() {
		if(this.getCenterPanel().getDisplayPanel() instanceof ResultsPanel){
			resultsPanel = (ResultsPanel)this.getCenterPanel().getDisplayPanel();
			resultsPanel.resizeContent();
		}
	}

	public void onAttach() 
	{
		setPixelSize(ApplicationContext.getAdjustedWindowData().getWindowWidth(), 
				ApplicationContext.getAdjustedWindowData().getWindowHeight());
		super.onAttach();
	}
}
