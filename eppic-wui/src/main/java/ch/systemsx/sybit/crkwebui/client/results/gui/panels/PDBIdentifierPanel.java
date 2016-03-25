package ch.systemsx.sybit.crkwebui.client.results.gui.panels;

import ch.systemsx.sybit.crkwebui.client.commons.appdata.AppPropertiesManager;
import ch.systemsx.sybit.crkwebui.client.commons.appdata.ApplicationContext;
import ch.systemsx.sybit.crkwebui.client.commons.gui.labels.EppicLabel;
import ch.systemsx.sybit.crkwebui.client.commons.gui.labels.LabelWithTooltip;
import ch.systemsx.sybit.crkwebui.client.commons.gui.links.LinkWithTooltip;
import ch.systemsx.sybit.crkwebui.client.commons.util.EscapedStringGenerator;
import ch.systemsx.sybit.crkwebui.shared.helpers.ExperimentalWarnings;
import ch.systemsx.sybit.shared.model.InputType;

import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.HTML;
import com.sencha.gxt.widget.core.client.container.HorizontalLayoutContainer;

/**
 * Panel used to store pdb identifier.
 * @author AS
 *
 */
public class PDBIdentifierPanel extends HorizontalLayoutContainer
{
	private FlexTable panelTable;
	
	public static HTML informationLabel;
	public static HTML pdbNameLabel;
	public static LabelWithTooltip warningLabel;
	
	public PDBIdentifierPanel(int viewType)
	{
		panelTable = new FlexTable();
		panelTable.setCellPadding(0);
		panelTable.setCellSpacing(0);
		
		this.add(panelTable, new HorizontalLayoutData(-1,-1));
		
		if(viewType == ResultsPanel.ASSEMBLIES_VIEW){
			informationLabel = new HTML(EscapedStringGenerator.generateEscapedString(
						AppPropertiesManager.CONSTANTS.info_panel_pdb_identifier() + ": "));
		}else if(viewType == ResultsPanel.INTERFACES_VIEW){
			informationLabel = new HTML(EscapedStringGenerator.generateEscapedString(
						AppPropertiesManager.CONSTANTS.info_panel_interface_pdb_identifier() + ": "));
		}
		
		informationLabel.addStyleName("eppic-pdb-identifier-label");
		
	}

	
	/**
	 * Sets values of pdb identifier.
	 * @param inputName pdb/file name
	 * @param spaceGroup space group
	 * @param expMethod method
	 * @param resolution resolution
	 * @param inputType type of the input - this information is used to determine whether link to pdb description should be added
	 */
	public void setPDBText(String inputName,
			  			   String spaceGroup,
			  			   String expMethod,
			  			   double resolution,
			  			   double rfreeValue,
			  			   int inputType)
	{
		
		pdbNameLabel = new HTML(inputName);
		
		//Check for warnings
		ExperimentalWarnings warnings = new ExperimentalWarnings(spaceGroup, expMethod, resolution, rfreeValue);
		warningLabel = warnings.getWarningLabel();
		
		pdbNameLabel.addStyleName("eppic-pdb-identifier-label");
		panelTable.setWidget(0, 0, informationLabel);
		panelTable.setWidget(0, 1, pdbNameLabel);
		
		if(warningLabel != null)
			panelTable.setWidget(0, 2, warningLabel);
		else
			panelTable.setWidget(0, 2, createEmptyWarningLabel(""));

		
	}
	
	
	public static void setPDBText(String inputName)
	{
		pdbNameLabel = new HTML(inputName);
	}	
	
	private LabelWithTooltip createEmptyWarningLabel(String text){
		LabelWithTooltip label = new LabelWithTooltip("", AppPropertiesManager.CONSTANTS.pdb_identifier_panel_warning_hint());
		label.addStyleName("eppic-header-warning");
		label.addStyleName("eppic-pdb-identifier-label");
		return label;
	}
}
