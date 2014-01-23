package ch.systemsx.sybit.crkwebui.client.results.gui.panels;

import ch.systemsx.sybit.crkwebui.client.commons.appdata.ApplicationContext;
import ch.systemsx.sybit.crkwebui.client.commons.events.ApplicationWindowResizeEvent;
import ch.systemsx.sybit.crkwebui.client.commons.gui.panels.DisplayPanel;
import ch.systemsx.sybit.crkwebui.client.commons.handlers.ApplicationWindowResizeHandler;
import ch.systemsx.sybit.crkwebui.client.commons.managers.EventBusManager;
import ch.systemsx.sybit.crkwebui.client.commons.util.EscapedStringGenerator;
import ch.systemsx.sybit.crkwebui.shared.model.PDBScoreItem;

import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.user.client.ui.DockLayoutPanel;
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
	private IdentifierHeaderPanel headerPanel;
	private VerticalLayoutContainer resultsContainer;
	
	private InformationPanel informationPanel;
	private ResultsGridPanel resultsGridContainer;

	public ResultsPanel(PDBScoreItem pdbScoreItem)
	{
		DockLayoutPanel dock = new DockLayoutPanel(Unit.PX);

		headerPanel = new IdentifierHeaderPanel(ApplicationContext.getWindowData().getWindowWidth() - 150);
		dock.addNorth(headerPanel, 70);
		
		resultsContainer = createResultsContainer(pdbScoreItem);
		dock.add(resultsContainer);
		
		this.setData(dock);
		
		initializeEventsListeners();
	}
	
	/**
	 * Creates the results container with information panel, grid panel
	 */
	private VerticalLayoutContainer createResultsContainer(PDBScoreItem pdbScoreItem){
		VerticalLayoutContainer mainContainer = new VerticalLayoutContainer();
		mainContainer.setScrollMode(ScrollMode.AUTOY);
		
		informationPanel = new InformationPanel(pdbScoreItem, ApplicationContext.getWindowData().getWindowWidth() - 180);
		mainContainer.add(informationPanel, new VerticalLayoutData(-1, 135, new Margins(10,0,10,0)));

		resultsGridContainer = new ResultsGridPanel(ApplicationContext.getWindowData().getWindowWidth() - 180);
		mainContainer.add(resultsGridContainer, new VerticalLayoutData(-1, 1, new Margins(0)));
		
		return mainContainer;
	}
	
	/**
	 * Sets content of results panel.
	 * @param resultsData results data of selected job
	 */
	public void fillResultsPanel(PDBScoreItem resultsData) 
	{
		resultsGridContainer.fillResultsGrid(resultsData);
		informationPanel.fillInfoPanel(resultsData);
		
		headerPanel.setPDBText(resultsData.getPdbName(),
							  	 	resultsData.getSpaceGroup(),
							  	 	resultsData.getExpMethod(),
							  	 	resultsData.getResolution(),
							  	 	resultsData.getRfreeValue(),
							  	 	resultsData.getInputType());
		
		headerPanel.setEppicLogoPanel(resultsData.getRunParameters().getCrkVersion());
		
		headerPanel.setPDBIdentifierSubtitle(EscapedStringGenerator.generateEscapedString(resultsData.getTitle()));
		
		headerPanel.setDownloadResultsLink(resultsData.getJobId());
	}

	public void resizeContent() 
	{
		int width = ApplicationContext.getWindowData().getWindowWidth() - 180;
		
		if(width < MIN_WIDTH) width = MIN_WIDTH-20;
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
