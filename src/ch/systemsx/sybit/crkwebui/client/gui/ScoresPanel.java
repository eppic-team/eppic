package ch.systemsx.sybit.crkwebui.client.gui;

import java.util.ArrayList;
import java.util.List;

import model.InterfaceScoreItem;
import model.InterfaceScoreItemKey;
import model.PDBScoreItem;
import ch.systemsx.sybit.crkwebui.client.model.ScoresModel;

import com.extjs.gxt.ui.client.Style.HorizontalAlignment;
import com.extjs.gxt.ui.client.store.GroupingStore;
import com.extjs.gxt.ui.client.widget.form.FormPanel;
import com.extjs.gxt.ui.client.widget.grid.ColumnConfig;
import com.extjs.gxt.ui.client.widget.grid.ColumnModel;
import com.extjs.gxt.ui.client.widget.grid.Grid;
import com.extjs.gxt.ui.client.widget.grid.GridGroupRenderer;
import com.extjs.gxt.ui.client.widget.grid.GroupColumnData;
import com.extjs.gxt.ui.client.widget.grid.GroupingView;
import com.extjs.gxt.ui.client.widget.grid.HeaderGroupConfig;
import com.extjs.gxt.ui.client.widget.layout.FitLayout;
import com.extjs.gxt.ui.client.widget.layout.FormData;

public class ScoresPanel extends FormPanel
{
	private List<ColumnConfig> scoresConfigs;
	private GroupingStore<ScoresModel> scoresStore;
	private ColumnModel scoresColumnModel;
	private Grid<ScoresModel> scoresGrid;
	
	private PDBScoreItem resultsData;
	
	public ScoresPanel(PDBScoreItem resultsData)
	{
		FormData formData = new FormData("100%");  
//		infoPanel.setFrame(true);  
		this.getHeader().setVisible(false);  
		this.setBodyBorder(false);
		this.setBorders(false);
		this.setLayout(new FitLayout());
		this.setPadding(0);
		
		scoresConfigs = createColumnConfig(); 
		   
		scoresStore = new GroupingStore<ScoresModel>();  
		scoresStore.groupBy("method");
		
		scoresColumnModel = new ColumnModel(scoresConfigs);  
   
		scoresColumnModel.addHeaderGroup(0, 0, new HeaderGroupConfig("Weighted", 1, 7));  
		
		scoresColumnModel.addHeaderGroup(1, 0, new HeaderGroupConfig("Structure 1", 1, 3));  
		scoresColumnModel.addHeaderGroup(1, 3, new HeaderGroupConfig("Structure 2", 1, 3));  
		
		scoresColumnModel.addHeaderGroup(0, 7, new HeaderGroupConfig("Unweighted", 1, 7));  
		
		scoresColumnModel.addHeaderGroup(1, 7, new HeaderGroupConfig("Structure 1", 1, 3));  
		scoresColumnModel.addHeaderGroup(1, 10, new HeaderGroupConfig("Structure 2", 1, 3));  
		
		scoresGrid = new Grid<ScoresModel>(scoresStore, scoresColumnModel);  
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
		
		List<ScoresModel> data = new ArrayList<ScoresModel>();
		
		///TODO
		for(InterfaceScoreItemKey key : resultsData.getInterfaceScores().keySet())
		{
			if(key.getInterfaceId() == selectedInterface)
			{
				InterfaceScoreItem interfaceScoreItem = resultsData.getInterfaceScores().get(key);
				
				if(interfaceScoreItem != null)
				{
					ScoresModel scoresModel = new ScoresModel("");
					scoresModel.set("unweightedrim1", interfaceScoreItem.getUnweightedRim1Scores());
					scoresModel.set("weightedrim1", interfaceScoreItem.getWeightedRim1Scores());
					scoresModel.set("unweightedcore1", interfaceScoreItem.getUnweightedCore1Scores());
					scoresModel.set("weightedcore1", interfaceScoreItem.getWeightedCore1Scores());
					scoresModel.set("unweightedrim2", interfaceScoreItem.getUnweightedRim2Scores());
					scoresModel.set("weightedrim2", interfaceScoreItem.getWeightedRim2Scores());
					scoresModel.set("unweightedcore2", interfaceScoreItem.getUnweightedCore2Scores());
					scoresModel.set("weightedcore2", interfaceScoreItem.getWeightedCore2Scores());
					scoresModel.set("unweightedscore", interfaceScoreItem.getUnweightedFinalScores());
					scoresModel.set("weightedscore", interfaceScoreItem.getWeightedFinalScores());
					
					scoresModel.set("weightedrat1", interfaceScoreItem.getWeightedRatio1Scores());
					scoresModel.set("unweightedrat1", interfaceScoreItem.getUnweightedRatio1Scores());
					scoresModel.set("weightedrat2", interfaceScoreItem.getWeightedRatio2Scores());
					scoresModel.set("unweightedrat2", interfaceScoreItem.getUnweightedRatio2Scores());
					
					scoresModel.set("method", interfaceScoreItem.getMethod());
					
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
		   
		ColumnConfig column = new ColumnConfig();  
		column.setId("weightedcore1");  
		column.setHeader("core1");  
		column.setWidth(75); 
		column.setAlignment(HorizontalAlignment.CENTER);
		configs.add(column); 
		
		column = new ColumnConfig();  
		column.setId("weightedrim1");  
		column.setHeader("rim1");  
		column.setWidth(75); 
		column.setAlignment(HorizontalAlignment.CENTER);
		configs.add(column); 
		
		column = new ColumnConfig();  
		column.setId("weightedrat1");  
		column.setHeader("rat1");  
		column.setWidth(75); 
		column.setAlignment(HorizontalAlignment.CENTER);
		configs.add(column); 
		
		column = new ColumnConfig();  
		column.setId("weightedcore2");  
		column.setHeader("core2");  
		column.setWidth(75);  
		column.setAlignment(HorizontalAlignment.CENTER);
		configs.add(column); 
   
		column = new ColumnConfig();  
		column.setId("weightedrim2");  
		column.setHeader("rim2");  
		column.setWidth(75);  
		column.setAlignment(HorizontalAlignment.CENTER);
		configs.add(column); 
		
		column = new ColumnConfig();  
		column.setId("weightedrat2");  
		column.setHeader("rat2");  
		column.setWidth(75);
		column.setAlignment(HorizontalAlignment.CENTER);  
		configs.add(column); 
		
		column = new ColumnConfig();  
		column.setId("weightedscore");  
		column.setHeader("score");  
		column.setWidth(75);  
		column.setAlignment(HorizontalAlignment.CENTER);
		configs.add(column); 
		
		column = new ColumnConfig();  
		column.setId("unweightedcore1");  
		column.setHeader("core1");  
		column.setWidth(75); 
		column.setAlignment(HorizontalAlignment.CENTER);
		configs.add(column); 
		
		column = new ColumnConfig();  
		column.setId("unweightedrim1");  
		column.setHeader("rim1");  
		column.setWidth(75);  
		column.setAlignment(HorizontalAlignment.CENTER);
		configs.add(column); 
		
		column = new ColumnConfig();  
		column.setId("unweightedrat1");  
		column.setHeader("rat1");  
		column.setWidth(75);  
		column.setAlignment(HorizontalAlignment.CENTER);
		configs.add(column); 
		
		column = new ColumnConfig();  
		column.setId("unweightedcore2");  
		column.setHeader("core2");  
		column.setWidth(75);  
		column.setAlignment(HorizontalAlignment.CENTER);
		configs.add(column); 
   
		column = new ColumnConfig();  
		column.setId("unweightedrim2");  
		column.setHeader("rim2");  
		column.setWidth(75);  
		column.setAlignment(HorizontalAlignment.CENTER);
		configs.add(column); 
		
		column = new ColumnConfig();  
		column.setId("unweightedrat2");  
		column.setHeader("rat2");  
		column.setWidth(75);  
		column.setAlignment(HorizontalAlignment.CENTER);
		configs.add(column); 
		
		column = new ColumnConfig();  
		column.setId("unweightedscore");  
		column.setHeader("score");  
		column.setWidth(75);  
		column.setAlignment(HorizontalAlignment.CENTER);
		configs.add(column); 
		
		column = new ColumnConfig();  
		column.setId("method");  
		column.setHeader("method");  
		column.setWidth(75);  
		column.setAlignment(HorizontalAlignment.CENTER);
		configs.add(column); 
		
		return configs;
	}
}
