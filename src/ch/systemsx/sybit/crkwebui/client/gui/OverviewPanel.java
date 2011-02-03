package ch.systemsx.sybit.crkwebui.client.gui;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import ch.systemsx.sybit.crkwebui.client.controllers.MainController;
import ch.systemsx.sybit.crkwebui.client.data.InterfaceScore;
import ch.systemsx.sybit.crkwebui.client.data.ResultsData;

import com.extjs.gxt.ui.client.GXT;  
import com.extjs.gxt.ui.client.Style.HorizontalAlignment;  
import com.extjs.gxt.ui.client.Style.Orientation;
import com.extjs.gxt.ui.client.aria.FocusManager;  
import com.extjs.gxt.ui.client.event.BaseEvent;  
import com.extjs.gxt.ui.client.event.Listener;  
import com.extjs.gxt.ui.client.store.ListStore;
import com.extjs.gxt.ui.client.util.Margins;
import com.extjs.gxt.ui.client.util.Padding;
import com.extjs.gxt.ui.client.util.Scroll;
import com.extjs.gxt.ui.client.widget.ContentPanel;
import com.extjs.gxt.ui.client.widget.Label;
import com.extjs.gxt.ui.client.widget.LayoutContainer;  
import com.extjs.gxt.ui.client.widget.TabItem;  
import com.extjs.gxt.ui.client.widget.TabPanel;  
import com.extjs.gxt.ui.client.widget.VerticalPanel;  
import com.extjs.gxt.ui.client.widget.button.Button;  
import com.extjs.gxt.ui.client.widget.form.DateField;  
import com.extjs.gxt.ui.client.widget.form.FormPanel;  
import com.extjs.gxt.ui.client.widget.form.HtmlEditor;  
import com.extjs.gxt.ui.client.widget.form.Radio;  
import com.extjs.gxt.ui.client.widget.form.RadioGroup;  
import com.extjs.gxt.ui.client.widget.form.TextField;  
import com.extjs.gxt.ui.client.widget.form.FormPanel.LabelAlign;  
import com.extjs.gxt.ui.client.widget.grid.ColumnConfig;
import com.extjs.gxt.ui.client.widget.grid.ColumnModel;
import com.extjs.gxt.ui.client.widget.grid.Grid;
import com.extjs.gxt.ui.client.widget.grid.GridView;
import com.extjs.gxt.ui.client.widget.grid.GridViewConfig;
import com.extjs.gxt.ui.client.widget.grid.HeaderGroupConfig;
import com.extjs.gxt.ui.client.widget.layout.ColumnData;  
import com.extjs.gxt.ui.client.widget.layout.ColumnLayout;  
import com.extjs.gxt.ui.client.widget.layout.FitLayout;  
import com.extjs.gxt.ui.client.widget.layout.FormData;  
import com.extjs.gxt.ui.client.widget.layout.FormLayout;  
import com.extjs.gxt.ui.client.widget.layout.HBoxLayout;
import com.extjs.gxt.ui.client.widget.layout.HBoxLayout.HBoxLayoutAlign;
import com.extjs.gxt.ui.client.widget.layout.RowData;
import com.extjs.gxt.ui.client.widget.layout.RowLayout;
import com.extjs.gxt.ui.client.widget.layout.VBoxLayout;
import com.extjs.gxt.ui.client.widget.layout.VBoxLayout.VBoxLayoutAlign;
import com.extjs.gxt.ui.client.widget.layout.VBoxLayoutData;
import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.Element;  
import com.google.gwt.user.client.ui.HTML;

public class OverviewPanel extends ContentPanel 
{
	private MainController mainController;
	
	private HTML downloadResultsLink;
	
	private List<ColumnConfig> resultsConfigs;
	private ListStore<ResultsModel> resultsStore;
	private ColumnModel resultsColumnModel;
	private Grid<ResultsModel> resultsGrid;
	
	private ResultsData resultsData;
	
	public OverviewPanel(MainController mainController, ResultsData resultsData)
	{
		this.mainController = mainController;
		this.resultsData = resultsData;
		this.setBorders(false);
		this.setBodyBorder(false);
		this.getHeader().setVisible(false);
		this.setLayout(new RowLayout(Orientation.VERTICAL));
		
		createInfoPanel();
		
		resultsConfigs = createColumnConfig(); 
   
		resultsStore = new ListStore<ResultsModel>();  
   
		resultsColumnModel = new ColumnModel(resultsConfigs);  
   
		resultsColumnModel.addHeaderGroup(0, 7, new HeaderGroupConfig("Weighted", 1, 7));  
		
		resultsColumnModel.addHeaderGroup(1, 7, new HeaderGroupConfig("Structure 1", 1, 3));  
		resultsColumnModel.addHeaderGroup(1, 10, new HeaderGroupConfig("Structure 2", 1, 3));  
		
		resultsColumnModel.addHeaderGroup(0, 14, new HeaderGroupConfig("Unweighted", 1, 7));  
		
		resultsColumnModel.addHeaderGroup(1, 14, new HeaderGroupConfig("Structure 1", 1, 3));  
		resultsColumnModel.addHeaderGroup(1, 17, new HeaderGroupConfig("Structure 2", 1, 3));  
		
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
		resultsGrid.setStyleAttribute("borderTop", "none");  
		resultsGrid.getView().setForceFit(true);  
		resultsGrid.setBorders(true);  
		resultsGrid.setStripeRows(true);
		resultsGrid.setColumnLines(true);
		
		this.add(resultsGrid, new RowData(1, 1, new Margins(0)));  
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
		
		return configs;
	}
	
	public void setResults(ResultsData resultsData)
	{
		fillResultsGrid(resultsData);
	}
	
	public void fillResultsGrid(ResultsData resultsData)
	{
		resultsStore.removeAll();
		
		List<ResultsModel> data = new ArrayList<ResultsModel>();
		
		Map<Integer, InterfaceScore> interfacesMap = resultsData.getInterfScoreMap();
		
		if(interfacesMap != null)
		{
			for(InterfaceScore interfaceScore : interfacesMap.values())
			{
				ResultsModel resultsModel = new ResultsModel(String.valueOf(interfaceScore.getRim1Scores()[0]));
				resultsModel.set("id", interfaceScore.getId());
				resultsModel.set("interface", interfaceScore.getFirstChainId() + " + " + interfaceScore.getSecondChainId());
				resultsModel.set("area", interfaceScore.getInterfArea());
				resultsModel.set("size1", interfaceScore.getCoreSize1());
				resultsModel.set("size2", interfaceScore.getCoreSize2());
				resultsModel.set("n1", interfaceScore.getNumHomologs1());
				resultsModel.set("n2", interfaceScore.getNumHomologs2());
				resultsModel.set("unweightedrim1", interfaceScore.getRim1Scores()[0]);
				resultsModel.set("weightedrim1", interfaceScore.getRim1Scores()[1]);
				resultsModel.set("unweightedcore1", interfaceScore.getCore1Scores()[0]);
				resultsModel.set("weightedcore1", interfaceScore.getCore1Scores()[1]);
				resultsModel.set("unweightedrim2", interfaceScore.getRim2Scores()[0]);
				resultsModel.set("weightedrim2", interfaceScore.getRim2Scores()[1]);
				resultsModel.set("unweightedcore2", interfaceScore.getCore2Scores()[0]);
				resultsModel.set("weightedcore2", interfaceScore.getCore2Scores()[1]);
				resultsModel.set("unweightedscore", interfaceScore.getFinalScores()[0]);
				resultsModel.set("weightedscore", interfaceScore.getFinalScores()[1]);
				
				resultsModel.set("weightedrat1", "");
				resultsModel.set("unweightedrat1", "");
				resultsModel.set("weightedrat2", "");
				resultsModel.set("unweightedrat2", "");
				
				data.add(resultsModel);
			}
		}
		
		resultsStore.add(data);
		resultsGrid.reconfigure(resultsStore, resultsColumnModel);
	}
	
	private void createInfoPanel()
	{
		FormData formData = new FormData("100%");  
		FormPanel infoPanel = new FormPanel();  
		infoPanel.setFrame(true);  
		infoPanel.getHeader().setVisible(false);  
		infoPanel.setBodyBorder(false);
		infoPanel.setBorders(false);
		   
     	LayoutContainer main = new LayoutContainer();  
     	main.setLayout(new ColumnLayout());  
     	main.setBorders(false);
		   
     	VBoxLayout vBoxLayout = new VBoxLayout();  
     	vBoxLayout.setPadding(new Padding(5));  
     	vBoxLayout.setVBoxLayoutAlign(VBoxLayoutAlign.LEFT);  
     	VBoxLayoutData vBoxLayoutData = new VBoxLayoutData(new Margins(0));
     	vBoxLayoutData.setFlex(1);
     	
     	LayoutContainer left = new LayoutContainer(); 
     	left.setHeight(80);
     	left.setStyleAttribute("paddingRight", "10px");
     	
     	left.setLayout(vBoxLayout);  
   
     	Label label = new Label("PDB identifier: " + resultsData.getPdbName());
     	left.add(label, vBoxLayoutData);  
     	
     	label = new Label("Score method:");
     	left.add(label, vBoxLayoutData);  
     	
     	label = new Label("Score type: ");
     	left.add(label, vBoxLayoutData);  
     	
     	label = new Label("Total core size xtal-call cutoff: ");
     	left.add(label, vBoxLayoutData);
     	
     	vBoxLayout = new VBoxLayout();  
     	vBoxLayout.setPadding(new Padding(5));  
     	vBoxLayout.setVBoxLayoutAlign(VBoxLayoutAlign.LEFT);  
     	vBoxLayoutData = new VBoxLayoutData(new Margins(0));
     	vBoxLayoutData.setFlex(1);
     	
     	LayoutContainer center = new LayoutContainer();  
     	center.setStyleAttribute("paddingRight", "10px");  
     	center.setStyleAttribute("paddingLeft", "10px"); 
     	center.setHeight(80);
     	center.setLayout(vBoxLayout);  
   
     	label = new Label("Min number homologs required: ");
     	center.add(label, vBoxLayoutData);  
     	
     	label = new Label("Sequence identity cutoff:");
     	center.add(label, vBoxLayoutData);  
     	
     	label = new Label("Query coverage cutoff: ");
     	center.add(label, vBoxLayoutData);  
     	
     	label = new Label("Per-member core size xtal-call cutoff: ");
     	center.add(label, vBoxLayoutData); 
     	
     	vBoxLayout = new VBoxLayout();  
     	vBoxLayout.setPadding(new Padding(5));  
     	vBoxLayout.setVBoxLayoutAlign(VBoxLayoutAlign.LEFT);  
     	vBoxLayoutData = new VBoxLayoutData(new Margins(0));
     	vBoxLayoutData.setFlex(1);
     	
     	LayoutContainer right = new LayoutContainer();  
     	right.setStyleAttribute("paddingLeft", "10px"); 
     	right.setHeight(80);
     	right.setLayout(vBoxLayout);  
   
     	label = new Label("Max num sequences used: ");
     	right.add(label, vBoxLayoutData);  
     	
     	label = new Label("Bio-call cutoff: " + resultsData.getBioCutoff());
     	right.add(label, vBoxLayoutData);  
     	
     	HTML html = new HTML();
     	html.setHTML("Xtal-call cutoff: <b>" + resultsData.getXtalCutoff() + "</b>");
     	right.add(html, vBoxLayoutData);  
     	
     	downloadResultsLink = new HTML();
		downloadResultsLink.setHTML("<a href=" + GWT.getModuleBaseURL() + "fileDownload?id=" + resultsData.getJobId() + ">Download results</a>");
		right.add(downloadResultsLink);
     	
     	main.add(left, new ColumnData(.33));  
     	main.add(center, new ColumnData(.33));  
     	main.add(right, new ColumnData(.33));  
   
     	infoPanel.add(main, new FormData("100%"));  
		   
		this.add(infoPanel, new RowData(1, -1, new Margins(0)));
	}
}
