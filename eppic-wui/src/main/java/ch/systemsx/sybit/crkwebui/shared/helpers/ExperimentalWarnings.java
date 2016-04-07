package ch.systemsx.sybit.crkwebui.shared.helpers;

import com.google.gwt.user.client.Window;

import ch.systemsx.sybit.crkwebui.client.commons.appdata.AppPropertiesManager;
import ch.systemsx.sybit.crkwebui.client.commons.appdata.ApplicationContext;
import ch.systemsx.sybit.crkwebui.client.commons.gui.labels.LabelWithTooltip;

/**
 * Class to determine the warnings on methods, resolution and rfree for a pdb
 * @author biyani_n
 *
 */
public class ExperimentalWarnings {
	
	private boolean emWarning;
	private boolean resolutionWarning;
	private boolean rfreeWarning;
	//public static LabelWithTooltip staticWarningsLabel = null;
	

	public ExperimentalWarnings(String spaceGroup,
	   String expMethod,
	   double resolution,
	   double rFree){
		// TODO we should try to set a constant "alw//ays-low-res exp method=ELECTRON MICROSCOPY". 
		// It's not ideal that the name is hard-coded
		emWarning = (expMethod!=null && expMethod.equals("ELECTRON MICROSCOPY") && (resolution <=0 || resolution>=99));
		resolutionWarning = (ApplicationContext.getSettings().getResolutionCutOff() > 0 && 
				             resolution > ApplicationContext.getSettings().getResolutionCutOff() && resolution > 0 && resolution<99);
		rfreeWarning = (ApplicationContext.getSettings().getRfreeCutOff() > 0 && 
				        rFree > ApplicationContext.getSettings().getRfreeCutOff() && rFree > 0 && rFree<1);
	}

	public boolean isEmWarning() {
		return emWarning;
	}

	public void setEmWarning(boolean emWarning) {
		this.emWarning = emWarning;
	}

	public boolean isResolutionWarning() {
		return resolutionWarning;
	}

	public void setResolutionWarning(boolean resolutionWarning) {
		this.resolutionWarning = resolutionWarning;
	}

	public boolean isRfreeWarning() {
		return rfreeWarning;
	}

	public void setRfreeWarning(boolean rfreeWarning) {
		this.rfreeWarning = rfreeWarning;
	}
	
	public LabelWithTooltip getWarningLabel(){
		LabelWithTooltip warningLabel = null;
		if (this.isEmWarning()) {
			warningLabel = createWarningLabel(AppPropertiesManager.CONSTANTS.warning_EM_title());			
		}
		else if(this.isResolutionWarning()) {			
			warningLabel = createWarningLabel(AppPropertiesManager.CONSTANTS.warning_LowRes_title());
		}else if(this.isRfreeWarning()){
			warningLabel = createWarningLabel(AppPropertiesManager.CONSTANTS.warning_HighRfree_title());
		}
		return warningLabel;
	} 
	
	private LabelWithTooltip createWarningLabel(String text){
		LabelWithTooltip label = new LabelWithTooltip("*"+text+"*", AppPropertiesManager.CONSTANTS.pdb_identifier_panel_warning_hint());
		label.addStyleName("eppic-header-warning");
		label.addStyleName("eppic-pdb-identifier-label");
		//this.staticWarningsLabel = label;
		return label;
	}
	
	
}
