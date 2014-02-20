package ch.systemsx.sybit.crkwebui.client.residues.gui.panels;

import java.util.ArrayList;
import java.util.List;

import ch.systemsx.sybit.crkwebui.client.commons.appdata.ApplicationContext;
import ch.systemsx.sybit.crkwebui.client.commons.gui.cell.TwoDecimalDoubleCell;
import ch.systemsx.sybit.crkwebui.client.commons.util.EscapedStringGenerator;
import ch.systemsx.sybit.crkwebui.client.residues.gui.grid.utils.ResiduePagingMemoryProxy;
import ch.systemsx.sybit.crkwebui.shared.model.Residue;

import com.google.gwt.core.shared.GWT;
import com.google.gwt.editor.client.Editor.Path;
import com.google.gwt.event.logical.shared.ResizeEvent;
import com.google.gwt.event.logical.shared.ResizeHandler;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.sencha.gxt.core.client.GXT;
import com.sencha.gxt.core.client.ValueProvider;
import com.sencha.gxt.data.shared.ListStore;
import com.sencha.gxt.data.shared.ModelKeyProvider;
import com.sencha.gxt.data.shared.PropertyAccess;
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

import eppic.model.ResidueDB;

/**
 * Panel used to display residues data for one structure.
 * @author srebniak_a
 *
 */
public class ResiduesGridPanel extends VerticalLayoutContainer
{
    /**
     * Interface used to project Residue class to Grid
     * @author biyani_n
     *
     */
	public interface ResidueProperties extends PropertyAccess<Residue> {
		
		  @Path("residueNumber")
		  ModelKeyProvider<Residue> key();
		 
		  ValueProvider<Residue, Integer> residueNumber();	  
		  ValueProvider<Residue, String> pdbResidueNumber();
		  ValueProvider<Residue, String> residueType();
		  ValueProvider<Residue, Double> asa();
		  ValueProvider<Residue, Double> bsa();
		  ValueProvider<Residue, Double> bsaPercentage();
		  ValueProvider<Residue, Integer> region();
		  ValueProvider<Residue, Double> entropyScore();
	}
	
	private static final ResidueProperties props = GWT.create(ResidueProperties.class);
    
    private static final int ROW_HEIGHT = 22;
    private static final int PAGING_TOOL_BAR_HEIGHT = 27;
	
    private List<ColumnConfig<Residue, ?>> residuesConfigs;
    private ListStore<Residue> residuesStore;
    private ColumnModel<Residue> residuesColumnModel;
    private Grid<Residue> residuesGrid;

    private ResiduePagingMemoryProxy proxy;
    private PagingLoader<PagingLoadConfig, PagingLoadResult<Residue>> loader;
    private PagingToolBar pagingToolbar;
    private boolean useBufferedView = false;

    private List<Residue> data;

    private int nrOfRows = 20;

    public ResiduesGridPanel()
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

    	data = new ArrayList<Residue>();
    	proxy = new ResiduePagingMemoryProxy(data);
    	
    	residuesStore = new ListStore<Residue>(props.key());
    	    	
    	loader = new PagingLoader<PagingLoadConfig, PagingLoadResult<Residue>>(proxy);
        loader.setRemoteSort(true);
        loader.addLoadHandler(new LoadResultListStoreBinding<PagingLoadConfig, Residue, PagingLoadResult<Residue>>(residuesStore));
    	//loader.setSortField("residueNumber");

    	residuesColumnModel = new ColumnModel<Residue>(residuesConfigs);

    	residuesGrid = createResiduesGrid();
    	this.add(residuesGrid, new VerticalLayoutData(1, 1));

    	if(!useBufferedView)
    	{
    		pagingToolbar = new PagingToolBar(nrOfRows);
    		pagingToolbar.getElement().getStyle().setProperty("borderBottom", "none");
    		pagingToolbar.bind(loader);
    		this.add(pagingToolbar, new VerticalLayoutData(1, PAGING_TOOL_BAR_HEIGHT));
    	}

    }

    /**
     * Fills in with the general grid properties
     */
    private void fillColumnProperties(ColumnConfig<Residue,?> column, String type){
    	column.setColumnHeaderClassName("eppic-default-font");
		column.setWidth(Integer.parseInt(ApplicationContext.getSettings().getGridProperties().get("residues_"+type+"_width")));
		column.setHeader(EscapedStringGenerator.generateSafeHtml(
				ApplicationContext.getSettings().getGridProperties().get("residues_"+type+"_header")));
		
    }
    
    /**
     * Creates columns configurations for residues grid.
     * @return list of columns configurations for residues grid
     */
    private List<ColumnConfig<Residue,?>> createColumnConfig()
    {
    	List<ColumnConfig<Residue,?>> configs = new ArrayList<ColumnConfig<Residue, ?>>();
    	
    	ColumnConfig<Residue,Integer> residueNumberCol = 
    			new ColumnConfig<Residue, Integer>(props.residueNumber());
    	fillColumnProperties(residueNumberCol, "residueNumber");
    	residueNumberCol.setAlignment(HasHorizontalAlignment.ALIGN_RIGHT);
    	
    	ColumnConfig<Residue,String> pdbResidueNumberCol = 
    			new ColumnConfig<Residue, String>(props.pdbResidueNumber());
    	fillColumnProperties(pdbResidueNumberCol, "pdbResidueNumber");
    	pdbResidueNumberCol.setAlignment(HasHorizontalAlignment.ALIGN_RIGHT);
    	
    	ColumnConfig<Residue,String> residueTypeCol = 
    			new ColumnConfig<Residue, String>(props.residueType());
        fillColumnProperties(residueTypeCol, "residueType");
    	residueTypeCol.setAlignment(HasHorizontalAlignment.ALIGN_CENTER);
    	
    	ColumnConfig<Residue, Double> asaCol = 
    			new ColumnConfig<Residue, Double>(props.asa());
        fillColumnProperties(asaCol, "asa");
    	asaCol.setCell(new TwoDecimalDoubleCell());
    	asaCol.setAlignment(HasHorizontalAlignment.ALIGN_RIGHT);
    	
    	ColumnConfig<Residue,Double> bsaCol = 
    			new ColumnConfig<Residue, Double>(props.bsa());
        fillColumnProperties(bsaCol, "bsa");
    	bsaCol.setCell(new TwoDecimalDoubleCell());
    	bsaCol.setAlignment(HasHorizontalAlignment.ALIGN_RIGHT);
   
    	ColumnConfig<Residue,Double> bsaPercentageCol = 
    			new ColumnConfig<Residue, Double>(props.bsaPercentage());
    	fillColumnProperties(bsaPercentageCol, "bsaPercentage");
    	bsaPercentageCol.setCell(new TwoDecimalDoubleCell());
    	bsaPercentageCol.setAlignment(HasHorizontalAlignment.ALIGN_RIGHT);
    	
    	ColumnConfig<Residue,Double> entropyScoreCol = 
    			new ColumnConfig<Residue, Double>(props.entropyScore());
    	fillColumnProperties(entropyScoreCol, "entropyScore");
    	entropyScoreCol.setCell(new TwoDecimalDoubleCell());
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
    private Grid<Residue> createResiduesGrid()
    {
    	Grid<Residue> residuesGrid = new Grid<Residue>(residuesStore, residuesColumnModel);
    	
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
    		LiveGridView<Residue> view = new LiveGridView<Residue>();
    		view.setRowHeight(20);
    		residuesGrid.setView(view);
    	}

    	residuesGrid.getView().setViewConfig(new GridViewConfig<Residue>(){
    		@Override
    		public String getRowStyle(Residue model, int rowIndex) {
    			if (model != null)
    			{
    				if (model.getRegion() == ResidueDB.SURFACE)
    				{
    					return "eppic-grid-row-surface";
    				}
    				else if(model.getRegion() == ResidueDB.CORE_EVOLUTIONARY)
    				{
    					return "eppic-grid-row-core-evolutionary";
    				}
    				else if(model.getRegion() == ResidueDB.CORE_GEOMETRY)
    				{
    					return "eppic-grid-row-core-geometry";
    				}
    				else if(model.getRegion() == ResidueDB.RIM_EVOLUTIONARY)
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
					Residue model,
					ValueProvider<? super Residue, ?> valueProvider,
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
    public void fillResiduesGrid(List<Residue> residueValues)
    {
    	residuesStore.clear();

    	data = new ArrayList<Residue>();

    	if (residueValues != null) {
    		data = residueValues;
    	}
    }

    /**
     * Limits amount of visible entries based on type of the entry.
     * @param isShowAll show all or only rim/core entries
     */
    public void applyFilter(boolean isShowAll)
    {
    	List<Residue> dataToSet = new ArrayList<Residue>();
    	for(Residue item : data)
    	{
    		if((isShowAll) ||
    				((item.getRegion() == ResidueDB.CORE_GEOMETRY) ||
    						(item.getRegion() == ResidueDB.CORE_EVOLUTIONARY) ||
    						(item.getRegion() == ResidueDB.RIM_EVOLUTIONARY)))
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
    	//exclude one header row and one paging row and a bit more space to be on safe side
    	nrOfRows = (this.getOffsetHeight()-ROW_HEIGHT-PAGING_TOOL_BAR_HEIGHT-2)/ ROW_HEIGHT;

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
