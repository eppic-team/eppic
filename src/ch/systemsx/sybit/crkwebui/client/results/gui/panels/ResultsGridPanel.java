package ch.systemsx.sybit.crkwebui.client.results.gui.panels;

import java.util.ArrayList;
import java.util.List;

import ch.systemsx.sybit.crkwebui.client.commons.appdata.AppPropertiesManager;
import ch.systemsx.sybit.crkwebui.client.commons.appdata.ApplicationContext;
import ch.systemsx.sybit.crkwebui.client.commons.events.SelectResultsRowEvent;
import ch.systemsx.sybit.crkwebui.client.commons.events.ShowDetailsEvent;
import ch.systemsx.sybit.crkwebui.client.commons.events.ShowInterfaceResiduesEvent;
import ch.systemsx.sybit.crkwebui.client.commons.events.ShowThumbnailEvent;
import ch.systemsx.sybit.crkwebui.client.commons.events.ShowViewerEvent;
import ch.systemsx.sybit.crkwebui.client.commons.events.WindowHideEvent;
import ch.systemsx.sybit.crkwebui.client.commons.gui.cell.TwoDecimalDoubleCell;
import ch.systemsx.sybit.crkwebui.client.commons.handlers.SelectResultsRowHandler;
import ch.systemsx.sybit.crkwebui.client.commons.handlers.ShowDetailsHandler;
import ch.systemsx.sybit.crkwebui.client.commons.handlers.ShowThumbnailHandler;
import ch.systemsx.sybit.crkwebui.client.commons.handlers.ShowViewerHandler;
import ch.systemsx.sybit.crkwebui.client.commons.handlers.WindowHideHandler;
import ch.systemsx.sybit.crkwebui.client.commons.managers.EventBusManager;
import ch.systemsx.sybit.crkwebui.client.commons.managers.ViewerRunner;
import ch.systemsx.sybit.crkwebui.client.commons.util.EscapedStringGenerator;
import ch.systemsx.sybit.crkwebui.client.results.data.InterfaceItemModel;
import ch.systemsx.sybit.crkwebui.client.results.data.InterfaceItemModelProperties;
import ch.systemsx.sybit.crkwebui.client.results.gui.cells.DetailsButtonCell;
import ch.systemsx.sybit.crkwebui.client.results.gui.cells.MethodCallCell;
import ch.systemsx.sybit.crkwebui.client.results.gui.cells.OperatorTypeCell;
import ch.systemsx.sybit.crkwebui.client.results.gui.cells.ThumbnailCell;
import ch.systemsx.sybit.crkwebui.client.results.gui.cells.WarningsCell;
import ch.systemsx.sybit.crkwebui.client.results.gui.grids.contextmenus.ResultsPanelContextMenu;
import ch.systemsx.sybit.crkwebui.shared.model.InterfaceItem;
import ch.systemsx.sybit.crkwebui.shared.model.InterfaceScoreItem;
import ch.systemsx.sybit.crkwebui.shared.model.PDBScoreItem;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.NativeEvent;
import com.google.gwt.i18n.client.NumberFormat;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.sencha.gxt.core.client.ValueProvider;
import com.sencha.gxt.core.client.dom.ScrollSupport.ScrollMode;
import com.sencha.gxt.core.client.resources.CommonStyles;
import com.sencha.gxt.core.client.util.KeyNav;
import com.sencha.gxt.widget.core.client.FramedPanel;
import com.sencha.gxt.widget.core.client.container.VerticalLayoutContainer;
import com.sencha.gxt.widget.core.client.grid.Grid;
import com.sencha.gxt.widget.core.client.grid.ColumnConfig;
import com.sencha.gxt.widget.core.client.grid.ColumnModel;
import com.sencha.gxt.widget.core.client.tips.QuickTip;
import com.sencha.gxt.data.shared.ListStore;

public class ResultsGridPanel extends VerticalLayoutContainer
{
	private ListStore<InterfaceItemModel> resultsStore;
	private ColumnModel<InterfaceItemModel> resultsColumnModel;
	private Grid<InterfaceItemModel> resultsGrid;
	
	private int panelWidth;
	
	private static final InterfaceItemModelProperties props = GWT.create(InterfaceItemModelProperties.class);
	
	final NumberFormat number = NumberFormat.getFormat("0.00");
	
	//Columns to be used later
	ColumnConfig<InterfaceItemModel, String> thumbnailColumn;
	ColumnConfig<InterfaceItemModel, String> warningsColumn;
	
	public ResultsGridPanel(int width)
	{
		
		this.panelWidth = width;
		this.setWidth(panelWidth);
		
		FramedPanel gridPanel = new FramedPanel();
		
		gridPanel.getHeader().setVisible(false);
		gridPanel.setBorders(true);
		gridPanel.setBodyBorder(false);
		
		VerticalLayoutContainer panelContainer = new VerticalLayoutContainer();
		
		panelContainer.setScrollMode(ScrollMode.AUTO);
		
		gridPanel.setWidget(panelContainer);
		
		resultsStore = new ListStore<InterfaceItemModel>(props.key());
		List<ColumnConfig<InterfaceItemModel, ?>> resultsConfigs = createColumnConfig();
		
		resultsColumnModel = new ColumnModel<InterfaceItemModel>(resultsConfigs);
		
		resultsGrid = createResultsGrid();		
		panelContainer.add(resultsGrid);
		
		this.add(panelContainer, new VerticalLayoutData(-1,-1));
		
		initializeEventsListeners();
	}
	
	/**
	 * Creates configurations of the columns for results grid.
	 * @return list of columns configurations for results grid
	 */
	private List<ColumnConfig<InterfaceItemModel, ?>> createColumnConfig() 
	{
		List<ColumnConfig<InterfaceItemModel, ?>> configs = new ArrayList<ColumnConfig<InterfaceItemModel, ?>>();
		
		thumbnailColumn = getThumbnailColumn();
		configs.add(thumbnailColumn);
		configs.add(getIdColumn());
		configs.add(getChainsColumn());
		configs.add(getAreaColumn());
		configs.add(getOperatorColumn());
		configs.add(getSizesColumn());
		configs.add(getMethodsColumn(props.geometryCall(),"Geometry"));
		configs.add(getMethodsColumn(props.coreRimCall(),"Entropy"));
		configs.add(getMethodsColumn(props.coreSurfaceCall(),"Z-scores"));
		configs.add(getFinalCallColumn());
		configs.add(getDetailsColumn());
		warningsColumn = getWarningsColumn();
		configs.add(warningsColumn);

		return configs;
	}
	
	private ColumnConfig<InterfaceItemModel, String> getWarningsColumn() {
		ColumnConfig<InterfaceItemModel, String> column = new ColumnConfig<InterfaceItemModel, String>(props.warningsImagePath(),
				Integer.parseInt(ApplicationContext.getSettings().getGridProperties().get("results_warnings_width")));
		
		column.setHeader(EscapedStringGenerator.generateEscapedString(
				ApplicationContext.getSettings().getGridProperties().get("results_warnings_header")));
		
		column.setCell(new WarningsCell(resultsStore));

		column.setColumnTextClassName("eppic-results-grid-common-cells");
		column.setAlignment(HasHorizontalAlignment.ALIGN_CENTER);
		return column;
	}

	private ColumnConfig<InterfaceItemModel, String> getDetailsColumn() {
		ColumnConfig<InterfaceItemModel, String> column = new ColumnConfig<InterfaceItemModel, String>(props.detailsButtonText(),
				Integer.parseInt(ApplicationContext.getSettings().getGridProperties().get("results_details_width")));
		
		column.setHeader(EscapedStringGenerator.generateEscapedString(
				ApplicationContext.getSettings().getGridProperties().get("results_details_header")));
		column.setCell(new DetailsButtonCell());

		column.setColumnTextClassName(CommonStyles.get().inlineBlock());
		column.setAlignment(HasHorizontalAlignment.ALIGN_CENTER);
		column.setResizable(false);
		return column;
	}

	private ColumnConfig<InterfaceItemModel, String> getFinalCallColumn() {
		ColumnConfig<InterfaceItemModel, String> column = new ColumnConfig<InterfaceItemModel, String>(props.finalCallName(),
				Integer.parseInt(ApplicationContext.getSettings().getGridProperties().get("results_METHODS_width")));
		
		column.setHeader(EscapedStringGenerator.generateEscapedString(
				ApplicationContext.getSettings().getGridProperties().get("results_finalCallName_header")));
		
		column.setCell(new MethodCallCell(resultsStore, "finalCallName"));

		column.setColumnTextClassName("eppic-results-final-call");
		column.setAlignment(HasHorizontalAlignment.ALIGN_CENTER);
		return column;
	}

	private ColumnConfig<InterfaceItemModel, ?> getMethodsColumn(
			ValueProvider<InterfaceItemModel, String> vp,
			String type) {
		ColumnConfig<InterfaceItemModel, String> column = new ColumnConfig<InterfaceItemModel, String>(vp,
				Integer.parseInt(ApplicationContext.getSettings().getGridProperties().get("results_METHODS_width")));
		
		column.setHeader(EscapedStringGenerator.generateEscapedString(
				ApplicationContext.getSettings().getGridProperties().get("results_"+type+"_header")));
		
		MethodCallCell callCell = new MethodCallCell(resultsStore, type);
		column.setCell(callCell);
		
		column.setColumnTextClassName("eppic-results-grid-common-cells");
		column.setAlignment(HasHorizontalAlignment.ALIGN_CENTER);
		return column;
	}

	private ColumnConfig<InterfaceItemModel, String> getSizesColumn() {
		ColumnConfig<InterfaceItemModel, String> sizesColumn = new ColumnConfig<InterfaceItemModel, String>(props.sizes(),
				Integer.parseInt(ApplicationContext.getSettings().getGridProperties().get("results_sizes_width")));
		
		sizesColumn.setHeader(EscapedStringGenerator.generateEscapedString(
				ApplicationContext.getSettings().getGridProperties().get("results_sizes_header")));
		
		sizesColumn.setColumnTextClassName("eppic-results-grid-common-cells");
		sizesColumn.setAlignment(HasHorizontalAlignment.ALIGN_CENTER);
		return sizesColumn;
	}

	private ColumnConfig<InterfaceItemModel, String> getOperatorColumn() {
		ColumnConfig<InterfaceItemModel, String> operatorColumn = new ColumnConfig<InterfaceItemModel, String>(props.operatorType(),
				Integer.parseInt(ApplicationContext.getSettings().getGridProperties().get("results_operatorType_width")));
		
		operatorColumn.setHeader(EscapedStringGenerator.generateEscapedString(
				ApplicationContext.getSettings().getGridProperties().get("results_operatorType_header")));
		
		operatorColumn.setCell(new OperatorTypeCell(resultsStore));
		
		operatorColumn.setColumnTextClassName("eppic-results-grid-common-cells");
		operatorColumn.setAlignment(HasHorizontalAlignment.ALIGN_CENTER);
		return operatorColumn;
	}

	private ColumnConfig<InterfaceItemModel, Double> getAreaColumn() {
		ColumnConfig<InterfaceItemModel, Double> areaColumn = new ColumnConfig<InterfaceItemModel, Double>(props.area(),
				Integer.parseInt(ApplicationContext.getSettings().getGridProperties().get("results_area_width")));
		
		SafeHtmlBuilder sb = new SafeHtmlBuilder();
		
		areaColumn.setHeader(sb.appendHtmlConstant(ApplicationContext.getSettings().getGridProperties().get("results_area_header")).toSafeHtml());
		
		areaColumn.setCell(new TwoDecimalDoubleCell());
		
		areaColumn.setColumnTextClassName("eppic-results-grid-common-cells");
		areaColumn.setAlignment(HasHorizontalAlignment.ALIGN_CENTER);
		return areaColumn;
	}

	/**
	 * Creates the chains Column's config
	 * @return column with it's columnconfig
	 */
	private ColumnConfig<InterfaceItemModel, String> getChainsColumn() {
		ColumnConfig<InterfaceItemModel, String> chainColumn = new ColumnConfig<InterfaceItemModel, String>(props.name(),
				Integer.parseInt(ApplicationContext.getSettings().getGridProperties().get("results_name_width")));
		
		chainColumn.setHeader(EscapedStringGenerator.generateEscapedString(
				ApplicationContext.getSettings().getGridProperties().get("results_name_header")));
		
		chainColumn.setColumnTextClassName("eppic-results-grid-common-cells");
		chainColumn.setAlignment(HasHorizontalAlignment.ALIGN_CENTER);
		return chainColumn;
	}

	/**
	 * Creates the Id Column's config
	 * @return column with it's columnconfig
	 */
	private ColumnConfig<InterfaceItemModel, Integer> getIdColumn() {
		ColumnConfig<InterfaceItemModel, Integer> idColumn = new ColumnConfig<InterfaceItemModel, Integer>(props.id(),
				Integer.parseInt(ApplicationContext.getSettings().getGridProperties().get("results_id_width")));
		
		idColumn.setHeader(EscapedStringGenerator.generateEscapedString(
				ApplicationContext.getSettings().getGridProperties().get("results_id_header")));
		
		idColumn.setColumnTextClassName("eppic-results-grid-common-cells");
		idColumn.setAlignment(HasHorizontalAlignment.ALIGN_CENTER);
		return idColumn;
	}
	
	/**
	 * Creates the thumbnail Column's config
	 * @return
	 */
	private ColumnConfig<InterfaceItemModel, String> getThumbnailColumn(){
		ColumnConfig<InterfaceItemModel, String> thumbnailColumn = new ColumnConfig<InterfaceItemModel, String>(props.thumbnailUrl(),
				Integer.parseInt(ApplicationContext.getSettings().getGridProperties().get("results_thumbnail_width")));
		
		thumbnailColumn.setHeader(EscapedStringGenerator.generateEscapedString(
				ApplicationContext.getSettings().getGridProperties().get("results_thumbnail_header")));
		
		thumbnailColumn.setCell(new ThumbnailCell(resultsStore));
		
		thumbnailColumn.setColumnTextClassName("eppic-results-grid-common-cells");
		thumbnailColumn.setAlignment(HasHorizontalAlignment.ALIGN_CENTER);
		thumbnailColumn.setResizable(false);
		
		return thumbnailColumn;
	}
	
	/**
	 * Creates grid storing results of calculations for each of the interfaces.
	 * @return interfaces grid
	 */
	private Grid<InterfaceItemModel> createResultsGrid()
	{
		final Grid<InterfaceItemModel> resultsGrid = new Grid<InterfaceItemModel>(resultsStore, resultsColumnModel);
		resultsGrid.setBorders(false);
		resultsGrid.getView().setStripeRows(true);
		resultsGrid.getView().setColumnLines(false);
		resultsGrid.getView().setForceFit(true);
		resultsGrid.setContextMenu(new ResultsPanelContextMenu());
		
		resultsGrid.getView().setEmptyText(AppPropertiesManager.CONSTANTS.results_grid_empty_text());
		
		resultsGrid.addStyleName("eppic-results-grid");
		resultsGrid.addStyleName("eppic-default-font");
		
		new KeyNav(resultsGrid)
		{
			@Override
            public void onEnter(NativeEvent event) 
			{
				InterfaceItemModel interfaceItemModel = resultsGrid.getSelectionModel().getSelectedItem();
				if(interfaceItemModel != null)
				{
					EventBusManager.EVENT_BUS.fireEvent(new ShowDetailsEvent());
				}
			}
		};
		
		new QuickTip(resultsGrid);
		
		return resultsGrid;
	}
	
	/**
	 * Sets content of results grid.
	 * @param resultsData results data of selected job
	 */
	public void fillResultsGrid(PDBScoreItem resultsData)
	{
		boolean hideWarnings = true;
		
		resultsStore.clear();

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

				for(InterfaceScoreItem interfaceScoreItem : interfaceItem.getInterfaceScores())
				{
					if(interfaceScoreItem.getMethod().equals("Geometry"))
					{
						model.setGeometryCall(interfaceScoreItem.getCallName());
					}

					if(interfaceScoreItem.getMethod().equals("Entropy"))
					{
						model.setCoreRimCall(interfaceScoreItem.getCallName());
					}

					if(interfaceScoreItem.getMethod().equals("Z-scores"))
					{
						model.setCoreSurfaceCall(interfaceScoreItem.getCallName());
					}
				}
				
				model.setId(interfaceItem.getId());
				model.setName(interfaceItem.getChain1() + "+" + interfaceItem.getChain2());
				model.setArea(interfaceItem.getArea());
				model.setSizes(String.valueOf(interfaceItem.getSize1()) + " + " + String.valueOf(interfaceItem.getSize2()));
				model.setFinalCallName(interfaceItem.getFinalCallName());
				model.setOperator(interfaceItem.getOperator());
				model.setOperatorType(interfaceItem.getOperatorType());
				model.setIsInfinite(interfaceItem.getIsInfinite());
				model.setWarnings(interfaceItem.getWarnings());

				data.add(model);
			}
		}
		
		resultsStore.addAll(data);
		
		boolean resizeGrid = false;
		int warningsColIndex = 0;
		for(ColumnConfig<InterfaceItemModel, ?> col: resultsColumnModel.getColumns()){
			if(col.equals(warningsColumn))
			{
				warningsColIndex = resultsColumnModel.indexOf(col);
			}
		}

		if(resultsColumnModel.getColumn(warningsColIndex).isHidden() != hideWarnings)
		{
			resizeGrid = true;
		}

		resultsColumnModel.getColumn(warningsColIndex).setHidden(hideWarnings);

		resultsGrid.reconfigure(resultsStore, resultsColumnModel);
		
		if(resizeGrid)
		{
			resizeContent(panelWidth);
		}
	}
	
	/**
	 * Adjusts size of the results grid based on the current screen size and
	 * initial settings for the columns.
	 */
	public void resizeContent(int width) 
	{
		this.panelWidth = width;
		this.setWidth(panelWidth);
		resultsGrid.clearSizeCache();
		resultsGrid.getView().refresh(true);
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
				ViewerRunner.runViewer(String.valueOf(resultsGrid.getSelectionModel().getSelectedItem().getId()));
			}
		}); 
		
		EventBusManager.EVENT_BUS.addHandler(ShowThumbnailEvent.TYPE, new ShowThumbnailHandler() {
			
			@Override
			public void onShowThumbnail(ShowThumbnailEvent event)
			{
				for(ColumnConfig<InterfaceItemModel, ?> column : resultsGrid.getColumnModel().getColumns())
				{
					if(column.equals(thumbnailColumn))
					{
						column.setHidden(event.isHideThumbnail());
						resizeContent(panelWidth);
					}
				}
			}
		}); 
		
		EventBusManager.EVENT_BUS.addHandler(ShowDetailsEvent.TYPE, new ShowDetailsHandler() 
		{
			@Override
			public void onShowDetails(ShowDetailsEvent event) 
			{
				EventBusManager.EVENT_BUS.fireEvent(new ShowInterfaceResiduesEvent((Integer)resultsGrid.getSelectionModel().getSelectedItem().getId()));
			}
		}); 
		
		EventBusManager.EVENT_BUS.addHandler(SelectResultsRowEvent.TYPE, new SelectResultsRowHandler() {
			
			@Override
			public void onSelectResultsRow(SelectResultsRowEvent event) {
				resultsGrid.getSelectionModel().select(event.getRowIndex(), false);
			}
		}); 
	}
	
}
