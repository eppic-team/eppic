/**
 * 
 */
package ch.systemsx.sybit.crkwebui.client.results.gui.panels;

import ch.systemsx.sybit.crkwebui.client.commons.appdata.AppPropertiesManager;
import ch.systemsx.sybit.crkwebui.client.commons.appdata.ApplicationContext;
import ch.systemsx.sybit.crkwebui.shared.model.PDBScoreItem;

import com.extjs.gxt.ui.client.Style.Scroll;
import com.extjs.gxt.ui.client.util.Margins;
import com.extjs.gxt.ui.client.widget.form.FieldSet;
import com.extjs.gxt.ui.client.widget.layout.ColumnLayout;
import com.extjs.gxt.ui.client.widget.layout.RowData;
import com.extjs.gxt.ui.client.widget.Label;

/**
 * Panel containing general information about the pdb
 * @author biyani_n
 *
 */
public class GeneralInfoPanel extends FieldSet {
	
	private GeneralInfoRowContainer experimentContainer;
	private GeneralInfoRowContainer spaceGroupContainer;
	private GeneralInfoRowContainer resolutionContainer;
	private GeneralInfoRowContainer rFreeContainer;
	
	public GeneralInfoPanel(PDBScoreItem pdbScoreItem){
		this.setHeading(AppPropertiesManager.CONSTANTS.info_panel_general_info());
		 
		this.setBorders(true);
		this.setLayout(new ColumnLayout());
		this.setScrollMode(Scroll.AUTOX);

		this.addStyleName("eppic-rounded-border");
		
		generateGeneralInfoPanel();
		
		fillGeneralInfoPanel(pdbScoreItem.getSpaceGroup(), 
									pdbScoreItem.getExpMethod(), 
									pdbScoreItem.getResolution(), 
									pdbScoreItem.getRfreeValue());
	}
	
	/**
	 * Generates the General Info Panel
	 */
	public void generateGeneralInfoPanel(){
		experimentContainer = new GeneralInfoRowContainer();
		this.add(experimentContainer, new RowData(-1, -1,  new Margins(0, 0, 0, 0)));
		
		spaceGroupContainer = new GeneralInfoRowContainer();
		this.add(spaceGroupContainer, new RowData(-1, -1,  new Margins(0, 0, 0, 0)));
		
		resolutionContainer = new GeneralInfoRowContainer();
		this.add(resolutionContainer, new RowData(-1, -1,  new Margins(0, 0, 0, 0)));
		
		rFreeContainer = new GeneralInfoRowContainer();
		this.add(rFreeContainer, new RowData(-1, -1,  new Margins(0, 0, 0, 0)));
	}
	
	/**
	 * Fills the general information panel
	 */
	public void fillGeneralInfoPanel(String spaceGroup,
			   String expMethod,
			   double resolution,
			   double rFree){
		
		this.removeAll();
		this.generateGeneralInfoPanel();
		
		if(expMethod != null) experimentContainer.fillContent(AppPropertiesManager.CONSTANTS.info_panel_experiment(), expMethod);
		if(spaceGroup != null) spaceGroupContainer.fillContent(AppPropertiesManager.CONSTANTS.info_panel_spacegroup(), spaceGroup);
		if(resolution > 0) resolutionContainer.fillContent(AppPropertiesManager.CONSTANTS.info_panel_resolution(), resolution+"Å");
		if(rFree > 0) rFreeContainer.fillContent(AppPropertiesManager.CONSTANTS.info_panel_rfree(), rFree+" ");
		
		if(expMethod == null && spaceGroup == null && resolution <=0 && rFree <=0){
			this.removeAll();
			Label nothingFound = new Label(AppPropertiesManager.CONSTANTS.info_panel_nothing_found());
			nothingFound.addStyleName("eppic-general-info-label");
			this.add(nothingFound);
		}
	}
	
	/**
	 * Resizes the panel with a minimum and maximum width
	 */
	public void resize() {

		int minWidth = GeneralInfoRowContainer.getContainerLength();
		int maxWidth = GeneralInfoRowContainer.getContainerLength()*2 - 50;

		int windowWidth = ApplicationContext.getWindowData().getWindowWidth();
		int currWidth = (int) Math.round(0.20*windowWidth);

		if(currWidth < minWidth)
			this.setWidth(minWidth);
		else if(currWidth > maxWidth)
			this.setWidth(maxWidth);
		else
			this.setWidth(currWidth);

	}

}
