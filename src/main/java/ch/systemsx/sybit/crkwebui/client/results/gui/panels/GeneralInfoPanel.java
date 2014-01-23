/**
 * 
 */
package ch.systemsx.sybit.crkwebui.client.results.gui.panels;

import ch.systemsx.sybit.crkwebui.client.commons.appdata.AppPropertiesManager;
import ch.systemsx.sybit.crkwebui.client.commons.util.EscapedStringGenerator;
import ch.systemsx.sybit.crkwebui.client.commons.util.StyleGenerator;
import ch.systemsx.sybit.crkwebui.shared.model.ExperimentalWarnings;
import ch.systemsx.sybit.crkwebui.shared.model.PDBScoreItem;

import com.google.gwt.i18n.client.NumberFormat;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Label;
import com.sencha.gxt.core.client.dom.ScrollSupport.ScrollMode;
import com.sencha.gxt.core.client.dom.XElement;
import com.sencha.gxt.widget.core.client.container.CssFloatLayoutContainer;
import com.sencha.gxt.widget.core.client.form.FieldSet;
import com.sencha.gxt.widget.core.client.tips.ToolTip;
import com.sencha.gxt.widget.core.client.tips.ToolTipConfig;

/**
 * Panel containing general information about the pdb
 * @author biyani_n
 *
 */
public class GeneralInfoPanel extends FieldSet {
	
	private static final int PANEL_WIDTH = 370;
	
	private static final int EXPERIMENT_ROW = 0;
	private static final int SPACEGROUP_ROW = 1;
	private static final int RESOLUTION_ROW = 2;
	private static final int RFREE_ROW = 3;
	
	private FlexTable panelTable;
	
	public GeneralInfoPanel(PDBScoreItem pdbScoreItem){
		
		this.setHeadingHtml(StyleGenerator.defaultFontStyleString(
				AppPropertiesManager.CONSTANTS.info_panel_general_info()));
		 
		this.setBorders(true);
		this.setWidth(PANEL_WIDTH);
		
		this.addStyleName("eppic-rounded-border");
		this.addStyleName("eppic-info-panel");
		
		CssFloatLayoutContainer mainContainer = new CssFloatLayoutContainer();
    	mainContainer.setScrollMode(ScrollMode.AUTO);
		
		panelTable = new FlexTable();
		panelTable.setCellPadding(0);
		panelTable.setCellSpacing(0);
		
		mainContainer.add(panelTable);
    	
    	this.setWidget(mainContainer);
		
		fillGeneralInfoPanel(pdbScoreItem.getSpaceGroup(), 
									pdbScoreItem.getExpMethod(), 
									pdbScoreItem.getResolution(), 
									pdbScoreItem.getRfreeValue());
	}
	
	/**
	 * Fills the general information panel
	 */
	public void fillGeneralInfoPanel(String spaceGroup,
			   String expMethod,
			   double resolution,
			   double rFree){
		
		panelTable.clear();
		
		if(expMethod == null && spaceGroup == null && resolution <=0 && rFree <=0){
			Label nothingFound = new Label(AppPropertiesManager.CONSTANTS.info_panel_nothing_found());
			nothingFound.addStyleName("eppic-general-info-label");
			panelTable.setWidget(0, 0, nothingFound);
			return;
		} else{
			
		
		if(expMethod != null)
			fillContent(EXPERIMENT_ROW, AppPropertiesManager.CONSTANTS.info_panel_experiment(), expMethod);
		if(spaceGroup != null)
			fillContent(SPACEGROUP_ROW, AppPropertiesManager.CONSTANTS.info_panel_spacegroup(), spaceGroup);
		if(resolution > 0)
			fillContent(RESOLUTION_ROW, AppPropertiesManager.CONSTANTS.info_panel_resolution(), resolution+" Å");
		if(rFree > 0)
			fillContent(RFREE_ROW, AppPropertiesManager.CONSTANTS.info_panel_rfree(), NumberFormat.getFormat("0.00").format(rFree));
		
		}
		
		fillWarnings(new ExperimentalWarnings(spaceGroup, expMethod, resolution, rFree));
		
	}
	
	/**
	 * Fills the table with label, value and warning if != null
	 * @param row
	 * @param fieldLabel
	 * @param fieldValue
	 */
	private void fillContent(int row, String fieldLabel, String fieldValue){
		HTML fieldLabelHTML = new HTML(EscapedStringGenerator.generateEscapedString(fieldLabel));
		fieldLabelHTML.addStyleName("eppic-general-info-label");
		HTML fieldValueHTML = new HTML(EscapedStringGenerator.generateEscapedString(fieldValue));
		fieldValueHTML.addStyleName("eppic-general-info-label-value");
		
		panelTable.setWidget(row, 0, fieldLabelHTML);
		panelTable.setWidget(row, 2, fieldValueHTML);	
	}
	
	/**
	 * Fills warning images if any
	 * @param spaceGroup
	 * @param expMethod
	 * @param resolution
	 * @param rFree
	 */
	private void fillWarnings(ExperimentalWarnings warnings){
		
		if (warnings.isEmWarning()){
			panelTable.setWidget(EXPERIMENT_ROW, 1, getWarningImage(AppPropertiesManager.CONSTANTS.warning_EM_text()));			
		}
		if(warnings.isResolutionWarning()) {			
			panelTable.setWidget(RESOLUTION_ROW, 1, getWarningImage(AppPropertiesManager.CONSTANTS.warning_LowRes_text()));
		}
		if(warnings.isRfreeWarning()){
			panelTable.setWidget(RFREE_ROW, 1, getWarningImage(AppPropertiesManager.CONSTANTS.warning_HighRfree_text()));
		}
		
	}
	
	/**
	 * Returns the warning image with it's tooltip
	 * @param tooltipText
	 * @return
	 */
	private Image getWarningImage(String tooltipText){
		Image warningImage = null;
		if(tooltipText != null){
			warningImage = new Image("resources/icons/warning_icon_14x14.png");
			warningImage.getElement().<XElement>cast().applyStyles("verticalAlign:bottom;");
			ToolTipConfig ttConfig = new ToolTipConfig(tooltipText);
			new ToolTip(warningImage, ttConfig);
		}
		
		return warningImage;
	}
	
	/**
	 * Resizes the panel with a minimum and maximum width
	 */
	public void resizePanel() {

//		int minWidth = GeneralInfoRowContainer.getContainerLength() + 40;
//		int maxWidth = GeneralInfoRowContainer.getContainerLength()*2 - 50;
//
//		int windowWidth = ApplicationContext.getWindowData().getWindowWidth();
//		int currWidth = (int) Math.round(0.20*windowWidth);
//
//		if(currWidth < minWidth)
//			this.setWidth(minWidth);
//		else if(currWidth > maxWidth)
//			this.setWidth(maxWidth);
//		else
//			this.setWidth(currWidth);

	}

}
