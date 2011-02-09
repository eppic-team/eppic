package ch.systemsx.sybit.crkwebui.client.gui;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import model.InterfaceScoreItem;
import model.InterfaceScoreItemKey;
import model.PDBScoreItem;
import ch.systemsx.sybit.crkwebui.client.controllers.MainController;
import ch.systemsx.sybit.crkwebui.client.gui.renderers.GridCellRendererFactory;

import com.extjs.gxt.ui.client.Style.HorizontalAlignment;
import com.extjs.gxt.ui.client.data.BeanModel;
import com.extjs.gxt.ui.client.data.BeanModelFactory;
import com.extjs.gxt.ui.client.data.BeanModelLookup;
import com.extjs.gxt.ui.client.store.GroupingStore;
import com.extjs.gxt.ui.client.widget.form.FormPanel;
import com.extjs.gxt.ui.client.widget.grid.ColumnConfig;
import com.extjs.gxt.ui.client.widget.grid.ColumnModel;
import com.extjs.gxt.ui.client.widget.grid.Grid;
import com.extjs.gxt.ui.client.widget.grid.GridCellRenderer;
import com.extjs.gxt.ui.client.widget.grid.GridGroupRenderer;
import com.extjs.gxt.ui.client.widget.grid.GroupColumnData;
import com.extjs.gxt.ui.client.widget.grid.GroupingView;
import com.extjs.gxt.ui.client.widget.grid.HeaderGroupConfig;
import com.extjs.gxt.ui.client.widget.layout.FitLayout;
import com.extjs.gxt.ui.client.widget.layout.FormData;
import com.google.gwt.user.client.Window;

public class ScoresPanel extends FormPanel
{
	private MainController mainController;
	
	private List<ColumnConfig> scoresConfigs;
	private GroupingStore<BeanModel> scoresStore;
	private ColumnModel scoresColumnModel;
	private Grid<BeanModel> scoresGrid;
	
	private PDBScoreItem resultsData;
	
	public ScoresPanel(PDBScoreItem resultsData,
					   MainController mainController)
	{
		this.mainController = mainController;
		FormData formData = new FormData("100%");  
//		infoPanel.setFrame(true);  
		this.getHeader().setVisible(false);  
		this.setBodyBorder(false);
		this.setBorders(false);
		this.setLayout(new FitLayout());
		this.setPadding(0);
		
		scoresConfigs = createColumnConfig(); 
		   
		scoresStore = new GroupingStore<BeanModel>();  
		scoresStore.groupBy("method");
		
		scoresColumnModel = new ColumnModel(scoresConfigs);  

		scoresColumnModel.addHeaderGroup(0, 0, new HeaderGroupConfig("Weighted", 1, 7));  
		
		scoresColumnModel.addHeaderGroup(1, 0, new HeaderGroupConfig("Structure 1", 1, 3));  
		scoresColumnModel.addHeaderGroup(1, 3, new HeaderGroupConfig("Structure 2", 1, 3));  
		
		scoresColumnModel.addHeaderGroup(0, 7, new HeaderGroupConfig("Unweighted", 1, 7));  
		
		scoresColumnModel.addHeaderGroup(1, 7, new HeaderGroupConfig("Structure 1", 1, 3));  
		scoresColumnModel.addHeaderGroup(1, 10, new HeaderGroupConfig("Structure 2", 1, 3));  
		
		scoresGrid = new Grid<BeanModel>(scoresStore, scoresColumnModel);  
//		scoresGrid.getView().setForceFit(true);  
		scoresGrid.setBorders(true);  
		scoresGrid.setStripeRows(true);
		scoresGrid.setColumnLines(true);
		
		GroupingView view = new GroupingView();  
	    view.setShowGroupedColumn(false);  
	    view.setForceFit(true);  
	    view.setGroupRenderer(new GridGroupRenderer() {  
	    	public String render(GroupColumnData data) {  
	    		String f = scoresColumnModel.getColumnById(data.field).getHeader();  
	    		String l = data.models.size() == 1 ? "Item" : "Items";  
	    		return f + ": " + data.group + " (" + data.models.size() + " " + l + ")";  
	    	}  
	    });  
	    
	    scoresGrid.setView(view);
		
		this.add(scoresGrid);  
		   
	}
	
	public void fillResultsGrid(PDBScoreItem resultsData,  
								int selectedInterface)
	{
		scoresStore.removeAll();
		
		List<BeanModel> data = new ArrayList<BeanModel>();
		
		///TODO
		for(InterfaceScoreItemKey key : resultsData.getInterfaceScores().keySet())
		{
			if(key.getInterfaceId() == selectedInterface)
			{
				InterfaceScoreItem interfaceScoreItem = resultsData.getInterfaceScores().get(key);
				
				if(interfaceScoreItem != null)
				{
					BeanModelFactory beanModelFactory = BeanModelLookup.get().getFactory(InterfaceScoreItem.class);
					BeanModel scoresModel = beanModelFactory.createModel(interfaceScoreItem);
//					ScoresModel scoresModel = new ScoresModel("");
//					scoresModel.set("unweightedrim1", interfaceScoreItem.getUnweightedRim1Scores());
//					scoresModel.set("weightedrim1", interfaceScoreItem.getWeightedRim1Scores());
//					scoresModel.set("unweightedcore1", interfaceScoreItem.getUnweightedCore1Scores());
//					scoresModel.set("weightedcore1", interfaceScoreItem.getWeightedCore1Scores());
//					scoresModel.set("unweightedrim2", interfaceScoreItem.getUnweightedRim2Scores());
//					scoresModel.set("weightedrim2", interfaceScoreItem.getWeightedRim2Scores());
//					scoresModel.set("unweightedcore2", interfaceScoreItem.getUnweightedCore2Scores());
//					scoresModel.set("weightedcore2", interfaceScoreItem.getWeightedCore2Scores());
//					scoresModel.set("unweightedscore", interfaceScoreItem.getUnweightedFinalScores());
//					scoresModel.set("weightedscore", interfaceScoreItem.getWeightedFinalScores());
//					
//					scoresModel.set("weightedrat1", interfaceScoreItem.getWeightedRatio1Scores());
//					scoresModel.set("unweightedrat1", interfaceScoreItem.getUnweightedRatio1Scores());
//					scoresModel.set("weightedrat2", interfaceScoreItem.getWeightedRatio2Scores());
//					scoresModel.set("unweightedrat2", interfaceScoreItem.getUnweightedRatio2Scores());
//					
//					scoresModel.set("method", interfaceScoreItem.getMethod());
					
					data.add(scoresModel);
				}
				
				scoresStore.add(data);
				scoresGrid.reconfigure(scoresStore, scoresColumnModel);
			}
		}
	}
	
	private List<ColumnConfig> createColumnConfig()
	{
		List<ColumnConfig> configs = new ArrayList<ColumnConfig>();  
		   
		BeanModelFactory beanModelFactory = BeanModelLookup.get().getFactory(InterfaceScoreItem.class);
		BeanModel model = beanModelFactory.createModel(new InterfaceScoreItem());
		
//		String list = "";
//		
//		for(int i=0; i<model.getPropertyNames().size(); i++)
//		{
//			list = list + "," + model.getProperties().values().;
//		}
//		
		String columnOrder = mainController.getSettings().getGridProperties().get("scores_columns");
		
		String[] columns = null;
		
		if(columnOrder == null)
		{
			columns = new String[model.getPropertyNames().size()];
			
			Iterator<String> fieldsIterator = model.getPropertyNames().iterator();
			
			int i=0;
			
			while(fieldsIterator.hasNext())
			{
				columns[i] = fieldsIterator.next();
				i++;
			}
		}
		else
		{
			columns = columnOrder.split(",");
		}
			
		for(String columnName : columns)
		{
			boolean addColumn = true;
			
			String customAdd = mainController.getSettings().getGridProperties().get("scores_" + columnName + "_add");
			if(customAdd != null)
			{
				if(!customAdd.equals("yes"))
				{
					addColumn = false;
				}
			}
			
			if(addColumn)
			{
				boolean displayColumn = true;
				
				String customVisibility = mainController.getSettings().getGridProperties().get("scores_" + columnName + "_visible");
				if(customVisibility != null)
				{
					if(!customVisibility.equals("yes"))
					{
						displayColumn = false;
					}
				}
				
				
				
				int columnWidth = 75;
				String customColumnWidth = mainController.getSettings().getGridProperties().get("scores_" + columnName + "_width");
				if(customColumnWidth != null)
				{
					columnWidth = Integer.parseInt(customColumnWidth);
				}
				
				
				
				String customRenderer = mainController.getSettings().getGridProperties().get("scores_" + columnName + "_renderer");
				
				GridCellRenderer<BeanModel> renderer = null;
				if((customRenderer != null) && (!customRenderer.equals("")))
				{
					renderer = GridCellRendererFactory.createGridCellRenderer(customRenderer, mainController);
				}
				
				String header = columnName;
				String customHeader = mainController.getSettings().getGridProperties().get("scores_" + columnName + "_header");
				if(customHeader != null)
				{
					header = customHeader;
				}
				
				ColumnConfig column = new ColumnConfig();
				column.setId(columnName);  
				column.setHeader(header);  
				column.setWidth(columnWidth); 
				column.setAlignment(HorizontalAlignment.CENTER);

				column.setHidden(!displayColumn);
				
				if(renderer != null)
				{
					column.setRenderer(renderer);
				}
				
				configs.add(column);  
			}
		}
		
		return configs;
		
//		List<ColumnConfig> configs = new ArrayList<ColumnConfig>();  
//		   
//		ColumnConfig column = new ColumnConfig();  
//		column.setId("weightedcore1");  
//		column.setHeader("core1");  
//		column.setWidth(75); 
//		column.setAlignment(HorizontalAlignment.CENTER);
//		configs.add(column); 
//		
//		column = new ColumnConfig();  
//		column.setId("weightedrim1");  
//		column.setHeader("rim1");  
//		column.setWidth(75); 
//		column.setAlignment(HorizontalAlignment.CENTER);
//		configs.add(column); 
//		
//		column = new ColumnConfig();  
//		column.setId("weightedrat1");  
//		column.setHeader("rat1");  
//		column.setWidth(75); 
//		column.setAlignment(HorizontalAlignment.CENTER);
//		configs.add(column); 
//		
//		column = new ColumnConfig();  
//		column.setId("weightedcore2");  
//		column.setHeader("core2");  
//		column.setWidth(75);  
//		column.setAlignment(HorizontalAlignment.CENTER);
//		configs.add(column); 
//   
//		column = new ColumnConfig();  
//		column.setId("weightedrim2");  
//		column.setHeader("rim2");  
//		column.setWidth(75);  
//		column.setAlignment(HorizontalAlignment.CENTER);
//		configs.add(column); 
//		
//		column = new ColumnConfig();  
//		column.setId("weightedrat2");  
//		column.setHeader("rat2");  
//		column.setWidth(75);
//		column.setAlignment(HorizontalAlignment.CENTER);  
//		configs.add(column); 
//		
//		column = new ColumnConfig();  
//		column.setId("weightedscore");  
//		column.setHeader("score");  
//		column.setWidth(75);  
//		column.setAlignment(HorizontalAlignment.CENTER);
//		configs.add(column); 
//		
//		column = new ColumnConfig();  
//		column.setId("unweightedcore1");  
//		column.setHeader("core1");  
//		column.setWidth(75); 
//		column.setAlignment(HorizontalAlignment.CENTER);
//		configs.add(column); 
//		
//		column = new ColumnConfig();  
//		column.setId("unweightedrim1");  
//		column.setHeader("rim1");  
//		column.setWidth(75);  
//		column.setAlignment(HorizontalAlignment.CENTER);
//		configs.add(column); 
//		
//		column = new ColumnConfig();  
//		column.setId("unweightedrat1");  
//		column.setHeader("rat1");  
//		column.setWidth(75);  
//		column.setAlignment(HorizontalAlignment.CENTER);
//		configs.add(column); 
//		
//		column = new ColumnConfig();  
//		column.setId("unweightedcore2");  
//		column.setHeader("core2");  
//		column.setWidth(75);  
//		column.setAlignment(HorizontalAlignment.CENTER);
//		configs.add(column); 
//   
//		column = new ColumnConfig();  
//		column.setId("unweightedrim2");  
//		column.setHeader("rim2");  
//		column.setWidth(75);  
//		column.setAlignment(HorizontalAlignment.CENTER);
//		configs.add(column); 
//		
//		column = new ColumnConfig();  
//		column.setId("unweightedrat2");  
//		column.setHeader("rat2");  
//		column.setWidth(75);  
//		column.setAlignment(HorizontalAlignment.CENTER);
//		configs.add(column); 
//		
//		column = new ColumnConfig();  
//		column.setId("unweightedscore");  
//		column.setHeader("score");  
//		column.setWidth(75);  
//		column.setAlignment(HorizontalAlignment.CENTER);
//		configs.add(column); 
//		
//		column = new ColumnConfig();  
//		column.setId("method");  
//		column.setHeader("method");  
//		column.setWidth(75);  
//		column.setAlignment(HorizontalAlignment.CENTER);
//		configs.add(column); 
//		
//		return configs;
	}
}
