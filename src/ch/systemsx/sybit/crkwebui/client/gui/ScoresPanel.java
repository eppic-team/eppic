package ch.systemsx.sybit.crkwebui.client.gui;

import java.util.ArrayList;
import java.util.List;

import model.InterfaceScore;
import model.PdbScore;
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
	
	private PdbScore resultsData;
	
	public ScoresPanel(PdbScore resultsData)
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
	
	public void fillResultsGrid(PdbScore resultsData,  
								int selectedInterface)
	{
		scoresStore.removeAll();
		
		List<ScoresModel> data = new ArrayList<ScoresModel>();
		
		InterfaceScore interfaceScore = resultsData.getInterfScore(selectedInterface);
		
		//TODO remove this loop
		for(int i=0; i<2; i++)
		{
			if(interfaceScore != null)
			{
				ScoresModel scoresModel = new ScoresModel(String.valueOf(interfaceScore.getRim1Scores()[0]));
				scoresModel.set("unweightedrim1", interfaceScore.getRim1Scores()[0]);
				scoresModel.set("weightedrim1", interfaceScore.getRim1Scores()[1]);
				scoresModel.set("unweightedcore1", interfaceScore.getCore1Scores()[0]);
				scoresModel.set("weightedcore1", interfaceScore.getCore1Scores()[1]);
				scoresModel.set("unweightedrim2", interfaceScore.getRim2Scores()[0]);
				scoresModel.set("weightedrim2", interfaceScore.getRim2Scores()[1]);
				scoresModel.set("unweightedcore2", interfaceScore.getCore2Scores()[0]);
				scoresModel.set("weightedcore2", interfaceScore.getCore2Scores()[1]);
				scoresModel.set("unweightedscore", interfaceScore.getFinalScores()[0]);
				scoresModel.set("weightedscore", interfaceScore.getFinalScores()[1]);
				
				scoresModel.set("weightedrat1", interfaceScore.getRatio1Scores()[0]);
				scoresModel.set("unweightedrat1", interfaceScore.getRatio1Scores()[1]);
				scoresModel.set("weightedrat2", interfaceScore.getRatio2Scores()[0]);
				scoresModel.set("unweightedrat2", interfaceScore.getRatio2Scores()[1]);
				
				scoresModel.set("method", "Entropy");
				
				if(i == 1)
				{
					scoresModel.set("method", "KAKS");
				}
				
				data.add(scoresModel);
			}
		}
		
		scoresStore.add(data);
		scoresGrid.reconfigure(scoresStore, scoresColumnModel);
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
