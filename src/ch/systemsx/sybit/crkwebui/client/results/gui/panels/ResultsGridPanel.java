package ch.systemsx.sybit.crkwebui.client.results.gui.panels;

import java.util.ArrayList;
import java.util.List;

import ch.systemsx.sybit.crkwebui.client.commons.appdata.AppPropertiesManager;
import ch.systemsx.sybit.crkwebui.client.commons.appdata.ApplicationContext;
import ch.systemsx.sybit.crkwebui.client.commons.events.SaveResultsPanelGridSettingsEvent;
import ch.systemsx.sybit.crkwebui.client.commons.events.SelectResultsRowEvent;
import ch.systemsx.sybit.crkwebui.client.commons.events.ShowDetailsEvent;
import ch.systemsx.sybit.crkwebui.client.commons.events.ShowInterfaceResiduesEvent;
import ch.systemsx.sybit.crkwebui.client.commons.events.ShowThumbnailEvent;
import ch.systemsx.sybit.crkwebui.client.commons.events.ShowViewerEvent;
import ch.systemsx.sybit.crkwebui.client.commons.events.WindowHideEvent;
import ch.systemsx.sybit.crkwebui.client.commons.gui.util.GridColumnConfigGenerator;
import ch.systemsx.sybit.crkwebui.client.commons.gui.util.GridResizer;
import ch.systemsx.sybit.crkwebui.client.commons.handlers.SaveResultsPanelGridSettingsHandler;
import ch.systemsx.sybit.crkwebui.client.commons.handlers.SelectResultsRowHandler;
import ch.systemsx.sybit.crkwebui.client.commons.handlers.ShowDetailsHandler;
import ch.systemsx.sybit.crkwebui.client.commons.handlers.ShowThumbnailHandler;
import ch.systemsx.sybit.crkwebui.client.commons.handlers.ShowViewerHandler;
import ch.systemsx.sybit.crkwebui.client.commons.handlers.WindowHideHandler;
import ch.systemsx.sybit.crkwebui.client.commons.managers.EventBusManager;
import ch.systemsx.sybit.crkwebui.client.commons.managers.ViewerRunner;
import ch.systemsx.sybit.crkwebui.client.results.data.InterfaceItemModel;
import ch.systemsx.sybit.crkwebui.client.results.gui.grids.contextmenus.ResultsPanelContextMenu;
import ch.systemsx.sybit.crkwebui.client.results.gui.renderers.ResultsGridCellRendererFactoryImpl;
import ch.systemsx.sybit.crkwebui.shared.model.InterfaceItem;
import ch.systemsx.sybit.crkwebui.shared.model.InterfaceScoreItem;
import ch.systemsx.sybit.crkwebui.shared.model.PDBScoreItem;
import ch.systemsx.sybit.crkwebui.shared.model.SupportedMethod;

import com.extjs.gxt.ui.client.Style.Scroll;
import com.extjs.gxt.ui.client.data.BaseListLoader;
import com.extjs.gxt.ui.client.data.MemoryProxy;
import com.extjs.gxt.ui.client.event.BaseEvent;
import com.extjs.gxt.ui.client.event.ComponentEvent;
import com.extjs.gxt.ui.client.event.Events;
import com.extjs.gxt.ui.client.event.GridEvent;
import com.extjs.gxt.ui.client.event.Listener;
import com.extjs.gxt.ui.client.store.ListStore;
import com.extjs.gxt.ui.client.util.KeyNav;
import com.extjs.gxt.ui.client.widget.ContentPanel;
import com.extjs.gxt.ui.client.widget.grid.ColumnConfig;
import com.extjs.gxt.ui.client.widget.grid.ColumnModel;
import com.extjs.gxt.ui.client.widget.grid.Grid;
import com.extjs.gxt.ui.client.widget.layout.FitLayout;

public class ResultsGridPanel extends ContentPanel
{
	private ListStore<InterfaceItemModel> resultsStore;
	private ColumnModel resultsColumnModel;
	private Grid<InterfaceItemModel> resultsGrid;
	private GridResizer gridResizer;
	private List<Integer> initialColumnWidth;
	
	private MemoryProxy proxy;
	private BaseListLoader loader;
	
	private int assignedWidth;
	
	public ResultsGridPanel(boolean showThumbnail)
	{
		this.getHeader().setVisible(false);
		this.setBorders(true);
		this.setBodyBorder(false);
		this.setLayout(new FitLayout());
		this.setScrollMode(Scroll.AUTOX);
		
		List<ColumnConfig> resultsConfigs = createColumnConfig();
		
		if(!showThumbnail)
		{
			for(ColumnConfig column : resultsConfigs)
			{
				if(column.getId().equals("thumbnail"))
				{
					column.setHidden(true);
				}
			}
		}

		proxy = new MemoryProxy(null);
		loader = new BaseListLoader(proxy);
		
		resultsStore = new ListStore<InterfaceItemModel>(loader);
		resultsColumnModel = new ColumnModel(resultsConfigs);
		
		resultsGrid = createResultsGrid();
		this.add(resultsGrid);
		
		gridResizer = new GridResizer(resultsGrid, initialColumnWidth, false, true);
		initializeEventsListeners();
	}
	
	/**
	 * Creates configurations of the columns for results grid.
	 * @return list of columns configurations for results grid
	 */
	private List<ColumnConfig> createColumnConfig() 
	{
		List<ColumnConfig> configs = GridColumnConfigGenerator.createColumnConfigs(ResultsGridCellRendererFactoryImpl.getInstance(),
																				   "results",
																				   new InterfaceItemModel());

		if(configs != null)
		{
			initialColumnWidth = new ArrayList<Integer>();
			
			for(ColumnConfig columnConfig : configs)
			{
				initialColumnWidth.add(columnConfig.getWidth());
			}
		}

		return configs;
	}
	
	/**
	 * Creates grid storing results of calculations for each of the interfaces.
	 * @return interfaces grid
	 */
	private Grid<InterfaceItemModel> createResultsGrid()
	{
		final Grid<InterfaceItemModel> resultsGrid = new Grid<InterfaceItemModel>(resultsStore, resultsColumnModel);
		resultsGrid.setBorders(false);
		resultsGrid.setStripeRows(true);
		resultsGrid.setColumnLines(false);
		resultsGrid.setContextMenu(new ResultsPanelContextMenu());
		resultsGrid.disableTextSelection(false);
		
		resultsGrid.getView().setForceFit(false);
		resultsGrid.getView().setEmptyText(AppPropertiesManager.CONSTANTS.results_grid_empty_text());

		resultsGrid.addListener(Events.ColumnResize, new Listener<GridEvent>() {
			@Override
			public void handleEvent(GridEvent ge) 
			{
				int widthToSet = (int) (ge.getWidth() / gridResizer.getGridWidthMultiplier());
				ApplicationContext.getSettings().getGridProperties().put("results_" + resultsGrid.getColumnModel().getColumnId(ge.getColIndex()) + "_width", 
																	 	 String.valueOf(widthToSet));
			}
		});
		
		new KeyNav<ComponentEvent>(resultsGrid)
		{
			@Override
            public void onEnter(ComponentEvent ce) 
			{
				InterfaceItemModel interfaceItemModel = resultsGrid.getSelectionModel().getSelectedItem();
				if(interfaceItemModel != null)
				{
					EventBusManager.EVENT_BUS.fireEvent(new ShowDetailsEvent());
				}
			}
		};
		
		return resultsGrid;
	}
	
	/**
	 * Sets content of results grid.
	 * @param resultsData results data of selected job
	 */
	public void fillResultsGrid(PDBScoreItem resultsData)
	{
		boolean hideWarnings = true;
		
		resultsStore.removeAll();

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

				for (SupportedMethod method : ApplicationContext.getSettings().getScoresTypes())
				{
					for(InterfaceScoreItem interfaceScoreItem : interfaceItem.getInterfaceScores())
					{
						if(interfaceScoreItem.getMethod().equals(method.getName()))
						{
							model.set(method.getName(), interfaceScoreItem.getCallName());
						}
					}
				}
				
				model.setId(interfaceItem.getId());
				model.setName(interfaceItem.getChain1() + "+" + interfaceItem.getChain2());
				model.setArea(interfaceItem.getArea());
				model.setSizes(String.valueOf(interfaceItem.getSize1()) + " + " + String.valueOf(interfaceItem.getSize2()));
				model.setFinalCallName(interfaceItem.getFinalCallName());
				model.setOperator(interfaceItem.getOperator());
				model.setWarnings(interfaceItem.getWarnings());

				data.add(model);
			}
		}

		proxy.setData(data);
		loader.load();
		
		boolean resizeGrid = false;
		if(resultsColumnModel.getColumnById("warnings").isHidden() != hideWarnings)
		{
			resizeGrid = true;
		}
		
		resultsColumnModel.getColumnById("warnings").setHidden(hideWarnings);
		
		resultsGrid.reconfigure(resultsStore, resultsColumnModel);
		
		if(resizeGrid)
		{
			resizeGrid();
		}
	}
	
	/**
	 * Adjusts size of the results grid based on the current screen size and
	 * initial settings for the columns.
	 */
	public void resizeGrid() 
	{
		gridResizer.resize(assignedWidth - 2);
		this.setWidth(assignedWidth + 2);
		
		loader.load();
		resultsGrid.getView().refresh(true);
	}
	
	/**
	 * Saves currently customized by the user grid settings to reuse them for
	 * displaying other results.
	 */
	private void saveGridSettings()
	{
		for(int i=0; i<resultsGrid.getColumnModel().getColumnCount(); i++)
		{
			String value = ApplicationContext.getSettings().getGridProperties().get("results_" + resultsGrid.getColumnModel().getColumn(i).getId() + "_visible");
			
			if(resultsGrid.getColumnModel().getColumn(i).isHidden() && ((value == null) || (!value.equals("no"))))
			{
				ApplicationContext.getSettings().getGridProperties().put("results_" + resultsGrid.getColumnModel().getColumn(i).getId() + "_visible", 
																	 "no");	
			}
			else if(!resultsGrid.getColumnModel().getColumn(i).isHidden() && (value != null) && (!value.equals("yes")))
			{
				ApplicationContext.getSettings().getGridProperties().put("results_" + resultsGrid.getColumnModel().getColumn(i).getId() + "_visible", 
																	 "yes");	
			}
		}
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
				if(resultsGrid.isVisible())
				{
					resultsGrid.focus();
				}
			}
		});
		
		EventBusManager.EVENT_BUS.addHandler(ShowViewerEvent.TYPE, new ShowViewerHandler() 
		{
			@Override
			public void onShowViewer(ShowViewerEvent event) 
			{
				ViewerRunner.runViewer(String.valueOf(resultsGrid.getSelectionModel().getSelectedItem().get("id")));
			}
		}); 
		
		EventBusManager.EVENT_BUS.addHandler(ShowThumbnailEvent.TYPE, new ShowThumbnailHandler() {
			
			@Override
			public void onShowThumbnail(ShowThumbnailEvent event)
			{
				for(ColumnConfig column : resultsGrid.getColumnModel().getColumns())
				{
					if(column.getId().equals("thumbnail"))
					{
						column.setHidden(event.isHideThumbnail());
						resizeGrid();
					}
				}
			}
		}); 
		
		EventBusManager.EVENT_BUS.addHandler(ShowDetailsEvent.TYPE, new ShowDetailsHandler() 
		{
			@Override
			public void onShowDetails(ShowDetailsEvent event) 
			{
				EventBusManager.EVENT_BUS.fireEvent(new ShowInterfaceResiduesEvent((Integer)resultsGrid.getSelectionModel().getSelectedItem().get("id")));
			}
		}); 
		
		EventBusManager.EVENT_BUS.addHandler(SelectResultsRowEvent.TYPE, new SelectResultsRowHandler() {
			
			@Override
			public void onSelectResultsRow(SelectResultsRowEvent event) {
				resultsGrid.getSelectionModel().select(event.getRowIndex(), false);
			}
		}); 
		
		EventBusManager.EVENT_BUS.addHandler(SaveResultsPanelGridSettingsEvent.TYPE, new SaveResultsPanelGridSettingsHandler() {
			
			@Override
			public void onSaveResultsPanelGridSettings(SaveResultsPanelGridSettingsEvent event) 
			{
				saveGridSettings();
			}
		});
	}
	
	/**
	 * Sets width available for the grid.
	 * @param assignedWidth max width available for the grid
	 */
	public void setAssignedWidth(int assignedWidth)
	{
		this.assignedWidth = assignedWidth;
	}
}
