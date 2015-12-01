package ch.systemsx.sybit.crkwebui.client.main.controllers;

import java.util.List;

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
import ch.systemsx.sybit.crkwebui.client.commons.events.SearchResultsDataRetrievedEvent;
import ch.systemsx.sybit.crkwebui.client.commons.events.ShowAboutEvent;
import ch.systemsx.sybit.crkwebui.client.commons.events.ShowAlignmentsEvent;
import ch.systemsx.sybit.crkwebui.client.commons.events.ShowAssembliesEvent;
import ch.systemsx.sybit.crkwebui.client.commons.events.ShowErrorEvent;
import ch.systemsx.sybit.crkwebui.client.commons.events.ShowHomologsEvent;
import ch.systemsx.sybit.crkwebui.client.commons.events.ShowInterfaceResiduesEvent;
import ch.systemsx.sybit.crkwebui.client.commons.events.ShowInterfacesOfAssemblyDataEvent;
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
import ch.systemsx.sybit.crkwebui.client.commons.gui.labels.EppicLabel;
import ch.systemsx.sybit.crkwebui.client.commons.gui.panels.DisplayPanel;
import ch.systemsx.sybit.crkwebui.client.commons.gui.windows.IFramePanel;
import ch.systemsx.sybit.crkwebui.client.commons.handlers.ApplicationInitHandler;
import ch.systemsx.sybit.crkwebui.client.commons.handlers.HideAllWindowsHandler;
import ch.systemsx.sybit.crkwebui.client.commons.handlers.HideWaitingHandler;
import ch.systemsx.sybit.crkwebui.client.commons.handlers.InterfaceResiduesDataRetrievedHandler;
import ch.systemsx.sybit.crkwebui.client.commons.handlers.RefreshStatusDataHandler;
import ch.systemsx.sybit.crkwebui.client.commons.handlers.SearchResultsDataRetrievedHandler;
import ch.systemsx.sybit.crkwebui.client.commons.handlers.ShowAboutHandler;
import ch.systemsx.sybit.crkwebui.client.commons.handlers.ShowAlignmentsHandler;
import ch.systemsx.sybit.crkwebui.client.commons.handlers.ShowErrorHandler;
import ch.systemsx.sybit.crkwebui.client.commons.handlers.ShowHomologsHandler;
import ch.systemsx.sybit.crkwebui.client.commons.handlers.ShowInterfaceResiduesWindowHandler;
import ch.systemsx.sybit.crkwebui.client.commons.handlers.ShowInterfacesOfAssemblyDataHandler;
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
import ch.systemsx.sybit.crkwebui.client.input.gui.panels.InputDataPanel;
import ch.systemsx.sybit.crkwebui.client.main.gui.panels.MainViewPort;
import ch.systemsx.sybit.crkwebui.client.main.gui.panels.MainViewScrollable;
import ch.systemsx.sybit.crkwebui.client.results.gui.panels.ResultsGridPanel;
import ch.systemsx.sybit.crkwebui.client.results.gui.panels.ResultsPanel;
import ch.systemsx.sybit.crkwebui.client.results.gui.panels.StatusPanel;
import ch.systemsx.sybit.crkwebui.client.search.gui.panels.SearchPanel;
import ch.systemsx.sybit.crkwebui.shared.model.Assembly;
import ch.systemsx.sybit.crkwebui.shared.model.InterfaceCluster;
import ch.systemsx.sybit.crkwebui.shared.model.PDBSearchResult;
import ch.systemsx.sybit.crkwebui.shared.model.PdbInfo;
import ch.systemsx.sybit.crkwebui.shared.model.ProcessingInProgressData;
import ch.systemsx.sybit.shared.model.StatusOfJob;

import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.Scheduler;
import com.google.gwt.core.client.Scheduler.ScheduledCommand;
import com.google.gwt.i18n.client.NumberFormat;
import com.google.gwt.user.client.History;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.gwt.user.client.ui.RootPanel;
import com.sencha.gxt.core.client.util.Margins;
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
	
	public void setExperimentalInfo(){
		PdbInfo resultsData = ApplicationContext.getPdbInfo();
		String html_experiment_info = "";
		if(resultsData !=null){
			html_experiment_info += "<span class='eppic-general-info-label-new'>" + AppPropertiesManager.CONSTANTS.info_panel_experiment() + "</span> <span class='eppic-general-info-label-value-new'>" + resultsData.getExpMethod() + "</span>";
			
			//if experimental type is "NMR" don't display Space Group
			if(ApplicationContext.getPdbInfo().getExpMethod()!= null && !ApplicationContext.getPdbInfo().getExpMethod().contains("NMR"))
				html_experiment_info += "&nbsp;&nbsp;&nbsp;&nbsp;<span class='eppic-general-info-label-new'>" + AppPropertiesManager.CONSTANTS.info_panel_spacegroup() + "</span> <span class='eppic-general-info-label-value-new'>" + resultsData.getSpaceGroup() + "</span>";
			
			// if resolution >=99 don't display Resolution
			double resolution = resultsData.getResolution();
			if(resolution < 99)
				html_experiment_info += "&nbsp;&nbsp;&nbsp;&nbsp;<span class='eppic-general-info-label-new'>" + AppPropertiesManager.CONSTANTS.info_panel_resolution() + "</span> <span class='eppic-general-info-label-value-new'>" + NumberFormat.getFormat("0.0").format(resultsData.getResolution()) + "</span>";
			
			//if Rfree>=1 don't display Rfree
			double rfree = Double.parseDouble(NumberFormat.getFormat("0.00").format(resultsData.getRfreeValue()));
			if(rfree < 1)
				html_experiment_info += "&nbsp;&nbsp;&nbsp;&nbsp;<span class='eppic-general-info-label-new'>" + AppPropertiesManager.CONSTANTS.info_panel_rfree() + "</span> <span class='eppic-general-info-label-value-new'>" +  NumberFormat.getFormat("0.00").format(resultsData.getRfreeValue()) + "</span>";
		}
		ResultsPanel.headerPanel.experimentinfo.setHTML(html_experiment_info);
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

				int num_interfaces = 0;
				List<InterfaceCluster> clusters = ApplicationContext.getPdbInfo().getInterfaceClusters();
				for(InterfaceCluster ic : clusters){
					num_interfaces += ic.getInterfaces().size();
				}
				if(ApplicationContext.getSelectedViewType() == ResultsPanel.ASSEMBLIES_VIEW){
					///show the assemblies view 
					displayResultView(event.getPdbScoreItem(), ResultsPanel.ASSEMBLIES_VIEW); //the new default view
					ResultsPanel.headerPanel.pdbIdentifierPanel.informationLabel.setHTML("Assembly Analysis of: ");
					ResultsPanel.headerPanel.pdbIdentifierPanel.pdbNameLabel.setHTML("<a target='_blank' href='http://www.pdb.org/pdb/explore/explore.do?structureId="+ApplicationContext.getPdbInfo().getPdbCode()+"'>"+ApplicationContext.getPdbInfo().getPdbCode()+"</a>");
					ResultsPanel.informationPanel.assemblyInfoPanel.setHeadingHtml("General Information " + ApplicationContext.getPdbInfo().getPdbCode());											
					ResultsPanel.informationPanel.assemblyInfoPanel.assembly_info.setHTML("<table cellpadding=0 cellspacing=0><tr><td width='150px'><span class='eppic-general-info-label-new'>Assemblies</span></td><td><span class='eppic-general-info-label-value-new'>" + ApplicationContext.getPdbInfo().getAssemblies().size() + "</span></td></tr><tr><td><span class='eppic-general-info-label-new'>Interfaces</span></td><td><span class='eppic-general-info-label-value-new'><a href='" + GWT.getHostPageBaseURL() + "#interfaces/"+ApplicationContext.getPdbInfo().getPdbCode()+"'>" + num_interfaces + "</a></span></td></tr><tr><td><span class='eppic-general-info-label-new'>Interface clusters</span></td><td><span class='eppic-general-info-label-value-new'><a href='" + GWT.getHostPageBaseURL() + "#clusters/"+ApplicationContext.getPdbInfo().getPdbCode()+"'>" + ApplicationContext.getPdbInfo().getInterfaceClusters().size()+"</a></span></td></tr></table>");	
					ResultsPanel.informationPanel.removeTopologyPanel(ApplicationContext.getPdbInfo());
					setExperimentalInfo();					
				}else if(ApplicationContext.getSelectedViewType() == ResultsPanel.INTERFACES_VIEW){
					//show the interfaces view
					displayResultView(event.getPdbScoreItem(), ResultsPanel.INTERFACES_VIEW);
					if(ApplicationContext.getSelectedAssemblyId() == -1){
						ResultsPanel.headerPanel.pdbIdentifierPanel.informationLabel.setHTML("All Interfaces of: ");
						ResultsPanel.headerPanel.pdbIdentifierPanel.pdbNameLabel.setHTML("<a target='_blank' href='http://www.pdb.org/pdb/explore/explore.do?structureId="+event.getPdbScoreItem().getPdbCode()+"'>"+event.getPdbScoreItem().getPdbCode()+"</a>");
						ResultsPanel.informationPanel.assemblyInfoPanel.setHeadingHtml("General Information");				
						//ResultsPanel.informationPanel.assemblyInfoPanel.assembly_info.setHTML("<table cellpadding=0 cellspacing=0><tr><td width='150px'><span class='eppic-general-info-label-new'>Assemblies</span></td><td><span class='eppic-general-info-label-value-new'>" + ApplicationContext.getPdbInfo().getAssemblies().size() + "</span></td></tr><tr><td><span class='eppic-general-info-label-new'>Interfaces</span></td><td><span class='eppic-general-info-label-value-new'>" + num_interfaces + "</span></td></tr><tr><td><span class='eppic-general-info-label-new'>Interface clusters</span></td><td><span class='eppic-general-info-label-value-new'>" + ApplicationContext.getPdbInfo().getInterfaceClusters().size()+"</span></td></tr></table>");
						//		assemblies_toolbar_link = new HTML("<a href='" + GWT.getHostPageBaseURL() + "#interfaces/"+ApplicationContext.getPdbInfo().getPdbCode()+"'>View All Interfaces</a>");
						
						if(History.getToken().contains("clusters"))
							ResultsPanel.informationPanel.assemblyInfoPanel.assembly_info.setHTML("<table cellpadding=0 cellspacing=0><tr><td width='150px'><span class='eppic-general-info-label-new'>Assemblies</span></td><td><span class='eppic-general-info-label-value-new'><a href='" + GWT.getHostPageBaseURL() + "#id/"+ApplicationContext.getPdbInfo().getPdbCode()+"'>" + ApplicationContext.getPdbInfo().getAssemblies().size() + "</a></span></td></tr><tr><td><span class='eppic-general-info-label-new'>Interfaces</span></td><td><span class='eppic-general-info-label-value-new'><a href='" + GWT.getHostPageBaseURL() + "#interfaces/"+ApplicationContext.getPdbInfo().getPdbCode()+"'>" + num_interfaces + "</a></span></td></tr><tr><td><span class='eppic-general-info-label-new'>Interface clusters</span></td><td><span class='eppic-general-info-label-value-new'>" + ApplicationContext.getPdbInfo().getInterfaceClusters().size()+"</span></td></tr></table>");
						if(History.getToken().contains("interfaces")){
							if(ApplicationContext.getSelectedAssemblyId() < 0){
								ResultsPanel.informationPanel.assemblyInfoPanel.assembly_info.setHTML("<table cellpadding=0 cellspacing=0><tr><td width='150px'><span class='eppic-general-info-label-new'>Assemblies</span></td><td><span class='eppic-general-info-label-value-new'><a href='" + GWT.getHostPageBaseURL() + "#id/"+ApplicationContext.getPdbInfo().getPdbCode()+"'>" + ApplicationContext.getPdbInfo().getAssemblies().size() + "</a></span></td></tr><tr><td><span class='eppic-general-info-label-new'>Interfaces</span></td><td><span class='eppic-general-info-label-value-new'>" + num_interfaces + "</span></td></tr><tr><td><span class='eppic-general-info-label-new'>Interface clusters</span></td><td><span class='eppic-general-info-label-value-new'><a href='" + GWT.getHostPageBaseURL() + "#clusters/"+ApplicationContext.getPdbInfo().getPdbCode()+"'>" + ApplicationContext.getPdbInfo().getInterfaceClusters().size()+"</a></span></td></tr></table>");
							}
							else{
								ResultsPanel.informationPanel.assemblyInfoPanel.assembly_info.setHTML("<table cellpadding=0 cellspacing=0><tr><td width='150px'><span class='eppic-general-info-label-new'>Assemblies</span></td><td><span class='eppic-general-info-label-value-new'><a href='" + GWT.getHostPageBaseURL() + "#id/"+ApplicationContext.getPdbInfo().getPdbCode()+"'>" + ApplicationContext.getPdbInfo().getAssemblies().size() + "</a></span></td></tr><tr><td><span class='eppic-general-info-label-new'>Interfaces</span></td><td><span class='eppic-general-info-label-value-new'>" + num_interfaces + "</span></td></tr><tr><td><span class='eppic-general-info-label-new'>Interface clusters</span></td><td><span class='eppic-general-info-label-value-new'><a href='" + GWT.getHostPageBaseURL() + "#clusters/"+ApplicationContext.getPdbInfo().getPdbCode()+"/" + ApplicationContext.getSelectedAssemblyId() + "/'>" + ApplicationContext.getPdbInfo().getInterfaceClusters().size()+"</a></span></td></tr></table>");
							}
						}	
						ResultsPanel.informationPanel.removeTopologyPanel(ApplicationContext.getPdbInfo());						
						setExperimentalInfo();
					}else{
						ResultsPanel.headerPanel.pdbIdentifierPanel.informationLabel.setHTML("Interface Analysis of: Assembly " + ApplicationContext.getSelectedAssemblyId() + " in ");
						ResultsPanel.headerPanel.pdbIdentifierPanel.pdbNameLabel.setHTML("<a target='_blank' href='http://www.pdb.org/pdb/explore/explore.do?structureId="+event.getPdbScoreItem().getPdbCode()+"'>"+event.getPdbScoreItem().getPdbCode()+"</a>");
						int assemblyID = ApplicationContext.getSelectedAssemblyId();
						List<Assembly> assemblies = ApplicationContext.getPdbInfo().getAssemblies();
						String assembly_string = "";
						for(Assembly a : assemblies){
							if(a.getId() == assemblyID){
								//do the color
								if (a.getPredictionString() != null && a.getPredictionString().equalsIgnoreCase("xtal"))
									assembly_string += "<table cellpadding=0 cellspacing=0><tr><td><span class='eppic-general-info-label-new'>Prediction</span></td><td>&nbsp;&nbsp;<span class='eppic-general-info-label-value-new' style='color:red'><b>" + a.getPredictionString() + "</b></span></td></tr>";
								if (a.getPredictionString() != null && a.getPredictionString().equalsIgnoreCase("bio"))
									assembly_string += "<table cellpadding=0 cellspacing=0><tr><td><span class='eppic-general-info-label-new'>Prediction</span></td><td>&nbsp;&nbsp;<span class='eppic-general-info-label-value-new' style='color:green'><b>" + a.getPredictionString() + "</b></span></td></tr>";
								assembly_string += "<tr><td width='150px'><span class='eppic-general-info-label-new'>Macromolecular Size</span></td><td>&nbsp;&nbsp;<span class='eppic-general-info-label-value-new'>" + a.getMmSizeString() + "</span></td></tr>";
								assembly_string += "<tr><td><span class='eppic-general-info-label-new'>Stoichiometry</span></td><td>&nbsp;&nbsp;<span class='eppic-general-info-label-value-new'>" + a.getStoichiometryString() + "</span></td></tr>";
								assembly_string += "<tr><td><span class='eppic-general-info-label-new'>Symmetry</span></td><td>&nbsp;&nbsp;<span class='eppic-general-info-label-value-new'>" + a.getSymmetryString() + "</span></td></tr></table>";
								break;
							}
						}
						ResultsPanel.informationPanel.assemblyInfoPanel.assembly_info.setHTML(assembly_string);
						ResultsPanel.informationPanel.assemblyInfoPanel.setHeadingHtml("Assembly " + assemblyID + " of " + assemblies.size());
						ResultsPanel.informationPanel.addTopologyPanel(ApplicationContext.getPdbInfo());						
						setExperimentalInfo();	
					}
				}
			}
		});
		
		EventBusManager.EVENT_BUS.addHandler(SearchResultsDataRetrievedEvent.TYPE, new SearchResultsDataRetrievedHandler() {
			
			@Override
			public void onSearchResultsDataRetrieved(final SearchResultsDataRetrievedEvent event) {
				Scheduler.get().scheduleDeferred(new ScheduledCommand() {
					@Override
					public void execute() 
					{
						displaySearchView(event.getPdbCode(), event.getChain(), event.getResults());
					}
				});
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
				mainViewPort.displayHomologsWindow(event.getChainCluster(),
						event.getJobId(), 
						event.getPdbInfo(),
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
														  ApplicationContext.getPdbInfo(),
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
		token = token.replaceAll(" ", "");
		EventBusManager.EVENT_BUS.fireEvent(new HideAllWindowsEvent());
		EventBusManager.EVENT_BUS.fireEvent(new ShowTopPanelSearchBoxEvent());
		if ((token != null) && (token.length() > 3) && (token.startsWith("id"))) //main results screen - show list of assemblies
		{
			Window.setTitle(AppPropertiesManager.CONSTANTS.window_title_loading());
			ApplicationContext.setSelectedJobId(token.substring(3));
			ApplicationContext.setSelectedViewType(ResultsPanel.ASSEMBLIES_VIEW);
			ApplicationContext.setSelectedAssemblyId(-1);
			displayResults();
		}
		else if ((token != null) && (token.length() > 10) && (token.startsWith("interfaces"))) //show list of interfaces belonging to assembly x
		{
			ApplicationContext.setSelectedViewType(ResultsPanel.INTERFACES_VIEW);
			Window.setTitle(AppPropertiesManager.CONSTANTS.window_title_loading());
			ApplicationContext.setSelectedJobId(token.substring(11,15));
			int assemblyId = -1;
			String idString = token.substring(16,token.length());
			try {
				assemblyId = Integer.parseInt(idString);
			} catch (Exception e) {}
			if(assemblyId > 0)
				ApplicationContext.setSelectedAssemblyId(assemblyId);
			displayResults();
			ResultsGridPanel.clustersViewButton.setValue(false);
			ResultsGridPanel.clustersView.groupBy(null);
			ResultsGridPanel.clusterIdColumn.setHidden(true);
		}
		else if ((token != null) && (token.length() > 8) && (token.startsWith("clusters"))) //show list of interfaces (clusters view) belonging to assembly x
		{
			ApplicationContext.setSelectedViewType(ResultsPanel.INTERFACES_VIEW);
			Window.setTitle(AppPropertiesManager.CONSTANTS.window_title_loading());
			ApplicationContext.setSelectedJobId(token.substring(9,13));
			int assemblyId = -1;
			String idString = token.substring(14,token.length());
			try {
				assemblyId = Integer.parseInt(idString);
			} catch (Exception e) {}
			if(assemblyId > 0)
				ApplicationContext.setSelectedAssemblyId(assemblyId);
			displayResults();
			ResultsGridPanel.clusterIdColumn.setHidden(true);
			ResultsGridPanel.clustersViewButton.setValue(true);
			ResultsGridPanel.clustersView.groupBy(ResultsGridPanel.clusterIdColumn);
		}		
		else if ((token != null) && (token.length() > 14) && (token.startsWith("searchUniprot")))
		{
			Window.setTitle(AppPropertiesManager.CONSTANTS.window_title_searching());
			displayUniprotSearch(token.substring(14));
		}
		else if ((token != null) && (token.length() > 10) && (token.startsWith("searchPdb")))
		{
			Window.setTitle(AppPropertiesManager.CONSTANTS.window_title_searching());
			String[] tokenParts = token.split("/");
			displayPdbSearch(tokenParts[1], tokenParts[2]);
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
		else if((token != null) && (token.equals("statistics") || token.equals("!statistics")))
		{
			displayStatistics();
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
		IFramePanel helpPanel = new IFramePanel("help.html");
		displayPanelInCentralPanel(helpPanel);
	}
	
	/**
	 * Displays downloads panel.
	 */
	public void displayDownloads()
	{
		Window.setTitle(AppPropertiesManager.CONSTANTS.window_title_downloads());
		ApplicationContext.setSelectedJobId("");
		IFramePanel downloadsPanel = new IFramePanel("downloads.html");
		displayPanelInCentralPanel(downloadsPanel);
	}

	/**
	 * Displays releases panel.
	 */
	public void displayReleases()
	{
		Window.setTitle(AppPropertiesManager.CONSTANTS.window_title_releases());
		ApplicationContext.setSelectedJobId("");
		IFramePanel releasesPanel = new IFramePanel("releases.html");
		displayPanelInCentralPanel(releasesPanel);
	}
	
	/**
	 * Displays publication panel.
	 */
	public void displayPublications()
	{
		Window.setTitle(AppPropertiesManager.CONSTANTS.window_title_publications());
		ApplicationContext.setSelectedJobId("");
		IFramePanel publicationsPanel = new IFramePanel("publications.html");
		displayPanelInCentralPanel(publicationsPanel);
	}
	
	/**
	 * Displays faq panel.
	 */
	public void displayFAQ()
	{
		Window.setTitle(AppPropertiesManager.CONSTANTS.window_title_faq());
		ApplicationContext.setSelectedJobId("");
		IFramePanel faqPanel = new IFramePanel("faq.html");
		displayPanelInCentralPanel(faqPanel);
	}
	
	/**
	 * Displays statistics panel.
	 */
	public void displayStatistics()
	{
		Window.setTitle(AppPropertiesManager.CONSTANTS.window_title_statistics());
		ApplicationContext.setSelectedJobId("");
		IFramePanel faqPanel = new IFramePanel("statistics.html");
		displayPanelInCentralPanel(faqPanel);
	}
	
	public void displayPanelInCentralPanel(DisplayPanel panel) {
	    ApplicationContext.setDoStatusPanelRefreshing(false);
	    mainViewPort.getCenterPanel().setDisplayPanel(panel);
	}
	
	/**
	 * Retrieves results of processing for displaying central panel content.
	 */
	//the original method
	/*public void displayResults()
	{
		mainViewPort.mask(AppPropertiesManager.CONSTANTS.defaultmask());
		if(mainViewPort.getResultsPanel() != null){
			EventBusManager.EVENT_BUS.fireEvent(new UncheckClustersRadioEvent());
		}
		CrkWebServiceProvider.getServiceController().getResultsOfProcessing(ApplicationContext.getSelectedJobId());
	}*/
	
	public void displayResults()
	{
		mainViewPort.mask(AppPropertiesManager.CONSTANTS.defaultmask());
		CrkWebServiceProvider.getServiceController().getResultsOfProcessing(ApplicationContext.getSelectedJobId());
		if(ApplicationContext.getSelectedViewType() == ResultsPanel.ASSEMBLIES_VIEW){
			displayResultView(ApplicationContext.getPdbInfo(), ResultsPanel.ASSEMBLIES_VIEW);
		}
	}
	  

	/**
	 * Displays results data panel.
	 * @param resultData results of processing
	 */
	private void displayResultView(PdbInfo resultData, int viewType)
	{
		
		ApplicationContext.setDoStatusPanelRefreshing(false);

		ResultsPanel resultsPanel = null;
		PdbInfo newResultsData = resultData;
		if(ApplicationContext.getSelectedAssemblyId() != -1){
			Assembly assembly = newResultsData.getAssemblyById(ApplicationContext.getSelectedAssemblyId());
			if(assembly != null){
				List<InterfaceCluster> interfaceClusters = assembly.getInterfaceClusters();
				newResultsData.setInterfaceClusters(interfaceClusters);
			}
		}

		if((mainViewPort.getCenterPanel().getDisplayPanel() != null) &&
		   (mainViewPort.getCenterPanel().getDisplayPanel() instanceof ResultsPanel))
		{
			resultsPanel = (ResultsPanel)mainViewPort.getCenterPanel().getDisplayPanel();
			resultsPanel.fillResultsPanel(newResultsData, viewType);
		}
		else if(mainViewPort.getResultsPanel() != null)
		{
			resultsPanel = mainViewPort.getResultsPanel();
			resultsPanel.fillResultsPanel(newResultsData, viewType);
			mainViewPort.getCenterPanel().setDisplayPanel(resultsPanel);
			resultsPanel.resizeContent();
		}
		else
		{
			resultsPanel = new ResultsPanel(newResultsData, viewType);
			resultsPanel.fillResultsPanel(newResultsData, viewType);
			mainViewPort.setResultsPanel(resultsPanel);
			mainViewPort.getCenterPanel().setDisplayPanel(resultsPanel);
			resultsPanel.resizeContent();
		}
		EventBusManager.EVENT_BUS.fireEvent(new GetFocusOnJobsListEvent());
		Window.setTitle(resultData.getTruncatedInputName() + " - " + AppPropertiesManager.CONSTANTS.window_title_results() );
	}
	
	/**
	 * Retrieves results of search by uniprot id for displaying central panel content.
	 */
	public void displayUniprotSearch(String uniProtId)
	{
		mainViewPort.mask(AppPropertiesManager.CONSTANTS.defaultmask());
		CrkWebServiceProvider.getServiceController().getListOfPDBsHavingAUniProt(uniProtId);
	}
	
	/**
	 * Retrieves results of search by pdbCode and chainId for displaying central panel content.
	 */
	public void displayPdbSearch(String pdbCode, String chain)
	{
		mainViewPort.mask(AppPropertiesManager.CONSTANTS.defaultmask());
		CrkWebServiceProvider.getServiceController().getListOfPDBsbyPdbCode(pdbCode, chain);
	}
	
	/**
	 * Displays search panel.
	 * @param string 
	 */
	private void displaySearchView(String pdbCode, String chain, List<PDBSearchResult> resultList)
	{
		ApplicationContext.setDoStatusPanelRefreshing(false);

		SearchPanel searchPanel = null;
		
		String label = AppPropertiesManager.CONSTANTS.search_panel_pdb_subtitle();

		if((mainViewPort.getCenterPanel().getDisplayPanel() != null) &&
		   (mainViewPort.getCenterPanel().getDisplayPanel() instanceof SearchPanel))
		{
			searchPanel = (SearchPanel)mainViewPort.getCenterPanel().getDisplayPanel();
			searchPanel.fillSearchPanel(pdbCode, chain,  label, resultList);
		}
		else if(mainViewPort.getSearchPanel() != null)
		{
			searchPanel = mainViewPort.getSearchPanel();
			searchPanel.fillSearchPanel(pdbCode, chain, label, resultList);
			mainViewPort.getCenterPanel().setDisplayPanel(searchPanel);
			searchPanel.resizePanel();
		}
		else
		{
			searchPanel = new SearchPanel();
			searchPanel.fillSearchPanel(pdbCode, chain, label, resultList);
			mainViewPort.setSearchPanel(searchPanel);
			mainViewPort.getCenterPanel().setDisplayPanel(searchPanel);
			searchPanel.resizePanel();
		}

		Window.setTitle(AppPropertiesManager.CONSTANTS.window_title_searced() + " - " + pdbCode + " Chain " + chain);
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
						statusData.getInputName());
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
			CrkWebServiceProvider.getServiceController().getInterfaceResidues(ApplicationContext.getPdbInfo().getJobId(),
												   ApplicationContext.getPdbInfo().getInterface(interfaceId).getUid(),
												   interfaceId);
		}
		
		mainViewPort.displayResiduesWindow(interfaceId);
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
