package ch.systemsx.sybit.crkwebui.client.gui;

import java.util.ArrayList;
import java.util.List;

import ch.systemsx.sybit.crkwebui.client.gui.util.GridColumnConfigGenerator;
import ch.systemsx.sybit.crkwebui.client.gui.util.GridUtil;
import ch.systemsx.sybit.crkwebui.client.model.InterfaceResidueItemModel;
import ch.systemsx.sybit.crkwebui.shared.model.InterfaceResidueItem;
import ch.systemsx.sybit.crkwebui.shared.model.InterfaceResidueType;

import com.extjs.gxt.ui.client.GXT;
import com.extjs.gxt.ui.client.Style.Scroll;
import com.extjs.gxt.ui.client.data.BasePagingLoader;
import com.extjs.gxt.ui.client.data.ModelData;
import com.extjs.gxt.ui.client.data.PagingLoader;
import com.extjs.gxt.ui.client.data.PagingModelMemoryProxy;
import com.extjs.gxt.ui.client.store.ListStore;
import com.extjs.gxt.ui.client.util.Margins;
import com.extjs.gxt.ui.client.widget.ContentPanel;
import com.extjs.gxt.ui.client.widget.grid.BufferView;
import com.extjs.gxt.ui.client.widget.grid.ColumnConfig;
import com.extjs.gxt.ui.client.widget.grid.ColumnModel;
import com.extjs.gxt.ui.client.widget.grid.Grid;
import com.extjs.gxt.ui.client.widget.grid.GridViewConfig;
import com.extjs.gxt.ui.client.widget.layout.FitLayout;
import com.extjs.gxt.ui.client.widget.layout.RowData;
import com.extjs.gxt.ui.client.widget.toolbar.PagingToolBar;

/**
 * Panel used to display residues data for one structure.
 * @author srebniak_a
 *
 */
public class ResiduesPanel extends ContentPanel
{
	private List<ColumnConfig> residuesConfigs;
	private ListStore<InterfaceResidueItemModel> residuesStore;
	private ColumnModel residuesColumnModel;
	private Grid<InterfaceResidueItemModel> residuesGrid;
	private List<Integer> initialColumnWidth;

	private PagingModelMemoryProxy proxy;
	private PagingLoader loader;

	private List<InterfaceResidueItemModel> data;

	private int nrOfRows = 20;
	private PagingToolBar pagingToolbar;

	private boolean useBufferedView = false;

	public ResiduesPanel(int width)
	{
		if(GXT.isIE8)
		{
			useBufferedView = true;
		}

		this.setBodyBorder(false);
		this.setBorders(false);
		this.setLayout(new FitLayout());
		this.getHeader().setVisible(false);
		this.setScrollMode(Scroll.NONE);

		residuesConfigs = createColumnConfig();

		proxy = new PagingModelMemoryProxy(null);
		loader = new BasePagingLoader(proxy);
		loader.setRemoteSort(true);

		residuesStore = new ListStore<InterfaceResidueItemModel>(loader);
		residuesColumnModel = new ColumnModel(residuesConfigs);

		residuesGrid = createResiduesGrid();
		this.add(residuesGrid, new RowData(1, 1, new Margins(0)));

		if(!useBufferedView)
		{
			pagingToolbar = new PagingToolBar(nrOfRows);
			pagingToolbar.bind(loader);

			this.setBottomComponent(pagingToolbar);
		}

	}

	/**
	 * Creates columns configurations for residues grid.
	 * @return list of columns configurations for residues grid
	 */
	private List<ColumnConfig> createColumnConfig()
	{
		List<ColumnConfig> configs = GridColumnConfigGenerator.createColumnConfigs(
				   "residues",
				   new InterfaceResidueItemModel());

		if(configs != null)
		{
			initialColumnWidth = new ArrayList<Integer>();

			for(ColumnConfig columnConfig : configs)
			{
				initialColumnWidth.add(columnConfig.getWidth());
			}
		}

		return configs;
	}

	private Grid<InterfaceResidueItemModel> createResiduesGrid()
	{
		Grid<InterfaceResidueItemModel> residuesGrid = new Grid<InterfaceResidueItemModel>(residuesStore, residuesColumnModel);
		residuesGrid.setBorders(false);
		residuesGrid.setStripeRows(true);
		residuesGrid.setColumnLines(false);
		residuesGrid.setLoadMask(true);

		residuesGrid.disableTextSelection(false);

		residuesGrid.getView().setForceFit(false);

		this.add(residuesGrid, new RowData(1, 1, new Margins(0)));

		if(useBufferedView)
		{
			BufferView view = new BufferView();
			view.setScrollDelay(0);
			view.setRowHeight(20);
			residuesGrid.setView(view);
		}

		residuesGrid.getView().setViewConfig(new GridViewConfig(){
			@Override
			public String getRowStyle(ModelData model, int rowIndex,
					ListStore<ModelData> ds) {
				if (model != null)
				{
					if ((Integer)model.get("assignment") == InterfaceResidueType.SURFACE.getAssignment())
					{
						return "eppic-grid-row-surface";
					}
					else if((Integer)model.get("assignment") == InterfaceResidueType.CORE_EVOLUTIONARY.getAssignment())
					{
						return "eppic-grid-row-core-evolutionary";
					}
					else if((Integer)model.get("assignment") == InterfaceResidueType.CORE_GEOMETRY.getAssignment())
					{
						return "eppic-grid-row-core-geometry";
					}
					else if((Integer)model.get("assignment") == InterfaceResidueType.RIM.getAssignment())
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
		});
		
		return residuesGrid;
	}
	
	/**
	 * Sets content of residues grid.
	 * @param residueValues list of items to add to the grid
	 */
	public void fillResiduesGrid(List<InterfaceResidueItem> residueValues)
	{
		residuesStore.removeAll();

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
				(((Integer)item.get("assignment") == InterfaceResidueType.CORE_GEOMETRY.getAssignment()) ||
				((Integer)item.get("assignment") == InterfaceResidueType.CORE_EVOLUTIONARY.getAssignment()) ||
				((Integer)item.get("assignment") == InterfaceResidueType.RIM.getAssignment())))
			{
				dataToSet.add(item);
			}
		}

		if(useBufferedView)
		{
			residuesStore.removeAll();
			residuesStore.add(dataToSet);
			residuesStore.commitChanges();
		}
		else
		{
			proxy.setData(dataToSet);
			loader.load(0, nrOfRows);
		}
	}

	/**
	 * Cleans content of residues grid.
	 */
	public void cleanResiduesGrid()
	{
		residuesStore.removeAll();
	}

	/**
	 * Adjusts size of the residues grid based on the size of the screen and initial
	 * settings for the grid.
	 * @param assignedWidth width assigned for the grid
	 */
	public void resizeGrid(int assignedWidth)
	{
		nrOfRows = (this.getHeight() - 50)  / 22;

		int scoresGridWidthOfAllVisibleColumns = GridUtil.calculateWidthOfVisibleColumns(residuesGrid,
																						 initialColumnWidth) + 10;

		if(useBufferedView)
		{
			scoresGridWidthOfAllVisibleColumns += 20;
		}

		int nrOfColumn = residuesGrid.getColumnModel().getColumnCount();

		if (GridUtil.checkIfForceFit(scoresGridWidthOfAllVisibleColumns,
									 assignedWidth))
		{
			float gridWidthMultiplier = (float)assignedWidth / scoresGridWidthOfAllVisibleColumns;

			for (int i = 0; i < nrOfColumn; i++)
			{
				residuesGrid.getColumnModel().setColumnWidth(i, (int)(initialColumnWidth.get(i) * gridWidthMultiplier), true);
			}
		}
		else
		{
			for (int i = 0; i < nrOfColumn; i++)
			{
				residuesGrid.getColumnModel().getColumn(i).setWidth(initialColumnWidth.get(i));
			}

			assignedWidth = scoresGridWidthOfAllVisibleColumns;
		}

		residuesGrid.setWidth(assignedWidth);
		this.setWidth(assignedWidth + 10);

		if(!useBufferedView)
		{
			pagingToolbar.setPageSize(nrOfRows);
			loader.load(0, nrOfRows);
			pagingToolbar.setActivePage(1);
		}
		else
		{
			residuesGrid.getView().refresh(true);
		}

		this.layout();
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
