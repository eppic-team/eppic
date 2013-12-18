package ch.systemsx.sybit.crkwebui.client.results.gui.panels;

import java.util.ArrayList;
import java.util.List;

import ch.systemsx.sybit.crkwebui.client.commons.appdata.AppPropertiesManager;
import ch.systemsx.sybit.crkwebui.client.commons.appdata.ApplicationContext;
import ch.systemsx.sybit.crkwebui.client.commons.events.SelectResultsRowEvent;
import ch.systemsx.sybit.crkwebui.client.commons.events.ShowDetailsEvent;
import ch.systemsx.sybit.crkwebui.client.commons.events.ShowInterfaceResiduesEvent;
import ch.systemsx.sybit.crkwebui.client.commons.events.ShowViewerEvent;
import ch.systemsx.sybit.crkwebui.client.commons.events.ShowViewerSelectorEvent;
import ch.systemsx.sybit.crkwebui.client.commons.events.WindowHideEvent;
import ch.systemsx.sybit.crkwebui.client.commons.handlers.SelectResultsRowHandler;
import ch.systemsx.sybit.crkwebui.client.commons.handlers.ShowDetailsHandler;
import ch.systemsx.sybit.crkwebui.client.commons.handlers.ShowViewerHandler;
import ch.systemsx.sybit.crkwebui.client.commons.handlers.WindowHideHandler;
import ch.systemsx.sybit.crkwebui.client.commons.managers.EventBusManager;
import ch.systemsx.sybit.crkwebui.client.commons.managers.ViewerRunner;
import ch.systemsx.sybit.crkwebui.client.results.data.InterfaceItemModel;
import ch.systemsx.sybit.crkwebui.shared.model.InterfaceItem;
import ch.systemsx.sybit.crkwebui.shared.model.InterfaceScoreItem;
import ch.systemsx.sybit.crkwebui.shared.model.PDBScoreItem;

import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.sencha.gxt.core.client.dom.ScrollSupport.ScrollMode;
import com.sencha.gxt.widget.core.client.FramedPanel;
import com.sencha.gxt.widget.core.client.button.TextButton;
import com.sencha.gxt.widget.core.client.container.VerticalLayoutContainer;
import com.sencha.gxt.widget.core.client.event.SelectEvent;
import com.sencha.gxt.widget.core.client.event.SelectEvent.SelectHandler;
import com.sencha.gxt.widget.core.client.form.CheckBox;
import com.sencha.gxt.widget.core.client.grid.Grid;
import com.sencha.gxt.widget.core.client.toolbar.FillToolItem;
import com.sencha.gxt.widget.core.client.toolbar.ToolBar;

public class ResultsGridPanel extends VerticalLayoutContainer
{
	private VerticalLayoutContainer panelContainer;
	private int panelWidth;
	private CheckBox clustersViewCheckBox;
	
	private ResultsInterfacesGrid interfacesGrid;
	private ResultsClustersGrid clustersGrid;
	
	public ResultsGridPanel(int width)
	{
		
		this.panelWidth = width;
		this.setWidth(panelWidth);
		
		FramedPanel gridPanel = new FramedPanel();
		
		gridPanel.getHeader().setVisible(false);
		gridPanel.setBorders(true);
		gridPanel.setBodyBorder(false);
		
		panelContainer = new VerticalLayoutContainer();
		panelContainer.setScrollMode(ScrollMode.AUTO);
		gridPanel.setWidget(panelContainer);
	
		panelContainer.add(createSelectorToolBar(), new VerticalLayoutData(1,-1));
		
		interfacesGrid = new ResultsInterfacesGrid(panelWidth);
		clustersGrid = new ResultsClustersGrid(panelWidth);
		
		panelContainer.add(interfacesGrid, new VerticalLayoutData(1,-1));

		this.add(panelContainer, new VerticalLayoutData(-1,-1));
		
		initializeEventsListeners();
	}
	
	/**
	 * Creates the toolbar displayed on the top of grid
	 * @return
	 */
	private ToolBar createSelectorToolBar(){
		ToolBar toolBar = new ToolBar();
		
		clustersViewCheckBox = createClustersViewCheckBox();
		toolBar.add(clustersViewCheckBox);
		
		TextButton changeViewerButton = new TextButton(
				AppPropertiesManager.CONSTANTS.results_grid_selector_button());
		changeViewerButton.addSelectHandler(new SelectHandler() {
			
			@Override
			public void onSelect(SelectEvent event) {
				EventBusManager.EVENT_BUS.fireEvent(new ShowViewerSelectorEvent());
				
			}
		});
		
		toolBar.add(new FillToolItem());
		toolBar.add(changeViewerButton);
		
		return toolBar;
	}
	
	/**
	 * Creates the check box for clusters view
	 */
	private CheckBox createClustersViewCheckBox(){
		CheckBox clustersViewButton = new CheckBox();
		clustersViewButton.setBoxLabel(AppPropertiesManager.CONSTANTS.results_grid_clusters_label());
		clustersViewButton.setToolTip(AppPropertiesManager.CONSTANTS.results_grid_clusters_tooltip());
		clustersViewButton.addValueChangeHandler(new ValueChangeHandler<Boolean>() {

			@Override
			public void onValueChange(ValueChangeEvent<Boolean> event) {
				if (!event.getValue()) {
					panelContainer.remove(clustersGrid);
		        	panelContainer.add(interfacesGrid, new VerticalLayoutData(1,-1));
				} else{
					panelContainer.remove(interfacesGrid);
		        	panelContainer.add(clustersGrid, new VerticalLayoutData(1,-1));
				}
				
			}
		});
		
		return clustersViewButton;
	}
	
	/**
	 * Sets content of results grid.
	 * @param resultsData results data of selected job
	 */
	public void fillResultsGrid(PDBScoreItem resultsData)
	{		
		boolean hideWarnings = true;
		
		List<InterfaceItemModel> data = new ArrayList<InterfaceItemModel>();

		List<InterfaceItem> interfaceItems = resultsData.getInterfaceItems();

		if (interfaceItems != null)
		{
			for (InterfaceItem interfaceItem : interfaceItems) 
			{
				if((interfaceItem.getWarnings() != null) &&
				   (interfaceItem.getWarnings().size() > 0))
			    {
					hideWarnings = false;
			    }
				
				InterfaceItemModel model = new InterfaceItemModel();

				for(InterfaceScoreItem interfaceScoreItem : interfaceItem.getInterfaceScores())
				{
					if(interfaceScoreItem.getMethod().equals("Geometry"))
					{
						model.setGeometryCall(interfaceScoreItem.getCallName());
					}

					if(interfaceScoreItem.getMethod().equals("Entropy"))
					{
						model.setCoreRimCall(interfaceScoreItem.getCallName());
					}

					if(interfaceScoreItem.getMethod().equals("Z-scores"))
					{
						model.setCoreSurfaceCall(interfaceScoreItem.getCallName());
					}
				}
				
				model.setId(interfaceItem.getId());
				model.setClusterId(interfaceItem.getClusterId());
				model.setName(interfaceItem.getChain1() + "+" + interfaceItem.getChain2());
				model.setArea(interfaceItem.getArea());
				model.setSizes(String.valueOf(interfaceItem.getSize1()) + " + " + String.valueOf(interfaceItem.getSize2()));
				model.setFinalCallName(interfaceItem.getFinalCallName());
				model.setOperator(interfaceItem.getOperator());
				model.setOperatorType(interfaceItem.getOperatorType());
				model.setIsInfinite(interfaceItem.getIsInfinite());
				model.setWarnings(interfaceItem.getWarnings());
				String thumbnailUrl = ApplicationContext.getSettings().getResultsLocation() +
						ApplicationContext.getPdbScoreItem().getJobId() + 
						"/" + ApplicationContext.getPdbScoreItem().getPdbName() +
						"." + interfaceItem.getId() + ".75x75.png";
				model.setThumbnailUrl(thumbnailUrl);

				data.add(model);
			}
		}
		
		interfacesGrid.fillResultsGrid(data, hideWarnings);
		clustersGrid.fillResultsGrid(data, hideWarnings);
		
	}
	
	/**
	 * Adjusts size of the results grid based on the current screen size and
	 * initial settings for the columns.
	 */
	public void resizeContent(int width) 
	{	
		panelWidth = width;
		panelContainer.setWidth(panelWidth);
		interfacesGrid.resizeContent(width);
		clustersGrid.resizeContent(width);
	}
	
	/**
	 * gets the current displayed grid normal/clusters
	 */
	private Grid<InterfaceItemModel> getCurrentGrid(){
		if(clustersViewCheckBox.getValue())
			return clustersGrid;
		else
			return interfacesGrid;
	}
	
	/**
	 * Events listeners initialization.
	 */
	private void initializeEventsListeners()
	{
		EventBusManager.EVENT_BUS.addHandler(WindowHideEvent.TYPE, new WindowHideHandler() 
		{
			@Override
			public void onWindowHide(WindowHideEvent event) 
			{
				if(getCurrentGrid().isVisible())
				{
					//resultsGrid.focus();
				}
			}
		});
		
		EventBusManager.EVENT_BUS.addHandler(ShowViewerEvent.TYPE, new ShowViewerHandler() 
		{
			@Override
			public void onShowViewer(ShowViewerEvent event) 
			{
				ViewerRunner.runViewer(String.valueOf(getCurrentGrid().getSelectionModel().getSelectedItem().getId()));
			}
		});
		
		EventBusManager.EVENT_BUS.addHandler(ShowDetailsEvent.TYPE, new ShowDetailsHandler() 
		{
			@Override
			public void onShowDetails(ShowDetailsEvent event) 
			{
				EventBusManager.EVENT_BUS.fireEvent(new ShowInterfaceResiduesEvent((Integer)getCurrentGrid().getSelectionModel().getSelectedItem().getId()));
			}
		}); 
		
		EventBusManager.EVENT_BUS.addHandler(SelectResultsRowEvent.TYPE, new SelectResultsRowHandler() {
			
			@Override
			public void onSelectResultsRow(SelectResultsRowEvent event) {
				getCurrentGrid().getSelectionModel().select(event.getRowIndex(), false);
			}
		}); 
	}
	
}
