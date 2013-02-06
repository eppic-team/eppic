package ch.systemsx.sybit.crkwebui.client.results.gui.panels;

import ch.systemsx.sybit.crkwebui.client.commons.events.ApplicationWindowResizeEvent;
import ch.systemsx.sybit.crkwebui.client.commons.gui.panels.DisplayPanel;
import ch.systemsx.sybit.crkwebui.client.commons.handlers.ApplicationWindowResizeHandler;
import ch.systemsx.sybit.crkwebui.client.commons.managers.EventBusManager;
import ch.systemsx.sybit.crkwebui.client.commons.util.EscapedStringGenerator;
import ch.systemsx.sybit.crkwebui.shared.model.PDBScoreItem;

import com.extjs.gxt.ui.client.Style.Orientation;
import com.extjs.gxt.ui.client.util.Margins;
import com.extjs.gxt.ui.client.widget.form.FormPanel;
import com.extjs.gxt.ui.client.widget.layout.RowData;
import com.extjs.gxt.ui.client.widget.layout.RowLayout;

/**
 * Panel used to display the results of the calculations.
 * @author srebniak_a
 *
 */
public class ResultsPanel extends DisplayPanel
{
	private PDBIdentifierPanel pdbIdentifierPanel;
	private PDBIdentifierSubtitlePanel pdbIdentifierSubtitlePanel;
	
	private InfoPanel infoPanel;

	private ResultsSelectorsPanel resultsSelectorsPanel;
	
	private ResultsGridPanel resultsGridContainer;

	public ResultsPanel(PDBScoreItem pdbScoreItem)
	{
		this.setBorders(true);
		this.setLayout(new RowLayout(Orientation.VERTICAL));
		this.addStyleName("eppic-default-padding");

		pdbIdentifierPanel = new PDBIdentifierPanel();
		this.add(pdbIdentifierPanel);
		
		FormPanel breakPanel = createBreakPanel();
		this.add(breakPanel, new RowData(1, 1.1, new Margins(0)));
		
		pdbIdentifierSubtitlePanel = new PDBIdentifierSubtitlePanel();
		this.add(pdbIdentifierSubtitlePanel);
		
		breakPanel = createBreakPanel();
		this.add(breakPanel, new RowData(1, 10, new Margins(0)));
		
		infoPanel = new InfoPanel(pdbScoreItem);
		this.add(infoPanel, new RowData(1, 80, new Margins(0)));
		
		resultsSelectorsPanel = new ResultsSelectorsPanel();
		this.add(resultsSelectorsPanel, new RowData(1, 35, new Margins(0)));

		breakPanel = createBreakPanel();
		this.add(breakPanel, new RowData(1, 5, new Margins(0)));
		
		resultsGridContainer = new ResultsGridPanel(resultsSelectorsPanel.getShowThumbnailCheckBox().getValue());
		this.add(resultsGridContainer, new RowData(1, 1, new Margins(0)));
		
		initializeEventsListeners();
	}
	
	/**
	 * Creates panel used to separate rows.
	 * @return break panel
	 */
	private FormPanel createBreakPanel()
	{
		FormPanel breakPanel = new FormPanel();
		breakPanel.setBorders(false);
		breakPanel.setBodyBorder(false);
		breakPanel.setPadding(0);
		breakPanel.getHeader().setVisible(false);
		return breakPanel;
	}

	/**
	 * Sets content of results panel.
	 * @param resultsData results data of selected job
	 */
	public void fillResultsPanel(PDBScoreItem resultsData) 
	{
		resultsGridContainer.fillResultsGrid(resultsData);
		infoPanel.generateInfoPanel(resultsData);
		
		pdbIdentifierPanel.setPDBText(resultsData.getPdbName(),
							  	 	resultsData.getSpaceGroup(),
							  	 	resultsData.getExpMethod(),
							  	 	resultsData.getResolution(),
							  	 	resultsData.getInputType());
		
		pdbIdentifierSubtitlePanel.setPDBIdentifierSubtitle(EscapedStringGenerator.generateEscapedString(resultsData.getTitle()));
	}

	public void resizeContent() 
	{
		resultsGridContainer.resizeGrid();
		this.layout();
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
