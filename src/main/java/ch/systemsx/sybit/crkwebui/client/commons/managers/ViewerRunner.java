package ch.systemsx.sybit.crkwebui.client.commons.managers;

import ch.systemsx.sybit.crkwebui.client.commons.appdata.AppPropertiesManager;
import ch.systemsx.sybit.crkwebui.client.commons.appdata.ApplicationContext;
import ch.systemsx.sybit.crkwebui.client.commons.events.ShowErrorEvent;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.Window;

/**
 * 3D viewer runner.
 */
public class ViewerRunner 
{
	/**
	 * Starts selected viewer. Type of the viewer is determined based on the option selected in viewer selector.
	 * @param interfaceId identifier of the interface
	 */
	public static void runViewer(String interfaceId)
	{
		if(ApplicationContext.getSelectedViewer().equals(AppPropertiesManager.CONSTANTS.viewer_jmol()))
		{
			showJmolViewer(interfaceId);
		}
		else if(ApplicationContext.getSelectedViewer().equals(AppPropertiesManager.CONSTANTS.viewer_local()))
		{
			downloadFileFromServer("interface", interfaceId);
		}
		else if(ApplicationContext.getSelectedViewer().equals(AppPropertiesManager.CONSTANTS.viewer_pse()))
		{
			downloadFileFromServer("pse", interfaceId);
		}
		else
		{
			EventBusManager.EVENT_BUS.fireEvent(new ShowErrorEvent("No viewer selected"));
		}
	}

	/**
	 * Displays jmol viewer.
	 * @param interfaceNr interface identifier
	 */
	private static void showJmolViewer(String interfaceNr)
	{
		int size = ApplicationContext.getWindowData().getWindowHeight() - 20;
		if(size > ApplicationContext.getWindowData().getWindowWidth() - 20)
		{
			size = ApplicationContext.getWindowData().getWindowWidth() - 20;
		}
		
		int jmolAppletSize = size - 40;
		
		String jmolViewerUrl = GWT.getModuleBaseURL() + "jmolViewer";
		jmolViewerUrl += "?id=" + ApplicationContext.getPdbScoreItem().getJobId() + 
						 "&input=" + ApplicationContext.getPdbScoreItem().getPdbName() + 
						 "&interface=" + interfaceNr +
						 "&size=" + jmolAppletSize;
		
		Window.open(jmolViewerUrl, "", "width=" + size + "," +
										"height=" + size);

	}

	/**
	 * Downloads file from the server.
	 * @param type type of the file to download
	 * @param interfaceId identifier of the interface
	 */
	private static void downloadFileFromServer(String type, String interfaceId)
	{
		String fileDownloadServletUrl = GWT.getModuleBaseURL() + "fileDownload";
		fileDownloadServletUrl += "?type=" + type + "&id=" + ApplicationContext.getPdbScoreItem().getJobId() + "&interface=" + interfaceId;
		Window.open(fileDownloadServletUrl, "", "");
	}
}
