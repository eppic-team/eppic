package ch.systemsx.sybit.crkwebui.client.gui;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import model.InterfaceScore;
import model.PdbScore;
import ch.systemsx.sybit.crkwebui.client.controllers.MainController;
import ch.systemsx.sybit.crkwebui.client.model.ResultsModel;

import com.extjs.gxt.ui.client.Style.HorizontalAlignment;
import com.extjs.gxt.ui.client.Style.Orientation;
import com.extjs.gxt.ui.client.event.ButtonEvent;
import com.extjs.gxt.ui.client.event.Events;
import com.extjs.gxt.ui.client.event.GridEvent;
import com.extjs.gxt.ui.client.event.Listener;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.store.ListStore;
import com.extjs.gxt.ui.client.util.Margins;
import com.extjs.gxt.ui.client.widget.BoxComponent;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.extjs.gxt.ui.client.widget.form.FormPanel;
import com.extjs.gxt.ui.client.widget.grid.ColumnConfig;
import com.extjs.gxt.ui.client.widget.grid.ColumnData;
import com.extjs.gxt.ui.client.widget.grid.ColumnModel;
import com.extjs.gxt.ui.client.widget.grid.Grid;
import com.extjs.gxt.ui.client.widget.grid.GridCellRenderer;
import com.extjs.gxt.ui.client.widget.layout.FitLayout;
import com.extjs.gxt.ui.client.widget.layout.RowData;
import com.extjs.gxt.ui.client.widget.layout.RowLayout;

public class OverviewPanel extends FormPanel 
{
	private MainController mainController;
	
	private List<ColumnConfig> resultsConfigs;
	private ListStore<ResultsModel> resultsStore;
	private ColumnModel resultsColumnModel;
	private Grid<ResultsModel> resultsGrid;
	private GridCellRenderer<ResultsModel> methodRenderer;
	private GridCellRenderer<ResultsModel> detailsButtonRenderer;
	
	private PdbScore resultsData;
	
	private InfoPanel infoPanel;
	private ScoresPanel scoresPanel;
	
	public OverviewPanel(MainController mainController, final PdbScore resultsData)
	{
		this.mainController = mainController;
		this.resultsData = resultsData;
		this.setBorders(true);
		this.setBodyBorder(false);
		this.getHeader().setVisible(false);
		this.setLayout(new RowLayout(Orientation.VERTICAL));
		
		detailsButtonRenderer = new GridCellRenderer<ResultsModel>() 
		{  
			  
			private boolean init;  
		  
			public Object render(final ResultsModel model, String property, ColumnData config, final int rowIndex,  
		          final int colIndex, ListStore<ResultsModel> store, Grid<ResultsModel> grid) 
			{  
		        if (!init) 
		        {  
		        	init = true;  
		        	grid.addListener(Events.ColumnResize, new Listener<GridEvent<ResultsModel>>() 
        			{  
		  
		        		public void handleEvent(GridEvent<ResultsModel> be) 
		        		{  
		        			for (int i = 0; i < be.getGrid().getStore().getCount(); i++) 
		        			{  
		        				if (be.getGrid().getView().getWidget(i, be.getColIndex()) != null  
		        						&& be.getGrid().getView().getWidget(i, be.getColIndex()) instanceof BoxComponent) 
		        				{  
		        					((BoxComponent) be.getGrid().getView().getWidget(i, be.getColIndex())).setWidth(be.getWidth() - 10);  
		        				}  
		        			}  
		        		}  
        			});  
		        }  
		  
		        Button detailsButton = new Button("Details", new SelectionListener<ButtonEvent>() {
					
					@Override
					public void componentSelected(ButtonEvent ce) 
					{
						InterfacesWindow iw = new InterfacesWindow(null);
						iw.show();
					}
				});
		        
		        detailsButton.setWidth(grid.getColumnModel().getColumnWidth(colIndex) - 10);  
		        detailsButton.setToolTip("Show interfaces details");  
		  
		        return detailsButton;  
			}  
	    };  
		    
		createInfoPanel();
		
		resultsConfigs = createColumnConfig(); 
   
		resultsStore = new ListStore<ResultsModel>();  
		
		resultsColumnModel = new ColumnModel(resultsConfigs);  
		
//		if (widget) {  
//			Slider s = new Slider();  
//			s.setWidth(100);  
//   
//			// ugly, but centers slider  
//			FlexTable tbl = new FlexTable();  
//			tbl.setWidth("100%");  
//			tbl.setHTML(0, 0, " ");  
//			tbl.setHTML(0, 1, "<span style='white-space: nowrap;font-size: 11px'>Slide Me:  </span>");  
//			tbl.setWidget(0, 2, s);  
//			tbl.setHTML(0, 3, " ");  
//			tbl.getCellFormatter().setWidth(0, 0, "50%");  
//			tbl.getCellFormatter().setWidth(0, 3, "50%");  
//			cm.addHeaderGroup(1, 0, new HeaderGroupConfig(tbl, 1, 2));  
//		} else {  
//			cm.addHeaderGroup(1, 0, new HeaderGroupConfig("Stock Information", 1, 2));  
//		}  
   
		resultsGrid = new Grid<ResultsModel>(resultsStore, resultsColumnModel);  
//		resultsGrid.setStyleAttribute("borderTop", "none");  
		resultsGrid.getView().setForceFit(true);  
		resultsGrid.setBorders(true);  
		resultsGrid.setStripeRows(true);
		resultsGrid.setColumnLines(true);
		
		final FormPanel scoresPanelWrapper = new FormPanel();
		scoresPanelWrapper.setLayout(new FitLayout());
		scoresPanelWrapper.setBodyBorder(false);
		scoresPanelWrapper.setBorders(false);
		scoresPanelWrapper.getHeader().setVisible(false);
		scoresPanelWrapper.setPadding(0);
		
		resultsGrid.addListener(Events.OnClick, new Listener<GridEvent>() 
		{
			@Override
			public void handleEvent(GridEvent be)
			{
				if(scoresPanel == null)
				{
					createScoresPanel();
					scoresPanelWrapper.add(scoresPanel);
					scoresPanelWrapper.layout();
				}
				
				scoresPanel.fillResultsGrid(resultsData, be.getRowIndex() + 1);
			}
		});
		
		this.add(resultsGrid, new RowData(1, 0.65, new Margins(0)));  
		
		FormPanel breakPanel = new FormPanel();
		breakPanel.setBorders(false);
		breakPanel.setBodyBorder(false);
		breakPanel.setPadding(0);
		breakPanel.getHeader().setVisible(false);
		this.add(breakPanel, new RowData(1, 0.05, new Margins(0))); 
		
		
//		scoresPanel.setVisible(false);
		this.add(scoresPanelWrapper, new RowData(1, 0.3, new Margins(0)));
	}
	
	private List<ColumnConfig> createColumnConfig()
	{
		List<ColumnConfig> configs = new ArrayList<ColumnConfig>();  
		   
		ColumnConfig column = new ColumnConfig();  
		column.setId("id");  
		column.setHeader("id");  
		column.setWidth(50); 
		column.setAlignment(HorizontalAlignment.CENTER);
		configs.add(column);  
		
		column = new ColumnConfig();  
		column.setId("interface");  
		column.setHeader("Interface");  
		column.setWidth(100); 
		column.setAlignment(HorizontalAlignment.CENTER);
		configs.add(column);  
   
		column = new ColumnConfig();  
		column.setId("area");  
		column.setHeader("Area");  
		column.setWidth(100); 
		column.setAlignment(HorizontalAlignment.CENTER);
		configs.add(column);  
   
		column = new ColumnConfig();  
		column.setId("size1");  
		column.setHeader("Size 1");  
		column.setWidth(75);
		column.setAlignment(HorizontalAlignment.CENTER);
		configs.add(column);  
		
		column = new ColumnConfig();  
		column.setId("size2");  
		column.setHeader("Size 2");  
		column.setWidth(75); 
		column.setAlignment(HorizontalAlignment.CENTER);
		configs.add(column); 
		
		column = new ColumnConfig();  
		column.setId("n1");  
		column.setHeader("n1");  
		column.setWidth(75);  
		column.setAlignment(HorizontalAlignment.CENTER);
		configs.add(column); 
		
		column = new ColumnConfig();  
		column.setId("n2");  
		column.setHeader("n2");  
		column.setWidth(75);  
		column.setAlignment(HorizontalAlignment.CENTER);
		configs.add(column); 
		
		column = new ColumnConfig();  
		column.setId("entropy");  
		column.setHeader("Entropy");  
		column.setWidth(75);  
		column.setAlignment(HorizontalAlignment.CENTER);
		column.setRenderer(methodRenderer);
		configs.add(column); 
		
		column = new ColumnConfig();  
		column.setId("kaks");  
		column.setHeader("Kaks");  
		column.setWidth(75);  
		column.setAlignment(HorizontalAlignment.CENTER);
		configs.add(column); 
		
		column = new ColumnConfig();  
		column.setId("final");  
		column.setHeader("Final");  
		column.setWidth(75);  
		column.setAlignment(HorizontalAlignment.CENTER);
		configs.add(column); 
		
		column = new ColumnConfig();  
		column.setId("details");  
		column.setHeader("");  
		column.setWidth(50);  
		column.setRenderer(detailsButtonRenderer);
		column.setAlignment(HorizontalAlignment.CENTER);
		configs.add(column); 
		
		return configs;
	}
	
	public void setResults(PdbScore resultsData)
	{
		fillResultsGrid(resultsData);
	}
	
	public void fillResultsGrid(PdbScore resultsData)
	{
		resultsStore.removeAll();
		
		List<ResultsModel> data = new ArrayList<ResultsModel>();
		
		Map<Integer, InterfaceScore> interfacesMap = resultsData.getInterfaceScoreMap();
		
		if(interfacesMap != null)
		{
			for(InterfaceScore interfaceScore : interfacesMap.values())
			{
				ResultsModel resultsModel = new ResultsModel(String.valueOf(interfaceScore.getRim1Scores()[0]));
				resultsModel.set("id", interfaceScore.getId());
				resultsModel.set("interface", interfaceScore.getFirstChainId() + " + " + interfaceScore.getSecondChainId());
				resultsModel.set("area", interfaceScore.getInterfArea());
				resultsModel.set("size1", interfaceScore.getCoreSize1()[0]);
				resultsModel.set("size2", interfaceScore.getCoreSize2()[0]);
				resultsModel.set("n1", interfaceScore.getNumHomologs1());
				resultsModel.set("n2", interfaceScore.getNumHomologs2());
				resultsModel.set("entropy", "Bio");
				
				data.add(resultsModel);
			}
		}
		
		resultsStore.add(data);
		resultsGrid.reconfigure(resultsStore, resultsColumnModel);
	}
	
	private void createInfoPanel()
	{
		infoPanel = new InfoPanel(resultsData);
		this.add(infoPanel, new RowData(1, -1, new Margins(0)));
	}
	
	private void createScoresPanel()
	{
		scoresPanel = new ScoresPanel(resultsData);
//		this.add(scoresPanel, new RowData(1, 0.3, new Margins(0)));
	}
}
