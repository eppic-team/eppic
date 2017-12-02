package ch.systemsx.sybit.crkwebui.shared.helpers;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import ch.systemsx.sybit.crkwebui.client.commons.appdata.AppPropertiesManager;
import ch.systemsx.sybit.crkwebui.client.commons.appdata.ApplicationContext;
import ch.systemsx.sybit.crkwebui.client.commons.gui.labels.LabelWithTooltip;
import ch.systemsx.sybit.crkwebui.client.commons.util.EscapedStringGenerator;
import eppic.EppicParams;

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
	private boolean nonStandardSg;
	private boolean nonStandardCoordFrameConvention;
	private int maxNumClashesAnyInterface;
	
	private static final String[] spaceGroups = {"P 1", "P 2", "P 21", "P 1 2 1", "P 1 21 1", "C 2", "C 1 2 1", "P 2 2 2", "P 2 2 21", "P 21 21 2",
									"P 21 21 21", "C 2 2 2", "C 2 2 21", "F 2 2 2", "I 2 2 2", "I 21 21 21", "P 4", "P 41", "P 42",
									"P 43", "I 4", "I 41", "P 4 2 2", "P 4 21 2", "P 41 2 2", "P 41 21 2", "P 42 2 2", "P 42 21 2",
									"P 43 2 2", "P 43 21 2", "I 4 2 2", "I 4 21 2", "P 3", "P 31", "P 32", "R 3", "P 3 1 2",
									"P 31 1 2", "P 32 1 2", "P 3 2 1", "P 31 2 1", "P 32 2 1", "R 3 2", "P 6", "P 61", "P 62",
									"P 63", "P 64", "P 65", "P 6 2 2", "P 61 2 2", "P 62 2 2", "P 63 2 2", "P 64 2 2", "P 65 2 2",
									"P 2 3", "P 21 3", "F 2 3", "I 2 3", "I 21 3", "P 4 3 2", "P 42 3 2", "P 43 3 2", "P 41 3 2",
									"F 4 3 2", "F 41 3 2", "I 4 3 2", "I 41 3 2", "I 41 2 2", "P 1 1 2", "P 1 1 21", "B 2", "B 1 1 2",
									"A 1 2 1", "C 1 21 1", "I 1 2 1", "I 2", "I 1 21 1", "P 21 2 2", "P 2 21 2", "P 21 2 21", "P 2 21 21",
									"H 3", "H 3 2"};
	public static final Set<String> spaceGroupsSet = new HashSet<>(Arrays.asList(spaceGroups));
	
	
	public ExperimentalWarnings(String spaceGroup,
	   String expMethod,
	   double resolution,
	   double rFree, 
	   boolean nonStandardSg,
	   boolean nonStandardCoordFrameConvention,
	   int maxNumClashesAnyInterface){
		// TODO we should try to set a constant "always-low-res exp method=ELECTRON MICROSCOPY". 
		// It's not ideal that the name is hard-coded
		emWarning = (expMethod!=null && expMethod.equals("ELECTRON MICROSCOPY") && (resolution <=0 || resolution>=99));
		resolutionWarning = (ApplicationContext.getSettings().getResolutionCutOff() > 0 && 
				             resolution > ApplicationContext.getSettings().getResolutionCutOff() && resolution > 0 && resolution<99);
		rfreeWarning = (ApplicationContext.getSettings().getRfreeCutOff() > 0 && 
				        rFree > ApplicationContext.getSettings().getRfreeCutOff() && rFree > 0 && rFree<1);
		noRfreeWarning = (rFree == 1 && expMethod.equals("X-RAY DIFFRACTION"));
		spaceGroupWarning = (expMethod!=null && expMethod.equals("X-RAY DIFFRACTION") && spaceGroup!=null && !spaceGroupsSet.contains(spaceGroup));
		this.nonStandardSg = nonStandardSg;
		this.nonStandardCoordFrameConvention = nonStandardCoordFrameConvention;
		this.maxNumClashesAnyInterface = maxNumClashesAnyInterface;
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
	
	/**
	 * @return the maxNumClashesAnyInterface
	 */
	public int getMaxNumClashesAnyInterface() {
		return maxNumClashesAnyInterface;
	}

	/**
	 * @param maxNumClashesAnyInterface the maxNumClashesAnyInterface to set
	 */
	public void setMaxNumClashesAnyInterface(int maxNumClashesAnyInterface) {
		this.maxNumClashesAnyInterface = maxNumClashesAnyInterface;
	}

	//for single and multiple warnings
	public LabelWithTooltip getWarningLabel(){
		LabelWithTooltip warningLabel = null;
		
		List<String> titles = new ArrayList<>();
		List<String> texts = new ArrayList<>();
		
		if (this.isEmWarning()) {
			titles.add(AppPropertiesManager.CONSTANTS.warning_EM_title());
			texts.add(AppPropertiesManager.CONSTANTS.warning_EM_text());
		}
		if(this.isResolutionWarning()) {
			titles.add(AppPropertiesManager.CONSTANTS.warning_LowRes_title());
			texts.add(AppPropertiesManager.CONSTANTS.warning_LowRes_text());
		}
		if(this.isRfreeWarning()){
			titles.add(AppPropertiesManager.CONSTANTS.warning_HighRfree_title());
			texts.add(AppPropertiesManager.CONSTANTS.warning_HighRfree_text());
		}
		if(this.isNoRfreeWarning()){
			titles.add(AppPropertiesManager.CONSTANTS.warning_NoRfree_title());
			texts.add(AppPropertiesManager.CONSTANTS.warning_NoRfree_text());
		}
		if(this.isSpaceGroupWarning()){
			titles.add(AppPropertiesManager.CONSTANTS.warning_SpaceGroup_title());
			texts.add(AppPropertiesManager.CONSTANTS.warning_SpaceGroup_text());			
		}
		if(this.nonStandardSg){
			titles.add(AppPropertiesManager.CONSTANTS.warning_NonStandardSg_title());
			texts.add(AppPropertiesManager.CONSTANTS.warning_NonStandardSg_text());			
		}
		if(this.nonStandardCoordFrameConvention){
			titles.add(AppPropertiesManager.CONSTANTS.warning_NonStandardCoordFrameConvention_title());
			texts.add(AppPropertiesManager.CONSTANTS.warning_NonStandardCoordFrameConvention_text());
		}
		if(this.maxNumClashesAnyInterface>EppicParams.NUM_CLASHES_FOR_ERROR){
			titles.add(AppPropertiesManager.CONSTANTS.warning_TooManyClashes_title());
			texts.add(AppPropertiesManager.CONSTANTS.warning_TooManyClashes_text());
		}
		
		String tooltipText = generateWarningsTemplate(titles, texts);
		if(titles.size() > 1){
			warningLabel = createWarningLabel(titles.size() + " Warnings", tooltipText);
		} else {
			warningLabel = createWarningLabel("Warning", tooltipText);
		}
		return warningLabel;
	} 
	
	private LabelWithTooltip createWarningLabel(String text, String tooltipText){
		LabelWithTooltip label = new LabelWithTooltip("<img src='resources/icons/warning_icon_xl.png' style='height:17px' />&nbsp;*"+text+"*", tooltipText);
		label.addStyleName("eppic-header-warning");
		label.addStyleName("eppic-pdb-identifier-label");
		return label;
	}
	
	private String generateWarningsTemplate(List<String> titles, List<String> texts) {
		if (titles.size()!=texts.size()) return "";
		
		StringBuilder warningsList = new StringBuilder("<div><ul class=\"eppic-default-font eppic-results-grid-tooltip eppic-tooltip-list\">");
		
		for(int i=0; i<titles.size();i++) {
			if(!titles.get(i).equals("")) {
				warningsList.append("<li>");
				warningsList.append("<strong>");
				warningsList.append(EscapedStringGenerator.generateSanitizedString(titles.get(i)));
				warningsList.append("</strong>");
				warningsList.append("<br>");
				warningsList.append(EscapedStringGenerator.generateSanitizedString(texts.get(i)));
				warningsList.append("</li>");
			}
		}
			
		warningsList.append("</ul></div>");
		
		return warningsList.toString();
	}
	
}
