package ch.systemsx.sybit.crkwebui.client.gui;

import java.util.ArrayList;
import java.util.List;

import ch.systemsx.sybit.crkwebui.client.controllers.MainController;
import ch.systemsx.sybit.crkwebui.client.model.MyJobsModel;
import ch.systemsx.sybit.crkwebui.shared.model.StatusData;

import com.extjs.gxt.ui.client.event.ButtonEvent;
import com.extjs.gxt.ui.client.event.IconButtonEvent;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.store.ListStore;
import com.extjs.gxt.ui.client.widget.ContentPanel;
import com.extjs.gxt.ui.client.widget.MessageBox;
import com.extjs.gxt.ui.client.widget.Window;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.extjs.gxt.ui.client.widget.button.ToolButton;
import com.extjs.gxt.ui.client.widget.grid.ColumnConfig;
import com.extjs.gxt.ui.client.widget.grid.ColumnData;
import com.extjs.gxt.ui.client.widget.grid.ColumnModel;
import com.extjs.gxt.ui.client.widget.grid.Grid;
import com.extjs.gxt.ui.client.widget.grid.GridCellRenderer;
import com.extjs.gxt.ui.client.widget.layout.FitLayout;
import com.extjs.gxt.ui.client.widget.toolbar.ToolBar;
import com.google.gwt.user.client.History;
import com.google.gwt.user.client.ui.Hyperlink;

public class MyJobsPanel extends ContentPanel
{
	private MainController mainController;
	
	private Grid<MyJobsModel> myJobsGrid;
	private ListStore<MyJobsModel> myJobsStore;
	private ColumnModel myJobsColumnModel;
	
	private Button addNew;
	private Button killJob;
	
	public MyJobsPanel(final MainController mainController)
	{
		this.mainController = mainController;
		this.setLayout(new FitLayout());
		this.setHeading("My Jobs");
		
		ToolBar toolBar = new ToolBar();  
		
		addNew = new Button("New", new SelectionListener<ButtonEvent>() {  
			   
			public void componentSelected(ButtonEvent ce) 
			{
				History.newItem("");
				mainController.displayInputView();
			}  
		}); 
		
		toolBar.add(addNew);  
		
		killJob = new Button("Stop", new SelectionListener<ButtonEvent>() {  
			   
			public void componentSelected(ButtonEvent ce) 
			{
				MyJobsModel selectedItem = myJobsGrid.getSelectionModel().getSelectedItem();
				
				if(selectedItem != null)
				{
					mainController.killJob(selectedItem.getJobId());
				}
				else
				{
					MessageBox.alert("Stopping job", "No job selected from the list", null);
				}
			}  
		}); 
		
		toolBar.add(killJob);  
		
		Button test = new Button("Test", new SelectionListener<ButtonEvent>() {  
			   
			public void componentSelected(ButtonEvent ce) 
			{
				openJmol("");
			}
		}); 
		
		toolBar.add(test);  
		
		this.setTopComponent(toolBar);
		
		this.getHeader().addTool(  
				new ToolButton("x-tool-gear", new SelectionListener<IconButtonEvent>() {  
	   
				public void componentSelected(IconButtonEvent ce) 
				{  
					mainController.getJobsForCurrentSession();
				}  
	   
        })); 
		
		GridCellRenderer<MyJobsModel> jobRenderer = new GridCellRenderer<MyJobsModel>() {  

			@Override
			public Object render(MyJobsModel model, 
								 String property,
								 ColumnData config, 
								 int rowIndex, 
								 int colIndex,
								 ListStore<MyJobsModel> store, 
								 Grid<MyJobsModel> grid) 
			{
				String input = (String) model.get("input");
				String jobId = (String) model.get(property);
				
				Hyperlink link = new Hyperlink(input, "id/" + jobId);
		        return link;  
			}  
	    };  
		
		GridCellRenderer<MyJobsModel> statusRenderer = new GridCellRenderer<MyJobsModel>() {  

			@Override
			public Object render(MyJobsModel model, 
								 String property,
								 ColumnData config, 
								 int rowIndex, 
								 int colIndex,
								 ListStore<MyJobsModel> store, 
								 Grid<MyJobsModel> grid) 
			{
				String value = (String) model.get(property);
				String color = "black";
				
				if(value == null)
				{
					return value;
				}
				else if(value.equals("Error"))
				{
					color = "red";
				}
				else if(value.equals("Finished"))
				{
					color = "green";
				}
				else
				{
					return value;
				}
					
		        return "<span qtitle='" + myJobsColumnModel.getColumnById(property).getHeader() + "' qtip='" + value  
		            + "' style='font-weight: bold;color:" + color + "'>" + value + "</span>";  
			}  
	    };  
		
		List<ColumnConfig> configs = new ArrayList<ColumnConfig>();  
		  
	    ColumnConfig column = new ColumnConfig();  
	    column.setId("jobId");  
	    column.setHeader("Job ID"); 
	    column.setDataIndex("jobId");
	    column.setWidth(100);  
	    column.setRowHeader(true); 
	    column.setRenderer(jobRenderer);
	    configs.add(column);
	    
	    column = new ColumnConfig();  
	    column.setId("status");  
	    column.setHeader("Status"); 
	    column.setDataIndex("status");
	    column.setWidth(80);  
	    column.setRowHeader(true);
	    column.setRenderer(statusRenderer);
	    configs.add(column);
	  
	    myJobsStore = new ListStore<MyJobsModel>();  
	    myJobsColumnModel = new ColumnModel(configs);  
	  
	    myJobsGrid = new Grid<MyJobsModel>(myJobsStore, myJobsColumnModel);
	    myJobsGrid.setStyleAttribute("borderTop", "none");  
	    myJobsGrid.setAutoExpandColumn("jobId");  
	    myJobsGrid.setBorders(false);  
	    myJobsGrid.setStripeRows(true);  
	    myJobsGrid.setColumnLines(true);  
	    myJobsGrid.setColumnReordering(true);
	    myJobsGrid.setAutoHeight(true);
	    myJobsGrid.setAutoWidth(true);
	    
		this.add(myJobsGrid);
	}
	
	public void setJobs(List<StatusData> jobs)
	{
		myJobsStore.removeAll();
		
		List<MyJobsModel> data = new ArrayList<MyJobsModel>();
		
		for(StatusData statusData : jobs)
		{
			MyJobsModel myJobsModel = new MyJobsModel(statusData.getJobId(),
													  statusData.getStatus(),
													  statusData.getInput());
			data.add(myJobsModel);
		}
		
		myJobsStore.add(data);
		myJobsGrid.reconfigure(myJobsStore, myJobsColumnModel);
	}
	
	public native void openJmol(String msg) /*-{
		var jmolWindow = window.open("", "Jmol");
		$wnd.jmolInitialize("http://localhost/jmol");
		$wnd.jmolSetDocument(jmolWindow.document);
        $wnd.jmolApplet(900,'load http://localhost/pdb1aor.pdb');
	}-*/;
}
