package ch.systemsx.sybit.crkwebui.shared.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Settings stored on the server side and transfered to the client during initialization
 * @author srebniak_a
 *
 */
public class ApplicationSettings implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	// supported scoring methods
	private List<SupportedMethod> scoresTypes;

	// grid settings - column specification
	private Map<String, String> gridProperties;

	// input parameters
	private InputParameters defaultParametersValues;

	// available values for reduced alphabet
	private List<Integer> reducedAlphabetList;
	
	private List<String> searchModeList;
	
	// *********************************************
	// * Settings for captcha
	// *********************************************
	private int nrOfJobsForSession;
	private boolean useCaptcha;
	private String captchaPublicKey;
	private int nrOfAllowedSubmissionsWithoutCaptcha;
	// *********************************************
	
	private String resultsLocation;
	
	private Map<String, String> runParametersNames;
	
	// information which willbe displayed in the bottom panel when accessing the page - e.g. when the server will not be accessible
	// this information is loaded by each new access to the server so that it does not require the server to be restarted
	private String notificationOnStart;
	
	private String pdbLinkUrl;
	private String uniprotLinkUrl;
	
	public ApplicationSettings()
	{
		this.scoresTypes = new ArrayList<SupportedMethod>();
	}

	public void setScoresTypes(List<SupportedMethod> scoresTypes) {
		this.scoresTypes = scoresTypes;
	}

	public List<SupportedMethod> getScoresTypes() {
		return scoresTypes;
	}

	public void setGridProperties(Map<String, String> gridProperties) {
		this.gridProperties = gridProperties;
	}

	public Map<String, String> getGridProperties() {
		return gridProperties;
	}

	public void setDefaultParametersValues(
			InputParameters defaultParametersValues) {
		this.defaultParametersValues = defaultParametersValues;
	}

	public InputParameters getDefaultParametersValues() {
		return defaultParametersValues;
	}

	public void setReducedAlphabetList(List<Integer> reducedAlphabetList) {
		this.reducedAlphabetList = reducedAlphabetList;
	}

	public List<Integer> getReducedAlphabetList() {
		return reducedAlphabetList;
	}

	public void setNrOfJobsForSession(int nrOfJobsForSession) {
		this.nrOfJobsForSession = nrOfJobsForSession;
	}

	public int getNrOfJobsForSession() {
		return nrOfJobsForSession;
	}

	public String getResultsLocation() {
		return resultsLocation;
	}

	public void setResultsLocation(String resultsLocation) {
		this.resultsLocation = resultsLocation;
	}
	
	public boolean isUseCaptcha() {
		return useCaptcha;
	}

	public void setUseCaptcha(boolean useCaptcha) {
		this.useCaptcha = useCaptcha;
	}

	public String getCaptchaPublicKey() {
		return captchaPublicKey;
	}

	public void setCaptchaPublicKey(String captchaPublicKey) {
		this.captchaPublicKey = captchaPublicKey;
	}
	
	public void setNrOfAllowedSubmissionsWithoutCaptcha(
			int nrOfAllowedSubmissionsWithoutCaptcha) {
		this.nrOfAllowedSubmissionsWithoutCaptcha = nrOfAllowedSubmissionsWithoutCaptcha;
	}

	public int getNrOfAllowedSubmissionsWithoutCaptcha() {
		return nrOfAllowedSubmissionsWithoutCaptcha;
	}

	public void setRunParametersNames(Map<String, String> runParametersNames) {
		this.runParametersNames = runParametersNames;
	}

	public Map<String, String> getRunParametersNames() {
		return runParametersNames;
	}

	public void setNotificationOnStart(String notificationOnStart) {
		this.notificationOnStart = notificationOnStart;
	}

	public String getNotificationOnStart() {
		return notificationOnStart;
	}

	public void setPdbLinkUrl(String pdbLinkUrl) {
		this.pdbLinkUrl = pdbLinkUrl;
	}

	public String getPdbLinkUrl() {
		return pdbLinkUrl;
	}

	public void setSearchModeList(List<String> searchModeList) {
		this.searchModeList = searchModeList;
	}

	public List<String> getSearchModeList() {
		return searchModeList;
	}

	public void setUniprotLinkUrl(String uniprotLinkUrl) {
		this.uniprotLinkUrl = uniprotLinkUrl;
	}

	public String getUniprotLinkUrl() {
		return uniprotLinkUrl;
	}
}
