package ch.systemsx.sybit.crkwebui.client.homologs.gui.panels;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import ch.systemsx.sybit.crkwebui.client.commons.appdata.ApplicationContext;
import ch.systemsx.sybit.crkwebui.client.homologs.data.HomologsItemModel;
import ch.systemsx.sybit.crkwebui.client.homologs.data.HomologsItemModelProperties;
import ch.systemsx.sybit.crkwebui.client.homologs.gui.cells.IdentityBarCell;
import ch.systemsx.sybit.crkwebui.client.homologs.gui.cells.UniprotIdCell;
import ch.systemsx.sybit.crkwebui.shared.model.HomologIdentityData;
import ch.systemsx.sybit.crkwebui.shared.model.HomologItem;

import com.google.gwt.core.client.GWT;
import com.sencha.gxt.data.shared.ListStore;
import com.sencha.gxt.widget.core.client.container.VerticalLayoutContainer;
import com.sencha.gxt.widget.core.client.grid.ColumnConfig;
import com.sencha.gxt.widget.core.client.grid.ColumnModel;
import com.sencha.gxt.widget.core.client.grid.Grid;

/**
 * Grid containing information about the homologs
 * @author nikhil
 *
 */
public class HomologsGrid extends VerticalLayoutContainer
{
	private static final HomologsItemModelProperties props = GWT.create(HomologsItemModelProperties.class);
	
	private List<ColumnConfig<HomologsItemModel, ?>> configs;
    private ListStore<HomologsItemModel> store;
    private ColumnModel<HomologsItemModel> columnModel;
    private Grid<HomologsItemModel> grid;
    
    public HomologsGrid(){
    	this.setBorders(false);
    	
    	store = new ListStore<HomologsItemModel>(props.key());
    	configs = createColumnConfig();
    	columnModel = new ColumnModel<HomologsItemModel>(configs);
    	grid = createHomologsGrid();
    	
    	this.add(grid, new VerticalLayoutData(1, 1));
    }

    /**
     * Creates the homologs grid
     * @return
     */
    private Grid<HomologsItemModel> createHomologsGrid() {
    	Grid<HomologsItemModel> grid = new Grid<HomologsItemModel>(store, columnModel);
    	
    	grid.setBorders(false);
    	grid.getView().setStripeRows(false);
    	grid.getView().setColumnLines(false);
    	grid.setLoadMask(true);

    	grid.getView().setForceFit(true);
    	grid.addStyleName("eppic-default-font");
    	
    	return grid;
	}

	/**
     * Creates the column config for the grid
     * @return
     */
	private List<ColumnConfig<HomologsItemModel, ?>> createColumnConfig() {
		List<ColumnConfig<HomologsItemModel, ?>> configs = new ArrayList<ColumnConfig<HomologsItemModel, ?>>();
		
		ColumnConfig<HomologsItemModel, String> uniprotIdCol = 
				new ColumnConfig<HomologsItemModel, String>(props.uniId(), 
						Integer.parseInt(ApplicationContext.getSettings().getGridProperties().get("homologs_uniprot_width")));
		uniprotIdCol.setHeader(ApplicationContext.getSettings().getGridProperties().get("homologs_uniprot_header"));
		uniprotIdCol.setCell(new UniprotIdCell());
		
		ColumnConfig<HomologsItemModel, HomologIdentityData> seqIdCol = 
				new ColumnConfig<HomologsItemModel, HomologIdentityData>(props.idData(),
						Integer.parseInt(ApplicationContext.getSettings().getGridProperties().get("homologs_identity_width")));
		seqIdCol.setHeader(ApplicationContext.getSettings().getGridProperties().get("homologs_identity_header"));
		seqIdCol.setCell(new IdentityBarCell());

		ColumnConfig<HomologsItemModel, String> firstTaxCol = 
				new ColumnConfig<HomologsItemModel, String>(props.firstTaxon(), 
						Integer.parseInt(ApplicationContext.getSettings().getGridProperties().get("homologs_firstTax_width")));
		firstTaxCol.setHeader(ApplicationContext.getSettings().getGridProperties().get("homologs_firstTax_header"));
		
		ColumnConfig<HomologsItemModel, String> lastTaxCol = 
				new ColumnConfig<HomologsItemModel, String>(props.lastTaxon(), 
						Integer.parseInt(ApplicationContext.getSettings().getGridProperties().get("homologs_lastTax_width")));
		lastTaxCol.setHeader(ApplicationContext.getSettings().getGridProperties().get("homologs_lastTax_header"));
		
		configs.add(uniprotIdCol);
		configs.add(seqIdCol);
		configs.add(firstTaxCol);
		configs.add(lastTaxCol);
		
		//add header style
		for(ColumnConfig<HomologsItemModel, ?> col: configs)
			col.setColumnHeaderClassName("eppic-default-font");
		
		return configs;
	}
	
	/**
	 * Fills in the grid
	 * @param homologs
	 */
	public void fillHomologsGrid(List<HomologItem> homologs, int queryLength){
		
		store.clear();
		
		List<HomologsItemModel> data = new ArrayList<HomologsItemModel>();
		
		if(homologs != null){
			for(HomologItem homolog: homologs){
				HomologIdentityData idData = new HomologIdentityData(
						homolog.getSeqIdToQuery(), 
						homolog.getQueryStart(), 
						homolog.getQueryEnd(), 
						queryLength);
				
				HomologsItemModel item = new HomologsItemModel(
						homolog.getUid(), 
						homolog.getUniId(), 
						idData, 
						homolog.getQueryCov(), 
						homolog.getFirstTaxon(), 
						homolog.getLastTaxon());
				data.add(item);
			}
			//Sort the list by identity
			Collections.sort(data, new Comparator<HomologsItemModel>() {
				@Override
				public int compare(HomologsItemModel o1, HomologsItemModel o2) {
					return -1*(o1.getIdData().compareTo(o2.getIdData()));
				}

			});
			store.addAll(data);
		}
		
	}
	
	/**
	 * Resizes the grid
	 */
	public void resizePanel(){
		grid.clearSizeCache();
		grid.getView().refresh(true);
	}
}
