package ch.systemsx.sybit.crkwebui.client.results.gui.panels;

import ch.systemsx.sybit.crkwebui.client.commons.appdata.AppPropertiesManager;
import ch.systemsx.sybit.crkwebui.client.commons.appdata.ApplicationContext;
import ch.systemsx.sybit.crkwebui.client.commons.gui.labels.EppicLabel;
import ch.systemsx.sybit.crkwebui.client.commons.gui.labels.LabelWithTooltip;
import ch.systemsx.sybit.crkwebui.client.commons.gui.links.LinkWithTooltip;
import ch.systemsx.sybit.crkwebui.client.commons.util.EscapedStringGenerator;
import ch.systemsx.sybit.crkwebui.shared.model.ExperimentalWarnings;
import ch.systemsx.sybit.crkwebui.shared.model.InputType;

import com.google.gwt.user.client.ui.FlexTable;
import com.sencha.gxt.widget.core.client.container.HorizontalLayoutContainer;

/**
 * Panel used to store pdb identifier.
 * @author AS
 *
 */
public class PDBIdentifierPanel extends HorizontalLayoutContainer
{
	private FlexTable panelTable;
	
	private EppicLabel informationLabel;
	private EppicLabel pdbNameLabel;
	private LabelWithTooltip warningLabel;
	
	public PDBIdentifierPanel()
	{
		panelTable = new FlexTable();
		panelTable.setCellPadding(0);
		panelTable.setCellSpacing(0);
		
		this.add(panelTable, new HorizontalLayoutData(-1,-1));
		
		informationLabel = new EppicLabel(
				EscapedStringGenerator.generateEscapedString(
						AppPropertiesManager.CONSTANTS.info_panel_pdb_identifier() + ": "));
		informationLabel.addStyleName("eppic-pdb-identifier-label");
		
	}
	
	/**
	 * Sets values of pdb identifier.
	 * @param pdbName pdb name
	 * @param spaceGroup space group
	 * @param expMethod method
	 * @param resolution resolution
	 * @param inputType type of the input - this information is used to determine whether link to pdb description should be added
	 */
	public void setPDBText(String pdbName,
			  			   String spaceGroup,
			  			   String expMethod,
			  			   double resolution,
			  			   double rfreeValue,
			  			   int inputType)
	{
		panelTable.clear();
		
		if(inputType == InputType.PDBCODE.getIndex())
		{
			pdbNameLabel = new LinkWithTooltip(EscapedStringGenerator.generateEscapedString(pdbName),
								AppPropertiesManager.CONSTANTS.pdb_identifier_panel_label_hint(),
								 ApplicationContext.getSettings().getPdbLinkUrl() + pdbName);
		}
		else
		{
			pdbNameLabel = new EppicLabel(EscapedStringGenerator.generateEscapedString(pdbName));
			
		}
		
		//Check for warnings
		ExperimentalWarnings warnings = new ExperimentalWarnings(spaceGroup, expMethod, resolution, rfreeValue);
		if (warnings.isEmWarning()) {
			warningLabel = createWarningLabel(AppPropertiesManager.CONSTANTS.warning_EM_title());			
		}
		else if(warnings.isResolutionWarning()) {			
			warningLabel = createWarningLabel(AppPropertiesManager.CONSTANTS.warning_LowRes_title());
		}else if(warnings.isRfreeWarning()){
			warningLabel = createWarningLabel(AppPropertiesManager.CONSTANTS.warning_HighRfree_title());
		}else{
			warningLabel = null;
		}
		
		pdbNameLabel.addStyleName("eppic-pdb-identifier-label");
		panelTable.setWidget(0, 0, informationLabel);
		panelTable.setWidget(0, 1, pdbNameLabel);
		if(warningLabel != null)
			panelTable.setWidget(0, 2, warningLabel);
		
	}
	
	private LabelWithTooltip createWarningLabel(String text){
		LabelWithTooltip label = new LabelWithTooltip("*"+text+"*", AppPropertiesManager.CONSTANTS.pdb_identifier_panel_warning_hint());
		label.addStyleName("eppic-header-warning");
		label.addStyleName("eppic-pdb-identifier-label");
		return label;
	}
}
