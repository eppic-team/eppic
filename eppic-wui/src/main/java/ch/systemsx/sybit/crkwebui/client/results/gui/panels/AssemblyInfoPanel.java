package ch.systemsx.sybit.crkwebui.client.results.gui.panels;

import ch.systemsx.sybit.crkwebui.client.commons.appdata.AppPropertiesManager;
import ch.systemsx.sybit.crkwebui.client.commons.util.StyleGenerator;
import ch.systemsx.sybit.crkwebui.shared.model.PdbInfo;

import com.google.gwt.user.client.ui.FlexTable;
import com.sencha.gxt.core.client.dom.ScrollSupport.ScrollMode;
import com.sencha.gxt.widget.core.client.container.CssFloatLayoutContainer;
import com.sencha.gxt.widget.core.client.form.FieldSet;

public class AssemblyInfoPanel extends FieldSet {
	
	private static final int PANEL_WIDTH = 370;
	private FlexTable panelTable;
	
	public AssemblyInfoPanel(PdbInfo pdbInfo){
		
		this.setHeadingHtml(StyleGenerator.defaultFontStyleString(
				AppPropertiesManager.CONSTANTS.info_panel_assembly_info()));
		 
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
		
		/*fillGeneralInfoPanel(pdbInfo.getSpaceGroup(), 
									pdbInfo.getExpMethod(), 
									pdbInfo.getResolution(), 
									pdbInfo.getRfreeValue());*/
	}

}
