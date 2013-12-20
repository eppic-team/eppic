package ch.systemsx.sybit.crkwebui.client.residues.gui.panels;

import java.util.ArrayList;
import java.util.List;

import ch.systemsx.sybit.crkwebui.client.commons.appdata.ApplicationContext;
import ch.systemsx.sybit.crkwebui.client.commons.gui.cell.TwoDecimalFloatCell;
import ch.systemsx.sybit.crkwebui.client.commons.util.EscapedStringGenerator;
import ch.systemsx.sybit.crkwebui.client.residues.data.InterfaceResidueItemModel;
import ch.systemsx.sybit.crkwebui.client.residues.data.InterfaceResidueItemModelProperties;
import ch.systemsx.sybit.crkwebui.client.residues.gui.grid.utils.ResiduePagingMemoryProxy;
import ch.systemsx.sybit.crkwebui.shared.model.InterfaceResidueItem;
import ch.systemsx.sybit.crkwebui.shared.model.InterfaceResidueType;

import com.google.gwt.core.shared.GWT;
import com.google.gwt.event.logical.shared.ResizeEvent;
import com.google.gwt.event.logical.shared.ResizeHandler;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.sencha.gxt.core.client.GXT;
import com.sencha.gxt.core.client.ValueProvider;
import com.sencha.gxt.data.shared.ListStore;
import com.sencha.gxt.data.shared.loader.LoadResultListStoreBinding;
import com.sencha.gxt.data.shared.loader.PagingLoadConfig;
import com.sencha.gxt.data.shared.loader.PagingLoadResult;
import com.sencha.gxt.data.shared.loader.PagingLoader;
import com.sencha.gxt.widget.core.client.container.VerticalLayoutContainer;
import com.sencha.gxt.widget.core.client.grid.ColumnConfig;
import com.sencha.gxt.widget.core.client.grid.ColumnModel;
import com.sencha.gxt.widget.core.client.grid.Grid;
import com.sencha.gxt.widget.core.client.grid.GridViewConfig;
import com.sencha.gxt.widget.core.client.grid.LiveGridView;
import com.sencha.gxt.widget.core.client.toolbar.PagingToolBar;

/**
 * Panel used to display residues data for one structure.
 * @author srebniak_a
 *
 */
public class ResiduesPanel extends VerticalLayoutContainer
{
    private static final InterfaceResidueItemModelProperties props = GWT.create(InterfaceResidueItemModelProperties.class);
	
    private List<ColumnConfig<InterfaceResidueItemModel, ?>> residuesConfigs;
    private ListStore<InterfaceResidueItemModel> residuesStore;
    private ColumnModel<InterfaceResidueItemModel> residuesColumnModel;
    private Grid<InterfaceResidueItemModel> residuesGrid;

    private ResiduePagingMemoryProxy proxy;
    private PagingLoader<PagingLoadConfig, PagingLoadResult<InterfaceResidueItemModel>> loader;
    private PagingToolBar pagingToolbar;
    private boolean useBufferedView = false;

    private List<InterfaceResidueItemModel> data;

    private int nrOfRows = 20;

    public ResiduesPanel()
    {
    	if(GXT.isIE8())
    	{
    		useBufferedView = true;
    	}

    	this.setBorders(false);
    	
    	this.addResizeHandler(new ResizeHandler() {
			
			@Override
			public void onResize(ResizeEvent event) {
				resizeGrid();
				
			}
		});

    	residuesConfigs = createColumnConfig();

    	data = new ArrayList<InterfaceResidueItemModel>();
    	proxy = new ResiduePagingMemoryProxy(data);
    	
    	//proxy.setComparator(new InterfaceResidueItemComparator());
    	
    	residuesStore = new ListStore<InterfaceResidueItemModel>(props.key());
    	//residuesStore.setStoreSorter(new ResiduesPanelSorter());
    	    	
    	loader = new PagingLoader<PagingLoadConfig, PagingLoadResult<InterfaceResidueItemModel>>(proxy);
        loader.setRemoteSort(true);
        loader.addLoadHandler(new LoadResultListStoreBinding<PagingLoadConfig, InterfaceResidueItemModel, PagingLoadResult<InterfaceResidueItemModel>>(residuesStore));
    	//loader.setSortField("residueNumber");

    	residuesColumnModel = new ColumnModel<InterfaceResidueItemModel>(residuesConfigs);

    	residuesGrid = createResiduesGrid();
    	this.add(residuesGrid, new VerticalLayoutData(1, 1));

    	if(!useBufferedView)
    	{
    		pagingToolbar = new PagingToolBar(nrOfRows);
    		pagingToolbar.getElement().getStyle().setProperty("borderBottom", "none");
    		pagingToolbar.bind(loader);
    		this.add(pagingToolbar, new VerticalLayoutData(1, -1));
    	}

    }

    /**
     * Fills in with the general grid properties
     */
    private void fillColumnProperties(ColumnConfig<InterfaceResidueItemModel,?> column, String type){
    	column.setColumnHeaderClassName("eppic-default-font");
		column.setWidth(Integer.parseInt(ApplicationContext.getSettings().getGridProperties().get("residues_"+type+"_width")));
		column.setHeader(EscapedStringGenerator.generateSafeHtml(
				ApplicationContext.getSettings().getGridProperties().get("residues_"+type+"_header")));
		
    }
    
    /**
     * Creates columns configurations for residues grid.
     * @return list of columns configurations for residues grid
     */
    private List<ColumnConfig<InterfaceResidueItemModel,?>> createColumnConfig()
    {
    	List<ColumnConfig<InterfaceResidueItemModel,?>> configs = new ArrayList<ColumnConfig<InterfaceResidueItemModel, ?>>();
    	
    	ColumnConfig<InterfaceResidueItemModel,Integer> residueNumberCol = 
    			new ColumnConfig<InterfaceResidueItemModel, Integer>(props.residueNumber());
    	fillColumnProperties(residueNumberCol, "residueNumber");
    	residueNumberCol.setAlignment(HasHorizontalAlignment.ALIGN_RIGHT);
    	
    	ColumnConfig<InterfaceResidueItemModel,String> pdbResidueNumberCol = 
    			new ColumnConfig<InterfaceResidueItemModel, String>(props.pdbResidueNumber());
    	fillColumnProperties(pdbResidueNumberCol, "pdbResidueNumber");
    	pdbResidueNumberCol.setAlignment(HasHorizontalAlignment.ALIGN_RIGHT);
    	
    	ColumnConfig<InterfaceResidueItemModel,String> residueTypeCol = 
    			new ColumnConfig<InterfaceResidueItemModel, String>(props.residueType());
        fillColumnProperties(residueTypeCol, "residueType");
    	residueTypeCol.setAlignment(HasHorizontalAlignment.ALIGN_CENTER);
    	
    	ColumnConfig<InterfaceResidueItemModel,Float> asaCol = 
    			new ColumnConfig<InterfaceResidueItemModel, Float>(props.asa());
        fillColumnProperties(asaCol, "asa");
    	asaCol.setCell(new TwoDecimalFloatCell());
    	asaCol.setAlignment(HasHorizontalAlignment.ALIGN_RIGHT);
    	
    	ColumnConfig<InterfaceResidueItemModel,Float> bsaCol = 
    			new ColumnConfig<InterfaceResidueItemModel, Float>(props.bsa());
        fillColumnProperties(bsaCol, "bsa");
    	bsaCol.setCell(new TwoDecimalFloatCell());
    	bsaCol.setAlignment(HasHorizontalAlignment.ALIGN_RIGHT);
   
    	ColumnConfig<InterfaceResidueItemModel,Float> bsaPercentageCol = 
    			new ColumnConfig<InterfaceResidueItemModel, Float>(props.bsaPercentage());
    	fillColumnProperties(bsaPercentageCol, "bsaPercentage");
    	bsaPercentageCol.setCell(new TwoDecimalFloatCell());
    	bsaPercentageCol.setAlignment(HasHorizontalAlignment.ALIGN_RIGHT);
    	
    	ColumnConfig<InterfaceResidueItemModel,Float> entropyScoreCol = 
    			new ColumnConfig<InterfaceResidueItemModel, Float>(props.entropyScore());
    	fillColumnProperties(entropyScoreCol, "entropyScore");
    	entropyScoreCol.setCell(new TwoDecimalFloatCell());
    	entropyScoreCol.setAlignment(HasHorizontalAlignment.ALIGN_RIGHT);
    	
    	configs.add(residueNumberCol);
    	configs.add(pdbResidueNumberCol);
    	configs.add(residueTypeCol);
    	configs.add(asaCol);
    	configs.add(bsaCol);
    	configs.add(bsaPercentageCol);
    	configs.add(entropyScoreCol);
    	
    	return configs;
    }

    /**
     * Generates grid used to store residues items.
     * @return residues grid
     */
    private Grid<InterfaceResidueItemModel> createResiduesGrid()
    {
    	Grid<InterfaceResidueItemModel> residuesGrid = new Grid<InterfaceResidueItemModel>(residuesStore, residuesColumnModel);
    	
    	residuesGrid.setBorders(false);
    	residuesGrid.getView().setStripeRows(false);
    	residuesGrid.getView().setColumnLines(false);
    	residuesGrid.setLoadMask(true);
    	residuesGrid.setLoader(loader);
    	//residuesGrid.disableTextSelection(false);

    	residuesGrid.getView().setForceFit(true);
    	residuesGrid.addStyleName("eppic-default-font");

    	if(useBufferedView)
    	{
    		LiveGridView<InterfaceResidueItemModel> view = new LiveGridView<InterfaceResidueItemModel>();
    		view.setRowHeight(20);
    		residuesGrid.setView(view);
    	}

    	residuesGrid.getView().setViewConfig(new GridViewConfig<InterfaceResidueItemModel>(){
    		@Override
    		public String getRowStyle(InterfaceResidueItemModel model, int rowIndex) {
    			if (model != null)
    			{
    				if (model.getAssignment() == InterfaceResidueType.SURFACE.getAssignment())
    				{
    					return "eppic-grid-row-surface";
    				}
    				else if(model.getAssignment() == InterfaceResidueType.CORE_EVOLUTIONARY.getAssignment())
    				{
    					return "eppic-grid-row-core-evolutionary";
    				}
    				else if(model.getAssignment() == InterfaceResidueType.CORE_GEOMETRY.getAssignment())
    				{
    					return "eppic-grid-row-core-geometry";
    				}
    				else if(model.getAssignment() == InterfaceResidueType.RIM.getAssignment())
    				{
    					return "eppic-grid-row-rim";
    				}
    				else
    				{
    					return "eppic-grid-row-buried";
    				}
    			}

    			return "";
    		}

			@Override
			public String getColStyle(
					InterfaceResidueItemModel model,
					ValueProvider<? super InterfaceResidueItemModel, ?> valueProvider,
					int rowIndex, int colIndex) {
				return "";
			}
    	});

    	return residuesGrid;
    }

    /**
     * Sets content of residues grid.
     * @param residueValues list of items to add to the grid
     */
    public void fillResiduesGrid(List<InterfaceResidueItem> residueValues)
    {
    	residuesStore.clear();

    	data = new ArrayList<InterfaceResidueItemModel>();

    	if (residueValues != null) {
    		for (InterfaceResidueItem residueValue : residueValues) {
    			InterfaceResidueItemModel model = new InterfaceResidueItemModel();
    			model.setEntropyScore(residueValue.getEntropyScore());
    			model.setStructure(residueValue.getStructure());
    			model.setResidueNumber(residueValue.getResidueNumber());
    			model.setPdbResidueNumber(residueValue.getPdbResidueNumber());
    			model.setResidueType(residueValue.getResidueType());
    			model.setAsa(residueValue.getAsa());
    			model.setBsa(residueValue.getBsa());
    			model.setBsaPercentage(residueValue.getBsaPercentage());
    			model.setAssignment(residueValue.getAssignment());
    			data.add(model);
    		}
    	}
    }

    /**
     * Limits amount of visible entries based on type of the entry.
     * @param isShowAll show all or only rim/core entries
     */
    public void applyFilter(boolean isShowAll)
    {
    	List<InterfaceResidueItemModel> dataToSet = new ArrayList<InterfaceResidueItemModel>();
    	for(InterfaceResidueItemModel item : data)
    	{
    		if((isShowAll) ||
    				((item.getAssignment() == InterfaceResidueType.CORE_GEOMETRY.getAssignment()) ||
    						(item.getAssignment() == InterfaceResidueType.CORE_EVOLUTIONARY.getAssignment()) ||
    						(item.getAssignment() == InterfaceResidueType.RIM.getAssignment())))
    		{
    			dataToSet.add(item);
    		}
    	}

    	if(useBufferedView)
    	{
    		residuesStore.clear();
    		residuesStore.addAll(dataToSet);
    		residuesStore.commitChanges();
    	}
    	else
    	{
    		proxy.setList(dataToSet);
    		loader.load(0, nrOfRows);
    	}
    }

    /**
     * Cleans content of residues grid.
     */
    public void cleanResiduesGrid()
    {
	residuesStore.clear();
    }

    /**
     * Adjusts size of the residues grid based on the size of the screen and initial
     * settings for the grid.
     * @param assignedWidth width assigned for the grid
     */
    public void resizeGrid()
    {
    	//A Correction of 22 to 26 was done in GXT 3.0.1
    	nrOfRows = this.getOffsetHeight() / 26;

    	if(!useBufferedView)
    	{
    		pagingToolbar.setPageSize(nrOfRows);
    		loader.load(0, nrOfRows);
    		pagingToolbar.setActivePage(1);
    		if(!pagingToolbar.isEnabled()) pagingToolbar.enable();
    	}

    	residuesGrid.clearSizeCache();
    	residuesGrid.getView().refresh(true);
    }

    public void increaseActivePage()
    {
    	pagingToolbar.setActivePage(pagingToolbar.getActivePage() + 1);
    }

    public void decreaseActivePage()
    {
    	pagingToolbar.setActivePage(pagingToolbar.getActivePage() - 1);
    }
}
