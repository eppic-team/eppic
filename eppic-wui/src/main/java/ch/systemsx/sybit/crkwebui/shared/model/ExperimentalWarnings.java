package ch.systemsx.sybit.crkwebui.shared.model;

import ch.systemsx.sybit.crkwebui.client.commons.appdata.ApplicationContext;

/**
 * Class to determine the warnings on methods, resolution and rfree for a pdb
 * @author biyani_n
 *
 */
public class ExperimentalWarnings {
	
	private boolean emWarning;
	private boolean resolutionWarning;
	private boolean rfreeWarning;

	public ExperimentalWarnings(String spaceGroup,
	   String expMethod,
	   double resolution,
	   double rFree){
		
		// TODO we should try to set a constant "always-low-res exp method=ELECTRON MICROSCOPY". 
		// It's not ideal that the name is hard-coded
		emWarning = (expMethod!=null && expMethod.equals("ELECTRON MICROSCOPY") && resolution <=0);
		resolutionWarning = (ApplicationContext.getSettings().getResolutionCutOff() > 0 && 
				             resolution > ApplicationContext.getSettings().getResolutionCutOff() && resolution > 0);
		rfreeWarning = (ApplicationContext.getSettings().getRfreeCutOff() > 0 && 
				        rFree > ApplicationContext.getSettings().getRfreeCutOff() && rFree > 0);
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
	
	
	
}
