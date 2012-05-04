package ch.systemsx.sybit.crkwebui.client.controllers;

import java.util.HashMap;
import java.util.List;

import ch.systemsx.sybit.crkwebui.client.gui.InputDataPanel;
import ch.systemsx.sybit.crkwebui.client.gui.MainViewPort;
import ch.systemsx.sybit.crkwebui.client.gui.ResultsPanel;
import ch.systemsx.sybit.crkwebui.client.gui.StatusPanel;
import ch.systemsx.sybit.crkwebui.shared.model.ApplicationSettings;
import ch.systemsx.sybit.crkwebui.shared.model.HomologsInfoItem;
import ch.systemsx.sybit.crkwebui.shared.model.InterfaceResidueItem;
import ch.systemsx.sybit.crkwebui.shared.model.InterfaceResiduesItemsList;
import ch.systemsx.sybit.crkwebui.shared.model.PDBScoreItem;
import ch.systemsx.sybit.crkwebui.shared.model.ProcessingInProgressData;
import ch.systemsx.sybit.crkwebui.shared.model.RunJobData;
import ch.systemsx.sybit.crkwebui.shared.model.StatusOfJob;

import com.extjs.gxt.ui.client.GXT;
import com.extjs.gxt.ui.client.event.Listener;
import com.extjs.gxt.ui.client.event.MessageBoxEvent;
import com.extjs.gxt.ui.client.widget.MessageBox;
import com.extjs.gxt.ui.client.widget.Viewport;
import com.google.gwt.core.client.GWT;
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
	public static final AppProperties CONSTANTS = (AppProperties) GWT.create(AppProperties.class);

	private MainViewPort mainViewPort;

	private int windowWidth = Window.getClientWidth();
	private int windowHeight = Window.getClientHeight();

	private ServiceController serviceController;

	private ApplicationSettings settings;

	private PDBScoreItem pdbScoreItem;
	private InterfaceResiduesItemsList residuesForInterface;

	private String selectedJobId;
	private boolean debug;

	private Timer autoRefreshMyJobs;
	private boolean canRefreshMyJobs = true;

	private boolean doStatusPanelRefreshing = false;

	private int nrOfSubmissions = 0;

	private String selectedViewer = MainController.CONSTANTS.viewer_jmol();

	public MainController(Viewport viewport)
	{
		this.serviceController = new ServiceControllerImpl(this);
	}

	/**
	 * Retrieves general application settings.
	 */
	public void loadSettings()
	{
		serviceController.loadSettings();
	}

	/**
	 * Displays proper central panel based on provided token type.
	 * @param token value determining type of the panel to display
	 */
	public void displayView(String token)
	{
		hideWindows();

		if ((token != null) && (token.length() > 3) && (token.startsWith("id")))
		{
			Window.setTitle(CONSTANTS.window_title_loading());
			selectedJobId = token.substring(3);
			debug = false;
			displayResults(false);
		}
		else if ((token != null) && (token.length() > 4) && (token.startsWith("deb")))
		{
			Window.setTitle(CONSTANTS.window_title_loading());
			selectedJobId = token.substring(4);
			debug = true;
			displayResults(true);
		}
		else
		{
			Window.setTitle(CONSTANTS.window_title_input());
			selectedJobId = "";
			displayInputView();
		}
	}

	/**
	 * Hides all visible windows.
	 */
	private void hideWindows()
	{
		if(mainViewPort.getAlignmentsWindow() != null)
		{
			mainViewPort.getAlignmentsWindow().setVisible(false);
		}

		if(mainViewPort.getInterfacesResiduesWindow() != null)
		{
			mainViewPort.getInterfacesResiduesWindow().setVisible(false);
		}

		if(mainViewPort.getAboutWindow() != null)
		{
			mainViewPort.getAboutWindow().setVisible(false);
		}

		if((mainViewPort.getCenterPanel().getDisplayPanel() != null) &&
			(mainViewPort.getCenterPanel().getDisplayPanel() instanceof ResultsPanel))
		{
			ResultsPanel resultsPanel = (ResultsPanel)mainViewPort.getCenterPanel().getDisplayPanel();

			if(resultsPanel.getInfoPanel() != null)
			{
				if(resultsPanel.getInfoPanel().getQueryWarningsTooltip() != null)
				{
					resultsPanel.getInfoPanel().getQueryWarningsTooltip().setVisible(false);
				}

				if(resultsPanel.getInfoPanel().getInputParametersTooltip() != null)
				{
					resultsPanel.getInfoPanel().getInputParametersTooltip().setVisible(false);
				}
			}
		}
	}

	/**
	 * Displays input data panel.
	 */
	public void displayInputView()
	{
		doStatusPanelRefreshing = false;

		InputDataPanel inputDataPanel = new InputDataPanel(this);
		mainViewPort.getCenterPanel().setDisplayPanel(inputDataPanel);
	}

	/**
	 * Retrieves results of processing for displaying central panel content.
	 * @param debug flag specifying whether detailed information should be displayed
	 */
	public void displayResults(boolean debug)
	{
		mainViewPort.mask(CONSTANTS.defaultmask());
		serviceController.getResultsOfProcessing(selectedJobId, debug);
	}

	/**
	 * Displays results data panel.
	 * @param resultData results of processing
	 */
	public void displayResultView(PDBScoreItem resultData)
	{
		doStatusPanelRefreshing = false;

		if((mainViewPort.getCenterPanel().getDisplayPanel() != null) &&
		   (mainViewPort.getCenterPanel().getDisplayPanel() instanceof ResultsPanel))
		{
			ResultsPanel resultsPanel = (ResultsPanel)mainViewPort.getCenterPanel().getDisplayPanel();
			resultsPanel.fillResultsPanel(resultData);
			resultsPanel.layout();
		}
		else
		{
			ResultsPanel resultsPanel = new ResultsPanel(this);
			resultsPanel.fillResultsGrid(resultData);
			mainViewPort.getCenterPanel().setDisplayPanel(resultsPanel);
			resultsPanel.resizeGrid();
		}

		mainViewPort.getMyJobsPanel().getMyJobsGrid().focus();
		Window.setTitle(CONSTANTS.window_title_results() + " - " + resultData.getPdbName());
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
			statusPanel = new StatusPanel(this);
			mainViewPort.getCenterPanel().setDisplayPanel(statusPanel);
		}

		if(statusPanel != null)
		{
			statusPanel.fillData(statusData);
		}

		if((statusData.getStatus() != null) && (statusData.getStatus().equals(StatusOfJob.RUNNING.getName())))
		{
			doStatusPanelRefreshing = true;
		}
		else
		{
			doStatusPanelRefreshing = false;
		}

		mainViewPort.getCenterPanel().layout();

		mainViewPort.getMyJobsPanel().getMyJobsGrid().focus();
		Window.setTitle(CONSTANTS.window_title_processing() + " - " + statusData.getInput());
	}

	public void getCurrentStatusData(boolean debug)
	{
		serviceController.getCurrentStatusData(selectedJobId, debug);
	}

	public void getJobsForCurrentSession()
	{
		if(canRefreshMyJobs)
		{
			canRefreshMyJobs = false;
			serviceController.getJobsForCurrentSession();
		}
	}

	public void getInterfaceResidues(int interfaceId)
	{
		mainViewPort.displayInterfacesWindow(interfaceId);

		if((residuesForInterface != null) &&
		   (residuesForInterface.containsKey(interfaceId)))
		{
			setInterfacesResiduesWindowData(residuesForInterface.get(interfaceId));
		}
		else
		{
			serviceController.getInterfaceResidues(pdbScoreItem.getJobId(),
												   pdbScoreItem.getInterfaceItem(interfaceId - 1).getUid(),
												   interfaceId);
		}
	}

	public void setJobs(List<ProcessingInProgressData> statusData) {
		mainViewPort.getMyJobsPanel().setJobs(statusData);
	}

	public void untieJobsFromSession() {
		serviceController.untieJobsFromSession();
	}

	public void getAllResidues(String jobId, int interfaceUid)
	{
		if(!GXT.isIE8)
		{
			serviceController.getAllResidues(jobId, interfaceUid);
		}
	}

	public void setSettings(ApplicationSettings settings) {
		this.settings = settings;
	}

	public ApplicationSettings getSettings() {
		return settings;
	}

	public void setPDBScoreItem(PDBScoreItem pdbScoreItem) {
		this.pdbScoreItem = pdbScoreItem;
	}

	public PDBScoreItem getPdbScoreItem() {
		return pdbScoreItem;
	}

	/**
	 * Retrieves main view.
	 * @return main view
	 */
	public MainViewPort getMainViewPort() {
		return mainViewPort;
	}

	/**
	 * Stars new job.
	 * @param runJobData run job data
	 */
	public void runJob(RunJobData runJobData) {
		serviceController.runJob(runJobData);
	}

	/**
	 * Stops job.
	 * @param jobToStop identifier of the job to stop
	 */
	public void stopJob(String jobToStop)
	{
		serviceController.stopJob(jobToStop, debug);
	}

	/**
	 * Removes job.
	 * @param jobToDelete identifier of the job to remove
	 */
	public void deleteJob(String jobToDelete)
	{
		if(jobToDelete.equals(selectedJobId))
		{
			mainViewPort.getMyJobsPanel().selectPrevious(jobToDelete);
		}

		serviceController.deleteJob(jobToDelete);
	}

	/**
	 * Auto refreshes jobs grid.
	 */
	public void runMyJobsAutoRefresh()
	{
		getJobsForCurrentSession();

		autoRefreshMyJobs = new Timer()
		{
			public void run()
			{
				if((doStatusPanelRefreshing) &&
					(selectedJobId != null) &&
					(!selectedJobId.equals("")))
				{
					getCurrentStatusData(debug);
				}
				else
				{
					getJobsForCurrentSession();
				}
			}
		};

		autoRefreshMyJobs.scheduleRepeating(10000);
	}

	/**
	 * Retrieves selected job identifier.
	 * @return job identifier
	 */
	public String getSelectedJobId() {
		return selectedJobId;
	}

	/**
	 * Sets currently selected job identifier.
	 * @param selectedJobId job identifier
	 */
	public void setSelectedJobId(String selectedJobId) {
		this.selectedJobId = selectedJobId;
	}

	/**
	 * Refreshes content of the status panel.
	 * @param statusData status data of the current job
	 */
	public void refreshStatusView(ProcessingInProgressData statusData)
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
	 * Sets number of submitted jobs.
	 * @param nrOfSubmissions number of submitted jobs
	 */
	public void setNrOfSubmissions(int nrOfSubmissions)
	{
		this.nrOfSubmissions = nrOfSubmissions;
	}

	/**
	 * Retrieves number of submitted jobs. This information is used in a case of using recaptcha protection to limit
	 * number of submissions.
	 * @return number of submitted jobs.
	 */
	public int getNrOfSubmissions()
	{
		return nrOfSubmissions;
	}

	/**
	 * Sets selected viewer type.
	 * @param selectedViewer viewer type
	 */
	public void setSelectedViewer(String selectedViewer)
	{
		this.selectedViewer = selectedViewer;
	}

	/**
	 * Starts selected viewer. Type of the viewer is determined based on the option selected in viewer selector.
	 * @param interfaceId identifier of the interface
	 */
	public void runViewer(String interfaceId)
	{
		if(selectedViewer.equals(MainController.CONSTANTS.viewer_jmol()))
		{
			showJmolViewer(interfaceId);
		}
		else if(selectedViewer.equals(MainController.CONSTANTS.viewer_local()))
		{
			downloadFileFromServer("interface", interfaceId);
		}
		else if(selectedViewer.equals(MainController.CONSTANTS.viewer_pse()))
		{
			downloadFileFromServer("pse", interfaceId);
		}
		else
		{
			showError("No viewer selected");
		}
	}

	/**
	 * Displays jmol viewer.
	 * @param interfaceNr interface identifier
	 */
	public void showJmolViewer(String interfaceNr)
	{
		String resultsLocation = settings.getResultsLocation();

		int size = windowHeight;
		if(size > windowWidth)
		{
			size = windowWidth;
		}

		openJmol(GWT.getHostPageBaseURL() + "Jmol.html",
				 GWT.getHostPageBaseURL() + "resources/jmol",
				 GWT.getHostPageBaseURL() + resultsLocation,
				 interfaceNr,
				 pdbScoreItem.getJobId(),
				 pdbScoreItem.getPdbName(),
				 size,
				 pdbScoreItem.getInterfaceItem(Integer.parseInt(interfaceNr) - 1).getJmolScript());
	}

	/*
	 * height and width should be set always - otherwise firefox is opening new tab ( and not window )
	 */
	/**
	 * Opens jmol viewer window.
	 * @param jmolPage page where jmol is going to be embedded
	 * @param jmolResources path to jmol resources
	 * @param url server url
	 * @param interfaceNr interface identifier
	 * @param filename name of the file
	 * @param size size of the window
	 * @param jmolScript jmol script which is going to be executed
	 */
	public native void openJmol(String jmolPage,
								String jmolResources,
								String url,
								String interfaceNr,
								String selectedJob,
								String filename,
								int size,
								String jmolScript) /*-{
		var jmolwindow = window.open(jmolPage, "Jmol", "status=no,width=" + size + ",height=" + size);
		jmolwindow.document.body.innerHTML = "";
		$wnd.jmolInitialize(jmolResources);
		$wnd.jmolSetCallback("language", "en");
		$wnd.jmolSetDocument(jmolwindow.document);
		$wnd.jmolApplet(size - 20, 'load ' + url + selectedJob + "/" + filename + "." + interfaceNr + '.pdb.gz;' + jmolScript);
	}-*/;

	/**
	 * Downloads file from the server.
	 * @param type type of the file to download
	 * @param interfaceId identifier of the interface
	 */
	public void downloadFileFromServer(String type, String interfaceId)
	{
		String fileDownloadServletUrl = GWT.getModuleBaseURL() + "fileDownload";
		fileDownloadServletUrl += "?type=" + type + "&id=" + pdbScoreItem.getJobId() + "&interface=" + interfaceId;
		Window.open(fileDownloadServletUrl, "", "");
	}

	/**
	 * Displays error.
	 * @param errorMessage message of the error
	 */
	public void showError(String errorMessage)
	{
		if(mainViewPort != null)
		{
			mainViewPort.showError(errorMessage);
		}
		else
		{
			Window.alert(errorMessage);
		}
	}

	/**
	 * Shows messagebox with provided message.
	 * @param title title of message
	 * @param message text of the message
	 */
	public void showMessage(String title, String message)
	{
		MessageBox infoMessageBox = MessageBox.info(title, message, new Listener<MessageBoxEvent>() {

			@Override
			public void handleEvent(MessageBoxEvent be) {
				mainViewPort.getMyJobsPanel().getMyJobsGrid().focus();
			}
		});

		infoMessageBox.setMinWidth(300);
		infoMessageBox.setMaxWidth(windowWidth);
	}

	/**
	 * Shows waiting messagebox.
	 * @param text
	 */
	public void showWaiting(String text)
	{
		mainViewPort.showWaiting(text);
	}

	/**
	 * Hides waiting messagebox.
	 */
	public void hideWaiting()
	{
		mainViewPort.hideWaiting();
	}

	/**
	 * Retrieves width of the window.
	 * @return width of the window
	 */
	public int getWindowWidth() {
		return windowWidth;
	}

	/**
	 * Sets width of the window.
	 * @param windowWidth width of the window
	 */
	public void setWindowWidth(int windowWidth) {
		this.windowWidth = windowWidth;
	}

	/**
	 * Retrieves height of the window.
	 * @return height of the window
	 */
	public int getWindowHeight() {
		return windowHeight;
	}

	/**
	 * Sets height of the window.
	 * @param windowHeight height of the window
	 */
	public void setWindowHeight(int windowHeight) {
		this.windowHeight = windowHeight;
	}

	/**
	 * Initializes main view.
	 */
	public void setMainView()
	{
		mainViewPort = new MainViewPort(this);
		RootPanel.get().add(mainViewPort);
	}

	/**
	 * Stops automated refreshing of jobs grid.
	 */
	public void stopMyJobsAutoRefresh()
	{
		autoRefreshMyJobs.cancel();
	}

	/**
	 * Updates text of the status label.
	 * @param message information to display
	 * @param isError flag pointing whether message is error
	 */
	public void updateStatusLabel(String message, boolean isError)
	{
		if((mainViewPort != null) && (mainViewPort.getBottomPanel() != null))
		{
			mainViewPort.getBottomPanel().updateStatusMessage(message, isError);
		}
		else
		{
			showError(message);
		}
	}

	/**
	 * Sets resize flag for all windows.
	 * @param resizeWindow flag pointing whether window should be resized
	 */
	public void setResizeWindows(boolean resizeWindow)
	{
		if(mainViewPort.getInterfacesResiduesWindow() != null)
		{
			mainViewPort.getInterfacesResiduesWindow().setResizeWindow(true);
		}

		if(mainViewPort.getAlignmentsWindow() != null)
		{
			mainViewPort.getAlignmentsWindow().setResizeWindow(true);
		}

		if(mainViewPort.getAboutWindow() != null)
		{
			mainViewPort.getAboutWindow().setResizeWindow(true);
		}
	}

	/**
	 * Retrieves information about user browser.
	 * @return information about browser of the user
	 */
	public native static String getUserAgent() /*-{
		return navigator.userAgent.toLowerCase();
	}-*/;

	/**
	 * Resizes results grid.
	 */
	public void resizeResultsGrid()
    {
		if((mainViewPort != null) &&
           (mainViewPort.getCenterPanel() != null) &&
           (mainViewPort.getCenterPanel().getDisplayPanel() != null) &&
           (mainViewPort.getCenterPanel().getDisplayPanel() instanceof ResultsPanel))
           {
		    	((ResultsPanel)mainViewPort.getCenterPanel().getDisplayPanel()).resizeGrid();
           }
    }

	/**
	 * Cleans content of central panel.
	 */
	public void cleanCenterPanel()
	{
		mainViewPort.getCenterPanel().removeAll();
		mainViewPort.getCenterPanel().setDisplayPanel(null);
	}

	/**
	 * Sets data for interface residues items window.
	 * @param result interface residues data
	 */
	public void setInterfacesResiduesWindowData(
			final HashMap<Integer, List<InterfaceResidueItem>> result)
	{
		Scheduler.get().scheduleDeferred(new ScheduledCommand() {
			@Override
			public void execute() {
				if(result.containsKey(1))
				{					
					mainViewPort.getInterfacesResiduesWindow().getInterfacesResiduesPanel().getFirstStructurePanelSummary().fillResiduesGrid();
					mainViewPort.getInterfacesResiduesWindow().getInterfacesResiduesPanel().getFirstStructurePanel()
							.fillResiduesGrid(result.get(1));
					//mainViewPort.getInterfacesResiduesWindow().getInterfacesResiduesPanel().getFirstStructureBottomContainer().fillScoresLabels();
					mainViewPort.getInterfacesResiduesWindow().getInterfacesResiduesPanel().getFirstStructurePanel().applyFilter(false);
				}

				if(result.containsKey(2))
				{
					mainViewPort.getInterfacesResiduesWindow().getInterfacesResiduesPanel().getSecondStructurePanelSummary().fillResiduesGrid();
					mainViewPort.getInterfacesResiduesWindow().getInterfacesResiduesPanel().getSecondStructurePanel()
							.fillResiduesGrid(result.get(2));
					//mainViewPort.getInterfacesResiduesWindow().getInterfacesResiduesPanel().getSecondStructureBottomContainer().fillScoresLabels();
					mainViewPort.getInterfacesResiduesWindow().getInterfacesResiduesPanel().getSecondStructurePanel().applyFilter(false);					
				}
			}
		});
	}

	/**
	 * Retrieves list of interface residues.
	 * @return interface residues
	 */
	public InterfaceResiduesItemsList getInterfaceResiduesItemsList()
	{
		return residuesForInterface;
	}

	/**
	 * Sets residues list for interface.
	 * @param residuesForInterface residues list
	 */
	public void setResiduesForInterface(InterfaceResiduesItemsList residuesForInterface)
	{
		this.residuesForInterface = residuesForInterface;
	}

	/**
	 * Cleans interface residues.
	 */
	public void cleanResiduesForInterface()
	{
		if(residuesForInterface != null)
		{
			residuesForInterface.clear();
			residuesForInterface = null;
		}
	}

	public void setCanRefreshMyJobs()
	{
		this.canRefreshMyJobs = true;
	}

	/**
	 * Displays alignments window.
	 * @param homologsInfoItem homologs info item
	 * @param xPosition left corner
	 * @param yPosition top corner
	 */
	public void showAlignments(HomologsInfoItem homologsInfoItem,
							   int xPosition,
							   int yPosition)
	{
		mainViewPort.displayAlignmentsWindow(homologsInfoItem,
											 xPosition,
											 yPosition);
	}

	/**
	 * Displays about window.
	 */
	public void showAbout()
	{
		mainViewPort.displayAboutWindow();
	}
}
