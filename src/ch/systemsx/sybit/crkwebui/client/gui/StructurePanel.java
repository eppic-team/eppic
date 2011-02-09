package ch.systemsx.sybit.crkwebui.client.gui;

import java.util.ArrayList;
import java.util.List;

import model.PdbScore;
import ch.systemsx.sybit.crkwebui.client.model.InterfacesModel;

import com.extjs.gxt.ui.client.Style.HorizontalAlignment;
import com.extjs.gxt.ui.client.store.GroupingStore;
import com.extjs.gxt.ui.client.widget.form.FormPanel;
import com.extjs.gxt.ui.client.widget.grid.ColumnConfig;
import com.extjs.gxt.ui.client.widget.grid.ColumnModel;
import com.extjs.gxt.ui.client.widget.grid.Grid;
import com.extjs.gxt.ui.client.widget.grid.HeaderGroupConfig;
import com.extjs.gxt.ui.client.widget.layout.FitLayout;

public class StructurePanel extends FormPanel 
{
	private List<ColumnConfig> interfacesConfigs;
	private GroupingStore<InterfacesModel> interfacesStore;
	private ColumnModel interfacesColumnModel;
	private Grid<InterfacesModel> interfacesGrid;
	
	private PdbScore resultsData;
	
	public StructurePanel(PdbScore resultsData, String header)
	{
		this.resultsData = resultsData;
		
		this.setBodyBorder(false);
		this.setBorders(false);
		this.setLayout(new FitLayout());
		this.setPadding(0);
		this.getHeader().setVisible(false);
		
		interfacesConfigs = createColumnConfig(); 
		   
		interfacesStore = new GroupingStore<InterfacesModel>();
		
		List<InterfacesModel> data = new ArrayList<InterfacesModel>();
		
		for(int j=0; j<100; j++)
		{
			InterfacesModel interfacesModel = new InterfacesModel("a");
			interfacesModel.set("seq", j);
			interfacesModel.set("res", j);
			interfacesModel.set("asa", j);
			interfacesModel.set("bsa", j);
			interfacesModel.set("%bsa", j);
			interfacesModel.set("entropy", j);
			interfacesModel.set("kaks", j);
			
			data.add(interfacesModel);
		}
		
		interfacesStore.add(data);
		
		interfacesColumnModel = new ColumnModel(interfacesConfigs); 
		
		interfacesColumnModel.addHeaderGroup(0, 0, new HeaderGroupConfig(header, 1, 7));  
		
		interfacesGrid = new Grid<InterfacesModel>(interfacesStore, interfacesColumnModel);  
		interfacesGrid.setBorders(true);  
		interfacesGrid.setStripeRows(true);
		interfacesGrid.setColumnLines(true);
		interfacesGrid.getView().setForceFit(true);
//		interfacesGrid.setAutoHeight(true);
		
		this.add(interfacesGrid);
		
	}
	
	private List<ColumnConfig> createColumnConfig()
	{
		List<ColumnConfig> configs = new ArrayList<ColumnConfig>();  
		   
		ColumnConfig column = new ColumnConfig();  
		column.setId("seq");  
		column.setHeader("seq");  
		column.setWidth(75); 
		column.setAlignment(HorizontalAlignment.CENTER);
		configs.add(column); 
		
		column = new ColumnConfig();  
		column.setId("res");  
		column.setHeader("res");  
		column.setWidth(75); 
		column.setAlignment(HorizontalAlignment.CENTER);
		configs.add(column); 
		
		column = new ColumnConfig();  
		column.setId("asa");  
		column.setHeader("asa");  
		column.setWidth(75); 
		column.setAlignment(HorizontalAlignment.CENTER);
		configs.add(column); 
		
		column = new ColumnConfig();  
		column.setId("bsa");  
		column.setHeader("bsa");  
		column.setWidth(75);  
		column.setAlignment(HorizontalAlignment.CENTER);
		configs.add(column); 
   
		column = new ColumnConfig();  
		column.setId("%bsa");  
		column.setHeader("%bsa");  
		column.setWidth(75);  
		column.setAlignment(HorizontalAlignment.CENTER);
		configs.add(column); 
		
		column = new ColumnConfig();  
		column.setId("entropy");  
		column.setHeader("entropy");  
		column.setWidth(75);
		column.setAlignment(HorizontalAlignment.CENTER);  
		configs.add(column); 
		
		column = new ColumnConfig();  
		column.setId("kaks");  
		column.setHeader("kaks");  
		column.setWidth(75);  
		column.setAlignment(HorizontalAlignment.CENTER);
		configs.add(column); 
		
		return configs;
	}
}
