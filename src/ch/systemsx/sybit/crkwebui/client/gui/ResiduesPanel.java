package ch.systemsx.sybit.crkwebui.client.gui;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import model.InterfaceResidueItem;
import model.InterfaceResidueMethodItem;
import model.InterfaceScoreItem;
import ch.systemsx.sybit.crkwebui.client.controllers.MainController;
import ch.systemsx.sybit.crkwebui.client.gui.renderers.GridCellRendererFactory;

import com.extjs.gxt.ui.client.Style.HorizontalAlignment;
import com.extjs.gxt.ui.client.Style.Scroll;
import com.extjs.gxt.ui.client.data.BasePagingLoader;
import com.extjs.gxt.ui.client.data.BeanModel;
import com.extjs.gxt.ui.client.data.BeanModelFactory;
import com.extjs.gxt.ui.client.data.BeanModelLookup;
import com.extjs.gxt.ui.client.data.ModelData;
import com.extjs.gxt.ui.client.data.PagingLoader;
import com.extjs.gxt.ui.client.data.PagingModelMemoryProxy;
import com.extjs.gxt.ui.client.store.ListStore;
import com.extjs.gxt.ui.client.store.Store;
import com.extjs.gxt.ui.client.store.StoreFilter;
import com.extjs.gxt.ui.client.widget.ContentPanel;
import com.extjs.gxt.ui.client.widget.grid.AggregationRowConfig;
import com.extjs.gxt.ui.client.widget.grid.ColumnConfig;
import com.extjs.gxt.ui.client.widget.grid.ColumnModel;
import com.extjs.gxt.ui.client.widget.grid.Grid;
import com.extjs.gxt.ui.client.widget.grid.GridCellRenderer;
import com.extjs.gxt.ui.client.widget.grid.GridViewConfig;
import com.extjs.gxt.ui.client.widget.grid.HeaderGroupConfig;
import com.extjs.gxt.ui.client.widget.layout.FitLayout;
import com.extjs.gxt.ui.client.widget.toolbar.PagingToolBar;
import com.google.gwt.i18n.client.NumberFormat;

/**
 * This panel is used to display the residues for one structure
 * @author srebniak_a
 *
 */
public class ResiduesPanel extends ContentPanel
{
	private List<ColumnConfig> residuesConfigs;
	private ListStore<BeanModel> residuesStore;
	private ColumnModel residuesColumnModel;
	private Grid<BeanModel> residuesGrid;
	private List<Integer> initialColumnWidth;
	
	private PagingModelMemoryProxy proxy;
	private PagingLoader loader;

	private MainController mainController;
	
	private List<BeanModel> data;
	private boolean isShowAll;
	
	private int nrOfRows = 20;
	private int nrOfAggregationRows = 3;
	
	private PagingToolBar pagingToolbar;
	
	private int structure;
	
	public ResiduesPanel(
						 String header, 
						 final MainController mainController,
						 int width,
						 int height,
						 int structure) 
	{
		this.mainController = mainController;
		this.structure = structure;
		this.setBodyBorder(false);
		this.setBorders(false);
		this.setLayout(new FitLayout());
		this.getHeader().setVisible(false);
		this.setScrollMode(Scroll.AUTO);

		residuesConfigs = createColumnConfig();

		proxy = new PagingModelMemoryProxy(null); 
		loader = new BasePagingLoader(proxy);  
		loader.setRemoteSort(true);
		   
		residuesStore = new ListStore<BeanModel>(loader);
		
		residuesStore.addFilter(new StoreFilter<BeanModel>() {
			
			@Override
			public boolean select(Store<BeanModel> store,
								  BeanModel parent,
								  BeanModel item, 
								  String property) 
			{
				if(isShowAll)
				{
					return true;
				}
				else if(((Integer)item.get("assignment") == InterfaceResidueItem.CORE) || 
						((Integer)item.get("assignment") == InterfaceResidueItem.RIM))
				{
					return true;
				}
				
				return false;
			}
		});
		
		residuesColumnModel = new ColumnModel(residuesConfigs);
		
		residuesColumnModel.addHeaderGroup(0, 0, new HeaderGroupConfig(header,
				1, residuesColumnModel.getColumnCount()));

		residuesGrid = new Grid<BeanModel>(residuesStore, residuesColumnModel);
		residuesGrid.setBorders(false);
		residuesGrid.setStripeRows(true);
		residuesGrid.setColumnLines(true);
		
		residuesGrid.disableTextSelection(false);
		
		residuesGrid.getView().setForceFit(true);
		
		residuesGrid.getView().setViewConfig(new GridViewConfig(){
			@Override
			public String getRowStyle(ModelData model, int rowIndex,
					ListStore<ModelData> ds) {
				if (model != null)
				{
					if ((Integer)model.get("assignment") == InterfaceResidueItem.SURFACE)
					{
						return "surface";
					}
					else if((Integer)model.get("assignment") == InterfaceResidueItem.CORE)
					{
						return "core";
					}
					else if((Integer)model.get("assignment") == InterfaceResidueItem.RIM)
					{
						return "rim";
					}
					else
					{
						return "buried";
					}
				}
				
				return "";
			}
		});
		
		this.add(residuesGrid);
		
		nrOfRows = (height)/22;
		nrOfRows -= nrOfAggregationRows;
		
		pagingToolbar = new PagingToolBar(nrOfRows);
		pagingToolbar.bind(loader);
		
		this.setBottomComponent(pagingToolbar);
	}
	
	private List<ColumnConfig> createColumnConfig() {
		List<ColumnConfig> configs = new ArrayList<ColumnConfig>();

		BeanModelFactory beanModelFactory = BeanModelLookup.get().getFactory(
				InterfaceResidueItem.class);
		BeanModel model = beanModelFactory
				.createModel(new InterfaceResidueItem());

		String columnOrder = mainController.getSettings().getGridProperties()
				.get("residues_columns");

		String[] columns = null;

		if (columnOrder == null) {
			columns = new String[model.getPropertyNames().size()];

			Iterator<String> fieldsIterator = model.getPropertyNames()
					.iterator();

			int i = 0;

			while (fieldsIterator.hasNext()) {
				columns[i] = fieldsIterator.next();
				i++;
			}
		} else {
			columns = columnOrder.split(",");
		}
		
		if (columns != null) {
			initialColumnWidth = new ArrayList<Integer>();
		}

		for (String columnName : columns) {
			boolean addColumn = true;

			String customAdd = mainController.getSettings().getGridProperties()
					.get("residues_" + columnName + "_add");
			if (customAdd != null) {
				if (!customAdd.equals("yes")) {
					addColumn = false;
				}
			}

			if (addColumn) {
				boolean displayColumn = true;

				String customVisibility = mainController.getSettings()
						.getGridProperties()
						.get("residues_" + columnName + "_visible");
				if (customVisibility != null) {
					if (!customVisibility.equals("yes")) {
						displayColumn = false;
					}
				}

				int columnWidth = 75;
				String customColumnWidth = mainController.getSettings()
						.getGridProperties()
						.get("residues_" + columnName + "_width");
				if (customColumnWidth != null) {
					columnWidth = Integer.parseInt(customColumnWidth);
				}
				
				String customRenderer = mainController.getSettings()
						.getGridProperties()
						.get("residues_" + columnName + "_renderer");

				GridCellRenderer<BeanModel> renderer = null;
				if ((customRenderer != null) && (!customRenderer.equals(""))) {
					renderer = GridCellRendererFactory.createGridCellRenderer(
							customRenderer, mainController);
				}

				String header = columnName;
				String customHeader = mainController.getSettings()
						.getGridProperties()
						.get("residues_" + columnName + "_header");
				if (customHeader != null) {
					header = customHeader;
				}
				
				String tootlip = mainController.getSettings()
						.getGridProperties()
						.get("residues_" + columnName + "_tooltip");

				if (columnName.equals("METHODS")) {
					for (String method : mainController.getSettings()
							.getScoresTypes()) {
						ColumnConfig column = new ColumnConfig();
						column.setId(method);
						column.setHeader(method);
						column.setWidth(columnWidth);
						initialColumnWidth.add(columnWidth);
						column.setAlignment(HorizontalAlignment.CENTER);
						column.setHidden(!displayColumn);

						if (renderer != null) {
							column.setRenderer(renderer);
						}
						
						if (tootlip != null) {
							column.setToolTip(tootlip);
						}

						configs.add(column);
					}
				} else {
					ColumnConfig column = new ColumnConfig();
					column.setId(columnName);
					column.setHeader(header);
					column.setWidth(columnWidth);
					initialColumnWidth.add(columnWidth);
					column.setAlignment(HorizontalAlignment.CENTER);

					column.setHidden(!displayColumn);

					if (renderer != null) {
						column.setRenderer(renderer);
					}
					
					if (tootlip != null) {
						column.setToolTip(tootlip);
					}

					configs.add(column);
				}
			}
		}

		return configs;

	}

	public void fillResiduesGrid(List<InterfaceResidueItem> residueValues) 
	{
		residuesStore.removeAll();

		data = new ArrayList<BeanModel>();

		if (residueValues != null) {
			for (InterfaceResidueItem residueValue : residueValues) {
				BeanModelFactory beanModelFactory = BeanModelLookup.get()
						.getFactory(InterfaceResidueItem.class);
				BeanModel model = beanModelFactory.createModel(residueValue);

				for (String method : mainController.getSettings()
						.getScoresTypes()) {
					
					String processedMethod = method;
					if(method.equals("Entropy"))
					{
						processedMethod = "entropy";
					}
					else if(method.equals("Kaks"))
					{
						processedMethod = "kaks";
					}
					else if(method.equals("Geometry"))
					{
						processedMethod = "geometry";
					}
					
					for(InterfaceResidueMethodItem interfaceResidueMethodItem : residueValue.getInterfaceResidueMethodItems())
					{
						if(interfaceResidueMethodItem.getMethod().equals(processedMethod))
						{
							model.set(method, interfaceResidueMethodItem.getScore());
						}
					}
				}

				data.add(model);
			}
		}

		proxy.setData(data);
		
		createAggregationRows();
//		residuesStore.add(data);
		
//		residuesStore.
		
//		residuesStore.applyFilters("residueType");
//		residuesGrid.reconfigure(residuesStore, residuesColumnModel);
	}
	
	public void applyFilter(boolean isShowAll)
	{
//		residuesStore.applyFilters("residueType");
//		loader.load(0, 20);
		
		this.isShowAll = isShowAll;
		
		List<BeanModel> dataToSet = new ArrayList<BeanModel>();
		for(BeanModel item : data)
		{
			if((isShowAll) ||
				(((Integer)item.get("assignment") == InterfaceResidueItem.CORE) || 
				((Integer)item.get("assignment") == InterfaceResidueItem.RIM)))
			{
				dataToSet.add(item);
			}
		}
		proxy.setData(dataToSet);
		loader.load(0, nrOfRows);
	}
	
	public void cleanResiduesGrid()
	{
		residuesStore.removeAll();
		residuesColumnModel.getAggregationRows().clear();
		residuesGrid.reconfigure(residuesStore, residuesColumnModel);
	}
	
	public void resizeGrid() 
	{
		int scoresGridWidthOfAllVisibleColumns = calculateWidthOfVisibleColumns();
		
		nrOfRows = (int)((mainController.getMainViewPort().getInterfacesResiduesWindow().getHeight() - 220)  / 22);
		nrOfRows -= nrOfAggregationRows;
		
		if (checkIfForceFit(scoresGridWidthOfAllVisibleColumns, 
							(int)((mainController.getMainViewPort().getInterfacesResiduesWindow().getInterfacesResiduesPanel().getWidth() - 20) * 0.48))) 
		{
			this.setScrollMode(Scroll.NONE);
//			residuesGrid.setAutoHeight(true);
		} 
		else 
		{
			this.setScrollMode(Scroll.AUTOX);
			residuesGrid.setWidth(scoresGridWidthOfAllVisibleColumns);
//			residuesGrid.setAutoHeight(true);
			
			int nrOfColumn = residuesGrid.getColumnModel().getColumnCount();

			for (int i = 0; i < nrOfColumn; i++) {
				residuesGrid.getColumnModel().getColumn(i)
						.setWidth(initialColumnWidth.get(i));
			}
			
			residuesGrid.setHeight(mainController.getMainViewPort().getInterfacesResiduesWindow().getHeight() - 190);
			nrOfRows--;
		}
		
		pagingToolbar.setPageSize(nrOfRows);
		loader.load(0, nrOfRows);
		pagingToolbar.setActivePage(1);
		
		this.layout();
	}
	
	private boolean checkIfForceFit(int scoresGridWidthOfAllVisibleColumns,
									int width)
	{
		if(scoresGridWidthOfAllVisibleColumns < width)
		{
			return true;
		}
		else
		{
			return false;
		}
	}
	
	private int calculateWidthOfVisibleColumns()
	{
		int scoresGridWidthOfAllVisibleColumns = 0;
		
		for(int i=0; i<residuesGrid.getColumnModel().getColumnCount(); i++)
		{
			if(!residuesGrid.getColumnModel().getColumn(i).isHidden())
			{
				scoresGridWidthOfAllVisibleColumns += initialColumnWidth.get(i);
			}
		}
		
		return scoresGridWidthOfAllVisibleColumns;
	}
	
	public PagingToolBar getResiduesGridPagingToolbar()
	{
		return pagingToolbar;
	}

	public Grid<BeanModel> getResiduesGrid() 
	{
		return residuesGrid;
	}
	
	private void createAggregationRows()
	{
		residuesColumnModel.getAggregationRows().clear();
		
		NumberFormat number = NumberFormat.getFormat("0.00");
		
		Map<String, String> coreMethodValues = new HashMap<String, String>();
		Map<String, String> rimMethodValues = new HashMap<String, String>();
		Map<String, String> ratioMethodValues = new HashMap<String, String>();
		
		for (InterfaceScoreItem scoreItem : mainController.getPdbScoreItem().getInterfaceItem(mainController.getMainViewPort().getInterfacesResiduesWindow().getSelectedInterface() - 1).getInterfaceScores()) 
		{
			String coreValue = "";
			String rimValue = "";
			String ratioValue = "";
			
			if(structure == 1)
			{
				coreValue = number.format(scoreItem.getUnweightedCore1Scores()) + 
							" (" + 
							number.format(scoreItem.getWeightedCore1Scores()) + 
							")";
				
				rimValue = number.format(scoreItem.getUnweightedRim1Scores()) + 
						   " (" + 
						   number.format(scoreItem.getWeightedRim1Scores()) + 
						   ")";
				
				ratioValue = number.format(scoreItem.getUnweightedRatio1Scores()) + 
							 " (" + 
							 number.format(scoreItem.getWeightedRatio1Scores()) + 
							 ")";
			}
			else
			{
				coreValue = number.format(scoreItem.getUnweightedCore2Scores()) + 
							" (" + 
							number.format(scoreItem.getWeightedCore2Scores()) + 
							")";
				
				rimValue = number.format(scoreItem.getUnweightedRim2Scores()) + 
						   " (" + 
						   number.format(scoreItem.getWeightedRim2Scores()) + 
						   ")";
				
				ratioValue = number.format(scoreItem.getUnweightedRatio2Scores()) + 
							 " (" + 
							 number.format(scoreItem.getWeightedRatio2Scores()) + 
							 ")";
			}
			
			coreMethodValues.put(scoreItem.getMethod(), coreValue);
			rimMethodValues.put(scoreItem.getMethod(), rimValue);
			ratioMethodValues.put(scoreItem.getMethod(), ratioValue);
		}
	
		AggregationRowConfig<BeanModel> totalCores = new AggregationRowConfig<BeanModel>();  
		
		int nrOfCores = 0;
		int nrOfRims = 0;
		for(BeanModel dataItem : data)
		{
			if(dataItem.get("assignment") != null)
			{
				if(dataItem.get("assignment").equals(InterfaceResidueItem.CORE))
			    {
					nrOfCores++;
				}
			    else if(dataItem.get("assignment").equals(InterfaceResidueItem.RIM))
			    {
			    	nrOfRims++;
				}
			}
		}
		
		totalCores.setHtml("residueNumber", MainController.CONSTANTS.interfaces_residues_aggergation_total_cores() + " (" + nrOfCores + ")"); 
		totalCores.setCellStyle("residueNumber", "aggregation-row-head");
		
		for (String method : mainController.getSettings()
				.getScoresTypes()) 
		{
			totalCores.setHtml(method, coreMethodValues.get(method)); 
		}
		
		residuesColumnModel.addAggregationRow(totalCores);  
		
		
		AggregationRowConfig<BeanModel> totalRims = new AggregationRowConfig<BeanModel>();  
		totalRims.setHtml("residueNumber", MainController.CONSTANTS.interfaces_residues_aggergation_total_rims() + " (" + nrOfRims + ")"); 
		totalRims.setCellStyle("residueNumber", "aggregation-row-head");
		
		for (String method : mainController.getSettings()
				.getScoresTypes()) 
		{
			totalRims.setHtml(method, rimMethodValues.get(method)); 
		}
		
		residuesColumnModel.addAggregationRow(totalRims);  
		
		AggregationRowConfig<BeanModel> totalRatios = new AggregationRowConfig<BeanModel>();  
		totalRatios.setHtml("residueNumber", MainController.CONSTANTS.interfaces_residues_aggergation_ratios() + " ("); 
		totalRatios.setCellStyle("residueNumber", "aggregation-row-head");
		
		for (String method : mainController.getSettings()
				.getScoresTypes()) 
		{
			totalRatios.setHtml(method, ratioMethodValues.get(method)); 
		}
		
		residuesColumnModel.addAggregationRow(totalRatios);  
	}
}
