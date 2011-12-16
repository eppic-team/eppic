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
 * This is the main application controller
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

	public void loadSettings() 
	{
		serviceController.loadSettings();
	}

	public void displayView(String token)
	{
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

	public void displayInputView()
	{
		doStatusPanelRefreshing = false;

		InputDataPanel inputDataPanel = new InputDataPanel(this);
		mainViewPort.getCenterPanel().setDisplayPanel(inputDataPanel);
	}

	public void displayResults(boolean debug)
	{
		mainViewPort.mask(CONSTANTS.defaultmask());
//		mainViewPort.showWaiting(CONSTANTS.defaultmask());
		serviceController.getResultsOfProcessing(selectedJobId, debug);
	}

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

	public MainViewPort getMainViewPort() {
		return mainViewPort;
	}

	public void runJob(RunJobData runJobData) {
		serviceController.runJob(runJobData);
	}
	
	public void stopJob(String jobToStop) 
	{
		serviceController.stopJob(jobToStop, debug);
	}
	
	public void deleteJob(String jobToDelete) 
	{
		if(jobToDelete.equals(selectedJobId))
		{
			mainViewPort.getMyJobsPanel().selectPrevious(jobToDelete);
		}
		
		serviceController.deleteJob(jobToDelete);
	}

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

	public String getSelectedJobId() {
		return selectedJobId;
	}

	public void setSelectedJobId(String selectedJobId) {
		this.selectedJobId = selectedJobId;
	}

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
	
	public void setNrOfSubmissions(int nrOfSubmissions)
	{
		this.nrOfSubmissions = nrOfSubmissions;
	}
	
	public int getNrOfSubmissions()
	{
		return nrOfSubmissions;
	}

	public void setSelectedViewer(String selectedViewer)
	{
		this.selectedViewer = selectedViewer;
	}
	
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
	
	public void showJmolViewer(String interfaceNr) 
	{
//		String url = GWT.getModuleBaseURL() + "crkresults/";
//		String url = GWT.getHostPageBaseURL() + settings.getResultsLocation();
//		
//		if(!url.endsWith("/"))
//		{
//			url += "/";
//		}
//		
//		Window.alert(url);
		
		String url = settings.getResultsLocation();
		
		int size = windowHeight;
		if(size > windowWidth)
		{
			size = windowWidth;
		}
		
		openJmol(url, 
				 interfaceNr, 
				 pdbScoreItem.getJobId(),
				 pdbScoreItem.getPdbName(),
				 size,
				 pdbScoreItem.getInterfaceItem(Integer.parseInt(interfaceNr) - 1).getJmolScript());
	}
	
	/*
	 * height and width should be set always - otherwise firefox is opening new tab ( and not window )
	 */
	public native void openJmol(String url, 
								String interfaceNr, 
								String selectedJob,
								String filename,
								int size,
								String jmolScript) /*-{
		var jmolwindow = window.open("", "Jmol", "status=no,width=" + size + ",height=" + size);
		jmolwindow.document.body.innerHTML = "";
		$wnd.jmolInitialize("resources/jmol");
		$wnd.jmolSetCallback("language", "en");
		$wnd.jmolSetDocument(jmolwindow.document);
		$wnd.jmolApplet(size - 20, 'load ' + url + selectedJob + "/" + filename + "." + interfaceNr + '.rimcore.pdb;' + jmolScript);
	}-*/;
	
	public void downloadFileFromServer(String type, String interfaceId)
	{
		String fileDownloadServletUrl = GWT.getModuleBaseURL() + "fileDownload";
		fileDownloadServletUrl += "?type=" + type + "&id=" + pdbScoreItem.getJobId() + "&interface=" + interfaceId;
		Window.open(fileDownloadServletUrl, "", "");
//		RequestBuilder builder = new RequestBuilder(RequestBuilder.GET, URL.encode(fileDownloadServletUrl));
//
//		try 
//		{
//			Request request = builder.sendRequest(null, new RequestCallback() 
//			{
//				public void onError(Request request, Throwable exception) 
//		    	{
//					showError("Error during downloading file from server: " + exception.getMessage());
//		    	}
//
//		    	public void onResponseReceived(Request request, Response response) 
//		    	{
//		    		if (200 == response.getStatusCode()) 
//		    		{
//		    			Window.alert(response.getText());
//		    		}
//		    		else
//		    		{
//		    			showError("Could not download file from server: " + response.getStatusText());
//		    		}
//		    	}
//			});
//		} 
//		catch (RequestException e) 
//		{
//			showError("Error during downloading file from server: " + e.getMessage());
//		}
	}
	
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
	
	public void showWaiting(String text)
	{
		mainViewPort.showWaiting(text);
	}
	
	public void hideWaiting()
	{
		mainViewPort.hideWaiting();
	}
	
	public int getWindowWidth() {
		return windowWidth;
	}

	public void setWindowWidth(int windowWidth) {
		this.windowWidth = windowWidth;
	}

	public int getWindowHeight() {
		return windowHeight;
	}

	public void setWindowHeight(int windowHeight) {
		this.windowHeight = windowHeight;
	}
	
	public void setMainView()
	{
		mainViewPort = new MainViewPort(this);
		RootPanel.get().add(mainViewPort);
	}
	
	public void stopMyJobsAutoRefresh()
	{
		autoRefreshMyJobs.cancel();
	}
	
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
		
//		if(isError)
//		{
//			mainViewPort.showError(message);
//		}
	}

	public void setResizeWindows(boolean resizeWindow) 
	{
		mainViewPort.getInterfacesResiduesWindow().setResizeWindow(true);
		mainViewPort.getAlignmentsWindow().setResizeWindow(true);
	}
	
	public native static String getUserAgent() /*-{
		return navigator.userAgent.toLowerCase();
	}-*/;
	
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
	
	public void resizeScoresGrid()
    {
//		if((mainViewPort != null) &&
//           (mainViewPort.getCenterPanel() != null) &&
//           (mainViewPort.getCenterPanel().getDisplayPanel() != null) &&
//           (mainViewPort.getCenterPanel().getDisplayPanel() instanceof ResultsPanel))
//           {
//		    	((ResultsPanel)mainViewPort.getCenterPanel().getDisplayPanel()).resizeScoresGrid();
//           }
    }

	public void cleanCenterPanel() 
	{
		mainViewPort.getCenterPanel().removeAll();
		mainViewPort.getCenterPanel().setDisplayPanel(null);		
	}

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
					mainViewPort.getInterfacesResiduesWindow().getInterfacesResiduesPanel().getFirstStructurePanel().applyFilter(false);
				}
				
				if(result.containsKey(2))
				{
					mainViewPort.getInterfacesResiduesWindow().getInterfacesResiduesPanel().getSecondStructurePanelSummary().fillResiduesGrid();
					mainViewPort.getInterfacesResiduesWindow().getInterfacesResiduesPanel().getSecondStructurePanel()
							.fillResiduesGrid(result.get(2));
					mainViewPort.getInterfacesResiduesWindow().getInterfacesResiduesPanel().getSecondStructurePanel().applyFilter(false);
				}				
			}
		});
		
//		mainViewPort.getInterfacesResiduesWindow().getInterfacesResiduesPanel().resizeResiduesPanels();		
	}

	public InterfaceResiduesItemsList getInterfaceResiduesItemsList() 
	{
		return residuesForInterface;
	}

	public void setResiduesForInterface(InterfaceResiduesItemsList residuesForInterface) 
	{
		this.residuesForInterface = residuesForInterface;
	}
	
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
	
	public void showAlignments(HomologsInfoItem homologsInfoItem,
							   int xPosition,
							   int yPosition) 
	{
		mainViewPort.displayAlignmentsWindow(homologsInfoItem,
											 xPosition,
											 yPosition);
	}
}
