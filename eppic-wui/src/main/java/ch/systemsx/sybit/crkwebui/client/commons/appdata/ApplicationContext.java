package ch.systemsx.sybit.crkwebui.client.commons.appdata;

import java.util.Collections;
import java.util.List;

import eppic.dtomodel.ApplicationSettings;
import eppic.dtomodel.InterfaceCluster;
import eppic.dtomodel.ResiduesList;
import eppic.dtomodel.PdbInfo;
import eppic.dtomodel.WindowData;

/**
 * Application wide data.
 *
 */
public class ApplicationContext 
{
	/**
	 * Main application window data.
	 */
	private static WindowData windowData;
	
	/**
	 * Main application window data adjusted limitted by min values of width and height.
	 */
	private static WindowData adjustedWindowData = new WindowData();
	
	/**
	 * General application settings.
	 */
	private static ApplicationSettings settings;

	private static int selectedViewType;
	
	private static int selectedAssemblyId;
	
	/**
	 * Identifier of selected job.
	 */
	private static String selectedJobId;
	
	/**
	 * Selected interface.
	 */
	private static int selectedInterface;

	/**
	 * List of residues data for all interfaces for selected job.
	 */
	private static ResiduesList residuesForInterface;

	/**
	 * Selected pdb score item.
	 */
	private static PdbInfo pdbInfo;
	
	/**
	 * Flag pointing whether content of the status panel should be refreshed using timer.
	 */
	private static boolean doStatusPanelRefreshing = false;

	/**
	 * Nr of total submissions done by the user. This information is used to validate rights to do further submissions.
	 */
	private static int nrOfSubmissions = 0;
	
	/**
	 * Type of the viewer (jmol, local, pse, etc.)
	 */
	private static String selectedViewer = AppPropertiesManager.CONSTANTS.viewer_jmol();

	/**
	 * Flag pointing whether there is any job with status running or waiting for the session.
	 */
	private static boolean isAnyJobRunning;
	
	/**
	 * Retrieves main application window data.
	 * @return main application window data
	 */
	public static WindowData getWindowData() {
		return windowData;
	}
	
	/**
	 * Sets main application window data.
	 * @param windowData main application window data
	 */
	public static void setWindowData(WindowData windowData) {
		ApplicationContext.windowData = windowData;
	}
	
	/**
	 * Retrieves general application settings.
	 * @return general application settings
	 */
	public static ApplicationSettings getSettings() {
		return settings;
	}
	
	/**
	 * Sets general application settings.
	 * @param settings general application settings
	 */
	public static void setSettings(ApplicationSettings settings) {
		ApplicationContext.settings = settings;
	}
	
	/**
	 * Retrieves selected pdb score item.
	 * @return selected pdb score item
	 */
	public static PdbInfo getPdbInfo() {
		return pdbInfo;
	}
	
	/**
	 * Sets selected pdb score item.
	 * @param pdbInfo selected pdb score item
	 */
	public static void setPdbScoreItem(PdbInfo pdbScoreItem) {
		ApplicationContext.pdbInfo = pdbScoreItem;
		Collections.sort(pdbScoreItem.getInterfaceClusters());
	}
	
	/**
	 * Retrieves list of interface residues.
	 * @return interface residues
	 */
	public static ResiduesList getResiduesForInterface() {
		return residuesForInterface;
	}
	
	/**
	 * Sets residues list for interface.
	 * @param residuesForInterface residues list
	 */
	public static void setResiduesForInterface(
			ResiduesList residuesForInterface) {
		ApplicationContext.residuesForInterface = residuesForInterface;
	}
	
	/**
	 * Retrieves selected job identifier.
	 * @return job identifier
	 */
	public static String getSelectedJobId() {
		return selectedJobId;
	}
	
	/**
	 * Sets currently selected job identifier.
	 * @param selectedJobId job identifier
	 */
	public static void setSelectedJobId(String selectedJobId) {
		ApplicationContext.selectedJobId = selectedJobId;
	}
	
	/**
	 * Retrieves information whether content of status panel can be refreshed.
	 * @return information whether content of status panel can be refreshed
	 */
	public static boolean isDoStatusPanelRefreshing() {
		return doStatusPanelRefreshing;
	}
	
	/**
	 * Sets information whether content of status panel can be refreshed.
	 * @param doStatusPanelRefreshing information whether content of status panel can be refreshed
	 */
	public static void setDoStatusPanelRefreshing(boolean doStatusPanelRefreshing) {
		ApplicationContext.doStatusPanelRefreshing = doStatusPanelRefreshing;
	}
	
	/**
	 * Retrieves number of submitted jobs. This information is used in a case of using recaptcha protection to limit
	 * number of submissions.
	 * @return number of submitted jobs.
	 */
	public static int getNrOfSubmissions() {
		return nrOfSubmissions;
	}
	
	/**
	 * Sets number of submitted jobs.
	 * @param nrOfSubmissions number of submitted jobs
	 */
	public static void setNrOfSubmissions(int nrOfSubmissions) {
		ApplicationContext.nrOfSubmissions = nrOfSubmissions;
	}
	
	/**
	 * Retrieves selected viewer type.
	 * @return selected viewer type
	 */
	public static String getSelectedViewer() {
		return selectedViewer;
	}
	
	/**
	 * Sets selected viewer type.
	 * @param selectedViewer viewer type
	 */
	public static void setSelectedViewer(String selectedViewer) {
		ApplicationContext.selectedViewer = selectedViewer;
	}

	/**
	 * Sets selected interface id.
	 * @param selectedInterface selected interface id
	 */
	public static void setSelectedInterface(int selectedInterface) {
		ApplicationContext.selectedInterface = selectedInterface;
	}

	/**
	 * Retrieves selected interface id.
	 * @return selected interface id
	 */
	public static int getSelectedInterface() {
		return selectedInterface;
	}
	
	/**
	 * Retrieves selected view type.
	 * @return selected interface id
	 */
	public static int getSelectedViewType() {
		return selectedViewType;
	}
	
	public static void setSelectedViewType(int selectedViewType) {
		ApplicationContext.selectedViewType = selectedViewType;
	}
	
	/**
	 * Retrieves selected assembly.
	 * @return selected assembly id ; no selected assembly = -1
	 */
	public static int getSelectedAssemblyId() {
		return selectedAssemblyId;
	}
	
	public static void setSelectedAssemblyId(int selectedAssemblyId) {
		ApplicationContext.selectedAssemblyId = selectedAssemblyId;
	}
	
	/**
	 * Cleans interface residues.
	 */
	public static void cleanResiduesForInterface()
	{
		if(residuesForInterface != null)
		{
			residuesForInterface.clear();
			residuesForInterface = null;
		}
	}
	
	/**
	 * Retrieves information about user browser.
	 * @return information about browser of the user
	 */
	public native static String getUserAgent() /*-{
		return navigator.userAgent.toLowerCase();
	}-*/;
	
	/**
	 * Retrieves adjusted main application window data.
	 * @return adjusted main application window data
	 */
	public static WindowData getAdjustedWindowData() {
		return adjustedWindowData;
	}
	
	/**
	 * Sets adjusted main application window data.
	 * @param windowData adjusted main application window data
	 */
	public static void setAdjustedWindowData(WindowData adjustedWindowData) {
		ApplicationContext.adjustedWindowData = adjustedWindowData;
	}
	
	/**
	 * Sets stored value of height/width of the window based on the min allowed value and current height/width of 
	 * client window. This information is used to properly resize internal panels.
	 * 
	 * @param windowWidth width of client window
	 * @param windowHeight height of client window
	 */
	public static void adjustWindowData(int windowWidth,
										int windowHeight)
	{
		boolean addHorizontalScroll = false;
		boolean addVerticalScroll = false;
		
		if(windowHeight < settings.getScreenSettings().getMinWindowData().getWindowHeight())
		{
			windowHeight = settings.getScreenSettings().getMinWindowData().getWindowHeight();
			addVerticalScroll = true;
		}
		
		if(windowWidth < settings.getScreenSettings().getMinWindowData().getWindowWidth())
		{
			windowWidth = settings.getScreenSettings().getMinWindowData().getWindowWidth();
			addHorizontalScroll = true;
		}
		
		if(addVerticalScroll)
		{
			windowWidth -= 15;
		}
		
		if(addHorizontalScroll)
		{
			windowHeight -= 15;
		}
		
		adjustedWindowData.setWindowHeight(windowHeight);
		adjustedWindowData.setWindowWidth(windowWidth);
	}
	
	/**
	 * Retrieves information whether there is any job running/waiting for the session.
	 * @return information whether there is any job running/waiting for the session
	 */
	public static boolean isAnyJobRunning() {
		return isAnyJobRunning;
	}

	/**
	 * Sets information whether there is any job running/waiting for the session.
	 * @param isAnyJobRunning information whether there is any job running/waiting for the session.
	 */
	public static void setAnyJobRunning(boolean isAnyJobRunning) {
		ApplicationContext.isAnyJobRunning = isAnyJobRunning;
	}
	
	public static int getNumberOfInterfaces(){
		List<InterfaceCluster> clusters = pdbInfo.getInterfaceClusters();
		int count = 0;
		for(InterfaceCluster c : clusters){
			count = count + c.getInterfaces().size();
		}
		return count;
	}
	
}
