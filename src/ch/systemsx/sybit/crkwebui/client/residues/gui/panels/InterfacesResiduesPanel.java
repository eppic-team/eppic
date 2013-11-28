package ch.systemsx.sybit.crkwebui.client.residues.gui.panels;

import java.util.List;

import ch.systemsx.sybit.crkwebui.client.commons.appdata.AppPropertiesManager;
import ch.systemsx.sybit.crkwebui.shared.model.InterfaceResidueItem;
import ch.systemsx.sybit.crkwebui.shared.model.PDBScoreItem;

import com.google.gwt.event.logical.shared.SelectionEvent;
import com.google.gwt.event.logical.shared.SelectionHandler;
import com.sencha.gxt.cell.core.client.form.ComboBoxCell.TriggerAction;
import com.sencha.gxt.core.client.util.Margins;
import com.sencha.gxt.data.shared.LabelProvider;
import com.sencha.gxt.data.shared.ListStore;
import com.sencha.gxt.data.shared.ModelKeyProvider;
import com.sencha.gxt.widget.core.client.container.HorizontalLayoutContainer;
import com.sencha.gxt.widget.core.client.container.HorizontalLayoutContainer.HorizontalLayoutData;
import com.sencha.gxt.widget.core.client.container.VerticalLayoutContainer;
import com.sencha.gxt.widget.core.client.form.ComboBox;
import com.sencha.gxt.widget.core.client.form.FormPanel;
import com.sencha.gxt.widget.core.client.toolbar.FillToolItem;
import com.sencha.gxt.widget.core.client.toolbar.LabelToolItem;
import com.sencha.gxt.widget.core.client.toolbar.ToolBar;

/**
 * Panel containing interface residues for both structures.
 * @author AS
 *
 */
public class InterfacesResiduesPanel extends VerticalLayoutContainer 
{
	private StructurePanel firstStructurePanel;
	private StructurePanel secondStructurePanel;
	
	private ComboBox<String> residuesFilterComboBox;
	
	public InterfacesResiduesPanel()
	{
		this.setBorders(false);
		
		HorizontalLayoutContainer residuesLayoutContainer = new HorizontalLayoutContainer();
	
		firstStructurePanel = new StructurePanel(1);
		residuesLayoutContainer.add(firstStructurePanel, new HorizontalLayoutData(0.49, 1, new Margins(0)));

		FormPanel breakPanel = createBreakPanel();
		residuesLayoutContainer.add(breakPanel, new HorizontalLayoutData(0.02, 1, new Margins(0)));
		
		secondStructurePanel = new StructurePanel(2);
		residuesLayoutContainer.add(secondStructurePanel, new HorizontalLayoutData(0.49, 1, new Margins(0)));
		
		this.add(createToolbar(), new VerticalLayoutData(1,-1));
		
		this.add(residuesLayoutContainer, new VerticalLayoutData(1, 1));

	}
	
	/**
	 * Creates panel used as a break between structures panels.
	 * @return break panel
	 */
	private FormPanel createBreakPanel()
	{
		FormPanel breakPanel = new FormPanel();
		breakPanel.setBorders(false);
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
	private ComboBox<String> createResiduesFilterComboBox()
	{
		ListStore<String> store = new ListStore<String>(new ModelKeyProvider<String>() {
			@Override
			public String getKey(String item) {
				return item;
			}
		});

		store.add(AppPropertiesManager.CONSTANTS.interfaces_residues_combo_all());
		store.add(AppPropertiesManager.CONSTANTS.interfaces_residues_combo_rimcore());
		
		final ComboBox<String> residuesFilterComboBox = new ComboBox<String>(store, new LabelProvider<String>() {
			@Override
			public String getLabel(String item) {
				return item;
			}
		});
		
		residuesFilterComboBox.setId("residuesfilter");
		residuesFilterComboBox.setTriggerAction(TriggerAction.ALL);  
		residuesFilterComboBox.setEditable(false);  
		//residuesFilterComboBox.setFireChangeEventOnSetValue(true);  
		residuesFilterComboBox.setWidth(100);  

		residuesFilterComboBox.setValue(AppPropertiesManager.CONSTANTS.interfaces_residues_combo_rimcore());
		
		residuesFilterComboBox.addSelectionHandler(new SelectionHandler<String>() {
			
			@Override
			public void onSelection(SelectionEvent<String> event) {
				boolean showAll = true;
				
				if(!event.getSelectedItem().equals(AppPropertiesManager.CONSTANTS.interfaces_residues_combo_all()))
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
		residuesFilterComboBox.setValue(AppPropertiesManager.CONSTANTS.interfaces_residues_combo_rimcore());
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
		firstStructurePanel.fillHeader(firstChainName);
		secondStructurePanel.fillHeader(secondChainName);
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
	 */
	public void resizeResiduesPanels() 
	{
		firstStructurePanel.resizeResiduesPanels();
		secondStructurePanel.resizeResiduesPanels();
	}
}
