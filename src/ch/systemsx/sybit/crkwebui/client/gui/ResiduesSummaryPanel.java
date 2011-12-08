package ch.systemsx.sybit.crkwebui.client.gui;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import ch.systemsx.sybit.crkwebui.client.controllers.MainController;
import ch.systemsx.sybit.crkwebui.client.model.InterfaceResidueSummaryModel;
import ch.systemsx.sybit.crkwebui.shared.model.InterfaceScoreItem;
import ch.systemsx.sybit.crkwebui.shared.model.SupportedMethod;

import com.extjs.gxt.ui.client.GXT;
import com.extjs.gxt.ui.client.Style.Scroll;
import com.extjs.gxt.ui.client.data.ModelData;
import com.extjs.gxt.ui.client.store.ListStore;
import com.extjs.gxt.ui.client.widget.ContentPanel;
import com.extjs.gxt.ui.client.widget.grid.ColumnConfig;
import com.extjs.gxt.ui.client.widget.grid.ColumnModel;
import com.extjs.gxt.ui.client.widget.grid.Grid;
import com.extjs.gxt.ui.client.widget.grid.GridViewConfig;
import com.extjs.gxt.ui.client.widget.layout.FitLayout;
import com.google.gwt.i18n.client.NumberFormat;

/**
 * This panel is used to display the residues for one structure
 * @author srebniak_a
 *
 */
public class ResiduesSummaryPanel extends ContentPanel
{
	private List<ColumnConfig> residuesConfigs;
	private ListStore<InterfaceResidueSummaryModel> residuesStore;
	private ColumnModel residuesColumnModel;
	private Grid<InterfaceResidueSummaryModel> residuesGrid;
	private List<Integer> initialColumnWidth;
	
	private MainController mainController;
	
	private int structure;
	
	private boolean useBufferedView = false;
	
	public ResiduesSummaryPanel(
						 String header, 
						 final MainController mainController,
						 int height,
						 int structure) 
	{
		if(GXT.isIE8)
		{
			useBufferedView = true;
		}
		
		this.mainController = mainController;
		this.structure = structure;
		this.setBodyBorder(false);
		this.setBorders(false);
		this.setLayout(new FitLayout());
		this.getHeader().setVisible(false);
		this.setScrollMode(Scroll.NONE);
		this.setHeight(height);

		residuesConfigs = createColumnConfig();

		residuesStore = new ListStore<InterfaceResidueSummaryModel>();
		residuesColumnModel = new ColumnModel(residuesConfigs);
		
		residuesGrid = new Grid<InterfaceResidueSummaryModel>(residuesStore, residuesColumnModel);
		residuesGrid.setBorders(false);
		residuesGrid.setStripeRows(true);
		residuesGrid.setColumnLines(false);
		residuesGrid.setHideHeaders(true);
		residuesGrid.getSelectionModel().setLocked(true);
		residuesGrid.setLoadMask(true);
		
		residuesGrid.getView().setViewConfig(new GridViewConfig(){
			@Override
			public String getRowStyle(ModelData model, int rowIndex,
					ListStore<ModelData> ds) 
			{
				return "summary";
			}
		});
		
		residuesGrid.disableTextSelection(false);
		residuesGrid.getView().setForceFit(false);
		this.add(residuesGrid);
	}
	
	private List<ColumnConfig> createColumnConfig() 
	{
		List<ColumnConfig> configs = GridColumnConfigGenerator.createColumnConfigs(mainController,
				   "residues_summary",
				   new InterfaceResidueSummaryModel());

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

	public void fillResiduesGrid() 
	{
		residuesStore.removeAll();
		
		List<InterfaceResidueSummaryModel> interfaceSummaryItems = new ArrayList<InterfaceResidueSummaryModel>();

		NumberFormat number = NumberFormat.getFormat("0.00");
		
		Map<String, String> coreMethodValues = new HashMap<String, String>();
		Map<String, String> rimMethodValues = new HashMap<String, String>();
		Map<String, String> ratioMethodValues = new HashMap<String, String>();
		
		for (InterfaceScoreItem scoreItem : mainController.getPdbScoreItem().getInterfaceItem(mainController.getMainViewPort().getInterfacesResiduesWindow().getSelectedInterface() - 1).getInterfaceScores()) 
		{
			String coreValue = "";
			String rimValue = "";
			String ratioValue = "";
			
			if(structure == 1)
			{
				coreValue = number.format(scoreItem.getUnweightedCore1Scores());
				rimValue = number.format(scoreItem.getUnweightedRim1Scores());
				ratioValue = number.format(scoreItem.getUnweightedRatio1Scores());
			}
			else
			{
				coreValue = number.format(scoreItem.getUnweightedCore2Scores());
				rimValue = number.format(scoreItem.getUnweightedRim2Scores());
				ratioValue = number.format(scoreItem.getUnweightedRatio2Scores());
			}
			
			coreMethodValues.put(scoreItem.getMethod(), coreValue);
			rimMethodValues.put(scoreItem.getMethod(), rimValue);
			ratioMethodValues.put(scoreItem.getMethod(), ratioValue);
		}
	
		InterfaceResidueSummaryModel model = new InterfaceResidueSummaryModel();
		model.setTitle(MainController.CONSTANTS.interfaces_residues_aggergation_total_cores());
		
		double asa = 0;
		double bsa = 0;
		
		if(structure == 1)
		{
			asa = mainController.getPdbScoreItem().getInterfaceItem(mainController.getMainViewPort().getInterfacesResiduesWindow().getSelectedInterface() - 1).getAsaC1();
			bsa = mainController.getPdbScoreItem().getInterfaceItem(mainController.getMainViewPort().getInterfacesResiduesWindow().getSelectedInterface() - 1).getBsaC1();
		}
		else
		{
			asa = mainController.getPdbScoreItem().getInterfaceItem(mainController.getMainViewPort().getInterfacesResiduesWindow().getSelectedInterface() - 1).getAsaC2();
			bsa = mainController.getPdbScoreItem().getInterfaceItem(mainController.getMainViewPort().getInterfacesResiduesWindow().getSelectedInterface() - 1).getBsaC2();
		}
		
		model.setAsa(asa);
		model.setBsa(bsa);
		
		for (SupportedMethod method : mainController.getSettings().getScoresTypes()) 
		{
			model.set(method.getName(), coreMethodValues.get(method.getName())); 
		}
		
		interfaceSummaryItems.add(model);
		
		
		model = new InterfaceResidueSummaryModel();
		model.setTitle(MainController.CONSTANTS.interfaces_residues_aggergation_total_rims());
		
		asa = 0;
		bsa = 0;
		
		if(structure == 1)
		{
			asa = mainController.getPdbScoreItem().getInterfaceItem(mainController.getMainViewPort().getInterfacesResiduesWindow().getSelectedInterface() - 1).getAsaR1();
			bsa = mainController.getPdbScoreItem().getInterfaceItem(mainController.getMainViewPort().getInterfacesResiduesWindow().getSelectedInterface() - 1).getBsaR1();
		}
		else
		{
			asa = mainController.getPdbScoreItem().getInterfaceItem(mainController.getMainViewPort().getInterfacesResiduesWindow().getSelectedInterface() - 1).getAsaR2();
			bsa = mainController.getPdbScoreItem().getInterfaceItem(mainController.getMainViewPort().getInterfacesResiduesWindow().getSelectedInterface() - 1).getBsaR2();
		}
		
		model.setAsa(asa);
		model.setBsa(bsa);
		
		for (SupportedMethod method : mainController.getSettings().getScoresTypes()) 
		{
			model.set(method.getName(), rimMethodValues.get(method.getName())); 
		}
		
		interfaceSummaryItems.add(model);
		
		
		model = new InterfaceResidueSummaryModel();
		model.setTitle(MainController.CONSTANTS.interfaces_residues_aggergation_ratios());
		
		for (SupportedMethod method : mainController.getSettings().getScoresTypes()) 
		{
			model.set(method.getName(), ratioMethodValues.get(method.getName())); 
		}
		
		interfaceSummaryItems.add(model);
		
		residuesStore.add(interfaceSummaryItems);
	}
	
	public void cleanResiduesGrid()
	{
		residuesStore.removeAll();
	}
	
	public void resizeGrid(int assignedWidth) 
	{
		int scoresGridWidthOfAllVisibleColumns = GridUtil.calculateWidthOfVisibleColumns(residuesGrid, initialColumnWidth) + 10;
		
		if(useBufferedView)
		{
			scoresGridWidthOfAllVisibleColumns += 20;
		}

//		int assignedWidth = (int)((mainController.getMainViewPort().getInterfacesResiduesWindow().getWindowWidth() - 30) * 0.48);
		
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
		
//		if(useBufferedView)
		{
			residuesGrid.getView().refresh(true);
		}
		
		this.layout();
	}
	
	public Grid<InterfaceResidueSummaryModel> getResiduesGrid() 
	{
		return residuesGrid;
	}
}
