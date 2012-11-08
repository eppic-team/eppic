package ch.systemsx.sybit.crkwebui.client.gui;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import ch.systemsx.sybit.crkwebui.client.controllers.AppPropertiesManager;
import ch.systemsx.sybit.crkwebui.client.controllers.ApplicationContext;
import ch.systemsx.sybit.crkwebui.client.controllers.EventBusManager;
import ch.systemsx.sybit.crkwebui.client.controllers.ViewerRunner;
import ch.systemsx.sybit.crkwebui.client.events.ApplicationWindowResizeEvent;
import ch.systemsx.sybit.crkwebui.client.events.SelectResultsRowEvent;
import ch.systemsx.sybit.crkwebui.client.events.ShowDetailsEvent;
import ch.systemsx.sybit.crkwebui.client.events.ShowInterfaceResiduesEvent;
import ch.systemsx.sybit.crkwebui.client.events.ShowViewerEvent;
import ch.systemsx.sybit.crkwebui.client.events.WindowHideEvent;
import ch.systemsx.sybit.crkwebui.client.gui.util.EscapedStringGenerator;
import ch.systemsx.sybit.crkwebui.client.gui.util.GridColumnConfigGenerator;
import ch.systemsx.sybit.crkwebui.client.handlers.ApplicationWindowResizeHandler;
import ch.systemsx.sybit.crkwebui.client.handlers.SelectResultsRowHandler;
import ch.systemsx.sybit.crkwebui.client.handlers.ShowDetailsHandler;
import ch.systemsx.sybit.crkwebui.client.handlers.ShowViewerHandler;
import ch.systemsx.sybit.crkwebui.client.handlers.WindowHideHandler;
import ch.systemsx.sybit.crkwebui.client.model.InterfaceItemModel;
import ch.systemsx.sybit.crkwebui.shared.model.InterfaceItem;
import ch.systemsx.sybit.crkwebui.shared.model.InterfaceScoreItem;
import ch.systemsx.sybit.crkwebui.shared.model.PDBScoreItem;
import ch.systemsx.sybit.crkwebui.shared.model.SupportedMethod;

import com.extjs.gxt.ui.client.Style.Orientation;
import com.extjs.gxt.ui.client.core.Template;
import com.extjs.gxt.ui.client.event.BaseEvent;
import com.extjs.gxt.ui.client.event.ComponentEvent;
import com.extjs.gxt.ui.client.event.Events;
import com.extjs.gxt.ui.client.event.FieldEvent;
import com.extjs.gxt.ui.client.event.GridEvent;
import com.extjs.gxt.ui.client.event.Listener;
import com.extjs.gxt.ui.client.store.ListStore;
import com.extjs.gxt.ui.client.util.KeyNav;
import com.extjs.gxt.ui.client.util.Margins;
import com.extjs.gxt.ui.client.widget.ContentPanel;
import com.extjs.gxt.ui.client.widget.Label;
import com.extjs.gxt.ui.client.widget.LayoutContainer;
import com.extjs.gxt.ui.client.widget.form.CheckBox;
import com.extjs.gxt.ui.client.widget.form.ComboBox.TriggerAction;
import com.extjs.gxt.ui.client.widget.form.FormPanel;
import com.extjs.gxt.ui.client.widget.form.SimpleComboBox;
import com.extjs.gxt.ui.client.widget.grid.ColumnConfig;
import com.extjs.gxt.ui.client.widget.grid.ColumnModel;
import com.extjs.gxt.ui.client.widget.grid.Grid;
import com.extjs.gxt.ui.client.widget.layout.FitLayout;
import com.extjs.gxt.ui.client.widget.layout.RowData;
import com.extjs.gxt.ui.client.widget.layout.RowLayout;
import com.extjs.gxt.ui.client.widget.layout.VBoxLayout;
import com.extjs.gxt.ui.client.widget.layout.VBoxLayout.VBoxLayoutAlign;
import com.extjs.gxt.ui.client.widget.tips.ToolTipConfig;
import com.google.gwt.user.client.Cookies;

/**
 * Panel used to display the results of the calculations.
 * @author srebniak_a
 *
 */
public class ResultsPanel extends DisplayPanel
{
	private PDBIdentifierPanel pdbIdentifierPanel;
	private Label pdbTitle;
	
	private InfoPanel infoPanel;
	
	private CheckBox showThumbnailCheckBox;
	private SimpleComboBox<String> viewerTypeComboBox;
	
	// ***************************************
	// * Results grid
	// ***************************************
	private ContentPanel resultsGridContainer;
	private Grid<InterfaceItemModel> resultsGrid;
	private List<ColumnConfig> resultsConfigs;
	private ListStore<InterfaceItemModel> resultsStore;
	private ColumnModel resultsColumnModel;
	private Map<String, Integer> initialColumnWidth;
	
	private float gridWidthMultiplier;
	// ***************************************

	public ResultsPanel(PDBScoreItem pdbScoreItem)
	{
		this.setBorders(true);
		this.setLayout(new RowLayout(Orientation.VERTICAL));
		this.addStyleName("eppic-default-padding");

		pdbIdentifierPanel = new PDBIdentifierPanel();
		this.add(pdbIdentifierPanel);
		
		FormPanel breakPanel = new FormPanel();
		breakPanel.setBorders(false);
		breakPanel.setBodyBorder(false);
		breakPanel.setPadding(0);
		breakPanel.getHeader().setVisible(false);
		this.add(breakPanel, new RowData(1, 1.1, new Margins(0)));
		
		pdbTitle = new Label();
		pdbTitle.addStyleName("eppic-pdb-title-label");
		this.add(pdbTitle);
		
		breakPanel = new FormPanel();
		breakPanel.setBorders(false);
		breakPanel.setBodyBorder(false);
		breakPanel.setPadding(0);
		breakPanel.getHeader().setVisible(false);
		this.add(breakPanel, new RowData(1, 10, new Margins(0)));
		
		createInfoPanel(pdbScoreItem);

		createViewerTypePanel();

		breakPanel = new FormPanel();
		breakPanel.setBorders(false);
		breakPanel.setBodyBorder(false);
		breakPanel.setPadding(0);
		breakPanel.getHeader().setVisible(false);
		this.add(breakPanel, new RowData(1, 5, new Margins(0)));
		
		resultsConfigs = createColumnConfig();
		
		if(!showThumbnailCheckBox.getValue())
		{
			for(ColumnConfig column : resultsConfigs)
			{
				if(column.getId().equals("thumbnail"))
				{
					column.setHidden(true);
				}
			}
		}

		resultsStore = new ListStore<InterfaceItemModel>();
		resultsColumnModel = new ColumnModel(resultsConfigs);

		resultsGrid = new Grid<InterfaceItemModel>(resultsStore, resultsColumnModel);
		resultsGrid.getView().setForceFit(false);
		resultsGrid.getView().setEmptyText(AppPropertiesManager.CONSTANTS.results_grid_empty_text());

		resultsGrid.setBorders(false);
		resultsGrid.setStripeRows(true);
		resultsGrid.setColumnLines(false);
		resultsGrid.setColumnReordering(true);

//		resultsGrid.addListener(Events.CellClick, resultsGridListener);
		resultsGrid.setContextMenu(new ResultsPanelContextMenu());
		resultsGrid.disableTextSelection(false);
//		resultsGrid.setAutoHeight(true);
//		fillResultsGrid(mainController.getPdbScoreItem());
		
		resultsGrid.addListener(Events.ContextMenu, new Listener<BaseEvent>(){
			@Override
			public void handleEvent(BaseEvent be) {
				resizeGrid();
			}
		});
		
		resultsGrid.addListener(Events.ColumnResize, new Listener<GridEvent>() {
			@Override
			public void handleEvent(GridEvent ge) 
			{
				int widthToSet = (int) (ge.getWidth() / gridWidthMultiplier);
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

		resultsGridContainer = new ContentPanel();
		resultsGridContainer.getHeader().setVisible(false);
		resultsGridContainer.setBorders(true);
		resultsGridContainer.setBodyBorder(false);
		resultsGridContainer.setLayout(new FitLayout());
//		resultsGridContainer.setScrollMode(Scroll.AUTO);
		resultsGridContainer.add(resultsGrid);
		
		this.add(resultsGridContainer, new RowData(1, 1, new Margins(0)));
		
		initializeEventsListeners();
	}

	/**
	 * Creates configurations of the columns for results grid.
	 * @return list of columns configurations for results grid
	 */
	private List<ColumnConfig> createColumnConfig() 
	{
		List<ColumnConfig> configs = GridColumnConfigGenerator.createColumnConfigs("results",
																				   new InterfaceItemModel());

		if(configs != null)
		{
			initialColumnWidth = new HashMap<String, Integer>();
			
			for(ColumnConfig columnConfig : configs)
			{
				initialColumnWidth.put(columnConfig.getId(), columnConfig.getWidth());
			}
		}

		return configs;
	}

	/**
	 * Generates info panel containing general information about finished job.
	 */
	private void createInfoPanel(PDBScoreItem pdbScoreItem)
	{
		infoPanel = new InfoPanel(pdbScoreItem);
		this.add(infoPanel, new RowData(1, 80, new Margins(0)));
	}

	/**
	 * Generates panel containing viewer and thumbnail selectors.
	 */
	private void createViewerTypePanel() 
	{
		LayoutContainer optionsLocation = new LayoutContainer();
		optionsLocation.setLayout(new RowLayout(Orientation.HORIZONTAL));
		optionsLocation.addStyleName("eppic-default-top-padding");

		LayoutContainer viewerTypePanelLocation = new LayoutContainer();
		viewerTypePanelLocation.setBorders(false);

		VBoxLayout vBoxLayout = new VBoxLayout();
		vBoxLayout.setVBoxLayoutAlign(VBoxLayoutAlign.LEFT);

		viewerTypePanelLocation.setLayout(vBoxLayout);

		FormPanel viewerTypePanel = new FormPanel();
		viewerTypePanel.getHeader().setVisible(false);
		viewerTypePanel.setBorders(false);
		viewerTypePanel.setBodyBorder(false);
		viewerTypePanel.setFieldWidth(100);
		viewerTypePanel.setPadding(0);

		viewerTypeComboBox = new SimpleComboBox<String>();
		viewerTypeComboBox.setId("viewercombo");
		viewerTypeComboBox.setTriggerAction(TriggerAction.ALL);
		viewerTypeComboBox.setEditable(false);
		viewerTypeComboBox.setFireChangeEventOnSetValue(true);
		viewerTypeComboBox.setWidth(100);
		viewerTypeComboBox.add(AppPropertiesManager.CONSTANTS.viewer_local());
		viewerTypeComboBox.add(AppPropertiesManager.CONSTANTS.viewer_jmol());
		viewerTypeComboBox.add(AppPropertiesManager.CONSTANTS.viewer_pse());
		viewerTypeComboBox.setToolTip(createViewerTypeComboBoxToolTipConfig());
		
		String viewerCookie = Cookies.getCookie("crkviewer");
		if (viewerCookie != null) {
			viewerTypeComboBox.setSimpleValue(viewerCookie);
		} else {
			viewerTypeComboBox.setSimpleValue(AppPropertiesManager.CONSTANTS.viewer_jmol());
		}

		ApplicationContext.setSelectedViewer(viewerTypeComboBox.getValue()
				.getValue());

		viewerTypeComboBox.setFieldLabel(AppPropertiesManager.CONSTANTS.results_grid_viewer_combo_label());
		viewerTypeComboBox.setLabelStyle("eppic-default-label");
		viewerTypeComboBox.addListener(Events.Change,
				new Listener<FieldEvent>() {
					public void handleEvent(FieldEvent be) {
						Cookies.setCookie("crkviewer", viewerTypeComboBox
								.getValue().getValue());
						ApplicationContext.setSelectedViewer(viewerTypeComboBox
								.getValue().getValue());
					}
				});

		viewerTypePanel.add(viewerTypeComboBox);
		viewerTypePanelLocation.add(viewerTypePanel);
		
		LayoutContainer showThumbnailPanelLocation = new LayoutContainer();
		showThumbnailPanelLocation.setBorders(false);
		
		vBoxLayout = new VBoxLayout();
		vBoxLayout.setVBoxLayoutAlign(VBoxLayoutAlign.RIGHT);
		showThumbnailPanelLocation.setLayout(vBoxLayout);
		
		showThumbnailCheckBox = new CheckBox();
		showThumbnailCheckBox.setBoxLabel(AppPropertiesManager.CONSTANTS.results_grid_show_thumbnails());
		displayThumbnails();
		showThumbnailCheckBox.addListener(Events.Change, new Listener<FieldEvent>() {

			@Override
			public void handleEvent(FieldEvent event)
			{
				Cookies.setCookie("crkthumbnail", String.valueOf(showThumbnailCheckBox.getValue()));
				
				for(ColumnConfig column : resultsGrid.getColumnModel().getColumns())
				{
					if(column.getId().equals("thumbnail"))
					{
						if(showThumbnailCheckBox.getValue())
						{
							column.setHidden(false);
						}
						else
						{
							column.setHidden(true);
						}
						
						resizeGrid();
					}
				}
			}
			
		});
		
		showThumbnailPanelLocation.add(showThumbnailCheckBox);

		optionsLocation.add(viewerTypePanelLocation, new RowData(0.5, 1, new Margins(0)));
		optionsLocation.add(showThumbnailPanelLocation, new RowData(0.5, 1, new Margins(0)));
		this.add(optionsLocation, new RowData(1, 35, new Margins(0)));
	}

	/**
	 * Creates configuration of the tooltip displayed over viewer type selector.
	 * @return configuration of tooltip displayed over viewer type selector
	 */
	private ToolTipConfig createViewerTypeComboBoxToolTipConfig()
	{
		ToolTipConfig viewerTypeComboBoxToolTipConfig = new ToolTipConfig();  
		viewerTypeComboBoxToolTipConfig.setTitle("3D viewer selector");
		viewerTypeComboBoxToolTipConfig.setTemplate(new Template(generateViewerTypeComboBoxTooltipTemplate()));  
		viewerTypeComboBoxToolTipConfig.setShowDelay(0);
		viewerTypeComboBoxToolTipConfig.setDismissDelay(0);
		return viewerTypeComboBoxToolTipConfig;
	}
	
	/**
	 * Generates content of viewer type tooltip.
	 * @return content of viewer type tooltip
	 */
	private String generateViewerTypeComboBoxTooltipTemplate()
	{
		String viewerTypeDescription = "To run selected 3D viewer please click one of the thumbnails on the list below. The following options are provided: " +
									   "<div><ul class=\"eppic-tooltip-list\">" +
									   "<li>PDB file downloadable to a local molecular viewer</li>" +
									   "<li>Browser embedded Jmol viewer (no need for local viewer)</li>" +
									   "<li>PyMol session file (.pse) to be opened in local PyMol</li>" +
									   "</ul></div>";
		return viewerTypeDescription;
	}

	/**
	 * Sets content of results panel.
	 * @param resultsData results data of selected job
	 */
	public void fillResultsPanel(PDBScoreItem resultsData) 
	{
		fillResultsGrid(resultsData);
		infoPanel.generateInfoPanel(resultsData);
		
		pdbIdentifierPanel.setPDBText(resultsData.getPdbName(),
							  	 	resultsData.getSpaceGroup(),
							  	 	resultsData.getExpMethod(),
							  	 	resultsData.getResolution(),
							  	 	resultsData.getInputType());
		
		pdbTitle.setText(EscapedStringGenerator.generateEscapedString(resultsData.getTitle()));
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

		resultsStore.add(data);
		
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
		int limit = 50;
		if(ApplicationContext.isMyJobsListVisible())
		{
			limit += ApplicationContext.getMyJobsPanelWidth();
		}
		else
		{
			limit += 25;
		}
		
		int resultsGridWidthOfAllVisibleColumns = 0;
		
		for(int i=0; i<resultsGrid.getColumnModel().getColumnCount(); i++)
		{
			if(!resultsGrid.getColumnModel().getColumn(i).isHidden())
			{
				resultsGridWidthOfAllVisibleColumns += initialColumnWidth.get(resultsGrid.getColumnModel().getColumn(i).getId());
			}
		}
		
		if (resultsGridWidthOfAllVisibleColumns < ApplicationContext.getAdjustedWindowData().getWindowWidth() - limit) 
		{
			int maxWidth = ApplicationContext.getAdjustedWindowData().getWindowWidth() - limit - 20;
			gridWidthMultiplier = (float)maxWidth / resultsGridWidthOfAllVisibleColumns;
			
			int nrOfColumn = resultsGrid.getColumnModel().getColumnCount();
			
			for (int i = 0; i < nrOfColumn; i++) 
			{
				resultsGrid.getColumnModel().setColumnWidth(i, (int)(initialColumnWidth.get(resultsGrid.getColumnModel().getColumn(i).getId()) * gridWidthMultiplier), true);
			}
		} 
		else 
		{
			gridWidthMultiplier = 1;
			
			int nrOfColumn = resultsGrid.getColumnModel().getColumnCount();

			for (int i = 0; i < nrOfColumn; i++) {
				resultsGrid.getColumnModel().getColumn(i)
						.setWidth(initialColumnWidth.get(resultsGrid.getColumnModel().getColumn(i).getId()));
			}
		}
		
		resultsGrid.setWidth(ApplicationContext.getAdjustedWindowData().getWindowWidth() - limit);

//		resultsGrid.reconfigure(resultsStore, resultsColumnModel);
		resultsGrid.getView().refresh(true);
		resultsGrid.getView().layout();
		resultsGrid.repaint();
		
		
		this.layout();
		
		if(resultsGrid.getView().getHeader() != null)
		{
			resultsGrid.getView().getHeader().refresh();
		}
	}

	/**
	 * Sets value of thumbnail checkbox based on saved cookie.
	 */
	private void displayThumbnails()
	{
		String thumbnailCookie = Cookies.getCookie("crkthumbnail");
		if(thumbnailCookie == null)
		{
			thumbnailCookie = "true";
		}
		
		if (thumbnailCookie.equals("true"))
		{
			showThumbnailCheckBox.setValue(true);
		} 
		else
		{
			showThumbnailCheckBox.setValue(false);
		}
	}

	/**
	 * Saves currently customized by the user grid settings to reuse them for
	 * displaying other results.
	 */
	public void saveGridSettings()
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
		
		EventBusManager.EVENT_BUS.addHandler(ApplicationWindowResizeEvent.TYPE, new ApplicationWindowResizeHandler() {
			
			@Override
			public void onResizeApplicationWindow(ApplicationWindowResizeEvent event) {
				resizeGrid();
			}
		});
	}
}
