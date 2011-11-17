package ch.systemsx.sybit.crkwebui.client.gui;

import ch.systemsx.sybit.crkwebui.client.controllers.MainController;

import com.extjs.gxt.ui.client.Style.Orientation;
import com.extjs.gxt.ui.client.Style.Scroll;
import com.extjs.gxt.ui.client.event.Events;
import com.extjs.gxt.ui.client.event.FieldEvent;
import com.extjs.gxt.ui.client.event.Listener;
import com.extjs.gxt.ui.client.util.Margins;
import com.extjs.gxt.ui.client.util.Padding;
import com.extjs.gxt.ui.client.widget.LayoutContainer;
import com.extjs.gxt.ui.client.widget.form.ComboBox.TriggerAction;
import com.extjs.gxt.ui.client.widget.form.FormPanel;
import com.extjs.gxt.ui.client.widget.form.SimpleComboBox;
import com.extjs.gxt.ui.client.widget.layout.RowData;
import com.extjs.gxt.ui.client.widget.layout.RowLayout;
import com.extjs.gxt.ui.client.widget.layout.VBoxLayout;
import com.extjs.gxt.ui.client.widget.layout.VBoxLayout.VBoxLayoutAlign;
import com.extjs.gxt.ui.client.widget.layout.VBoxLayoutData;
import com.extjs.gxt.ui.client.widget.toolbar.FillToolItem;
import com.extjs.gxt.ui.client.widget.toolbar.ToolBar;

public class InterfacesResiduesPanel extends FormPanel 
{
	private ResiduesPanel firstStructure;
	private ResiduesPanel secondStructure;
	
	private ResiduesSummaryPanel firstStructureSummary;
	private ResiduesSummaryPanel secondStructureSummary;
	
	private ToolBar toolbar;

	private SimpleComboBox<String> residuesFilterComboBox;
	
	private boolean wasResized = false;
	
	public InterfacesResiduesPanel(MainController mainController,
								   int width,
								   int height)
	{
		// infoPanel.setFrame(true);
		this.getHeader().setVisible(false);
		this.setBodyBorder(false);
		this.setBorders(false);
		this.setLayout(new RowLayout(Orientation.VERTICAL));
		
		LayoutContainer residuesLayoutContainer = new LayoutContainer();
		residuesLayoutContainer.setLayout(new RowLayout(Orientation.HORIZONTAL));
		
//		String interfaceName = mainController.getPdbScoreItem().getInterfaceItem(selectedInterface - 1).getName();
//		
//		String firstStructureName = "";
//		String secondStructureName = "";
//		
//		if((interfaceName !=  null) && (interfaceName.contains("+")))
//		{
//			firstStructureName = interfaceName.substring(0, interfaceName.indexOf("+"));
//			
//			if(interfaceName.indexOf("+") < interfaceName.length() - 1)
//			{
//				secondStructureName = interfaceName.substring(interfaceName.indexOf("+") + 1);
//			}
//		}
		
		firstStructure = new ResiduesPanel(
										   MainController.CONSTANTS.interfaces_residues_panel_first_structure(), 
										   mainController,
										   width,
										   height - 110);
		
		secondStructure = new ResiduesPanel(
											MainController.CONSTANTS.interfaces_residues_panel_second_structure(),
											mainController,
											width,
											height - 110);
		
		firstStructureSummary = new ResiduesSummaryPanel("", mainController, 70, 1);
		secondStructureSummary = new ResiduesSummaryPanel("", mainController, 70, 2);
		
		VBoxLayout layout = new VBoxLayout();  
        layout.setPadding(new Padding(0));  
        layout.setVBoxLayoutAlign(VBoxLayoutAlign.LEFT);  
  
		LayoutContainer firstStructureContainer = new LayoutContainer();
		firstStructureContainer.setScrollMode(Scroll.AUTOX);
		firstStructureContainer.setLayout(layout);
		firstStructureContainer.add(firstStructure, new VBoxLayoutData(new Margins(0)));
		
		FormPanel firstStructureBreakPanel = new FormPanel();
		firstStructureBreakPanel.setBodyBorder(false);
		firstStructureBreakPanel.setBorders(false);
		firstStructureBreakPanel.getHeader().setVisible(false);
		firstStructureBreakPanel.setHeight(20);
		firstStructureContainer.add(firstStructureBreakPanel, new VBoxLayoutData(new Margins(0)));

		firstStructureContainer.add(firstStructureSummary, new VBoxLayoutData(new Margins(0)));
		
		FormPanel firstStructureScrollPanel = new FormPanel();
		firstStructureScrollPanel.setBodyBorder(false);
		firstStructureScrollPanel.setBorders(false);
		firstStructureScrollPanel.getHeader().setVisible(false);
		firstStructureScrollPanel.setHeight(20);
		firstStructureContainer.add(firstStructureScrollPanel, new VBoxLayoutData(new Margins(0)));
		
		residuesLayoutContainer.add(firstStructureContainer, new RowData(0.48, 1, new Margins(0)));
		
		layout = new VBoxLayout();  
        layout.setPadding(new Padding(0));  
        layout.setVBoxLayoutAlign(VBoxLayoutAlign.LEFT);  
        
		LayoutContainer secondStructureContainer = new LayoutContainer();
		secondStructureContainer.setScrollMode(Scroll.AUTOX);
		secondStructureContainer.setLayout(layout);
		secondStructureContainer.add(secondStructure, new VBoxLayoutData(new Margins(0)));
		
		FormPanel secondStructureBreakPanel = new FormPanel();
		secondStructureBreakPanel.setBodyBorder(false);
		secondStructureBreakPanel.setBorders(false);
		secondStructureBreakPanel.getHeader().setVisible(false);
		secondStructureBreakPanel.setHeight(20);
		secondStructureContainer.add(secondStructureBreakPanel, new VBoxLayoutData(new Margins(0)));
		
		secondStructureContainer.add(secondStructureSummary, new VBoxLayoutData(new Margins(0)));

		FormPanel breakPanel = new FormPanel();
		breakPanel.setBodyBorder(false);
		breakPanel.setBorders(false);
		breakPanel.getHeader().setVisible(false);
		residuesLayoutContainer.add(breakPanel, new RowData(0.04, 1, new Margins(0)));
		
		FormPanel secondStructureScrollPanel = new FormPanel();
		secondStructureScrollPanel.setBodyBorder(false);
		secondStructureScrollPanel.setBorders(false);
		secondStructureScrollPanel.getHeader().setVisible(false);
		secondStructureScrollPanel.setHeight(20);
		secondStructureContainer.add(secondStructureScrollPanel, new VBoxLayoutData(new Margins(0)));

		residuesLayoutContainer.add(secondStructureContainer, new RowData(0.48, 1, new Margins(0)));
		
		this.add(residuesLayoutContainer, new RowData(1, 1, new Margins(0, 0, 0, 0)));
		
//		LayoutContainer firstStructureContainer = new LayoutContainer();
//		firstStructureContainer.setScrollMode(Scroll.AUTOX);
//		firstStructureContainer.setLayout(new RowLayout(Orientation.VERTICAL));
//		firstStructureContainer.add(firstStructure, new RowData(1, 1, new Margins(0)));
//		
//		FormPanel firstStructureBreakPanel = new FormPanel();
//		firstStructureBreakPanel.setBodyBorder(false);
//		firstStructureBreakPanel.setBorders(false);
//		firstStructureBreakPanel.getHeader().setVisible(false);
//		firstStructureContainer.add(firstStructureBreakPanel, new RowData(1, 20, new Margins(0)));
//
//		firstStructureContainer.add(firstStructureSummary, new RowData(1, 70, new Margins(0)));
//		
//		FormPanel firstStructureScrollPanel = new FormPanel();
//		firstStructureScrollPanel.setBodyBorder(false);
//		firstStructureScrollPanel.setBorders(false);
//		firstStructureScrollPanel.getHeader().setVisible(false);
//		firstStructureContainer.add(firstStructureScrollPanel, new RowData(1, 20, new Margins(0)));
//		
//		residuesLayoutContainer.add(firstStructureContainer, new RowData(0.48, 1, new Margins(0)));
//		
//		LayoutContainer secondStructureContainer = new LayoutContainer();
//		secondStructureContainer.setScrollMode(Scroll.AUTOX);
//		secondStructureContainer.setLayout(new RowLayout());
//		secondStructureContainer.add(secondStructure, new RowData(1, 1));
//		
//		FormPanel secondStructureBreakPanel = new FormPanel();
//		secondStructureBreakPanel.setBodyBorder(false);
//		secondStructureBreakPanel.setBorders(false);
//		secondStructureBreakPanel.getHeader().setVisible(false);
//		secondStructureContainer.add(secondStructureBreakPanel, new RowData(20, 1));
//		
//		secondStructureContainer.add(secondStructureSummary, new RowData(70, 1));
//
//		FormPanel breakPanel = new FormPanel();
//		breakPanel.setBodyBorder(false);
//		breakPanel.setBorders(false);
//		breakPanel.getHeader().setVisible(false);
//		residuesLayoutContainer.add(breakPanel, new RowData(0.04, 1, new Margins(0)));
//		
//		FormPanel secondStructureScrollPanel = new FormPanel();
//		secondStructureScrollPanel.setBodyBorder(false);
//		secondStructureScrollPanel.setBorders(false);
//		secondStructureScrollPanel.getHeader().setVisible(false);
//		secondStructureContainer.add(secondStructureScrollPanel, new RowData(20, 1));
//
//		residuesLayoutContainer.add(secondStructureContainer, new RowData(0.48, 1, new Margins(0)));
//		
//		this.add(residuesLayoutContainer, new RowData(1, 1, new Margins(0, 0, 0, 0)));
		
		toolbar = new ToolBar();  
		
		residuesFilterComboBox = new SimpleComboBox<String>();
		residuesFilterComboBox.setId("residuesfilter");
		residuesFilterComboBox.setTriggerAction(TriggerAction.ALL);  
		residuesFilterComboBox.setEditable(false);  
		residuesFilterComboBox.setFireChangeEventOnSetValue(true);  
		residuesFilterComboBox.setWidth(100);  
		residuesFilterComboBox.add(MainController.CONSTANTS.interfaces_residues_combo_all());  
		residuesFilterComboBox.add(MainController.CONSTANTS.interfaces_residues_combo_rimcore());  
		residuesFilterComboBox.setSimpleValue(MainController.CONSTANTS.interfaces_residues_combo_rimcore());
		
		residuesFilterComboBox.setFieldLabel(MainController.CONSTANTS.interfaces_residues_combo_title());
		residuesFilterComboBox.addListener(Events.Change, new Listener<FieldEvent>() 
		{  
			public void handleEvent(FieldEvent be) 
			{
				boolean showAll = true;
				
				if(!residuesFilterComboBox.getValue().getValue().equals(MainController.CONSTANTS.interfaces_residues_combo_all()))
				{
					showAll = false;
				}
				
				firstStructure.applyFilter(showAll);
				secondStructure.applyFilter(showAll);
			}  
		}); 
		
		toolbar.add(new FillToolItem());
		
		toolbar.add(residuesFilterComboBox);  
		
		this.setTopComponent(toolbar);
	}

	public void cleanData()
	{
		residuesFilterComboBox.setSimpleValue(MainController.CONSTANTS.interfaces_residues_combo_rimcore());
	}

	public void resizeResiduesPanels() 
	{
		firstStructure.resizeGrid();
		secondStructure.resizeGrid();
		firstStructureSummary.resizeGrid();
		secondStructureSummary.resizeGrid();
		this.layout(true);
//		this.repaint();
//		this.recalculate();
	}
	
	public ResiduesPanel getFirstStructurePanel() 
	{
		return firstStructure;
	}

	public ResiduesPanel getSecondStructurePanel() 
	{
		return secondStructure;
	}
	
	public ResiduesSummaryPanel getFirstStructurePanelSummary() 
	{
		return firstStructureSummary;
	}

	public ResiduesSummaryPanel getSecondStructurePanelSummary() 
	{
		return secondStructureSummary;
	}

}
