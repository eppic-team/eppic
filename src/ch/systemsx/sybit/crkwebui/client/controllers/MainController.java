package ch.systemsx.sybit.crkwebui.client.controllers;

import java.util.List;

import model.PDBScoreItem;
import ch.systemsx.sybit.crkwebui.client.gui.InputDataPanel;
import ch.systemsx.sybit.crkwebui.client.gui.InterfacesResiduesWindow;
import ch.systemsx.sybit.crkwebui.client.gui.MainViewPort;
import ch.systemsx.sybit.crkwebui.client.gui.ResultsPanel;
import ch.systemsx.sybit.crkwebui.client.gui.StatusPanel;
import ch.systemsx.sybit.crkwebui.shared.model.ApplicationSettings;
import ch.systemsx.sybit.crkwebui.shared.model.ProcessingInProgressData;
import ch.systemsx.sybit.crkwebui.shared.model.RunJobData;

import com.extjs.gxt.ui.client.widget.MessageBox;
import com.extjs.gxt.ui.client.widget.Viewport;
import com.google.gwt.core.client.GWT;
import com.google.gwt.http.client.Request;
import com.google.gwt.http.client.RequestBuilder;
import com.google.gwt.http.client.RequestCallback;
import com.google.gwt.http.client.RequestException;
import com.google.gwt.http.client.Response;
import com.google.gwt.http.client.URL;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.RootPanel;

public class MainController 
{
	public static final AppProperties CONSTANTS = (AppProperties) GWT.create(AppProperties.class);
	
	private MainViewPort mainViewPort;
	
	private int windowWidth = Window.getClientWidth();
	private int windowHeight = Window.getClientHeight();

	private ServiceController serviceController;

	private ApplicationSettings settings;

	private PDBScoreItem pdbScoreItem;

	private String selectedJobId;

	private Timer autoRefreshMyJobs;
	
	private boolean doStatusPanelRefreshing = false;
	
	private int nrOfSubmissions = 0;
	
	private String selectedViewer = "Jmol";
	
	private Timer loadingTimer;
	
	public MainController(Viewport viewport) 
	{
		this.serviceController = new ServiceControllerImpl(this);
	}

	public void test(String testValue) 
	{
		serviceController.test(testValue);
	}
	
	public void loadSettings() 
	{
		serviceController.loadSettings();
	}

	public void displayView(String token)
	{
		if ((token != null) && (token.length() > 3) && (token.startsWith("id"))) 
		{
			selectedJobId = token.substring(3);
			displayResults();
		}
		// else if((token != null) &&
		// (token.length() > 10) &&
		// (token.startsWith("interface")))
		// {
		// String selectedInterface = token.substring(9, token.indexOf("/"));
		// String selectedId = token.substring(token.indexOf("/") + 1);
		// displayResults(selectedId);
		// }
		else
		{
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

	public void displayResults()
	{
		serviceController.getResultsOfProcessing(selectedJobId);
	}

	public void displayResultView(PDBScoreItem resultData) 
	{
		doStatusPanelRefreshing = false;
		
		ResultsPanel resultsPanel = new ResultsPanel(this, resultData);
		resultsPanel.setResults(resultData);
		mainViewPort.getCenterPanel().setDisplayPanel(resultsPanel);
	}

	public void displayStatusView(ProcessingInProgressData statusData) 
	{
		StatusPanel statusPanel = new StatusPanel(this);
		mainViewPort.getCenterPanel().setDisplayPanel(statusPanel);
		
		statusPanel.fillData(statusData);
		
		if((statusData.getStatus() != null) && (statusData.getStatus().equals("Running")))
		{
			doStatusPanelRefreshing = true;
		}
		else
		{
			doStatusPanelRefreshing = false;
		}
		
		mainViewPort.getCenterPanel().layout();
	}
	
	public void getCurrentStatusData()
	{
		serviceController.getCurrentStatusData(selectedJobId);
	}

	public void getJobsForCurrentSession() {
		serviceController.getJobsForCurrentSession();
	}

	public void getInterfaceResidues(int interfaceId) 
	{
		mainViewPort.displayInterfacesWindow();
		
		serviceController.getInterfaceResidues(selectedJobId, interfaceId);
	}

	public void setJobs(List<ProcessingInProgressData> statusData) {
		mainViewPort.getMyJobsPanel().setJobs(statusData);
	}

	public void untieJobsFromSession() {
		serviceController.untieJobsFromSession();
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
	
	public void killJob(String selectedId) {
		serviceController.killJob(selectedId);
	}

	public void runMyJobsAutoRefresh() 
	{
		getJobsForCurrentSession();
		
		autoRefreshMyJobs = new Timer() 
		{
			public void run() 
			{
				getJobsForCurrentSession();
				
				if((doStatusPanelRefreshing) && 
					(selectedJobId != null) && 
					(!selectedJobId.equals("")))
				{
					getCurrentStatusData();
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
		if(mainViewPort.getCenterPanel().getDisplayPanel() instanceof StatusPanel)
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
		if(selectedViewer.equals("Jmol"))
		{
			showJmolViewer(interfaceId);
		}
		else if(selectedViewer.equals("Local"))
		{
			downloadFileFromServer("interface", interfaceId);
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
				 selectedJobId,
				 pdbScoreItem.getPdbName(),
				 size);
	}
	
	/*
	 * height and width should be set always - otherwise firefox is opening new tab ( and not window )
	 */
	public native void openJmol(String url, 
								String interfaceNr, 
								String selectedJob,
								String filename,
								int size) /*-{
		var jmolwindow = window.open("", "Jmol", "status=yes,width=" + size + ",height=" + size);
		$wnd.jmolInitialize("resources/jmol");
		$wnd.jmolSetCallback("language", "en");
		$wnd.jmolSetDocument(jmolwindow.document);
		$wnd.jmolApplet(size - 20,"cartoon on" + 'load ' + url + selectedJob + "/" + filename + "." + interfaceNr + '.rimcore.pdb');
	}-*/;
	
	public void downloadFileFromServer(String type, String interfaceId)
	{
		String fileDownloadServletUrl = GWT.getModuleBaseURL() + "fileDownload";
		fileDownloadServletUrl += "?type=" + type + "&id=" + selectedJobId + "&interface=" + interfaceId;
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
	
	public void showError(String errorMessage) {
//		mainViewPort.showError(errorMessage);
		Window.alert(errorMessage);
	}
	
	public void showMessage(String title, String message)
	{
		MessageBox infoMessageBox = MessageBox.info(title, message, null);
		infoMessageBox.setMinWidth(300);
		infoMessageBox.setMaxWidth(windowWidth);
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
	
	public void showWaiting(String text)
	{
		mainViewPort.showWaiting(text);
	}
	
	public void hideWaiting()
	{
		mainViewPort.hideWaiting();
	}
	
	public void updateStatusLabel(String message, boolean isError)
	{
		mainViewPort.getBottomPanel().updateStatusMessage(message, isError);
		
//		if(isError)
//		{
//			mainViewPort.showError(message);
//		}
	}
	
//	buttonBar.add(new Button("Wait", new SelectionListener<ButtonEvent>() {  
//	      public void componentSelected(ButtonEvent ce) {  
//	        final MessageBox box = MessageBox.wait("Progress",  
//	            "Saving your data, please wait...", "Saving...");  
//	        Timer t = new Timer() {  
//	          @Override  
//	          public void run() {  
//	            Info.display("Message", "Your fake data was saved", "");  
//	            box.close();  
//	          }  
//	        };  
//	        t.schedule(5000);  
//	      }  
//	    }));  
	
}
