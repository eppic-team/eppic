package ch.systemsx.sybit.crkwebui.client.gui;

import ch.systemsx.sybit.crkwebui.client.controllers.AppPropertiesManager;
import ch.systemsx.sybit.crkwebui.client.gui.util.EscapedStringGenerator;

import com.extjs.gxt.ui.client.Style.Orientation;
import com.extjs.gxt.ui.client.Style.Scroll;
import com.extjs.gxt.ui.client.event.Events;
import com.extjs.gxt.ui.client.event.FieldEvent;
import com.extjs.gxt.ui.client.event.Listener;
import com.extjs.gxt.ui.client.util.Margins;
import com.extjs.gxt.ui.client.widget.Label;
import com.extjs.gxt.ui.client.widget.LayoutContainer;
import com.extjs.gxt.ui.client.widget.form.ComboBox.TriggerAction;
import com.extjs.gxt.ui.client.widget.form.FormPanel;
import com.extjs.gxt.ui.client.widget.form.SimpleComboBox;
import com.extjs.gxt.ui.client.widget.layout.AnchorData;
import com.extjs.gxt.ui.client.widget.layout.AnchorLayout;
import com.extjs.gxt.ui.client.widget.layout.RowData;
import com.extjs.gxt.ui.client.widget.layout.RowLayout;
import com.extjs.gxt.ui.client.widget.layout.VBoxLayout;
import com.extjs.gxt.ui.client.widget.layout.VBoxLayout.VBoxLayoutAlign;
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
	private Label firstStructureHeader;
	private Label secondStructureHeader;
	
	private ResiduesPanel firstStructurePanel;
	private ResiduesPanel secondStructurePanel;
	
	private ResiduesSummaryPanel firstStructureSummaryPanel;
	private ResiduesSummaryPanel secondStructureSummaryPanel;
	
	private SimpleComboBox<String> residuesFilterComboBox;
	
	public InterfacesResiduesPanel(int width,
								   int height)
	{
		this.getHeader().setVisible(false);
		this.setBodyBorder(false);
		this.setBorders(false);
		this.setLayout(new RowLayout(Orientation.VERTICAL));
		
		LayoutContainer residuesLayoutContainer = new LayoutContainer();
		residuesLayoutContainer.setLayout(new RowLayout(Orientation.HORIZONTAL));
	
		firstStructureHeader = new Label();
		secondStructureHeader = new Label();
		
		firstStructurePanel = new ResiduesPanel(width);
		firstStructurePanel.setScrollMode(Scroll.NONE);
		
		secondStructurePanel = new ResiduesPanel(width);
		secondStructurePanel.setScrollMode(Scroll.NONE);
		
		firstStructureSummaryPanel = new ResiduesSummaryPanel("", 1);
		secondStructureSummaryPanel = new ResiduesSummaryPanel("", 2);
		
		firstStructureSummaryPanel.setHeight(90);
		firstStructureSummaryPanel.setScrollMode(Scroll.NONE);
		secondStructureSummaryPanel.setHeight(90);
		secondStructureSummaryPanel.setScrollMode(Scroll.NONE);
		
		LayoutContainer firstStructureContainer = createStructurePanel(firstStructureHeader,
																	   firstStructurePanel,
																	   firstStructureSummaryPanel);
		residuesLayoutContainer.add(firstStructureContainer, new RowData(0.48, 1, new Margins(0)));

		FormPanel breakPanel = new FormPanel();
		breakPanel.setBodyBorder(false);
		breakPanel.setBorders(false);
		breakPanel.getHeader().setVisible(false);
		residuesLayoutContainer.add(breakPanel, new RowData(0.04, 1, new Margins(0)));
		
		LayoutContainer secondStructureContainer = createStructurePanel(secondStructureHeader,
																	    secondStructurePanel,
																	    secondStructureSummaryPanel);
		
		residuesLayoutContainer.add(secondStructureContainer, new RowData(0.48, 1, new Margins(0)));
		
		this.add(residuesLayoutContainer, new RowData(1, 1, new Margins(0, 0, 0, 0)));
		
		ToolBar toolbar = createToolbar();  
		this.setTopComponent(toolbar);
	}
	
	/**
	 * Creates toolbar.
	 * @return toolbar
	 */
	private ToolBar createToolbar()
	{
		ToolBar toolbar = new ToolBar();  
		
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
				
				firstStructurePanel.applyFilter(showAll);
				secondStructurePanel.applyFilter(showAll);
			}  
		}); 
		
		toolbar.add(new FillToolItem());
		
		toolbar.add(new LabelToolItem(AppPropertiesManager.CONSTANTS.interfaces_residues_combo_title() + ": ")); 
		toolbar.add(residuesFilterComboBox);  
		
		return toolbar;
	}
	
	/**
	 * Creates structure panel.
	 * @param structureHeader structure header label
	 * @param residuesPanel residues panel
	 * @param residuesSummaryPanel residues summary panel
	 * @return structure panel
	 */
	private LayoutContainer createStructurePanel(Label structureHeader,
												 ResiduesPanel residuesPanel,
												 ResiduesSummaryPanel residuesSummaryPanel)
	{
		LayoutContainer structureContainer = new LayoutContainer();
		structureContainer.setScrollMode(Scroll.AUTOX);
		structureContainer.setLayout(new AnchorLayout());

		VBoxLayout layout = new VBoxLayout();
	    layout.setVBoxLayoutAlign(VBoxLayoutAlign.CENTER);
	    
		LayoutContainer structureHeaderPanel = new LayoutContainer();
		structureHeaderPanel.setHeight(20);
		structureHeaderPanel.setLayout(layout);
		structureHeaderPanel.add(structureHeader);
		structureContainer.add(structureHeaderPanel);
		
		structureContainer.add(residuesPanel, new AnchorData("none -155"));
		
		FormPanel structureBreakPanel = new FormPanel();
		structureBreakPanel.setBodyBorder(false);
		structureBreakPanel.setBorders(false);
		structureBreakPanel.getHeader().setVisible(false);
		structureBreakPanel.setHeight(20);
		structureContainer.add(structureBreakPanel);

		structureContainer.add(residuesSummaryPanel);
		
		return structureContainer;
	}
	
	/**
	 * Cleans data of residues filter.
	 */
	public void cleanData()
	{
		residuesFilterComboBox.setSimpleValue(AppPropertiesManager.CONSTANTS.interfaces_residues_combo_rimcore());
		firstStructurePanel.cleanResiduesGrid();
		secondStructurePanel.cleanResiduesGrid();
		firstStructureSummaryPanel.cleanResiduesGrid();
		secondStructureSummaryPanel.cleanResiduesGrid();
	}
	
	/**
	 * Fills headers of structures panels.
	 * @param firstChainName name of first structure
	 * @param secondChainName name of second structure
	 */
	public void fillHeaders(String firstChainName, String secondChainName) 
	{
		firstStructureHeader.setText(AppPropertiesManager.CONSTANTS.interfaces_residues_panel_structure() + " " + 
									 EscapedStringGenerator.generateEscapedString(firstChainName));
		secondStructureHeader.setText(AppPropertiesManager.CONSTANTS.interfaces_residues_panel_structure() + " " + 
									 EscapedStringGenerator.generateEscapedString(secondChainName));
	}

	/**
	 * Resizes interfaces residues panels.
	 * @param assignedWidth
	 * @param assignedHeight
	 */
	public void resizeResiduesPanels(int assignedWidth, int assignedHeight) 
	{
		int assignedResiduesWidth = (int)((assignedWidth - 36) * 0.48) - 10;
		firstStructurePanel.resizeGrid(assignedResiduesWidth);
		secondStructurePanel.resizeGrid(assignedResiduesWidth);
		firstStructureSummaryPanel.resizeGrid(assignedResiduesWidth);
		secondStructureSummaryPanel.resizeGrid(assignedResiduesWidth);
		this.layout(true);
	}
	
	/**
	 * Retrieves panel containing interface residues for first structure.
	 * @return panel containing interface residues for first structure
	 */
	public ResiduesPanel getFirstStructurePanel() 
	{
		return firstStructurePanel;
	}

	/**
	 * Retrieves panel containing interface residues for second structure.
	 * @return panel containing interface residues for second structure
	 */
	public ResiduesPanel getSecondStructurePanel() 
	{
		return secondStructurePanel;
	}
	
	/**
	 * Retrieves panel containing interface residues summary for first structure.
	 * @return panel containing interface residues summary for first structure
	 */
	public ResiduesSummaryPanel getFirstStructurePanelSummary() 
	{
		return firstStructureSummaryPanel;
	}

	/**
	 * Retrieves panel containing interface residues summary for second structure.
	 * @return panel containing interface residues summary for second structure
	 */
	public ResiduesSummaryPanel getSecondStructurePanelSummary() 
	{
		return secondStructureSummaryPanel;
	}
}
