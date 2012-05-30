package ch.systemsx.sybit.crkwebui.client.gui;

import ch.systemsx.sybit.crkwebui.client.controllers.AppPropertiesManager;
import ch.systemsx.sybit.crkwebui.client.controllers.MainController;

import com.extjs.gxt.ui.client.Style.Orientation;
import com.extjs.gxt.ui.client.Style.Scroll;
import com.extjs.gxt.ui.client.event.Events;
import com.extjs.gxt.ui.client.event.FieldEvent;
import com.extjs.gxt.ui.client.event.Listener;
import com.extjs.gxt.ui.client.util.Margins;
import com.extjs.gxt.ui.client.widget.LayoutContainer;
import com.extjs.gxt.ui.client.widget.form.ComboBox.TriggerAction;
import com.extjs.gxt.ui.client.widget.form.FormPanel;
import com.extjs.gxt.ui.client.widget.form.SimpleComboBox;
import com.extjs.gxt.ui.client.widget.layout.AnchorData;
import com.extjs.gxt.ui.client.widget.layout.AnchorLayout;
import com.extjs.gxt.ui.client.widget.layout.RowData;
import com.extjs.gxt.ui.client.widget.layout.RowLayout;
import com.extjs.gxt.ui.client.widget.toolbar.FillToolItem;
import com.extjs.gxt.ui.client.widget.toolbar.LabelToolItem;
import com.extjs.gxt.ui.client.widget.toolbar.ToolBar;

/**
 * Panel containing interface residues for both structures.
 * @author AS
 *
 */
public class InterfacesResiduesPanel extends FormPanel 
{
	private ResiduesPanel firstStructure;
	private ResiduesPanel secondStructure;
	
	private ResiduesSummaryPanel firstStructureSummary;
	private ResiduesSummaryPanel secondStructureSummary;
	
	private ToolBar toolbar;

	private SimpleComboBox<String> residuesFilterComboBox;
	
	public InterfacesResiduesPanel(MainController mainController,
								   int width,
								   int height)
	{
		this.getHeader().setVisible(false);
		this.setBodyBorder(false);
		this.setBorders(false);
		this.setLayout(new RowLayout(Orientation.VERTICAL));
		
		LayoutContainer residuesLayoutContainer = new LayoutContainer();
		residuesLayoutContainer.setLayout(new RowLayout(Orientation.HORIZONTAL));
		
		firstStructure = new ResiduesPanel(
										   AppPropertiesManager.CONSTANTS.interfaces_residues_panel_first_structure(), 
										   mainController,
										   width);
		
		secondStructure = new ResiduesPanel(
											AppPropertiesManager.CONSTANTS.interfaces_residues_panel_second_structure(),
											mainController,
											width);
		
		firstStructureSummary = new ResiduesSummaryPanel("", mainController, 1);
		secondStructureSummary = new ResiduesSummaryPanel("", mainController, 2);
		
		LayoutContainer firstStructureContainer = new LayoutContainer();
		firstStructureContainer.setScrollMode(Scroll.AUTOX);
		firstStructureContainer.setLayout(new AnchorLayout());
		firstStructureContainer.add(firstStructure, new AnchorData("none -135"));
		firstStructure.setScrollMode(Scroll.NONE);
		
		FormPanel firstStructureBreakPanel = new FormPanel();
		firstStructureBreakPanel.setBodyBorder(false);
		firstStructureBreakPanel.setBorders(false);
		firstStructureBreakPanel.getHeader().setVisible(false);
		firstStructureBreakPanel.setHeight(20);
		firstStructureContainer.add(firstStructureBreakPanel);

		firstStructureContainer.add(firstStructureSummary);
		firstStructureSummary.setHeight(90);
		firstStructureSummary.setScrollMode(Scroll.NONE);
				
		residuesLayoutContainer.add(firstStructureContainer, new RowData(0.48, 1, new Margins(0)));
		
		
		LayoutContainer secondStructureContainer = new LayoutContainer();
		secondStructureContainer.setScrollMode(Scroll.AUTOX);
		secondStructureContainer.setLayout(new AnchorLayout());
		secondStructureContainer.add(secondStructure, new AnchorData("none -135"));
		secondStructure.setScrollMode(Scroll.NONE);
		
		FormPanel secondStructureBreakPanel = new FormPanel();
		secondStructureBreakPanel.setBodyBorder(false);
		secondStructureBreakPanel.setBorders(false);
		secondStructureBreakPanel.getHeader().setVisible(false);
		secondStructureBreakPanel.setHeight(20);
		secondStructureContainer.add(secondStructureBreakPanel);

		secondStructureContainer.add(secondStructureSummary);
		secondStructureSummary.setHeight(90);
		secondStructureSummary.setScrollMode(Scroll.NONE);
		
		FormPanel breakPanel = new FormPanel();
		breakPanel.setBodyBorder(false);
		breakPanel.setBorders(false);
		breakPanel.getHeader().setVisible(false);
		residuesLayoutContainer.add(breakPanel, new RowData(0.04, 1, new Margins(0)));
		
		residuesLayoutContainer.add(secondStructureContainer, new RowData(0.48, 1, new Margins(0)));
		
		this.add(residuesLayoutContainer, new RowData(1, 1, new Margins(0, 0, 0, 0)));
		
		
		
		toolbar = new ToolBar();  
		
		residuesFilterComboBox = new SimpleComboBox<String>();
		residuesFilterComboBox.setId("residuesfilter");
		residuesFilterComboBox.setTriggerAction(TriggerAction.ALL);  
		residuesFilterComboBox.setEditable(false);  
		residuesFilterComboBox.setFireChangeEventOnSetValue(true);  
		residuesFilterComboBox.setWidth(100);  
		residuesFilterComboBox.add(AppPropertiesManager.CONSTANTS.interfaces_residues_combo_all());  
		residuesFilterComboBox.add(AppPropertiesManager.CONSTANTS.interfaces_residues_combo_rimcore());  
		residuesFilterComboBox.setSimpleValue(AppPropertiesManager.CONSTANTS.interfaces_residues_combo_rimcore());
		
		residuesFilterComboBox.addListener(Events.Change, new Listener<FieldEvent>() 
		{  
			public void handleEvent(FieldEvent be) 
			{
				boolean showAll = true;
				
				if(!residuesFilterComboBox.getValue().getValue().equals(AppPropertiesManager.CONSTANTS.interfaces_residues_combo_all()))
				{
					showAll = false;
				}
				
				firstStructure.applyFilter(showAll);
				secondStructure.applyFilter(showAll);
			}  
		}); 
		
		toolbar.add(new FillToolItem());
		
		toolbar.add(new LabelToolItem(AppPropertiesManager.CONSTANTS.interfaces_residues_combo_title()+": ")); 
		toolbar.add(residuesFilterComboBox);  
		
		this.setTopComponent(toolbar);
	}

	/**
	 * Cleans data of residues filter.
	 */
	public void cleanData()
	{
		residuesFilterComboBox.setSimpleValue(AppPropertiesManager.CONSTANTS.interfaces_residues_combo_rimcore());
	}

	/**
	 * Resizes interfaces residues panels.
	 * @param assignedWidth
	 * @param assignedHeight
	 */
	public void resizeResiduesPanels(int assignedWidth, int assignedHeight) 
	{
		int assignedResiduesWidth = (int)((assignedWidth - 36) * 0.48) - 10;
		firstStructure.resizeGrid(assignedResiduesWidth);
		secondStructure.resizeGrid(assignedResiduesWidth);
		firstStructureSummary.resizeGrid(assignedResiduesWidth);
		secondStructureSummary.resizeGrid(assignedResiduesWidth);
		this.layout(true);
	}
	
	/**
	 * Retrieves panel containing interface residues for first structure.
	 * @return panel containing interface residues for first structure
	 */
	public ResiduesPanel getFirstStructurePanel() 
	{
		return firstStructure;
	}

	/**
	 * Retrieves panel containing interface residues for second structure.
	 * @return panel containing interface residues for second structure
	 */
	public ResiduesPanel getSecondStructurePanel() 
	{
		return secondStructure;
	}
	
	/**
	 * Retrieves panel containing interface residues summary for first structure.
	 * @return panel containing interface residues summary for first structure
	 */
	public ResiduesSummaryPanel getFirstStructurePanelSummary() 
	{
		return firstStructureSummary;
	}

	/**
	 * Retrieves panel containing interface residues summary for second structure.
	 * @return panel containing interface residues summary for second structure
	 */
	public ResiduesSummaryPanel getSecondStructurePanelSummary() 
	{
		return secondStructureSummary;
	}

}
