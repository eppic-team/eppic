package ch.systemsx.sybit.crkwebui.client.main.controllers;

import ch.systemsx.sybit.crkwebui.client.commons.appdata.AppPropertiesManager;
import ch.systemsx.sybit.crkwebui.client.commons.appdata.ApplicationContext;
import ch.systemsx.sybit.crkwebui.client.commons.events.ApplicationInitEvent;
import ch.systemsx.sybit.crkwebui.client.commons.events.GetFocusOnJobsListEvent;
import ch.systemsx.sybit.crkwebui.client.commons.events.GetFocusOnPdbCodeFieldEvent;
import ch.systemsx.sybit.crkwebui.client.commons.events.HideAllWindowsEvent;
import ch.systemsx.sybit.crkwebui.client.commons.events.HideTopPanelSearchBoxEvent;
import ch.systemsx.sybit.crkwebui.client.commons.events.HideWaitingEvent;
import ch.systemsx.sybit.crkwebui.client.commons.events.InterfaceResiduesDataRetrievedEvent;
import ch.systemsx.sybit.crkwebui.client.commons.events.RefreshStatusDataEvent;
import ch.systemsx.sybit.crkwebui.client.commons.events.ShowAboutEvent;
import ch.systemsx.sybit.crkwebui.client.commons.events.ShowAlignmentsEvent;
import ch.systemsx.sybit.crkwebui.client.commons.events.ShowErrorEvent;
import ch.systemsx.sybit.crkwebui.client.commons.events.ShowHomologsEvent;
import ch.systemsx.sybit.crkwebui.client.commons.events.ShowInterfaceResiduesEvent;
import ch.systemsx.sybit.crkwebui.client.commons.events.ShowMessageEvent;
import ch.systemsx.sybit.crkwebui.client.commons.events.ShowNoResultsDataEvent;
import ch.systemsx.sybit.crkwebui.client.commons.events.ShowResultsDataEvent;
import ch.systemsx.sybit.crkwebui.client.commons.events.ShowStatusDataEvent;
import ch.systemsx.sybit.crkwebui.client.commons.events.ShowTopPanelSearchBoxEvent;
import ch.systemsx.sybit.crkwebui.client.commons.events.ShowViewerSelectorEvent;
import ch.systemsx.sybit.crkwebui.client.commons.events.ShowWaitingEvent;
import ch.systemsx.sybit.crkwebui.client.commons.events.StopJobsListAutoRefreshEvent;
import ch.systemsx.sybit.crkwebui.client.commons.events.UncheckClustersRadioEvent;
import ch.systemsx.sybit.crkwebui.client.commons.events.UpdateStatusLabelEvent;
import ch.systemsx.sybit.crkwebui.client.commons.gui.data.StatusMessageType;
import ch.systemsx.sybit.crkwebui.client.commons.gui.info.PopUpInfo;
import ch.systemsx.sybit.crkwebui.client.commons.gui.panels.DisplayPanel;
import ch.systemsx.sybit.crkwebui.client.commons.handlers.ApplicationInitHandler;
import ch.systemsx.sybit.crkwebui.client.commons.handlers.HideAllWindowsHandler;
import ch.systemsx.sybit.crkwebui.client.commons.handlers.HideWaitingHandler;
import ch.systemsx.sybit.crkwebui.client.commons.handlers.InterfaceResiduesDataRetrievedHandler;
import ch.systemsx.sybit.crkwebui.client.commons.handlers.RefreshStatusDataHandler;
import ch.systemsx.sybit.crkwebui.client.commons.handlers.ShowAboutHandler;
import ch.systemsx.sybit.crkwebui.client.commons.handlers.ShowAlignmentsHandler;
import ch.systemsx.sybit.crkwebui.client.commons.handlers.ShowErrorHandler;
import ch.systemsx.sybit.crkwebui.client.commons.handlers.ShowHomologsHandler;
import ch.systemsx.sybit.crkwebui.client.commons.handlers.ShowInterfaceResiduesWindowHandler;
import ch.systemsx.sybit.crkwebui.client.commons.handlers.ShowMessageHandler;
import ch.systemsx.sybit.crkwebui.client.commons.handlers.ShowNoResultsDataHandler;
import ch.systemsx.sybit.crkwebui.client.commons.handlers.ShowResultsDataHandler;
import ch.systemsx.sybit.crkwebui.client.commons.handlers.ShowStatusDataHandler;
import ch.systemsx.sybit.crkwebui.client.commons.handlers.ShowViewerSelectorHandler;
import ch.systemsx.sybit.crkwebui.client.commons.handlers.ShowWaitingHandler;
import ch.systemsx.sybit.crkwebui.client.commons.handlers.StopJobsListAutoRefreshHandler;
import ch.systemsx.sybit.crkwebui.client.commons.handlers.UpdateStatusLabelHandler;
import ch.systemsx.sybit.crkwebui.client.commons.managers.EventBusManager;
import ch.systemsx.sybit.crkwebui.client.commons.services.eppic.CrkWebServiceProvider;
import ch.systemsx.sybit.crkwebui.client.commons.util.EscapedStringGenerator;
import ch.systemsx.sybit.crkwebui.client.downloads.gui.panels.DownloadsPanel;
import ch.systemsx.sybit.crkwebui.client.faq.gui.panels.FAQPanel;
import ch.systemsx.sybit.crkwebui.client.help.gui.panels.HelpPanel;
import ch.systemsx.sybit.crkwebui.client.input.gui.panels.InputDataPanel;
import ch.systemsx.sybit.crkwebui.client.main.gui.panels.MainViewPort;
import ch.systemsx.sybit.crkwebui.client.main.gui.panels.MainViewScrollable;
import ch.systemsx.sybit.crkwebui.client.publications.gui.panels.PublicationsPanel;
import ch.systemsx.sybit.crkwebui.client.releases.gui.panels.ReleasesPanel;
import ch.systemsx.sybit.crkwebui.client.results.gui.panels.ResultsPanel;
import ch.systemsx.sybit.crkwebui.client.results.gui.panels.StatusPanel;
import ch.systemsx.sybit.crkwebui.shared.model.PDBScoreItem;
import ch.systemsx.sybit.crkwebui.shared.model.ProcessingInProgressData;
import ch.systemsx.sybit.crkwebui.shared.model.StatusOfJob;

import com.google.gwt.core.client.Scheduler;
import com.google.gwt.core.client.Scheduler.ScheduledCommand;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.gwt.user.client.ui.RootPanel;
import com.sencha.gxt.widget.core.client.Dialog;
import com.sencha.gxt.widget.core.client.container.FlowLayoutContainer;
import com.sencha.gxt.widget.core.client.container.Viewport;
import com.sencha.gxt.widget.core.client.event.HideEvent;
import com.sencha.gxt.widget.core.client.event.HideEvent.HideHandler;

/**
 * Main application controller.
 * @author srebniak_a
 *
 */
public class MainController
{
	private MainViewPort mainViewPort;
	
	/**
	 * Timer used to automatically refresh list of jobs in my jobs panel.
	 */
	private Timer autoRefreshMyJobs;
	
	/**
	 * Dialog box used to show information
	 */
	private Dialog infoMessageBox = new Dialog();

	/**
	 * Creates instance of main controller with specified viewport.
	 * @param viewport main viewport
	 */
	public MainController(Viewport viewport)
	{
		initializeEventsListeners();
	}
	
	private void initializeEventsListeners()
	{
		EventBusManager.EVENT_BUS.addHandler(ApplicationInitEvent.TYPE, new ApplicationInitHandler() {
			
			@Override
			public void onApplicationInit(ApplicationInitEvent event) {
				setMainView();
				runMyJobsAutoRefresh();
			}
		});
		
		EventBusManager.EVENT_BUS.addHandler(UpdateStatusLabelEvent.TYPE, new UpdateStatusLabelHandler() 
		{
			@Override
			public void onUpdateStatusLabel(UpdateStatusLabelEvent event)
			{
				if(event.getMessageType() != StatusMessageType.NO_ERROR)
				{
					PopUpInfo.show("Web-Application Error",event.getStatusText());
				}
			}
		});
		
		EventBusManager.EVENT_BUS.addHandler(ShowResultsDataEvent.TYPE, new ShowResultsDataHandler() {
			
			@Override
			public void onShowResultsData(ShowResultsDataEvent event) {
				displayResultView(event.getPdbScoreItem());
			}
		});
		
		EventBusManager.EVENT_BUS.addHandler(ShowStatusDataEvent.TYPE, new ShowStatusDataHandler() {
			
			@Override
			public void onShowStatusData(ShowStatusDataEvent event) {
				displayStatusView(event.getStatusData());
			}
		});
		
		EventBusManager.EVENT_BUS.addHandler(RefreshStatusDataEvent.TYPE, new RefreshStatusDataHandler() {
			
			@Override
			public void onRefreshStatusData(RefreshStatusDataEvent event) {
				refreshStatusView(event.getStatusData());
			}
		});
		
		EventBusManager.EVENT_BUS.addHandler(ShowNoResultsDataEvent.TYPE, new ShowNoResultsDataHandler() {
			
			@Override
			public void onShowNoResultsData(ShowNoResultsDataEvent event) {
				cleanCenterPanel();
			}
		});
		
		EventBusManager.EVENT_BUS.addHandler(ShowErrorEvent.TYPE, new ShowErrorHandler() {
			
			@Override
			public void onShowError(ShowErrorEvent event) 
			{
				showError(event.getErrorText());
			}
		});
		
		EventBusManager.EVENT_BUS.addHandler(ShowMessageEvent.TYPE, new ShowMessageHandler() {
			
			@Override
			public void onShowMessage(ShowMessageEvent event) {
				showMessage(event.getTitle(), event.getMessage());
			}
		});
		
		EventBusManager.EVENT_BUS.addHandler(ShowAboutEvent.TYPE, new ShowAboutHandler() 
		{
			@Override
			public void onShowAbout(ShowAboutEvent event) 
			{
				mainViewPort.displayAboutWindow();
			}
		});
		
		EventBusManager.EVENT_BUS.addHandler(ShowViewerSelectorEvent.TYPE, new ShowViewerSelectorHandler() {
			
			@Override
			public void onShowWindow(ShowViewerSelectorEvent event) {
				mainViewPort.displayViewerSelectorWindow();
				
			}
		});
		
		EventBusManager.EVENT_BUS.addHandler(ShowAlignmentsEvent.TYPE, new ShowAlignmentsHandler() {
			
			@Override
			public void onShowAlignments(ShowAlignmentsEvent event) {
				mainViewPort.displayAlignmentsWindow(event.getHomologsInfoItem(),
										event.getPdbName(), 
										event.getxPosition(), 
										event.getyPostiton());
			}
		});
		
		EventBusManager.EVENT_BUS.addHandler(ShowHomologsEvent.TYPE, new ShowHomologsHandler() {
			
			@Override
			public void onShowHomologs(ShowHomologsEvent event) {
				mainViewPort.displayHomologsWindow(event.getHomologsInfoItem(),
						event.getJobId(), 
						event.getxPosition(), 
						event.getyPosition());
				
			}
		});
		
		EventBusManager.EVENT_BUS.addHandler(ShowInterfaceResiduesEvent.TYPE, new ShowInterfaceResiduesWindowHandler() {
			
			@Override
			public void onShowInterfaceResidues(ShowInterfaceResiduesEvent event) {
				showInterfaceResidues(event.getInterfaceId());
			}
		}); 
		
		EventBusManager.EVENT_BUS.addHandler(ShowWaitingEvent.TYPE, new ShowWaitingHandler() {
			
			@Override
			public void onShowWaiting(ShowWaitingEvent event) {
				mainViewPort.displayWaiting(event.getMessage());
			}
		});
		
		EventBusManager.EVENT_BUS.addHandler(HideWaitingEvent.TYPE, new HideWaitingHandler() {
			
			@Override
			public void onHideWaiting(HideWaitingEvent event) {
				mainViewPort.hideWaiting();
			}
		});
		
		EventBusManager.EVENT_BUS.addHandler(StopJobsListAutoRefreshEvent.TYPE, new StopJobsListAutoRefreshHandler() {
			
			@Override
			public void onStopJobsListAutoRefresh(StopJobsListAutoRefreshEvent event) {
				stopMyJobsAutoRefresh();
			}
		});
		
		
		EventBusManager.EVENT_BUS.addHandler(UpdateStatusLabelEvent.TYPE, new UpdateStatusLabelHandler() {
			
			@Override
			public void onUpdateStatusLabel(UpdateStatusLabelEvent event) 
			{
				if((mainViewPort == null) || (mainViewPort.getBottomPanel() == null))
				{
					showError(event.getStatusText());
				}
			}
		});
		
		EventBusManager.EVENT_BUS.addHandler(InterfaceResiduesDataRetrievedEvent.TYPE, new InterfaceResiduesDataRetrievedHandler() {
			
			@Override
			public void onInterfaceResiduesDataRetrieved(
					final InterfaceResiduesDataRetrievedEvent event) {
				
				Scheduler.get().scheduleDeferred(new ScheduledCommand() {
					@Override
					public void execute() 
					{
						mainViewPort.fillInterfacesWindow(event.getInterfaceResidues(),
														  ApplicationContext.getPdbScoreItem(),
														  ApplicationContext.getSelectedInterface());
					}
				});
			}
		});
		
		EventBusManager.EVENT_BUS.addHandler(HideAllWindowsEvent.TYPE, new HideAllWindowsHandler() {
			
			@Override
			public void onHideAllWindows(HideAllWindowsEvent event) {
				mainViewPort.hideAllWindows();
			}
		});
	}
	
	/**
	 * Displays error.
	 * @param errorMessage message of the error
	 */
	private void showError(String errorMessage)
	{
		if(mainViewPort == null)
		{
			Window.alert(errorMessage);
		}
		else
		{
			mainViewPort.displayError(errorMessage);
		}
	}

	/**
	 * Shows messagebox with provided message.
	 * @param title title of message
	 * @param message text of the message
	 */
	private void showMessage(String title, String message)
	{
		infoMessageBox = new Dialog();
		
		infoMessageBox.setHeadingHtml(EscapedStringGenerator.generateEscapedString(title));
		infoMessageBox.add(new HTMLPanel(EscapedStringGenerator.generateSafeHtml(message)));
		
		infoMessageBox.setHideOnButtonClick(true);
	    infoMessageBox.setPixelSize(350,200);
		
		infoMessageBox.show();
	    
		infoMessageBox.addHideHandler(new HideHandler() {
			@Override
			public void onHide(HideEvent event) {
				EventBusManager.EVENT_BUS.fireEvent(new GetFocusOnJobsListEvent());
				
			}
		});

		infoMessageBox.setResizable(true);
		if(infoMessageBox.getMinWidth() > ApplicationContext.getWindowData().getWindowWidth() - 20)
		{
			infoMessageBox.setWidth(ApplicationContext.getWindowData().getWindowWidth() - 20);
		}
		
		Scheduler.get().scheduleDeferred(new ScheduledCommand() {
			@Override
			public void execute() {
				infoMessageBox.focus();
			}
	    });
	}

	/**
	 * Displays proper central panel based on provided token type.
	 * @param token value determining type of the panel to display
	 */
	public void displayView(String token)
	{
		EventBusManager.EVENT_BUS.fireEvent(new HideAllWindowsEvent());
		EventBusManager.EVENT_BUS.fireEvent(new ShowTopPanelSearchBoxEvent());
		if ((token != null) && (token.length() > 3) && (token.startsWith("id")))
		{
			Window.setTitle(AppPropertiesManager.CONSTANTS.window_title_loading());
			ApplicationContext.setSelectedJobId(token.substring(3));
			displayResults();
		}
		else if((token != null) && (token.equals("help") || token.equals("!help")))
		{
			displayHelp();
		}
		else if((token != null) && (token.equals("downloads") || token.equals("!downloads")))
		{
			displayDownloads();
		}
		else if((token != null) && (token.equals("releases") || token.equals("!releases")))
		{
			displayReleases();
		}
		else if((token != null) && (token.equals("publications") || token.equals("!publications")))
		{
			displayPublications();
		}
		else if((token != null) && (token.equals("faq") || token.equals("!faq")))
		{
			displayFAQ();
		}
		else
		{
			Window.setTitle(AppPropertiesManager.CONSTANTS.window_title_input());
			ApplicationContext.setSelectedJobId("");
			displayInputView();
			EventBusManager.EVENT_BUS.fireEvent(new HideTopPanelSearchBoxEvent());
		}
	}
	
	/**
	 * Initializes main view.
	 */
	private void setMainView()
	{
		mainViewPort = new MainViewPort(this);
		MainViewScrollable mainViewScrollable = new MainViewScrollable(mainViewPort);
		
		Viewport viewPort = new Viewport();
		viewPort.setLayoutData(new FlowLayoutContainer());
		viewPort.add(mainViewScrollable);
		
		RootPanel.get().add(viewPort);
	}

	/**
	 * Displays input data panel.
	 */
	public void displayInputView()
	{
		ApplicationContext.setDoStatusPanelRefreshing(false);

		InputDataPanel inputDataPanel = null;
		
		if((mainViewPort.getCenterPanel().getDisplayPanel() != null) &&
		   (mainViewPort.getCenterPanel().getDisplayPanel() instanceof InputDataPanel))
		{
			inputDataPanel = (InputDataPanel)mainViewPort.getCenterPanel().getDisplayPanel();
			inputDataPanel.resetToDefault();
		}
		else if(mainViewPort.getInputDataPanel() != null)
		{
			inputDataPanel = mainViewPort.getInputDataPanel();
			inputDataPanel.resetToDefault();
			mainViewPort.getCenterPanel().setDisplayPanel(inputDataPanel);
		}
		else{
			inputDataPanel = new InputDataPanel();
			mainViewPort.setInputDataPanel(inputDataPanel);
			mainViewPort.getCenterPanel().setDisplayPanel(inputDataPanel);
		}
		
		Scheduler.get().scheduleDeferred(new ScheduledCommand() {
			@Override
			public void execute() {
				EventBusManager.EVENT_BUS.fireEvent(new GetFocusOnPdbCodeFieldEvent());
			}
	    });
	}
	
	/**
	 * Displays help panel.
	 */
	public void displayHelp()
	{
	    Window.setTitle(AppPropertiesManager.CONSTANTS.window_title_help());
		ApplicationContext.setSelectedJobId("");
		HelpPanel helpPanel = new HelpPanel();
		displayPanelInCentralPanel(helpPanel);
	}
	
	/**
	 * Displays downloads panel.
	 */
	public void displayDownloads()
	{
		Window.setTitle(AppPropertiesManager.CONSTANTS.window_title_downloads());
		ApplicationContext.setSelectedJobId("");
		DownloadsPanel downloadsPanel = new DownloadsPanel();
		displayPanelInCentralPanel(downloadsPanel);
	}

	/**
	 * Displays releases panel.
	 */
	public void displayReleases()
	{
		Window.setTitle(AppPropertiesManager.CONSTANTS.window_title_releases());
		ApplicationContext.setSelectedJobId("");
		ReleasesPanel releasesPanel = new ReleasesPanel();
		displayPanelInCentralPanel(releasesPanel);
	}
	
	/**
	 * Displays publication panel.
	 */
	public void displayPublications()
	{
		Window.setTitle(AppPropertiesManager.CONSTANTS.window_title_publications());
		ApplicationContext.setSelectedJobId("");
		PublicationsPanel publicationsPanel = new PublicationsPanel();
		displayPanelInCentralPanel(publicationsPanel);
	}
	
	/**
	 * Displays faq panel.
	 */
	public void displayFAQ()
	{
		Window.setTitle(AppPropertiesManager.CONSTANTS.window_title_faq());
		ApplicationContext.setSelectedJobId("");
		FAQPanel faqPanel = new FAQPanel();
		displayPanelInCentralPanel(faqPanel);
	}
	
	public void displayPanelInCentralPanel(DisplayPanel panel) {
	    ApplicationContext.setDoStatusPanelRefreshing(false);
	    mainViewPort.getCenterPanel().setDisplayPanel(panel);
	}
	
	/**
	 * Retrieves results of processing for displaying central panel content.
	 */
	public void displayResults()
	{
		mainViewPort.mask(AppPropertiesManager.CONSTANTS.defaultmask());
		if(mainViewPort.getResultsPanel() != null){
			EventBusManager.EVENT_BUS.fireEvent(new UncheckClustersRadioEvent());
		}
		CrkWebServiceProvider.getServiceController().getResultsOfProcessing(ApplicationContext.getSelectedJobId());
	}

	/**
	 * Displays results data panel.
	 * @param resultData results of processing
	 */
	private void displayResultView(PDBScoreItem resultData)
	{
		ApplicationContext.setDoStatusPanelRefreshing(false);

		ResultsPanel resultsPanel = null;
		
		if((mainViewPort.getCenterPanel().getDisplayPanel() != null) &&
		   (mainViewPort.getCenterPanel().getDisplayPanel() instanceof ResultsPanel))
		{
			resultsPanel = (ResultsPanel)mainViewPort.getCenterPanel().getDisplayPanel();
			resultsPanel.fillResultsPanel(resultData);
			//resultsPanel.layout();
		}
		else if(mainViewPort.getResultsPanel() != null)
		{
			resultsPanel = mainViewPort.getResultsPanel();
			resultsPanel.fillResultsPanel(resultData);
			mainViewPort.getCenterPanel().setDisplayPanel(resultsPanel);
			resultsPanel.resizeContent();
		}
		else
		{
			resultsPanel = new ResultsPanel(resultData);
			resultsPanel.fillResultsPanel(resultData);
			mainViewPort.setResultsPanel(resultsPanel);
			mainViewPort.getCenterPanel().setDisplayPanel(resultsPanel);
			resultsPanel.resizeContent();
		}

		EventBusManager.EVENT_BUS.fireEvent(new GetFocusOnJobsListEvent());
		Window.setTitle(AppPropertiesManager.CONSTANTS.window_title_results() + " - " + 
						resultData.getPdbName());
	}

	/**
	 * Displays status panel.
	 * @param statusData status data of the current job
	 */
	public void displayStatusView(ProcessingInProgressData statusData)
	{
		StatusPanel statusPanel = null;

		if((mainViewPort.getCenterPanel().getDisplayPanel() != null) &&
		   (mainViewPort.getCenterPanel().getDisplayPanel() instanceof StatusPanel))
		{
			statusPanel = (StatusPanel)mainViewPort.getCenterPanel().getDisplayPanel();
			statusPanel.cleanData();
		}
		else
		{
			statusPanel = new StatusPanel();
			mainViewPort.getCenterPanel().setDisplayPanel(statusPanel);
		}

		if(statusPanel != null)
		{
			statusPanel.fillData(statusData);
		}

		if((statusData.getStatus() != null) &&
		   ((statusData.getStatus().equals(StatusOfJob.RUNNING.getName())) ||
			(statusData.getStatus().equals(StatusOfJob.WAITING.getName())) ||
			(statusData.getStatus().equals(StatusOfJob.QUEUING.getName()))))
		{
			ApplicationContext.setDoStatusPanelRefreshing(true);
		}
		else
		{
			ApplicationContext.setDoStatusPanelRefreshing(false);
		}

		//mainViewPort.getCenterPanel().layout();

		EventBusManager.EVENT_BUS.fireEvent(new GetFocusOnJobsListEvent());
		Window.setTitle(AppPropertiesManager.CONSTANTS.window_title_processing() + " - " + 
						statusData.getInput());
	}
	
	/**
	 * Refreshes content of the status panel.
	 * @param statusData status data of the current job
	 */
	private void refreshStatusView(ProcessingInProgressData statusData)
	{
		if((mainViewPort.getCenterPanel().getDisplayPanel() != null) &&
		   (mainViewPort.getCenterPanel().getDisplayPanel() instanceof StatusPanel))
		{
			StatusPanel statusPanel = (StatusPanel)mainViewPort.getCenterPanel().getDisplayPanel();
			statusPanel.fillData(statusData);
			//mainViewPort.getCenterPanel().layout();
		}
	}
	
	/**
	 * Cleans content of central panel.
	 */
	private void cleanCenterPanel()
	{
		//mainViewPort.getCenterPanel().removeAll();
		mainViewPort.getCenterPanel().setDisplayPanel(null);
	}
	
	/**
	 * Show interface residues items window.
	 * @param interfaceId interface identifier
	 */
	private void showInterfaceResidues(int interfaceId)
	{
		

		if((ApplicationContext.getResiduesForInterface() != null) &&
		   (ApplicationContext.getResiduesForInterface().containsKey(interfaceId)))
		{
			EventBusManager.EVENT_BUS.fireEvent(new InterfaceResiduesDataRetrievedEvent(ApplicationContext.getResiduesForInterface().get(interfaceId)));
		}
		else
		{
			CrkWebServiceProvider.getServiceController().getInterfaceResidues(ApplicationContext.getPdbScoreItem().getJobId(),
												   ApplicationContext.getPdbScoreItem().getInterfaceItem(interfaceId - 1).getUid(),
												   interfaceId);
		}
		
		mainViewPort.displayInterfacesWindow(interfaceId);
	}
	
	/**
	 * Auto refreshes jobs grid.
	 */
	private void runMyJobsAutoRefresh()
	{
		CrkWebServiceProvider.getServiceController().getJobsForCurrentSession();

		autoRefreshMyJobs = new Timer()
		{
			public void run()
			{
				if((ApplicationContext.isDoStatusPanelRefreshing()) &&
					(ApplicationContext.getSelectedJobId() != null) &&
					(!ApplicationContext.getSelectedJobId().equals("")))
				{
					CrkWebServiceProvider.getServiceController().getCurrentStatusData(ApplicationContext.getSelectedJobId());
				}
				else if(ApplicationContext.isAnyJobRunning())
				{
					CrkWebServiceProvider.getServiceController().getJobsForCurrentSession();
				}
			}
		};

		autoRefreshMyJobs.scheduleRepeating(10000);
	}
	
	/**
	 * Stops automated refreshing of jobs grid.
	 */
	private void stopMyJobsAutoRefresh()
	{
		autoRefreshMyJobs.cancel();
	}
}
