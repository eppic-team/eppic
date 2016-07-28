package ch.systemsx.sybit.crkwebui.shared.helpers;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

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
	private boolean noRfreeWarning;
	private boolean spaceGroupWarning;
	private String warningTooltip;
	//public static LabelWithTooltip staticWarningsLabel = null;
	
	public String[] spaceGroups = {null, "P 1", "P 2", "P 21", "P 1 2 1", "P 1 21 1", "C 2", "C 1 2 1", "P 2 2 2", "P 2 2 21", "P 21 21 2",
									"P 21 21 21", "C 2 2 2", "C 2 2 21", "F 2 2 2", "I 2 2 2", "I 21 21 21", "P 4", "P 41", "P 42",
									"P 43", "I 4", "I 41", "P 4 2 2", "P 4 21 2", "P 41 2 2", "P 41 21 2", "P 42 2 2", "P 42 21 2",
									"P 43 2 2", "P 43 21 2", "I 4 2 2", "I 4 21 2", "P 3", "P 31", "P 32", "R 3", "P 3 1 2",
									"P 31 1 2", "P 32 1 2", "P 3 2 1", "P 31 2 1", "P 32 2 1", "R 3 2", "P 6", "P 61", "P 62",
									"P 63", "P 64", "P 65", "P 6 2 2", "P 61 2 2", "P 62 2 2", "P 63 2 2", "P 64 2 2", "P 65 2 2",
									"P 2 3", "P 21 3", "F 2 3", "I 2 3", "I 21 3", "P 4 3 2", "P 42 3 2", "P 43 3 2", "P 41 3 2",
									"F 4 3 2", "F 41 3 2", "I 4 3 2", "I 41 3 2", "P 1 1 2", "P 1 1 21", "B 2", "B 1 1 2",
									"A 1 2 1", "C 1 21 1", "I 1 2 1", "I 2", "I 1 21 1", "P 21 2 2", "P 2 21 2", "P 21 2 21", "P 2 21 21",
									"H 3", "H 3 2"};
	
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
		noRfreeWarning = (rFree == 1 && expMethod.equals("X-RAY DIFFRACTION"));
		spaceGroupWarning = (expMethod.equals("X-RAY DIFFRACTION") && !Arrays.asList(spaceGroups).contains(spaceGroup));
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
	
	public boolean isNoRfreeWarning() {
		return noRfreeWarning;
	}

	public void setRfreeWarning(boolean rfreeWarning) {
		this.rfreeWarning = rfreeWarning;
	}
	
	public boolean isSpaceGroupWarning(){
		return spaceGroupWarning;
	}
	
	public void setSpaceGroupWarning(boolean spaceGroupWarning){
		this.spaceGroupWarning = spaceGroupWarning;
	}
	
	//for single warnings only
	/*public LabelWithTooltip getWarningLabel(){
		LabelWithTooltip warningLabel = null;
		if (this.isEmWarning()) {
			warningTooltip = AppPropertiesManager.CONSTANTS.warning_EM_text();
			warningLabel = createWarningLabel(AppPropertiesManager.CONSTANTS.warning_EM_title(), warningTooltip);	
		}else if(this.isResolutionWarning()) {	
			warningTooltip = AppPropertiesManager.CONSTANTS.warning_LowRes_text();
			warningLabel = createWarningLabel(AppPropertiesManager.CONSTANTS.warning_LowRes_title(), warningTooltip);	
		}else if(this.isRfreeWarning()){
			warningTooltip = AppPropertiesManager.CONSTANTS.warning_HighRfree_text();
			warningLabel = createWarningLabel(AppPropertiesManager.CONSTANTS.warning_HighRfree_title(), warningTooltip);
		}else if(this.isNoRfreeWarning()){
			warningTooltip = AppPropertiesManager.CONSTANTS.warning_NoRfree_text();
			warningLabel = createWarningLabel(AppPropertiesManager.CONSTANTS.warning_NoRfree_title(), warningTooltip);	
		}
		return warningLabel;
	} */
	
	//for single and multiple warnings
	public LabelWithTooltip getWarningLabel(){
		LabelWithTooltip warningLabel = null;
		int warningCount = 0;
		String multiWarningTooltip = "WARNINGS<br>";
		
		if (this.isEmWarning()) {
			warningTooltip = AppPropertiesManager.CONSTANTS.warning_EM_text();
			warningLabel = createWarningLabel(AppPropertiesManager.CONSTANTS.warning_EM_title(), warningTooltip);
			warningCount++;
			multiWarningTooltip += "&bull; " + AppPropertiesManager.CONSTANTS.warning_EM_title().replace("Warning: ", "") + "<br>" + AppPropertiesManager.CONSTANTS.warning_EM_text() + "<br>";
		}
		if(this.isResolutionWarning()) {
			warningTooltip = AppPropertiesManager.CONSTANTS.warning_LowRes_text();
			warningLabel = createWarningLabel(AppPropertiesManager.CONSTANTS.warning_LowRes_title(), warningTooltip);	
			warningCount++;
			multiWarningTooltip += "&bull; " + AppPropertiesManager.CONSTANTS.warning_LowRes_title().replace("Warning: ", "") + "<br>" + AppPropertiesManager.CONSTANTS.warning_LowRes_text() + "<br>";
		}
		if(this.isRfreeWarning()){
			warningTooltip = AppPropertiesManager.CONSTANTS.warning_HighRfree_text();
			warningLabel = createWarningLabel(AppPropertiesManager.CONSTANTS.warning_HighRfree_title(), warningTooltip);
			warningCount++;
			multiWarningTooltip += "&bull; " + AppPropertiesManager.CONSTANTS.warning_HighRfree_title().replace("Warning: ", "") + "<br>" + AppPropertiesManager.CONSTANTS.warning_HighRfree_text() + "<br>";
		}
		if(this.isNoRfreeWarning()){
			warningTooltip = AppPropertiesManager.CONSTANTS.warning_NoRfree_text();
			warningLabel = createWarningLabel(AppPropertiesManager.CONSTANTS.warning_NoRfree_title(), warningTooltip);	
			warningCount++;
			multiWarningTooltip += "&bull; " + AppPropertiesManager.CONSTANTS.warning_NoRfree_title().replace("Warning: ", "") + "<br>" + AppPropertiesManager.CONSTANTS.warning_NoRfree_text() + "<br>";
		}
		if(this.isSpaceGroupWarning()){
			warningTooltip = AppPropertiesManager.CONSTANTS.warning_SpaceGroup_text();
			warningLabel = createWarningLabel(AppPropertiesManager.CONSTANTS.warning_SpaceGroup_title(), warningTooltip);	
			warningCount++;
			multiWarningTooltip += "&bull; " + AppPropertiesManager.CONSTANTS.warning_SpaceGroup_title().replace("Warning: ", "") + "<br>" + AppPropertiesManager.CONSTANTS.warning_SpaceGroup_text() + "<br>";
		}		
		if(warningCount > 1){
			warningLabel = createWarningLabel(warningCount + " Warnings", multiWarningTooltip);
		}
		return warningLabel;
	} 
	
	private LabelWithTooltip createWarningLabel(String text, String tooltipText){
		LabelWithTooltip label = new LabelWithTooltip("<img src='resources/icons/warning_icon_xl.png' style='height:17px' />&nbsp;*"+text+"*", tooltipText);
		label.addStyleName("eppic-header-warning");
		label.addStyleName("eppic-pdb-identifier-label");
		//this.staticWarningsLabel = label;
		return label;
	}
	
	
}
