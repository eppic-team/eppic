/**
 * 
 */
package ch.systemsx.sybit.crkwebui.client.results.gui.panels;

import ch.systemsx.sybit.crkwebui.client.commons.appdata.AppPropertiesManager;
import ch.systemsx.sybit.crkwebui.shared.model.PDBScoreItem;

import com.google.gwt.user.client.ui.Label;
import com.sencha.gxt.core.client.dom.ScrollSupport.ScrollMode;
import com.sencha.gxt.core.client.util.Margins;
import com.sencha.gxt.widget.core.client.container.VerticalLayoutContainer;
import com.sencha.gxt.widget.core.client.container.VerticalLayoutContainer.VerticalLayoutData;
import com.sencha.gxt.widget.core.client.form.FieldSet;

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
		this.setHeadingHtml(AppPropertiesManager.CONSTANTS.info_panel_general_info());
		 
		this.setBorders(true);

		this.setWidth(GeneralInfoRowContainer.getContainerLength() + 40);
		
		this.addStyleName("eppic-rounded-border");
		this.addStyleName("eppic-info-panel");
		
		//generateGeneralInfoPanel();
		
		fillGeneralInfoPanel(pdbScoreItem.getSpaceGroup(), 
									pdbScoreItem.getExpMethod(), 
									pdbScoreItem.getResolution(), 
									pdbScoreItem.getRfreeValue());
	}
	
	/**
	 * Generates the General Info Panel
	 */
	public void generateGeneralInfoPanel(){
		VerticalLayoutContainer mainContainer = new VerticalLayoutContainer();
		mainContainer.setScrollMode(ScrollMode.NONE);
		
		experimentContainer = new GeneralInfoRowContainer();
		mainContainer.add(experimentContainer, new VerticalLayoutData(1, -1,  new Margins(0)));
		
		spaceGroupContainer = new GeneralInfoRowContainer();
		mainContainer.add(spaceGroupContainer, new VerticalLayoutData(1, -1,  new Margins(0)));
		
		resolutionContainer = new GeneralInfoRowContainer();
		mainContainer.add(resolutionContainer, new VerticalLayoutData(1, -1,  new Margins(0)));
		
		rFreeContainer = new GeneralInfoRowContainer();
		mainContainer.add(rFreeContainer, new VerticalLayoutData(1, -1,  new Margins(0)));
		
		this.setWidget(mainContainer);
	}
	
	/**
	 * Fills the general information panel
	 */
	public void fillGeneralInfoPanel(String spaceGroup,
			   String expMethod,
			   double resolution,
			   double rFree){
		
		if(expMethod == null && spaceGroup == null && resolution <=0 && rFree <=0){
			Label nothingFound = new Label(AppPropertiesManager.CONSTANTS.info_panel_nothing_found());
			nothingFound.addStyleName("eppic-general-info-label");
			this.setWidget(nothingFound);
			return;
		}
		
		this.generateGeneralInfoPanel();
		
		if(expMethod != null) experimentContainer.fillContent(AppPropertiesManager.CONSTANTS.info_panel_experiment(), expMethod);
		if(spaceGroup != null) spaceGroupContainer.fillContent(AppPropertiesManager.CONSTANTS.info_panel_spacegroup(), spaceGroup);
		if(resolution > 0) resolutionContainer.fillContent(AppPropertiesManager.CONSTANTS.info_panel_resolution(), resolution+" Å");
		if(rFree > 0) rFreeContainer.fillContent(AppPropertiesManager.CONSTANTS.info_panel_rfree(), rFree+" ");
		
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
