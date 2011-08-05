package ch.systemsx.sybit.crkwebui.client.gui;

import ch.systemsx.sybit.crkwebui.client.controllers.MainController;

import com.extjs.gxt.ui.client.Style.Orientation;
import com.extjs.gxt.ui.client.event.Events;
import com.extjs.gxt.ui.client.event.FieldEvent;
import com.extjs.gxt.ui.client.event.Listener;
import com.extjs.gxt.ui.client.util.Margins;
import com.extjs.gxt.ui.client.widget.form.ComboBox.TriggerAction;
import com.extjs.gxt.ui.client.widget.form.FormPanel;
import com.extjs.gxt.ui.client.widget.form.SimpleComboBox;
import com.extjs.gxt.ui.client.widget.layout.RowData;
import com.extjs.gxt.ui.client.widget.layout.RowLayout;
import com.extjs.gxt.ui.client.widget.toolbar.FillToolItem;
import com.extjs.gxt.ui.client.widget.toolbar.ToolBar;

public class InterfacesResiduesPanel extends FormPanel 
{
	private ResiduesPanel firstStructure;
	private ResiduesPanel secondStructure;
	
	private ToolBar toolbar;

	private SimpleComboBox<String> residuesFilterComboBox;
	
	public InterfacesResiduesPanel(MainController mainController,
								   int width,
								   int height)
	{
		// infoPanel.setFrame(true);
		this.getHeader().setVisible(false);
		this.setBodyBorder(false);
		this.setBorders(false);
		this.setLayout(new RowLayout(Orientation.HORIZONTAL));
		
//		LayoutContainer l = new LayoutContainer();
//		l.setLayout(new RowLayout(Orientation.HORIZONTAL));
		
		firstStructure = new ResiduesPanel(
										   MainController.CONSTANTS.interfaces_residues_panel_first_structure(), 
										   mainController,
										   width,
										   height,
										   1);
		
		secondStructure = new ResiduesPanel(
											MainController.CONSTANTS.interfaces_residues_panel_second_structure(),
											mainController,
											width,
											height,
											2);

		this.add(firstStructure, new RowData(0.48, 1, new Margins(0)));
		
		FormPanel breakPanel = new FormPanel();
		breakPanel.setBodyBorder(false);
		breakPanel.setBorders(false);
		breakPanel.getHeader().setVisible(false);
		this.add(breakPanel, new RowData(0.04, 1, new Margins(0)));

		this.add(secondStructure, new RowData(0.48, 1, new Margins(0)));
		
//		LayoutContainer summaryPanel = new LayoutContainer();
//		summaryPanel.setLayout(new RowLayout(Orientation.HORIZONTAL));
//		
//		summaryPanel.add(new ResiduesSummaryPanel("", mainController, width, height, 1), new RowData(0.48, 1, new Margins(0)));
//		FormPanel summaryPanelBreak = new FormPanel();
//		summaryPanelBreak.setBodyBorder(false);
//		summaryPanelBreak.setBorders(false);
//		summaryPanelBreak.getHeader().setVisible(false);
//		summaryPanel.add(summaryPanelBreak, new RowData(0.04, 1, new Margins(0)));
//		summaryPanel.add(new ResiduesSummaryPanel("", mainController, width, height, 2), new RowData(0.48, 1, new Margins(0)));
//		
//		this.add(l, new RowData(1, 1, new Margins(0)));
//		
//		FormPanel generalPanelBreak = new FormPanel();
//		generalPanelBreak.setBodyBorder(false);
//		generalPanelBreak.setBorders(false);
//		generalPanelBreak.getHeader().setVisible(false);
//		
//		this.add(generalPanelBreak, new RowData(1, 20, new Margins(0)));
//		this.add(summaryPanel, new RowData(1, 65, new Margins(0)));
		
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
	}
	
	public ResiduesPanel getFirstStructurePanel() 
	{
		return firstStructure;
	}

	public ResiduesPanel getSecondStructurePanel() 
	{
		return secondStructure;
	}

}
