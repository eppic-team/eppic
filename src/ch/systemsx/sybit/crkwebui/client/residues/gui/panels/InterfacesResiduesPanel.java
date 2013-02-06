package ch.systemsx.sybit.crkwebui.client.residues.gui.panels;

import java.util.List;

import ch.systemsx.sybit.crkwebui.client.commons.appdata.AppPropertiesManager;
import ch.systemsx.sybit.crkwebui.client.commons.util.EscapedStringGenerator;
import ch.systemsx.sybit.crkwebui.shared.model.InterfaceResidueItem;
import ch.systemsx.sybit.crkwebui.shared.model.PDBScoreItem;

import com.extjs.gxt.ui.client.Style.Orientation;
import com.extjs.gxt.ui.client.event.Events;
import com.extjs.gxt.ui.client.event.FieldEvent;
import com.extjs.gxt.ui.client.event.Listener;
import com.extjs.gxt.ui.client.util.Margins;
import com.extjs.gxt.ui.client.widget.LayoutContainer;
import com.extjs.gxt.ui.client.widget.form.ComboBox.TriggerAction;
import com.extjs.gxt.ui.client.widget.form.FormPanel;
import com.extjs.gxt.ui.client.widget.form.SimpleComboBox;
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
	private StructurePanel firstStructurePanel;
	private StructurePanel secondStructurePanel;
	
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
	
		firstStructurePanel = new StructurePanel(width, 1);
		residuesLayoutContainer.add(firstStructurePanel, new RowData(0.48, 1, new Margins(0)));

		FormPanel breakPanel = createBreakPanel();
		residuesLayoutContainer.add(breakPanel, new RowData(0.04, 1, new Margins(0)));
		
		secondStructurePanel = new StructurePanel(width, 2);
		residuesLayoutContainer.add(secondStructurePanel, new RowData(0.48, 1, new Margins(0)));
		
		this.add(residuesLayoutContainer, new RowData(1, 1, new Margins(0, 0, 0, 0)));
		
		ToolBar toolbar = createToolbar();  
		this.setTopComponent(toolbar);
	}
	
	/**
	 * Creates panel used as a break between structures panels.
	 * @return break panel
	 */
	private FormPanel createBreakPanel()
	{
		FormPanel breakPanel = new FormPanel();
		breakPanel.setBodyBorder(false);
		breakPanel.setBorders(false);
		breakPanel.getHeader().setVisible(false);
		return breakPanel;
	}
	
	/**
	 * Creates toolbar.
	 * @return toolbar
	 */
	private ToolBar createToolbar()
	{
		ToolBar toolbar = new ToolBar();  
		
		toolbar.add(new FillToolItem());
		
		toolbar.add(new LabelToolItem(AppPropertiesManager.CONSTANTS.interfaces_residues_combo_title() + ": ")); 

		residuesFilterComboBox = createResiduesFilterComboBox();
		toolbar.add(residuesFilterComboBox);  
		
		return toolbar;
	}
	
	/**
	 * Creates selector used to limit types of interface residues to display.
	 * @return residues filter combobox
	 */
	private SimpleComboBox<String> createResiduesFilterComboBox()
	{
		final SimpleComboBox<String> residuesFilterComboBox = new SimpleComboBox<String>();
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
				
				firstStructurePanel.getResiduesPanel().applyFilter(showAll);
				secondStructurePanel.getResiduesPanel().applyFilter(showAll);
			}  
		}); 
		
		return residuesFilterComboBox;
	}
	
	/**
	 * Cleans data of structure filter and panels.
	 */
	public void cleanData()
	{
		residuesFilterComboBox.setSimpleValue(AppPropertiesManager.CONSTANTS.interfaces_residues_combo_rimcore());
		firstStructurePanel.cleanData();
		secondStructurePanel.cleanData();
	}
	
	/**
	 * Fills content of structure panel
	 * @param structureNr nr of the structure
	 * @param pdbScoreItem result of calculations
	 * @param selectedInterface interface for which residues are to be displayed
	 * @param interfaceResidueItems residues to display
	 */
	public void fillStructurePanel(int structureNr,
								   PDBScoreItem pdbScoreItem,
								   int selectedInterface,
								   List<InterfaceResidueItem> interfaceResidueItems)
	{
		if(structureNr == 1)
		{
			firstStructurePanel.fillStructurePanel(pdbScoreItem, selectedInterface, interfaceResidueItems);
		}
		else
		{
			secondStructurePanel.fillStructurePanel(pdbScoreItem, selectedInterface, interfaceResidueItems);
		}
	}
	
	/**
	 * Fills headers of structures panels.
	 * @param firstChainName name of first structure
	 * @param secondChainName name of second structure
	 */
	public void fillHeaders(String firstChainName, String secondChainName) 
	{
		firstStructurePanel.fillHeader(AppPropertiesManager.CONSTANTS.interfaces_residues_panel_structure() + " " + 
									 EscapedStringGenerator.generateEscapedString(firstChainName));
		secondStructurePanel.fillHeader(AppPropertiesManager.CONSTANTS.interfaces_residues_panel_structure() + " " + 
									 EscapedStringGenerator.generateEscapedString(secondChainName));
	}
	
	public void increaseActivePages()
	{
		firstStructurePanel.increaseActivePage();
		secondStructurePanel.increaseActivePage();
	}
	
	public void decreaseActivePages()
	{
		firstStructurePanel.decreaseActivePage();
		secondStructurePanel.decreaseActivePage();
	}

	/**
	 * Resizes interfaces residues panels.
	 * @param assignedWidth
	 * @param assignedHeight
	 */
	public void resizeResiduesPanels(int assignedWidth, int assignedHeight) 
	{
		int assignedResiduesWidth = (int)((assignedWidth - 36) * 0.48) - 10;
		firstStructurePanel.resizeResiduesPanels(assignedResiduesWidth, assignedHeight);
		secondStructurePanel.resizeResiduesPanels(assignedResiduesWidth, assignedHeight);
		this.layout(true);
	}
}
