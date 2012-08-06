package ch.systemsx.sybit.crkwebui.client.controllers;

import ch.systemsx.sybit.crkwebui.client.data.WindowData;
import ch.systemsx.sybit.crkwebui.client.events.ApplicationInitEvent;
import ch.systemsx.sybit.crkwebui.client.events.ApplicationWindowResizeEvent;
import ch.systemsx.sybit.crkwebui.client.events.GetFocusOnJobsListEvent;
import ch.systemsx.sybit.crkwebui.client.events.GetFocusOnPdbCodeFieldEvent;
import ch.systemsx.sybit.crkwebui.client.events.HideAllWindowsEvent;
import ch.systemsx.sybit.crkwebui.client.events.HideWaitingEvent;
import ch.systemsx.sybit.crkwebui.client.events.InterfaceResiduesDataRetrievedEvent;
import ch.systemsx.sybit.crkwebui.client.events.RefreshStatusDataEvent;
import ch.systemsx.sybit.crkwebui.client.events.ShowAboutEvent;
import ch.systemsx.sybit.crkwebui.client.events.ShowAlignmentsEvent;
import ch.systemsx.sybit.crkwebui.client.events.ShowErrorEvent;
import ch.systemsx.sybit.crkwebui.client.events.ShowInterfaceResiduesEvent;
import ch.systemsx.sybit.crkwebui.client.events.ShowMessageEvent;
import ch.systemsx.sybit.crkwebui.client.events.ShowNoResultsDataEvent;
import ch.systemsx.sybit.crkwebui.client.events.ShowResultsDataEvent;
import ch.systemsx.sybit.crkwebui.client.events.ShowStatusDataEvent;
import ch.systemsx.sybit.crkwebui.client.events.ShowWaitingEvent;
import ch.systemsx.sybit.crkwebui.client.events.StopJobsListAutoRefreshEvent;
import ch.systemsx.sybit.crkwebui.client.events.UpdateStatusLabelEvent;
import ch.systemsx.sybit.crkwebui.client.gui.HelpPanel;
import ch.systemsx.sybit.crkwebui.client.gui.InputDataPanel;
import ch.systemsx.sybit.crkwebui.client.gui.MainViewPort;
import ch.systemsx.sybit.crkwebui.client.gui.ResultsPanel;
import ch.systemsx.sybit.crkwebui.client.gui.StatusPanel;
import ch.systemsx.sybit.crkwebui.client.handlers.ApplicationInitHandler;
import ch.systemsx.sybit.crkwebui.client.handlers.ApplicationWindowResizeHandler;
import ch.systemsx.sybit.crkwebui.client.handlers.HideAllWindowsHandler;
import ch.systemsx.sybit.crkwebui.client.handlers.HideWaitingHandler;
import ch.systemsx.sybit.crkwebui.client.handlers.InterfaceResiduesDataRetrievedHandler;
import ch.systemsx.sybit.crkwebui.client.handlers.RefreshStatusDataHandler;
import ch.systemsx.sybit.crkwebui.client.handlers.ShowAboutHandler;
import ch.systemsx.sybit.crkwebui.client.handlers.ShowAlignmentsHandler;
import ch.systemsx.sybit.crkwebui.client.handlers.ShowErrorHandler;
import ch.systemsx.sybit.crkwebui.client.handlers.ShowInterfaceResiduesWindowHandler;
import ch.systemsx.sybit.crkwebui.client.handlers.ShowMessageHandler;
import ch.systemsx.sybit.crkwebui.client.handlers.ShowNoResultsDataHandler;
import ch.systemsx.sybit.crkwebui.client.handlers.ShowResultsDataHandler;
import ch.systemsx.sybit.crkwebui.client.handlers.ShowStatusDataHandler;
import ch.systemsx.sybit.crkwebui.client.handlers.ShowWaitingHandler;
import ch.systemsx.sybit.crkwebui.client.handlers.StopJobsListAutoRefreshHandler;
import ch.systemsx.sybit.crkwebui.client.handlers.UpdateStatusLabelHandler;
import ch.systemsx.sybit.crkwebui.shared.model.PDBScoreItem;
import ch.systemsx.sybit.crkwebui.shared.model.ProcessingInProgressData;
import ch.systemsx.sybit.crkwebui.shared.model.StatusOfJob;

import com.extjs.gxt.ui.client.event.Listener;
import com.extjs.gxt.ui.client.event.MessageBoxEvent;
import com.extjs.gxt.ui.client.widget.MessageBox;
import com.extjs.gxt.ui.client.widget.Viewport;
import com.google.gwt.core.client.Scheduler;
import com.google.gwt.core.client.Scheduler.ScheduledCommand;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.RootPanel;

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
	 * Creates instance of main controller with specified viewport.
	 * @param viewport main viewport
	 */
	public MainController(Viewport viewport)
	{
		ApplicationContext.setWindowData(new WindowData(Window.getClientWidth(), Window.getClientHeight()));
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
		
		EventBusManager.EVENT_BUS.addHandler(ShowAlignmentsEvent.TYPE, new ShowAlignmentsHandler() {
			
			@Override
			public void onShowAlignments(ShowAlignmentsEvent event) {
				mainViewPort.displayAlignmentsWindow(event.getHomologsInfoItem(),
										event.getPdbName(), 
										event.getxPosition(), 
										event.getyPostiton());
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
		
		EventBusManager.EVENT_BUS.addHandler(ApplicationWindowResizeEvent.TYPE, new ApplicationWindowResizeHandler() {
			
			@Override
			public void onResizeApplicationWindow(ApplicationWindowResizeEvent event) {
				mainViewPort.setAllWindowsToResize();
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
		MessageBox infoMessageBox = MessageBox.info(title, message, new Listener<MessageBoxEvent>() {

			@Override
			public void handleEvent(MessageBoxEvent be) {
				EventBusManager.EVENT_BUS.fireEvent(new GetFocusOnJobsListEvent());
			}
		});

		infoMessageBox.setMinWidth(300);
		infoMessageBox.setMaxWidth(ApplicationContext.getWindowData().getWindowWidth());
	}

	/**
	 * Displays proper central panel based on provided token type.
	 * @param token value determining type of the panel to display
	 */
	public void displayView(String token)
	{
		EventBusManager.EVENT_BUS.fireEvent(new HideAllWindowsEvent());

		if ((token != null) && (token.length() > 3) && (token.startsWith("id")))
		{
			Window.setTitle(AppPropertiesManager.CONSTANTS.window_title_loading());
			ApplicationContext.setSelectedJobId(token.substring(3));
			displayResults();
		}
		else if((token != null) && (token.equals("help")))
		{
			Window.setTitle(AppPropertiesManager.CONSTANTS.window_title_help());
			ApplicationContext.setSelectedJobId("");
			displayHelp();
		}
		else
		{
			Window.setTitle(AppPropertiesManager.CONSTANTS.window_title_input());
			ApplicationContext.setSelectedJobId("");
			displayInputView();
		}
	}
	
	/**
	 * Initializes main view.
	 */
	private void setMainView()
	{
		mainViewPort = new MainViewPort(this);
		RootPanel.get().add(mainViewPort);
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
			inputDataPanel.resetValues();
			inputDataPanel.layout();
		}
		else if(mainViewPort.getInputDataPanel() != null)
		{
			inputDataPanel = mainViewPort.getInputDataPanel();
			inputDataPanel.resetValues();
			mainViewPort.getCenterPanel().setDisplayPanel(inputDataPanel);
		}
		else
		{
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
		ApplicationContext.setDoStatusPanelRefreshing(false);

		HelpPanel helpPanel = new HelpPanel();
		mainViewPort.getCenterPanel().setDisplayPanel(helpPanel);
	}

	/**
	 * Retrieves results of processing for displaying central panel content.
	 */
	public void displayResults()
	{
		mainViewPort.mask(AppPropertiesManager.CONSTANTS.defaultmask());
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
			resultsPanel.layout();
		}
		else if(mainViewPort.getResultsPanel() != null)
		{
			resultsPanel = mainViewPort.getResultsPanel();
			resultsPanel.fillResultsPanel(resultData);
			mainViewPort.getCenterPanel().setDisplayPanel(resultsPanel);
			resultsPanel.resizeGrid();
		}
		else
		{
			resultsPanel = new ResultsPanel(resultData);
			resultsPanel.fillResultsPanel(resultData);
			mainViewPort.setResultsPanel(resultsPanel);
			mainViewPort.getCenterPanel().setDisplayPanel(resultsPanel);
			resultsPanel.resizeGrid();
		}

		EventBusManager.EVENT_BUS.fireEvent(new GetFocusOnJobsListEvent());
		Window.setTitle(AppPropertiesManager.CONSTANTS.window_title_results() + " - " + resultData.getPdbName());
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
			statusPanel = new StatusPanel(ApplicationContext.getWindowData().getWindowHeight());
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

		mainViewPort.getCenterPanel().layout();

		EventBusManager.EVENT_BUS.fireEvent(new GetFocusOnJobsListEvent());
		Window.setTitle(AppPropertiesManager.CONSTANTS.window_title_processing() + " - " + statusData.getInput());
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
			mainViewPort.getCenterPanel().layout();
		}
	}
	
	/**
	 * Cleans content of central panel.
	 */
	private void cleanCenterPanel()
	{
		mainViewPort.getCenterPanel().removeAll();
		mainViewPort.getCenterPanel().setDisplayPanel(null);
	}
	
	/**
	 * Show interface residues items window.
	 * @param interfaceId interface identifier
	 */
	private void showInterfaceResidues(int interfaceId)
	{
		mainViewPort.displayInterfacesWindow(interfaceId);

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
				else
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
