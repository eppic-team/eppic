package ch.systemsx.sybit.crkwebui.client.results.gui.panels;

import ch.systemsx.sybit.crkwebui.client.commons.events.ApplicationWindowResizeEvent;
import ch.systemsx.sybit.crkwebui.client.commons.gui.panels.DisplayPanel;
import ch.systemsx.sybit.crkwebui.client.commons.handlers.ApplicationWindowResizeHandler;
import ch.systemsx.sybit.crkwebui.client.commons.managers.EventBusManager;
import ch.systemsx.sybit.crkwebui.client.commons.util.EscapedStringGenerator;
import ch.systemsx.sybit.crkwebui.shared.model.PDBScoreItem;

import com.extjs.gxt.ui.client.Style.Orientation;
import com.extjs.gxt.ui.client.util.Margins;
import com.extjs.gxt.ui.client.widget.layout.RowData;
import com.extjs.gxt.ui.client.widget.layout.RowLayout;

/**
 * Panel used to display the results of the calculations.
 * @author srebniak_a
 *
 */
public class ResultsPanel extends DisplayPanel
{
	private IdentifierHeaderPanel identifierHeaderPanel;
	
	private InfoPanel infoPanel;

	private ResultsSelectorsPanel resultsSelectorsPanel;
	
	private ResultsGridPanel resultsGridContainer;

	public ResultsPanel(PDBScoreItem pdbScoreItem)
	{
		this.setLayout(new RowLayout(Orientation.VERTICAL));

		identifierHeaderPanel = new IdentifierHeaderPanel();
		this.add(identifierHeaderPanel);
		
		infoPanel = new InfoPanel(pdbScoreItem);
		this.add(infoPanel, new RowData(1, 90, new Margins(0)));
		
		resultsSelectorsPanel = new ResultsSelectorsPanel(pdbScoreItem);
		this.add(resultsSelectorsPanel, new RowData(1, 40, new Margins(0, 0, 5, 0)));

		resultsGridContainer = new ResultsGridPanel();
		this.add(resultsGridContainer, new RowData(1, 1, new Margins(0)));
		
		initializeEventsListeners();
	}
	
	/**
	 * Sets content of results panel.
	 * @param resultsData results data of selected job
	 */
	public void fillResultsPanel(PDBScoreItem resultsData) 
	{
		resultsGridContainer.fillResultsGrid(resultsData);
		infoPanel.generateInfoPanel(resultsData);
		
		identifierHeaderPanel.setPDBText(resultsData.getPdbName(),
							  	 	resultsData.getSpaceGroup(),
							  	 	resultsData.getExpMethod(),
							  	 	resultsData.getResolution(),
							  	 	resultsData.getInputType());
		
		identifierHeaderPanel.setEppicLogoPanel(resultsData.getRunParameters().getCrkVersion());
		
		identifierHeaderPanel.setPDBIdentifierSubtitle(EscapedStringGenerator.generateEscapedString(resultsData.getTitle()));
	}

	public void resizeContent() 
	{
		resultsGridContainer.setAssignedWidth(this.getWidth(true));
		resultsGridContainer.resizeGrid();
		identifierHeaderPanel.resize();
		this.layout(true);
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
