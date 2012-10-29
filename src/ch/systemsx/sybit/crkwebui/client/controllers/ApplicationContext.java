package ch.systemsx.sybit.crkwebui.client.controllers;

import ch.systemsx.sybit.crkwebui.shared.model.ApplicationSettings;
import ch.systemsx.sybit.crkwebui.shared.model.InterfaceResiduesItemsList;
import ch.systemsx.sybit.crkwebui.shared.model.PDBScoreItem;
import ch.systemsx.sybit.crkwebui.shared.model.WindowData;

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
	private static InterfaceResiduesItemsList residuesForInterface;

	/**
	 * Selected pdb score item.
	 */
	private static PDBScoreItem pdbScoreItem;
	
	
	
	/**
	 * Flag pointing whether my jobs panel is visible.
	 */
	private static boolean isMyJobsListVisible = true;
	
	private static int myJobsPanelWidth;
	
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
	public static PDBScoreItem getPdbScoreItem() {
		return pdbScoreItem;
	}
	
	/**
	 * Sets selected pdb score item.
	 * @param pdbScoreItem selected pdb score item
	 */
	public static void setPdbScoreItem(PDBScoreItem pdbScoreItem) {
		ApplicationContext.pdbScoreItem = pdbScoreItem;
	}
	
	/**
	 * Retrieves list of interface residues.
	 * @return interface residues
	 */
	public static InterfaceResiduesItemsList getResiduesForInterface() {
		return residuesForInterface;
	}
	
	/**
	 * Sets residues list for interface.
	 * @param residuesForInterface residues list
	 */
	public static void setResiduesForInterface(
			InterfaceResiduesItemsList residuesForInterface) {
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
	 * Sets information whether jobs panel is visible.
	 * @param isMyJobsListVisible information whether jobs panel is visible
	 */
	public static void setMyJobsListVisible(boolean isMyJobsListVisible) {
		ApplicationContext.isMyJobsListVisible = isMyJobsListVisible;
	}

	/**
	 * Retrieves information whether jobs list panel is visible
	 * @return information whether jobs list panel is visible
	 */
	public static boolean isMyJobsListVisible() {
		return isMyJobsListVisible;
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
	
	public static int getMyJobsPanelWidth() {
		return myJobsPanelWidth;
	}

	public static void setMyJobsPanelWidth(int myJobsPanelWidth) {
		ApplicationContext.myJobsPanelWidth = myJobsPanelWidth;
	}
	
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
	 * Sets stored value of width of the window based on the min allowed value and current width of 
	 * client window. This information is used to properly resize internal panels.
	 * 
	 * @param windowWidth width of client window
	 */
	public static void adjustWindowWidth(int windowWidth)
	{
		if(windowWidth < settings.getScreenSettings().getMinWindowData().getWindowWidth())
		{
			windowWidth = settings.getScreenSettings().getMinWindowData().getWindowWidth() - 25; 
		}
		
		adjustedWindowData.setWindowWidth(windowWidth);
	}
	
	/**
	 * Sets stored value of height of the window based on the min allowed value and current height of 
	 * client window. This information is used to properly resize internal panels.
	 * 
	 * @param windowHeight height of client window
	 */
	public static void adjustWindowHeight(int windowHeight)
	{
		if(windowHeight < settings.getScreenSettings().getMinWindowData().getWindowHeight())
		{
			windowHeight = settings.getScreenSettings().getMinWindowData().getWindowHeight() - 25; 
		}
		
		adjustedWindowData.setWindowHeight(windowHeight);
	}
}
