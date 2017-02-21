package ch.systemsx.sybit.crkwebui.client.results.gui.panels;

import ch.systemsx.sybit.crkwebui.client.commons.appdata.ApplicationContext;
import ch.systemsx.sybit.crkwebui.client.commons.events.ApplicationWindowResizeEvent;
import ch.systemsx.sybit.crkwebui.client.commons.gui.panels.DisplayPanel;
import ch.systemsx.sybit.crkwebui.client.commons.handlers.ApplicationWindowResizeHandler;
import ch.systemsx.sybit.crkwebui.client.commons.managers.EventBusManager;
import ch.systemsx.sybit.crkwebui.client.commons.util.EscapedStringGenerator;
import ch.systemsx.sybit.crkwebui.shared.model.PdbInfo;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.user.client.ui.DockLayoutPanel;
import com.google.gwt.user.client.ui.HTML;
import com.sencha.gxt.widget.core.client.container.VerticalLayoutContainer;
import com.sencha.gxt.core.client.dom.ScrollSupport.ScrollMode;
import com.sencha.gxt.core.client.util.Margins;

/**
 * Panel used to display the results of the calculations.
 * @author srebniak_a
 *
 */
public class ResultsPanel extends DisplayPanel
{
	public static IdentifierHeaderPanel headerPanel;
	public static InformationPanel informationPanel;
	
	private VerticalLayoutContainer resultsContainer;	
	public static AssemblyResultsGridPanel assemblyResultsGridContainer;
	public static ResultsGridPanel resultsGridContainer;
	private VerticalLayoutContainer mainContainer;
	private HTML spacer;
	public final static int ASSEMBLIES_VIEW = 1;
	public final static int INTERFACES_VIEW = 2;
	private int viewType;


	public ResultsPanel(PdbInfo pdbScoreItem, int viewType)
	{
		
		this.viewType = viewType;
		
		DockLayoutPanel dock = new DockLayoutPanel(Unit.PX);

		headerPanel = new IdentifierHeaderPanel(ApplicationContext.getWindowData().getWindowWidth() - 150, pdbScoreItem, viewType);
		dock.addNorth(headerPanel, 115); //set the height of the info
		
		resultsContainer = createResultsContainer(pdbScoreItem);
		dock.add(resultsContainer);
		
		this.setData(dock);
		
		initializeEventsListeners();
	}
	
	/**
	 * Creates the results container with information panel, grid panel
	 */
	private VerticalLayoutContainer createResultsContainer(PdbInfo pdbScoreItem){
		mainContainer = new VerticalLayoutContainer();
		mainContainer.setScrollMode(ScrollMode.AUTOY);
		spacer = new HTML("<br>");
		informationPanel = new InformationPanel(pdbScoreItem, ApplicationContext.getWindowData().getWindowWidth() - 180);
		//mainContainer.add(informationPanel, new VerticalLayoutData(-1, 135, new Margins(10,0,10,0)));
		mainContainer.add(spacer);
		mainContainer.add(informationPanel, new VerticalLayoutData(-1, 135, new Margins(0,0,0,0)));
		resultsGridContainer = new ResultsGridPanel(ApplicationContext.getWindowData().getWindowWidth() - 180);
		assemblyResultsGridContainer = new AssemblyResultsGridPanel(ApplicationContext.getWindowData().getWindowWidth() - 180);
		if(viewType == ASSEMBLIES_VIEW){
			mainContainer.add(assemblyResultsGridContainer);
		}
		else if(viewType == INTERFACES_VIEW){
			mainContainer.add(resultsGridContainer);
		}
		return mainContainer;
	}
	
	/**
	 * Sets content of results panel.
	 * @param resultsData results data of selected job
	 */
	public void fillResultsPanel(PdbInfo resultsData, int viewType) 
	{
		this.viewType = viewType;

		headerPanel.setPDBText(resultsData.getInputName(),
							  	 	resultsData.getSpaceGroup(),
							  	 	resultsData.getExpMethod(),
							  	 	resultsData.getResolution(),
							  	 	resultsData.getRfreeValue(),
							  	 	resultsData.isNonStandardSg(),
							  	 	resultsData.isNonStandardCoordFrameConvention(),
							  	 	resultsData.getInputType());
		
		headerPanel.setEppicLogoPanel(resultsData.getRunParameters().getEppicVersion());
		
		headerPanel.setPDBIdentifierSubtitle(EscapedStringGenerator.generateEscapedString(resultsData.getTitle()));
		
		headerPanel.setDownloadResultsLink(resultsData.getJobId());
		
		informationPanel.fillInfoPanel(resultsData);
		
		if(viewType == ASSEMBLIES_VIEW){
			//AssemblyResultsGridPanel.assemblies_toolbar_link.setHTML("<a href='" + GWT.getHostPageBaseURL() + "#interfaces/"+ApplicationContext.getSelectedJobId()+"'>View All Interfaces</a>");
			AssemblyResultsGridPanel.assemblies_toolbar_link.setHTML("<a href='" + GWT.getHostPageBaseURL() + "#assembly/"+ApplicationContext.getSelectedJobId()+"'>View All Interfaces</a>");
			mainContainer.remove(informationPanel);
			mainContainer.add(informationPanel);
			assemblyResultsGridContainer.fillResultsGrid(resultsData);
			mainContainer.add(assemblyResultsGridContainer);
			mainContainer.remove(resultsGridContainer);
		}else if(viewType == INTERFACES_VIEW){
			ResultsGridPanel.toolbar_link.setHTML("<a href='" + GWT.getHostPageBaseURL() + "#id/"+ApplicationContext.getSelectedJobId()+"'>View All Assemblies</a>");	
			mainContainer.remove(informationPanel);
			mainContainer.add(informationPanel);
			mainContainer.remove(assemblyResultsGridContainer);
			resultsGridContainer.fillResultsGrid(resultsData);
			mainContainer.add(resultsGridContainer);
		}				
	}

	public void resizeContent() 
	{
		int width = ApplicationContext.getWindowData().getWindowWidth() - 180;
		
		if(width < MIN_WIDTH) width = MIN_WIDTH-20;
		assemblyResultsGridContainer.resizeContent(width);
		resultsGridContainer.resizeContent(width);
		headerPanel.resizePanel(width + 30);
		informationPanel.resizePanel(width);
	}
	
	/**
	 * Events listeners initialization.
	 */
	private void initializeEventsListeners()
	{
		EventBusManager.EVENT_BUS.addHandler(ApplicationWindowResizeEvent.TYPE, new ApplicationWindowResizeHandler() {
			
			@Override
			public void onResizeApplicationWindow(ApplicationWindowResizeEvent event) {
				resizeContent();
			}
		});
	}
	
}
