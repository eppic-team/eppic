package ch.systemsx.sybit.crkwebui.client.gui;

import java.util.ArrayList;
import java.util.List;

import model.InterfaceResidueItem;
import model.InterfaceResidueMethodItem;
import ch.systemsx.sybit.crkwebui.client.controllers.MainController;
import ch.systemsx.sybit.crkwebui.client.model.InterfaceItemModel;
import ch.systemsx.sybit.crkwebui.client.model.InterfaceResidueItemModel;

import com.extjs.gxt.ui.client.Style.Scroll;
import com.extjs.gxt.ui.client.data.BasePagingLoader;
import com.extjs.gxt.ui.client.data.ModelData;
import com.extjs.gxt.ui.client.data.PagingLoader;
import com.extjs.gxt.ui.client.data.PagingModelMemoryProxy;
import com.extjs.gxt.ui.client.store.ListStore;
import com.extjs.gxt.ui.client.store.Store;
import com.extjs.gxt.ui.client.store.StoreFilter;
import com.extjs.gxt.ui.client.widget.ContentPanel;
import com.extjs.gxt.ui.client.widget.grid.ColumnConfig;
import com.extjs.gxt.ui.client.widget.grid.ColumnModel;
import com.extjs.gxt.ui.client.widget.grid.Grid;
import com.extjs.gxt.ui.client.widget.grid.GridViewConfig;
import com.extjs.gxt.ui.client.widget.grid.HeaderGroupConfig;
import com.extjs.gxt.ui.client.widget.layout.FitLayout;
import com.extjs.gxt.ui.client.widget.toolbar.PagingToolBar;

/**
 * This panel is used to display the residues for one structure
 * @author srebniak_a
 *
 */
public class ResiduesPanel extends ContentPanel
{
	private List<ColumnConfig> residuesConfigs;
	private ListStore<InterfaceResidueItemModel> residuesStore;
	private ColumnModel residuesColumnModel;
	private Grid<InterfaceResidueItemModel> residuesGrid;
	private List<Integer> initialColumnWidth;
	
	private PagingModelMemoryProxy proxy;
	private PagingLoader loader;

	private MainController mainController;
	
	private List<InterfaceResidueItemModel> data;
	private boolean isShowAll;
	
	private int nrOfRows = 20;
	private PagingToolBar pagingToolbar;
	
	public ResiduesPanel(
						 String header, 
						 final MainController mainController,
						 int width,
						 int height,
						 int structure) 
	{
		this.mainController = mainController;
		this.setBodyBorder(false);
		this.setBorders(false);
		this.setLayout(new FitLayout());
		this.getHeader().setVisible(false);
		this.setScrollMode(Scroll.NONE);

		residuesConfigs = createColumnConfig();

		proxy = new PagingModelMemoryProxy(null); 
		loader = new BasePagingLoader(proxy);  
		loader.setRemoteSort(true);
		   
		residuesStore = new ListStore<InterfaceResidueItemModel>(loader);
		
		residuesStore.addFilter(new StoreFilter<InterfaceResidueItemModel>() {
			
			@Override
			public boolean select(Store<InterfaceResidueItemModel> store,
								  InterfaceResidueItemModel parent,
								  InterfaceResidueItemModel item, 
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

		residuesGrid = new Grid<InterfaceResidueItemModel>(residuesStore, residuesColumnModel);
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
		
		nrOfRows = (height - 60)/22;
		
		pagingToolbar = new PagingToolBar(nrOfRows);
		pagingToolbar.bind(loader);
		
		this.setBottomComponent(pagingToolbar);
	}
	
	private List<ColumnConfig> createColumnConfig() 
	{
		List<ColumnConfig> configs = GridColumnConfigGenerator.createColumnConfigs(mainController,
				   "residues",
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

	public void fillResiduesGrid(List<InterfaceResidueItem> residueValues) 
	{
		residuesStore.removeAll();

		data = new ArrayList<InterfaceResidueItemModel>();

		if (residueValues != null) {
			for (InterfaceResidueItem residueValue : residueValues) {
				InterfaceResidueItemModel model = new InterfaceResidueItemModel();

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
				
				model.setStructure(residueValue.getStructure());
				model.setResidueNumber(residueValue.getResidueNumber());
				model.setResidueType(residueValue.getResidueType());
				model.setAsa(residueValue.getAsa());
				model.setBsa(residueValue.getBsa());
				model.setBsaPercentage(residueValue.getBsaPercentage());
				model.setAssignment(residueValue.getAssignment());
				data.add(model);
			}
		}

		proxy.setData(data);
		
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
		
		List<InterfaceResidueItemModel> dataToSet = new ArrayList<InterfaceResidueItemModel>();
		for(InterfaceResidueItemModel item : data)
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
	}
	
	public void resizeGrid() 
	{
		int scoresGridWidthOfAllVisibleColumns = calculateWidthOfVisibleColumns();
		
		nrOfRows = (int)((mainController.getMainViewPort().getInterfacesResiduesWindow().getHeight() - 330)  / 22);
		
		int assignedWidth = (int)((mainController.getMainViewPort().getInterfacesResiduesWindow().getInterfacesResiduesPanel().getWidth() - 20) * 0.48);
		
		if (checkIfForceFit(scoresGridWidthOfAllVisibleColumns, 
							assignedWidth)) 
		{
			this.setScrollMode(Scroll.NONE);
			this.setWidth(assignedWidth);
//			residuesGrid.setAutoHeight(true);
		} 
		else 
		{
			this.setScrollMode(Scroll.AUTOX);
			residuesGrid.setWidth(scoresGridWidthOfAllVisibleColumns);
			this.setWidth(scoresGridWidthOfAllVisibleColumns);
//			residuesGrid.setAutoHeight(true);
			
			int nrOfColumn = residuesGrid.getColumnModel().getColumnCount();

			for (int i = 0; i < nrOfColumn; i++) {
				residuesGrid.getColumnModel().getColumn(i)
						.setWidth(initialColumnWidth.get(i));
			}
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

	public Grid<InterfaceResidueItemModel> getResiduesGrid() 
	{
		return residuesGrid;
	}
	
	private void createAggregationRows()
	{
//		residuesColumnModel.getAggregationRows().clear();
//		
//		NumberFormat number = NumberFormat.getFormat("0.00");
//		
//		Map<String, String> coreMethodValues = new HashMap<String, String>();
//		Map<String, String> rimMethodValues = new HashMap<String, String>();
//		Map<String, String> ratioMethodValues = new HashMap<String, String>();
//		
//		for (InterfaceScoreItem scoreItem : mainController.getPdbScoreItem().getInterfaceItem(mainController.getMainViewPort().getInterfacesResiduesWindow().getSelectedInterface() - 1).getInterfaceScores()) 
//		{
//			String coreValue = "";
//			String rimValue = "";
//			String ratioValue = "";
//			
//			if(structure == 1)
//			{
//				coreValue = number.format(scoreItem.getUnweightedCore1Scores()) + 
//							" (" + 
//							number.format(scoreItem.getWeightedCore1Scores()) + 
//							")";
//				
//				rimValue = number.format(scoreItem.getUnweightedRim1Scores()) + 
//						   " (" + 
//						   number.format(scoreItem.getWeightedRim1Scores()) + 
//						   ")";
//				
//				ratioValue = number.format(scoreItem.getUnweightedRatio1Scores()) + 
//							 " (" + 
//							 number.format(scoreItem.getWeightedRatio1Scores()) + 
//							 ")";
//			}
//			else
//			{
//				coreValue = number.format(scoreItem.getUnweightedCore2Scores()) + 
//							" (" + 
//							number.format(scoreItem.getWeightedCore2Scores()) + 
//							")";
//				
//				rimValue = number.format(scoreItem.getUnweightedRim2Scores()) + 
//						   " (" + 
//						   number.format(scoreItem.getWeightedRim2Scores()) + 
//						   ")";
//				
//				ratioValue = number.format(scoreItem.getUnweightedRatio2Scores()) + 
//							 " (" + 
//							 number.format(scoreItem.getWeightedRatio2Scores()) + 
//							 ")";
//			}
//			
//			coreMethodValues.put(scoreItem.getMethod(), coreValue);
//			rimMethodValues.put(scoreItem.getMethod(), rimValue);
//			ratioMethodValues.put(scoreItem.getMethod(), ratioValue);
//		}
//	
//		AggregationRowConfig<InterfaceResidueItemModel> totalCores = new AggregationRowConfig<InterfaceResidueItemModel>();  
//		
//		int nrOfCores = 0;
//		int nrOfRims = 0;
//		for(InterfaceResidueItemModel dataItem : data)
//		{
//			if(dataItem.get("assignment") != null)
//			{
//				if(dataItem.get("assignment").equals(InterfaceResidueItem.CORE))
//			    {
//					nrOfCores++;
//				}
//			    else if(dataItem.get("assignment").equals(InterfaceResidueItem.RIM))
//			    {
//			    	nrOfRims++;
//				}
//			}
//		}
//		
//		totalCores.setHtml("residueNumber", MainController.CONSTANTS.interfaces_residues_aggergation_total_cores() + " (" + nrOfCores + ")"); 
//		totalCores.setCellStyle("residueNumber", "aggregation-row-head");
//		
//		for (String method : mainController.getSettings()
//				.getScoresTypes()) 
//		{
//			totalCores.setHtml(method, coreMethodValues.get(method)); 
//		}
//		
//		residuesColumnModel.addAggregationRow(totalCores);  
//		
//		
//		AggregationRowConfig<InterfaceResidueItemModel> totalRims = new AggregationRowConfig<InterfaceResidueItemModel>();  
//		totalRims.setHtml("residueNumber", MainController.CONSTANTS.interfaces_residues_aggergation_total_rims() + " (" + nrOfRims + ")"); 
//		totalRims.setCellStyle("residueNumber", "aggregation-row-head");
//		
//		for (String method : mainController.getSettings()
//				.getScoresTypes()) 
//		{
//			totalRims.setHtml(method, rimMethodValues.get(method)); 
//		}
//		
//		residuesColumnModel.addAggregationRow(totalRims);  
//		
//		AggregationRowConfig<InterfaceResidueItemModel> totalRatios = new AggregationRowConfig<InterfaceResidueItemModel>();  
//		totalRatios.setHtml("residueNumber", MainController.CONSTANTS.interfaces_residues_aggergation_ratios() + " ("); 
//		totalRatios.setCellStyle("residueNumber", "aggregation-row-head");
//		
//		for (String method : mainController.getSettings()
//				.getScoresTypes()) 
//		{
//			totalRatios.setHtml(method, ratioMethodValues.get(method)); 
//		}
//		
//		residuesColumnModel.addAggregationRow(totalRatios);  
	}
}
